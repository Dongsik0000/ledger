package ledger.recurring;

import ledger.cmmn.util.ParamUtil;
import ledger.cycle.PayCycle;
import ledger.entry.service.EntryService;
import ledger.holiday.service.HolidayService;
import ledger.recurring.service.RecurringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 고정 항목의 결제일 거래를 만든다. 스케줄러(매일·기동 시)와 RecurringApiController(저장·활성화 직후)가 쓴다.
// 대상은 지난달·이번 달 결제일. 처리 기록(recurring_run)을 먼저 넣고 넣었을 때만 거래를 만들어 중복을 막는다.
@Component
public class RecurringGenerator {

    private static final Logger logger = LoggerFactory.getLogger(RecurringGenerator.class);
    private static final int TITLE_MAX = 100;   // ledger_entry.title VARCHAR(100)

    @Autowired
    private RecurringService recurringService;

    @Autowired
    private EntryService entryService;

    @Autowired
    private HolidayService holidayService;

    private final TransactionTemplate tx;

    @Autowired
    public RecurringGenerator(PlatformTransactionManager transactionManager) {
        this.tx = new TransactionTemplate(transactionManager);
    }

    // 스케줄러: 모든 사용자의 활성 항목. 한 항목이 실패해도 로그만 남기고 다음 항목을 계속한다
    public int generateAll(LocalDate today) {
        Set<LocalDate> holidays = holidays(today);
        int created = 0;
        for (Map<String, Object> item : recurringService.selectActiveItems()) {
            try {
                created += generate(item, today, holidays);
            } catch (RuntimeException e) {
                logger.error("고정 항목 자동 기록 실패: id={}", item.get("id"), e);
            }
        }
        return created;
    }

    // 신규·결제일 변경·재활성화 직후: 이미 지난 결제일은 건너뜀(entry_id 없는 기록)으로 둔다.
    // item: id, userId, name, type, amount, categoryId, paymentMethodId, dayOfMonth, adjust, memo, installment*
    public void skipPassed(Map<String, Object> item, LocalDate today) {
        for (PayCycle.Due due : duesBefore(day(item), (String) item.get("adjust"), today, holidays(today))) {
            if (Installment.amountFor(item, due.month()) == null) {
                continue;   // 할부 기간 밖
            }
            recurringService.insertRun(ParamUtil.map(
                    "recurringId", item.get("id"), "periodYm", due.month().toString(), "entryId", null));
        }
    }

    // 저장 직후: 결제일이 오늘까지인데 아직 처리되지 않은 분을 바로 기록(오늘이 결제일이면 오늘 기록)
    public void generate(Map<String, Object> item, LocalDate today) {
        generate(item, today, holidays(today));
    }

    // 저장 전 확인용: 할부 마지막 회차 결제일이 이미 왔는지
    public boolean isFinished(Map<String, Object> item, LocalDate today) {
        return isFinished(item, today, holidays(today));
    }

    // 할부의 마지막 회차 결제일이 오늘까지 왔으면 끝(그 회차는 기록 또는 건너뜀 처리됨). 일반 항목은 끝나지 않는다
    static boolean isFinished(Map<String, Object> item, LocalDate today, Set<LocalDate> holidays) {
        if (!Installment.isInstallment(item)) {
            return false;
        }
        YearMonth last = Installment.lastMonth(Installment.start(item), Installment.months(item));
        return !PayCycle.dueDate(last, day(item), (String) item.get("adjust"), holidays).isAfter(today);
    }

    // 지난달·이번 달 결제일 중 today 이하(생성 대상)
    static List<PayCycle.Due> duesUpTo(int day, String adjust, LocalDate today, Set<LocalDate> holidays) {
        List<PayCycle.Due> dues = new ArrayList<>();
        for (PayCycle.Due due : candidates(day, adjust, today, holidays)) {
            if (!due.date().isAfter(today)) {
                dues.add(due);
            }
        }
        return dues;
    }

