#!/bin/bash

# Authentication API Test Script
# This script tests all authentication endpoints

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Authentication API Test Suite"
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
MAX_RETRIES=5
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Server is running${NC}"
        echo ""
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
        echo "Server not ready, waiting... ($RETRY_COUNT/$MAX_RETRIES)"
        sleep 2
    else
        echo -e "${RED}✗ Server is not running. Please start the server first.${NC}"
        echo "Run: mvn spring-boot:run"
        exit 1
    fi
done

echo "========================================="
echo "Running Tests..."
echo "========================================="
echo ""

# Test 1: Check Username Availability (Available)
echo "Test 1: Check Username Availability (Available)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/auth/check-username/nonexistentuser123")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Check username availability" "$STATUS" "200"
echo "Response: $BODY"
echo ""

# Test 2: Check Username Availability (Taken)
echo "Test 2: Check Username Availability (Taken - admin)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/auth/check-username/admin")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Check existing username" "$STATUS" "200"
echo "Response: $BODY"
echo ""

# Test 3: Check Email Availability
echo "Test 3: Check Email Availability"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/auth/check-email/test@example.com")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Check email availability" "$STATUS" "200"
echo "Response: $BODY"
echo ""

# Test 4: Login with Invalid Credentials
echo "Test 4: Login with Invalid Credentials"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"invaliduser","password":"wrongpass","rememberMe":false}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Login with invalid credentials" "$STATUS" "401"
echo "Response: $BODY"
echo ""

# Test 5: Login with Valid Credentials
echo "Test 5: Login with Valid Credentials (admin/admin123)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":false}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Login with valid credentials" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"

# Extract token
TOKEN=$(echo "$BODY" | jq -r '.token // empty' 2>/dev/null)
if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}Token extracted successfully${NC}"
else
    echo -e "${RED}Failed to extract token${NC}"
fi
echo ""

# Test 6: Get Current User (Authenticated)
if [ -n "$TOKEN" ]; then
    echo "Test 6: Get Current User (Authenticated)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/auth/me" \
      -H "Authorization: Bearer $TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get current user" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 6: Skipped (no token)${NC}"
    echo ""
fi

# Test 7: Get Current User (Unauthenticated)
echo "Test 7: Get Current User (Unauthenticated)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/auth/me")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get current user without token" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 8: Login with rememberMe=true to get refresh token
echo "Test 8: Login with rememberMe=true"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":true}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Login with rememberMe" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"

# Extract refresh token
REFRESH_TOKEN=$(echo "$BODY" | jq -r '.refreshToken // empty' 2>/dev/null)
if [ -n "$REFRESH_TOKEN" ]; then
    echo -e "${GREEN}Refresh token extracted successfully${NC}"
else
    echo -e "${YELLOW}No refresh token (might be null if rememberMe=false)${NC}"
fi
echo ""

# Test 9: Refresh Token
if [ -n "$REFRESH_TOKEN" ] && [ "$REFRESH_TOKEN" != "null" ]; then
    echo "Test 9: Refresh Access Token"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/refresh" \
      -H "Content-Type: application/json" \
      -d "{\"token\":\"$REFRESH_TOKEN\"}")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Refresh token" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 9: Skipped (no refresh token)${NC}"
    echo ""
fi

# Test 10: Refresh Token with Invalid Token
echo "Test 10: Refresh Token with Invalid Token"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/refresh" \
  -H "Content-Type: application/json" \
  -d '{"token":"invalid-token-12345"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Refresh with invalid token" "$STATUS" "401"
echo "Response: $BODY"
echo ""

# Test 11: Logout
if [ -n "$TOKEN" ]; then
    echo "Test 11: Logout"
    echo "Waiting 2 seconds to avoid rate limiting..."
    sleep 2
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/logout" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Logout" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 11: Skipped (no token)${NC}"
    echo ""
fi

# Test 12: Logout without Authentication
echo "Test 12: Logout without Authentication"
echo "Waiting 2 seconds to avoid rate limiting..."
sleep 2
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/logout" \
  -H "Content-Type: application/json")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Logout without token" "$STATUS" "403"
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
