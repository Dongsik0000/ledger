package ledger.dashboard.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 대시보드 집계 SQL 호출. 매퍼: sqlmap/mappers/ledger/dashboard/dashboard.xml
@Repository("dashboardDAO")
public class DashboardDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.dashboard.dao.DashboardDAO";

    // date 전날까지 거래 합(수입 − 지출). from 이 있으면 그날부터
    public long selectBalanceBefore(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectBalanceBefore", param);
    }

    // 기간 수입·지출 합
    public Map<String, Object> selectPeriodSum(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectPeriodSum", param);
    }

    // 자산 합계
    public long selectAssetTotal(long userId) {
        return sqlSession.selectOne(SQL_PATH + ".selectAssetTotal", userId);
    }

    // 예정 고정지출 계산용 활성 지출 고정 항목
    public List<Map<String, Object>> selectActiveExpenseItems(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectActiveExpenseItems", userId);
    }

    // 처리된(건너뜀 포함) 결제 "고정항목id|YYYY-MM" 목록
    public List<String> selectRunKeys(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRunKeys", param);
    }
}
