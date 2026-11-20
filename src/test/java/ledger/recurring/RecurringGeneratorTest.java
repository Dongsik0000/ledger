package ledger.recurring;

import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 고정 항목 거래 내용(할부 회차 표시) 테스트
class RecurringGeneratorTest {

    // 할부 거래 내용에는 회차를 붙이고, 100자를 넘지 않게 이름을 줄인다
    @Test
    void installmentTitleHasRound() {
        Map<String, Object> item = new HashMap<>();
        item.put("installmentMonths", 3);
        item.put("installmentStart", "2026-10");
        item.put("name", "노트북");
        assertEquals("노트북 (2/3)", RecurringGenerator.title(item, YearMonth.of(2026, 11)));

        item.put("name", "가".repeat(100));
        String title = RecurringGenerator.title(item, YearMonth.of(2026, 12));
        assertEquals(100, title.length());
        assertTrue(title.endsWith(" (3/3)"));

        Map<String, Object> regular = new HashMap<>();
        regular.put("name", "넷플릭스");
        assertEquals("넷플릭스", RecurringGenerator.title(regular, YearMonth.of(2026, 11)));
    }
}
