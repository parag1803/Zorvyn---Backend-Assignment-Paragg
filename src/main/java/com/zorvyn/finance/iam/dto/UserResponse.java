package com.zorvyn.finance.iam.dto;

import com.zorvyn.finance.iam.domain.UserStatus;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * User response DTO — never exposes password hash or internal fields.
 */
@Getter
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private UserStatus status;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    private UserResponse(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.fullName = builder.fullName;
        this.role = builder.role;
        this.status = builder.status;
        this.lastLoginAt = builder.lastLoginAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String role;
        private UserStatus status;
        private Instant lastLoginAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder status(UserStatus status) { this.status = status; return this; }
        public Builder lastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserResponse build() { return new UserResponse(this); }
    }
}
