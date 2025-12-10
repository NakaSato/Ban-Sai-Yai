-- Create accounting table for double-entry bookkeeping
CREATE TABLE IF NOT EXISTS accounting (
    id SERIAL PRIMARY KEY,
    fiscal_period VARCHAR(20) NOT NULL,
    account_code VARCHAR(10) NOT NULL,
    account_name VARCHAR(100),
    debit DECIMAL(15, 2) DEFAULT 0.00,
    credit DECIMAL(15, 2) DEFAULT 0.00,
    transaction_date DATE,
    description VARCHAR(500),
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indices for performance optimization
CREATE INDEX IF NOT EXISTS idx_accounting_fiscal_period ON accounting(fiscal_period);
CREATE INDEX IF NOT EXISTS idx_accounting_debit ON accounting(debit);
CREATE INDEX IF NOT EXISTS idx_accounting_credit ON accounting(credit);
CREATE INDEX IF NOT EXISTS idx_accounting_account_code ON accounting(account_code);
CREATE INDEX IF NOT EXISTS idx_accounting_transaction_date ON accounting(transaction_date);
CREATE INDEX IF NOT EXISTS idx_accounting_reference ON accounting(reference_type, reference_id);

-- Insert sample accounting entries for testing
INSERT INTO accounting (fiscal_period, account_code, account_name, debit, credit, transaction_date, description, reference_type, created_by) VALUES
('2023-12', '1001', 'Cash', 50000.00, 0.00, '2023-12-01', 'Opening balance', 'SYSTEM', 'system'),
('2023-12', '3001', 'Share Capital', 0.00, 50000.00, '2023-12-01', 'Opening balance', 'SYSTEM', 'system'),
('2023-12', '1001', 'Cash', 10000.00, 0.00, '2023-12-05', 'Member deposit', 'SAVINGS', 'system'),
('2023-12', '2001', 'Member Savings', 0.00, 10000.00, '2023-12-05', 'Member deposit', 'SAVINGS', 'system'),
('2023-12', '1002', 'Loans Receivable', 25000.00, 0.00, '2023-12-10', 'Loan disbursement', 'LOAN', 'system'),
('2023-12', '1001', 'Cash', 0.00, 25000.00, '2023-12-10', 'Loan disbursement', 'LOAN', 'system'),
('2023-12', '1001', 'Cash', 5000.00, 0.00, '2023-12-15', 'Loan repayment', 'PAYMENT', 'system'),
('2023-12', '1002', 'Loans Receivable', 0.00, 4500.00, '2023-12-15', 'Loan repayment - principal', 'PAYMENT', 'system'),
('2023-12', '4001', 'Interest Income', 0.00, 500.00, '2023-12-15', 'Loan repayment - interest', 'PAYMENT', 'system');

COMMIT;

