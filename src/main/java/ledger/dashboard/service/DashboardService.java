package ledger.dashboard.service;

import java.util.List;
import java.util.Map;

// 대시보드 집계 DAO 연결. 계산은 DashboardApiController·PayCycle 에 있다.
public interface DashboardService {

    // date 전날까지 거래 합(수입 − 지출). from 이 있으면 그날부터
    long selectBalanceBefore(Map<String, Object> param);

    // 기간 수입·지출 합
    Map<String, Object> selectPeriodSum(Map<String, Object> param);

    // 자산 합계
    long selectAssetTotal(long userId);

    // 예정 고정지출 계산용 활성 지출 고정 항목
    List<Map<String, Object>> selectActiveExpenseItems(long userId);

    // 처리된(건너뜀 포함) 결제 "고정항목id|YYYY-MM" 목록
    List<String> selectRunKeys(Map<String, Object> param);
}
