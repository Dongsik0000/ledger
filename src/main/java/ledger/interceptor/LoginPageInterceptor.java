package ledger.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ledger.cmmn.util.SessionUtil;
import org.springframework.web.servlet.HandlerInterceptor;

// /ledger/** 요청은 세션에 userId 가 있어야 통과. Ajax 는 JSON 으로, 화면 요청은 /login 으로 보낸다.
public class LoginPageInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (SessionUtil.getUserId(request.getSession()) != null) {
            return true;
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
