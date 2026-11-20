package ledger.cmmn.util;

// Ajax 공통 응답 {code, message, data}. @ResponseBody 로 그대로 반환하고, message·data 는 없으면 null
public class Response {

    private final String code;
    private final String message;
    private final Object data;

    private Response(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 코드만
    public static Response of(String code) {
        return new Response(code, null, null);
    }

    // 코드와 데이터
    public static Response of(String code, Object data) {
        return new Response(code, null, data);
    }

    // 코드·화면에 보일 문구·데이터
    public static Response of(String code, String message, Object data) {
        return new Response(code, message, data);
    }

    // 입력 오류(90). message 는 화면에 그대로 보인다
    public static Response invalid(String message) {
        return new Response(Constants.INVALID, message, null);
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
