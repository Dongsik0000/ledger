package ledger.settings.dao;

import jakarta.annotation.Resource;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

// 설정(주기·카테고리·결제수단) SQL 호출. 매퍼: sqlmap/mappers/ledger/settings/settings.xml
@Repository("settingsDAO")
public class SettingsDAO {

    @Resource(name = "sqlSession-ledger-postgre")
    private SqlSessionTemplate sqlSession;

    public static final String SQL_PATH = "ledger.settings.dao.SettingsDAO";

    // 주기 설정(급여일·보정)과 시작 잔액
    public Map<String, Object> selectUserSetting(long userId) {
        return sqlSession.selectOne(SQL_PATH + ".selectUserSetting", userId);
    }

    // 주기 시작일·주말 보정 저장
    public int updateUserSetting(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateUserSetting", param);
    }

    // 시작 잔액 저장. 기준일이 null 이면 해제
    public int updateOpening(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateOpening", param);
    }

    // 카테고리 전체(지출 먼저, 표시 순서대로, 숨김 포함)
    public List<Map<String, Object>> selectCategoryList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectCategoryList", userId);
    }

    // 내 카테고리 하나
    public Map<String, Object> selectCategory(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectCategory", param);
    }

    // 같은 구분·이름 카테고리 수(수정 중인 자기 자신 제외)
    public int countCategoryName(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countCategoryName", param);
    }

    // 이 카테고리를 쓰는 거래·고정 항목 수
    public int countCategoryUsage(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countCategoryUsage", param);
    }

    // 구분 안 가장 큰 표시 순서(새 항목은 그 뒤)
    public int selectMaxCategoryOrder(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectMaxCategoryOrder", param);
    }

    // 구분 안 카테고리 id(표시 순서대로)
    public List<Long> selectCategoryIds(Map<String, Object> param) {
        return sqlSession.selectList(SQL_PATH + ".selectCategoryIds", param);
    }

    // 카테고리 추가
    public int insertCategory(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertCategory", param);
    }

    // 카테고리 수정
    public int updateCategory(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateCategory", param);
    }

    // 카테고리 표시 순서 변경
    public int updateCategoryOrder(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updateCategoryOrder", param);
    }

    // 카테고리 삭제
    public int deleteCategory(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deleteCategory", param);
    }

    // 결제수단 전체(표시 순서대로, 숨김 포함)
    public List<Map<String, Object>> selectPaymentList(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectPaymentList", userId);
    }

    // 내 결제수단 하나
    public Map<String, Object> selectPayment(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".selectPayment", param);
    }

    // 같은 이름 결제수단 수(수정 중인 자기 자신 제외)
    public int countPaymentName(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countPaymentName", param);
    }

    // 이 결제수단을 쓰는 거래·고정 항목 수
    public int countPaymentUsage(Map<String, Object> param) {
        return sqlSession.selectOne(SQL_PATH + ".countPaymentUsage", param);
    }

    // 가장 큰 표시 순서(새 항목은 그 뒤)
    public int selectMaxPaymentOrder(long userId) {
        return sqlSession.selectOne(SQL_PATH + ".selectMaxPaymentOrder", userId);
    }

    // 결제수단 id(표시 순서대로)
    public List<Long> selectPaymentIds(long userId) {
        return sqlSession.selectList(SQL_PATH + ".selectPaymentIds", userId);
    }

    // 결제수단 추가
    public int insertPayment(Map<String, Object> param) {
        return sqlSession.insert(SQL_PATH + ".insertPayment", param);
    }

    // 결제수단 수정
    public int updatePayment(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updatePayment", param);
    }

    // 결제수단 표시 순서 변경
    public int updatePaymentOrder(Map<String, Object> param) {
        return sqlSession.update(SQL_PATH + ".updatePaymentOrder", param);
    }

    // 결제수단 삭제
    public int deletePayment(Map<String, Object> param) {
        return sqlSession.delete(SQL_PATH + ".deletePayment", param);
    }
}
