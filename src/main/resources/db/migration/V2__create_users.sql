-- ═══════════════════════════════════════════════════════════════════════
-- V2: Create users table
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role_id       UUID NOT NULL REFERENCES roles(id),
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    deleted_at    TIMESTAMP,
    created_by    UUID,
    updated_by    UUID,
    version       BIGINT DEFAULT 0
);

CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_users_role_id ON users(role_id);
