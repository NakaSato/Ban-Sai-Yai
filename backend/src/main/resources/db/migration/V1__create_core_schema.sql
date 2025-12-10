-- ============================================================================
-- V1: Core Schema Migration for Ban Sai Yai Cooperative Management System
-- PostgreSQL 16 Compatible
-- ============================================================================

-- ============================================================================
-- 1. Create ENUM types
-- ============================================================================

CREATE TYPE loan_type_enum AS ENUM ('PERSONAL', 'EMERGENCY', 'BUSINESS', 'EDUCATION', 'HOUSING');
CREATE TYPE loan_status_enum AS ENUM ('PENDING', 'APPROVED', 'ACTIVE', 'COMPLETED', 'REJECTED', 'DEFAULTED', 'CLOSED');
CREATE TYPE account_type_enum AS ENUM ('SAVINGS', 'FIXED_DEPOSIT', 'SHARE_CAPITAL', 'CURRENT');
CREATE TYPE payment_type_enum AS ENUM ('LOAN_REPAYMENT', 'DEPOSIT', 'WITHDRAWAL', 'FEE', 'INTEREST', 'PENALTY', 'TRANSFER');
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'VERIFIED', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED');
CREATE TYPE transaction_type_enum AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'FEE', 'INTEREST', 'DIVIDEND');
CREATE TYPE collateral_type_enum AS ENUM ('LAND_DEED', 'VEHICLE', 'SAVINGS_BOOK', 'GOVERNMENT_BOND', 'OTHER');
CREATE TYPE approval_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'DRAFT');
CREATE TYPE user_role_enum AS ENUM ('ADMIN', 'PRESIDENT', 'SECRETARY', 'OFFICER', 'MEMBER');
CREATE TYPE user_status_enum AS ENUM ('ACTIVE', 'SUSPENDED');

-- ============================================================================
-- 2. Create users table (must be first due to foreign key dependencies)
-- ============================================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    role user_role_enum NOT NULL,
    status user_status_enum NOT NULL DEFAULT 'ACTIVE',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP,
    last_login TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for users table
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- ============================================================================
-- 3. Create member table
-- ============================================================================

