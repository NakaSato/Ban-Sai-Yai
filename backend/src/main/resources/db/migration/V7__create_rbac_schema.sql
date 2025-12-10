-- ============================================================================
-- RBAC Security System Migration
-- Creates roles, permissions, role_permissions tables and extends existing tables
-- PostgreSQL Compatible
-- ============================================================================

-- ============================================================================
-- 1. Create roles table
-- ============================================================================
CREATE TABLE IF NOT EXISTS roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_role_name CHECK (role_name IN ('ROLE_OFFICER', 'ROLE_SECRETARY', 'ROLE_PRESIDENT', 'ROLE_MEMBER'))
);

-- Seed roles data
INSERT INTO roles (role_name, description) VALUES
('ROLE_OFFICER', 'Operational user responsible for cash handling and transaction entry'),
('ROLE_SECRETARY', 'Auditor/Controller responsible for accounting accuracy and financial statements'),
('ROLE_PRESIDENT', 'Approver/Executive with super admin visibility and user management'),
('ROLE_MEMBER', 'End user with restricted access to personal data only');

-- ============================================================================
-- 2. Create permissions table
-- ============================================================================
CREATE TABLE IF NOT EXISTS permissions (
    perm_id SERIAL PRIMARY KEY,
    perm_slug VARCHAR(50) NOT NULL UNIQUE,
    module VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_module ON permissions(module);

-- Seed permissions data
INSERT INTO permissions (perm_slug, module, description) VALUES
-- Member Management
('member.view', 'Member Management', 'View member profiles'),
('member.create', 'Member Management', 'Register new members'),
('member.edit', 'Member Management', 'Edit member information'),
('member.delete', 'Member Management', 'Delete member records'),

-- Transaction Management
('transaction.create', 'Transactions', 'Create deposits and withdrawals'),
('transaction.view', 'Transactions', 'View transaction history'),
('transaction.void', 'Transactions', 'Void transactions within 24 hours'),
('transaction.approve', 'Transactions', 'Approve transactions'),

-- Loan Management
('loan.view', 'Loans', 'View loan details'),
('loan.create', 'Loans', 'Create loan applications'),
('loan.approve', 'Loans', 'Approve loan applications'),
('loan.edit', 'Loans', 'Edit loan information'),
('loan.delete', 'Loans', 'Delete loan records'),

-- Accounting
('accounting.view', 'Accounting', 'View general ledger'),
('accounting.edit', 'Accounting', 'Edit chart of accounts'),
('accounting.post', 'Accounting', 'Post journal entries'),
('accounting.close', 'Accounting', 'Close accounting periods'),

-- Reporting
('report.operational', 'Reporting', 'View operational reports'),
('report.financial', 'Reporting', 'View financial statements'),

-- Cash Reconciliation
('cash.reconcile', 'Cash Management', 'Perform cash reconciliation'),
('cash.approve_variance', 'Cash Management', 'Approve cash discrepancies'),
('cash.close_day', 'Cash Management', 'Close business day'),

-- Audit and System
('audit.view', 'System', 'View audit logs'),
('system.manage_users', 'System', 'Manage user accounts'),
('system.manage_roles', 'System', 'Manage roles and permissions'),
('system.set_dividend', 'System', 'Set dividend rate');

-- ============================================================================
-- 3. Create role_permissions junction table
-- ============================================================================
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    perm_id INT NOT NULL,
    PRIMARY KEY (role_id, perm_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    FOREIGN KEY (perm_id) REFERENCES permissions(perm_id) ON DELETE CASCADE
);

CREATE INDEX idx_role_id ON role_permissions(role_id);
CREATE INDEX idx_perm_id ON role_permissions(perm_id);

-- ============================================================================
-- 4. Seed role_permissions mappings
-- ============================================================================

-- Officer permissions (Operational user - cash handling and transactions)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_OFFICER'
AND p.perm_slug IN (
    'member.view',
    'member.create',
    'transaction.create',
    'transaction.view',
    'transaction.void',
    'loan.view',
    'report.operational',
    'cash.reconcile'
);

-- Secretary permissions (Auditor/Controller - accounting and approval)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_SECRETARY'
AND p.perm_slug IN (
    'member.view',
    'transaction.view',
    'loan.view',
    'accounting.view',
    'accounting.edit',
    'accounting.post',
    'accounting.close',
    'report.operational',
    'report.financial',
    'cash.approve_variance'
);

-- President permissions (Executive - approvals and oversight)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_PRESIDENT'
AND p.perm_slug IN (
    'member.view',
    'member.edit',
    'transaction.view',
    'loan.view',
    'loan.approve',
    'accounting.view',
    'report.operational',
    'report.financial',
    'audit.view',
    'system.manage_users',
    'system.manage_roles',
    'system.set_dividend'
);

-- Member permissions (End user - personal data only)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_MEMBER'
AND p.perm_slug IN (
    'member.view'
);

-- ============================================================================
-- 5. Extend users table with role_id and status
-- ============================================================================
ALTER TABLE users
ADD COLUMN IF NOT EXISTS role_id INT,
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'Active',
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
ADD CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);

