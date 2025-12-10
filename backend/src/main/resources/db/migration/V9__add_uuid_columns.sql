-- V9: Add UUID columns to all tables for enhanced security
-- This migration adds UUID columns alongside existing BIGINT IDs
-- Dual-key strategy allows gradual migration without breaking changes

-- ============================================
-- Members Table
-- ============================================
ALTER TABLE members 
ADD COLUMN uuid BINARY(16) AFTER id;

-- Generate UUIDs for existing records
UPDATE members SET uuid = UNHEX(REPLACE(UUID(), '-', ''));

-- Make UUID non-nullable and add unique constraint
ALTER TABLE members 
MODIFY COLUMN uuid BINARY(16) NOT NULL,
ADD UNIQUE INDEX idx_members_uuid (uuid);

-- ============================================
-- Loans Table
-- ============================================
ALTER TABLE loan 
ADD COLUMN uuid BINARY(16) AFTER loan_id;

UPDATE loan SET uuid = UNHEX(REPLACE(UUID(), '-', ''));

ALTER TABLE loan 
MODIFY COLUMN uuid BINARY(16) NOT NULL,
ADD UNIQUE INDEX idx_loan_uuid (uuid);

-- ============================================
-- Payments Table
-- ============================================
ALTER TABLE payment 
ADD COLUMN uuid BINARY(16) AFTER payment_id;

UPDATE payment SET uuid = UNHEX(REPLACE(UUID(), '-', ''));

ALTER TABLE payment 
MODIFY COLUMN uuid BINARY(16) NOT NULL,
ADD UNIQUE INDEX idx_payment_uuid (uuid);

-- ============================================
-- Savings Accounts Table
-- ============================================
ALTER TABLE saving_account 
ADD COLUMN uuid BINARY(16) AFTER account_id;

UPDATE saving_account SET uuid = UNHEX(REPLACE(UUID(), '-', ''));

ALTER TABLE saving_account 
MODIFY COLUMN uuid BINARY(16) NOT NULL,
ADD UNIQUE INDEX idx_saving_account_uuid (uuid);

-- ============================================
-- Guarantors Table
-- ============================================
ALTER TABLE guarantor 
ADD COLUMN uuid BINARY(16) AFTER guarantor_id;

UPDATE guarantor SET uuid = UNHEX(REPLACE(UUID(), '-', ''));

ALTER TABLE guarantor 
MODIFY COLUMN uuid BINARY(16) NOT NULL,
ADD UNIQUE INDEX idx_guarantor_uuid (uuid);

-- ============================================
-- Users Table
-- ============================================
ALTER TABLE users 
ADD COLUMN uuid BINARY(16) AFTER id;

UPDATE users SET uuid = UNHEX(REPLACE(UUID(), '-', ''));

ALTER TABLE users 
MODIFY COLUMN uuid BINARY(16) NOT NULL,
ADD UNIQUE INDEX idx_users_uuid (uuid);

-- ============================================
-- Roles Table
-- ============================================
ALTER TABLE roles 
ADD COLUMN uuid BINARY(16) AFTER role_id;

UPDATE roles SET uuid = UNHEX(REPLACE(UUID(), '-', ''));

ALTER TABLE roles 
MODIFY COLUMN uuid BINARY(16) NOT NULL,
ADD UNIQUE INDEX idx_roles_uuid (uuid);

-- ============================================
-- Cash Reconciliation Table (if exists)
-- ============================================
-- Note: Only run if table exists
SET @table_exists = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() 
    AND table_name = 'cash_reconciliation'
);

SET @sql = IF(@table_exists > 0,
    'ALTER TABLE cash_reconciliation ADD COLUMN uuid BINARY(16) AFTER id',
    'SELECT "Table cash_reconciliation does not exist, skipping" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'UPDATE cash_reconciliation SET uuid = UNHEX(REPLACE(UUID(), "-", ""))',
    'SELECT "Skipping UUID generation" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'ALTER TABLE cash_reconciliation 
     MODIFY COLUMN uuid BINARY(16) NOT NULL,
     ADD UNIQUE INDEX idx_cash_reconciliation_uuid (uuid)',
    'SELECT "Skipping constraints" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- Audit Logs Table (if exists)
-- ============================================
SET @table_exists = (
    SELECT COUNT(*) 
    FROM information_schema.tables 
    WHERE table_schema = DATABASE() 
    AND table_name = 'audit_logs'
);

SET @sql = IF(@table_exists > 0,
    'ALTER TABLE audit_logs ADD COLUMN uuid BINARY(16) AFTER id',
    'SELECT "Table audit_logs does not exist, skipping" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'UPDATE audit_logs SET uuid = UNHEX(REPLACE(UUID(), "-", ""))',
    'SELECT "Skipping UUID generation" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(@table_exists > 0,
    'ALTER TABLE audit_logs 
     MODIFY COLUMN uuid BINARY(16) NOT NULL,
     ADD UNIQUE INDEX idx_audit_logs_uuid (uuid)',
    'SELECT "Skipping constraints" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- Performance Optimization Indexes
-- ============================================

-- Composite indexes for common queries with UUID
CREATE INDEX idx_loan_member_uuid ON loan(member_id, uuid);
CREATE INDEX idx_loan_status_uuid ON loan(status, uuid);
CREATE INDEX idx_payment_member_uuid ON payment(member_id, uuid);
CREATE INDEX idx_saving_account_member_uuid ON saving_account(member_id, uuid);

-- Covering indexes for list queries
CREATE INDEX idx_members_status_uuid ON members(status, uuid);
CREATE INDEX idx_members_created_uuid ON members(created_at DESC, uuid);

-- ============================================
-- Helper Functions (MySQL 8.0+)
-- ============================================

-- Note: These functions help convert between UUID string and binary formats
-- They are built-in in MySQL 8.0+, but defined here for reference

-- UUID_TO_BIN(uuid_string) - converts string UUID to binary
-- BIN_TO_UUID(uuid_binary) - converts binary UUID to string

-- Example usage in queries:
-- SELECT BIN_TO_UUID(uuid) as uuid_string FROM members WHERE uuid = UUID_TO_BIN('550e8400-e29b-41d4-a716-446655440000');

-- ============================================
-- Verification Queries
-- ============================================

-- Verify all tables have UUID columns
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = DATABASE()
AND column_name = 'uuid'
ORDER BY table_name;

-- Verify unique indexes exist
SELECT 
    table_name,
    index_name,
    non_unique,
    seq_in_index,
    column_name
FROM information_schema.statistics
WHERE table_schema = DATABASE()
AND index_name LIKE '%uuid%'
ORDER BY table_name, index_name, seq_in_index;
