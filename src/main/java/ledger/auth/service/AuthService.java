package ledger.auth.service;

import java.util.Map;

// 인증 DAO 연결. 로직은 AuthApiController 에 있다.
public interface AuthService {

    Map<String, Object> selectUserByUsername(String username);

    String selectPasswordHash(long userId);

    int updatePassword(Map<String, Object> param);

    int insertUser(Map<String, Object> param);

    int insertDefaultCategories(Map<String, Object> param);

    int insertDefaultPaymentMethods(Map<String, Object> param);
}
