package ledger.summary.controller;

import jakarta.servlet.http.HttpSession;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import ledger.dashboard.service.DashboardService;
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

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 요약·통계: 달력 월 기준 표 3종과 차트 데이터. 현재 주기 지표는 /ledger/dashboard/summary
@Controller
@RequestMapping("/ledger/summary")
public class SummaryApiController {

    private static final int MAX_MONTHS = 24;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private DashboardService dashboardService;

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
        if (from == null || to == null || from.isAfter(to) || ChronoUnit.MONTHS.between(from, to) >= MAX_MONTHS) {
            return Response.invalid("기간은 시작 월부터 종료 월까지 최대 24개월로 골라 주세요.");
        }

        List<String> months = SummaryTables.months(from, to);
        Map<String, Object> range = ParamUtil.map("userId", userId, "from", from.atDay(1), "to", to.atEndOfMonth());

        // 월별: 수입·지출·그 달 잔액·누적 잔액(시작 월 전까지의 잔액에서 이어서)
        Map<String, Map<String, Object>> byMonth = new HashMap<>();
        for (Map<String, Object> m : summaryService.selectMonthly(range)) {
            byMonth.put((String) m.get("month"), m);
        }
        long cumulative = dashboardService.selectBalanceBefore(ParamUtil.map("userId", userId, "date", from.atDay(1)));
        List<Map<String, Object>> monthly = new ArrayList<>();
        for (String month : months) {
            Map<String, Object> m = byMonth.get(month);
            long income = m == null ? 0 : ((Number) m.get("income")).longValue();
            long expense = m == null ? 0 : ((Number) m.get("expense")).longValue();
            cumulative += income - expense;
            monthly.add(ParamUtil.map("month", month, "income", income, "expense", expense,
                    "net", income - expense, "cumulative", cumulative));
        }

        List<Map<String, Object>> expenseCategories = new ArrayList<>();
        for (Map<String, Object> c : settingsService.selectCategoryList(userId)) {
            if ("EXPENSE".equals(c.get("type"))) {
                expenseCategories.add(c);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("months", months);
        data.put("monthly", monthly);
        data.put("categoryRows", SummaryTables.categoryRows(expenseCategories, summaryService.selectCategoryMonthly(range), months));
        data.put("paymentRows", SummaryTables.paymentRows(settingsService.selectPaymentList(userId), summaryService.selectPaymentMonthly(range), months));
        return Response.of(Constants.SUCCESS, data);
    }

    // 도넛: 한 달 지출을 그룹 단위로
    @ResponseBody
    @PostMapping("/donut")
    public Response donut(@RequestBody HashMap<String, Object> param, HttpSession session) {
        YearMonth month = ParamUtil.month(param, "month");
        if (month == null) {
            return Response.invalid("월을 확인해 주세요.");
        }
        return Response.of(Constants.SUCCESS, summaryService.selectGroupExpense(ParamUtil.map(
                "userId", SessionUtil.getUserId(session), "from", month.atDay(1), "to", month.atEndOfMonth())));
    }
}
