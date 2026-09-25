package ledger.dashboard.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("dashboardDAO")
public class DashboardDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.dashboard.dao.DashboardDAO";

    public long selectBalanceBefore(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectBalanceBefore", param);
    }

    public Map<String, Object> selectPeriodSum(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectPeriodSum", param);
    }

    public long selectAssetTotal(long userId) {
        return sqlSession.selectOne(SQL_PATH + ".selectAssetTotal", userId);
    }

    public List<Map<String, Object>> selectActiveExpenseItems(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectActiveExpenseItems", userId);
    }

    public List<String> selectRunKeys(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRunKeys", param);
    }
}
