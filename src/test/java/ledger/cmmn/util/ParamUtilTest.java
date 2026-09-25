package ledger.cmmn.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParamUtilTest {

    @Test
    void strTrims() {
        assertEquals("abc", ParamUtil.str(Map.<String, Object>of("k", "  abc "), "k"));
    }

    @Test
    void rawKeepsSpaces() {
        assertEquals(" pw ", ParamUtil.raw(Map.<String, Object>of("k", " pw "), "k"));
    }

    @Test
    void missingKeyIsEmpty() {
        assertEquals("", ParamUtil.str(Map.of(), "k"));
        assertEquals("", ParamUtil.raw(Map.of(), "k"));
    }

    @Test
    void nullValueIsEmpty() {
        Map<String, Object> m = new HashMap<>();
        m.put("k", null);
        assertEquals("", ParamUtil.str(m, "k"));
        assertEquals("", ParamUtil.raw(m, "k"));
    }

    @Test
    void nullMapIsEmpty() {
        assertEquals("", ParamUtil.str(null, "k"));
    }

    @Test
    void numberBecomesString() {
        assertEquals("12", ParamUtil.str(Map.<String, Object>of("k", 12), "k"));
    }

    @Test
    void lngParsesDigitsAndCommas() {
        assertEquals(1930000L, ParamUtil.lng(Map.<String, Object>of("k", "1,930,000"), "k"));
        assertEquals(12L, ParamUtil.lng(Map.<String, Object>of("k", 12), "k"));
        assertEquals(-3L, ParamUtil.lng(Map.<String, Object>of("k", "-3"), "k"));
    }

    @Test
    void lngRejectsInvalid() {
        assertNull(ParamUtil.lng(Map.<String, Object>of("k", "12.5"), "k"));
        assertNull(ParamUtil.lng(Map.<String, Object>of("k", "abc"), "k"));
        assertNull(ParamUtil.lng(Map.<String, Object>of("k", ""), "k"));
        assertNull(ParamUtil.lng(Map.<String, Object>of("k", "9".repeat(19)), "k"));
    }

    @Test
    void integerRejectsOutOfRange() {
        assertEquals(31, ParamUtil.integer(Map.<String, Object>of("k", "31"), "k"));
        assertNull(ParamUtil.integer(Map.<String, Object>of("k", "3000000000"), "k"));
    }

    @Test
    void dateParsesIsoOnly() {
        assertEquals(LocalDate.of(2026, 2, 28), ParamUtil.date(Map.<String, Object>of("k", "2026-02-28"), "k"));
        assertNull(ParamUtil.date(Map.<String, Object>of("k", "2026-02-30"), "k"));
        assertNull(ParamUtil.date(Map.<String, Object>of("k", "2026/02/28"), "k"));
        assertNull(ParamUtil.date(Map.<String, Object>of("k", "+12026-02-28"), "k"));
    }

    @Test
    void monthParses() {
        assertEquals(YearMonth.of(2026, 10), ParamUtil.month(Map.<String, Object>of("k", "2026-10"), "k"));
        assertNull(ParamUtil.month(Map.<String, Object>of("k", "2026-13"), "k"));
        assertNull(ParamUtil.month(Map.<String, Object>of("k", "202610"), "k"));
    }

    @Test
    void boolParsesBooleanAndString() {
        assertEquals(Boolean.TRUE, ParamUtil.bool(Map.<String, Object>of("k", true), "k"));
        assertEquals(Boolean.FALSE, ParamUtil.bool(Map.<String, Object>of("k", "false"), "k"));
        assertNull(ParamUtil.bool(Map.<String, Object>of("k", "yes"), "k"));
    }

    @Test
    void mapAllowsNullValues() {
        Map<String, Object> m = ParamUtil.map("a", 1, "b", null);
        assertEquals(1, m.get("a"));
        assertTrue(m.containsKey("b"));
        assertNull(m.get("b"));
    }

    @Test
    void hasChecksTrimmedValue() {
        assertTrue(ParamUtil.has(Map.<String, Object>of("k", " a "), "k"));
        assertFalse(ParamUtil.has(Map.<String, Object>of("k", "  "), "k"));
        assertFalse(ParamUtil.has(Map.of(), "k"));
    }
}
