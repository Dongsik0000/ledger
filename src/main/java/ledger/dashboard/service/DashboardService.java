package ledger.dashboard.service;

import java.util.List;
import java.util.Map;

// 대시보드 집계 DAO 연결. 계산은 DashboardApiController·PayCycle 에 있다.
public interface DashboardService {

    long selectBalanceBefore(Map<String, Object> param);

    Map<String, Object> selectPeriodSum(Map<String, Object> param);

    long selectAssetTotal(long userId);

    List<Map<String, Object>> selectActiveExpenseItems(long userId);

    List<String> selectRunKeys(Map<String, Object> param);
}
