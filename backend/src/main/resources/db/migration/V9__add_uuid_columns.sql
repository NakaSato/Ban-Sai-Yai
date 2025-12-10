-- V9: Add UUID columns to all tables for enhanced security
-- This migration adds UUID columns alongside existing BIGINT IDs
-- Converted to Postgres Syntax

-- ============================================
-- Member Table (Singular 'member' per V4)
-- ============================================
ALTER TABLE member ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
ALTER TABLE member ALTER COLUMN uuid SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_member_uuid ON member(uuid);

-- ============================================
-- Loan Table (Singular 'loan' per Entity/V5)
-- ============================================
ALTER TABLE loan ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
ALTER TABLE loan ALTER COLUMN uuid SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_loan_uuid ON loan(uuid);

-- ============================================
-- Payments Table (Plural 'payments' per V5)
-- ============================================
ALTER TABLE payments ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
ALTER TABLE payments ALTER COLUMN uuid SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_uuid ON payments(uuid);

-- ============================================
-- Saving Account Table
-- ============================================
ALTER TABLE saving_account ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
ALTER TABLE saving_account ALTER COLUMN uuid SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_saving_account_uuid ON saving_account(uuid);

-- ============================================
-- Guarantor Table (Plural 'guarantors' per V1)
-- ============================================
ALTER TABLE guarantors ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
ALTER TABLE guarantors ALTER COLUMN uuid SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_guarantors_uuid ON guarantors(uuid);

-- ============================================
-- Users Table (Plural 'users' per V7)
-- ============================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
ALTER TABLE users ALTER COLUMN uuid SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_uuid ON users(uuid);

-- ============================================
-- Roles Table (Plural 'roles' per V7)
-- ============================================
ALTER TABLE roles ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
ALTER TABLE roles ALTER COLUMN uuid SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_uuid ON roles(uuid);

-- ============================================
-- Cash Reconciliations Table (Plural 'cash_reconciliations' per V7)
-- ============================================
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'cash_reconciliations') THEN
        ALTER TABLE cash_reconciliations ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
        ALTER TABLE cash_reconciliations ALTER COLUMN uuid SET NOT NULL;
        CREATE UNIQUE INDEX IF NOT EXISTS idx_cash_reconciliations_uuid ON cash_reconciliations(uuid);
    END IF;
END $$;

-- ============================================
-- System Audit Log Table (Singular/named 'system_audit_log' per V7)
-- ============================================
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'system_audit_log') THEN
        ALTER TABLE system_audit_log ADD COLUMN IF NOT EXISTS uuid UUID DEFAULT gen_random_uuid();
        ALTER TABLE system_audit_log ALTER COLUMN uuid SET NOT NULL;
        CREATE UNIQUE INDEX IF NOT EXISTS idx_system_audit_log_uuid ON system_audit_log(uuid);
    END IF;
END $$;

-- ============================================
-- Performance Optimization Indexes
-- ============================================

-- Composite indexes for common queries with UUID
-- Check table names carefully

-- loan
CREATE INDEX IF NOT EXISTS idx_loan_member_uuid ON loan(member_id, uuid);
CREATE INDEX IF NOT EXISTS idx_loan_status_uuid ON loan(status, uuid);

-- payments (plural)
CREATE INDEX IF NOT EXISTS idx_payments_member_uuid ON payments(member_id, uuid);

-- saving_account
CREATE INDEX IF NOT EXISTS idx_saving_account_member_uuid ON saving_account(member_id, uuid);

-- member (singular)
CREATE INDEX IF NOT EXISTS idx_member_status_uuid ON member(is_active, uuid); -- Assuming is_active column? V4 uses is_active in idx_member_active_name
CREATE INDEX IF NOT EXISTS idx_member_created_uuid ON member(created_at, uuid);

