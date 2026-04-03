package com.zorvyn.finance.iam.application;

import com.zorvyn.finance.iam.domain.*;
import com.zorvyn.finance.iam.dto.*;
import com.zorvyn.finance.iam.mapper.UserMapper;
import com.zorvyn.finance.iam.repository.RefreshTokenRepository;
import com.zorvyn.finance.iam.repository.RoleRepository;
import com.zorvyn.finance.iam.repository.UserRepository;
import com.zorvyn.finance.shared.audit.AuditEventService;
import com.zorvyn.finance.shared.exception.BusinessRuleException;
import com.zorvyn.finance.shared.exception.DuplicateResourceException;
import com.zorvyn.finance.shared.exception.ResourceNotFoundException;
import com.zorvyn.finance.shared.security.JwtTokenProvider;
import com.zorvyn.finance.shared.security.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Authentication service handling registration, login, token refresh, and logout.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;
    private final AuditEventService auditEventService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       TokenBlacklistService tokenBlacklistService,
                       UserMapper userMapper,
                       AuditEventService auditEventService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userMapper = userMapper;
        this.auditEventService = auditEventService;
    }

    /**
     * Register a new user with default VIEWER role.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Get default role
        Role viewerRole = roleRepository.findByName("VIEWER")
                .orElseThrow(() -> new BusinessRuleException("Default role VIEWER not found. System not properly initialized."));

        // Create user
        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setRole(viewerRole);
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getEmail());

        auditEventService.logAction("USER_REGISTERED", "User", saved.getId(), null, userMapper.toResponse(saved));

        return userMapper.toResponse(saved);
    }

    /**
     * Authenticate user and issue JWT token pair.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            auditEventService.logAction("LOGIN_FAILED", "User", user.getId(), null, null);
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check account status
        if (!user.isActive()) {
            throw new BusinessRuleException("Account is " + user.getStatus().name().toLowerCase() +
                    ". Please contact an administrator.", "ACCOUNT_INACTIVE");
        }

        // Update last login
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().getName());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        // Store refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiresAt(Instant.now().plusMillis(
                jwtTokenProvider.getAccessTokenExpiryMs() * 7)); // 7x access token expiry
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in: {}", user.getEmail());
        auditEventService.logAction("LOGIN_SUCCESS", "User", user.getId(), null, null);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    /**
     * Refresh access token using a valid refresh token.
     * Implements token rotation: the old refresh token is consumed and a new pair is issued.
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        // Find and validate refresh token
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));

        if (!storedToken.isValid()) {
            // Potential token reuse attack — revoke all tokens for this user
            if (storedToken.isUsed()) {
                log.warn("Refresh token reuse detected for user: {}. Revoking all tokens.", storedToken.getUserId());
                refreshTokenRepository.revokeAllByUserId(storedToken.getUserId());
            }
            throw new BusinessRuleException("Refresh token expired or revoked", "REFRESH_TOKEN_INVALID");
        }

        // Mark current refresh token as used
        storedToken.setUsed(true);
        refreshTokenRepository.save(storedToken);

        // Find user
        User user = userRepository.findActiveById(storedToken.getUserId())
                .orElseThrow(() -> new BusinessRuleException("User account not found or inactive"));

        // Generate new token pair
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().getName());
        String newRefreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId());

        // Store new refresh token
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenStr);
        newRefreshToken.setUserId(user.getId());
        newRefreshToken.setExpiresAt(Instant.now().plusMillis(
                jwtTokenProvider.getAccessTokenExpiryMs() * 7));
        refreshTokenRepository.save(newRefreshToken);

        log.debug("Token refreshed for user: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    /**
     * Logout: blacklist the access token and revoke all refresh tokens.
     */
    @Transactional
    public void logout(String accessToken, java.util.UUID userId) {
        tokenBlacklistService.blacklist(accessToken);
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("User logged out: {}", userId);
        auditEventService.logAction("LOGOUT", "User", userId, null, null);
    }
}
