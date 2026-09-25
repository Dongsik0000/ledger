package ledger.recurring.service;

import java.util.List;
import java.util.Map;

// 고정 항목 DAO 연결. 요청 로직은 RecurringApiController, 자동 기록은 RecurringGenerator 에 있다.
public interface RecurringService {

    List<Map<String, Object>> selectItemList(long userId);

    Map<String, Object> selectItem(Map<String, Object> param);

    List<Map<String, Object>> selectActiveItems();

    int insertItem(Map<String, Object> param);

    int updateItem(Map<String, Object> param);

    int updateItemActive(Map<String, Object> param);

    int deleteItem(Map<String, Object> param);

    int insertRun(Map<String, Object> param);

    int updateRunEntry(Map<String, Object> param);

    List<Map<String, Object>> selectRuns(Map<String, Object> param);
}
