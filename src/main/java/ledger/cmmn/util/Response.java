package ledger.cmmn.util;

// Ajax 공통 응답 포맷 (@ResponseBody 로 그대로 반환)
public class Response {

    private final String code;
    private final String message;
    private final Object data;

    private Response(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static Response of(String code) {
        return new Response(code, null, null);
    }

    public static Response of(String code, Object data) {
        return new Response(code, null, data);
    }

    public static Response of(String code, String message, Object data) {
        return new Response(code, message, data);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
