package ledger.entry.service;

import java.util.List;
import java.util.Map;

// 거래 DAO 연결과 자산 잔액 반영(트랜잭션). 입력 검사는 EntryApiController 에 있다.
public interface EntryService {

    // 조건별 거래 목록(날짜 내림차순)
    List<Map<String, Object>> selectEntryList(Map<String, Object> param);

    // 같은 조건의 건수·수입·지출 합계
    Map<String, Object> selectEntrySum(Map<String, Object> param);

    // 오늘까지의 최근 거래
    List<Map<String, Object>> selectRecentEntries(Map<String, Object> param);

    // 내용 자동 채우기: 내용별 가장 최근 구분·카테고리·결제수단·통장·금액
    List<Map<String, Object>> selectTitleSuggestions(Map<String, Object> param);

    // 내 거래 하나
    Map<String, Object> selectEntry(Map<String, Object> param);

    // 거래 추가
    int insertEntry(Map<String, Object> param);

    // 여러 건을 한 트랜잭션으로 추가. 하나라도 실패하면 전부 되돌린다
    int insertEntries(List<Map<String, Object>> entries);

    // 거래 수정
    int updateEntry(Map<String, Object> param);

    // 거래 삭제
    int deleteEntry(Map<String, Object> param);
}
