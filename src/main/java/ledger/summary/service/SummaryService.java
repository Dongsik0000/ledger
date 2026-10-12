package ledger.summary.service;

import java.util.List;
import java.util.Map;

// 요약 집계 DAO 연결. 표 만들기는 SummaryApiController·SummaryTables 에 있다.
public interface SummaryService {

    // 월(주기)별 수입·지출
    List<Map<String, Object>> selectMonthly(Map<String, Object> param);

    // 카테고리 × 월(주기) 지출
    List<Map<String, Object>> selectCategoryMonthly(Map<String, Object> param);

    // 결제수단 × 월(주기) 지출
    List<Map<String, Object>> selectPaymentMonthly(Map<String, Object> param);

    // 한 기간 지출을 그룹별로(도넛 차트)
    List<Map<String, Object>> selectGroupExpense(Map<String, Object> param);
}
