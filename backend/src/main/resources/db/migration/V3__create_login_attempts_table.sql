-- Create login_attempts table for rate limiting and brute force protection
CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    first_attempt_time TIMESTAMP,
    lockout_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_login_attempt_username UNIQUE (username)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_username ON login_attempts(username);
CREATE INDEX IF NOT EXISTS idx_first_attempt_time ON login_attempts(first_attempt_time);
CREATE INDEX IF NOT EXISTS idx_lockout_until ON login_attempts(lockout_until);

