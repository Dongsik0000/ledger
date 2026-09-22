package ledger.cmmn.util;

public class Constants {

    public static final String SUCCESS = "00"; // 공통 응답 성공
    public static final String FAIL    = "99"; // 공통 응답 실패

    // 로그인 결과 코드
    public static final String LOGIN_FAIL_NOT_EXIST      = "01"; // 계정 없음
    public static final String LOGIN_FAIL_WRONG_PASSWORD = "02"; // 비밀번호 불일치

    // 가입 결과 코드
    public static final String SIGNUP_FAIL_CODE     = "11"; // 가입 코드 불일치
    public static final String SIGNUP_FAIL_EXISTS   = "12"; // 아이디 중복

    // 세션 키
    public static final String SESSION_USER_ID  = "userId";   // Long, app_user.id — 모든 조회의 격리 기준
    public static final String SESSION_USERNAME = "username";
}
