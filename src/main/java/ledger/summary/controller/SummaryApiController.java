package ledger.summary.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.cycle.OpeningBalance;
import ledger.cycle.PayCycle;
import ledger.dashboard.service.DashboardService;
import ledger.holiday.service.HolidayService;
import ledger.settings.service.SettingsService;
import ledger.summary.SummaryTables;
import ledger.summary.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 요약·통계: 달력 월 또는 주기("m월 주기" = m월 급여일 ~ 다음 달 급여일 전날) 기준 표 3종과 차트 데이터.
// 현재 주기 지표는 /ledger/dashboard/summary
@Controller
@RequestMapping("/ledger/summary")
public class SummaryApiController {

    private static final int MAX_MONTHS = 24;
    private static final Set<String> BASES = Set.of("MONTH", "CYCLE");

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private HolidayService holidayService;

    @GetMapping
    public String summary() {
        return "summary/summaryMain";
    }

    @ResponseBody
    @PostMapping("/load")
    public Response load(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        YearMonth from = ParamUtil.month(param, "from");
        YearMonth to = ParamUtil.month(param, "to");
        String basis = ParamUtil.has(param, "basis") ? ParamUtil.str(param, "basis") : "MONTH";
        if (from == null || to == null || from.isAfter(to) || ChronoUnit.MONTHS.between(from, to) >= MAX_MONTHS
                || !BASES.contains(basis)) {
            return Response.invalid("기간은 시작 월부터 종료 월까지 최대 24개월로 골라 주세요.");
        }

        List<String> months = SummaryTables.months(from, to);
        Map<String, Object> setting = settingsService.selectUserSetting(userId);
        OpeningBalance opening = OpeningBalance.of(setting);

        // 각 월(주기)의 기간: 달력 월이면 1일~말일, 주기면 그 달 급여일 ~ 다음 달 급여일 전날
        List<PayCycle.Cycle> periods = new ArrayList<>();
        List<Integer> holidayMissingYears = new ArrayList<>();
        if ("CYCLE".equals(basis)) {
            int payDay = ((Number) setting.get("payDay")).intValue();
            boolean adjust = Boolean.TRUE.equals(setting.get("payDayAdjust"));
            Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                    "from", from.minusMonths(1).atDay(1), "to", to.plusMonths(2).atEndOfMonth())));
            for (String month : months) {
                periods.add(PayCycle.cycleStarting(YearMonth.parse(month), payDay, adjust, holidays));
            }
            // 보정은 등록된 공휴일만 안다. 공휴일이 하나도 없는 해는 주말만 반영됐다고 알린다
            if (adjust) {
                for (int y = periods.get(0).start().getYear(); y <= periods.get(periods.size() - 1).end().getYear(); y++) {
                    int year = y;
                    if (holidays.stream().noneMatch(d -> d.getYear() == year)) {
                        holidayMissingYears.add(year);
                    }
                }
            }
        } else {
            for (String month : months) {
                YearMonth m = YearMonth.parse(month);
                periods.add(new PayCycle.Cycle(m.atDay(1), m.atEndOfMonth()));
            }
        }
        List<Map<String, Object>> buckets = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            buckets.add(ParamUtil.map("key", months.get(i), "from", periods.get(i).start(), "to", periods.get(i).end()));
        }
        LocalDate rangeFrom = periods.get(0).start();
        Map<String, Object> range = ParamUtil.map("userId", userId, "from", rangeFrom, "to", periods.get(periods.size() - 1).end(),
                "openingDate", opening.countFrom(), "buckets", "CYCLE".equals(basis) ? buckets : null);

        // 월(주기)별: 수입·지출·그 기간 잔액·기간 말 누적 잔액(첫 기간 전까지의 잔액에서 이어서).
        // 누적은 시작 잔액 기준일 이전 거래를 빼고 세며, 기준일 전에 끝나는 기간은 알 수 없어 null
        Map<String, Map<String, Object>> byMonth = new HashMap<>();
        for (Map<String, Object> m : summaryService.selectMonthly(range)) {
            byMonth.put((String) m.get("month"), m);
        }
        long cumulative = opening.balanceAt(dashboardService.selectBalanceBefore(
                ParamUtil.map("userId", userId, "date", rangeFrom, "from", opening.countFrom())));
        List<Map<String, Object>> monthly = new ArrayList<>();
        for (int i = 0; i < months.size(); i++) {
            String month = months.get(i);
            Map<String, Object> m = byMonth.get(month);
            long income = m == null ? 0 : ((Number) m.get("income")).longValue();
            long expense = m == null ? 0 : ((Number) m.get("expense")).longValue();
            cumulative += m == null ? 0 : ((Number) m.get("balanceNet")).longValue();
            boolean known = opening.known(periods.get(i).end().plusDays(1));
            monthly.add(ParamUtil.map("month", month, "from", periods.get(i).start().toString(), "to", periods.get(i).end().toString(),
                    "income", income, "expense", expense, "net", income - expense, "cumulative", known ? cumulative : null));
        }

        List<Map<String, Object>> expenseCategories = new ArrayList<>();
        for (Map<String, Object> c : settingsService.selectCategoryList(userId)) {
            if ("EXPENSE".equals(c.get("type"))) {
                expenseCategories.add(c);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("basis", basis);
        data.put("months", months);
        data.put("monthly", monthly);
        data.put("holidayMissingYears", holidayMissingYears);
        data.put("categoryRows", SummaryTables.categoryRows(expenseCategories, summaryService.selectCategoryMonthly(range), months));
        data.put("paymentRows", SummaryTables.paymentRows(settingsService.selectPaymentList(userId), summaryService.selectPaymentMonthly(range), months));
        return Response.of(Constants.SUCCESS, data);
    }

    // 도넛: 한 달(또는 그 달 주기) 지출을 그룹 단위로
    @ResponseBody
    @PostMapping("/donut")
    public Response donut(@RequestBody HashMap<String, Object> param, HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        YearMonth month = ParamUtil.month(param, "month");
        String basis = ParamUtil.has(param, "basis") ? ParamUtil.str(param, "basis") : "MONTH";
        if (month == null || !BASES.contains(basis)) {
            return Response.invalid("월을 확인해 주세요.");
        }
        PayCycle.Cycle period = new PayCycle.Cycle(month.atDay(1), month.atEndOfMonth());
        if ("CYCLE".equals(basis)) {
            Map<String, Object> setting = settingsService.selectUserSetting(userId);
            Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                    "from", month.minusMonths(1).atDay(1), "to", month.plusMonths(2).atEndOfMonth())));
            period = PayCycle.cycleStarting(month, ((Number) setting.get("payDay")).intValue(),
                    Boolean.TRUE.equals(setting.get("payDayAdjust")), holidays);
        }
        return Response.of(Constants.SUCCESS, summaryService.selectGroupExpense(ParamUtil.map(
                "userId", userId, "from", period.start(), "to", period.end())));
    }
}
