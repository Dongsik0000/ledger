package ledger.summary;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SummaryTablesTest {

    private static Map<String, Object> category(long id, String name, String group, boolean active) {
        return ledger.cmmn.util.ParamUtil.map("id", id, "type", "EXPENSE", "name", name, "groupName", group, "active", active);
    }

    private static Map<String, Object> amount(long id, String month, long amount) {
        return ledger.cmmn.util.ParamUtil.map("id", id, "month", month, "amount", amount);
    }

    @Test
    void monthsBetweenInclusive() {
        assertEquals(List.of("2026-11", "2026-12", "2027-01"),
                SummaryTables.months(YearMonth.of(2026, 11), YearMonth.of(2027, 1)));
    }

    // 식비 그룹(식비·카페)이 순서상 떨어져 있어도 모이고, 소계는 합계에 중복되지 않는다
    @Test
    void categoryRowsGroupMembersAndSubtotal() {
        List<Map<String, Object>> categories = List.of(
                category(1, "식비", "식비", true),
                category(2, "교통", null, true),
                category(3, "카페", "식비", true),
                category(4, "옛날", null, false),
                category(5, "숨김무기록", null, false));
        List<Map<String, Object>> amounts = List.of(
                amount(1, "2026-09", 100), amount(3, "2026-09", 50), amount(2, "2026-10", 30), amount(4, "2026-10", 7));

        List<SummaryTables.Row> rows = SummaryTables.categoryRows(categories, amounts, List.of("2026-09", "2026-10"));

        assertEquals(List.of("식비", "카페", "식비 그룹 소계", "교통", "옛날", "합계"),
                rows.stream().map(SummaryTables.Row::label).toList());
        assertEquals(List.of(150L, 0L), rows.get(2).values());
        assertEquals(150L, rows.get(2).total());
        assertEquals(List.of(150L, 37L), rows.get(5).values());
        assertEquals(187L, rows.get(5).total());
        assertEquals("subtotal", rows.get(2).kind());
        assertEquals("total", rows.get(5).kind());
    }

    // 멤버가 하나뿐인 그룹은 소계 행을 만들지 않는다
    @Test
    void singleMemberGroupHasNoSubtotal() {
        List<SummaryTables.Row> rows = SummaryTables.categoryRows(
                List.of(category(1, "저축", "저축", true)), List.of(amount(1, "2026-09", 10)), List.of("2026-09"));
        assertEquals(List.of("저축", "합계"), rows.stream().map(SummaryTables.Row::label).toList());
    }

    // 결제수단 없는 지출은 "없음" 행, 기록 없는 결제수단도 표시
    @Test
    void paymentRowsIncludeNone() {
        List<Map<String, Object>> payments = List.of(
                ledger.cmmn.util.ParamUtil.map("id", 10L, "name", "카드", "active", true),
                ledger.cmmn.util.ParamUtil.map("id", 11L, "name", "현금", "active", true));
        List<Map<String, Object>> amounts = List.of(amount(10, "2026-09", 40), amountNull("2026-09", 5));

        List<SummaryTables.Row> rows = SummaryTables.paymentRows(payments, amounts, List.of("2026-09"));

        assertEquals(List.of("카드", "현금", "없음", "합계"), rows.stream().map(SummaryTables.Row::label).toList());
        assertEquals(45L, rows.get(3).total());
    }

    private static Map<String, Object> amountNull(String month, long amount) {
        return ledger.cmmn.util.ParamUtil.map("id", null, "month", month, "amount", amount);
    }
}
