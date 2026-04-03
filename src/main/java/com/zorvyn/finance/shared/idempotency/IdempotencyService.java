package com.zorvyn.finance.shared.idempotency;

import com.zorvyn.finance.shared.exception.IdempotencyConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Service managing idempotency key lifecycle:
 * 1. Check if key exists (replay cached response)
 * 2. Create key as STARTED (begin processing)
 * 3. Mark key as COMPLETED with cached response
 * 4. Mark key as FAILED on error
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final IdempotencyKeyRepository repository;
    private final long ttlHours;

    public IdempotencyService(IdempotencyKeyRepository repository,
                               @Value("${app.idempotency.ttl-hours}") long ttlHours) {
        this.repository = repository;
        this.ttlHours = ttlHours;
    }

    /**
     * Check if an idempotency key already exists and return it if completed.
     */
    @Transactional(readOnly = true)
    public Optional<IdempotencyKey> findExisting(String key) {
        return repository.findByKey(key);
    }

    /**
     * Create a new idempotency key entry with STARTED status.
     * Uses a new transaction to ensure the key is visible to concurrent requests.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyKey startProcessing(String key, String requestHash,
                                           String method, String path) {
        // Double-check for existing key (race condition handling)
        Optional<IdempotencyKey> existing = repository.findByKey(key);
        if (existing.isPresent()) {
            IdempotencyKey ik = existing.get();
            if (ik.getStatus() == IdempotencyKey.Status.STARTED) {
                throw new IdempotencyConflictException(
                        "Request with this idempotency key is currently being processed");
            }
            if (ik.getStatus() == IdempotencyKey.Status.COMPLETED) {
                return ik; // Will be replayed by the filter
            }
        }

        IdempotencyKey entry = new IdempotencyKey();
        entry.setKey(key);
        entry.setStatus(IdempotencyKey.Status.STARTED);
        entry.setRequestHash(requestHash);
        entry.setRequestMethod(method);
        entry.setRequestPath(path);
        entry.setExpiresAt(Instant.now().plus(ttlHours, ChronoUnit.HOURS));

        return repository.save(entry);
    }

    /**
     * Mark the key as completed with the response data.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(String key, int responseStatus, String responseBody) {
        repository.findByKey(key).ifPresent(entry -> {
            entry.setStatus(IdempotencyKey.Status.COMPLETED);
            entry.setResponseStatus(responseStatus);
            entry.setResponseBody(responseBody);
            repository.save(entry);
            log.debug("Idempotency key completed: {}", key);
        });
    }

    /**
     * Mark the key as failed so it can be retried.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String key) {
        repository.findByKey(key).ifPresent(entry -> {
            repository.delete(entry);
            log.debug("Idempotency key removed on failure: {}", key);
        });
    }
}
