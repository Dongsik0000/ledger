package ledger.recurring.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("recurringDAO")
public class RecurringDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.recurring.dao.RecurringDAO";

    public List<Map<String, Object>> selectItemList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectItemList", userId);
    }

    public Map<String, Object> selectItem(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectItem", param);
    }

    public List<Map<String, Object>> selectActiveItems() {
        return sqlSession.selectList(SQL_PATH + ".selectActiveItems");
    }

    // 실행 후 param.id 에 생성된 키
    public int insertItem(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertItem", param);
    }

    public int updateItem(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateItem", param);
    }

    public int updateItemActive(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateItemActive", param);
    }

    public int deleteItem(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteItem", param);
    }

    public int insertRun(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertRun", param);
    }

    public int updateRunEntry(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateRunEntry", param);
    }

    public List<Map<String, Object>> selectRuns(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRuns", param);
    }
}
