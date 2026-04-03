-- ═══════════════════════════════════════════════════════════════════════
-- V4: Create idempotency keys table
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id              UUID PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    status          VARCHAR(20) NOT NULL,
    request_hash    VARCHAR(64),
    request_method  VARCHAR(10),
    request_path    VARCHAR(500),
    response_status INT,
    response_body   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_idempotency_key ON idempotency_keys(idempotency_key);
CREATE INDEX idx_idempotency_expires_at ON idempotency_keys(expires_at);
