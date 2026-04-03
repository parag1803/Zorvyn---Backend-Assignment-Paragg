package com.zorvyn.finance.iam.application;

import com.zorvyn.finance.iam.domain.*;
import com.zorvyn.finance.iam.dto.LoginRequest;
import com.zorvyn.finance.iam.dto.LoginResponse;
import com.zorvyn.finance.iam.dto.RegisterRequest;
import com.zorvyn.finance.iam.dto.UserResponse;
import com.zorvyn.finance.iam.mapper.UserMapper;
import com.zorvyn.finance.iam.repository.RefreshTokenRepository;
import com.zorvyn.finance.iam.repository.RoleRepository;
import com.zorvyn.finance.iam.repository.UserRepository;
import com.zorvyn.finance.shared.audit.AuditEventService;
import com.zorvyn.finance.shared.exception.DuplicateResourceException;
import com.zorvyn.finance.shared.security.JwtTokenProvider;
import com.zorvyn.finance.shared.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private UserMapper userMapper;
    @Mock private AuditEventService auditEventService;

    @InjectMocks
    private AuthService authService;

    private Role viewerRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        viewerRole = new Role();
        viewerRole.setId(UUID.randomUUID());
        viewerRole.setName("VIEWER");

        testUser = new User();
        testUser.setEmail("test@zorvyn.com");
        testUser.setPasswordHash("$2a$12$hashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(viewerRole);
        testUser.setStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Register: should create user with VIEWER role")
    void register_shouldCreateUserWithViewerRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@zorvyn.com");
        request.setPassword("Password123");
        request.setFirstName("New");
        request.setLastName("User");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(userMapper.toResponse(any(User.class))).thenReturn(
                UserResponse.builder().email("new@zorvyn.com").role("VIEWER").build());

        UserResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("new@zorvyn.com");
        assertThat(response.getRole()).isEqualTo("VIEWER");
        verify(userRepository).save(any(User.class));
        verify(auditEventService).logAction(eq("USER_REGISTERED"), eq("User"), any(), isNull(), any());
    }

    @Test
    @DisplayName("Register: should reject duplicate email")
    void register_shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@zorvyn.com");
        request.setPassword("Password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Login: should return JWT tokens for valid credentials")
    void login_shouldReturnTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@zorvyn.com");
        request.setPassword("Password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(900000L);
        when(userMapper.toResponse(any(User.class))).thenReturn(
                UserResponse.builder().email("test@zorvyn.com").build());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Login: should reject invalid password")
    void login_shouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@zorvyn.com");
        request.setPassword("WrongPassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Login: should reject non-existent user")
    void login_shouldRejectNonExistentUser() {
        LoginRequest request = new LoginRequest();
        request.setEmail("ghost@zorvyn.com");
        request.setPassword("Password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Logout: should blacklist token and revoke refresh tokens")
    void logout_shouldBlacklistAndRevoke() {
        UUID userId = UUID.randomUUID();
        String token = "some-access-token";

        authService.logout(token, userId);

        verify(tokenBlacklistService).blacklist(token);
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
}
