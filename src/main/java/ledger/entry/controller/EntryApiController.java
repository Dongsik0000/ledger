package ledger.entry.controller;

import jakarta.servlet.http.HttpSession;
import ledger.account.service.AccountService;
import ledger.asset.service.AssetService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.LikeUtil;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.cycle.PayCycle;
import ledger.entry.service.EntryService;
import ledger.holiday.service.HolidayService;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 거래: 달력 월 목록·필터·검색·추가(여러 건 포함)·수정·삭제. 입력 검사는 여기, 자산 잔액 반영과 트랜잭션은 EntryServiceImpl.
@Controller
@RequestMapping("/ledger/entry")
public class EntryApiController {

    public static final long AMOUNT_MAX = 99_999_999_999L;
    private static final int TITLE_MAX = 100;   // ledger_entry.title VARCHAR(100)
    private static final int MEMO_MAX = 500;    // ledger_entry.memo VARCHAR(500)
    private static final int KEYWORD_MAX = 50;
    private static final Set<String> TYPES = Set.of("EXPENSE", "INCOME");
    // 조회 기간: 달력 월 / 주기(date 가 속한 급여 주기) / 최근 12개월(11개월 전 1일부터) / 전체.
    // 넓은 기간(최근 12개월·전체)은 최근 거래부터 WIDE_LIMIT 건까지만 보낸다
    private static final Set<String> PERIODS = Set.of("MONTH", "CYCLE", "RECENT12", "ALL");
    private static final int WIDE_LIMIT = 500;
    private static final int BATCH_MAX = 50;
    private static final int SUGGEST_LIMIT = 200;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Autowired
    private EntryService entryService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private HolidayService holidayService;

    // 거래 내역 화면
    @GetMapping
    public String entry() {
        return "entry/entryMain";
    }

