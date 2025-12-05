-- Add indices on saving_transaction table for cash box calculations
CREATE INDEX IF NOT EXISTS idx_saving_transaction_date ON saving_transaction(transaction_date);
CREATE INDEX IF NOT EXISTS idx_saving_transaction_type ON saving_transaction(transaction_type);
CREATE INDEX IF NOT EXISTS idx_saving_transaction_date_type ON saving_transaction(transaction_date, transaction_type);

-- Add indices on payments table for cash box calculations
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS idx_payments_type ON payments(payment_type);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(payment_status);
CREATE INDEX IF NOT EXISTS idx_payments_date_status ON payments(payment_date, payment_status);

-- Add indices on loan table for disbursement tracking
CREATE INDEX IF NOT EXISTS idx_loan_disbursement_date ON loan(disbursement_date);
CREATE INDEX IF NOT EXISTS idx_loan_disbursement_status ON loan(disbursement_date, status);

-- Add indices for transaction feed (recent transactions query)
-- These indices help optimize the sorting and filtering for the transaction feed
CREATE INDEX IF NOT EXISTS idx_saving_transaction_created_at ON saving_transaction(created_at);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);
