package ledger.cmmn.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptLimiterTest {

    private final long[] now = {1_000_000L};
    private LoginAttemptLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginAttemptLimiter(() -> now[0]);
    }

    private void fail(String key, int times) {
        for (int i = 0; i < times; i++) {
            limiter.recordFailure(key);
        }
    }

    @Test
    void fourFailuresNotBlocked() {
        fail("a", 4);
        assertFalse(limiter.isBlocked("a"));
    }

    @Test
    void fifthFailureBlocks() {
        fail("a", 5);
        assertTrue(limiter.isBlocked("a"));
    }

    @Test
    void blockExpiresAfterFiveMinutes() {
        fail("a", 5);
        now[0] += LoginAttemptLimiter.BLOCK_MILLIS - 1;
        assertTrue(limiter.isBlocked("a"));
        now[0] += 1;
        assertFalse(limiter.isBlocked("a"));
        fail("a", 1);   // 차단이 끝나면 횟수도 새로 센다
        assertFalse(limiter.isBlocked("a"));
    }

    @Test
    void oldFailuresForgotten() {
        fail("a", 4);
        now[0] += LoginAttemptLimiter.BLOCK_MILLIS + 1;
        fail("a", 1);
        assertFalse(limiter.isBlocked("a"));
    }

    @Test
    void resetClears() {
        fail("a", 4);
        limiter.reset("a");
        fail("a", 4);
        assertFalse(limiter.isBlocked("a"));
    }

    @Test
    void keysAreIndependent() {
        fail("a", 5);
        assertFalse(limiter.isBlocked("b"));
    }
}
