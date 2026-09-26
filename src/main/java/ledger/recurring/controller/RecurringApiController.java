package ledger.recurring.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.cycle.PayCycle;
import ledger.entry.controller.EntryApiController;
import ledger.holiday.service.HolidayService;
import ledger.recurring.Installment;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

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
            dues.removeIf(d -> Installment.amountFor(item, d.month()) == null);   // 할부 기간 밖
            duesByItem.put(id(item), dues);
            dues.forEach(d -> months.add(d.month().toString()));
        }
        // 다음 결제일 계산에 쓰는 달(지난달~다다음 달)의 처리 기록도 함께 읽는다
        if (!items.isEmpty()) {
            for (int i = -1; i <= 2; i++) {
                months.add(YearMonth.from(today).plusMonths(i).toString());
            }
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
            // 이번 주기 결제 금액: 할부는 이번 주기 회차 금액(기간 밖이면 0), 일반 항목은 amount
            long dueAmount = ((Number) item.get("amount")).longValue();
            if (Installment.isInstallment(item)) {
                YearMonth first = dues.isEmpty() ? null : dues.get(0).month();
                dueAmount = first == null ? 0 : Installment.amountFor(item, first);
                item.put("installmentRound", first == null ? 0
                        : Installment.roundOf(Installment.start(item), Installment.months(item), first));
            }
            if (active && expense) {
                monthlyFixed += dueAmount;
            }
            item.put("dueAmount", dueAmount);
            item.put("dueDates", dueDates);
            // 다음 결제일(휴일 보정·할부 기간·처리 여부 반영). 비활성이거나 더 없으면 null
            PayCycle.Due next = active ? nextDue(item, today, holidays, p -> runs.containsKey(id(item) + "|" + p)) : null;
            item.put("nextDue", next == null ? null : next.date().toString());
            item.put("nextAmount", next == null ? null : Installment.amountFor(item, next.month()));
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
        Boolean installment = ParamUtil.has(param, "installment") ? ParamUtil.bool(param, "installment") : Boolean.FALSE;

        // 할부: 총액·개월 수·첫 결제월을 받고, amount 는 2회차 이후 금액으로 계산한다
        Long installmentTotal = null;
        Integer installmentMonths = null;
        YearMonth installmentStart = null;
        if (Boolean.TRUE.equals(installment)) {
            installmentTotal = ParamUtil.lng(param, "installmentTotal");
            installmentMonths = ParamUtil.integer(param, "installmentMonths");
            installmentStart = ParamUtil.month(param, "installmentStart");
            if (!"EXPENSE".equals(type) || installmentTotal == null || installmentMonths == null || installmentStart == null
                    || installmentMonths < Installment.MONTHS_MIN || installmentMonths > Installment.MONTHS_MAX
                    || installmentTotal < installmentMonths || installmentTotal > EntryApiController.AMOUNT_MAX) {
                return Response.invalid("할부는 지출만 가능해요. 총액·개월 수(2~60개월)·첫 결제월을 확인해 주세요.");
            }
            amount = Installment.baseAmount(installmentTotal, installmentMonths);
        }

        if ((ParamUtil.has(param, "id") && id == null) || name.isEmpty() || name.length() > NAME_MAX
                || !TYPES.contains(type) || amount == null || amount < 1 || amount > EntryApiController.AMOUNT_MAX
                || categoryId == null || (ParamUtil.has(param, "paymentMethodId") && paymentMethodId == null)
                || dayOfMonth == null || dayOfMonth < 1 || dayOfMonth > 31 || !ADJUSTS.contains(adjust)
                || memo.length() > MEMO_MAX || active == null || installment == null) {
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

        Map<String, Object> saved = null;
        if (id != null) {
            saved = recurringService.selectItem(ParamUtil.map("id", id, "userId", userId));
            if (saved == null) {
                return Response.of(Constants.NOT_FOUND);
            }
        }

        Map<String, Object> item = ParamUtil.map("userId", userId, "id", id, "name", name, "type", type,
                "amount", amount, "categoryId", categoryId, "paymentMethodId", paymentMethodId,
                "dayOfMonth", dayOfMonth, "adjust", adjust, "active", active, "memo", memo.isEmpty() ? null : memo,
                "installmentTotal", installmentTotal, "installmentMonths", installmentMonths,
                "installmentStart", installmentStart == null ? null : installmentStart.toString());
        LocalDate today = LocalDate.now(SEOUL);
        if (recurringGenerator.isFinished(item, today)) {
            return Response.invalid("마지막 회차 결제일이 이미 지났어요. 지난 회차는 거래 내역에 직접 기록해 주세요.");
        }
        if (id == null) {
            recurringService.insertItem(item);            // item.id 에 새 키
        } else {
            recurringService.updateItem(item);
        }

        if (active) {
            if (shouldSkipPassed(saved, item)) {
                recurringGenerator.skipPassed(item, today);
            }
            recurringGenerator.generate(item, today);
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
        boolean wasActive = Boolean.TRUE.equals(saved.get("active"));
        key.put("active", active);
        recurringService.updateItemActive(key);
        // 꺼져 있던 항목을 켤 때만: 꺼져 있던 동안 지난 결제일은 건너뛰고, 오늘 결제일이면 기록
        if (active && !wasActive) {
            LocalDate today = LocalDate.now(SEOUL);
            saved.put("userId", userId);
            recurringGenerator.skipPassed(saved, today);
            recurringGenerator.generate(saved, today);
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

    // 오늘 이후(오늘 포함) 아직 처리하지 않은 첫 결제일. 할부는 기간 안에서만 찾고, 마지막 회차가 지났으면 null.
    // 보정(PREV_BIZ)으로 다음 달분이 이번 달로 당겨질 수 있어 지난달분부터 본다
    static PayCycle.Due nextDue(Map<String, Object> item, LocalDate today, Set<LocalDate> holidays, Predicate<YearMonth> handled) {
        boolean installment = Installment.isInstallment(item);
        YearMonth m = YearMonth.from(today).minusMonths(1);
        if (installment && Installment.start(item).isAfter(m)) {
            m = Installment.start(item);
        }
        for (int i = 0; i < 24; i++, m = m.plusMonths(1)) {
            if (installment && m.isAfter(Installment.lastMonth(Installment.start(item), Installment.months(item)))) {
                return null;
            }
            LocalDate date = PayCycle.dueDate(m, ((Number) item.get("dayOfMonth")).intValue(), (String) item.get("adjust"), holidays);
            if (!date.isBefore(today) && !handled.test(m)) {
                return new PayCycle.Due(m, date);
            }
        }
        return null;
    }

    // 지난 결제일 건너뛰기는 신규·결제일/보정 변경·할부 일정 변경·재활성화일 때만.
    // 금액·이름만 바꾼 수정에서 건너뛰면, 스케줄러 실패로 누락된 결제가 조용히 "건너뜀"으로 가려진다
    static boolean shouldSkipPassed(Map<String, Object> saved, Map<String, Object> item) {
        if (saved == null || !Boolean.TRUE.equals(saved.get("active"))) {
            return true;
        }
        return !Objects.equals(number(saved.get("dayOfMonth")), number(item.get("dayOfMonth")))
                || !Objects.equals(saved.get("adjust"), item.get("adjust"))
                || !Objects.equals(text(saved.get("installmentStart")), text(item.get("installmentStart")))
                || !Objects.equals(number(saved.get("installmentMonths")), number(item.get("installmentMonths")));
    }

    // DB(Short·Integer)와 요청 값(Integer)의 타입 차이, CHAR 공백을 무시하고 비교
    private static Long number(Object v) {
        return v == null ? null : ((Number) v).longValue();
    }

    private static String text(Object v) {
        return v == null ? null : v.toString().trim();
    }

    private static long id(Map<String, Object> item) {
        return ((Number) item.get("id")).longValue();
    }
}
