package ledger.account.service;

import java.util.List;
import java.util.Map;

// 통장·이체·정기 이체 DAO 연결. 로직은 AccountApiController 와 TransferGenerator 에 있다.
public interface AccountService {

    // 통장 목록과 잔액(기준 잔액 + to 까지의 거래·이체)
    List<Map<String, Object>> selectAccountList(Map<String, Object> param);

    // 입력 창의 통장 선택지(기본 통장 먼저)
    List<Map<String, Object>> selectAccountOptions(long userId);

    // 내 통장 하나. 없거나 남의 것이면 null
    Map<String, Object> selectAccount(Map<String, Object> param);

    // 같은 이름 통장 수(수정 중인 자기 자신 제외)
    int countAccountName(Map<String, Object> param);

    // 이 통장을 쓰는 거래·고정 항목·이체 수
    int countAccountUsage(Map<String, Object> param);

    // 통장 추가
    int insertAccount(Map<String, Object> param);

    // 통장 수정. 기준 잔액·기준일이 바뀌면 기준 시각도 새로
    int updateAccount(Map<String, Object> param);

    // 기존 기본 통장 해제(기본 통장은 사용자당 하나)
    int clearDefault(Map<String, Object> param);

    // 통장 삭제. 정기 이체는 함께 삭제
    int deleteAccount(Map<String, Object> param);

    // 최근 이체 목록(최신순 limit 건)
    List<Map<String, Object>> selectTransferList(Map<String, Object> param);

    // 이체 추가. param.id 에 새 키
    int insertTransfer(Map<String, Object> param);

    // 이체 수정
    int updateTransfer(Map<String, Object> param);

    // 이체 삭제
    int deleteTransfer(Map<String, Object> param);

    // 정기 이체 목록
    List<Map<String, Object>> selectRecurringList(long userId);

    // 내 정기 이체 하나
    Map<String, Object> selectRecurring(Map<String, Object> param);

    // 스케줄러용: 모든 사용자의 활성 정기 이체
    List<Map<String, Object>> selectActiveRecurring();

    // 정기 이체 추가. param.id 에 새 키
    int insertRecurring(Map<String, Object> param);

    // 정기 이체 수정
    int updateRecurring(Map<String, Object> param);

    // 정기 이체 삭제. 이미 기록된 이체는 남는다
    int deleteRecurring(Map<String, Object> param);

    // 정기 이체의 그 달 처리 기록. 이미 있으면 0(중복 기록 방지)
    int insertRecurringRun(Map<String, Object> param);

    // 처리 기록에 만든 이체 연결
    int updateRecurringRunTransfer(Map<String, Object> param);

    // 처리된(건너뜀 포함) "정기이체id|YYYY-MM" 목록
    List<String> selectRecurringRunKeys(Map<String, Object> param);
}
