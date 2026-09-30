package ledger.account.service.impl;

import ledger.account.dao.AccountDAO;
import ledger.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDAO accountDAO;

    @Override
    public List<Map<String, Object>> selectAccountList(Map<String, Object> param) {
        return accountDAO.selectAccountList(param);
    }

    @Override
    public List<Map<String, Object>> selectAccountOptions(long userId) {
        return accountDAO.selectAccountOptions(userId);
    }

    @Override
    public Map<String, Object> selectAccount(Map<String, Object> param) {
        return accountDAO.selectAccount(param);
    }

    @Override
    public int countAccountName(Map<String, Object> param) {
        return accountDAO.countAccountName(param);
    }

    @Override
    public int countAccountUsage(Map<String, Object> param) {
        return accountDAO.countAccountUsage(param);
    }

    @Override
    public int insertAccount(Map<String, Object> param) {
        return accountDAO.insertAccount(param);
    }

    @Override
    public int updateAccount(Map<String, Object> param) {
        return accountDAO.updateAccount(param);
    }

    @Override
    public int clearDefault(Map<String, Object> param) {
        return accountDAO.clearDefault(param);
    }

    @Override
    public int deleteAccount(Map<String, Object> param) {
        return accountDAO.deleteAccount(param);
    }

    @Override
    public List<Map<String, Object>> selectTransferList(Map<String, Object> param) {
        return accountDAO.selectTransferList(param);
    }

    @Override
    public int insertTransfer(Map<String, Object> param) {
        return accountDAO.insertTransfer(param);
    }

    @Override
    public int updateTransfer(Map<String, Object> param) {
        return accountDAO.updateTransfer(param);
    }

    @Override
    public int deleteTransfer(Map<String, Object> param) {
        return accountDAO.deleteTransfer(param);
    }

    @Override
    public List<Map<String, Object>> selectRecurringList(long userId) {
        return accountDAO.selectRecurringList(userId);
    }

    @Override
    public Map<String, Object> selectRecurring(Map<String, Object> param) {
        return accountDAO.selectRecurring(param);
    }

    @Override
    public List<Map<String, Object>> selectActiveRecurring() {
        return accountDAO.selectActiveRecurring();
    }

    @Override
    public int insertRecurring(Map<String, Object> param) {
        return accountDAO.insertRecurring(param);
    }

    @Override
    public int updateRecurring(Map<String, Object> param) {
        return accountDAO.updateRecurring(param);
    }

    @Override
    public int deleteRecurring(Map<String, Object> param) {
        return accountDAO.deleteRecurring(param);
    }

    @Override
    public int insertRecurringRun(Map<String, Object> param) {
        return accountDAO.insertRecurringRun(param);
    }

    @Override
    public int updateRecurringRunTransfer(Map<String, Object> param) {
        return accountDAO.updateRecurringRunTransfer(param);
    }

    @Override
    public List<String> selectRecurringRunKeys(Map<String, Object> param) {
        return accountDAO.selectRecurringRunKeys(param);
    }
}
