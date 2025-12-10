#!/bin/bash

# Audit API Test Script
# Tests all audit endpoints (requires PRESIDENT role)

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Audit API Test Suite"
echo "========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
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
echo "Step 1: Login to get PRESIDENT token"
echo "========================================="
echo ""

# First, try to login with admin (who should have PRESIDENT role or equivalent)
echo "Attempting login with admin credentials..."
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":false}')
LOGIN_STATUS=$(echo "$LOGIN_RESPONSE" | tail -n1)
LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$LOGIN_STATUS" == "200" ]; then
    echo -e "${GREEN}✓ Login successful${NC}"
    TOKEN=$(echo "$LOGIN_BODY" | jq -r '.token // empty' 2>/dev/null)
    ROLE=$(echo "$LOGIN_BODY" | jq -r '.role // empty' 2>/dev/null)
    echo "Role: $ROLE"
    echo "Token: ${TOKEN:0:50}..."
    echo ""
else
    echo -e "${RED}✗ Login failed${NC}"
    echo "Response: $LOGIN_BODY"
    exit 1
fi

# Check if user has required permissions
PERMISSIONS=$(echo "$LOGIN_BODY" | jq -r '.permissions[]' 2>/dev/null | grep -i "PRESIDENT\|ADMIN" | head -1)
if [ -z "$PERMISSIONS" ]; then
    echo -e "${YELLOW}⚠ Warning: User may not have PRESIDENT role. Tests may fail with 403.${NC}"
    echo ""
fi

echo "========================================="
echo "Running Audit API Tests..."
echo "========================================="
echo ""

# Test 1: Get Critical Actions
echo "Test 1: Get Critical Actions"
echo "   GET /api/audit/critical-actions"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/critical-actions" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get critical actions" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 2: Get Role Violations
echo "Test 2: Get Role Violations"
echo "   GET /api/audit/role-violations"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/role-violations" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get role violations" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 3: Get Activity Heatmap (without date range)
echo "Test 3: Get Activity Heatmap (no date range)"
echo "   GET /api/audit/activity-heatmap"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/activity-heatmap" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get activity heatmap (no dates)" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 4: Get Activity Heatmap (with date range)
echo "Test 4: Get Activity Heatmap (with date range)"
START_DATE="2024-01-01T00:00:00"
END_DATE="2025-12-31T23:59:59"
echo "   GET /api/audit/activity-heatmap?startDate=$START_DATE&endDate=$END_DATE"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/activity-heatmap?startDate=$START_DATE&endDate=$END_DATE" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get activity heatmap (with dates)" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 5: Get Off-Hours Alerts (without date range)
echo "Test 5: Get Off-Hours Alerts (no date range)"
echo "   GET /api/audit/off-hours-alerts"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/off-hours-alerts" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get off-hours alerts (no dates)" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 6: Get Off-Hours Alerts (with date range)
echo "Test 6: Get Off-Hours Alerts (with date range)"
echo "   GET /api/audit/off-hours-alerts?startDate=$START_DATE&endDate=$END_DATE"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/off-hours-alerts?startDate=$START_DATE&endDate=$END_DATE" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get off-hours alerts (with dates)" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 7: Get Audit Summary (without date range)
echo "Test 7: Get Audit Summary (no date range)"
echo "   GET /api/audit/summary"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/summary" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get audit summary (no dates)" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 8: Get Audit Summary (with date range)
echo "Test 8: Get Audit Summary (with date range)"
echo "   GET /api/audit/summary?startDate=$START_DATE&endDate=$END_DATE"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/summary?startDate=$START_DATE&endDate=$END_DATE" \
  -H "Authorization: Bearer $TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get audit summary (with dates)" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 9: Unauthorized Access (no token)
echo "Test 9: Unauthorized Access (no token)"
echo "   GET /api/audit/critical-actions (without authentication)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/audit/critical-actions")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized access" "$STATUS" "403"
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
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
fi
