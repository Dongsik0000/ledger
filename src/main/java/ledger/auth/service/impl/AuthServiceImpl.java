package ledger.auth.service.impl;

import ledger.auth.dao.AuthDAO;
import ledger.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("authService")
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthDAO authDAO;

    @Override
    public Map<String, Object> selectUserByUsername(String username) {
        return authDAO.selectUserByUsername(username);
    }

    @Override
    public int insertUser(Map<String, Object> param) {
        return authDAO.insertUser(param);
    }

    @Override
    public int insertDefaultCategories(Map<String, Object> param) {
        return authDAO.insertDefaultCategories(param);
    }

    @Override
    public int insertDefaultPaymentMethods(Map<String, Object> param) {
        return authDAO.insertDefaultPaymentMethods(param);
    }
}
