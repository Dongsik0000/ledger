package ledger.auth.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthApiControllerTest {

    @Test
    void loginKeyUsesLowercaseUsername() {
        assertEquals("login|tester|203.0.113.7", AuthApiController.loginAttemptKey("Tester", "203.0.113.7"));
    }

    // 검증을 통과하지 못한 아이디(50자 초과, 빈 값)는 키에 넣지 않는다 — 긴 키로 메모리를 채우는 공격 방지
    @Test
    void loginKeyDropsInvalidUsername() {
        assertEquals("login||203.0.113.7", AuthApiController.loginAttemptKey("x".repeat(51), "203.0.113.7"));
        assertEquals("login||203.0.113.7", AuthApiController.loginAttemptKey("", "203.0.113.7"));
    }
}
