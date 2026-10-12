package ledger.cmmn.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 클라이언트 IP 판단(nginx 뒤) 테스트
class RequestUtilTest {

    // 루프백에서 온 요청은 X-Real-IP 를 쓴다
    @Test
    void loopbackWithHeaderUsesHeader() {
        assertEquals("203.0.113.7", RequestUtil.clientIp("127.0.0.1", "203.0.113.7"));
        assertEquals("203.0.113.7", RequestUtil.clientIp("0:0:0:0:0:0:0:1", " 203.0.113.7 "));
    }

    // 헤더가 없으면 접속 주소
    @Test
    void loopbackWithoutHeaderUsesRemote() {
        assertEquals("127.0.0.1", RequestUtil.clientIp("127.0.0.1", null));
        assertEquals("127.0.0.1", RequestUtil.clientIp("127.0.0.1", "  "));
    }

    // 루프백이 아니면 헤더를 믿지 않는다
    @Test
    void nonLoopbackIgnoresHeader() {
        assertEquals("198.51.100.2", RequestUtil.clientIp("198.51.100.2", "203.0.113.7"));
    }

    // 너무 긴 헤더는 무시
    @Test
    void tooLongHeaderIgnored() {
        assertEquals("127.0.0.1", RequestUtil.clientIp("127.0.0.1", "1".repeat(46)));
    }
}
