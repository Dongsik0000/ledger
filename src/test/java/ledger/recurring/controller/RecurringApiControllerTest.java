package ledger.recurring.controller;

import ledger.cmmn.util.ParamUtil;
import ledger.cycle.PayCycle;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecurringApiControllerTest {

    private static Map<String, Object> saved(int day, String adjust, boolean active) {
        return ParamUtil.map("dayOfMonth", day, "adjust", adjust, "active", active);
    }

    private static Map<String, Object> item(int day, String adjust) {
        return ParamUtil.map("dayOfMonth", day, "adjust", adjust);
    }

    private static Map<String, Object> installment(String start, int months) {
        return ParamUtil.map("dayOfMonth", 1, "adjust", "NEXT_BIZ", "active", true,
                "installmentStart", start, "installmentMonths", months);
    }

    // 금액·이름만 바꾼 수정은 지난 결제일을 건너뛰지 않는다(스케줄러 누락이 조용히 "건너뜀"이 되지 않게)
    @Test
    void amountOnlyChangeDoesNotSkip() {
        assertFalse(RecurringApiController.shouldSkipPassed(saved(5, "NONE", true), item(5, "NONE")));
    }

    @Test
    void dayOrAdjustChangeSkips() {
        assertTrue(RecurringApiController.shouldSkipPassed(saved(5, "NONE", true), item(6, "NONE")));
        assertTrue(RecurringApiController.shouldSkipPassed(saved(5, "NONE", true), item(5, "PREV_BIZ")));
    }

    @Test
    void reactivationSkips() {
        assertTrue(RecurringApiController.shouldSkipPassed(saved(5, "NONE", false), item(5, "NONE")));
    }

    // 신규 저장(saved 없음)은 건너뛴다
    @Test
    void newItemSkips() {
        assertTrue(RecurringApiController.shouldSkipPassed(null, item(5, "NONE")));
    }

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 26);

    private static Map<String, Object> regular(int day, String adjust) {
        return ParamUtil.map("dayOfMonth", day, "adjust", adjust, "amount", 20_000L);
    }

    private static PayCycle.Due due(int y, int m, String date) {
        return new PayCycle.Due(YearMonth.of(y, m), LocalDate.parse(date));
    }

    // 다음 결제일: 오늘 이후 첫 결제일
    @Test
    void nextDueIsFirstUpcoming() {
        assertEquals(due(2026, 10, "2026-10-01"), RecurringApiController.nextDue(regular(1, "NONE"), TODAY, Set.of(), p -> false));
        assertEquals(due(2026, 9, "2026-09-28"), RecurringApiController.nextDue(regular(28, "NONE"), TODAY, Set.of(), p -> false));
    }

    // 오늘이 결제일이어도 이미 처리(기록·건너뜀)했으면 다음 달
    @Test
    void nextDueSkipsHandledToday() {
        assertEquals(due(2026, 9, "2026-09-26"), RecurringApiController.nextDue(regular(26, "NONE"), TODAY, Set.of(), p -> false));
        assertEquals(due(2026, 10, "2026-10-26"),
                RecurringApiController.nextDue(regular(26, "NONE"), TODAY, Set.of(), p -> p.equals(YearMonth.of(2026, 9))));
    }

    // 휴일 보정 반영: 2026-11-01(일) PREV_BIZ → 10/30
    @Test
    void nextDueAppliesAdjust() {
        assertEquals(due(2026, 11, "2026-10-30"),
                RecurringApiController.nextDue(regular(1, "PREV_BIZ"), LocalDate.of(2026, 10, 2), Set.of(), p -> false));
    }

    // 할부는 기간 안에서만. 첫 결제월이 멀면 그달, 마지막 회차가 지났으면 없음
    @Test
    void nextDueRespectsInstallmentRange() {
        Map<String, Object> later = ParamUtil.map("dayOfMonth", 1, "adjust", "NONE", "amount", 109_140L,
                "installmentTotal", 327_420L, "installmentMonths", 3, "installmentStart", "2027-03");
        assertEquals(due(2027, 3, "2027-03-01"), RecurringApiController.nextDue(later, TODAY, Set.of(), p -> false));
        Map<String, Object> done = ParamUtil.map("dayOfMonth", 1, "adjust", "NONE", "amount", 109_140L,
                "installmentTotal", 327_420L, "installmentMonths", 3, "installmentStart", "2026-07");
        assertNull(RecurringApiController.nextDue(done, TODAY, Set.of(), p -> false));
    }

    // 할부 일정(첫 결제월·개월 수)이 바뀌거나 일반 ↔ 할부로 바뀌면 건너뛴다. 총액만 바꾸면 그대로
    @Test
    void installmentScheduleChangeSkips() {
        assertFalse(RecurringApiController.shouldSkipPassed(installment("2026-10", 3), installment("2026-10", 3)));
        assertTrue(RecurringApiController.shouldSkipPassed(installment("2026-10", 3), installment("2026-11", 3)));
        assertTrue(RecurringApiController.shouldSkipPassed(installment("2026-10", 3), installment("2026-10", 6)));
        assertTrue(RecurringApiController.shouldSkipPassed(saved(1, "NEXT_BIZ", true), installment("2026-10", 3)));
    }
}
