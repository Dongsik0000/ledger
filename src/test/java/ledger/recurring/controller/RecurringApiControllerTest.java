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

    // 금액·이름만 바꾼 수정은 지난 결제일을 건너뛰지 않는다(스케줄러 누락이 조용히 "건너뜀"이 되지 않게)
    @Test
    void amountOnlyChangeDoesNotSkip() {
        assertFalse(RecurringApiController.shouldSkipPassed(saved(5, "NONE", true), 5, "NONE"));
    }

    @Test
    void dayOrAdjustChangeSkips() {
        assertTrue(RecurringApiController.shouldSkipPassed(saved(5, "NONE", true), 6, "NONE"));
        assertTrue(RecurringApiController.shouldSkipPassed(saved(5, "NONE", true), 5, "PREV_BIZ"));
    }

    @Test
    void reactivationSkips() {
        assertTrue(RecurringApiController.shouldSkipPassed(saved(5, "NONE", false), 5, "NONE"));
    }

    // 신규 저장(saved 없음)은 건너뛴다
    @Test
    void newItemSkips() {
        assertTrue(RecurringApiController.shouldSkipPassed(null, 5, "NONE"));
    }
}
