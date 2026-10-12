package ledger.summary.service.impl;

import ledger.summary.dao.SummaryDAO;
import ledger.summary.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

// 요약 집계 서비스: DAO 위임
@Service("summaryService")
public class SummaryServiceImpl implements SummaryService {

    @Autowired
    private SummaryDAO summaryDAO;

    // 월(주기)별 수입·지출
    @Override
    public List<Map<String, Object>> selectMonthly(Map<String, Object> param) {
        return summaryDAO.selectMonthly(param);
    }

    // 카테고리 × 월(주기) 지출
    @Override
    public List<Map<String, Object>> selectCategoryMonthly(Map<String, Object> param) {
        return summaryDAO.selectCategoryMonthly(param);
    }

    // 결제수단 × 월(주기) 지출
    @Override
    public List<Map<String, Object>> selectPaymentMonthly(Map<String, Object> param) {
        return summaryDAO.selectPaymentMonthly(param);
    }

    // 한 기간 지출을 그룹별로(도넛 차트)
    @Override
    public List<Map<String, Object>> selectGroupExpense(Map<String, Object> param) {
        return summaryDAO.selectGroupExpense(param);
    }
}
