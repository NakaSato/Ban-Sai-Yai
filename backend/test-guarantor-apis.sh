#!/bin/bash

# Guarantor API Test Script
# Tests guarantor access control and loan viewing endpoints

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Guarantor API Test Suite"
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
echo "Step 1: Login with Admin Token"
echo "========================================="
echo ""

# Login as admin
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
echo "Running Guarantor API Tests..."
echo "========================================="
echo ""

# Test 1: Get Guaranteed Loans for Member
echo "Test 1: Get Guaranteed Loans for Member"
echo "   GET /api/members/1/guaranteed-loans"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/1/guaranteed-loans" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get guaranteed loans for member 1" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 2: Get Guaranteed Loans for Another Member
echo "Test 2: Get Guaranteed Loans for Member 2"
echo "   GET /api/members/2/guaranteed-loans"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/2/guaranteed-loans" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get guaranteed loans for member 2" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 3: Get Loan by ID (with guarantor access control)
echo "Test 3: Get Loan by ID"
echo "   GET /api/loans/1"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get loan by ID" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 4: Get Non-existent Loan
echo "Test 4: Get Non-existent Loan"
echo "   GET /api/loans/99999"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get non-existent loan" "$STATUS" "404"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 5: Unauthorized Access - Get Guaranteed Loans
echo "Test 5: Unauthorized Access - Get Guaranteed Loans"
echo "   GET /api/members/1/guaranteed-loans (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/1/guaranteed-loans")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized get guaranteed loans" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 6: Unauthorized Access - Get Loan
echo "Test 6: Unauthorized Access - Get Loan by ID"
echo "   GET /api/loans/1 (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/loans/1")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized get loan" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 7: Get Guaranteed Loans for Invalid Member ID
echo "Test 7: Get Guaranteed Loans for Invalid Member"
echo "   GET /api/members/99999/guaranteed-loans"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/99999/guaranteed-loans" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
# Could be 200 with empty array or 404
if [ "$STATUS" == "200" ] || [ "$STATUS" == "404" ]; then
    echo -e "${GREEN}✓ PASS${NC} - Get guaranteed loans for invalid member (HTTP $STATUS)"
    PASSED_TESTS=$((PASSED_TESTS + 1))
else
    echo -e "${RED}✗ FAIL${NC} - Get guaranteed loans for invalid member (Expected: 200 or 404, Got: $STATUS)"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi
TOTAL_TESTS=$((TOTAL_TESTS + 1))
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
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
        echo -e "${YELLOW}⚠ Most tests passed - some endpoints may need data or implementation${NC}"
    else
        echo -e "${RED}✗ Many tests failed - check implementation and data${NC}"
    fi
    exit 1
fi
