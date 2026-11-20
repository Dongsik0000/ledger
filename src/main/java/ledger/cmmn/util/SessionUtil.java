package ledger.cmmn.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

// 로그인 세션 도우미: 사용자 id 읽기, 로그인·로그아웃
public class SessionUtil {

    private SessionUtil() {
    }

    // 로그인 사용자 id. 인터셉터를 지난 요청에서는 null 이 아니다.
    public static Long getUserId(HttpSession session) {
        return (Long) session.getAttribute(Constants.SESSION_USER_ID);
    }

    // 로그인 성공 시 세션 ID 를 새로 발급한다(세션 고정 공격 방지). 기존 속성은 유지된다.
    // passwordHash: 지금 비밀번호 해시. 나중에 비밀번호가 바뀌면 이 세션은 끊긴다(LoginPageInterceptor)
    public static void login(HttpServletRequest request, Long userId, String username, String passwordHash) {
        request.getSession(true);
        request.changeSessionId();
        HttpSession session = request.getSession(false);
        session.setAttribute(Constants.SESSION_USER_ID, userId);
        session.setAttribute(Constants.SESSION_USERNAME, username);
        session.setAttribute(Constants.SESSION_PASSWORD_HASH, passwordHash);
    }

    // 세션 폐기
    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
