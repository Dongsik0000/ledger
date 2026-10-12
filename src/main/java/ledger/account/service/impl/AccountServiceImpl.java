package ledger.account.service.impl;

import ledger.account.dao.AccountDAO;
import ledger.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 통장·이체·정기 이체 서비스: DAO 위임
@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountDAO accountDAO;

    // 통장 목록과 잔액(기준 잔액 + to 까지의 거래·이체)
    @Override
    public List<Map<String, Object>> selectAccountList(Map<String, Object> param) {
        return accountDAO.selectAccountList(param);
    }

    // 입력 창의 통장 선택지(기본 통장 먼저)
    @Override
    public List<Map<String, Object>> selectAccountOptions(long userId) {
        return accountDAO.selectAccountOptions(userId);
    }

    // 내 통장 하나. 없거나 남의 것이면 null
    @Override
    public Map<String, Object> selectAccount(Map<String, Object> param) {
        return accountDAO.selectAccount(param);
    }

    // 같은 이름 통장 수(수정 중인 자기 자신 제외)
    @Override
    public int countAccountName(Map<String, Object> param) {
        return accountDAO.countAccountName(param);
    }

    // 이 통장을 쓰는 거래·고정 항목·이체 수
    @Override
    public int countAccountUsage(Map<String, Object> param) {
        return accountDAO.countAccountUsage(param);
    }

    // 통장 추가
    @Override
    public int insertAccount(Map<String, Object> param) {
        return accountDAO.insertAccount(param);
    }

    // 통장 수정. 기준 잔액·기준일이 바뀌면 기준 시각도 새로
    @Override
    public int updateAccount(Map<String, Object> param) {
        return accountDAO.updateAccount(param);
    }

    // 기존 기본 통장 해제(기본 통장은 사용자당 하나)
    @Override
    public int clearDefault(Map<String, Object> param) {
        return accountDAO.clearDefault(param);
    }

    // 통장 삭제. 정기 이체는 함께 삭제
    @Override
    public int deleteAccount(Map<String, Object> param) {
        return accountDAO.deleteAccount(param);
    }

    // 최근 이체 목록(최신순 limit 건)
    @Override
    public List<Map<String, Object>> selectTransferList(Map<String, Object> param) {
        return accountDAO.selectTransferList(param);
    }

    // 이체 추가. param.id 에 새 키
    @Override
    public int insertTransfer(Map<String, Object> param) {
        return accountDAO.insertTransfer(param);
    }

    // 이체 수정
    @Override
    public int updateTransfer(Map<String, Object> param) {
        return accountDAO.updateTransfer(param);
    }

    // 이체 삭제
    @Override
    public int deleteTransfer(Map<String, Object> param) {
        return accountDAO.deleteTransfer(param);
    }

    // 정기 이체 목록
    @Override
    public List<Map<String, Object>> selectRecurringList(long userId) {
        return accountDAO.selectRecurringList(userId);
    }

    // 내 정기 이체 하나
    @Override
    public Map<String, Object> selectRecurring(Map<String, Object> param) {
        return accountDAO.selectRecurring(param);
    }

    // 스케줄러용: 모든 사용자의 활성 정기 이체
    @Override
    public List<Map<String, Object>> selectActiveRecurring() {
        return accountDAO.selectActiveRecurring();
    }

    // 정기 이체 추가. param.id 에 새 키
    @Override
    public int insertRecurring(Map<String, Object> param) {
        return accountDAO.insertRecurring(param);
    }

    // 정기 이체 수정
    @Override
    public int updateRecurring(Map<String, Object> param) {
        return accountDAO.updateRecurring(param);
    }

    // 정기 이체 삭제. 이미 기록된 이체는 남는다
    @Override
    public int deleteRecurring(Map<String, Object> param) {
        return accountDAO.deleteRecurring(param);
    }

    // 정기 이체의 그 달 처리 기록. 이미 있으면 0(중복 기록 방지)
    @Override
    public int insertRecurringRun(Map<String, Object> param) {
        return accountDAO.insertRecurringRun(param);
    }

    // 처리 기록에 만든 이체 연결
    @Override
    public int updateRecurringRunTransfer(Map<String, Object> param) {
        return accountDAO.updateRecurringRunTransfer(param);
    }

    // 처리된(건너뜀 포함) "정기이체id|YYYY-MM" 목록
    @Override
    public List<String> selectRecurringRunKeys(Map<String, Object> param) {
        return accountDAO.selectRecurringRunKeys(param);
    }
}
