package ledger.auth.service.impl;

import ledger.auth.dao.AuthDAO;
import ledger.auth.service.AuthService;
import ledger.cmmn.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service("authService")
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthDAO authDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${Globals.Ledger.SignupCode}")
    private String signupCode;

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        // resultType=HashMap 이면 키는 컬럼명 그대로(snake_case). mapUnderscoreToCamelCase 는 빈 매핑에만 적용된다
        Map<String, Object> user = authDAO.selectUserByUsername(username);
        if (user == null) {
            result.put("code", Constants.LOGIN_FAIL_NOT_EXIST);
            return result;
        }
        if (!passwordEncoder.matches(password, (String) user.get("password_hash"))) {
            result.put("code", Constants.LOGIN_FAIL_WRONG_PASSWORD);
            return result;
        }

        result.put("code", Constants.SUCCESS);
        result.put("userId", ((Number) user.get("id")).longValue());
        result.put("username", user.get("username"));
        return result;
    }

    @Override
    @Transactional
    public String signup(String username, String password, String code) {
        if (signupCode == null || signupCode.isBlank() || !signupCode.equals(code)) {
            return Constants.SIGNUP_FAIL_CODE;
        }
        if (authDAO.selectUserByUsername(username) != null) {
            return Constants.SIGNUP_FAIL_EXISTS;
        }

        Map<String, Object> param = new HashMap<>();
        param.put("username", username);
        param.put("passwordHash", passwordEncoder.encode(password));
        authDAO.insertUser(param);          // useGeneratedKeys → param.id
        authDAO.insertDefaultMaster(param); // 기본 카테고리/결제수단
        return Constants.SUCCESS;
    }
}
