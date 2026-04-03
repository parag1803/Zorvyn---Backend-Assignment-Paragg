-- ═══════════════════════════════════════════════════════════════════════
-- V5: Create audit log table
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS audit_log (
    id             UUID PRIMARY KEY,
    user_id        UUID,
    user_email     VARCHAR(255),
    action         VARCHAR(50) NOT NULL,
    entity_type    VARCHAR(100) NOT NULL,
    entity_id      UUID,
    old_value      TEXT,
    new_value      TEXT,
    ip_address     VARCHAR(45),
    user_agent     VARCHAR(500),
    correlation_id VARCHAR(100),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_log_action ON audit_log(action);
