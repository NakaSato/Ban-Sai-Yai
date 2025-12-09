-- Create login_attempts table for rate limiting and brute force protection
CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    first_attempt_time DATETIME,
    lockout_until DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uq_login_attempt_username UNIQUE (username)
);

-- Create indexes for performance
CREATE INDEX idx_username ON login_attempts(username);
CREATE INDEX idx_first_attempt_time ON login_attempts(first_attempt_time);
CREATE INDEX idx_lockout_until ON login_attempts(lockout_until);
