#!/bin/bash

# Roles API Test Script
# This script tests all role management endpoints

BASE_URL="http://localhost:9097/api"

echo "========================================="
echo "Roles API Test Suite"
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
echo "Setting up test users..."
echo "========================================="
echo ""

# Login as admin to get token
echo "Logging in as admin..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":false}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$STATUS" == "200" ]; then
    ADMIN_TOKEN=$(echo "$BODY" | jq -r '.token // empty' 2>/dev/null)
    echo -e "${GREEN}✓ Admin login successful${NC}"
else
    echo -e "${RED}✗ Admin login failed${NC}"
    exit 1
fi
echo ""

# Login as president (if exists)
echo "Logging in as president..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"president","password":"president123","rememberMe":false}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$STATUS" == "200" ]; then
    PRESIDENT_TOKEN=$(echo "$BODY" | jq -r '.token // empty' 2>/dev/null)
    echo -e "${GREEN}✓ President login successful${NC}"
else
    echo -e "${YELLOW}⚠ President login failed (user might not exist)${NC}"
    PRESIDENT_TOKEN=""
fi
echo ""

# Login as secretary (if exists)
echo "Logging in as secretary..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"secretary","password":"secretary123","rememberMe":false}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$STATUS" == "200" ]; then
    SECRETARY_TOKEN=$(echo "$BODY" | jq -r '.token // empty' 2>/dev/null)
    echo -e "${GREEN}✓ Secretary login successful${NC}"
else
    echo -e "${YELLOW}⚠ Secretary login failed (user might not exist)${NC}"
    SECRETARY_TOKEN=""
fi
echo ""

# Login as officer (if exists)
echo "Logging in as officer..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"officer","password":"officer123","rememberMe":false}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$STATUS" == "200" ]; then
    OFFICER_TOKEN=$(echo "$BODY" | jq -r '.token // empty' 2>/dev/null)
    echo -e "${GREEN}✓ Officer login successful${NC}"
else
    echo -e "${YELLOW}⚠ Officer login failed (user might not exist)${NC}"
    OFFICER_TOKEN=""
fi
echo ""

# Login as member (if exists)
echo "Logging in as member..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"member","password":"member123","rememberMe":false}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$STATUS" == "200" ]; then
    MEMBER_TOKEN=$(echo "$BODY" | jq -r '.token // empty' 2>/dev/null)
    echo -e "${GREEN}✓ Member login successful${NC}"
else
    echo -e "${YELLOW}⚠ Member login failed (user might not exist)${NC}"
    MEMBER_TOKEN=""
fi
echo ""

echo "========================================="
echo "Running Tests..."
echo "========================================="
echo ""

# Test 1: Get All Roles (Admin)
echo "Test 1: Get All Roles (Admin)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get all roles as admin" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 2: Get All Roles (President)
if [ -n "$PRESIDENT_TOKEN" ]; then
    echo "Test 2: Get All Roles (President)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles" \
      -H "Authorization: Bearer $PRESIDENT_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get all roles as president" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 2: Skipped (no president token)${NC}"
    echo ""
fi

# Test 3: Get All Roles (Secretary)
if [ -n "$SECRETARY_TOKEN" ]; then
    echo "Test 3: Get All Roles (Secretary)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles" \
      -H "Authorization: Bearer $SECRETARY_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get all roles as secretary" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 3: Skipped (no secretary token)${NC}"
    echo ""
fi

# Test 4: Get All Roles (Officer - Should Fail)
if [ -n "$OFFICER_TOKEN" ]; then
    echo "Test 4: Get All Roles (Officer - Should Fail)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles" \
      -H "Authorization: Bearer $OFFICER_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get all roles as officer (should fail)" "$STATUS" "403"
    echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 4: Skipped (no officer token)${NC}"
    echo ""
fi

# Test 5: Get All Roles (Member - Should Fail)
if [ -n "$MEMBER_TOKEN" ]; then
    echo "Test 5: Get All Roles (Member - Should Fail)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles" \
      -H "Authorization: Bearer $MEMBER_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get all roles as member (should fail)" "$STATUS" "403"
    echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 5: Skipped (no member token)${NC}"
    echo ""
fi

# Test 6: Get All Roles (Unauthenticated - Should Fail)
echo "Test 6: Get All Roles (Unauthenticated - Should Fail)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get all roles without token (should fail)" "$STATUS" "403"
echo "Response: $BODY"
echo ""

# Test 7: Get Role Hierarchy (Admin)
echo "Test 7: Get Role Hierarchy (Admin)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/hierarchy" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get role hierarchy as admin" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 8: Get Role Hierarchy (President)
if [ -n "$PRESIDENT_TOKEN" ]; then
    echo "Test 8: Get Role Hierarchy (President)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/hierarchy" \
      -H "Authorization: Bearer $PRESIDENT_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get role hierarchy as president" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 8: Skipped (no president token)${NC}"
    echo ""
