package com.zorvyn.finance.iam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Refresh token entity for token rotation.
 * Each refresh token is single-use — once consumed, a new pair (access+refresh) is issued.
 */
@Entity
@Table(name = "refresh_tokens",
       indexes = {
           @Index(name = "idx_refresh_token_user", columnList = "user_id"),
           @Index(name = "idx_refresh_token_token", columnList = "token", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !revoked && !isExpired();
    }
}
