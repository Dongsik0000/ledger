package ledger.account.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 통장·이체·정기 이체 SQL 호출. 매퍼: sqlmap/mappers/ledger/account/account.xml
@Repository("accountDAO")
public class AccountDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.account.dao.AccountDAO";

    // 통장 목록과 잔액(기준 잔액 + to 까지의 거래·이체)
    public List<Map<String, Object>> selectAccountList(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectAccountList", param);
    }

    // 입력 창의 통장 선택지(기본 통장 먼저)
    public List<Map<String, Object>> selectAccountOptions(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectAccountOptions", userId);
    }

    // 내 통장 하나. 없거나 남의 것이면 null
    public Map<String, Object> selectAccount(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectAccount", param);
    }

    // 같은 이름 통장 수(수정 중인 자기 자신 제외)
    public int countAccountName(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countAccountName", param);
    }

    // 이 통장을 쓰는 거래·고정 항목·이체 수
    public int countAccountUsage(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countAccountUsage", param);
    }

    // 통장 추가
    public int insertAccount(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertAccount", param);
    }

    // 통장 수정. 기준 잔액·기준일이 바뀌면 기준 시각도 새로
    public int updateAccount(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateAccount", param);
    }

    // 기존 기본 통장 해제(기본 통장은 사용자당 하나)
    public int clearDefault(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".clearDefault", param);
    }

    // 통장 삭제. 정기 이체는 함께 삭제
    public int deleteAccount(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteAccount", param);
    }

    // 최근 이체 목록(최신순 limit 건)
    public List<Map<String, Object>> selectTransferList(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectTransferList", param);
    }

    // 실행 후 param.id 에 생성된 키
    public int insertTransfer(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertTransfer", param);
    }

    // 이체 수정
    public int updateTransfer(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateTransfer", param);
    }

    // 이체 삭제
    public int deleteTransfer(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteTransfer", param);
    }

    // 정기 이체 목록
    public List<Map<String, Object>> selectRecurringList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectRecurringList", userId);
    }

    // 내 정기 이체 하나
    public Map<String, Object> selectRecurring(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectRecurring", param);
    }

    // 스케줄러용: 모든 사용자의 활성 정기 이체
    public List<Map<String, Object>> selectActiveRecurring() {
        return sqlSession.selectList(SQL_PATH + ".selectActiveRecurring");
    }

    // 실행 후 param.id 에 생성된 키
    public int insertRecurring(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertRecurring", param);
    }

    // 정기 이체 수정
    public int updateRecurring(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateRecurring", param);
    }

    // 정기 이체 삭제. 이미 기록된 이체는 남는다
    public int deleteRecurring(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteRecurring", param);
    }

    // 정기 이체의 그 달 처리 기록. 이미 있으면 0(중복 기록 방지)
    public int insertRecurringRun(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertRecurringRun", param);
    }

    // 처리 기록에 만든 이체 연결
    public int updateRecurringRunTransfer(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateRecurringRunTransfer", param);
    }

    // 처리된(건너뜀 포함) "정기이체id|YYYY-MM" 목록
    public List<String> selectRecurringRunKeys(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRecurringRunKeys", param);
    }
}
