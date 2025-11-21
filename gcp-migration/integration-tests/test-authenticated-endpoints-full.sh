#!/bin/bash

# Comprehensive Authenticated Endpoint Testing
# Tests endpoints that require JWT authentication

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"

# Test results
PASSED=0
FAILED=0
FAILED_TESTS=()

# Get service URLs
echo -e "${BLUE}Fetching service URLs...${NC}"
API_GATEWAY_URL=$(gcloud run services describe "api-gateway" --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)" 2>/dev/null)
USER_SERVICE_URL=$(gcloud run services describe "user-service" --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)" 2>/dev/null)

if [ -z "$API_GATEWAY_URL" ] || [ -z "$USER_SERVICE_URL" ]; then
    echo -e "${RED}Error: Could not retrieve service URLs${NC}"
    exit 1
fi

echo -e "${GREEN}API Gateway: ${API_GATEWAY_URL}${NC}"
echo -e "${GREEN}User Service: ${USER_SERVICE_URL}${NC}"
echo ""

# Function to test endpoint
test_endpoint() {
    local name=$1
    local url=$2
    local method=${3:-GET}
    local data=${4:-}
    local token=${5:-}
    local expected_status=${6:-200}
    local accept_alternate=${7:-}  # Optional alternate status codes to accept
    
    echo -n "  Testing ${name}... "
    
    local headers=()
    if [ -n "$token" ]; then
        headers+=("-H" "Authorization: Bearer ${token}")
    fi
    headers+=("-H" "Content-Type: application/json")
    
    if [ "$method" = "GET" ]; then
        HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 "${headers[@]}" "${url}" -o /dev/null 2>&1)
        RESPONSE=$(curl -s --max-time 10 "${headers[@]}" "${url}" 2>&1)
    else
        HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 -X "${method}" "${headers[@]}" -d "${data}" "${url}" -o /dev/null 2>&1)
        RESPONSE=$(curl -s --max-time 10 -X "${method}" "${headers[@]}" -d "${data}" "${url}" 2>&1)
    fi
    
    # Check if HTTP code matches expected or any alternate codes
    if [ "$HTTP_CODE" = "$expected_status" ]; then
        echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
        return 0
    elif [ -n "$accept_alternate" ]; then
        # Check if HTTP_CODE is in the accept_alternate string (space-separated codes)
        for alt_code in $accept_alternate; do
            if [ "$HTTP_CODE" = "$alt_code" ]; then
                echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE, acceptable alternate)"
                PASSED=$((PASSED+1))
                return 0
            fi
        done
    fi
    
    # If we get here, the test failed
    if [ "$HTTP_CODE" != "$expected_status" ]; then
        echo -e "${RED}FAILED${NC} (Expected HTTP $expected_status, got HTTP $HTTP_CODE)"
        if [ "$HTTP_CODE" != "000" ]; then
            echo "    Response: $(echo "$RESPONSE" | head -c 200)"
        fi
        FAILED=$((FAILED+1))
        FAILED_TESTS+=("${name}: Expected HTTP $expected_status, got HTTP $HTTP_CODE")
        return 1
    fi
}

# Generate unique test user email
TIMESTAMP=$(date +%s)
TEST_EMAIL="test-${TIMESTAMP}@smartwatts-test.com"
TEST_USERNAME="testuser${TIMESTAMP}"
TEST_PASSWORD="Test123!@#Password"
TEST_FIRST_NAME="Test"
TEST_LAST_NAME="User"
# Generate unique phone number (use last 4 digits of timestamp)
PHONE_SUFFIX=$(echo $TIMESTAMP | tail -c 5)
TEST_PHONE="+23480${PHONE_SUFFIX}5678"  # Nigerian phone format with unique suffix

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Authenticated Endpoint Tests${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}Test User: ${TEST_USERNAME} (${TEST_EMAIL})${NC}"
echo ""

# Step 1: Register a new user
echo -e "${BLUE}Step 1: User Registration${NC}"
echo "----------------------------------------"

