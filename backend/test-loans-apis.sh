#!/bin/bash

# Loans API Test Script
# Tests all loan management endpoints

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Loans API Test Suite"
echo "========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

print_result() {
    local test_name="$1"
    local status_code="$2"
    local expected_code="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$status_code" == "$expected_code" ]; then
        echo -e "${GREEN}✓ PASS${NC} - $test_name (HTTP $status_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAIL${NC} - $test_name (Expected: $expected_code, Got: $status_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# Check server
echo "Checking if server is running..."
if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}"
    echo ""
else
    echo -e "${RED}✗ Server is not running${NC}"
    exit 1
fi

# Login
echo "========================================="
echo "Step 1: Login"
echo "========================================="
echo ""

ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":false}')
ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r '.token // empty' 2>/dev/null)

if [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}✓ Admin login successful${NC}"
else
    echo -e "${RED}✗ Admin login failed${NC}"
    exit 1
fi
echo ""

echo "========================================="
echo "Running Loans API Tests..."
echo "========================================="
echo ""

# Test 1: Get All Loans
echo "Test 1: Get All Loans"
echo "   GET /loans"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get all loans" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 2: Get Loan by ID
echo "Test 2: Get Loan by ID"
echo "   GET /loans/1"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get loan by ID" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 3: Get Loans by Member
echo "Test 3: Get Loans by Member"
echo "   GET /loans/member/1"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/member/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get loans by member" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 4: Get Loans by Status
echo "Test 4: Get Loans by Status"
echo "   GET /loans/status/ACTIVE"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/status/ACTIVE" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get loans by status" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 5: Get Loan Statistics
echo "Test 5: Get Loan Statistics"
echo "   GET /loans/statistics"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/statistics" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get loan statistics" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 6: Apply for Loan
echo "Test 6: Apply for Loan"
echo "   POST /loans/apply"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/loans/apply" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "amount": 50000.00,
    "loanType": "PERSONAL",
    "purpose": "Test loan application",
    "termMonths": 12,
    "guarantors": []
  }')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Apply for loan" "$STATUS" "201"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"

LOAN_ID=$(echo "$BODY" | jq -r '.id // empty' 2>/dev/null)
if [ -n "$LOAN_ID" ]; then
    echo -e "${GREEN}Created loan ID: $LOAN_ID${NC}"
fi
echo ""

# Test 7: Search Loans
echo "Test 7: Search Loans"
echo "   GET /loans/search?query=test"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/search?query=test" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Search loans" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 8: Check Eligibility
echo "Test 8: Check Loan Eligibility"
echo "   GET /loans/eligibility/1"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/eligibility/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Check loan eligibility" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 9: Approve Loan (if we have one)
if [ -n "$LOAN_ID" ]; then
    echo "Test 9: Approve Loan"
    echo "   POST /loans/$LOAN_ID/approve"
    sleep 1
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/loans/$LOAN_ID/approve" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"notes":"Approved for testing"}')
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Approve loan" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 9: Skipped (no loan ID)${NC}"
    echo ""
fi

# Test 10: Unauthorized Access
echo "Test 10: Unauthorized Access - Get All Loans"
echo "   GET /loans (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized get loans" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 11: Get Non-existent Loan
echo "Test 11: Get Non-existent Loan"
echo "   GET /loans/99999"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get non-existent loan" "$STATUS" "404"
echo "Response: $BODY"
echo ""

echo "========================================="
echo "Test Summary"
echo "========================================="
echo "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo -e "${YELLOW}Pass Rate: $PASS_RATE%${NC}"
    exit 1
fi
