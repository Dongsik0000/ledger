package ledger.recurring;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Map;

// 할부 고정 항목 계산. 총액을 개월 수로 나눠 첫 결제월부터 매달 한 회차씩(무이자). 나머지(원 단위)는 1회차에.
// 고정 항목 map 키: amount, installmentTotal, installmentMonths, installmentStart("YYYY-MM"). 일반 항목은 할부 키가 null
public final class Installment {

    public static final int MONTHS_MIN = 2;
    public static final int MONTHS_MAX = 60;

    private Installment() {
    }

    public static boolean isInstallment(Map<String, Object> item) {
        return item.get("installmentMonths") != null;
    }

    // 2~n회차 금액(목록·월 고정지출 표시용으로 recurring_item.amount 에 저장)
    public static long baseAmount(long total, int months) {
        return total / months;
    }

    public static long amountOf(long total, int months, int round) {
        long base = baseAmount(total, months);
        return round == 1 ? total - base * (months - 1) : base;
    }

    // period 가 몇 회차인지(첫 결제월 = 1). 기간 밖이면 0
    public static int roundOf(YearMonth start, int months, YearMonth period) {
        long round = start.until(period, ChronoUnit.MONTHS) + 1;
        return round >= 1 && round <= months ? (int) round : 0;
    }

    public static YearMonth lastMonth(YearMonth start, int months) {
        return start.plusMonths(months - 1L);
    }

    // 그 달 결제 금액. 일반 항목은 amount, 할부는 회차 금액, 할부 기간 밖이면 null
    public static Long amountFor(Map<String, Object> item, YearMonth period) {
        if (!isInstallment(item)) {
            return ((Number) item.get("amount")).longValue();
        }
        int months = months(item);
        int round = roundOf(start(item), months, period);
        return round == 0 ? null : amountOf(total(item), months, round);
    }

    public static YearMonth start(Map<String, Object> item) {
        return YearMonth.parse(item.get("installmentStart").toString().trim());
    }

    public static int months(Map<String, Object> item) {
        return ((Number) item.get("installmentMonths")).intValue();
    }

    private static long total(Map<String, Object> item) {
        return ((Number) item.get("installmentTotal")).longValue();
    }
}
