-- ═══════════════════════════════════════════════════════════════════════
-- V1: Create roles and permissions tables with seed data
-- ═══════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS permissions (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource    VARCHAR(50) NOT NULL,
    action      VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id          UUID PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id       UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Seed permissions
INSERT INTO permissions (id, name, description, resource, action) VALUES
    ('a0000001-0000-0000-0000-000000000001', 'record:create', 'Create financial records', 'record', 'create'),
    ('a0000001-0000-0000-0000-000000000002', 'record:read', 'Read financial records', 'record', 'read'),
    ('a0000001-0000-0000-0000-000000000003', 'record:update', 'Update financial records', 'record', 'update'),
    ('a0000001-0000-0000-0000-000000000004', 'record:delete', 'Delete financial records', 'record', 'delete'),
    ('a0000001-0000-0000-0000-000000000005', 'user:manage', 'Manage users', 'user', 'manage'),
    ('a0000001-0000-0000-0000-000000000006', 'dashboard:read', 'View dashboard data', 'dashboard', 'read'),
    ('a0000001-0000-0000-0000-000000000007', 'analytics:read', 'View analytics & insights', 'analytics', 'read'),
    ('a0000001-0000-0000-0000-000000000008', 'analytics:export', 'Export analytics data', 'analytics', 'export');

-- Seed roles
INSERT INTO roles (id, name, description) VALUES
    ('b0000001-0000-0000-0000-000000000001', 'ADMIN', 'Full system access — can manage users, records, and analytics'),
    ('b0000001-0000-0000-0000-000000000002', 'ANALYST', 'Can view records and access insights and analytics'),
    ('b0000001-0000-0000-0000-000000000003', 'VIEWER', 'Can view dashboard summary and recent activity');

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000001'),
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000002'),
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000003'),
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000004'),
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000005'),
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000006'),
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000007'),
    ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000008');

-- ANALYST gets read permissions + analytics
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000002'),
    ('b0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000006'),
    ('b0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000007');

-- VIEWER gets dashboard read only
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0000001-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000006');
