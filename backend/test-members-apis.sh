#!/bin/bash

# Members API Test Script
# Tests all member management endpoints

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Members API Test Suite"
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
echo "Running Members API Tests..."
echo "========================================="
echo ""

# Test 1: Get All Members
echo "Test 1: Get All Members"
echo "   GET /members"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get all members" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 2: Get Member by ID
echo "Test 2: Get Member by ID"
echo "   GET /members/1"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/1" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get member by ID" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 3: Get Current Member Profile
echo "Test 3: Get Current Member Profile"
echo "   GET /members/me"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/me" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get current member profile" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 4: Create Member
echo "Test 4: Create Member"
echo "   POST /members"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "Member",
    "email": "test.member@example.com",
    "phone": "0812345678",
    "address": "123 Test Street",
    "dateOfBirth": "1990-01-01"
  }')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Create member" "$STATUS" "201"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"

MEMBER_ID=$(echo "$BODY" | jq -r '.id // empty' 2>/dev/null)
if [ -n "$MEMBER_ID" ]; then
    echo -e "${GREEN}Created member ID: $MEMBER_ID${NC}"
fi
echo ""

# Test 5: Update Member
if [ -n "$MEMBER_ID" ]; then
    echo "Test 5: Update Member"
    echo "   PUT /members/$MEMBER_ID"
    sleep 1
    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL/members/$MEMBER_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{
        "firstName": "Updated",
        "lastName": "Member",
        "email": "test.member@example.com",
        "phone": "0812345678",
        "address": "456 Updated Street",
        "dateOfBirth": "1990-01-01"
      }')
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Update member" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 5: Skipped (no member ID)${NC}"
    echo ""
fi

# Test 6: Search Members
echo "Test 6: Search Members"
echo "   GET /members/search?query=test"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/search?query=test" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Search members" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 7: Get Active Members
echo "Test 7: Get Active Members"
echo "   GET /members/active"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/active" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get active members" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 8: Get Inactive Members
echo "Test 8: Get Inactive Members"
echo "   GET /members/inactive"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/inactive" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get inactive members" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 9: Get Member Statistics
echo "Test 9: Get Member Statistics"
echo "   GET /members/statistics"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/statistics" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get member statistics" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 10: Delete Member
if [ -n "$MEMBER_ID" ]; then
    echo "Test 10: Delete Member"
    echo "   DELETE /members/$MEMBER_ID"
    sleep 1
    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/members/$MEMBER_ID" \
      -H "Authorization: Bearer $ADMIN_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Delete member" "$STATUS" "204"
    echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 10: Skipped (no member ID)${NC}"
    echo ""
fi

# Test 11: Unauthorized Access
echo "Test 11: Unauthorized Access - Get All Members"
echo "   GET /members (without authentication)"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Unauthorized get members" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 12: Get Non-existent Member
echo "Test 12: Get Non-existent Member"
echo "   GET /members/99999"
sleep 1
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/members/99999" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get non-existent member" "$STATUS" "404"
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
