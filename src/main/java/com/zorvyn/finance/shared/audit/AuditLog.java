package com.zorvyn.finance.shared.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Audit log entity capturing WHO did WHAT to WHICH record WHEN.
 * Provides a complete audit trail for compliance and debugging.
 */
@Entity
@Table(name = "audit_log",
       indexes = {
           @Index(name = "idx_audit_log_entity", columnList = "entity_type, entity_id"),
           @Index(name = "idx_audit_log_user", columnList = "user_id"),
           @Index(name = "idx_audit_log_created_at", columnList = "created_at")
       })
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
