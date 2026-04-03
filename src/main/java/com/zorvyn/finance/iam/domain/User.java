package com.zorvyn.finance.iam.domain;

import com.zorvyn.finance.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * User entity representing an authenticated user in the system.
 * Extends BaseEntity for UUID PK, audit fields, optimistic locking, and soft delete.
 */
@Entity
@Table(name = "users",
       indexes = {
           @Index(name = "idx_users_email", columnList = "email", unique = true),
           @Index(name = "idx_users_status", columnList = "status"),
           @Index(name = "idx_users_deleted_at", columnList = "deleted_at")
       })
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * Get the user's full display name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if the user account is currently active.
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isDeleted();
    }
}
