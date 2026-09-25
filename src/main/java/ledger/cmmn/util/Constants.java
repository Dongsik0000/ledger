package ledger.cmmn.util;

// 결과 코드. JS 의 App.CODE(common.js)와 같은 값을 유지한다.
public class Constants {

    public static final String SUCCESS = "00"; // 공통 응답 성공
    public static final String FAIL    = "99"; // 공통 응답 실패

    // 로그인 결과 코드. 계정 없음과 비밀번호 오류를 구분하지 않는다(아이디 존재 여부 노출 방지)
    public static final String LOGIN_FAIL    = "01";
    public static final String LOGIN_BLOCKED = "03"; // 연속 실패로 잠시 차단

    // 가입 결과 코드
    public static final String SIGNUP_FAIL_CODE   = "11"; // 가입 코드 불일치
    public static final String SIGNUP_FAIL_EXISTS = "12"; // 아이디 중복
    public static final String SIGNUP_BLOCKED     = "13"; // 가입 코드 연속 실패로 잠시 차단

    // 세션 키
    public static final String SESSION_USER_ID  = "userId";   // Long, app_user.id — 모든 조회의 격리 기준
    public static final String SESSION_USERNAME = "username";
}
