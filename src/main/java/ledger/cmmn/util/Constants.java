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

    // 공통 입력·데이터 결과 코드 (message 에 사용자 문구)
    public static final String INVALID             = "90"; // 입력 오류
    public static final String NOT_FOUND           = "91"; // 대상 없음(다른 사용자 것 포함, 구분하지 않음)
    public static final String DUPLICATE           = "92"; // 이름 중복
    public static final String HOLIDAY_KEY_MISSING = "93"; // 공휴일 API 키 미설정
    public static final String HOLIDAY_API_FAIL    = "94"; // 공휴일 API 호출 실패
    public static final String IN_USE              = "95"; // 거래·고정 항목이 사용 중이라 삭제·구분 변경 불가

    // 세션 키
    public static final String SESSION_USER_ID  = "userId";   // Long, app_user.id — 모든 조회의 격리 기준
    public static final String SESSION_USERNAME = "username";
}
