package ledger.recurring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

// 고정 항목 자동 기록: 매일 00:05(한국 시간) + 앱 기동 직후(꺼져 있던 동안의 결제일 따라잡기)
@Component
public class RecurringScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RecurringScheduler.class);
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Autowired
    private RecurringGenerator recurringGenerator;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void daily() {
        run("매일");
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onStartup() {
        run("기동");
    }

    // 기동 중 DB 오류 등으로 실패해도 앱 기동은 막지 않는다(다음 00:05 에 다시 시도)
    private void run(String trigger) {
        try {
            int created = recurringGenerator.generateAll(LocalDate.now(SEOUL));
            logger.info("고정 항목 자동 기록({}): {}건", trigger, created);
        } catch (RuntimeException e) {
            logger.error("고정 항목 자동 기록({}) 실패", trigger, e);
        }
    }
}
