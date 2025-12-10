#!/bin/bash

# Configuration
BASE_URL="http://localhost:9097/api"
USERNAME="president"
PASSWORD="president123"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "========================================="
echo "Add New Member Demo"
echo "========================================="

# 1. Login
echo "1. Logging in as $USERNAME..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}Login failed!${NC}"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi
echo -e "${GREEN}Login successful! Token acquired.${NC}"
echo ""

# 2. Add Member
echo "2. Creating new member..."
# Generate random numbers using bash RANDOM
RANDOM_4=$(printf "%04d" $((RANDOM % 10000)))
ID_CARD="123456789${RANDOM_4}"
PHONE="089123${RANDOM_4}"

PAYLOAD=$(cat <<EOF
{
  "name": "Demo Member ${RANDOM_4}",
  "idCard": "${ID_CARD}",
  "dateOfBirth": "1990-01-15",
  "address": "999 Peace Road, Bangkok 10110",
  "phone": "${PHONE}",
  "occupation": "Software Engineer",
  "monthlyIncome": 55000.00,
  "maritalStatus": "Single",
  "isActive": true
}
EOF
)

echo "Payload:"
echo "$PAYLOAD" | jq . 2>/dev/null || echo "$PAYLOAD"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/members" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    echo -e "${GREEN}✓ Member created successfully! (HTTP $HTTP_CODE)${NC}"
    echo "Response:"
    echo "$BODY" | jq . 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}✗ Failed to create member (HTTP $HTTP_CODE)${NC}"
    echo "Response:"
    echo "$BODY"
fi
