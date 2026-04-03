-- ═══════════════════════════════════════════════════════════════════════
-- V3: Create financial records table
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS financial_records (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES users(id),
    amount           DECIMAL(19,4) NOT NULL,
    type             VARCHAR(20) NOT NULL,
    category         VARCHAR(50) NOT NULL,
    description      VARCHAR(500),
    notes            TEXT,
    transaction_date DATE NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    deleted_at       TIMESTAMP,
    created_by       UUID,
    updated_by       UUID,
    version          BIGINT DEFAULT 0
);

CREATE INDEX idx_records_user_id ON financial_records(user_id);
CREATE INDEX idx_records_type ON financial_records(type);
CREATE INDEX idx_records_category ON financial_records(category);
CREATE INDEX idx_records_transaction_date ON financial_records(transaction_date);
CREATE INDEX idx_records_deleted_at ON financial_records(deleted_at);
CREATE INDEX idx_records_composite ON financial_records(user_id, type, transaction_date, deleted_at);
