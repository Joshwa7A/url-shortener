package com.joshwa.urlshortener.scheduler;

import com.joshwa.urlshortener.repository.ShortUrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class ShortUrlCleanupScheduler {

    private final ShortUrlRepository shortUrlRepository;
    private final Clock clock;
    private final Duration retentionDuration;

    public ShortUrlCleanupScheduler(
            ShortUrlRepository shortUrlRepository,
            Clock clock,
            @Value("${joshwa.shorturlapp.retention-duration}") Duration retentionDuration) {
        this.shortUrlRepository = shortUrlRepository;
        this.clock = clock;
        this.retentionDuration = retentionDuration;
    }

    @Scheduled(cron = "${joshwa.shorturlapp.cleanup.cron}")
    @Transactional
    public void cleanupExpiredShortUrls(){
        Instant now = clock.instant();
        Instant threshold=now.minus(retentionDuration);
        int rowsCleanedUp=shortUrlRepository.deleteByExpiryDateBefore(threshold);
        log.info("Cleanup job executed. Retention duration: {}, threshold time: {}, rows deleted: {}",
                retentionDuration, threshold, rowsCleanedUp);
    }
}
