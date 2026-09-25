package ledger.recurring.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.cycle.PayCycle;
import ledger.entry.controller.EntryApiController;
import ledger.holiday.service.HolidayService;
import ledger.recurring.RecurringGenerator;
import ledger.recurring.service.RecurringService;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// 고정 항목: 목록(이번 주기 상태)·추가·수정·활성 전환·삭제. 저장·활성화 직후 RecurringGenerator 로 결제일 처리.
@Controller
@RequestMapping("/ledger/recurring")
public class RecurringApiController {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int NAME_MAX = 100;   // recurring_item.name VARCHAR(100)
    private static final int MEMO_MAX = 500;
    private static final Set<String> TYPES = Set.of("EXPENSE", "INCOME");
    private static final Set<String> ADJUSTS = Set.of("NONE", "PREV_BIZ", "NEXT_BIZ");

    @Autowired
    private RecurringService recurringService;

    @Autowired
    private RecurringGenerator recurringGenerator;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private HolidayService holidayService;

    @GetMapping
    public String recurring() {
        return "recurring/recurringMain";
    }

    // 항목 + 이번 주기 상태(DONE 처리됨 / SKIPPED 거래 없이 건너뜀(등록 전 지난 결제일·생성된 거래 삭제) /
    //                     PLANNED 예정 / NONE 이번 주기 결제일 없음 / INACTIVE)
    // 지표: monthlyFixed(활성 지출 합), recorded(이번 주기에 기록된 지출). 아직 예정인 금액은 대시보드 summary 의 pendingFixed
    @ResponseBody
    @PostMapping("/list")
    public Response list(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        LocalDate today = LocalDate.now(SEOUL);
        Map<String, Object> setting = settingsService.selectUserSetting(userId);
        Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                "from", today.minusMonths(3).withDayOfMonth(1), "to", today.plusMonths(3))));
        PayCycle.Cycle cycle = PayCycle.cycleOf(today, ((Number) setting.get("payDay")).intValue(),
                Boolean.TRUE.equals(setting.get("payDayAdjust")), holidays);

        List<Map<String, Object>> items = recurringService.selectItemList(userId);
        Map<Long, List<PayCycle.Due>> duesByItem = new HashMap<>();
        Set<String> months = new TreeSet<>();
        for (Map<String, Object> item : items) {
            List<PayCycle.Due> dues = PayCycle.duesInCycle(cycle,
                    ((Number) item.get("dayOfMonth")).intValue(), (String) item.get("adjust"), holidays);
            duesByItem.put(id(item), dues);
            dues.forEach(d -> months.add(d.month().toString()));
        }

        // "항목id|YYYY-MM" → 연결된 거래 금액(건너뜀·거래 삭제면 null)
        Map<String, Object> runs = new HashMap<>();
        if (!months.isEmpty()) {
            for (Map<String, Object> run : recurringService.selectRuns(ParamUtil.map("userId", userId, "months", List.copyOf(months)))) {
                runs.put(run.get("recurringId") + "|" + run.get("periodYm"), run.get("amount"));
            }
        }

        long monthlyFixed = 0;
        long recorded = 0;
        for (Map<String, Object> item : items) {
            boolean active = Boolean.TRUE.equals(item.get("active"));
            boolean expense = "EXPENSE".equals(item.get("type"));
            List<PayCycle.Due> dues = duesByItem.get(id(item));
            boolean allHandled = !dues.isEmpty();   // 이번 주기 결제일에 모두 처리 기록이 있음
            boolean skipped = false;                // 처리 기록은 있지만 거래가 없음(건너뜀 또는 거래 삭제)
            List<String> dueDates = new ArrayList<>();
            for (PayCycle.Due due : dues) {
                String key = id(item) + "|" + due.month();
                dueDates.add(due.date().toString());
                if (!runs.containsKey(key)) {
                    allHandled = false;
                } else if (runs.get(key) == null) {
                    skipped = true;
                } else if (active && expense) {
                    recorded += ((Number) runs.get(key)).longValue();
                }
            }
            if (active && expense) {
                monthlyFixed += ((Number) item.get("amount")).longValue();
            }
            item.put("dueDates", dueDates);
            item.put("status", !active ? "INACTIVE" : dues.isEmpty() ? "NONE"
                    : !allHandled ? "PLANNED" : skipped ? "SKIPPED" : "DONE");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("monthlyFixed", monthlyFixed);
        data.put("recorded", recorded);
        return Response.of(Constants.SUCCESS, data);
    }

    @Transactional
    @ResponseBody
    @PostMapping("/save")
    public Response save(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        String name = ParamUtil.str(param, "name");
        String type = ParamUtil.str(param, "type");
        Long amount = ParamUtil.lng(param, "amount");
        Long categoryId = ParamUtil.lng(param, "categoryId");
        Long paymentMethodId = ParamUtil.lng(param, "paymentMethodId");
        Integer dayOfMonth = ParamUtil.integer(param, "dayOfMonth");
        String adjust = ParamUtil.has(param, "adjust") ? ParamUtil.str(param, "adjust") : "NONE";
        String memo = ParamUtil.str(param, "memo");
        Boolean active = ParamUtil.has(param, "active") ? ParamUtil.bool(param, "active") : Boolean.TRUE;

        if ((ParamUtil.has(param, "id") && id == null) || name.isEmpty() || name.length() > NAME_MAX
                || !TYPES.contains(type) || amount == null || amount < 1 || amount > EntryApiController.AMOUNT_MAX
                || categoryId == null || (ParamUtil.has(param, "paymentMethodId") && paymentMethodId == null)
                || dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 31 || !ADJUSTS.contains(adjust)
                || memo.length() > MEMO_MAX || active == null) {
            return Response.invalid("항목명(100자 이하)·금액(1원~999억 원 미만)·결제일(1~31일)·카테고리를 확인해 주세요.");
        }

        Map<String, Object> category = settingsService.selectCategory(ParamUtil.map("id", categoryId, "userId", userId));
        if (category == null || !type.equals(category.get("type"))) {
            return Response.invalid("카테고리를 다시 골라 주세요.");
        }
        if (paymentMethodId != null
                && settingsService.selectPayment(ParamUtil.map("id", paymentMethodId, "userId", userId)) == null) {
            return Response.invalid("결제수단을 다시 골라 주세요.");
        }

        Map<String, Object> item = ParamUtil.map("userId", userId, "id", id, "name", name, "type", type,
                "amount", amount, "categoryId", categoryId, "paymentMethodId", paymentMethodId,
                "dayOfMonth", dayOfMonth, "adjust", adjust, "active", active, "memo", memo.isEmpty() ? null : memo);
        if (id == null) {
            recurringService.insertItem(item);            // item.id 에 새 키
        } else if (recurringService.updateItem(item) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }

        if (active) {
            recurringGenerator.skipPassedAndGenerate(item, LocalDate.now(SEOUL));
        }
        return Response.of(Constants.SUCCESS);
    }

    @Transactional
    @ResponseBody
    @PostMapping("/toggle")
    public Response toggle(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        Long id = ParamUtil.lng(param, "id");
        Boolean active = ParamUtil.bool(param, "active");
        if (id == null || active == null) {
            return Response.invalid("항목과 활성 여부를 확인해 주세요.");
        }
        Map<String, Object> key = ParamUtil.map("id", id, "userId", userId);
        Map<String, Object> saved = recurringService.selectItem(key);
        if (saved == null) {
            return Response.of(Constants.NOT_FOUND);
        }
        key.put("active", active);
        recurringService.updateItemActive(key);
        if (active) {
            saved.put("userId", userId);
            recurringGenerator.skipPassedAndGenerate(saved, LocalDate.now(SEOUL));
        }
        return Response.of(Constants.SUCCESS);
    }

    // 항목 삭제. 이미 기록된 거래는 남는다(처리 기록만 CASCADE 로 삭제)
    @ResponseBody
    @PostMapping("/delete")
    public Response delete(@RequestBody HashMap<String, Object> param, HttpSession session) {
        Long id = ParamUtil.lng(param, "id");
        if (id == null) {
            return Response.invalid("삭제할 항목을 확인해 주세요.");
        }
        if (recurringService.deleteItem(ParamUtil.map("id", id, "userId", SessionUtil.getUserId(session))) == 0) {
            return Response.of(Constants.NOT_FOUND);
        }
        return Response.of(Constants.SUCCESS);
    }

    private static long id(Map<String, Object> item) {
        return ((Number) item.get("id")).longValue();
    }
}
