package ledger.entry.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.LikeUtil;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.entry.service.EntryService;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// 거래: 달력 월 목록·필터·검색·추가·수정·삭제. 로직은 여기, EntryServiceImpl 은 DAO 연결만.
@Controller
@RequestMapping("/ledger/entry")
public class EntryApiController {

    public static final long AMOUNT_MAX = 99_999_999_999L;
    private static final int TITLE_MAX = 100;   // ledger_entry.title VARCHAR(100)
    private static final int MEMO_MAX = 500;    // ledger_entry.memo VARCHAR(500)
    private static final int KEYWORD_MAX = 50;
    private static final Set<String> TYPES = Set.of("EXPENSE", "INCOME");

    @Autowired
    private EntryService entryService;

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public String entry() {
        return "entry/entryMain";
    }

    @ResponseBody
    @PostMapping("/list")
    public Response list(@RequestBody HashMap<String, Object> param, HttpSession session) {
        YearMonth month = ParamUtil.month(param, "month");
        String type = ParamUtil.str(param, "type");
        Long categoryId = ParamUtil.lng(param, "categoryId");
        Long paymentMethodId = ParamUtil.lng(param, "paymentMethodId");
        String keyword = ParamUtil.str(param, "keyword");

        if (month == null || (!type.isEmpty() && !TYPES.contains(type))
                || (ParamUtil.has(param, "categoryId") && categoryId == null)
                || (ParamUtil.has(param, "paymentMethodId") && paymentMethodId == null)
                || keyword.length() > KEYWORD_MAX) {
            return Response.invalid("조회 조건을 확인해 주세요.");
        }

        return Response.of(Constants.SUCCESS, entryService.selectEntryList(ParamUtil.map(
                "userId", SessionUtil.getUserId(session),
                "from", month.atDay(1), "to", month.atEndOfMonth(),
                "type", type.isEmpty() ? null : type,
                "categoryId", categoryId, "paymentMethodId", paymentMethodId,
                "keyword", LikeUtil.contains(keyword))));
    }

    @ResponseBody
    @PostMapping("/save")
    public Response save(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        LocalDate entryDate = ParamUtil.date(param, "entryDate");
        String type = ParamUtil.str(param, "type");
        Long categoryId = ParamUtil.lng(param, "categoryId");
        String title = ParamUtil.str(param, "title");
        Long amount = ParamUtil.lng(param, "amount");
        Long paymentMethodId = ParamUtil.lng(param, "paymentMethodId");
        String memo = ParamUtil.str(param, "memo");

        if ((ParamUtil.has(param, "id") && id == null) || entryDate == null || !TYPES.contains(type)
                || categoryId == null || title.isEmpty() || title.length() > TITLE_MAX
                || amount == null || amount < 1 || amount > AMOUNT_MAX
                || (ParamUtil.has(param, "paymentMethodId") && paymentMethodId == null)
                || memo.length() > MEMO_MAX) {
            return Response.invalid("날짜·금액(1원~999억 원 미만)·카테고리·내용(100자 이하)·메모(500자 이하)를 확인해 주세요.");
        }

        // 카테고리·결제수단은 같은 사용자 것이어야 하고, 카테고리는 거래 구분과 같아야 한다
        Map<String, Object> category = settingsService.selectCategory(ParamUtil.map("id", categoryId, "userId", userId));
        if (category == null || !type.equals(category.get("type"))) {
            return Response.invalid("카테고리를 다시 골라 주세요.");
        }
        if (paymentMethodId != null
                && settingsService.selectPayment(ParamUtil.map("id", paymentMethodId, "userId", userId)) == null) {
            return Response.invalid("결제수단을 다시 골라 주세요.");
        }

        Map<String, Object> entry = ParamUtil.map("userId", userId, "id", id, "entryDate", entryDate, "type", type,
                "categoryId", categoryId, "title", title, "amount", amount,
                "paymentMethodId", paymentMethodId, "memo", memo.isEmpty() ? null : memo);

        if (id == null) {
            entryService.insertEntry(entry);
        } else if (entryService.updateEntry(entry) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/delete")
    public Response delete(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 거래를 확인해 주세요.");
        }
        // 고정 항목이 만든 거래면 recurring_run.entry_id 는 FK 로 NULL 이 되어 그 달은 다시 생성되지 않는다(건너뛰기)
        if (entryService.deleteEntry(ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session))) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }
}
