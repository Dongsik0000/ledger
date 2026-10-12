package ledger.market;

import java.math.BigDecimal;
import java.math.RoundingMode;

// 주식 평가액 계산
public final class MarketValue {
    private MarketValue() {}

    // 달러 금액 × 환율을 원 단위로 반올림
    public static long krw(BigDecimal usd, BigDecimal rate) {
        if (usd == null || rate == null || usd.signum() <= 0 || rate.signum() <= 0) {
            throw new IllegalArgumentException("Positive USD amount and rate required");
        }
        return usd.multiply(rate).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
