package ledger.recurring;

import ledger.cycle.PayCycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
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
}
