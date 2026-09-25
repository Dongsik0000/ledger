package ledger.summary.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("summaryDAO")
public class SummaryDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.summary.dao.SummaryDAO";

    public List<Map<String, Object>> selectMonthly(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectMonthly", param);
    }

    public List<Map<String, Object>> selectCategoryMonthly(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectCategoryMonthly", param);
    }

    public List<Map<String, Object>> selectPaymentMonthly(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectPaymentMonthly", param);
    }

    public List<Map<String, Object>> selectGroupExpense(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectGroupExpense", param);
    }
}
