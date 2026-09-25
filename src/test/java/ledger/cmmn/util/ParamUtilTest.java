package ledger.cmmn.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
