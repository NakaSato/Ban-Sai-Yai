#!/bin/bash

# Dashboard API Test Script
# Tests all dashboard endpoints for different roles

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Dashboard API Test Suite"
echo "========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test result
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

# Wait for server to be ready
echo "Checking if server is running..."
if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Server is running${NC}"
    echo ""
else
    echo -e "${RED}✗ Server is not running. Please start the server first.${NC}"
    exit 1
fi

echo "========================================="
echo "Step 1: Login with Different Roles"
echo "========================================="
echo ""

# Login as admin (has access to most endpoints)
echo -e "${BLUE}Logging in as admin...${NC}"
ADMIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":false}')
ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r '.token // empty' 2>/dev/null)

if [ -n "$ADMIN_TOKEN" ]; then
    echo -e "${GREEN}✓ Admin login successful${NC}"
    echo "Token: ${ADMIN_TOKEN:0:50}..."
else
    echo -e "${RED}✗ Admin login failed${NC}"
    exit 1
fi
echo ""

echo "========================================="
echo "Global Dashboard Endpoints"
echo "========================================="
echo ""

# Test 1: Get Current Fiscal Period
echo "Test 1: Get Current Fiscal Period"
echo "   GET /api/dashboard/fiscal-period"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/fiscal-period")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get fiscal period" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 2: Search Members
echo "Test 2: Search Members"
echo "   GET /api/dashboard/members/search?q=admin&limit=5"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/members/search?q=admin&limit=5")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Search members" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

echo "========================================="
echo "Officer Dashboard Endpoints"
echo "========================================="
echo ""

# Test 3: Get Cash Box Tally
echo "Test 3: Get Cash Box Tally (Officer/President)"
echo "   GET /api/dashboard/officer/cash-box"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/officer/cash-box" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get cash box tally" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 4: Get Recent Transactions
echo "Test 4: Get Recent Transactions"
echo "   GET /api/dashboard/officer/recent-transactions?limit=10"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/officer/recent-transactions?limit=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get recent transactions" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 5: Get Member Financials
echo "Test 5: Get Member Financials"
echo "   GET /api/dashboard/members/1/financials"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/members/1/financials" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get member financials" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

echo "========================================="
echo "Secretary Dashboard Endpoints"
echo "========================================="
echo ""

# Test 6: Get Trial Balance
echo "Test 6: Get Trial Balance"
echo "   GET /api/dashboard/secretary/trial-balance"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/secretary/trial-balance" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get trial balance" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 7: Get Unclassified Count
echo "Test 7: Get Unclassified Transaction Count"
echo "   GET /api/dashboard/secretary/unclassified-count"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/secretary/unclassified-count" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get unclassified count" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 8: Get Financial Previews
echo "Test 8: Get Financial Previews"
echo "   GET /api/dashboard/secretary/financial-previews"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/secretary/financial-previews" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get financial previews" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

echo "========================================="
echo "President Dashboard Endpoints"
echo "========================================="
echo ""

# Test 9: Get PAR Analysis
echo "Test 9: Get PAR Analysis"
echo "   GET /api/dashboard/president/par-analysis"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/president/par-analysis" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get PAR analysis" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 10: Get PAR Details
echo "Test 10: Get PAR Details"
echo "   GET /api/dashboard/president/par-details?category=30_DAYS"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/president/par-details?category=30_DAYS" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get PAR details" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 11: Get Liquidity Ratio
echo "Test 11: Get Liquidity Ratio"
echo "   GET /api/dashboard/president/liquidity"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/president/liquidity" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get liquidity ratio" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 12: Get Membership Trends
echo "Test 12: Get Membership Trends"
echo "   GET /api/dashboard/president/membership-trends?months=12"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/president/membership-trends?months=12" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get membership trends" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

echo "========================================="
echo "Transaction Endpoints"
echo "========================================="
echo ""

# Test 13: Get Minimum Interest
echo "Test 13: Get Minimum Interest for Loan"
echo "   GET /api/dashboard/loans/1/minimum-interest"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/loans/1/minimum-interest" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get minimum interest" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 14: Process Deposit
echo "Test 14: Process Deposit"
echo "   POST /api/dashboard/transactions/deposit"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/dashboard/transactions/deposit" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"amount":1000.00,"description":"Test deposit"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Process deposit" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 15: Process Loan Payment
echo "Test 15: Process Loan Payment"
echo "   POST /api/dashboard/transactions/loan-payment"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/dashboard/transactions/loan-payment" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"loanId":1,"amount":500.00,"description":"Test loan payment"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Process loan payment" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

echo "========================================="
echo "Unauthorized Access Tests"
echo "========================================="
echo ""

# Test 16: Unauthorized Access - Cash Box
echo "Test 16: Unauthorized Access - Get Cash Box"
echo "   GET /api/dashboard/officer/cash-box (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/officer/cash-box")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized cash box access" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 17: Unauthorized Access - Trial Balance
echo "Test 17: Unauthorized Access - Get Trial Balance"
echo "   GET /api/dashboard/secretary/trial-balance (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/dashboard/secretary/trial-balance")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized trial balance access" "$STATUS" "403"
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
    if [ $PASS_RATE -ge 70 ]; then
        echo -e "${YELLOW}⚠ Most tests passed - some endpoints may need implementation${NC}"
    else
        echo -e "${RED}✗ Many tests failed - significant implementation needed${NC}"
    fi
    exit 1
fi
