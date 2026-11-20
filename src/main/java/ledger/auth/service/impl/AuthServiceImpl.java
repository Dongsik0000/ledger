package ledger.auth.service.impl;

import ledger.auth.dao.AuthDAO;
import ledger.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// 인증 서비스: DAO 위임
@Service("authService")
public class AuthServiceImpl implements AuthService {

    // 자식 → 부모 순서(auth.xml 주석 참고)
    private static final List<String> RECORDS = List.of("deleteRecurringTransfers", "deleteTransfers", "deleteRecurringItems",
            "deleteEntries", "deleteBankAccounts", "deleteAssets", "resetOpeningBalance");
    private static final List<String> ACCOUNT = List.of("deleteCategories", "deletePaymentMethods", "deleteUser");

    @Autowired
    private AuthDAO authDAO;

    // 기록 초기화. 하나라도 실패하면 전부 되돌린다
    @Override
    @Transactional
    public void resetData(long userId) {
        authDAO.deleteUserData(RECORDS, userId);
    }

    // 탈퇴. 기록을 지운 뒤 카테고리·결제수단·계정까지 한 트랜잭션으로
    @Override
    @Transactional
    public void deleteUser(long userId) {
        authDAO.deleteUserData(RECORDS, userId);
        authDAO.deleteUserData(ACCOUNT, userId);
    }

    @Override
    public Map<String, Object> selectUserByUsername(String username) {
        return authDAO.selectUserByUsername(username);
    }

    @Override
    public String selectPasswordHash(long userId) {
        return authDAO.selectPasswordHash(userId);
    }

    @Override
    public int updatePassword(Map<String, Object> param) {
        return authDAO.updatePassword(param);
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
