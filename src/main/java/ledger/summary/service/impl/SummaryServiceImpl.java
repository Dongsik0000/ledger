package ledger.summary.service.impl;

import ledger.summary.dao.SummaryDAO;
import ledger.summary.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("summaryService")
public class SummaryServiceImpl implements SummaryService {

    @Autowired
    private SummaryDAO summaryDAO;

    @Override
    public List<Map<String, Object>> selectMonthly(Map<String, Object> param) {
        return summaryDAO.selectMonthly(param);
    }

    @Override
    public List<Map<String, Object>> selectCategoryMonthly(Map<String, Object> param) {
        return summaryDAO.selectCategoryMonthly(param);
    }

    @Override
    public List<Map<String, Object>> selectPaymentMonthly(Map<String, Object> param) {
        return summaryDAO.selectPaymentMonthly(param);
    }

    @Override
    public List<Map<String, Object>> selectGroupExpense(Map<String, Object> param) {
        return summaryDAO.selectGroupExpense(param);
    }
}
