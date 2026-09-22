package ledger.auth.service;

import java.util.Map;

public interface AuthService {

    // 로그인. 성공 시 code=SUCCESS 와 함께 세션에 넣을 userId, username 을 돌려준다.
    Map<String, Object> login(String username, String password);

    // 가입. 코드 검증 → 아이디 중복 → 저장 → 기본 카테고리/결제수단 생성. 결과 코드 반환.
    String signup(String username, String password, String signupCode);
}
