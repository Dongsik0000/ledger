package ledger.recurring;

import ledger.cmmn.util.Constants;
import ledger.cmmn.util.ParamUtil;
import ledger.cycle.PayCycle;
import ledger.entry.service.EntryService;
import ledger.holiday.service.HolidayService;
import ledger.market.MarketDataClient;
import ledger.market.MarketValue;
import ledger.recurring.service.RecurringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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

    @Autowired
    private MarketDataClient marketData;

    private final TransactionTemplate tx;

    // 항목 한 건마다 트랜잭션을 따로 쓴다(한 건 실패가 다른 항목을 되돌리지 않게)
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

    // 신규·결제일 변경·재활성화 직후: 이미 지난 결제일은 건너뜀(entry_id 없는 기록)으로 둔다
    public void skipPassed(Map<String, Object> item, LocalDate today) {
        for (PayCycle.Due due : RecurringSchedule.duesBefore(RecurringSchedule.day(item), (String) item.get("adjust"),
                today, holidays(today))) {
            if (Installment.amountFor(item, due.month()) == null) {
                continue;   // 할부 기간 밖
            }
            recurringService.insertRun(ParamUtil.map(
                    "recurringId", item.get("id"), "periodYm", due.month().toString(), "entryId", null));
        }
    }

    // 저장 직후: 결제일이 오늘까지인데 아직 처리되지 않은 분을 바로 기록
    public void generate(Map<String, Object> item, LocalDate today) {
        generate(item, today, holidays(today));
    }

    // 저장 전 확인: 할부 마지막 회차 결제일이 이미 왔는지
    public boolean isFinished(Map<String, Object> item, LocalDate today) {
        return RecurringSchedule.isFinished(item, today, holidays(today));
    }

    // 처리되지 않은 결제일을 기록하고 새로 만든 건수를 돌려준다. 할부는 마지막 회차가 지나면 항목을 지운다(거래는 남는다)
    private int generate(Map<String, Object> item, LocalDate today, Set<LocalDate> holidays) {
        int created = 0;
        List<PayCycle.Due> dueDates = RecurringSchedule.duesUpTo(RecurringSchedule.day(item), (String) item.get("adjust"),
                today, holidays);
        Set<String> handled = new HashSet<>();
        if (!dueDates.isEmpty()) {
            List<String> months = dueDates.stream().map(d -> d.month().toString()).toList();
            for (Map<String, Object> run : recurringService.selectRuns(ParamUtil.map(
                    "userId", item.get("userId"), "months", months))) {
                if (((Number) run.get("recurringId")).longValue() == RecurringSchedule.id(item)) {
                    handled.add((String) run.get("periodYm"));
                }
            }
        }
        for (PayCycle.Due due : dueDates) {
            if (handled.contains(due.month().toString())) {
                continue;
            }
            Long amount = Installment.amountFor(item, due.month());
            if (amount == null) {
                continue;   // 할부 기간 밖
            }
            MarketDataClient.FxQuote fx = null;
            if (item.get("usdAmount") != null) {
                fx = marketData.usdKrwAt(due.date(), today);
                amount = MarketValue.krw((BigDecimal) item.get("usdAmount"), fx.rate());
                if (amount < 1 || amount > Constants.AMOUNT_MAX) {
                    throw new IllegalStateException("외화 고정지출 계산 금액이 허용 범위를 넘었습니다");
                }
            }
            long recordedAmount = amount;
            MarketDataClient.FxQuote usedFx = fx;
            if (Boolean.TRUE.equals(tx.execute(status -> generateOne(item, due, recordedAmount, usedFx)))) {
                created++;
            }
        }
        if (RecurringSchedule.isFinished(item, today, holidays)) {
            recurringService.deleteItem(ParamUtil.map("id", item.get("id"), "userId", item.get("userId")));
            logger.info("할부 마지막 회차 이후 고정 항목 삭제: id={}", item.get("id"));
        }
        return created;
    }

    // 한 트랜잭션: 처리 기록 → 거래 → 기록에 거래 연결. 기록이 이미 있으면(처리됨·건너뜀) 아무것도 하지 않는다
    private boolean generateOne(Map<String, Object> item, PayCycle.Due due, long amount, MarketDataClient.FxQuote fx) {
        String periodYm = due.month().toString();
        if (recurringService.insertRun(ParamUtil.map(
                "recurringId", item.get("id"), "periodYm", periodYm, "entryId", null,
                "fxRate", fx == null ? null : fx.rate(), "fxAsOf", fx == null ? null : fx.asOf())) == 0) {
            return false;
        }
        Map<String, Object> entry = ParamUtil.map("userId", item.get("userId"), "entryDate", due.date(),
                "type", item.get("type"), "categoryId", item.get("categoryId"), "title", title(item, due.month()),
                "amount", amount, "paymentMethodId", item.get("paymentMethodId"), "memo", item.get("memo"),
                "transferAssetId", item.get("transferAssetId"), "accountId", item.get("accountId"));
        entryService.insertEntry(entry);
        recurringService.updateRunEntry(ParamUtil.map("recurringId", item.get("id"), "periodYm", periodYm, "entryId", entry.get("id")));
        return true;
    }

    // 결제일 보정에 쓰는 앞뒤 공휴일(지난달·이번 달·다음 달 결제일을 덮는 범위)
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
}
