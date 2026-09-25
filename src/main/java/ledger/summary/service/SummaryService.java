package ledger.summary.service;

import java.util.List;
import java.util.Map;

// 요약 집계 DAO 연결. 표 만들기는 SummaryApiController·SummaryTables 에 있다.
public interface SummaryService {

    List<Map<String, Object>> selectMonthly(Map<String, Object> param);

    List<Map<String, Object>> selectCategoryMonthly(Map<String, Object> param);

    List<Map<String, Object>> selectPaymentMonthly(Map<String, Object> param);

    List<Map<String, Object>> selectGroupExpense(Map<String, Object> param);
}
