package ledger.auth.controller;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ledger.auth.service.AuthService;
import ledger.cmmn.util.Constants;
import ledger.cmmn.util.LoginAttemptLimiter;
import ledger.cmmn.util.ParamUtil;
import ledger.cmmn.util.RequestUtil;
import ledger.cmmn.util.Response;
import ledger.cmmn.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// 로그인·가입·로그아웃. 화면(/login, /signup)과 Ajax(/auth/**). 로직은 여기, AuthServiceImpl 은 DAO 연결만.
@Controller
public class AuthApiController {

    private static final int USERNAME_MAX = 50;       // app_user.username VARCHAR(50)
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX_BYTES = 72; // BCrypt 입력 한도. 넘으면 encode/matches 가 예외를 던진다

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptLimiter loginAttemptLimiter;

    @Value("${Globals.Ledger.SignupCode}")
    private String signupCode;

    // 없는 아이디도 BCrypt 비교를 한 번 해서 응답 시간으로 아이디 존재 여부가 드러나지 않게 한다
    private String dummyHash;

    @PostConstruct
    public void init() {
        dummyHash = passwordEncoder.encode("ledger-dummy-password");
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (SessionUtil.getUserId(session) != null) {
            return "redirect:/ledger/dashboard";
        }
        return "login/loginMain";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "login/signupMain";
    }

    @ResponseBody
    @PostMapping("/auth/login")
    public Response login(@RequestBody HashMap<String, Object> param, HttpServletRequest request) {
        String username = ParamUtil.str(param, "username");
        String password = ParamUtil.raw(param, "password");
        String attemptKey = "login|" + username.toLowerCase(Locale.ROOT) + "|" + RequestUtil.clientIp(request);

        if (loginAttemptLimiter.isBlocked(attemptKey)) {
            return Response.of(Constants.LOGIN_BLOCKED);
        }

        Map<String, Object> user = username.isEmpty() || username.length() > USERNAME_MAX
                ? null
                : authService.selectUserByUsername(username);
        // resultType=HashMap 이라 키는 컬럼명 그대로(password_hash)
        String hash = user == null ? dummyHash : (String) user.get("password_hash");
        boolean matched = fitsBcrypt(password) && passwordEncoder.matches(password, hash);

        if (user == null || !matched) {
            loginAttemptLimiter.recordFailure(attemptKey);
            return Response.of(Constants.LOGIN_FAIL);
        }

        loginAttemptLimiter.reset(attemptKey);
        SessionUtil.login(request, ((Number) user.get("id")).longValue(), (String) user.get("username"));
        return Response.of(Constants.SUCCESS);
    }

    // 가입: 사용자 + 기본 카테고리 + 기본 결제수단을 한 트랜잭션으로
    @Transactional
    @ResponseBody
    @PostMapping("/auth/signup")
    public Response signup(@RequestBody HashMap<String, Object> param, HttpServletRequest request) {
        String username = ParamUtil.str(param, "username");
        String password = ParamUtil.raw(param, "password");
        String code = ParamUtil.str(param, "signupCode");
        String attemptKey = "signup|" + RequestUtil.clientIp(request);

        if (loginAttemptLimiter.isBlocked(attemptKey)) {
            return Response.of(Constants.SIGNUP_BLOCKED);
        }
        if (username.isEmpty() || username.length() > USERNAME_MAX
                || password.length() < PASSWORD_MIN || !fitsBcrypt(password)) {
            return Response.of(Constants.FAIL, "아이디는 50자 이하, 비밀번호는 8자 이상 72바이트 이하로 입력해주세요.", null);
        }
        // 코드 검사를 중복 검사보다 먼저 — 코드가 없는 사람이 아이디 존재 여부를 알아낼 수 없게
        if (!isSignupCodeValid(code)) {
            loginAttemptLimiter.recordFailure(attemptKey);
            return Response.of(Constants.SIGNUP_FAIL_CODE);
        }
        if (authService.selectUserByUsername(username) != null) {
            return Response.of(Constants.SIGNUP_FAIL_EXISTS);
        }

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("passwordHash", passwordEncoder.encode(password));
        authService.insertUser(user);                 // useGeneratedKeys → user.id
        authService.insertDefaultCategories(user);
        authService.insertDefaultPaymentMethods(user);
        return Response.of(Constants.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/auth/logout")
    public Response logout(HttpServletRequest request) {
        SessionUtil.logout(request);
        return Response.of(Constants.SUCCESS);
    }

    private boolean fitsBcrypt(String password) {
        return !password.isEmpty() && password.getBytes(StandardCharsets.UTF_8).length <= PASSWORD_MAX_BYTES;
    }

    // 환경변수 LEDGER_SIGNUP_CODE 가 비어 있으면 가입 불가. 비교는 상수 시간
    private boolean isSignupCodeValid(String code) {
        if (signupCode == null || signupCode.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(signupCode.getBytes(StandardCharsets.UTF_8), code.getBytes(StandardCharsets.UTF_8));
    }
}
