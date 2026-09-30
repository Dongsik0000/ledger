package ledger.account.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("accountDAO")
public class AccountDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.account.dao.AccountDAO";

    public List<Map<String, Object>> selectAccountList(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectAccountList", param);
    }

    public List<Map<String, Object>> selectAccountOptions(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectAccountOptions", userId);
    }

    public Map<String, Object> selectAccount(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectAccount", param);
    }

    public int countAccountName(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countAccountName", param);
    }

    public int countAccountUsage(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countAccountUsage", param);
    }

    public int insertAccount(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertAccount", param);
    }

    public int updateAccount(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateAccount", param);
    }

    public int clearDefault(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".clearDefault", param);
    }

    public int deleteAccount(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteAccount", param);
    }

    public List<Map<String, Object>> selectTransferList(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectTransferList", param);
    }

    // 실행 후 param.id 에 생성된 키
    public int insertTransfer(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertTransfer", param);
    }

    public int updateTransfer(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateTransfer", param);
    }

    public int deleteTransfer(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteTransfer", param);
    }

    public List<Map<String, Object>> selectRecurringList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectRecurringList", userId);
    }

    public Map<String, Object> selectRecurring(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectRecurring", param);
    }

    public List<Map<String, Object>> selectActiveRecurring() {
        return sqlSession.selectList(SQL_PATH + ".selectActiveRecurring");
    }

    // 실행 후 param.id 에 생성된 키
    public int insertRecurring(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertRecurring", param);
    }

    public int updateRecurring(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateRecurring", param);
    }

    public int deleteRecurring(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteRecurring", param);
    }

    public int insertRecurringRun(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertRecurringRun", param);
    }

    public int updateRecurringRunTransfer(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateRecurringRunTransfer", param);
    }

    public List<String> selectRecurringRunKeys(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectRecurringRunKeys", param);
    }
}
