package ledger.recurring.service.impl;

import ledger.recurring.dao.RecurringDAO;
import ledger.recurring.service.RecurringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 고정 항목 서비스: DAO 위임
@Service("recurringService")
public class RecurringServiceImpl implements RecurringService {

    @Autowired
    private RecurringDAO recurringDAO;

    // 고정 항목 목록(활성 먼저, 결제일 순)
    @Override
    public List<Map<String, Object>> selectItemList(long userId) {
        return recurringDAO.selectItemList(userId);
    }

    // 내 고정 항목 하나
    @Override
    public Map<String, Object> selectItem(Map<String, Object> param) {
        return recurringDAO.selectItem(param);
    }

    // 스케줄러용: 모든 사용자의 활성 고정 항목
    @Override
    public List<Map<String, Object>> selectActiveItems() {
        return recurringDAO.selectActiveItems();
    }

    // 고정 항목 추가. param.id 에 새 키
    @Override
    public int insertItem(Map<String, Object> param) {
        return recurringDAO.insertItem(param);
    }

    // 고정 항목 수정
    @Override
    public int updateItem(Map<String, Object> param) {
        return recurringDAO.updateItem(param);
    }

    // 사용·중지 전환
    @Override
    public int updateItemActive(Map<String, Object> param) {
        return recurringDAO.updateItemActive(param);
    }

    // 고정 항목 삭제. 이미 만든 거래는 남는다
    @Override
    public int deleteItem(Map<String, Object> param) {
        return recurringDAO.deleteItem(param);
    }

    // 그 달 처리 기록. 이미 있으면 0(중복 생성 방지)
    @Override
    public int insertRun(Map<String, Object> param) {
        return recurringDAO.insertRun(param);
    }

    // 처리 기록에 만든 거래 연결
    @Override
    public int updateRunEntry(Map<String, Object> param) {
        return recurringDAO.updateRunEntry(param);
    }

    // 처리 기록과 기록된 거래 금액(이번 주기 상태 표시용)
    @Override
    public List<Map<String, Object>> selectRuns(Map<String, Object> param) {
        return recurringDAO.selectRuns(param);
    }
}
