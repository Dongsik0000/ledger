package ledger.cycle;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 기준 요일: 2026-09-25 금, 2026-10-25 일, 2026-10-31 토, 2026-11-01 일, 2026-10-01 목, 2026-12-01 화
class PayCycleTest {

    private static final Set<LocalDate> NONE = Set.of();

    private static LocalDate d(String s) {
        return LocalDate.parse(s);
    }

    private static PayCycle.Cycle cycle(String start, String end) {
        return new PayCycle.Cycle(d(start), d(end));
    }

    // ---- payDate ----

    @Test
    void payDateClampsToMonthEnd() {
        assertEquals(d("2026-02-28"), PayCycle.payDate(YearMonth.of(2026, 2), 31, false, NONE));
        assertEquals(d("2028-02-29"), PayCycle.payDate(YearMonth.of(2028, 2), 31, false, NONE));
        assertEquals(d("2026-04-30"), PayCycle.payDate(YearMonth.of(2026, 4), 31, false, NONE));
    }

    @Test
    void payDateAdjustsWeekendToPreviousWeekday() {
        assertEquals(d("2026-10-23"), PayCycle.payDate(YearMonth.of(2026, 10), 25, true, NONE));
        assertEquals(d("2026-10-25"), PayCycle.payDate(YearMonth.of(2026, 10), 25, false, NONE));
    }

    @Test
    void payDateSkipsHolidaysToo() {
        assertEquals(d("2026-10-22"), PayCycle.payDate(YearMonth.of(2026, 10), 25, true, Set.of(d("2026-10-23"))));
    }

    @Test
    void payDateCanMoveIntoPreviousMonth() {
        assertEquals(d("2026-10-30"), PayCycle.payDate(YearMonth.of(2026, 11), 1, true, NONE));
    }

    // ---- cycleOf ----

    @Test
    void payDayOneIsCalendarMonth() {
        assertEquals(cycle("2026-10-01", "2026-10-31"), PayCycle.cycleOf(d("2026-10-15"), 1, false, NONE));
    }

    @Test
    void payDay25SpansMonths() {
        assertEquals(cycle("2026-09-25", "2026-10-24"), PayCycle.cycleOf(d("2026-10-15"), 25, false, NONE));
        assertEquals(cycle("2026-09-25", "2026-10-24"), PayCycle.cycleOf(d("2026-10-24"), 25, false, NONE));
        assertEquals(cycle("2026-10-25", "2026-11-24"), PayCycle.cycleOf(d("2026-10-25"), 25, false, NONE));
    }

    @Test
    void payDay31UsesMonthEnds() {
        assertEquals(cycle("2026-02-28", "2026-03-30"), PayCycle.cycleOf(d("2026-03-05"), 31, false, NONE));
    }

    @Test
    void adjustedStartIsInclusiveAndPreviousCycleEndsBeforeIt() {
        assertEquals(cycle("2026-10-23", "2026-11-24"), PayCycle.cycleOf(d("2026-10-23"), 25, true, NONE));
        assertEquals(cycle("2026-09-25", "2026-10-22"), PayCycle.cycleOf(d("2026-10-22"), 25, true, NONE));
    }

    // 11/1(일) 급여일이 10/30(금)으로 당겨지면 10/30 부터 새 주기
    @Test
    void adjustmentIntoPreviousMonthStartsNextCycleEarly() {
        assertEquals(cycle("2026-10-30", "2026-11-30"), PayCycle.cycleOf(d("2026-10-31"), 1, true, NONE));
        assertEquals(cycle("2026-10-01", "2026-10-29"), PayCycle.cycleOf(d("2026-10-29"), 1, true, NONE));
    }

    // ---- dueDate ----

    @Test
    void dueDateAdjustments() {
        YearMonth oct = YearMonth.of(2026, 10);
        assertEquals(d("2026-10-31"), PayCycle.dueDate(oct, 31, "NONE", NONE));
        assertEquals(d("2026-10-30"), PayCycle.dueDate(oct, 31, "PREV_BIZ", NONE));
        assertEquals(d("2026-11-02"), PayCycle.dueDate(oct, 31, "NEXT_BIZ", NONE));
        assertEquals(d("2026-02-28"), PayCycle.dueDate(YearMonth.of(2026, 2), 31, "NONE", NONE));
    }

    // ---- duesInCycle ----

    @Test
    void duesInCycleFindsTheDueDateInsideCycle() {
        PayCycle.Cycle c = cycle("2026-09-25", "2026-10-24");
        assertEquals(List.of(new PayCycle.Due(YearMonth.of(2026, 10), d("2026-10-05"))),
                PayCycle.duesInCycle(c, 5, "NONE", NONE));
        assertEquals(List.of(new PayCycle.Due(YearMonth.of(2026, 9), d("2026-09-27"))),
                PayCycle.duesInCycle(c, 27, "NONE", NONE));
    }

    // 10/31(토) NEXT_BIZ → 11/2 : 10월분이 11월 주기에 들어온다. 11월분(11/30 월)도 같은 주기라 한 주기에 두 번
    @Test
    void nextBusinessDayCanFallIntoNextCycle() {
        PayCycle.Cycle nov = cycle("2026-11-01", "2026-11-30");
        assertEquals(List.of(
                        new PayCycle.Due(YearMonth.of(2026, 10), d("2026-11-02")),
                        new PayCycle.Due(YearMonth.of(2026, 11), d("2026-11-30"))),
                PayCycle.duesInCycle(nov, 31, "NEXT_BIZ", NONE));
    }
}
