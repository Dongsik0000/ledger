package ledger.cmmn.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

// CSV 칸·줄 만들기와 수식 주입 방지 테스트
class CsvUtilTest {

    // 평범한 글자는 그대로
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

    // 쉼표·따옴표·줄바꿈이 있으면 따옴표로 감싼다
    @Test
    void quotesWhenNeeded() {
        assertEquals("\"a,b\"", CsvUtil.cell("a,b"));
        assertEquals("\"say \"\"hi\"\"\"", CsvUtil.cell("say \"hi\""));
        assertEquals("\"line1\nline2\"", CsvUtil.cell("line1\nline2"));
    }

    // 칸은 쉼표로 잇고 줄 끝은 CRLF
    @Test
    void lineJoinsCellsWithCrLf() {
        assertEquals("a,\"b,c\",'=1\r\n", CsvUtil.line(Arrays.asList("a", "b,c", "=1")));
    }
}
