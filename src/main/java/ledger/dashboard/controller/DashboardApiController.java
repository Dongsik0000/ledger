package ledger.dashboard.controller;

import jakarta.servlet.http.HttpSession;
import ledger.asset.service.AssetService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.cycle.CycleLoader;
import ledger.cycle.OpeningBalance;
import ledger.cycle.PayCycle;
import ledger.dashboard.service.DashboardService;
import ledger.entry.service.EntryService;
import ledger.recurring.Installment;
import ledger.recurring.RecurringSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
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

    private static final int RECENT_LIMIT = 10;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private CycleLoader cycleLoader;

    @Autowired
    private EntryService entryService;

    // 대시보드 화면
    @GetMapping
    public String dashboard() {
        return "dashboard/dashboardMain";
    }

    // 이월 + 주기 수입 − 주기 지출 = 주기 잔액, (주기 잔액 − 예정 고정지출) ÷ 남은 일수 = 오늘 쓸 수 있는 돈
    @ResponseBody
    @PostMapping("/summary")
    public Response summary(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        LocalDate today = LocalDate.now(Constants.SEOUL);

        CycleLoader.UserCycle uc = cycleLoader.load(userId, today);
        PayCycle.Cycle cycle = uc.cycle();
        // 시작 잔액이 있으면 기준일 이전 거래를 빼고 그 금액부터 센다(기준일이 이번 주기 안이면 이월로 본다)
        OpeningBalance opening = OpeningBalance.of(uc.setting());
        long carryOver = opening.balanceAt(dashboardService.selectBalanceBefore(
                ParamUtil.map("userId", userId, "date", cycle.start(), "from", opening.countFrom())));
        Map<String, Object> sum = dashboardService.selectPeriodSum(
                ParamUtil.map("userId", userId, "from", opening.periodFrom(cycle.start()), "to", cycle.end()));
        long income = ((Number) sum.get("income")).longValue();
        long expense = ((Number) sum.get("expense")).longValue();
        long balance = carryOver + income - expense;
        long pending = pendingFixed(userId, cycle, today, uc.holidays());
        long daysLeft = ChronoUnit.DAYS.between(today, cycle.end()) + 1;
        boolean marketUpdated = assetService.refreshStocks(userId);
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
        data.put("marketUpdated", marketUpdated);
        data.put("totalBalance", balance + assetTotal);
        return Response.of(Constants.SUCCESS, data);
    }

    // 오늘이 속한 주기의 카테고리 예산과 사용액(환불 제외). 대시보드 막대와 거래 입력 창의 예산 안내가 쓴다
    @ResponseBody
    @PostMapping("/budgets")
    public Response budgets(HttpSession session) {
        long userId = SessionUtil.getUserId(session);
        PayCycle.Cycle cycle = cycleLoader.load(userId, LocalDate.now(Constants.SEOUL)).cycle();

        Map<String, Object> data = new HashMap<>();
        data.put("from", cycle.start().toString());
        data.put("to", cycle.end().toString());
        data.put("items", dashboardService.selectBudgetUsage(
                ParamUtil.map("userId", userId, "from", cycle.start(), "to", cycle.end())));
        return Response.of(Constants.SUCCESS, data);
    }

    // 최근 거래 RECENT_LIMIT 건(오늘까지)
    @ResponseBody
    @PostMapping("/recent")
    public Response recent(HttpSession session) {
        return Response.of(Constants.SUCCESS, entryService.selectRecentEntries(ParamUtil.map(
                "userId", SessionUtil.getUserId(session), "today", LocalDate.now(Constants.SEOUL), "limit", RECENT_LIMIT)));
    }

    // 예정 고정지출: 활성 지출 고정 항목의 이번 주기 결제일 중 오늘 이후(오늘 포함)이고 아직 처리(run)되지 않은 것의 합.
    // 할부는 기간 안의 달만, 그 회차 금액으로
    private long pendingFixed(long userId, PayCycle.Cycle cycle, LocalDate today, Set<LocalDate> holidays) {
        List<Map<String, Object>> items = dashboardService.selectActiveExpenseItems(userId);
        Map<Long, List<PayCycle.Due>> duesByItem = RecurringSchedule.duesInCycle(items, cycle, holidays);
        Set<String> months = new TreeSet<>();
        duesByItem.values().forEach(dues -> dues.forEach(d -> months.add(d.month().toString())));
        if (months.isEmpty()) {
            return 0;
        }

        Set<String> runs = new HashSet<>(dashboardService.selectRunKeys(
                ParamUtil.map("userId", userId, "months", List.copyOf(months))));
        long pending = 0;
        for (Map<String, Object> item : items) {
            long id = RecurringSchedule.id(item);
            for (PayCycle.Due due : duesByItem.get(id)) {
                if (!due.date().isBefore(today) && !runs.contains(id + "|" + due.month())) {
                    pending += Installment.amountFor(item, due.month());
                }
            }
        }
        return pending;
    }
}