fi

# Test 9: Get Role Hierarchy (Secretary - Should Fail)
if [ -n "$SECRETARY_TOKEN" ]; then
    echo "Test 9: Get Role Hierarchy (Secretary - Should Fail)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/hierarchy" \
      -H "Authorization: Bearer $SECRETARY_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get role hierarchy as secretary (should fail)" "$STATUS" "403"
    echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 9: Skipped (no secretary token)${NC}"
    echo ""
fi

# Test 10: Get Role Permissions (OFFICER)
echo "Test 10: Get Role Permissions (OFFICER)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/OFFICER/permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get OFFICER permissions" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 11: Get Role Permissions (PRESIDENT)
echo "Test 11: Get Role Permissions (PRESIDENT)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/PRESIDENT/permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get PRESIDENT permissions" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 12: Get Role Permissions (SECRETARY)
echo "Test 12: Get Role Permissions (SECRETARY)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/SECRETARY/permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get SECRETARY permissions" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 13: Get Role Permissions (MEMBER)
echo "Test 13: Get Role Permissions (MEMBER)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/MEMBER/permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get MEMBER permissions" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 14: Get All Permissions (Admin)
echo "Test 14: Get All Permissions (Admin)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/permissions" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get all permissions as admin" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 15: Get All Permissions (President)
if [ -n "$PRESIDENT_TOKEN" ]; then
    echo "Test 15: Get All Permissions (President)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/permissions" \
      -H "Authorization: Bearer $PRESIDENT_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get all permissions as president" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 15: Skipped (no president token)${NC}"
    echo ""
fi

# Test 16: Get All Permissions (Secretary - Should Fail)
if [ -n "$SECRETARY_TOKEN" ]; then
    echo "Test 16: Get All Permissions (Secretary - Should Fail)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/permissions" \
      -H "Authorization: Bearer $SECRETARY_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get all permissions as secretary (should fail)" "$STATUS" "403"
    echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 16: Skipped (no secretary token)${NC}"
    echo ""
fi

# Test 17: Get Roles by Permission
echo "Test 17: Get Roles by Permission (VIEW_MEMBERS)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/by-permission/VIEW_MEMBERS" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get roles with VIEW_MEMBERS permission" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 18: Check Can Manage Role (PRESIDENT can manage OFFICER)
echo "Test 18: Check Can Manage Role (PRESIDENT can manage OFFICER)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/PRESIDENT/can-manage/OFFICER" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Check PRESIDENT can manage OFFICER" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 19: Check Can Manage Role (OFFICER cannot manage PRESIDENT)
echo "Test 19: Check Can Manage Role (OFFICER cannot manage PRESIDENT)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/OFFICER/can-manage/PRESIDENT" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Check OFFICER can manage PRESIDENT" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 20: Validate Role Assignment (Admin assigning OFFICER)
echo "Test 20: Validate Role Assignment (Admin assigning OFFICER)"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/roles/validate-assignment" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"targetRole":"OFFICER"}')
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Validate admin can assign OFFICER role" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 21: Validate Role Assignment (Officer assigning PRESIDENT - should be invalid)
if [ -n "$OFFICER_TOKEN" ]; then
    echo "Test 21: Validate Role Assignment (Officer assigning PRESIDENT)"
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/roles/validate-assignment" \
      -H "Authorization: Bearer $OFFICER_TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"targetRole":"PRESIDENT"}')
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Validate officer can assign PRESIDENT role" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 21: Skipped (no officer token)${NC}"
    echo ""
fi

# Test 22: Get Role Statistics (Admin)
echo "Test 22: Get Role Statistics (Admin)"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/statistics" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
STATUS=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Get role statistics as admin" "$STATUS" "200"
echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
echo ""

# Test 23: Get Role Statistics (President)
if [ -n "$PRESIDENT_TOKEN" ]; then
    echo "Test 23: Get Role Statistics (President)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/statistics" \
      -H "Authorization: Bearer $PRESIDENT_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get role statistics as president" "$STATUS" "200"
    echo "Response: $BODY" | jq . 2>/dev/null || echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 23: Skipped (no president token)${NC}"
    echo ""
fi

# Test 24: Get Role Statistics (Secretary - Should Fail)
if [ -n "$SECRETARY_TOKEN" ]; then
    echo "Test 24: Get Role Statistics (Secretary - Should Fail)"
    RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/roles/statistics" \
      -H "Authorization: Bearer $SECRETARY_TOKEN")
    STATUS=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | sed '$d')
    print_result "Get role statistics as secretary (should fail)" "$STATUS" "403"
    echo "Response: $BODY"
    echo ""
else
    echo -e "${YELLOW}Test 24: Skipped (no secretary token)${NC}"
    echo ""
fi

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