    // 기간(달력 월·주기·최근 12개월·전체)과 필터로 거래 목록·합계
    @ResponseBody
    @PostMapping("/list")
    public Response list(@RequestBody HashMap<String, Object> param, HttpSession session) {
        String period = ParamUtil.has(param, "period") ? ParamUtil.str(param, "period") : "MONTH";
        YearMonth month = ParamUtil.month(param, "month");
        LocalDate date = ParamUtil.has(param, "date") ? ParamUtil.date(param, "date") : LocalDate.now(SEOUL);
        String type = ParamUtil.str(param, "type");
        Long categoryId = ParamUtil.lng(param, "categoryId");
        Long paymentMethodId = ParamUtil.lng(param, "paymentMethodId");
        String keyword = ParamUtil.str(param, "keyword");

        if (!PERIODS.contains(period) || ("MONTH".equals(period) && month == null) || date == null || (!type.isEmpty() && !TYPES.contains(type))
                || (ParamUtil.has(param, "categoryId") && categoryId == null)
                || (ParamUtil.has(param, "paymentMethodId") && paymentMethodId == null)
                || keyword.length() > KEYWORD_MAX) {
            return Response.invalid("조회 조건을 확인해 주세요.");
        }

        LocalDate from = null;
        LocalDate to = null;
        if ("MONTH".equals(period)) {
            from = month.atDay(1);
            to = month.atEndOfMonth();
        } else if ("RECENT12".equals(period)) {
            from = YearMonth.now(SEOUL).minusMonths(11).atDay(1);
        }
        Map<String, Object> data = new HashMap<>();
        if ("CYCLE".equals(period)) {
            long userId = SessionUtil.getUserId(session);
            Map<String, Object> setting = settingsService.selectUserSetting(userId);
            int payDay = ((Number) setting.get("payDay")).intValue();
            boolean adjust = Boolean.TRUE.equals(setting.get("payDayAdjust"));
            Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                    "from", date.minusMonths(3).withDayOfMonth(1), "to", date.plusMonths(3))));
            PayCycle.Cycle cycle = PayCycle.cycleOf(date, payDay, adjust, holidays);
            from = cycle.start();
            to = cycle.end();
            data.put("cycleMonth", PayCycle.baseMonth(cycle, payDay, adjust, holidays).toString());
            // 보정은 등록된 공휴일만 안다. 주기 연도의 공휴일이 하나도 없으면 주말만 반영됐다고 알린다
            data.put("holidayMissing", adjust && holidays.stream().noneMatch(d -> d.getYear() == cycle.start().getYear()));
        }
        Map<String, Object> query = ParamUtil.map(
                "userId", SessionUtil.getUserId(session), "from", from, "to", to,
                "type", type.isEmpty() ? null : type,
                "categoryId", categoryId, "paymentMethodId", paymentMethodId,
                "keyword", LikeUtil.contains(keyword),
                "limit", "MONTH".equals(period) || "CYCLE".equals(period) ? null : WIDE_LIMIT + 1);

        // 합계는 잘리지 않은 전체 조건 기준
        List<Map<String, Object>> rows = entryService.selectEntryList(query);
        boolean truncated = rows.size() > WIDE_LIMIT;
        data.put("rows", truncated ? rows.subList(0, WIDE_LIMIT) : rows);
        data.put("truncated", truncated);
        data.put("totals", entryService.selectEntrySum(query));
        data.put("from", from == null ? null : from.toString());
        data.put("to", to == null ? null : to.toString());
        return Response.of(Constants.SUCCESS, data);
    }

    // 내용 자동 채우기: 최근 1년 거래의 내용별 가장 최근 구분·카테고리·결제수단·통장·금액
    @ResponseBody
    @PostMapping("/titles")
    public Response titles(HttpSession session) {
        return Response.of(Constants.SUCCESS, entryService.selectTitleSuggestions(ParamUtil.map(
                "userId", SessionUtil.getUserId(session), "from", LocalDate.now(SEOUL).minusYears(1), "limit", SUGGEST_LIMIT)));
    }

    // 거래 한 건 추가·수정
    @ResponseBody
    @PostMapping("/save")
    public Response save(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Map<String, Object> entry = new HashMap<>();
        Response error = toEntry(param, SessionUtil.getUserId(session), entry);
        if (error != null) {
            return error;
        }

        try {
            if (entry.get("id") == null) {
                entryService.insertEntry(entry);
            } else if (entryService.updateEntry(entry) == 0) {
                return Response.of(Constants.NOT_FOUND);
            }
        } catch (IllegalStateException e) {
            return Response.invalid("자산 잔액을 반영할 수 없어요. 연결된 자산과 금액을 확인해 주세요.");
        }
        return Response.of(Constants.SUCCESS);
    }

    // 여러 건 추가: 모든 줄을 먼저 검사하고, 하나라도 틀리면 몇 번째 줄인지 알려 주고 아무것도 저장하지 않는다.
    // 저장은 한 트랜잭션(EntryServiceImpl.insertEntries)이라 자산 반영이 중간에 실패해도 전부 되돌린다
    @ResponseBody
    @PostMapping("/save-batch")
    @SuppressWarnings("unchecked")
    public Response saveBatch(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        if (!(param.get("rows") instanceof List<?> rows) || rows.isEmpty() || rows.size() > BATCH_MAX) {
            return Response.invalid("한 번에 1~" + BATCH_MAX + "건까지 저장할 수 있어요.");
        }
        List<Map<String, Object>> entries = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> entry = new HashMap<>();
            Response error = rows.get(i) instanceof Map<?, ?> row && !ParamUtil.has((Map<String, Object>) row, "id")
                    ? toEntry((Map<String, Object>) row, userId, entry)
                    : Response.invalid("새 거래만 추가할 수 있어요.");
            if (error != null) {
                return Response.invalid((i + 1) + "번째 줄: " + error.getMessage());
            }
            entries.add(entry);
        }

        try {
            entryService.insertEntries(entries);
        } catch (IllegalStateException e) {
            return Response.invalid("자산 잔액을 반영할 수 없어 저장하지 않았어요. 연결된 자산과 금액을 확인해 주세요.");
        }
        return Response.of(Constants.SUCCESS, Map.of("count", entries.size()));
    }

    // 거래 한 건 입력 검사. 문제가 없으면 entry 에 저장할 값을 담고 null, 있으면 그 응답
    private Response toEntry(Map<String, Object> param, long userId, Map<String, Object> entry) {
        Long id = ParamUtil.lng(param, "id");
        LocalDate entryDate = ParamUtil.date(param, "entryDate");
        String type = ParamUtil.str(param, "type");
        Long categoryId = ParamUtil.lng(param, "categoryId");
        String title = ParamUtil.str(param, "title");
        Long amount = ParamUtil.lng(param, "amount");
        Long paymentMethodId = ParamUtil.lng(param, "paymentMethodId");
        Long transferAssetId = ParamUtil.lng(param, "transferAssetId");
        Long accountId = ParamUtil.lng(param, "accountId");
        String memo = ParamUtil.str(param, "memo");

        if ((ParamUtil.has(param, "id") && id == null) || entryDate == null
                || (ParamUtil.has(param, "accountId") && accountId == null) || !TYPES.contains(type)
                || categoryId == null || title.isEmpty() || title.length() > TITLE_MAX
                || amount == null || amount < 1 || amount > AMOUNT_MAX
                || (ParamUtil.has(param, "paymentMethodId") && paymentMethodId == null)
                || (ParamUtil.has(param, "transferAssetId") && transferAssetId == null)
                || (transferAssetId != null && !"EXPENSE".equals(type))
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

        if (transferAssetId != null && assetService.selectCashAsset(ParamUtil.map("id", transferAssetId, "userId", userId)) == null) {
            return Response.invalid("이체할 적금·예금 자산을 다시 골라 주세요.");
        }
        if (accountId != null && accountService.selectAccount(ParamUtil.map("id", accountId, "userId", userId)) == null) {
            return Response.invalid("통장을 다시 골라 주세요.");
        }
        entry.putAll(ParamUtil.map("userId", userId, "id", id, "entryDate", entryDate, "type", type,
                "categoryId", categoryId, "title", title, "amount", amount,
                "paymentMethodId", paymentMethodId, "memo", memo.isEmpty() ? null : memo,
                "transferAssetId", transferAssetId, "accountId", accountId));
        return null;
    }

    // 거래 삭제. 연결 자산 잔액도 되돌린다
    @ResponseBody
    @PostMapping("/delete")
    public Response delete(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 거래를 확인해 주세요.");
        }
        // 고정 항목이 만든 거래면 recurring_run.entry_id 는 FK 로 NULL 이 되어 그 달은 다시 생성되지 않는다(건너뛰기)
        try {
            if (entryService.deleteEntry(ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session))) == 0) {
                return Response.of(Constants.NOT_FOUND);
            }
        } catch (IllegalStateException e) {
            return Response.invalid("연결된 자산 잔액을 되돌릴 수 없어요. 자산 잔액을 확인해 주세요.");
        }
        return Response.of(Constants.SUCCESS);
    }
}
