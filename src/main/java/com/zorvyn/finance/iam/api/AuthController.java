package com.zorvyn.finance.iam.api;

import com.zorvyn.finance.iam.application.AuthService;
import com.zorvyn.finance.iam.dto.*;
import com.zorvyn.finance.shared.dto.ApiResponse;
import com.zorvyn.finance.shared.security.SecurityUtils;
import com.zorvyn.finance.shared.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and token management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with VIEWER role")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(user, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email and password to receive JWT tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Exchange a valid refresh token for a new token pair")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate current access token and revoke all refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token, principal.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        // The principal already has basic info, but we fetch fresh from DB
        UserResponse user = SecurityUtils.getCurrentUser()
                .map(p -> UserResponse.builder()
                        .id(p.getId())
                        .email(p.getEmail())
                        .role(p.getRole())
                        .build())
                .orElse(null);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
