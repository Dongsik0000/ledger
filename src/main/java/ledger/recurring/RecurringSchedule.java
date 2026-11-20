package ledger.recurring;

import ledger.cycle.PayCycle;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

// 고정 항목·정기 이체의 결제일 계산. DB 없이 항목 map(dayOfMonth, adjust, installment*)과 날짜만 다룬다.
public final class RecurringSchedule {

    private RecurringSchedule() {
    }

    // 지난달·이번 달 결제일 중 today 이하(생성 대상)
    public static List<PayCycle.Due> duesUpTo(int day, String adjust, LocalDate today, Set<LocalDate> holidays) {
        List<PayCycle.Due> dues = new ArrayList<>();
        for (PayCycle.Due due : candidates(day, adjust, today, holidays)) {
            if (!due.date().isAfter(today)) {
                dues.add(due);
            }
        }
        return dues;
    }

    // 지난달·이번 달 결제일 중 today 보다 앞선 것(건너뛸 대상)
    public static List<PayCycle.Due> duesBefore(int day, String adjust, LocalDate today, Set<LocalDate> holidays) {
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

    // 항목 id → 주기 안 결제일(할부 기간 밖은 뺀다)
    public static Map<Long, List<PayCycle.Due>> duesInCycle(List<Map<String, Object>> items, PayCycle.Cycle cycle,
                                                            Set<LocalDate> holidays) {
        Map<Long, List<PayCycle.Due>> duesByItem = new HashMap<>();
        for (Map<String, Object> item : items) {
            List<PayCycle.Due> dues = PayCycle.duesInCycle(cycle, day(item), (String) item.get("adjust"), holidays);
            dues.removeIf(d -> Installment.amountFor(item, d.month()) == null);
            duesByItem.put(id(item), dues);
        }
        return duesByItem;
    }

    // 오늘 이후(오늘 포함) 아직 처리하지 않은 첫 결제일. 할부는 기간 안에서만 찾고, 마지막 회차가 지났으면 null.
    // 보정(PREV_BIZ)으로 다음 달분이 이번 달로 당겨질 수 있어 지난달분부터 본다
    public static PayCycle.Due nextDue(Map<String, Object> item, LocalDate today, Set<LocalDate> holidays,
                                       Predicate<YearMonth> handled) {
        boolean installment = Installment.isInstallment(item);
        YearMonth m = YearMonth.from(today).minusMonths(1);
        if (installment && Installment.start(item).isAfter(m)) {
            m = Installment.start(item);
        }
        for (int i = 0; i < 24; i++, m = m.plusMonths(1)) {
            if (installment && m.isAfter(Installment.lastMonth(Installment.start(item), Installment.months(item)))) {
                return null;
            }
            LocalDate date = PayCycle.dueDate(m, day(item), (String) item.get("adjust"), holidays);
            if (!date.isBefore(today) && !handled.test(m)) {
                return new PayCycle.Due(m, date);
            }
        }
        return null;
    }

    // 할부의 마지막 회차 결제일이 오늘까지 왔으면 끝. 일반 항목은 끝나지 않는다
    public static boolean isFinished(Map<String, Object> item, LocalDate today, Set<LocalDate> holidays) {
        if (!Installment.isInstallment(item)) {
            return false;
        }
        YearMonth last = Installment.lastMonth(Installment.start(item), Installment.months(item));
        return !PayCycle.dueDate(last, day(item), (String) item.get("adjust"), holidays).isAfter(today);
    }

    // 저장 직후 지난 결제일을 건너뛸지: 신규·재활성화·결제일/보정 변경·할부 일정 변경일 때만.
    // 금액·이름만 바꾼 수정에서 건너뛰면 스케줄러 실패로 누락된 결제가 조용히 "건너뜀"으로 가려진다
    public static boolean shouldSkipPassed(Map<String, Object> saved, Map<String, Object> item) {
        if (saved == null || !Boolean.TRUE.equals(saved.get("active"))) {
            return true;
        }
        return !Objects.equals(number(saved.get("dayOfMonth")), number(item.get("dayOfMonth")))
                || !Objects.equals(saved.get("adjust"), item.get("adjust"))
                || !Objects.equals(text(saved.get("installmentStart")), text(item.get("installmentStart")))
                || !Objects.equals(number(saved.get("installmentMonths")), number(item.get("installmentMonths")));
    }

    // 매월 결제일(1~31)
    public static int day(Map<String, Object> item) {
        return ((Number) item.get("dayOfMonth")).intValue();
    }

    // 항목 id
    public static long id(Map<String, Object> item) {
        return ((Number) item.get("id")).longValue();
    }

    // DB(Short·Integer)와 요청 값(Integer)의 타입 차이를 무시하고 비교
    private static Long number(Object v) {
        return v == null ? null : ((Number) v).longValue();
    }

    // CHAR 공백을 무시하고 비교
    private static String text(Object v) {
        return v == null ? null : v.toString().trim();
    }
}
