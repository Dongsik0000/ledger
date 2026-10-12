package ledger.market;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

// 달러 금액의 원화 평가 테스트
class MarketValueTest {
    // 달러 구독료를 원 단위로 반올림
    @Test
    void roundsUsdSubscriptionToNearestWon() {
        assertEquals(29_001, MarketValue.krw(new BigDecimal("20.00"), new BigDecimal("1450.045")));
        assertEquals(1, MarketValue.krw(new BigDecimal("0.01"), new BigDecimal("100.0")));
    }

    // 소수 주가 × 수량 평가
    @Test
    void valuesSharesWithFractionalDollarPrice() {
        BigDecimal shares = new BigDecimal("2");
        BigDecimal price = new BigDecimal("250.25");
        assertEquals(725_725, MarketValue.krw(shares.multiply(price), new BigDecimal("1450")));
    }

    // 0 금액·0 환율은 거부, 범위를 넘으면 ArithmeticException
    @Test
    void rejectsMissingOrInvalidMarketValues() {
        assertThrows(IllegalArgumentException.class, () -> MarketValue.krw(BigDecimal.ZERO, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> MarketValue.krw(BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(ArithmeticException.class, () -> MarketValue.krw(new BigDecimal("999999999999999999"), new BigDecimal("1000")));
    }
}