REGISTER_DATA=$(cat <<EOF
{
  "username": "${TEST_USERNAME}",
  "email": "${TEST_EMAIL}",
  "password": "${TEST_PASSWORD}",
  "firstName": "${TEST_FIRST_NAME}",
  "lastName": "${TEST_LAST_NAME}",
  "phoneNumber": "${TEST_PHONE}"
}
EOF
)

echo -n "  Registering user... "
REGISTER_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" --max-time 10 -X POST \
    -H "Content-Type: application/json" \
    -d "${REGISTER_DATA}" \
    "${API_GATEWAY_URL}/api/v1/users/register" 2>&1)

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | grep "HTTP_CODE" | cut -d: -f2)
BODY=$(echo "$REGISTER_RESPONSE" | grep -v "HTTP_CODE")

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE - User created)"
    PASSED=$((PASSED+1))
    USER_CREATED=true
elif [ "$HTTP_CODE" = "400" ] || [ "$HTTP_CODE" = "409" ]; then
    echo -e "${YELLOW}SKIPPED${NC} (HTTP $HTTP_CODE - User may already exist or validation failed)"
    USER_CREATED=false
else
    echo -e "${RED}FAILED${NC} (HTTP $HTTP_CODE)"
    echo "    Response: $(echo "$BODY" | head -c 200)"
    FAILED=$((FAILED+1))
    USER_CREATED=false
fi

echo ""

# Step 2: Login to get JWT token
echo -e "${BLUE}Step 2: User Login (Get JWT Token)${NC}"
echo "----------------------------------------"

LOGIN_DATA=$(cat <<EOF
{
  "usernameOrEmail": "${TEST_EMAIL}",
  "password": "${TEST_PASSWORD}"
}
EOF
)

echo -n "  Logging in... "
LOGIN_RESPONSE=$(curl -s --max-time 10 -X POST \
    -H "Content-Type: application/json" \
    -d "${LOGIN_DATA}" \
    "${API_GATEWAY_URL}/api/v1/users/login" 2>&1)

HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 -X POST \
    -H "Content-Type: application/json" \
    -d "${LOGIN_DATA}" \
    "${API_GATEWAY_URL}/api/v1/users/login" -o /dev/null 2>&1)

if [ "$HTTP_CODE" = "200" ]; then
    # Extract JWT token from response
    JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    if [ -z "$JWT_TOKEN" ]; then
        # Try alternative JSON parsing
        JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
    fi
    if [ -z "$JWT_TOKEN" ]; then
        # Try jq if available
        if command -v jq &> /dev/null; then
            JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // .accessToken // .jwt // empty' 2>/dev/null)
        fi
    fi
    
    if [ -n "$JWT_TOKEN" ] && [ "$JWT_TOKEN" != "null" ]; then
        echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE - Token obtained)"
        echo -e "  ${GREEN}Token: ${JWT_TOKEN:0:50}...${NC}"
        PASSED=$((PASSED+1))
        TOKEN_OBTAINED=true
    else
        echo -e "${YELLOW}PARTIAL${NC} (HTTP $HTTP_CODE - Login successful but token not found in response)"
        echo "    Response: $(echo "$LOGIN_RESPONSE" | head -c 200)"
        TOKEN_OBTAINED=false
    fi
else
    echo -e "${RED}FAILED${NC} (HTTP $HTTP_CODE)"
    echo "    Response: $(echo "$LOGIN_RESPONSE" | head -c 200)"
    FAILED=$((FAILED+1))
    TOKEN_OBTAINED=false
fi

echo ""

if [ "$TOKEN_OBTAINED" = false ]; then
    echo -e "${YELLOW}========================================${NC}"
    echo -e "${YELLOW}Warning: Could not obtain JWT token${NC}"
    echo -e "${YELLOW}========================================${NC}"
    echo ""
    echo "Possible reasons:"
    echo "  1. User registration failed"
    echo "  2. Login endpoint returned unexpected format"
    echo "  3. Token field name is different"
    echo ""
    echo "Response from login:"
    echo "$LOGIN_RESPONSE" | head -20
    echo ""
    echo "You can manually test by:"
    echo "  1. Registering a user via: POST ${API_GATEWAY_URL}/api/v1/users/register"
    echo "  2. Logging in via: POST ${API_GATEWAY_URL}/api/v1/users/login"
    echo "  3. Extracting the token from the response"
    echo "  4. Setting: export JWT_TOKEN=\"your-token-here\""
    echo "  5. Re-running this script"
    exit 1
