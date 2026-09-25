package ledger.cmmn.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

// 로그인·가입 코드 연속 실패 제한. 같은 키로 5번 실패하면 5분 차단. 메모리에만 둔다(단일 서버).
@Component
public class LoginAttemptLimiter {

    static final int MAX_FAILURES = 5;
    static final long BLOCK_MILLIS = 5 * 60 * 1000L;
    private static final int PURGE_THRESHOLD = 10_000; // 키가 이만큼 쌓이면 오래된 것부터 정리

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public LoginAttemptLimiter() {
        this(System::currentTimeMillis);
    }

    LoginAttemptLimiter(LongSupplier clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        return attempt != null && attempt.blockedUntil > clock.getAsLong();
    }

    public void recordFailure(String key) {
        long now = clock.getAsLong();
        if (attempts.size() > PURGE_THRESHOLD) {
            attempts.values().removeIf(a -> a.lastFailure + BLOCK_MILLIS < now && a.blockedUntil <= now);
        }
        attempts.compute(key, (k, attempt) -> {
            boolean blockEnded = attempt != null && attempt.blockedUntil != 0 && attempt.blockedUntil <= now;
            boolean stale = attempt != null && attempt.lastFailure + BLOCK_MILLIS < now;
            if (attempt == null || blockEnded || stale) {
                attempt = new Attempt();
            }
            attempt.failures++;
            attempt.lastFailure = now;
            if (attempt.failures >= MAX_FAILURES) {
                attempt.blockedUntil = now + BLOCK_MILLIS;
            }
            return attempt;
        });
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private static final class Attempt {
        int failures;
        long lastFailure;
        long blockedUntil;
    }
}
