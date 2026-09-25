package ledger.cycle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 주기(급여일 기준)와 고정 항목 결제일 계산. DB 없이 날짜만 다루는 순수 계산(plan.md "핵심 계산").
public final class PayCycle {

    // 주기: start ~ end (양 끝 포함)
    public record Cycle(LocalDate start, LocalDate end) {
    }

    // 고정 항목 결제 한 번: 어느 달 분(month)이 실제로 언제(date) 결제되는지
    public record Due(YearMonth month, LocalDate date) {
    }

    private PayCycle() {
    }

    // 그 달의 급여일. 짧은 달은 말일로. adjust 면 토·일·공휴일인 동안 하루씩 앞으로(전달로 넘어갈 수 있다)
    public static LocalDate payDate(YearMonth month, int payDay, boolean adjust, Set<LocalDate> holidays) {
        LocalDate d = month.atDay(Math.min(payDay, month.lengthOfMonth()));
        return adjust ? previousBusinessDay(d, holidays) : d;
    }

    // 고정 항목 결제일. adjust: NONE 그대로, PREV_BIZ 직전 평일, NEXT_BIZ 다음 평일
    public static LocalDate dueDate(YearMonth month, int day, String adjust, Set<LocalDate> holidays) {
        LocalDate d = month.atDay(Math.min(day, month.lengthOfMonth()));
        if ("PREV_BIZ".equals(adjust)) {
            return previousBusinessDay(d, holidays);
        }
        if ("NEXT_BIZ".equals(adjust)) {
            while (!isBusinessDay(d, holidays)) {
                d = d.plusDays(1);
            }
        }
        return d;
    }

    // today 가 속한 주기. 급여일이 today 이하인 가장 늦은 기준 월부터, 다음 기준 월 급여일 전날까지.
    // 보정으로 다음 달 급여일이 이번 달로 당겨질 수 있어 다음 달부터 확인한다.
    public static Cycle cycleOf(LocalDate today, int payDay, boolean adjust, Set<LocalDate> holidays) {
        YearMonth base = YearMonth.from(today).plusMonths(1);
        while (payDate(base, payDay, adjust, holidays).isAfter(today)) {
            base = base.minusMonths(1);
        }
        LocalDate start = payDate(base, payDay, adjust, holidays);
        LocalDate end = payDate(base.plusMonths(1), payDay, adjust, holidays).minusDays(1);
        return new Cycle(start, end);
    }

    // 주기 안에 드는 결제일들. 보정으로 앞뒤 달로 넘어오는 경우를 위해 시작 월 전달부터 끝 월 다음 달까지 본다
    public static List<Due> duesInCycle(Cycle cycle, int day, String adjust, Set<LocalDate> holidays) {
        List<Due> dues = new ArrayList<>();
        YearMonth last = YearMonth.from(cycle.end()).plusMonths(1);
        for (YearMonth m = YearMonth.from(cycle.start()).minusMonths(1); !m.isAfter(last); m = m.plusMonths(1)) {
            LocalDate date = dueDate(m, day, adjust, holidays);
            if (!date.isBefore(cycle.start()) && !date.isAfter(cycle.end())) {
                dues.add(new Due(m, date));
            }
        }
        return dues;
    }

    private static LocalDate previousBusinessDay(LocalDate d, Set<LocalDate> holidays) {
        while (!isBusinessDay(d, holidays)) {
            d = d.minusDays(1);
        }
        return d;
    }

    private static boolean isBusinessDay(LocalDate d, Set<LocalDate> holidays) {
        DayOfWeek w = d.getDayOfWeek();
        return w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY && !holidays.contains(d);
    }
}
