package ledger.summary.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 요약 집계 SQL 호출. 매퍼: sqlmap/mappers/ledger/summary/summary.xml
@Repository("summaryDAO")
public class SummaryDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.summary.dao.SummaryDAO";

    // 월(주기)별 수입·지출
    public List<Map<String, Object>> selectMonthly(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectMonthly", param);
    }

    // 카테고리 × 월(주기) 지출
    public List<Map<String, Object>> selectCategoryMonthly(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectCategoryMonthly", param);
    }

    // 결제수단 × 월(주기) 지출
    public List<Map<String, Object>> selectPaymentMonthly(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectPaymentMonthly", param);
    }

    // 한 기간 지출을 그룹별로(도넛 차트)
    public List<Map<String, Object>> selectGroupExpense(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectGroupExpense", param);
    }
}
