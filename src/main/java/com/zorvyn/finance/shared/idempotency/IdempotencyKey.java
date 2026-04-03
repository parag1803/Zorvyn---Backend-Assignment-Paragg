package com.zorvyn.finance.shared.idempotency;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Stores idempotency keys with their processing status and cached responses.
 * DB-backed for ACID consistency with business transactions.
 */
@Entity
@Table(name = "idempotency_keys",
       indexes = {
           @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
           @Index(name = "idx_idempotency_expires_at", columnList = "expires_at")
       })
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyKey {

    public enum Status {
        STARTED, COMPLETED, FAILED
    }

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_path", length = 500)
    private String requestPath;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
