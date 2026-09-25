package ledger.cmmn.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilTest {

    @Test
    void plainTextUnchanged() {
        assertEquals("점심", CsvUtil.cell("점심"));
        assertEquals("12500", CsvUtil.cell(12500L));
        assertEquals("", CsvUtil.cell(null));
    }

    // 스프레드시트가 수식으로 실행하지 않도록 앞에 '
    @Test
    void formulaPrefixesAreNeutralized() {
        assertEquals("'=HYPERLINK(\"\"http://x\"\")", CsvUtil.cell("=HYPERLINK(\"http://x\")").replaceAll("^\"|\"$", ""));
        assertEquals("'+1", CsvUtil.cell("+1"));
        assertEquals("'-1", CsvUtil.cell("-1"));
        assertEquals("'@SUM(A1)", CsvUtil.cell("@SUM(A1)"));
        assertEquals("'\tx", CsvUtil.cell("\tx"));
    }

    @Test
    void quotesWhenNeeded() {
        assertEquals("\"a,b\"", CsvUtil.cell("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", CsvUtil.cell("say \"hi\""));
        assertEquals("\"line1\nline2\"", CsvUtil.cell("line1\nline2"));
    }

    @Test
    void lineJoinsCellsWithCrLf() {
        assertEquals("a,\"b,c\",'=1\r\n", CsvUtil.line(Arrays.asList("a", "b,c", "=1")));
    }
}
