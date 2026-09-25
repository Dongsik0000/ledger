package ledger.dashboard.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.cycle.PayCycle;
import ledger.dashboard.service.DashboardService;
import ledger.entry.service.EntryService;
import ledger.holiday.service.HolidayService;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// 대시보드: 오늘이 속한 주기의 요약·오늘 쓸 수 있는 돈·최근 거래. 요약·고정 항목·자산·설정 화면도 summary 를 쓴다.
@Controller
@RequestMapping("/ledger/dashboard")
public class DashboardApiController {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int RECENT_LIMIT = 10;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private EntryService entryService;

    @GetMapping
    public String dashboard() {
        return "dashboard/dashboardMain";
    }

    // plan.md "핵심 계산": 이월 + 주기 수입 − 주기 지출 = 주기 잔액, (주기 잔액 − 예정 고정지출) ÷ 남은 일수 = 오늘 쓸 수 있는 돈
    @ResponseBody
    @PostMapping("/summary")
    public Response summary(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        LocalDate today = LocalDate.now(SEOUL);

        Map<String, Object> setting = settingsService.selectUserSetting(userId);
        int payDay = ((Number) setting.get("payDay")).intValue();
        boolean adjust = Boolean.TRUE.equals(setting.get("payDayAdjust"));
        // 주기·결제일 보정이 앞뒤 달로 넘어갈 수 있어 넉넉히 조회
        Set<LocalDate> holidays = new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                "from", today.minusMonths(3).withDayOfMonth(1), "to", today.plusMonths(3))));

        PayCycle.Cycle cycle = PayCycle.cycleOf(today, payDay, adjust, holidays);
        long carryOver = dashboardService.selectBalanceBefore(ParamUtil.map("userId", userId, "date", cycle.start()));
        Map<String, Object> sum = dashboardService.selectPeriodSum(
                ParamUtil.map("userId", userId, "from", cycle.start(), "to", cycle.end()));
        long income = ((Number) sum.get("income")).longValue();
        long expense = ((Number) sum.get("expense")).longValue();
        long balance = carryOver + income - expense;
        long pending = pendingFixed(userId, cycle, today, holidays);
        long daysLeft = ChronoUnit.DAYS.between(today, cycle.end()) + 1;
        long assetTotal = dashboardService.selectAssetTotal(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("today", today.toString());
        data.put("cycleStart", cycle.start().toString());
        data.put("cycleEnd", cycle.end().toString());
        data.put("daysLeft", daysLeft);
        data.put("carryOver", carryOver);
        data.put("cycleIncome", income);
        data.put("cycleExpense", expense);
        data.put("cycleBalance", balance);
        data.put("pendingFixed", pending);
        data.put("dailyBudget", Math.floorDiv(balance - pending, daysLeft));
        data.put("assetTotal", assetTotal);
        data.put("totalBalance", balance + assetTotal);
        return Response.of(Constants.SUCCESS, data);
    }

    @ResponseBody
    @PostMapping("/recent")
    public Response recent(HttpSession session) {
        return Response.of(Constants.SUCCESS, entryService.selectRecentEntries(ParamUtil.map(
                "userId", SessionUtil.getUserId(session), "today", LocalDate.now(SEOUL), "limit", RECENT_LIMIT)));
    }

    // 예정 고정지출: 활성 지출 고정 항목의 이번 주기 결제일 중 오늘 이후(오늘 포함)이고 아직 처리(run)되지 않은 것의 합
    private long pendingFixed(long userId, PayCycle.Cycle cycle, LocalDate today, Set<LocalDate> holidays) {
        List<Map<String, Object>> items = dashboardService.selectActiveExpenseItems(userId);
        Map<Long, List<PayCycle.Due>> duesByItem = new HashMap<>();
        Set<String> months = new TreeSet<>();
        for (Map<String, Object> item : items) {
            List<PayCycle.Due> dues = PayCycle.duesInCycle(cycle,
                    ((Number) item.get("dayOfMonth")).intValue(), (String) item.get("adjust"), holidays);
            duesByItem.put(((Number) item.get("id")).longValue(), dues);
            dues.forEach(d -> months.add(d.month().toString()));
        }
        if (months.isEmpty()) {
            return 0;
        }

        Set<String> runs = new HashSet<>(dashboardService.selectRunKeys(
                ParamUtil.map("userId", userId, "months", List.copyOf(months))));
        long pending = 0;
        for (Map<String, Object> item : items) {
            long id = ((Number) item.get("id")).longValue();
            for (PayCycle.Due due : duesByItem.get(id)) {
                if (!due.date().isBefore(today) && !runs.contains(id + "|" + due.month())) {
                    pending += ((Number) item.get("amount")).longValue();
                }
            }
        }
        return pending;
    }
}
