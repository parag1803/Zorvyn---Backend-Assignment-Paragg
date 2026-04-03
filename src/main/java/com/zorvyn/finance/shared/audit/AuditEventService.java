package com.zorvyn.finance.shared.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.finance.shared.security.SecurityUtils;
import com.zorvyn.finance.shared.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Audit event service for recording user actions.
 * Writes are async and use a separate transaction to not impact main business flow.
 */
@Service
public class AuditEventService {

    private static final Logger log = LoggerFactory.getLogger(AuditEventService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditEventService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, UUID entityId,
                           Object oldValue, Object newValue) {
        try {
            AuditLog entry = new AuditLog();
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setCorrelationId(MDC.get("correlationId"));

            SecurityUtils.getCurrentUser().ifPresent(user -> {
                entry.setUserId(user.getId());
                entry.setUserEmail(user.getEmail());
            });

            if (oldValue != null) {
                entry.setOldValue(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                entry.setNewValue(objectMapper.writeValueAsString(newValue));
            }

            auditLogRepository.save(entry);
            log.debug("Audit log recorded: {} {} {}", action, entityType, entityId);

        } catch (Exception e) {
            // Audit logging should never break the main business flow
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }
}
