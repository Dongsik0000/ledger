package ledger.recurring.service;

import java.util.List;
import java.util.Map;

// 고정 항목 DAO 연결. 요청 로직은 RecurringApiController, 자동 기록은 RecurringGenerator 에 있다.
public interface RecurringService {

    // 고정 항목 목록(활성 먼저, 결제일 순)
    List<Map<String, Object>> selectItemList(long userId);

    // 내 고정 항목 하나
    Map<String, Object> selectItem(Map<String, Object> param);

    // 스케줄러용: 모든 사용자의 활성 고정 항목
    List<Map<String, Object>> selectActiveItems();

    // 고정 항목 추가. param.id 에 새 키
    int insertItem(Map<String, Object> param);

    // 고정 항목 수정
    int updateItem(Map<String, Object> param);

    // 사용·중지 전환
    int updateItemActive(Map<String, Object> param);

    // 고정 항목 삭제. 이미 만든 거래는 남는다
    int deleteItem(Map<String, Object> param);

    // 그 달 처리 기록. 이미 있으면 0(중복 생성 방지)
    int insertRun(Map<String, Object> param);

    // 처리 기록에 만든 거래 연결
    int updateRunEntry(Map<String, Object> param);

    // 처리 기록과 기록된 거래 금액(이번 주기 상태 표시용)
    List<Map<String, Object>> selectRuns(Map<String, Object> param);
}
