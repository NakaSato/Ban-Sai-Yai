-- Test Data Setup for Payment Notification and Loan Document Testing
-- Run this after migrations to populate test data

-- ============================================================================
-- 1. Create Test Users
-- ============================================================================

-- Test Member User
INSERT INTO users (username, password, email, first_name, last_name, is_active, created_at)
VALUES 
    ('test_member1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: password123
     'member1@test.com', 'สมชาย', 'ใจดี', true, NOW()),
    ('test_member2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'member2@test.com', 'สมหญิง', 'รักดี', true, NOW())
ON CONFLICT (username) DO NOTHING;

-- Test Officer User
INSERT INTO users (username, password, email, first_name, last_name, is_active, created_at)
VALUES 
    ('test_officer1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'officer1@test.com', 'นายทดสอบ', 'เจ้าหน้าที่', true, NOW())
ON CONFLICT (username) DO NOTHING;

-- ============================================================================
-- 2. Assign Roles
-- ============================================================================

-- Get user IDs
DO $$
DECLARE
    member1_user_id BIGINT;
    member2_user_id BIGINT;
    officer1_user_id BIGINT;
    member_role_id BIGINT;
    officer_role_id BIGINT;
BEGIN
    -- Get user IDs
    SELECT id INTO member1_user_id FROM users WHERE username = 'test_member1';
    SELECT id INTO member2_user_id FROM users WHERE username = 'test_member2';
    SELECT id INTO officer1_user_id FROM users WHERE username = 'test_officer1';
    
    -- Get role IDs (assuming roles table exists)
    SELECT id INTO member_role_id FROM roles WHERE name = 'ROLE_MEMBER';
    SELECT id INTO officer_role_id FROM roles WHERE name = 'ROLE_OFFICER';
    
    -- Assign roles
    INSERT INTO user_roles (user_id, role_id)
    VALUES 
        (member1_user_id, member_role_id),
        (member2_user_id, member_role_id),
        (officer1_user_id, officer_role_id)
    ON CONFLICT DO NOTHING;
END $$;

-- ============================================================================
-- 3. Create Test Members
-- ============================================================================

INSERT INTO member (
    uuid, member_id, name, id_card, phone, email, address,
    join_date, is_active, member_type, created_at
)
VALUES 
    (gen_random_uuid(), 'BSY-TEST-0001', 'สมชาย ใจดี', '1234567890123', 
     '0812345678', 'member1@test.com', '123 ถ.ทดสอบ ต.ทดสอบ อ.เมือง จ.เชียงใหม่ 50000',
     '2024-01-01', true, 'REGULAR', NOW()),
    (gen_random_uuid(), 'BSY-TEST-0002', 'สมหญิง รักดี', '1234567890124',
     '0823456789', 'member2@test.com', '456 ถ.ทดสอบ ต.ทดสอบ อ.เมือง จ.เชียงใหม่ 50000',
     '2024-01-01', true, 'REGULAR', NOW())
ON CONFLICT (member_id) DO NOTHING;

-- ============================================================================
-- 4. Create Test Loans
-- ============================================================================

DO $$
DECLARE
    member1_id BIGINT;
    member2_id BIGINT;
BEGIN
    -- Get member IDs
    SELECT id INTO member1_id FROM member WHERE member_id = 'BSY-TEST-0001';
    SELECT id INTO member2_id FROM member WHERE member_id = 'BSY-TEST-0002';
    
    -- Create test loans
    INSERT INTO loan (
        uuid, loan_number, member_id, loan_type, loan_amount, interest_rate,
        loan_period_months, monthly_payment, outstanding_balance,
        start_date, end_date, status, is_active, created_at
    )
    VALUES 
        -- Loan for member 1
        (gen_random_uuid(), 'LN-TEST-0001', member1_id, 'GENERAL', 100000.00, 12.00,
         12, 8884.88, 95000.00, '2024-01-01', '2024-12-31', 'ACTIVE', true, NOW()),
        -- Another loan for member 1
        (gen_random_uuid(), 'LN-TEST-0002', member1_id, 'EMERGENCY', 50000.00, 10.00,
         6, 8607.92, 45000.00, '2024-06-01', '2024-11-30', 'ACTIVE', true, NOW()),
        -- Loan for member 2
        (gen_random_uuid(), 'LN-TEST-0003', member2_id, 'GENERAL', 80000.00, 12.00,
         12, 7107.90, 75000.00, '2024-02-01', '2025-01-31', 'ACTIVE', true, NOW())
    ON CONFLICT (loan_number) DO NOTHING;
END $$;

-- ============================================================================
-- 5. Create Sample Payment Notifications (for testing history)
-- ============================================================================

DO $$
DECLARE
    member1_id BIGINT;
    loan1_id BIGINT;
    officer1_id BIGINT;
BEGIN
    -- Get IDs
    SELECT id INTO member1_id FROM member WHERE member_id = 'BSY-TEST-0001';
    SELECT id INTO loan1_id FROM loan WHERE loan_number = 'LN-TEST-0001';
    SELECT id INTO officer1_id FROM users WHERE username = 'test_officer1';
    
    -- Create sample notifications
    INSERT INTO payment_notification (
        uuid, member_id, loan_id, pay_amount, pay_date,
        slip_image, status, created_at
    )
    VALUES 
        -- Approved notification
        (gen_random_uuid(), member1_id, loan1_id, 5000.00, NOW() - INTERVAL '7 days',
         'test_slip_approved.jpg', 'APPROVED', NOW() - INTERVAL '7 days'),
        -- Rejected notification
        (gen_random_uuid(), member1_id, loan1_id, 3000.00, NOW() - INTERVAL '5 days',
         'test_slip_rejected.jpg', 'REJECTED', NOW() - INTERVAL '5 days'),
        -- Pending notification
        (gen_random_uuid(), member1_id, loan1_id, 4000.00, NOW() - INTERVAL '1 day',
         'test_slip_pending.jpg', 'PENDING', NOW() - INTERVAL '1 day');
END $$;

-- ============================================================================
-- 6. Create Sample Collateral Documents
-- ============================================================================

DO $$
DECLARE
    loan1_id BIGINT;
    loan2_id BIGINT;
BEGIN
    -- Get loan IDs
    SELECT id INTO loan1_id FROM loan WHERE loan_number = 'LN-TEST-0001';
    SELECT id INTO loan2_id FROM loan WHERE loan_number = 'LN-TEST-0002';
    
    -- Create sample documents
    INSERT INTO collateral (
        uuid, loan_id, ownership_document, document_path,
        description, created_at, created_by
    )
    VALUES 
        (gen_random_uuid(), loan1_id, 'Land Title Deed', 
         '101/test_land_title.pdf', 'Chanote for 2 rai land', NOW(), 'test_officer1'),
        (gen_random_uuid(), loan1_id, 'House Registration',
         '101/test_house_reg.pdf', 'House registration document', NOW(), 'test_officer1'),
        (gen_random_uuid(), loan2_id, 'Vehicle Registration',
         '102/test_vehicle_reg.pdf', 'Car registration document', NOW(), 'test_officer1')
    ON CONFLICT DO NOTHING;
END $$;

-- ============================================================================
-- 7. Verification Queries
-- ============================================================================

-- Verify test data created
SELECT 'Test Users Created:' as info, COUNT(*) as count FROM users WHERE username LIKE 'test_%';
SELECT 'Test Members Created:' as info, COUNT(*) as count FROM member WHERE member_id LIKE 'BSY-TEST-%';
SELECT 'Test Loans Created:' as info, COUNT(*) as count FROM loan WHERE loan_number LIKE 'LN-TEST-%';
SELECT 'Test Notifications Created:' as info, COUNT(*) as count FROM payment_notification WHERE slip_image LIKE 'test_%';
SELECT 'Test Documents Created:' as info, COUNT(*) as count FROM collateral WHERE document_path LIKE '%/test_%';

-- Display test credentials
SELECT 
    'Test Credentials' as info,
    username,
    'password123' as password,
    email
FROM users 
WHERE username LIKE 'test_%'
ORDER BY username;

-- Display test member and loan info
SELECT 
    m.member_id,
    m.name,
    l.loan_number,
    l.loan_amount,
    l.outstanding_balance
FROM member m
JOIN loan l ON l.member_id = m.id
WHERE m.member_id LIKE 'BSY-TEST-%'
ORDER BY m.member_id, l.loan_number;
