package ledger.recurring;

import ledger.cmmn.util.ParamUtil;
import ledger.cycle.PayCycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 결제일 계산·할부 종료·건너뛰기 판단·다음 결제일 테스트
class RecurringScheduleTest {

    private static final Set<LocalDate> NONE = Set.of();

    // 저장돼 있던 항목 값(결제일·보정·사용 여부)
    private static Map<String, Object> saved(int day, String adjust, boolean active) {
        return ParamUtil.map("dayOfMonth", day, "adjust", adjust, "active", active);
    }

    // 새로 받은 항목 값(결제일·보정)
    private static Map<String, Object> item(int day, String adjust) {
        return ParamUtil.map("dayOfMonth", day, "adjust", adjust);
    }

    // 1일 결제·다음 영업일 보정 할부 항목
    private static Map<String, Object> installment(String start, int months) {
        return ParamUtil.map("dayOfMonth", 1, "adjust", "NEXT_BIZ", "active", true,
                "installmentStart", start, "installmentMonths", months);
    }

    // 금액·이름만 바꾼 수정은 지난 결제일을 건너뛰지 않는다(스케줄러 누락이 조용히 "건너뜀"이 되지 않게)
    @Test
    void amountOnlyChangeDoesNotSkip() {
        assertFalse(RecurringSchedule.shouldSkipPassed(saved(5, "NONE", true), item(5, "NONE")));
    }

    // 결제일·보정을 바꾸면 건너뛴다
    @Test
    void dayOrAdjustChangeSkips() {
        assertTrue(RecurringSchedule.shouldSkipPassed(saved(5, "NONE", true), item(6, "NONE")));
        assertTrue(RecurringSchedule.shouldSkipPassed(saved(5, "NONE", true), item(5, "PREV_BIZ")));
    }

    // 다시 켜면 건너뛴다
    @Test
    void reactivationSkips() {
        assertTrue(RecurringSchedule.shouldSkipPassed(saved(5, "NONE", false), item(5, "NONE")));
    }

