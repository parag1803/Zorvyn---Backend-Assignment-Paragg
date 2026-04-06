package com.zorvyn.finance.iam.api;

import com.zorvyn.finance.iam.application.UserService;
import com.zorvyn.finance.iam.domain.UserStatus;
import com.zorvyn.finance.iam.dto.UpdateUserRequest;
import com.zorvyn.finance.iam.dto.UserResponse;
import com.zorvyn.finance.shared.dto.ErrorResponse;
import com.zorvyn.finance.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(
    name = "User Management",
    description = "Admin-only operations for managing user accounts.\n\n" +
                  "All endpoints require the **ADMIN** role. Returns `403 Forbidden` for other roles."
)
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
        summary = "List all users",
        description = "Returns a paginated list of all active (non-deleted) users. **Admin only.**"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Returns the full profile of a specific user by UUID. **Admin only.**"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user",
        description = "Update a user's first name, last name, role, or status. **Admin only.**"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(user, "User updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Update user status",
        description = "Change a user's lifecycle status. **Admin only.**\n\n" +
                      "Valid statuses: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING`"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User status updated"),
        @ApiResponse(responseCode = "400", description = "Invalid status value",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<UserResponse>> updateUserStatus(
            @Parameter(description = "User UUID") @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Status update — valid values: ACTIVE, INACTIVE, SUSPENDED, PENDING",
                content = @Content(schema = @Schema(example = "{\"status\": \"ACTIVE\"}")))
            @RequestBody Map<String, String> body) {
        UserStatus status = UserStatus.valueOf(body.get("status").toUpperCase());
        UserResponse user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(user, "User status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Soft-deletes a user account (sets `deletedAt`). The user can no longer log in. " +
                      "**Admin only.**"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully — no content returned"),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden — ADMIN role required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID") @PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
