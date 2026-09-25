package ledger.cmmn.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    // 로그인 사용자 id. 인터셉터를 지난 요청에서는 null 이 아니다.
    public static Long getUserId(HttpSession session) {
        return (Long) session.getAttribute(Constants.SESSION_USER_ID);
    }

    // 로그인 성공 시 세션 ID 를 새로 발급한다(세션 고정 공격 방지). 기존 속성은 유지된다.
    public static void login(HttpServletRequest request, Long userId, String username) {
        request.getSession(true);
        request.changeSessionId();
        HttpSession session = request.getSession(false);
        session.setAttribute(Constants.SESSION_USER_ID, userId);
        session.setAttribute(Constants.SESSION_USERNAME, username);
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