    // 신규 저장(saved 없음)은 건너뛴다
    @Test
    void newItemSkips() {
        assertTrue(RecurringSchedule.shouldSkipPassed(null, item(5, "NONE")));
    }

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 26);

    // 일반 항목(금액 2만 원)
    private static Map<String, Object> regular(int day, String adjust) {
        return ParamUtil.map("dayOfMonth", day, "adjust", adjust, "amount", 20_000L);
    }

    // 기대 결제일(회차 월·날짜)
    private static PayCycle.Due due(int y, int m, String date) {
        return new PayCycle.Due(YearMonth.of(y, m), LocalDate.parse(date));
    }

    // 다음 결제일: 오늘 이후 첫 결제일
    @Test
    void nextDueIsFirstUpcoming() {
        assertEquals(due(2026, 10, "2026-10-01"), RecurringSchedule.nextDue(regular(1, "NONE"), TODAY, Set.of(), p -> false));
        assertEquals(due(2026, 9, "2026-09-28"), RecurringSchedule.nextDue(regular(28, "NONE"), TODAY, Set.of(), p -> false));
    }

    // 오늘이 결제일이어도 이미 처리(기록·건너뜀)했으면 다음 달
    @Test
    void nextDueSkipsHandledToday() {
        assertEquals(due(2026, 9, "2026-09-26"), RecurringSchedule.nextDue(regular(26, "NONE"), TODAY, Set.of(), p -> false));
        assertEquals(due(2026, 10, "2026-10-26"),
                RecurringSchedule.nextDue(regular(26, "NONE"), TODAY, Set.of(), p -> p.equals(YearMonth.of(2026, 9))));
    }

    // 휴일 보정 반영: 2026-11-01(일) PREV_BIZ → 10/30
    @Test
    void nextDueAppliesAdjust() {
        assertEquals(due(2026, 11, "2026-10-30"),
                RecurringSchedule.nextDue(regular(1, "PREV_BIZ"), LocalDate.of(2026, 10, 2), Set.of(), p -> false));
    }

    // 할부는 기간 안에서만. 첫 결제월이 멀면 그달, 마지막 회차가 지났으면 없음
    @Test
    void nextDueRespectsInstallmentRange() {
        Map<String, Object> later = ParamUtil.map("dayOfMonth", 1, "adjust", "NONE", "amount", 109_140L,
                "installmentTotal", 327_420L, "installmentMonths", 3, "installmentStart", "2027-03");
        assertEquals(due(2027, 3, "2027-03-01"), RecurringSchedule.nextDue(later, TODAY, Set.of(), p -> false));
        Map<String, Object> done = ParamUtil.map("dayOfMonth", 1, "adjust", "NONE", "amount", 109_140L,
                "installmentTotal", 327_420L, "installmentMonths", 3, "installmentStart", "2026-07");
        assertNull(RecurringSchedule.nextDue(done, TODAY, Set.of(), p -> false));
    }

    // 할부 일정(첫 결제월·개월 수)이 바뀌거나 일반 ↔ 할부로 바뀌면 건너뛴다. 총액만 바꾸면 그대로
    @Test
    void installmentScheduleChangeSkips() {
        assertFalse(RecurringSchedule.shouldSkipPassed(installment("2026-10", 3), installment("2026-10", 3)));
        assertTrue(RecurringSchedule.shouldSkipPassed(installment("2026-10", 3), installment("2026-11", 3)));
        assertTrue(RecurringSchedule.shouldSkipPassed(installment("2026-10", 3), installment("2026-10", 6)));
        assertTrue(RecurringSchedule.shouldSkipPassed(saved(1, "NEXT_BIZ", true), installment("2026-10", 3)));
    }

    // 오늘이 결제일이면 이번 달 포함, 지난달 분도 대상
    @Test
    void duesUpToIncludesToday() {
        assertEquals(List.of(due(2026, 8, "2026-08-26"), due(2026, 9, "2026-09-26")),
                RecurringSchedule.duesUpTo(26, "NONE", LocalDate.parse("2026-09-26"), NONE));
    }

    // 결제일이 아직 안 온 이번 달은 제외
    @Test
    void duesUpToExcludesFuture() {
        assertEquals(List.of(due(2026, 8, "2026-08-27")),
                RecurringSchedule.duesUpTo(27, "NONE", LocalDate.parse("2026-09-26"), NONE));
    }

    // 건너뛸 대상은 오늘보다 앞선 결제일만(오늘 결제일은 바로 기록하므로 제외)
    @Test
    void duesBeforeExcludesToday() {
        assertEquals(List.of(due(2026, 8, "2026-08-26")),
                RecurringSchedule.duesBefore(26, "NONE", LocalDate.parse("2026-09-26"), NONE));
        assertEquals(List.of(due(2026, 8, "2026-08-05"), due(2026, 9, "2026-09-05")),
                RecurringSchedule.duesBefore(5, "NONE", LocalDate.parse("2026-09-26"), NONE));
    }

    // 11/1(일) PREV_BIZ → 10/30(금): 다음 달분이 이번 달로 당겨지면 그날 기록 대상이어야 한다
    @Test
    void prevBusinessDayFromNextMonthIsDueToday() {
        assertEquals(List.of(due(2026, 9, "2026-09-01"), due(2026, 10, "2026-10-01"), due(2026, 11, "2026-10-30")),
                RecurringSchedule.duesUpTo(1, "PREV_BIZ", LocalDate.parse("2026-10-30"), NONE));
    }

    // 10/31 에 새로 만든 항목: 이미 지난 11월분(10/30)도 건너뛸 대상
    @Test
    void prevBusinessDayFromNextMonthIsSkippableAfterwards() {
        assertEquals(List.of(due(2026, 9, "2026-09-01"), due(2026, 10, "2026-10-01"), due(2026, 11, "2026-10-30")),
                RecurringSchedule.duesBefore(1, "PREV_BIZ", LocalDate.parse("2026-10-31"), NONE));
    }

    // 8/31(월) → 그대로, 9/30(수). NEXT_BIZ 로 다음 달로 넘어가도 "몇 월분"은 원래 달
    @Test
    void nextBusinessDayKeepsOriginalMonth() {
        // 2026-10-31(토) NEXT_BIZ → 11/2. 11/1 기준으로는 10월분이 아직 오지 않음
        assertEquals(List.of(), RecurringSchedule.duesUpTo(31, "NEXT_BIZ", LocalDate.parse("2026-11-01"), NONE));
        assertEquals(List.of(due(2026, 10, "2026-11-02")),
                RecurringSchedule.duesUpTo(31, "NEXT_BIZ", LocalDate.parse("2026-11-02"), NONE));
    }

    // 할부 고정 항목 값
    private static Map<String, Object> installmentItem(int day, String adjust, int months, String start) {
        return ParamUtil.map("dayOfMonth", day, "adjust", adjust, "amount", 109_140L,
                "installmentTotal", 327_420L, "installmentMonths", months, "installmentStart", start);
    }

    // 할부 3회(10·11·12월분): 마지막 결제일(12/1) 전에는 계속, 당일부터 끝
    @Test
    void installmentFinishesOnLastDueDate() {
        Map<String, Object> item = installmentItem(1, "NEXT_BIZ", 3, "2026-10");
        assertFalse(RecurringSchedule.isFinished(item, LocalDate.parse("2026-11-30"), NONE));
        assertTrue(RecurringSchedule.isFinished(item, LocalDate.parse("2026-12-01"), NONE));
    }

    // 마지막 결제일이 휴일로 밀리면(2027-08-01 일 → 8/2) 밀린 날까지 기다린다
    @Test
    void installmentFinishWaitsForAdjustedDate() {
        Map<String, Object> item = installmentItem(1, "NEXT_BIZ", 2, "2027-07");
        assertFalse(RecurringSchedule.isFinished(item, LocalDate.parse("2027-08-01"), NONE));
        assertTrue(RecurringSchedule.isFinished(item, LocalDate.parse("2027-08-02"), NONE));
    }

    // 일반 고정 항목은 끝나지 않는다
    @Test
    void regularItemNeverFinishes() {
        assertFalse(RecurringSchedule.isFinished(regular(1, "NONE"), LocalDate.parse("2030-01-01"), NONE));
    }
}
