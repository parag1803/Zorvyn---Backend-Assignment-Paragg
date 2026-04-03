package com.zorvyn.finance.shared.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Scheduled task to clean up expired idempotency keys.
 * Runs every hour to prevent unbounded table growth.
 */
@Component
public class IdempotencyCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupScheduler.class);

    private final IdempotencyKeyRepository repository;

    public IdempotencyCleanupScheduler(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedRate = 3600000) // every hour
    @Transactional
    public void cleanupExpiredKeys() {
        int deleted = repository.deleteExpiredKeys(Instant.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired idempotency keys", deleted);
        }
    }
}
