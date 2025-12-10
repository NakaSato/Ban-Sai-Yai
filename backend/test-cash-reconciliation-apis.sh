#!/bin/bash

# Cash Reconciliation API Test Script
# Tests all cash reconciliation endpoints

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Cash Reconciliation API Test Suite"
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

# Login as OFFICER (for creating reconciliation)
echo -e "${BLUE}Logging in as OFFICER...${NC}"
OFFICER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"officer","password":"officer123","rememberMe":false}')
OFFICER_TOKEN=$(echo "$OFFICER_RESPONSE" | jq -r '.token // empty' 2>/dev/null)

if [ -n "$OFFICER_TOKEN" ]; then
    echo -e "${GREEN}✓ Officer login successful${NC}"
    echo "Token: ${OFFICER_TOKEN:0:50}..."
else
    echo -e "${YELLOW}⚠ Officer login failed, trying admin...${NC}"
    # Fallback to admin
    OFFICER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d '{"username":"admin","password":"admin123","rememberMe":false}')
    OFFICER_TOKEN=$(echo "$OFFICER_RESPONSE" | jq -r '.token // empty' 2>/dev/null)
    echo -e "${GREEN}✓ Using admin token for officer tests${NC}"
fi
echo ""

# Login as SECRETARY (for approval/rejection)
echo -e "${BLUE}Logging in as SECRETARY...${NC}"
SECRETARY_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"secretary","password":"secretary123","rememberMe":false}')
SECRETARY_TOKEN=$(echo "$SECRETARY_RESPONSE" | jq -r '.token // empty' 2>/dev/null)

if [ -n "$SECRETARY_TOKEN" ]; then
    echo -e "${GREEN}✓ Secretary login successful${NC}"
    echo "Token: ${SECRETARY_TOKEN:0:50}..."
else
    echo -e "${YELLOW}⚠ Secretary login failed, using admin token...${NC}"
    SECRETARY_TOKEN="$OFFICER_TOKEN"
fi
echo ""

echo "========================================="
echo "Running Cash Reconciliation Tests..."
echo "========================================="
echo ""

# Test 1: Create Cash Reconciliation (OFFICER role)
echo "Test 1: Create Cash Reconciliation"
echo "   POST /api/cash-reconciliation"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/cash-reconciliation" \
  -H "Authorization: Bearer $OFFICER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"physicalCount":50000.00,"notes":"End of day count - Test"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Create reconciliation" "$STATUS" "201"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"

# Extract reconciliation ID if created successfully
RECONCILIATION_ID=$(echo "$BODY" | jq -r '.reconciliation.id // empty' 2>/dev/null)
if [ -n "$RECONCILIATION_ID" ]; then
    echo -e "${GREEN}Created reconciliation ID: $RECONCILIATION_ID${NC}"
fi
echo ""

# Test 2: Get Pending Reconciliations (SECRETARY role)
echo "Test 2: Get Pending Reconciliations"
echo "   GET /api/cash-reconciliation/pending"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/cash-reconciliation/pending" \
  -H "Authorization: Bearer $SECRETARY_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get pending reconciliations" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 3: Check Can Close Day
echo "Test 3: Check Can Close Day"
echo "   GET /api/cash-reconciliation/can-close-day"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/cash-reconciliation/can-close-day" \
  -H "Authorization: Bearer $OFFICER_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Check can close day" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 4: Get Reconciliation by ID (if we have one)
if [ -n "$RECONCILIATION_ID" ]; then
    echo "Test 4: Get Reconciliation by ID"
    echo "   GET /api/cash-reconciliation/$RECONCILIATION_ID"
    sleep 1
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/cash-reconciliation/$RECONCILIATION_ID" \
      -H "Authorization: Bearer $OFFICER_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get reconciliation by ID" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 4: Skipped (no reconciliation ID)${NC}"
    echo ""
fi

# Test 5: Approve Discrepancy (SECRETARY role)
if [ -n "$RECONCILIATION_ID" ]; then
    echo "Test 5: Approve Discrepancy"
    echo "   POST /api/cash-reconciliation/$RECONCILIATION_ID/approve"
    sleep 1
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/cash-reconciliation/$RECONCILIATION_ID/approve" \
      -H "Authorization: Bearer $SECRETARY_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"action":"APPROVE","notes":"Approved after verification - Test"}')
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Approve discrepancy" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 5: Skipped (no reconciliation ID)${NC}"
    echo ""
fi

# Test 6: Create another reconciliation for rejection test
echo "Test 6: Create Reconciliation for Rejection Test"
echo "   POST /api/cash-reconciliation"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/cash-reconciliation" \
  -H "Authorization: Bearer $OFFICER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"physicalCount":45000.00,"notes":"Test reconciliation for rejection"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Create reconciliation for rejection" "$STATUS" "201"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"

RECONCILIATION_ID_2=$(echo "$BODY" | jq -r '.reconciliation.id // empty' 2>/dev/null)
if [ -n "$RECONCILIATION_ID_2" ]; then
    echo -e "${GREEN}Created reconciliation ID: $RECONCILIATION_ID_2${NC}"
fi
echo ""

# Test 7: Reject Discrepancy (SECRETARY role)
if [ -n "$RECONCILIATION_ID_2" ]; then
    echo "Test 7: Reject Discrepancy"
    echo "   POST /api/cash-reconciliation/$RECONCILIATION_ID_2/reject"
    sleep 1
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/cash-reconciliation/$RECONCILIATION_ID_2/reject" \
      -H "Authorization: Bearer $SECRETARY_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"action":"REJECT","notes":"Discrepancy too large - needs recount"}')
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Reject discrepancy" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 7: Skipped (no reconciliation ID)${NC}"
    echo ""
fi

# Test 8: Unauthorized Access (no token)
echo "Test 8: Unauthorized Access - Create Reconciliation"
echo "   POST /api/cash-reconciliation (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/cash-reconciliation" \
  -H "Content-Type: application/json" \
  -d '{"physicalCount":50000.00,"notes":"Unauthorized test"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized create" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 9: Unauthorized Access - Get Pending
echo "Test 9: Unauthorized Access - Get Pending"
echo "   GET /api/cash-reconciliation/pending (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/cash-reconciliation/pending")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized get pending" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 10: Invalid Request - Missing required fields
echo "Test 10: Invalid Request - Missing Physical Count"
echo "   POST /api/cash-reconciliation (missing physicalCount)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/cash-reconciliation" \
  -H "Authorization: Bearer $OFFICER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"notes":"Missing physical count"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Invalid request - missing field" "$STATUS" "400"
echo "Response: $BODY"
echo ""

# Test 11: Invalid ID - Get non-existent reconciliation
echo "Test 11: Get Non-existent Reconciliation"
echo "   GET /api/cash-reconciliation/99999"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/cash-reconciliation/99999" \
  -H "Authorization: Bearer $OFFICER_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get non-existent reconciliation" "$STATUS" "404"
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
    echo -e "${YELLOW}⚠ Some tests failed - this may be expected if:${NC}"
    echo "  - Officer/Secretary users don't exist (using admin fallback)"
    echo "  - Database constraints prevent certain operations"
    echo "  - Business logic validation is strict"
    exit 1
fi
