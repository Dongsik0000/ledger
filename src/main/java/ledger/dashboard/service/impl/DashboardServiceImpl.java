package ledger.dashboard.service.impl;

import ledger.dashboard.dao.DashboardDAO;
import ledger.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("dashboardService")
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardDAO dashboardDAO;

    @Override
    public long selectBalanceBefore(Map<String, Object> param) {
        return dashboardDAO.selectBalanceBefore(param);
    }

    @Override
    public Map<String, Object> selectPeriodSum(Map<String, Object> param) {
        return dashboardDAO.selectPeriodSum(param);
    }

    @Override
    public long selectAssetTotal(long userId) {
        return dashboardDAO.selectAssetTotal(userId);
    }

    @Override
    public List<Map<String, Object>> selectActiveExpenseItems(long userId) {
        return dashboardDAO.selectActiveExpenseItems(userId);
    }

    @Override
    public List<String> selectRunKeys(Map<String, Object> param) {
        return dashboardDAO.selectRunKeys(param);
    }
}
