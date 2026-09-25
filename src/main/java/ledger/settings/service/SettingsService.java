package ledger.settings.service;

import java.util.List;
import java.util.Map;

// 설정(주기·카테고리·결제수단) DAO 연결. 로직은 SettingsApiController 에 있다.
public interface SettingsService {

    Map<String, Object> selectUserSetting(long userId);

    int updateUserSetting(Map<String, Object> param);

    List<Map<String, Object>> selectCategoryList(long userId);

    Map<String, Object> selectCategory(Map<String, Object> param);

    int countCategoryName(Map<String, Object> param);

    int countCategoryUsage(Map<String, Object> param);

    int selectMaxCategoryOrder(Map<String, Object> param);

    List<Long> selectCategoryIds(Map<String, Object> param);

    int insertCategory(Map<String, Object> param);

    int updateCategory(Map<String, Object> param);

    int updateCategoryOrder(Map<String, Object> param);

    int deleteCategory(Map<String, Object> param);

    List<Map<String, Object>> selectPaymentList(long userId);

    Map<String, Object> selectPayment(Map<String, Object> param);

    int countPaymentName(Map<String, Object> param);

    int countPaymentUsage(Map<String, Object> param);

    int selectMaxPaymentOrder(long userId);

    List<Long> selectPaymentIds(long userId);

    int insertPayment(Map<String, Object> param);

    int updatePayment(Map<String, Object> param);

    int updatePaymentOrder(Map<String, Object> param);

    int deletePayment(Map<String, Object> param);
}
