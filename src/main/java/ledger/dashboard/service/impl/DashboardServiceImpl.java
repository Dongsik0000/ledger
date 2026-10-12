package ledger.dashboard.service.impl;

import ledger.dashboard.dao.DashboardDAO;
import ledger.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 대시보드 집계 서비스: DAO 위임
@Service("dashboardService")
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardDAO dashboardDAO;

    // date 전날까지 거래 합(수입 − 지출). from 이 있으면 그날부터
    @Override
    public long selectBalanceBefore(Map<String, Object> param) {
        return dashboardDAO.selectBalanceBefore(param);
    }

    // 기간 수입·지출 합
    @Override
    public Map<String, Object> selectPeriodSum(Map<String, Object> param) {
        return dashboardDAO.selectPeriodSum(param);
    }

    // 자산 합계
    @Override
    public long selectAssetTotal(long userId) {
        return dashboardDAO.selectAssetTotal(userId);
    }

    // 예정 고정지출 계산용 활성 지출 고정 항목
    @Override
    public List<Map<String, Object>> selectActiveExpenseItems(long userId) {
        return dashboardDAO.selectActiveExpenseItems(userId);
    }

    // 처리된(건너뜀 포함) 결제 "고정항목id|YYYY-MM" 목록
    @Override
    public List<String> selectRunKeys(Map<String, Object> param) {
        return dashboardDAO.selectRunKeys(param);
    }
}
