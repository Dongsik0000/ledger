package ledger.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 대시보드 화면. 요약 API(/ledger/dashboard/summary)는 주기 계산 기능에서 추가한다.
@Controller
@RequestMapping("/ledger/dashboard")
public class DashboardApiController {

    @GetMapping
    public String dashboard() {
        return "dashboard/dashboardMain";
    }
}
