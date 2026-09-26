package ledger.recurring.controller;

import ledger.cmmn.util.ParamUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

    // 할부 일정(첫 결제월·개월 수)이 바뀌거나 일반 ↔ 할부로 바뀌면 건너뛴다. 총액만 바꾸면 그대로
    @Test
    void installmentScheduleChangeSkips() {
        assertFalse(RecurringApiController.shouldSkipPassed(installment("2026-10", 3), installment("2026-10", 3)));
        assertTrue(RecurringApiController.shouldSkipPassed(installment("2026-10", 3), installment("2026-11", 3)));
        assertTrue(RecurringApiController.shouldSkipPassed(installment("2026-10", 3), installment("2026-10", 6)));
        assertTrue(RecurringApiController.shouldSkipPassed(saved(1, "NEXT_BIZ", true), installment("2026-10", 3)));
    }
}
