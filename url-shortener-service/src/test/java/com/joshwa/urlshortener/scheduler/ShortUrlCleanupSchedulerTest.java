package com.joshwa.urlshortener.scheduler;

import com.joshwa.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlCleanupSchedulerTest {

    @Mock
    private ShortUrlRepository repository;

    @Mock
    private Clock clock;

    @Test
    void shouldDeleteExpiredUrls_usingRetentionDuration() {

        Duration retention = Duration.ofHours(1);
        Instant now = Instant.parse("2026-02-23T10:00:00Z");

        when(clock.instant()).thenReturn(now);

        ShortUrlCleanupScheduler scheduler =
                new ShortUrlCleanupScheduler(repository, clock, retention);

        scheduler.cleanupExpiredShortUrls();

        Instant expectedThreshold = now.minus(retention);

        verify(repository).deleteByExpiryDateBefore(expectedThreshold);
        verifyNoMoreInteractions(repository);
    }
}