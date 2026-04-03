package com.zorvyn.finance.shared.audit;

import com.zorvyn.finance.shared.security.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides the current authenticated user's ID for JPA @CreatedBy and @LastModifiedBy.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return SecurityUtils.getCurrentUserId();
    }
}