    // 지난달·이번 달 결제일 중 today 보다 앞선 것(건너뛸 대상)
    static List<PayCycle.Due> duesBefore(int day, String adjust, LocalDate today, Set<LocalDate> holidays) {
        List<PayCycle.Due> dues = new ArrayList<>();
        for (PayCycle.Due due : candidates(day, adjust, today, holidays)) {
            if (due.date().isBefore(today)) {
                dues.add(due);
            }
        }
        return dues;
    }

    // 지난달·이번 달·다음 달분. 다음 달분은 PREV_BIZ 보정으로 이번 달로 당겨질 수 있다(예: 11/1 일요일 → 10/30)
    private static List<PayCycle.Due> candidates(int day, String adjust, LocalDate today, Set<LocalDate> holidays) {
        YearMonth thisMonth = YearMonth.from(today);
        List<PayCycle.Due> dues = new ArrayList<>();
        for (YearMonth m : List.of(thisMonth.minusMonths(1), thisMonth, thisMonth.plusMonths(1))) {
            dues.add(new PayCycle.Due(m, PayCycle.dueDate(m, day, adjust, holidays)));
        }
        return dues;
    }

    // 할부는 마지막 회차 결제일이 지나면 항목을 지운다(기록된 거래는 남는다)
    private int generate(Map<String, Object> item, LocalDate today, Set<LocalDate> holidays) {
        int created = 0;
        for (PayCycle.Due due : duesUpTo(day(item), (String) item.get("adjust"), today, holidays)) {
            Long amount = Installment.amountFor(item, due.month());
            if (amount == null) {
                continue;   // 할부 기간 밖
            }
            Boolean done = tx.execute(status -> generateOne(item, due, amount));
            if (Boolean.TRUE.equals(done)) {
                created++;
            }
        }
        if (isFinished(item, today, holidays)) {
            recurringService.deleteItem(ParamUtil.map("id", item.get("id"), "userId", item.get("userId")));
            logger.info("할부 마지막 회차 이후 고정 항목 삭제: id={}", item.get("id"));
        }
        return created;
    }

    // 한 트랜잭션: 처리 기록 → 거래 → 기록에 거래 연결. 기록이 이미 있으면(처리됨·건너뜀) 아무것도 하지 않는다
    private boolean generateOne(Map<String, Object> item, PayCycle.Due due, long amount) {
        String periodYm = due.month().toString();
        if (recurringService.insertRun(ParamUtil.map(
                "recurringId", item.get("id"), "periodYm", periodYm, "entryId", null)) == 0) {
            return false;
        }
        Map<String, Object> entry = ParamUtil.map("userId", item.get("userId"), "entryDate", due.date(),
                "type", item.get("type"), "categoryId", item.get("categoryId"), "title", title(item, due.month()),
                "amount", amount, "paymentMethodId", item.get("paymentMethodId"), "memo", item.get("memo"));
        entryService.insertEntry(entry);
        recurringService.updateRunEntry(ParamUtil.map("recurringId", item.get("id"), "periodYm", periodYm, "entryId", entry.get("id")));
        return true;
    }

    private Set<LocalDate> holidays(LocalDate today) {
        return new HashSet<>(holidayService.selectHolidayDates(ParamUtil.map(
                "from", today.minusMonths(2).withDayOfMonth(1), "to", today.plusMonths(1))));
    }

    // 할부 거래는 "이름 (1/3)". 거래 내용은 100자까지라 이름을 줄인다
    static String title(Map<String, Object> item, YearMonth period) {
        String name = (String) item.get("name");
        if (!Installment.isInstallment(item)) {
            return name;
        }
        int months = Installment.months(item);
        String suffix = " (" + Installment.roundOf(Installment.start(item), months, period) + "/" + months + ")";
        return name.length() + suffix.length() > TITLE_MAX ? name.substring(0, TITLE_MAX - suffix.length()) + suffix : name + suffix;
    }

    private static int day(Map<String, Object> item) {
        return ((Number) item.get("dayOfMonth")).intValue();
    }
}