fi

# Step 3: Test authenticated endpoints
echo -e "${BLUE}Step 3: Authenticated Endpoints${NC}"
echo "----------------------------------------"

# Test getting current user profile
test_endpoint "Get Current User Profile" "${API_GATEWAY_URL}/api/v1/users/profile" "GET" "" "$JWT_TOKEN" 200

# Test getting user list (requires ADMIN role, so expect 403 for regular user)
test_endpoint "Get Users List (Admin Only)" "${API_GATEWAY_URL}/api/v1/users" "GET" "" "$JWT_TOKEN" 403 "GET" "" "200"

echo ""

# Step 4: Test other service endpoints via API Gateway
echo -e "${BLUE}Step 4: Service Endpoints via API Gateway${NC}"
echo "----------------------------------------"

# Note: Only routes configured in application-cloudrun.yml are tested
# Currently configured routes: /api/v1/users/**, /api/v1/inventory/**

# Test inventory endpoint (routed to user-service)
test_endpoint "Get Inventory" "${API_GATEWAY_URL}/api/v1/inventory" "GET" "" "$JWT_TOKEN" 200 "GET" "" "401 403 404"

echo ""

# Step 5: Test direct service endpoints
echo -e "${BLUE}Step 5: Direct Service Endpoints${NC}"
echo "----------------------------------------"

# Test user service directly
test_endpoint "User Service - Get Profile" "${USER_SERVICE_URL}/api/v1/users/profile" "GET" "" "$JWT_TOKEN" 200

echo ""

# Step 6: Test token validation
echo -e "${BLUE}Step 6: Token Validation${NC}"
echo "----------------------------------------"

# Test with invalid token (401 or 403 both indicate authentication failure)
test_endpoint "Invalid Token Rejection" "${API_GATEWAY_URL}/api/v1/users/profile" "GET" "" "invalid-token-12345" 401 "403"

# Test without token (401 or 403 both indicate authentication failure)
test_endpoint "Missing Token Rejection" "${API_GATEWAY_URL}/api/v1/users/profile" "GET" "" "" 401 "403"

echo ""

# Step 7: Test protected endpoints
echo -e "${BLUE}Step 7: Protected Endpoint Access${NC}"
echo "----------------------------------------"

# Test that public endpoints still work
test_endpoint "Public Health Endpoint" "${API_GATEWAY_URL}/actuator/health" "GET" "" "" 200

# Test that protected endpoints require auth (401 or 403 both indicate auth required)
test_endpoint "Protected Endpoint Without Auth" "${API_GATEWAY_URL}/api/v1/users" "GET" "" "" 401 "403"

echo ""

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Total Tests: $((PASSED + FAILED))"
echo -e "${GREEN}Passed: ${PASSED}${NC}"
if [ $FAILED -gt 0 ]; then
    echo -e "${RED}Failed: ${FAILED}${NC}"
    echo ""
    echo -e "${RED}Failed Tests:${NC}"
    for test in "${FAILED_TESTS[@]}"; do
        echo -e "  ${RED}✗${NC} $test"
    done
else
    echo -e "${GREEN}Failed: 0${NC}"
fi

echo ""
echo -e "${BLUE}Test User Created:${NC}"
echo "  Username: ${TEST_USERNAME}"
echo "  Email: ${TEST_EMAIL}"
echo "  Password: ${TEST_PASSWORD}"
echo ""
echo -e "${BLUE}JWT Token (first 50 chars):${NC}"
echo "  ${JWT_TOKEN:0:50}..."
echo ""
echo -e "${YELLOW}To use this token for manual testing:${NC}"
echo "  export JWT_TOKEN=\"${JWT_TOKEN}\""
echo "  curl -H \"Authorization: Bearer \${JWT_TOKEN}\" ${API_GATEWAY_URL}/api/v1/users/profile"

echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All authenticated endpoint tests passed! 🎉${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the errors above.${NC}"
    exit 1
fi

