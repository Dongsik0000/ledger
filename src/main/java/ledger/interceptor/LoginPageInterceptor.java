package ledger.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ledger.auth.service.AuthService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

// /ledger/** 요청은 세션에 userId 가 있어야 통과. Ajax 는 JSON 으로, 화면 요청은 /login 으로 보낸다.
// 로그인 때의 비밀번호 해시가 지금 DB 값과 다르면(다른 곳에서 비밀번호를 바꿈) 그 세션을 끊는다.
public class LoginPageInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    // 로그인하지 않았거나 비밀번호가 바뀐 세션이면 Ajax 는 sessionExpired, 화면은 /login 으로
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Long userId = SessionUtil.getUserId(session);
        if (userId != null) {
            if (Objects.equals(session.getAttribute(Constants.SESSION_PASSWORD_HASH), authService.selectPasswordHash(userId))) {
                return true;
            }
            session.invalidate();
        }

        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"sessionExpired\": true}");
        } else {
            response.sendRedirect(request.getContextPath() + "/login");
        }
        return false;
    }
}
