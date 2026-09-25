package ledger.cmmn.util;

import java.util.List;
import java.util.stream.Collectors;

// CSV 셀·줄 만들기(RFC 4180 + 스프레드시트 수식 주입 방지)
public class CsvUtil {

    private static final String FORMULA_START = "=+-@\t\r";

    private CsvUtil() {
    }

    // = + - @ 탭·CR 로 시작하면 앞에 ' 를 붙여 엑셀 등이 수식으로 실행하지 않게 하고,
    // 쉼표·따옴표·줄바꿈이 있으면 따옴표로 감싼다(안의 " 는 "")
    public static String cell(Object value) {
        String s = value == null ? "" : String.valueOf(value);
        if (!s.isEmpty() && FORMULA_START.indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            s = "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    public static String line(List<?> values) {
        return values.stream().map(CsvUtil::cell).collect(Collectors.joining(",")) + "\r\n";
    }
}
