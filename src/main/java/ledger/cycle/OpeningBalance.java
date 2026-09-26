package ledger.cycle;

import java.time.LocalDate;
import java.util.Map;

// 시작 잔액: 기준일(date)이 시작될 때의 잔액(amount). 기준일 이전 거래는 이미 그 안에 반영된 것으로 보고
// 잔액(이월·주기 잔액·누적)을 셀 때 빼며, 수입·지출 통계에는 그대로 남는다. date 가 없으면 모든 거래로 잔액을 센다.
public record OpeningBalance(long amount, LocalDate date) {

    // settingsService.selectUserSetting 결과: openingBalance, openingDate(java.sql.Date·LocalDate·"yyyy-MM-dd"·null)
    public static OpeningBalance of(Map<String, Object> setting) {
        Object d = setting.get("openingDate");
        LocalDate date = d == null ? null
                : d instanceof java.sql.Date sql ? sql.toLocalDate()
                : d instanceof LocalDate local ? local
                : LocalDate.parse(d.toString());
        Object a = setting.get("openingBalance");
        return new OpeningBalance(date == null || a == null ? 0 : ((Number) a).longValue(), date);
    }

    // 잔액에 넣는 거래의 시작일(이날 포함). null 이면 처음부터
    public LocalDate countFrom() {
        return date;
    }

    // x 가 시작될 때의 잔액. entrySum = [countFrom, x) 거래의 수입 − 지출
    public long balanceAt(long entrySum) {
        return date == null ? entrySum : amount + entrySum;
    }

    // 기간(start~) 수입·지출을 잔액에 더할 때의 시작일: 기준일이 기간 안이면 기준일부터
    public LocalDate periodFrom(LocalDate start) {
        return date != null && date.isAfter(start) ? date : start;
    }

    // x 가 시작될 때의 잔액을 알 수 있는지(기준일 이전은 모른다)
    public boolean known(LocalDate x) {
        return date == null || !x.isBefore(date);
    }
}
