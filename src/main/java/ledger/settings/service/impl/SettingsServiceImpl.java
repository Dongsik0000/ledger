package ledger.settings.service.impl;

import ledger.settings.dao.SettingsDAO;
import ledger.settings.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 설정(주기·카테고리·결제수단) 서비스: DAO 위임
@Service("settingsService")
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private SettingsDAO settingsDAO;

    // 주기 설정(급여일·보정)과 시작 잔액
    @Override
    public Map<String, Object> selectUserSetting(long userId) {
        return settingsDAO.selectUserSetting(userId);
    }

    // 주기 시작일·주말 보정 저장
    @Override
    public int updateUserSetting(Map<String, Object> param) {
        return settingsDAO.updateUserSetting(param);
    }

    // 시작 잔액 저장. 기준일이 null 이면 해제
    @Override
    public int updateOpening(Map<String, Object> param) {
        return settingsDAO.updateOpening(param);
    }

    // 카테고리 전체(지출 먼저, 표시 순서대로, 숨김 포함)
    @Override
    public List<Map<String, Object>> selectCategoryList(long userId) {
        return settingsDAO.selectCategoryList(userId);
    }

    // 내 카테고리 하나
    @Override
    public Map<String, Object> selectCategory(Map<String, Object> param) {
        return settingsDAO.selectCategory(param);
    }

    // 같은 구분·이름 카테고리 수(수정 중인 자기 자신 제외)
    @Override
    public int countCategoryName(Map<String, Object> param) {
        return settingsDAO.countCategoryName(param);
    }

    // 이 카테고리를 쓰는 거래·고정 항목 수
    @Override
    public int countCategoryUsage(Map<String, Object> param) {
        return settingsDAO.countCategoryUsage(param);
    }

    // 구분 안 가장 큰 표시 순서(새 항목은 그 뒤)
    @Override
    public int selectMaxCategoryOrder(Map<String, Object> param) {
        return settingsDAO.selectMaxCategoryOrder(param);
    }

    // 구분 안 카테고리 id(표시 순서대로)
    @Override
    public List<Long> selectCategoryIds(Map<String, Object> param) {
        return settingsDAO.selectCategoryIds(param);
    }

    // 카테고리 추가
    @Override
    public int insertCategory(Map<String, Object> param) {
        return settingsDAO.insertCategory(param);
    }

    // 카테고리 수정
    @Override
    public int updateCategory(Map<String, Object> param) {
        return settingsDAO.updateCategory(param);
    }

    // 카테고리 표시 순서 변경
    @Override
    public int updateCategoryOrder(Map<String, Object> param) {
        return settingsDAO.updateCategoryOrder(param);
    }

    // 카테고리 삭제
    @Override
    public int deleteCategory(Map<String, Object> param) {
        return settingsDAO.deleteCategory(param);
    }

    // 결제수단 전체(표시 순서대로, 숨김 포함)
    @Override
    public List<Map<String, Object>> selectPaymentList(long userId) {
        return settingsDAO.selectPaymentList(userId);
    }

    // 내 결제수단 하나
    @Override
    public Map<String, Object> selectPayment(Map<String, Object> param) {
        return settingsDAO.selectPayment(param);
    }

    // 같은 이름 결제수단 수(수정 중인 자기 자신 제외)
    @Override
    public int countPaymentName(Map<String, Object> param) {
        return settingsDAO.countPaymentName(param);
    }

    // 이 결제수단을 쓰는 거래·고정 항목 수
    @Override
    public int countPaymentUsage(Map<String, Object> param) {
        return settingsDAO.countPaymentUsage(param);
    }

    // 가장 큰 표시 순서(새 항목은 그 뒤)
    @Override
    public int selectMaxPaymentOrder(long userId) {
        return settingsDAO.selectMaxPaymentOrder(userId);
    }

    // 결제수단 id(표시 순서대로)
    @Override
    public List<Long> selectPaymentIds(long userId) {
        return settingsDAO.selectPaymentIds(userId);
    }

    // 결제수단 추가
    @Override
    public int insertPayment(Map<String, Object> param) {
        return settingsDAO.insertPayment(param);
    }

    // 결제수단 수정
    @Override
    public int updatePayment(Map<String, Object> param) {
        return settingsDAO.updatePayment(param);
    }

    // 결제수단 표시 순서 변경
    @Override
    public int updatePaymentOrder(Map<String, Object> param) {
        return settingsDAO.updatePaymentOrder(param);
    }

    // 결제수단 삭제
    @Override
    public int deletePayment(Map<String, Object> param) {
        return settingsDAO.deletePayment(param);
    }
}
