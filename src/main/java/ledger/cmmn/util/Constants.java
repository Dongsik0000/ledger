package ledger.cmmn.util;

import java.time.ZoneId;
import java.util.Set;

// 공통 상수: 결과 코드(JS App.CODE 와 같은 값), 세션 키, 업무 규칙 값
public class Constants {

    public static final String SUCCESS = "00";
    public static final String FAIL    = "99";

    // 로그인. 계정 없음과 비밀번호 오류는 구분하지 않는다(아이디 존재 여부 노출 방지)
    public static final String LOGIN_FAIL    = "01";
    public static final String LOGIN_BLOCKED = "03"; // 연속 실패로 잠시 차단

    // 가입
    public static final String SIGNUP_FAIL_CODE   = "11"; // 가입 코드 불일치
    public static final String SIGNUP_FAIL_EXISTS = "12"; // 아이디 중복
    public static final String SIGNUP_BLOCKED     = "13"; // 가입 코드 연속 실패로 잠시 차단

    // 입력·데이터 (message 에 화면 문구)
    public static final String INVALID             = "90"; // 입력 오류
    public static final String NOT_FOUND           = "91"; // 대상 없음(다른 사용자 것 포함, 구분하지 않음)
    public static final String DUPLICATE           = "92"; // 이름 중복
    public static final String HOLIDAY_KEY_MISSING = "93"; // 공휴일 API 키 미설정
    public static final String HOLIDAY_API_FAIL    = "94"; // 공휴일 API 호출 실패
    public static final String IN_USE              = "95"; // 사용 중이라 삭제·구분 변경 불가

    // 세션 키
    public static final String SESSION_USER_ID       = "userId";       // Long, app_user.id — 모든 조회의 격리 기준
    public static final String SESSION_USERNAME      = "username";
    public static final String SESSION_PASSWORD_HASH = "passwordHash"; // 로그인 때의 해시. DB 값과 다르면 LoginPageInterceptor 가 세션을 끊는다

    // 날짜 기준(오늘·주기·스케줄)
    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 금액 상한(999억 원 미만). 화면 입력(11자리)·DB CHECK 와 같은 값
    public static final long AMOUNT_MAX = 99_999_999_999L;

    // 거래·고정 항목·카테고리 구분
    public static final Set<String> ENTRY_TYPES = Set.of("EXPENSE", "INCOME");

    // 결제일·이체일 휴일 보정: 그대로 / 직전 영업일 / 다음 영업일
    public static final Set<String> DUE_ADJUSTS = Set.of("NONE", "PREV_BIZ", "NEXT_BIZ");

    private Constants() {
    }
}
