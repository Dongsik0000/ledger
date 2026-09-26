package ledger.settings.service.impl;

import ledger.settings.dao.SettingsDAO;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("settingsService")
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private SettingsDAO settingsDAO;

    @Override
    public Map<String, Object> selectUserSetting(long userId) {
        return settingsDAO.selectUserSetting(userId);
    }

    @Override
    public int updateUserSetting(Map<String, Object> param) {
        return settingsDAO.updateUserSetting(param);
    }

    @Override
    public int updateOpening(Map<String, Object> param) {
        return settingsDAO.updateOpening(param);
    }

    @Override
    public List<Map<String, Object>> selectCategoryList(long userId) {
        return settingsDAO.selectCategoryList(userId);
    }

    @Override
    public Map<String, Object> selectCategory(Map<String, Object> param) {
        return settingsDAO.selectCategory(param);
    }

    @Override
    public int countCategoryName(Map<String, Object> param) {
        return settingsDAO.countCategoryName(param);
    }

    @Override
    public int countCategoryUsage(Map<String, Object> param) {
        return settingsDAO.countCategoryUsage(param);
    }

    @Override
    public int selectMaxCategoryOrder(Map<String, Object> param) {
        return settingsDAO.selectMaxCategoryOrder(param);
    }

    @Override
    public List<Long> selectCategoryIds(Map<String, Object> param) {
        return settingsDAO.selectCategoryIds(param);
    }

    @Override
    public int insertCategory(Map<String, Object> param) {
        return settingsDAO.insertCategory(param);
    }

    @Override
    public int updateCategory(Map<String, Object> param) {
        return settingsDAO.updateCategory(param);
    }

    @Override
    public int updateCategoryOrder(Map<String, Object> param) {
        return settingsDAO.updateCategoryOrder(param);
    }

    @Override
    public int deleteCategory(Map<String, Object> param) {
        return settingsDAO.deleteCategory(param);
    }

    @Override
    public List<Map<String, Object>> selectPaymentList(long userId) {
        return settingsDAO.selectPaymentList(userId);
    }

    @Override
    public Map<String, Object> selectPayment(Map<String, Object> param) {
        return settingsDAO.selectPayment(param);
    }

    @Override
    public int countPaymentName(Map<String, Object> param) {
        return settingsDAO.countPaymentName(param);
    }

    @Override
    public int countPaymentUsage(Map<String, Object> param) {
        return settingsDAO.countPaymentUsage(param);
    }

    @Override
    public int selectMaxPaymentOrder(long userId) {
        return settingsDAO.selectMaxPaymentOrder(userId);
    }

    @Override
    public List<Long> selectPaymentIds(long userId) {
        return settingsDAO.selectPaymentIds(userId);
    }

    @Override
    public int insertPayment(Map<String, Object> param) {
        return settingsDAO.insertPayment(param);
    }

    @Override
    public int updatePayment(Map<String, Object> param) {
        return settingsDAO.updatePayment(param);
    }

    @Override
    public int updatePaymentOrder(Map<String, Object> param) {
        return settingsDAO.updatePaymentOrder(param);
    }

    @Override
    public int deletePayment(Map<String, Object> param) {
        return settingsDAO.deletePayment(param);
    }
}
