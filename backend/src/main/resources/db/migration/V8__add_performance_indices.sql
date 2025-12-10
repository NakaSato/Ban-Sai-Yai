-- Add additional performance indices
-- Note: Many basic indices are already defined in Entity classes and created by Hibernate.

-- Loan indices (Composite)
CREATE INDEX IF NOT EXISTS idx_loan_member_status ON loan(member_id, status);
CREATE INDEX IF NOT EXISTS idx_loan_type_status ON loan(loan_type, status);

-- User indices (Explicit performance indices, separate from unique constraints)
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- Refresh token indices (Additional)
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON refresh_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_refresh_token_revoked ON refresh_tokens(revoked);

