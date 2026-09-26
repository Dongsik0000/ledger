package ledger.entry.service;

import java.util.List;
import java.util.Map;

// 거래 DAO 연결. 로직은 EntryApiController 에 있다.
public interface EntryService {

    List<Map<String, Object>> selectEntryList(Map<String, Object> param);

    Map<String, Object> selectEntrySum(Map<String, Object> param);

    List<Map<String, Object>> selectRecentEntries(Map<String, Object> param);

    Map<String, Object> selectEntry(Map<String, Object> param);

    int insertEntry(Map<String, Object> param);

    int updateEntry(Map<String, Object> param);

    int deleteEntry(Map<String, Object> param);
}
