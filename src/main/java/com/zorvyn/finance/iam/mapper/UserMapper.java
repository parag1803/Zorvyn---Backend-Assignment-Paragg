package com.zorvyn.finance.iam.mapper;

import com.zorvyn.finance.iam.domain.User;
import com.zorvyn.finance.iam.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Maps User entities to UserResponse DTOs.
 * Keeps the mapping logic centralized and testable.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
