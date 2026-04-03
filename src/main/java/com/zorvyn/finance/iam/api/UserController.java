package com.zorvyn.finance.iam.api;

import com.zorvyn.finance.iam.application.UserService;
import com.zorvyn.finance.iam.domain.UserStatus;
import com.zorvyn.finance.iam.dto.UpdateUserRequest;
import com.zorvyn.finance.iam.dto.UserResponse;
import com.zorvyn.finance.shared.dto.ApiResponse;
import com.zorvyn.finance.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Admin operations for user management")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Paginated list of all active users (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get a specific user's details (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update user profile, role, or status (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Activate, deactivate, or suspend a user (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        UserStatus status = UserStatus.valueOf(body.get("status").toUpperCase());
        UserResponse user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(user, "User status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Soft-delete a user (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
