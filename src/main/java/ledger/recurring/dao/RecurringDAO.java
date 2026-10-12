package ledger.recurring.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 고정 항목 SQL 호출. 매퍼: sqlmap/mappers/ledger/recurring/recurring.xml
@Repository("recurringDAO")
public class RecurringDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.recurring.dao.RecurringDAO";

    // 고정 항목 목록(활성 먼저, 결제일 순)
    public List<Map<String, Object>> selectItemList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectItemList", userId);
    }

    // 내 고정 항목 하나
    public Map<String, Object> selectItem(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectItem", param);
    }

    // 스케줄러용: 모든 사용자의 활성 고정 항목
    public List<Map<String, Object>> selectActiveItems() {
        return sqlSession.selectList(SQL_PATH + ".selectActiveItems");
    }

    // 실행 후 param.id 에 생성된 키
    public int insertItem(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertItem", param);
    }

    // 고정 항목 수정
    public int updateItem(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateItem", param);
    }

    // 사용·중지 전환
    public int updateItemActive(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateItemActive", param);
    }

    // 고정 항목 삭제. 이미 만든 거래는 남는다
    public int deleteItem(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteItem", param);
    }

    // 그 달 처리 기록. 이미 있으면 0(중복 생성 방지)
    public int insertRun(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertRun", param);
    }

    // 처리 기록에 만든 거래 연결
    public int updateRunEntry(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateRunEntry", param);
    }

    // 처리 기록과 기록된 거래 금액(이번 주기 상태 표시용)
    public List<Map<String, Object>> selectRuns(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRuns", param);
    }
}