-- ============================================================================
-- 6. Create system_audit_log table
-- ============================================================================
CREATE TABLE IF NOT EXISTS system_audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    ip_address VARCHAR(45),
    old_values JSON,
    new_values JSON,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_user ON system_audit_log(user_id);
CREATE INDEX idx_audit_timestamp ON system_audit_log(timestamp);
CREATE INDEX idx_audit_action ON system_audit_log(action);
CREATE INDEX idx_audit_entity ON system_audit_log(entity_type, entity_id);

-- ============================================================================
-- 7. Create cash_reconciliations table
-- ============================================================================
CREATE TYPE reconciliation_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

CREATE TABLE IF NOT EXISTS cash_reconciliations (
    reconciliation_id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    officer_id BIGINT NOT NULL,
    physical_count DECIMAL(15, 2) NOT NULL,
    database_balance DECIMAL(15, 2) NOT NULL,
    variance DECIMAL(15, 2) NOT NULL,
    status reconciliation_status_enum DEFAULT 'PENDING',
    secretary_id BIGINT,
    secretary_notes TEXT,
    officer_notes TEXT,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (officer_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (secretary_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_reconciliation_date ON cash_reconciliations(date);
CREATE INDEX idx_reconciliation_status ON cash_reconciliations(status);
CREATE INDEX idx_reconciliation_officer ON cash_reconciliations(officer_id);
CREATE INDEX idx_reconciliation_secretary ON cash_reconciliations(secretary_id);

-- ============================================================================
-- 8. Extend login_attempts table with locked_until column
-- ============================================================================
-- Note: The lockout_until column already exists from V3 migration
-- We will use the existing lockout_until column for consistency
-- No changes needed to login_attempts table

-- ============================================================================
-- 9. Guarantor table already exists from V1 migration
-- ============================================================================
-- No changes needed

-- ============================================================================
-- 10. Add transaction creator and approver tracking
-- ============================================================================

-- Extend saving_transaction table
ALTER TABLE saving_transaction
ADD COLUMN IF NOT EXISTS creator_user_id BIGINT,
ADD COLUMN IF NOT EXISTS approver_user_id BIGINT,
ADD COLUMN IF NOT EXISTS approval_status approval_status_enum DEFAULT 'APPROVED',
ADD COLUMN IF NOT EXISTS voided_at TIMESTAMP NULL,
ADD CONSTRAINT fk_saving_transaction_creator FOREIGN KEY (creator_user_id) REFERENCES users(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_saving_transaction_approver FOREIGN KEY (approver_user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_saving_transaction_creator ON saving_transaction(creator_user_id);
CREATE INDEX IF NOT EXISTS idx_saving_transaction_approver ON saving_transaction(approver_user_id);
CREATE INDEX IF NOT EXISTS idx_saving_transaction_approval_status ON saving_transaction(approval_status);

-- Extend payments table
ALTER TABLE payments
ADD COLUMN IF NOT EXISTS creator_user_id BIGINT,
ADD COLUMN IF NOT EXISTS approver_user_id BIGINT,
ADD COLUMN IF NOT EXISTS approval_status approval_status_enum DEFAULT 'APPROVED',
ADD COLUMN IF NOT EXISTS voided_at TIMESTAMP NULL,
ADD CONSTRAINT fk_payments_creator FOREIGN KEY (creator_user_id) REFERENCES users(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_payments_approver FOREIGN KEY (approver_user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_payments_creator ON payments(creator_user_id);
CREATE INDEX IF NOT EXISTS idx_payments_approver ON payments(approver_user_id);
CREATE INDEX IF NOT EXISTS idx_payments_approval_status ON payments(approval_status);

-- Extend loan table for approval tracking
ALTER TABLE loan
ADD COLUMN IF NOT EXISTS approver_user_id BIGINT,
ADD CONSTRAINT fk_loan_approver FOREIGN KEY (approver_user_id) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_loan_approver ON loan(approver_user_id);

-- ============================================================================
-- COMMIT TRANSACTION
-- ============================================================================
COMMIT;

