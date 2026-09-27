package ledger.market;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MarketValueTest {
    @Test
    void roundsUsdSubscriptionToNearestWon() {
        assertEquals(29_001, MarketValue.krw(new BigDecimal("20.00"), new BigDecimal("1450.045")));
        assertEquals(1, MarketValue.krw(new BigDecimal("0.01"), new BigDecimal("100.0")));
    }

    @Test
    void valuesSharesWithFractionalDollarPrice() {
        BigDecimal shares = new BigDecimal("2");
        BigDecimal price = new BigDecimal("250.25");
        assertEquals(725_725, MarketValue.krw(shares.multiply(price), new BigDecimal("1450")));
    }

    @Test
    void rejectsMissingOrInvalidMarketValues() {
        assertThrows(IllegalArgumentException.class, () -> MarketValue.krw(BigDecimal.ZERO, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> MarketValue.krw(BigDecimal.ONE, BigDecimal.ZERO));
        assertThrows(ArithmeticException.class, () -> MarketValue.krw(new BigDecimal("999999999999999999"), new BigDecimal("1000")));
    }
}
