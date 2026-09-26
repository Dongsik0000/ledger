package ledger.recurring;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InstallmentTest {

    private static Map<String, Object> item(long amount, Long total, Integer months, String start) {
        Map<String, Object> item = new HashMap<>();
        item.put("amount", amount);
        item.put("installmentTotal", total);
        item.put("installmentMonths", months);
        item.put("installmentStart", start);
        return item;
    }

    // 나누어 떨어지면 모든 회차가 같다
    @Test
    void amountOfEvenSplit() {
        assertEquals(109_140, Installment.amountOf(327_420, 3, 1));
        assertEquals(109_140, Installment.amountOf(327_420, 3, 2));
        assertEquals(109_140, Installment.amountOf(327_420, 3, 3));
    }

    // 나머지(원 단위)는 1회차에 붙는다
    @Test
    void amountOfRemainderGoesToFirstRound() {
        assertEquals(33_334, Installment.amountOf(100_000, 3, 1));
        assertEquals(33_333, Installment.amountOf(100_000, 3, 2));
        assertEquals(33_333, Installment.amountOf(100_000, 3, 3));
        assertEquals(33_333, Installment.baseAmount(100_000, 3));
    }

    // 첫 결제월이 1회차, 해를 넘겨도 이어진다. 기간 밖은 0
    @Test
    void roundOfAcrossYear() {
        YearMonth start = YearMonth.of(2026, 11);
        assertEquals(0, Installment.roundOf(start, 3, YearMonth.of(2026, 10)));
        assertEquals(1, Installment.roundOf(start, 3, YearMonth.of(2026, 11)));
        assertEquals(2, Installment.roundOf(start, 3, YearMonth.of(2026, 12)));
        assertEquals(3, Installment.roundOf(start, 3, YearMonth.of(2027, 1)));
        assertEquals(0, Installment.roundOf(start, 3, YearMonth.of(2027, 2)));
    }

    @Test
    void lastMonth() {
        assertEquals(YearMonth.of(2027, 1), Installment.lastMonth(YearMonth.of(2026, 11), 3));
        assertEquals(YearMonth.of(2026, 11), Installment.lastMonth(YearMonth.of(2026, 10), 2));
    }

    // 일반 고정 항목은 매달 같은 금액
    @Test
    void amountForRegularItem() {
        Map<String, Object> item = item(20_000, null, null, null);
        assertFalse(Installment.isInstallment(item));
        assertEquals(20_000L, Installment.amountFor(item, YearMonth.of(2026, 10)));
    }

    // 할부는 회차 금액, 기간 밖이면 null(기록·예정 대상 아님)
    @Test
    void amountForInstallmentItem() {
        Map<String, Object> item = item(33_333, 100_000L, 3, "2026-10");
        assertTrue(Installment.isInstallment(item));
        assertNull(Installment.amountFor(item, YearMonth.of(2026, 9)));
        assertEquals(33_334L, Installment.amountFor(item, YearMonth.of(2026, 10)));
        assertEquals(33_333L, Installment.amountFor(item, YearMonth.of(2026, 12)));
        assertNull(Installment.amountFor(item, YearMonth.of(2027, 1)));
    }
}
