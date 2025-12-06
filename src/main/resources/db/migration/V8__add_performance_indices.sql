-- Add additional performance indices for loan and audit operations

-- Loan indices for approval workflow and status queries
CREATE INDEX IF NOT EXISTS idx_loan_status ON loan(status);
CREATE INDEX IF NOT EXISTS idx_loan_member_status ON loan(member_id, status);
CREATE INDEX IF NOT EXISTS idx_loan_application_date ON loan(application_date);
CREATE INDEX IF NOT EXISTS idx_loan_type_status ON loan(loan_type, status);

-- Audit log indices for security monitoring
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action);

-- User indices for authentication
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role);

-- Refresh token indices for token management
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON refresh_tokens(expiry_date);
CREATE INDEX IF NOT EXISTS idx_refresh_token_revoked ON refresh_tokens(is_revoked);

-- Login attempts indices for rate limiting
CREATE INDEX IF NOT EXISTS idx_login_attempts_username ON login_attempts(username);
CREATE INDEX IF NOT EXISTS idx_login_attempts_timestamp ON login_attempts(attempt_time);

-- Cash reconciliation indices
CREATE INDEX IF NOT EXISTS idx_cash_reconciliation_status ON cash_reconciliation(status);
CREATE INDEX IF NOT EXISTS idx_cash_reconciliation_date ON cash_reconciliation(reconciliation_date);
CREATE INDEX IF NOT EXISTS idx_cash_reconciliation_created_by ON cash_reconciliation(created_by);

-- Guarantor indices for loan guarantee queries
CREATE INDEX IF NOT EXISTS idx_guarantor_loan ON guarantor(loan_id);
CREATE INDEX IF NOT EXISTS idx_guarantor_member ON guarantor(member_id);
CREATE INDEX IF NOT EXISTS idx_guarantor_active ON guarantor(is_active);

-- Collateral indices
CREATE INDEX IF NOT EXISTS idx_collateral_loan ON collateral(loan_id);
CREATE INDEX IF NOT EXISTS idx_collateral_type ON collateral(collateral_type);

