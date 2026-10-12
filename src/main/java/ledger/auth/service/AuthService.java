package ledger.auth.service;

import java.util.Map;

// 인증 DAO 연결. 로직은 AuthApiController 에 있다.
public interface AuthService {

    // 아이디로 사용자(id·username·password_hash). 없으면 null
    Map<String, Object> selectUserByUsername(String username);

    // 비밀번호 해시
    String selectPasswordHash(long userId);

    // 비밀번호 해시 변경
    int updatePassword(Map<String, Object> param);

    // 사용자 추가. param.id 에 새 키
    int insertUser(Map<String, Object> param);

    // 가입 시 기본 카테고리
    int insertDefaultCategories(Map<String, Object> param);

    // 가입 시 기본 결제수단
    int insertDefaultPaymentMethods(Map<String, Object> param);
}
