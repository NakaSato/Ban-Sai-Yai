#!/bin/bash

# Payments API Test Script
# Tests all payment management endpoints

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Payments API Test Suite"
echo "========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
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
echo "Login"
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
echo "Running Payments API Tests..."
echo "========================================="
echo ""

# Test 1: Create Payment
echo "Test 1: Create Payment"
echo "   POST /api/payments"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/payments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "amount": 1000.00,
    "paymentType": "LOAN_PAYMENT",
    "description": "Test payment"
  }')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Create payment" "$STATUS" "201"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"

PAYMENT_ID=$(echo "$BODY" | jq -r '.id // empty' 2>/dev/null)
if [ -n "$PAYMENT_ID" ]; then
    echo -e "${GREEN}Created payment ID: $PAYMENT_ID${NC}"
fi
echo ""

# Test 2: Get Payment by ID
echo "Test 2: Get Payment by ID"
echo "   GET /api/payments/1"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get payment by ID" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 3: Get Payments by Member
echo "Test 3: Get Payments by Member"
echo "   GET /api/payments/member/1"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/member/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get payments by member" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 4: Get Pending Payments
echo "Test 4: Get Pending Payments"
echo "   GET /api/payments/pending"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/pending" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get pending payments" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 5: Get Overdue Payments
echo "Test 5: Get Overdue Payments"
echo "   GET /api/payments/overdue"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/overdue" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get overdue payments" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 6: Get Payment Statistics
echo "Test 6: Get Payment Statistics"
echo "   GET /api/payments/statistics"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/statistics" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get payment statistics" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 7: Search Payments
echo "Test 7: Search Payments"
echo "   GET /api/payments/search?query=test"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/search?query=test" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Search payments" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 8: Process Payment (if we have one)
if [ -n "$PAYMENT_ID" ]; then
    echo "Test 8: Process Payment"
    echo "   POST /api/payments/$PAYMENT_ID/process"
    sleep 1
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/payments/$PAYMENT_ID/process" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Process payment" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 8: Skipped (no payment ID)${NC}"
    echo ""
fi

# Test 9: Unauthorized Access
echo "Test 9: Unauthorized Access - Get Payments"
echo "   GET /api/payments/1 (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/1")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized get payment" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 10: Get Non-existent Payment
echo "Test 10: Get Non-existent Payment"
echo "   GET /api/payments/99999"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/payments/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get non-existent payment" "$STATUS" "404"
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
