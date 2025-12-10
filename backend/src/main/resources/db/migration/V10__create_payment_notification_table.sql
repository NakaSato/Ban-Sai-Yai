-- ============================================================================
-- V10: Payment Notification System Migration
-- Creates payment_notification table for mobile payment verification workflow
-- PostgreSQL Compatible
-- ============================================================================

-- ============================================================================
-- 1. Create notification_status ENUM type
-- ============================================================================
CREATE TYPE notification_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- ============================================================================
-- 2. Create payment_notification table
-- ============================================================================
CREATE TABLE payment_notification (
    noti_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    loan_id BIGINT NOT NULL,
    pay_amount DECIMAL(15, 2) NOT NULL,
    pay_date TIMESTAMP NOT NULL,
    slip_image VARCHAR(255),
    status notification_status_enum NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    approved_by_user_id BIGINT,
    rejected_by_user_id BIGINT,
    officer_comment TEXT,
    payment_id BIGINT,
    receipt_id BIGINT,
    
    -- Foreign key constraints
    CONSTRAINT fk_payment_notification_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_notification_loan FOREIGN KEY (loan_id) REFERENCES loan(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_notification_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_notification_rejected_by FOREIGN KEY (rejected_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_notification_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_notification_receipt FOREIGN KEY (receipt_id) REFERENCES receipt(id) ON DELETE SET NULL,
    
    -- Validation constraints
    CONSTRAINT chk_pay_amount_positive CHECK (pay_amount > 0),
    CONSTRAINT chk_pay_date_not_future CHECK (pay_date <= CURRENT_TIMESTAMP),
    CONSTRAINT chk_status_timestamps CHECK (
        (status = 'APPROVED' AND approved_at IS NOT NULL) OR
        (status = 'REJECTED' AND rejected_at IS NOT NULL) OR
        (status = 'PENDING' AND approved_at IS NULL AND rejected_at IS NULL)
    )
);

-- ============================================================================
-- 3. Create indexes for performance
-- ============================================================================

-- Composite index for pending notifications query (most common query)
CREATE INDEX idx_payment_notification_status_created ON payment_notification(status, created_at DESC);

-- Index for member's notification history
CREATE INDEX idx_payment_notification_member_id ON payment_notification(member_id);

-- Index for loan notifications
CREATE INDEX idx_payment_notification_loan_id ON payment_notification(loan_id);

-- Index for date range queries
CREATE INDEX idx_payment_notification_pay_date ON payment_notification(pay_date);

-- Index for approved notifications
CREATE INDEX idx_payment_notification_approved_by ON payment_notification(approved_by_user_id);

-- Index for finding notifications by payment reference
CREATE INDEX idx_payment_notification_payment_id ON payment_notification(payment_id);

-- ============================================================================
-- 4. Add new permissions for payment notification feature
-- ============================================================================

-- Insert new permissions
INSERT INTO permissions (perm_slug, module, description) VALUES
('payment.notify', 'Payment Notifications', 'Submit payment notifications with transfer slip'),
('payment.verify', 'Payment Notifications', 'View pending payment notifications'),
('payment.approve', 'Payment Notifications', 'Approve or reject payment notifications'),
('payment.history', 'Payment Notifications', 'View payment notification history');

-- ============================================================================
-- 5. Assign permissions to roles
-- ============================================================================

-- Member permissions (can submit and view own notifications)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_MEMBER'
AND p.perm_slug IN ('payment.notify', 'payment.history');

-- Officer permissions (can verify and approve)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_OFFICER'
AND p.perm_slug IN ('payment.verify', 'payment.approve', 'payment.history');

-- Secretary permissions (can verify and view)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_SECRETARY'
AND p.perm_slug IN ('payment.verify', 'payment.history');

-- President permissions (can verify and approve)
INSERT INTO role_permissions (role_id, perm_id)
SELECT r.role_id, p.perm_id
FROM roles r, permissions p
WHERE r.role_name = 'ROLE_PRESIDENT'
AND p.perm_slug IN ('payment.verify', 'payment.approve', 'payment.history');

-- ============================================================================
-- 6. Create function to update updated_at timestamp
-- ============================================================================
CREATE OR REPLACE FUNCTION update_payment_notification_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 7. Create trigger for automatic timestamp update
-- ============================================================================
CREATE TRIGGER trg_payment_notification_updated_at
    BEFORE UPDATE ON payment_notification
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_notification_timestamp();

-- ============================================================================
-- 8. Add comments for documentation
-- ============================================================================
COMMENT ON TABLE payment_notification IS 'Temporary holding table for member-submitted payment notifications pending officer verification';
COMMENT ON COLUMN payment_notification.noti_id IS 'Primary key - notification ID';
COMMENT ON COLUMN payment_notification.member_id IS 'Foreign key to member table';
COMMENT ON COLUMN payment_notification.loan_id IS 'Foreign key to loan table - which loan this payment is for';
COMMENT ON COLUMN payment_notification.pay_amount IS 'Amount transferred by member';
COMMENT ON COLUMN payment_notification.pay_date IS 'Date and time of the bank transfer';
COMMENT ON COLUMN payment_notification.slip_image IS 'File path to uploaded transfer slip image';
COMMENT ON COLUMN payment_notification.status IS 'Notification status: PENDING, APPROVED, or REJECTED';
COMMENT ON COLUMN payment_notification.officer_comment IS 'Officer comment, especially for rejections';
COMMENT ON COLUMN payment_notification.payment_id IS 'Reference to created payment record after approval';
COMMENT ON COLUMN payment_notification.receipt_id IS 'Reference to generated receipt after approval';

-- ============================================================================
-- 9. Create view for pending notifications with member/loan details
-- ============================================================================
CREATE OR REPLACE VIEW v_pending_payment_notifications AS
SELECT 
    pn.noti_id,
    pn.pay_amount,
    pn.pay_date,
    pn.slip_image,
    pn.created_at,
    pn.officer_comment,
    m.id AS member_id,
    m.member_id AS member_number,
    m.name AS member_name,
    m.phone AS member_phone,
    l.id AS loan_id,
    l.loan_number,
    l.outstanding_balance,
    l.loan_type,
    l.interest_rate,
    EXTRACT(DAY FROM (CURRENT_TIMESTAMP - pn.created_at)) AS days_pending
FROM payment_notification pn
INNER JOIN member m ON pn.member_id = m.id
INNER JOIN loan l ON pn.loan_id = l.id
WHERE pn.status = 'PENDING'
ORDER BY pn.created_at ASC;

COMMENT ON VIEW v_pending_payment_notifications IS 'View of pending payment notifications with member and loan details for officer verification screen';

-- ============================================================================
-- COMMIT TRANSACTION
-- ============================================================================
COMMIT;
