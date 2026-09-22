package ledger.cmmn.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    // 로그인 사용자 id. 인터셉터를 지난 요청에서는 null 이 아니다.
    public static Long getUserId(HttpSession session) {
        return (Long) session.getAttribute(Constants.SESSION_USER_ID);
    }

    public static void login(HttpSession session, Long userId, String username) {
        session.setAttribute(Constants.SESSION_USER_ID, userId);
        session.setAttribute(Constants.SESSION_USERNAME, username);
    }

    public static void logout(HttpSession session) {
        session.invalidate();
    }
}
