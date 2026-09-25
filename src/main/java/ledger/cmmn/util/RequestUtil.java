package ledger.cmmn.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {

    private static final int IP_MAX_LENGTH = 45; // IPv6 문자열 최대 길이

    private RequestUtil() {
    }

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    // 클라이언트 IP. 서버에서 Tomcat 은 127.0.0.1 에만 열려 있고 nginx 가 X-Real-IP 를 넣는다.
    // 그래서 루프백에서 온 요청일 때만 X-Real-IP 를 믿는다.
    public static String clientIp(HttpServletRequest request) {
        return clientIp(request.getRemoteAddr(), request.getHeader("X-Real-IP"));
    }

    static String clientIp(String remoteAddr, String realIpHeader) {
        boolean loopback = "127.0.0.1".equals(remoteAddr)
                || "0:0:0:0:0:0:0:1".equals(remoteAddr)
                || "::1".equals(remoteAddr);
        if (loopback && realIpHeader != null) {
            String realIp = realIpHeader.trim();
            if (!realIp.isEmpty() && realIp.length() <= IP_MAX_LENGTH) {
                return realIp;
            }
        }
        return remoteAddr;
    }
}
