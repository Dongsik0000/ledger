package ledger.auth.controller;

import jakarta.servlet.http.HttpSession;
import ledger.auth.service.AuthService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

// 로그인/가입 Ajax. 화면(JSP)은 CmmnController 가 담당한다.
@Controller
@RequestMapping("/auth")
public class AuthApiController {

    private static final Logger logger = LoggerFactory.getLogger(AuthApiController.class);

    @Autowired
    private AuthService authService;

    @ResponseBody
    @PostMapping("/login")
    public Response login(@RequestBody HashMap<String, Object> param, HttpSession session) {
        try {
            String username = String.valueOf(param.getOrDefault("username", "")).trim();
            String password = String.valueOf(param.getOrDefault("password", ""));

            Map<String, Object> result = authService.login(username, password);
            String code = (String) result.get("code");
            if (Constants.SUCCESS.equals(code)) {
                SessionUtil.login(session, (Long) result.get("userId"), (String) result.get("username"));
            }
            return Response.of(code);
        } catch (Exception e) {
            logger.error("로그인 처리 중 오류", e);
            return Response.of(Constants.FAIL, "로그인 처리 중 오류가 발생했습니다.", null);
        }
    }

    @ResponseBody
    @PostMapping("/signup")
    public Response signup(@RequestBody HashMap<String, Object> param) {
        try {
            String username = String.valueOf(param.getOrDefault("username", "")).trim();
            String password = String.valueOf(param.getOrDefault("password", ""));
            String code = String.valueOf(param.getOrDefault("signupCode", "")).trim();

            if (username.isEmpty() || password.length() < 8) {
                return Response.of(Constants.FAIL, "아이디와 8자 이상 비밀번호를 입력해주세요.", null);
            }
            return Response.of(authService.signup(username, password, code));
        } catch (Exception e) {
            logger.error("가입 처리 중 오류", e);
            return Response.of(Constants.FAIL, "가입 처리 중 오류가 발생했습니다.", null);
        }
    }
}
