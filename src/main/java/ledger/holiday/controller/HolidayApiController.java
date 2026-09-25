package ledger.holiday.controller;

import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.holiday.HolidayApiClient;
import ledger.holiday.HolidayXmlParser;
import ledger.holiday.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 공휴일(전 사용자 공용): 연도별 목록·직접 추가·삭제·공공데이터포털 가져오기. 화면은 설정(/ledger/settings) 안에 있다.
@Controller
@RequestMapping("/ledger/holiday")
public class HolidayApiController {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int NAME_MAX = 50;   // holiday.name VARCHAR(50)

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private HolidayApiClient holidayApiClient;

    // 그 해 공휴일 + 가져오기 가능 여부(키 값은 내보내지 않는다) + 고를 수 있는 연도
    @ResponseBody
    @PostMapping("/list")
    public Response list(@RequestBody HashMap<String, Object> param) {
        int thisYear = LocalDate.now(SEOUL).getYear();
        Integer year = ParamUtil.has(param, "year") ? ParamUtil.integer(param, "year") : Integer.valueOf(thisYear);
        if (year == null || year < 2000 || year > 2100) {
            return Response.invalid("연도를 확인해 주세요.");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("holidays", holidayService.selectHolidayList(ParamUtil.map(
                "from", LocalDate.of(year, 1, 1), "to", LocalDate.of(year, 12, 31))));
        data.put("keyConfigured", holidayApiClient.isConfigured());
        data.put("years", List.of(thisYear - 1, thisYear, thisYear + 1, thisYear + 2));
        return Response.of(Constants.SUCCESS, data);
    }

    @ResponseBody
    @PostMapping("/save")
    public Response save(@RequestBody HashMap<String, Object> param) {
        LocalDate date = ParamUtil.date(param, "date");
        String name = ParamUtil.str(param, "name");
        if (date == null || name.isEmpty() || name.length() > NAME_MAX) {
            return Response.invalid("날짜와 이름(50자 이하)을 확인해 주세요.");
        }
        if (holidayService.countHoliday(date) > 0) {
            return Response.of(Constants.DUPLICATE, "그 날짜에는 이미 공휴일이 있어요.", null);
        }
        holidayService.insertHoliday(ParamUtil.map("date", date, "name", name));
        return Response.of(Constants.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/delete")
    public Response delete(@RequestBody HashMap<String, Object> param) {
        LocalDate date = ParamUtil.date(param, "date");
        if (date == null) {
            return Response.invalid("삭제할 날짜를 확인해 주세요.");
        }
        if (holidayService.deleteHoliday(date) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    // 공공데이터포털에서 그 해 공휴일을 받아 저장(같은 날짜는 이름 갱신).
    // 외부 호출 동안 DB 커넥션을 잡지 않도록 트랜잭션 없이 한 건씩 저장한다(다시 가져와도 결과가 같다)
    @ResponseBody
    @PostMapping("/import")
    public Response importYear(@RequestBody HashMap<String, Object> param) {
        int thisYear = LocalDate.now(SEOUL).getYear();
        Integer year = ParamUtil.integer(param, "year");
        if (year == null || year < thisYear - 1 || year > thisYear + 2) {
            return Response.invalid("가져올 연도는 " + (thisYear - 1) + "~" + (thisYear + 2) + "년 중에서 골라 주세요.");
        }
        if (!holidayApiClient.isConfigured()) {
            return Response.of(Constants.HOLIDAY_KEY_MISSING, "공휴일 API 키가 설정되지 않았어요.", null);
        }
        List<HolidayXmlParser.Holiday> holidays = holidayApiClient.fetch(year);
        if (holidays == null) {
            return Response.of(Constants.HOLIDAY_API_FAIL, "공휴일 정보를 가져오지 못했어요. 잠시 뒤 다시 시도하거나 키를 확인해 주세요.", null);
        }
        for (HolidayXmlParser.Holiday h : holidays) {
            String name = h.name().length() > NAME_MAX ? h.name().substring(0, NAME_MAX) : h.name();
            holidayService.upsertHoliday(ParamUtil.map("date", h.date(), "name", name));
        }
        return Response.of(Constants.SUCCESS, Map.of("count", holidays.size()));
    }
}
