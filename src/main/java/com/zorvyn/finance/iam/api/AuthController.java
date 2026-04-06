package com.zorvyn.finance.iam.api;

import com.zorvyn.finance.iam.application.AuthService;
import com.zorvyn.finance.iam.dto.*;
import com.zorvyn.finance.shared.dto.ErrorResponse;
import com.zorvyn.finance.shared.security.SecurityUtils;
import com.zorvyn.finance.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User registration, login, token management, and profile access")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the VIEWER role. Email must be unique."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already in use",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.zorvyn.finance.shared.dto.ApiResponse.created(user, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login",
        description = "Authenticate with email and password. Returns a short-lived **access token** (15 min) " +
                      "and a long-lived **refresh token** (7 days)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful — returns JWT token pair"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid email or password",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh token",
        description = "Exchange a valid refresh token for a new access token + refresh token pair. " +
                      "The old refresh token is revoked immediately."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Logout",
        description = "Blacklists the current access token and revokes all active refresh tokens for the user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logged out successfully"),
        @ApiResponse(responseCode = "401", description = "Not authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<Void>> logout(HttpServletRequest request,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token, principal.getId());
        }
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the authenticated user's profile. Requires a valid Bearer token."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current user profile returned"),
        @ApiResponse(responseCode = "401", description = "Not authenticated — missing or invalid token",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<com.zorvyn.finance.shared.dto.ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse user = SecurityUtils.getCurrentUser()
                .map(p -> UserResponse.builder()
                        .id(p.getId())
                        .email(p.getEmail())
                        .role(p.getRole())
                        .build())
                .orElse(null);
        return ResponseEntity.ok(com.zorvyn.finance.shared.dto.ApiResponse.success(user));
    }
}
