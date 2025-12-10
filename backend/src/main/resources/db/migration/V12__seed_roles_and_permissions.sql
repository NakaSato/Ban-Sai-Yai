-- Seed default roles (Idempotent using name lookup)
-- Note: V7 might have already seeded these.
INSERT INTO roles (role_name, description, created_at) VALUES
('ROLE_PRESIDENT', 'President - Full system access', CURRENT_TIMESTAMP),
('ROLE_SECRETARY', 'Secretary - Administrative access', CURRENT_TIMESTAMP),
('ROLE_OFFICER', 'Officer - Operational access', CURRENT_TIMESTAMP),
('ROLE_MEMBER', 'Member - Basic access', CURRENT_TIMESTAMP)
ON CONFLICT (role_name) DO NOTHING;

-- Seed default permissions (Idempotent using slug lookup)
-- Adapting V12 permissions to V7 schema (perm_slug, module)
INSERT INTO permissions (perm_slug, module, description, created_at) VALUES
('VIEW_DASHBOARD', 'General', 'View dashboard', CURRENT_TIMESTAMP),
('MANAGE_MEMBERS', 'Member Management', 'Manage members', CURRENT_TIMESTAMP),
('MANAGE_LOANS', 'Loans', 'Manage loans', CURRENT_TIMESTAMP),
('MANAGE_SAVINGS', 'Accounting', 'Manage savings', CURRENT_TIMESTAMP),
('MANAGE_PAYMENTS', 'Transactions', 'Manage payments', CURRENT_TIMESTAMP),
('VIEW_REPORTS', 'Reporting', 'View reports', CURRENT_TIMESTAMP),
('MANAGE_SETTINGS', 'System', 'Manage system settings', CURRENT_TIMESTAMP),
('MANAGE_USERS', 'System', 'Manage users', CURRENT_TIMESTAMP)
ON CONFLICT (perm_slug) DO NOTHING;

-- Assign permissions to roles (Using lookups)

-- President: All newly added permissions
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_PRESIDENT'
AND p.perm_slug IN (
    'VIEW_DASHBOARD', 'MANAGE_MEMBERS', 'MANAGE_LOANS', 'MANAGE_SAVINGS', 
    'MANAGE_PAYMENTS', 'VIEW_REPORTS', 'MANAGE_SETTINGS', 'MANAGE_USERS'
)
ON CONFLICT DO NOTHING;

-- Secretary: Administrative permissions
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_SECRETARY'
AND p.perm_slug IN (
    'VIEW_DASHBOARD', 'MANAGE_MEMBERS', 'MANAGE_LOANS', 'MANAGE_SAVINGS', 
    'MANAGE_PAYMENTS', 'VIEW_REPORTS'
)
ON CONFLICT DO NOTHING;

-- Officer: Operational permissions
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_OFFICER'
AND p.perm_slug IN (
    'VIEW_DASHBOARD', 'MANAGE_MEMBERS', 'MANAGE_LOANS', 'MANAGE_SAVINGS', 'MANAGE_PAYMENTS'
)
ON CONFLICT DO NOTHING;

-- Member: Basic permissions
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_MEMBER'
AND p.perm_slug IN ('VIEW_DASHBOARD')
ON CONFLICT DO NOTHING;

-- Seed default users (password is bcrypt hash of username + "123")
-- Seed default users (password is bcrypt hash of username + "123")
-- president/president123, secretary/secretary123, officer/officer123, member/member123
INSERT INTO users (uuid, username, password, email, role, role_id, status, created_at, updated_at)
SELECT 
    gen_random_uuid(), 
    'president', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
    'president@bansaiyai.com', 
    'PRESIDENT',
    (SELECT role_id FROM roles WHERE role_name = 'ROLE_PRESIDENT'), 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'president');

INSERT INTO users (uuid, username, password, email, role, role_id, status, created_at, updated_at)
SELECT 
    gen_random_uuid(), 
    'secretary', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
    'secretary@bansaiyai.com', 
    'SECRETARY',
    (SELECT role_id FROM roles WHERE role_name = 'ROLE_SECRETARY'), 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'secretary');

INSERT INTO users (uuid, username, password, email, role, role_id, status, created_at, updated_at)
SELECT 
    gen_random_uuid(), 
    'officer', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
    'officer@bansaiyai.com', 
    'OFFICER',
    (SELECT role_id FROM roles WHERE role_name = 'ROLE_OFFICER'), 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'officer');

INSERT INTO users (uuid, username, password, email, role, role_id, status, created_at, updated_at)
SELECT 
    gen_random_uuid(), 
    'member', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 
    'member@bansaiyai.com', 
    'MEMBER',
    (SELECT role_id FROM roles WHERE role_name = 'ROLE_MEMBER'), 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'member');
