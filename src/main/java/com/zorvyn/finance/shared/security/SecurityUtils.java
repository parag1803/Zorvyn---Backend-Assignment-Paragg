package com.zorvyn.finance.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for accessing the current authenticated user from the security context.
 * Used by services and the AuditorAware implementation.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Get the current authenticated user's principal.
     */
    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    /**
     * Get the current user's ID. Returns empty if not authenticated.
     */
    public static Optional<UUID> getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId);
    }

    /**
     * Get the current user's role name.
     */
    public static Optional<String> getCurrentUserRole() {
        return getCurrentUser().map(UserPrincipal::getRole);
    }

    /**
     * Check if the current user has a specific role.
     */
    public static boolean hasRole(String role) {
        return getCurrentUserRole().map(r -> r.equalsIgnoreCase(role)).orElse(false);
    }

    /**
     * Check if the current user is an admin.
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
