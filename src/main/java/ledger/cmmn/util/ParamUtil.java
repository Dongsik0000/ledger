package ledger.cmmn.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

// @RequestBody HashMap 에서 값 꺼내기. 키가 없거나 값이 null 이면 "".
// 숫자·날짜 파서는 형식이 틀리면 null 을 돌려주고, Controller 가 INVALID(90)로 응답한다.
public class ParamUtil {

    private static final Pattern INTEGER = Pattern.compile("-?\\d{1,18}");
    private static final Pattern ISO_DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final Pattern ISO_MONTH = Pattern.compile("\\d{4}-\\d{2}");

    private ParamUtil() {
    }

    // 앞뒤 공백 제거 (아이디, 코드, 이름 등)
    public static String str(Map<String, Object> param, String key) {
        return raw(param, key).trim();
    }

    // 공백 유지 (비밀번호)
    public static String raw(Map<String, Object> param, String key) {
        Object value = param == null ? null : param.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    // DAO 에 넘길 파라미터 맵. Map.of 와 달리 null 값을 허용한다(선택 칸).
    // 요청 본문을 그대로 넘기지 않고 이것으로 필요한 값만 담는다(클라이언트가 userId 등을 끼워 넣지 못하게).
    public static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put((String) keyValues[i], keyValues[i + 1]);
        }
        return m;
    }

    public static boolean has(Map<String, Object> param, String key) {
        return !str(param, key).isEmpty();
    }

    // 정수. 쉼표 허용(금액 입력). 형식 오류·범위 초과는 null
    public static Long lng(Map<String, Object> param, String key) {
        String s = str(param, key).replace(",", "");
        return INTEGER.matcher(s).matches() ? Long.valueOf(s) : null;
    }

    public static Integer integer(Map<String, Object> param, String key) {
        Long v = lng(param, key);
        return v == null || v < Integer.MIN_VALUE || v > Integer.MAX_VALUE ? null : v.intValue();
    }

    // yyyy-MM-dd. 없는 날짜(2월 30일 등)는 null
    public static LocalDate date(Map<String, Object> param, String key) {
        String s = str(param, key);
        if (!ISO_DATE.matcher(s).matches()) {
            return null;
        }
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // yyyy-MM
    public static YearMonth month(Map<String, Object> param, String key) {
        String s = str(param, key);
        if (!ISO_MONTH.matcher(s).matches()) {
            return null;
        }
        try {
            return YearMonth.parse(s);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static Boolean bool(Map<String, Object> param, String key) {
        Object v = param == null ? null : param.get(key);
        if (v instanceof Boolean b) {
            return b;
        }
        String s = str(param, key);
        return "true".equals(s) ? Boolean.TRUE : "false".equals(s) ? Boolean.FALSE : null;
    }
}
