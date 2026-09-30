package ledger.account;

import ledger.account.service.AccountService;
import ledger.cmmn.util.ParamUtil;
import ledger.cycle.PayCycle;
import ledger.holiday.service.HolidayService;
import ledger.recurring.RecurringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// 정기 이체의 이체일 기록. 스케줄러(RecurringScheduler)와 AccountApiController(저장 직후)가 쓴다.
// 이체일 계산·처리 기록 방식은 고정 항목(RecurringGenerator)과 같다: 지난달·이번 달 이체일, 처리 기록을 먼저 넣어 중복 방지.
@Component
public class TransferGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TransferGenerator.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private HolidayService holidayService;

    private final TransactionTemplate tx;

    @Autowired
    public TransferGenerator(PlatformTransactionManager transactionManager) {
        this.tx = new TransactionTemplate(transactionManager);
    }

    // 스케줄러: 모든 사용자의 활성 정기 이체. 한 항목이 실패해도 로그만 남기고 다음 항목을 계속한다
    public int generateAll(LocalDate today) {
        Set<LocalDate> holidays = holidays(today);
        int created = 0;
        for (Map<String, Object> item : accountService.selectActiveRecurring()) {
            try {
                created += generate(item, today, holidays);
            } catch (RuntimeException e) {
                logger.error("정기 이체 자동 기록 실패: id={}", item.get("id"), e);
            }
        }
        return created;
    }

    // 신규·이체일 변경·재활성화 직후: 이미 지난 이체일은 건너뜀(transfer_id 없는 기록)으로 둔다
    public void skipPassed(Map<String, Object> item, LocalDate today) {
        for (PayCycle.Due due : RecurringGenerator.duesBefore(day(item), (String) item.get("adjust"), today, holidays(today))) {
            accountService.insertRecurringRun(ParamUtil.map(
                    "recurringId", item.get("id"), "periodYm", due.month().toString(), "transferId", null));
        }
    }

    // 저장 직후: 이체일이 오늘까지인데 아직 처리되지 않은 분을 바로 기록
    public void generate(Map<String, Object> item, LocalDate today) {
        generate(item, today, holidays(today));
    }

    private int generate(Map<String, Object> item, LocalDate today, Set<LocalDate> holidays) {
        int created = 0;
        for (PayCycle.Due due : RecurringGenerator.duesUpTo(day(item), (String) item.get("adjust"), today, holidays)) {
            if (Boolean.TRUE.equals(tx.execute(status -> generateOne(item, due)))) {
                created++;
            }
        }
        return created;
    }

    // 한 트랜잭션: 처리 기록 → 이체 → 기록에 이체 연결. 기록이 이미 있으면(처리됨·건너뜀) 아무것도 하지 않는다
    private boolean generateOne(Map<String, Object> item, PayCycle.Due due) {
        String periodYm = due.month().toString();
        if (accountService.insertRecurringRun(ParamUtil.map(
                "recurringId", item.get("id"), "periodYm", periodYm, "transferId", null)) == 0) {
            return false;
        }
        Map<String, Object> transfer = ParamUtil.map("userId", item.get("userId"), "transferDate", due.date(),
                "fromAccountId", item.get("fromAccountId"), "toAccountId", item.get("toAccountId"),
                "amount", item.get("amount"), "memo", item.get("memo"));
        accountService.insertTransfer(transfer);
        accountService.updateRecurringRunTransfer(ParamUtil.map(
                "recurringId", item.get("id"), "periodYm", periodYm, "transferId", transfer.get("id")));
        return true;
    }

    private Set<LocalDate> holidays(LocalDate today) {
        return new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                "from", today.minusMonths(2).withDayOfMonth(1), "to", today.plusMonths(1))));
    }

    private static int day(Map<String, Object> item) {
        return ((Number) item.get("dayOfMonth")).intValue();
    }
}
