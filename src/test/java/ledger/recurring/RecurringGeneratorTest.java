package ledger.recurring;

import ledger.cycle.PayCycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecurringGeneratorTest {

    private static final Set<LocalDate> NONE = Set.of();

    private static PayCycle.Due due(int y, int m, String date) {
        return new PayCycle.Due(YearMonth.of(y, m), LocalDate.parse(date));
    }

    // 오늘이 결제일이면 이번 달 포함, 지난달 분도 대상
    @Test
    void duesUpToIncludesToday() {
        assertEquals(List.of(due(2026, 8, "2026-08-26"), due(2026, 9, "2026-09-26")),
                RecurringGenerator.duesUpTo(26, "NONE", LocalDate.parse("2026-09-26"), NONE));
    }

    // 결제일이 아직 안 온 이번 달은 제외
    @Test
    void duesUpToExcludesFuture() {
        assertEquals(List.of(due(2026, 8, "2026-08-27")),
                RecurringGenerator.duesUpTo(27, "NONE", LocalDate.parse("2026-09-26"), NONE));
    }

    // 건너뛸 대상은 오늘보다 앞선 결제일만(오늘 결제일은 바로 기록하므로 제외)
    @Test
    void duesBeforeExcludesToday() {
        assertEquals(List.of(due(2026, 8, "2026-08-26")),
                RecurringGenerator.duesBefore(26, "NONE", LocalDate.parse("2026-09-26"), NONE));
        assertEquals(List.of(due(2026, 8, "2026-08-05"), due(2026, 9, "2026-09-05")),
                RecurringGenerator.duesBefore(5, "NONE", LocalDate.parse("2026-09-26"), NONE));
    }

    // 11/1(일) PREV_BIZ → 10/30(금): 다음 달분이 이번 달로 당겨지면 그날 기록 대상이어야 한다
    @Test
    void prevBusinessDayFromNextMonthIsDueToday() {
        assertEquals(List.of(due(2026, 9, "2026-09-01"), due(2026, 10, "2026-10-01"), due(2026, 11, "2026-10-30")),
                RecurringGenerator.duesUpTo(1, "PREV_BIZ", LocalDate.parse("2026-10-30"), NONE));
    }

    // 10/31 에 새로 만든 항목: 이미 지난 11월분(10/30)도 건너뛸 대상
    @Test
    void prevBusinessDayFromNextMonthIsSkippableAfterwards() {
        assertEquals(List.of(due(2026, 9, "2026-09-01"), due(2026, 10, "2026-10-01"), due(2026, 11, "2026-10-30")),
                RecurringGenerator.duesBefore(1, "PREV_BIZ", LocalDate.parse("2026-10-31"), NONE));
    }

    // 8/31(월) → 그대로, 9/30(수). NEXT_BIZ 로 다음 달로 넘어가도 "몇 월분"은 원래 달
    @Test
    void nextBusinessDayKeepsOriginalMonth() {
        // 2026-10-31(토) NEXT_BIZ → 11/2. 11/1 기준으로는 10월분이 아직 오지 않음
        assertEquals(List.of(), RecurringGenerator.duesUpTo(31, "NEXT_BIZ", LocalDate.parse("2026-11-01"), NONE));
        assertEquals(List.of(due(2026, 10, "2026-11-02")),
                RecurringGenerator.duesUpTo(31, "NEXT_BIZ", LocalDate.parse("2026-11-02"), NONE));
    }

    private static Map<String, Object> installment(int day, String adjust, int months, String start) {
        Map<String, Object> item = new HashMap<>();
        item.put("dayOfMonth", day);
        item.put("adjust", adjust);
        item.put("amount", 109_140L);
        item.put("installmentTotal", 327_420L);
        item.put("installmentMonths", months);
        item.put("installmentStart", start);
        return item;
    }

    // 할부 3회(10·11·12월분): 마지막 결제일(12/1) 전에는 계속, 당일부터 끝
    @Test
    void installmentFinishesOnLastDueDate() {
        Map<String, Object> item = installment(1, "NEXT_BIZ", 3, "2026-10");
        assertEquals(false, RecurringGenerator.isFinished(item, LocalDate.parse("2026-11-30"), NONE));
        assertEquals(true, RecurringGenerator.isFinished(item, LocalDate.parse("2026-12-01"), NONE));
    }

    // 마지막 결제일이 휴일로 밀리면(2027-08-01 일 → 8/2) 밀린 날까지 기다린다
    @Test
    void installmentFinishWaitsForAdjustedDate() {
        Map<String, Object> item = installment(1, "NEXT_BIZ", 2, "2027-07");
        assertEquals(false, RecurringGenerator.isFinished(item, LocalDate.parse("2027-08-01"), NONE));
        assertEquals(true, RecurringGenerator.isFinished(item, LocalDate.parse("2027-08-02"), NONE));
    }

    // 일반 고정 항목은 끝나지 않는다
    @Test
    void regularItemNeverFinishes() {
        Map<String, Object> item = new HashMap<>();
        item.put("dayOfMonth", 1);
        item.put("adjust", "NONE");
        item.put("amount", 20_000L);
        assertEquals(false, RecurringGenerator.isFinished(item, LocalDate.parse("2030-01-01"), NONE));
    }

    // 할부 거래 내용에는 회차를 붙이고, 100자를 넘지 않게 이름을 줄인다
    @Test
    void installmentTitleHasRound() {
        Map<String, Object> item = installment(1, "NEXT_BIZ", 3, "2026-10");
        item.put("name", "노트북");
        assertEquals("노트북 (2/3)", RecurringGenerator.title(item, YearMonth.of(2026, 11)));

        item.put("name", "가".repeat(100));
        String title = RecurringGenerator.title(item, YearMonth.of(2026, 12));
        assertEquals(100, title.length());
        assertEquals(true, title.endsWith(" (3/3)"));

        Map<String, Object> regular = new HashMap<>();
        regular.put("name", "넷플릭스");
        assertEquals("넷플릭스", RecurringGenerator.title(regular, YearMonth.of(2026, 11)));
    }
}