CREATE TABLE member (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    member_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    id_card VARCHAR(13) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    address VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    photo_path VARCHAR(500),
    registration_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    occupation VARCHAR(100),
    monthly_income DECIMAL(12, 2),
    marital_status VARCHAR(20),
    spouse_name VARCHAR(100),
    number_of_children INTEGER,
    share_capital DECIMAL(15, 2),
    user_id BIGINT UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for member table
CREATE INDEX idx_member_member_id ON member(member_id);
CREATE INDEX idx_member_id_card ON member(id_card);
CREATE INDEX idx_member_registration_date ON member(registration_date);
CREATE INDEX idx_member_uuid ON member(uuid);
CREATE INDEX idx_member_is_active ON member(is_active);

-- ============================================================================
-- 4. Create loan table
-- ============================================================================

CREATE TABLE loan (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    loan_number VARCHAR(50) NOT NULL UNIQUE,
    member_id BIGINT NOT NULL,
    loan_type loan_type_enum NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    term_months INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status loan_status_enum NOT NULL,
    purpose VARCHAR(500),
    approved_amount DECIMAL(15, 2),
    disbursement_date DATE,
    maturity_date DATE,
    outstanding_balance DECIMAL(15, 2),
    paid_principal DECIMAL(15, 2),
    paid_interest DECIMAL(15, 2),
    penalty_amount DECIMAL(15, 2),
    collateral_value DECIMAL(15, 2),
    guarantee_amount DECIMAL(15, 2),
    approval_notes VARCHAR(1000),
    rejection_reason VARCHAR(500),
    approved_by VARCHAR(100),
    approved_date DATE,
    disbursed_by VARCHAR(100),
    disbursement_reference VARCHAR(100),
    contract_document_path VARCHAR(500),
    approval_document_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_loan_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE RESTRICT
);

-- Create indexes for loan table
CREATE INDEX idx_loan_loan_number ON loan(loan_number);
CREATE INDEX idx_loan_member_id ON loan(member_id);
CREATE INDEX idx_loan_status ON loan(status);
CREATE INDEX idx_loan_start_date ON loan(start_date);
CREATE INDEX idx_loan_uuid ON loan(uuid);

-- ============================================================================
-- 5. Create collateral table (doc_ref equivalent)
-- ============================================================================

CREATE TABLE collateral (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    collateral_type collateral_type_enum NOT NULL,
    description VARCHAR(500),
    estimated_value DECIMAL(15, 2),
    document_number VARCHAR(100),
    document_path VARCHAR(500),
    verification_status VARCHAR(50),
    verified_by VARCHAR(100),
    verified_date DATE,
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_collateral_loan FOREIGN KEY (loan_id) REFERENCES loan(id) ON DELETE CASCADE
);

-- Create indexes for collateral table
CREATE INDEX idx_collateral_loan_id ON collateral(loan_id);
CREATE INDEX idx_collateral_type ON collateral(collateral_type);

-- ============================================================================
-- 6. Create guarantors table (mem_ref equivalent)
-- ============================================================================

CREATE TABLE guarantors (
    guarantor_id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    guarantee_amount DECIMAL(15, 2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_guarantor_loan FOREIGN KEY (loan_id) REFERENCES loan(id) ON DELETE CASCADE,
    CONSTRAINT fk_guarantor_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
    CONSTRAINT uk_loan_member UNIQUE (loan_id, member_id)
);

-- Create indexes for guarantors table
CREATE INDEX idx_guarantor_loan ON guarantors(loan_id);
CREATE INDEX idx_guarantor_member ON guarantors(member_id);
CREATE INDEX idx_guarantor_status ON guarantors(status);

-- ============================================================================
-- 7. Create loan_balance table (loan_forward equivalent)
-- ============================================================================

CREATE TABLE loan_balance (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL,
    balance_date DATE NOT NULL,
    opening_principal DECIMAL(15, 2),
    opening_interest DECIMAL(15, 2),
    opening_penalty DECIMAL(15, 2),
    principal_paid DECIMAL(15, 2),
    interest_paid DECIMAL(15, 2),
    penalty_paid DECIMAL(15, 2),
    closing_principal DECIMAL(15, 2),
    closing_interest DECIMAL(15, 2),
    closing_penalty DECIMAL(15, 2),
    total_paid DECIMAL(15, 2),
    outstanding_balance DECIMAL(15, 2),
    days_in_arrears INTEGER,
    payment_count INTEGER,
    average_payment DECIMAL(15, 2),
    interest_accrued DECIMAL(15, 2),
    penalty_accrued DECIMAL(15, 2),
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    forward_id BIGINT,
    forward_date DATE,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_loan_balance_loan FOREIGN KEY (loan_id) REFERENCES loan(id) ON DELETE CASCADE
);

-- Create indexes for loan_balance table
CREATE INDEX idx_loan_balance_loan_id ON loan_balance(loan_id);
CREATE INDEX idx_loan_balance_balance_date ON loan_balance(balance_date);
CREATE INDEX idx_loan_balance_forward_id ON loan_balance(forward_id);

-- ============================================================================
-- 8. Create saving_account table (saving equivalent)
-- ============================================================================

CREATE TABLE saving_account (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    member_id BIGINT NOT NULL,
    account_type account_type_enum NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    share_capital DECIMAL(15, 2) DEFAULT 0.00,
    interest_rate DECIMAL(5, 2) NOT NULL,
    minimum_balance DECIMAL(15, 2) DEFAULT 0.00,
    overdraft_limit DECIMAL(15, 2) DEFAULT 0.00,
    opening_date DATE NOT NULL,
    last_interest_date DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_frozen BOOLEAN NOT NULL DEFAULT FALSE,
    freeze_reason VARCHAR(500),
    notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_saving_account_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE RESTRICT
);

-- Create indexes for saving_account table
CREATE INDEX idx_saving_account_account_number ON saving_account(account_number);
CREATE INDEX idx_saving_account_member_id ON saving_account(member_id);
CREATE INDEX idx_saving_account_account_type ON saving_account(account_type);
CREATE INDEX idx_saving_account_is_active ON saving_account(is_active);

-- ============================================================================
-- 9. Create saving_balance table (saving_forward equivalent)
-- ============================================================================

CREATE TABLE saving_balance (
    id BIGSERIAL PRIMARY KEY,
    saving_account_id BIGINT NOT NULL,
    balance_date DATE NOT NULL,
    opening_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    closing_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_deposits DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    total_withdrawals DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    interest_earned DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    fees_charged DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(15, 2),
    average_balance DECIMAL(15, 2),
    days_below_minimum INTEGER NOT NULL DEFAULT 0,
    interest_rate DECIMAL(5, 2),
    is_month_end BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_saving_balance_account FOREIGN KEY (saving_account_id) REFERENCES saving_account(id) ON DELETE CASCADE
);

-- Create indexes for saving_balance table
CREATE INDEX idx_saving_balance_account_date ON saving_balance(saving_account_id, balance_date);
CREATE INDEX idx_saving_balance_balance_date ON saving_balance(balance_date);

-- ============================================================================
-- 10. Create saving_transaction table (pay_saving equivalent)
-- ============================================================================

CREATE TABLE saving_transaction (
    id BIGSERIAL PRIMARY KEY,
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    saving_account_id BIGINT NOT NULL,
    transaction_type transaction_type_enum NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    processed_date TIMESTAMP,
    description VARCHAR(500),
    reference_number VARCHAR(100),
    receipt_number VARCHAR(100),
    balance_before DECIMAL(15, 2),
    balance_after DECIMAL(15, 2),
    is_reversed BOOLEAN DEFAULT FALSE,
    reversal_reason VARCHAR(500),
    original_transaction_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_saving_transaction_account FOREIGN KEY (saving_account_id) REFERENCES saving_account(id) ON DELETE RESTRICT
);

-- Create indexes for saving_transaction table
CREATE INDEX idx_saving_transaction_account_id ON saving_transaction(saving_account_id);
CREATE INDEX idx_saving_transaction_transaction_date ON saving_transaction(transaction_date);
CREATE INDEX idx_saving_transaction_type ON saving_transaction(transaction_type);

-- ============================================================================
-- 11. Create payments table (pay_loan equivalent)
-- ============================================================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    member_id BIGINT NOT NULL,
    loan_id BIGINT,
    saving_account_id BIGINT,
    payment_type payment_type_enum NOT NULL,
    payment_status payment_status_enum NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    principal_amount DECIMAL(19, 2),
    interest_amount DECIMAL(19, 2),
    penalty_amount DECIMAL(19, 2),
    fee_amount DECIMAL(19, 2),
    tax_amount DECIMAL(19, 2),
    payment_date DATE,
    due_date DATE,
    processed_date TIMESTAMP,
    completed_date TIMESTAMP,
    payment_method VARCHAR(50),
    reference_number VARCHAR(100),
    transaction_id VARCHAR(100),
    bank_account VARCHAR(100),
    receipt_number VARCHAR(100),
    description VARCHAR(500),
    notes VARCHAR(1000),
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_frequency VARCHAR(50),
    recurring_end_date DATE,
    auto_debit BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by VARCHAR(100),
    verified_date TIMESTAMP,
    failed_reason VARCHAR(500),
    reversal_reason VARCHAR(500),
    original_payment_id BIGINT,
    version BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_payment_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_loan FOREIGN KEY (loan_id) REFERENCES loan(id) ON DELETE SET NULL,
    CONSTRAINT fk_payment_saving_account FOREIGN KEY (saving_account_id) REFERENCES saving_account(id) ON DELETE SET NULL
);

-- Create indexes for payments table
CREATE INDEX idx_payment_member_id ON payments(member_id);
CREATE INDEX idx_payment_loan_id ON payments(loan_id);
CREATE INDEX idx_payment_saving_account_id ON payments(saving_account_id);
CREATE INDEX idx_payment_payment_date ON payments(payment_date);
CREATE INDEX idx_payment_status ON payments(payment_status);

-- ============================================================================
-- 12. Create accounting_entry table (accounting equivalent)
-- ============================================================================

CREATE TABLE accounting_entry (
    id BIGSERIAL PRIMARY KEY,
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

-- Create indexes for accounting_entry table
CREATE INDEX idx_accounting_fiscal_period ON accounting_entry(fiscal_period);
CREATE INDEX idx_accounting_debit ON accounting_entry(debit);
CREATE INDEX idx_accounting_credit ON accounting_entry(credit);
CREATE INDEX idx_accounting_account_code ON accounting_entry(account_code);
CREATE INDEX idx_accounting_transaction_date ON accounting_entry(transaction_date);
CREATE INDEX idx_accounting_reference ON accounting_entry(reference_type, reference_id);

-- ============================================================================
-- 13. Create receipt table
-- ============================================================================

CREATE TABLE receipt (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    payment_id BIGINT,
    member_id BIGINT NOT NULL,
    receipt_date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    payment_method VARCHAR(50),
    description VARCHAR(500),
    issued_by VARCHAR(100),
    void_reason VARCHAR(500),
    is_voided BOOLEAN DEFAULT FALSE,
    voided_at TIMESTAMP,
    voided_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_receipt_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    CONSTRAINT fk_receipt_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE RESTRICT
);

-- Create indexes for receipt table
CREATE INDEX idx_receipt_receipt_number ON receipt(receipt_number);
CREATE INDEX idx_receipt_payment_id ON receipt(payment_id);
CREATE INDEX idx_receipt_member_id ON receipt(member_id);
CREATE INDEX idx_receipt_receipt_date ON receipt(receipt_date);

-- ============================================================================
-- 14. Create dividend_distributions table (dividend equivalent)
-- ============================================================================

CREATE TABLE dividend_distributions (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL UNIQUE,
    total_profit DECIMAL(19, 2) NOT NULL,
    dividend_rate DECIMAL(5, 2) NOT NULL,
    average_return_rate DECIMAL(5, 2) NOT NULL,
    total_dividend_amount DECIMAL(19, 2),
    total_average_return_amount DECIMAL(19, 2),
    status approval_status_enum NOT NULL,
    calculated_at TIMESTAMP,
    distributed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create indexes for dividend_distributions table
CREATE INDEX idx_dividend_year ON dividend_distributions(year);
CREATE INDEX idx_dividend_status ON dividend_distributions(status);

-- ============================================================================
-- 15. Create dividend_recipients table (div_plan equivalent)
-- ============================================================================

CREATE TABLE dividend_recipients (
    id BIGSERIAL PRIMARY KEY,
    dividend_distribution_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    share_amount DECIMAL(15, 2) NOT NULL,
    dividend_amount DECIMAL(15, 2) NOT NULL,
    average_return_amount DECIMAL(15, 2) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    paid_date DATE,
    payment_reference VARCHAR(100),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_dividend_recipient_distribution FOREIGN KEY (dividend_distribution_id) REFERENCES dividend_distributions(id) ON DELETE CASCADE,
    CONSTRAINT fk_dividend_recipient_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE RESTRICT
);

-- Create indexes for dividend_recipients table
CREATE INDEX idx_dividend_recipient_distribution_id ON dividend_recipients(dividend_distribution_id);
CREATE INDEX idx_dividend_recipient_member_id ON dividend_recipients(member_id);
CREATE INDEX idx_dividend_recipient_payment_status ON dividend_recipients(payment_status);

-- ============================================================================
-- 16. Create user_permissions table (for RBAC)
-- ============================================================================

CREATE TABLE user_permissions (
    user_id BIGINT NOT NULL,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id, permission),
    CONSTRAINT fk_user_permissions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index for user_permissions table
CREATE INDEX idx_user_permissions_user_id ON user_permissions(user_id);

-- ============================================================================
-- 17. Insert sample data for testing
-- ============================================================================

-- Insert sample accounting entries
INSERT INTO accounting_entry (fiscal_period, account_code, account_name, debit, credit, transaction_date, description, reference_type, created_by) VALUES
('2023-12', '1001', 'Cash', 50000.00, 0.00, '2023-12-01', 'Opening balance', 'SYSTEM', 'system'),
('2023-12', '3001', 'Share Capital', 0.00, 50000.00, '2023-12-01', 'Opening balance', 'SYSTEM', 'system'),
('2023-12', '1001', 'Cash', 10000.00, 0.00, '2023-12-05', 'Member deposit', 'SAVINGS', 'system'),
('2023-12', '2001', 'Member Savings', 0.00, 10000.00, '2023-12-05', 'Member deposit', 'SAVINGS', 'system'),
('2023-12', '1002', 'Loans Receivable', 25000.00, 0.00, '2023-12-10', 'Loan disbursement', 'LOAN', 'system'),
('2023-12', '1001', 'Cash', 0.00, 25000.00, '2023-12-10', 'Loan disbursement', 'LOAN', 'system'),
('2023-12', '1001', 'Cash', 5000.00, 0.00, '2023-12-15', 'Loan repayment', 'PAYMENT', 'system'),
('2023-12', '1002', 'Loans Receivable', 0.00, 4500.00, '2023-12-15', 'Loan repayment - principal', 'PAYMENT', 'system'),
('2023-12', '4001', 'Interest Income', 0.00, 500.00, '2023-12-15', 'Loan repayment - interest', 'PAYMENT', 'system');

-- ============================================================================
-- COMMIT TRANSACTION
-- ============================================================================

COMMIT;
