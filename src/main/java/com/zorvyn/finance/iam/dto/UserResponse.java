package com.zorvyn.finance.iam.dto;

import com.zorvyn.finance.iam.domain.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * User response DTO — never exposes password hash or internal fields.
 */
@Getter
@Builder
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
}
