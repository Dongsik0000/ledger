package ledger.cmmn.util;

import java.util.Map;

// @RequestBody HashMap 에서 문자열 꺼내기. 키가 없거나 값이 null 이면 "".
public class ParamUtil {

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
}
