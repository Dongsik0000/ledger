package ledger.settings.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("settingsDAO")
public class SettingsDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.settings.dao.SettingsDAO";

    public Map<String, Object> selectUserSetting(long userId) {
        return sqlSession.selectOne(SQL_PATH + ".selectUserSetting", userId);
    }

    public int updateUserSetting(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateUserSetting", param);
    }

    public int updateOpening(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateOpening", param);
    }

    public List<Map<String, Object>> selectCategoryList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectCategoryList", userId);
    }

    public Map<String, Object> selectCategory(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectCategory", param);
    }

    public int countCategoryName(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countCategoryName", param);
    }

    public int countCategoryUsage(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countCategoryUsage", param);
    }

    public int selectMaxCategoryOrder(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectMaxCategoryOrder", param);
    }

    public List<Long> selectCategoryIds(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectCategoryIds", param);
    }

    public int insertCategory(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertCategory", param);
    }

    public int updateCategory(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateCategory", param);
    }

    public int updateCategoryOrder(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateCategoryOrder", param);
    }

    public int deleteCategory(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteCategory", param);
    }

    public List<Map<String, Object>> selectPaymentList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectPaymentList", userId);
    }

    public Map<String, Object> selectPayment(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectPayment", param);
    }

    public int countPaymentName(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countPaymentName", param);
    }

    public int countPaymentUsage(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countPaymentUsage", param);
    }

    public int selectMaxPaymentOrder(long userId) {
        return sqlSession.selectOne(SQL_PATH + ".selectMaxPaymentOrder", userId);
    }

    public List<Long> selectPaymentIds(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectPaymentIds", userId);
    }

    public int insertPayment(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertPayment", param);
    }

    public int updatePayment(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updatePayment", param);
    }

    public int updatePaymentOrder(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updatePaymentOrder", param);
    }

    public int deletePayment(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deletePayment", param);
    }
}
