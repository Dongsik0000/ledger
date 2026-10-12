package ledger.entry.service;

import java.util.List;
import java.util.Map;

// 거래 DAO 연결과 자산 잔액 반영(트랜잭션). 입력 검사는 EntryApiController 에 있다.
public interface EntryService {

    List<Map<String, Object>> selectEntryList(Map<String, Object> param);

    Map<String, Object> selectEntrySum(Map<String, Object> param);

    List<Map<String, Object>> selectRecentEntries(Map<String, Object> param);

    Map<String, Object> selectEntry(Map<String, Object> param);

    int insertEntry(Map<String, Object> param);

    // 여러 건을 한 트랜잭션으로 추가. 하나라도 실패하면 전부 되돌린다
    int insertEntries(List<Map<String, Object>> entries);

    int updateEntry(Map<String, Object> param);

    int deleteEntry(Map<String, Object> param);
}
