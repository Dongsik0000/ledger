package ledger.cycle;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpeningBalanceTest {

    private static final LocalDate D = LocalDate.of(2026, 9, 23);

    // 시작 잔액이 없으면 지금처럼 모든 거래로 잔액을 센다
    @Test
    void noneCountsAllEntries() {
        OpeningBalance none = OpeningBalance.of(new HashMap<>());
        assertNull(none.countFrom());
        assertEquals(12_000, none.balanceAt(12_000));
        assertEquals(LocalDate.of(2026, 9, 1), none.periodFrom(LocalDate.of(2026, 9, 1)));
        assertTrue(none.known(LocalDate.of(2000, 1, 1)));
    }

    // 기준일 이후 거래만 더한다(기준일 이전 거래는 시작 잔액에 이미 들어 있다)
    @Test
    void balanceStartsFromOpening() {
        OpeningBalance o = new OpeningBalance(632_619, D);
        assertEquals(D, o.countFrom());
        assertEquals(632_619, o.balanceAt(0));
        assertEquals(632_619 - 30_000, o.balanceAt(-30_000));
    }

    // 주기가 기준일보다 먼저 시작하면 기간 합계는 기준일부터
    @Test
    void periodStartsAtOpeningWhenInside() {
        OpeningBalance o = new OpeningBalance(632_619, D);
        assertEquals(D, o.periodFrom(LocalDate.of(2026, 9, 1)));
        assertEquals(D, o.periodFrom(D));
        assertEquals(LocalDate.of(2026, 10, 23), o.periodFrom(LocalDate.of(2026, 10, 23)));
    }

    // 기준일 전날까지의 잔액은 알 수 없다
    @Test
    void unknownBeforeOpening() {
        OpeningBalance o = new OpeningBalance(632_619, D);
        assertFalse(o.known(D.minusDays(1)));
        assertTrue(o.known(D));
    }

    // DB 조회 결과(java.sql.Date)·문자열 모두 읽는다
    @Test
    void ofReadsSetting() {
        Map<String, Object> setting = new HashMap<>();
        setting.put("openingBalance", 632_619L);
        setting.put("openingDate", java.sql.Date.valueOf(D));
        assertEquals(new OpeningBalance(632_619, D), OpeningBalance.of(setting));
        setting.put("openingDate", "2026-09-23");
        assertEquals(new OpeningBalance(632_619, D), OpeningBalance.of(setting));
    }
}
