package ledger.recurring.service.impl;

import ledger.recurring.dao.RecurringDAO;
import ledger.recurring.service.RecurringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("recurringService")
public class RecurringServiceImpl implements RecurringService {

    @Autowired
    private RecurringDAO recurringDAO;

    @Override
    public List<Map<String, Object>> selectItemList(long userId) {
        return recurringDAO.selectItemList(userId);
    }

    @Override
    public Map<String, Object> selectItem(Map<String, Object> param) {
        return recurringDAO.selectItem(param);
    }

    @Override
    public List<Map<String, Object>> selectActiveItems() {
        return recurringDAO.selectActiveItems();
    }

    @Override
    public int insertItem(Map<String, Object> param) {
        return recurringDAO.insertItem(param);
    }

    @Override
    public int updateItem(Map<String, Object> param) {
        return recurringDAO.updateItem(param);
    }

    @Override
    public int updateItemActive(Map<String, Object> param) {
        return recurringDAO.updateItemActive(param);
    }

    @Override
    public int deleteItem(Map<String, Object> param) {
        return recurringDAO.deleteItem(param);
    }

    @Override
    public int insertRun(Map<String, Object> param) {
        return recurringDAO.insertRun(param);
    }

    @Override
    public int updateRunEntry(Map<String, Object> param) {
        return recurringDAO.updateRunEntry(param);
    }

    @Override
    public List<Map<String, Object>> selectRuns(Map<String, Object> param) {
        return recurringDAO.selectRuns(param);
    }
}
