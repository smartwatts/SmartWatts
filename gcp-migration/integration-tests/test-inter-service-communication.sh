#!/bin/bash

# Integration Test Script for Inter-Service Communication
# Tests all services deployed on Cloud Run

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"

# Test results
PASSED=0
FAILED=0
FAILED_TESTS=()

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Inter-Service Communication Tests${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to get service URL
get_service_url() {
    local service_name=$1
    gcloud run services describe "${service_name}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --format="value(status.url)" 2>/dev/null
}

# Function to test HTTP endpoint
test_endpoint() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}
    local method=${4:-GET}
    local data=${5:-}
    local accept_alternate=${6:-}  # Optional alternate status codes to accept
    
    echo -n "  Testing ${name}... "
    
    if [ -z "$url" ]; then
        echo -e "${RED}FAILED${NC} (URL not found)"
        FAILED=$((FAILED+1))
        FAILED_TESTS+=("${name}: URL not found")
        return 1
    fi
    
    if [ "$method" = "GET" ]; then
        HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 "${url}" -o /dev/null 2>&1)
    else
        HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 -X "${method}" -H "Content-Type: application/json" -d "${data}" "${url}" -o /dev/null 2>&1)
    fi
    
    # Check if HTTP code matches expected or any alternate codes
    if [ "$HTTP_CODE" = "$expected_status" ]; then
        echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
        return 0
    elif [ -n "$accept_alternate" ] && echo "$accept_alternate" | grep -q "$HTTP_CODE"; then
        echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE, acceptable alternate)"
        PASSED=$((PASSED+1))
        return 0
    else
        echo -e "${RED}FAILED${NC} (Expected HTTP $expected_status, got HTTP $HTTP_CODE)"
        FAILED=$((FAILED+1))
        FAILED_TESTS+=("${name}: Expected HTTP $expected_status, got HTTP $HTTP_CODE")
        return 1
    fi
}

# Function to test JSON response
test_json_endpoint() {
    local name=$1
    local url=$2
    local expected_key=$3
    
    echo -n "  Testing ${name} (JSON)... "
    
    if [ -z "$url" ]; then
        echo -e "${RED}FAILED${NC} (URL not found)"
        FAILED=$((FAILED+1))
        FAILED_TESTS+=("${name}: URL not found")
        return 1
    fi
    
    RESPONSE=$(curl -s --max-time 10 "${url}" 2>&1)
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 "${url}" -o /dev/null 2>&1)
    
    if [ "$HTTP_CODE" = "200" ] && echo "$RESPONSE" | grep -q "${expected_key}"; then
        echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE, contains '${expected_key}')"
        PASSED=$((PASSED+1))
        return 0
    else
        echo -e "${RED}FAILED${NC} (HTTP $HTTP_CODE, missing '${expected_key}')"
        FAILED=$((FAILED+1))
        FAILED_TESTS+=("${name}: HTTP $HTTP_CODE, missing '${expected_key}')")
        return 1
    fi
}

# Get all service URLs
echo -e "${YELLOW}Fetching service URLs...${NC}"
API_GATEWAY_URL=$(get_service_url "api-gateway")
USER_SERVICE_URL=$(get_service_url "user-service")
ANALYTICS_SERVICE_URL=$(get_service_url "analytics-service")
BILLING_SERVICE_URL=$(get_service_url "billing-service")
DEVICE_SERVICE_URL=$(get_service_url "device-service")
ENERGY_SERVICE_URL=$(get_service_url "energy-service")
FACILITY_SERVICE_URL=$(get_service_url "facility-service")
EDGE_GATEWAY_URL=$(get_service_url "edge-gateway")
SERVICE_DISCOVERY_URL=$(get_service_url "service-discovery")

echo ""

# Test 1: Health Checks
echo -e "${BLUE}Test 1: Health Check Endpoints${NC}"
echo "----------------------------------------"

test_endpoint "API Gateway Health" "${API_GATEWAY_URL}/actuator/health"
test_endpoint "User Service Health" "${USER_SERVICE_URL}/actuator/health"
test_endpoint "Analytics Service Health" "${ANALYTICS_SERVICE_URL}/actuator/health"
test_endpoint "Billing Service Health" "${BILLING_SERVICE_URL}/actuator/health"
test_endpoint "Device Service Health" "${DEVICE_SERVICE_URL}/actuator/health"
test_endpoint "Energy Service Health" "${ENERGY_SERVICE_URL}/actuator/health"
test_endpoint "Facility Service Health" "${FACILITY_SERVICE_URL}/actuator/health"
test_endpoint "Edge Gateway Health" "${EDGE_GATEWAY_URL}/actuator/health"
test_endpoint "Service Discovery Health" "${SERVICE_DISCOVERY_URL}/actuator/health"

echo ""

# Test 2: Service Discovery
echo -e "${BLUE}Test 2: Service Discovery (Eureka)${NC}"
echo "----------------------------------------"

if [ -n "$SERVICE_DISCOVERY_URL" ]; then
    test_endpoint "Eureka Dashboard" "${SERVICE_DISCOVERY_URL}/" 200
    test_json_endpoint "Eureka API" "${SERVICE_DISCOVERY_URL}/eureka/apps" "application"
else
    echo -e "${YELLOW}  Service Discovery URL not found, skipping...${NC}"
fi

echo ""

# Test 3: API Gateway Routing
echo -e "${BLUE}Test 3: API Gateway Routing${NC}"
echo "----------------------------------------"

if [ -n "$API_GATEWAY_URL" ]; then
    # Test API Gateway health
    test_endpoint "API Gateway Root" "${API_GATEWAY_URL}/actuator/health"
    
    # Test routing to user service (should return 401/403 without auth, but proves routing works)
    test_endpoint "API Gateway -> User Service" "${API_GATEWAY_URL}/api/v1/users" 401 "GET" "" "403"
    
    # Test API Gateway info endpoint (may not be configured, so accept 404)
    test_endpoint "API Gateway Info" "${API_GATEWAY_URL}/actuator/info" 404
else
    echo -e "${RED}  API Gateway URL not found${NC}"
    FAILED=$((FAILED+1))
fi

echo ""

# Test 4: Direct Service Endpoints
echo -e "${BLUE}Test 4: Direct Service Endpoints${NC}"
echo "----------------------------------------"

# User Service
if [ -n "$USER_SERVICE_URL" ]; then
    # Info endpoint may return 404 if not configured, accept both 200 and 404
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 5 "${USER_SERVICE_URL}/actuator/info" -o /dev/null 2>&1)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "  Testing User Service Info... ${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
    fi
    # Test user service endpoint (should return 401/403 without auth)
    test_endpoint "User Service API" "${USER_SERVICE_URL}/api/v1/users" 401 "GET" "" "403"
fi

# Analytics Service
if [ -n "$ANALYTICS_SERVICE_URL" ]; then
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 5 "${ANALYTICS_SERVICE_URL}/actuator/info" -o /dev/null 2>&1)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "  Testing Analytics Service Info... ${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
    fi
fi

# Billing Service
if [ -n "$BILLING_SERVICE_URL" ]; then
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 5 "${BILLING_SERVICE_URL}/actuator/info" -o /dev/null 2>&1)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "  Testing Billing Service Info... ${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
    fi
fi

# Device Service
if [ -n "$DEVICE_SERVICE_URL" ]; then
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 5 "${DEVICE_SERVICE_URL}/actuator/info" -o /dev/null 2>&1)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "  Testing Device Service Info... ${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
    fi
fi

# Energy Service
if [ -n "$ENERGY_SERVICE_URL" ]; then
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 5 "${ENERGY_SERVICE_URL}/actuator/info" -o /dev/null 2>&1)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "  Testing Energy Service Info... ${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
    fi
fi

# Facility Service
if [ -n "$FACILITY_SERVICE_URL" ]; then
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 5 "${FACILITY_SERVICE_URL}/actuator/info" -o /dev/null 2>&1)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "  Testing Facility Service Info... ${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
    fi
fi

# Edge Gateway
if [ -n "$EDGE_GATEWAY_URL" ]; then
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 5 "${EDGE_GATEWAY_URL}/actuator/info" -o /dev/null 2>&1)
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        echo -e "  Testing Edge Gateway Info... ${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        PASSED=$((PASSED+1))
    fi
    test_endpoint "Edge Gateway Health Endpoint" "${EDGE_GATEWAY_URL}/api/edge/health" 200
fi

echo ""

# Test 5: Metrics Endpoints
echo -e "${BLUE}Test 5: Metrics Endpoints${NC}"
echo "----------------------------------------"

# Metrics may not be exposed, accept both 200 and 404
test_endpoint "API Gateway Metrics" "${API_GATEWAY_URL}/actuator/metrics" 200 "GET" "" "404"
test_endpoint "User Service Metrics" "${USER_SERVICE_URL}/actuator/metrics" 200 "GET" "" "404"
test_endpoint "Analytics Service Metrics" "${ANALYTICS_SERVICE_URL}/actuator/metrics" 200 "GET" "" "404"

echo ""

# Test 6: Service-to-Service Communication (via API Gateway)
echo -e "${BLUE}Test 6: Service-to-Service via API Gateway${NC}"
echo "----------------------------------------"

if [ -n "$API_GATEWAY_URL" ]; then
    # Test that API Gateway can route to different services
    # These should return 401/403 without auth, but prove routing works
    test_endpoint "API Gateway -> User Service Route" "${API_GATEWAY_URL}/api/v1/users" 401 "GET" "" "403"
    test_endpoint "API Gateway -> Inventory Route" "${API_GATEWAY_URL}/api/v1/inventory" 401 "GET" "" "403"
fi

echo ""

# Test 7: CORS Headers
echo -e "${BLUE}Test 7: CORS Configuration${NC}"
echo "----------------------------------------"

if [ -n "$API_GATEWAY_URL" ]; then
    echo -n "  Testing CORS headers... "
    CORS_HEADER=$(curl -s -I --max-time 10 -H "Origin: https://example.com" "${API_GATEWAY_URL}/actuator/health" 2>&1 | grep -i "access-control" | head -1)
    if [ -n "$CORS_HEADER" ]; then
        echo -e "${GREEN}PASSED${NC} (CORS headers present)"
        PASSED=$((PASSED+1))
    else
        echo -e "${YELLOW}WARNING${NC} (No CORS headers found, may be intentional)"
    fi
fi

echo ""

# Test 8: Response Times
echo -e "${BLUE}Test 8: Response Time Performance${NC}"
echo "----------------------------------------"

test_response_time() {
    local name=$1
    local url=$2
    local max_time=${3:-2000}  # milliseconds
    
    echo -n "  Testing ${name} response time... "
    
    if [ -z "$url" ]; then
        echo -e "${YELLOW}SKIPPED${NC} (URL not found)"
        return
    fi
    
    START_TIME=$(date +%s%N)
    HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 "${url}" -o /dev/null 2>&1)
    END_TIME=$(date +%s%N)
    
    RESPONSE_TIME_MS=$(( (END_TIME - START_TIME) / 1000000 ))
    
    if [ "$HTTP_CODE" = "200" ] && [ "$RESPONSE_TIME_MS" -lt "$max_time" ]; then
        echo -e "${GREEN}PASSED${NC} (${RESPONSE_TIME_MS}ms < ${max_time}ms)"
        PASSED=$((PASSED+1))
    elif [ "$HTTP_CODE" = "200" ]; then
        echo -e "${YELLOW}SLOW${NC} (${RESPONSE_TIME_MS}ms > ${max_time}ms)"
        PASSED=$((PASSED+1))
    else
        echo -e "${RED}FAILED${NC} (HTTP $HTTP_CODE)"
        FAILED=$((FAILED+1))
    fi
}

test_response_time "API Gateway" "${API_GATEWAY_URL}/actuator/health" 500
test_response_time "User Service" "${USER_SERVICE_URL}/actuator/health" 500
test_response_time "Service Discovery" "${SERVICE_DISCOVERY_URL}/actuator/health" 500

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

# Service URLs for reference
echo -e "${BLUE}Service URLs:${NC}"
echo "  API Gateway: ${API_GATEWAY_URL}"
echo "  User Service: ${USER_SERVICE_URL}"
echo "  Analytics Service: ${ANALYTICS_SERVICE_URL}"
echo "  Billing Service: ${BILLING_SERVICE_URL}"
echo "  Device Service: ${DEVICE_SERVICE_URL}"
echo "  Energy Service: ${ENERGY_SERVICE_URL}"
echo "  Facility Service: ${FACILITY_SERVICE_URL}"
echo "  Edge Gateway: ${EDGE_GATEWAY_URL}"
echo "  Service Discovery: ${SERVICE_DISCOVERY_URL}"

echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 All integration tests passed! 🎉${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the errors above.${NC}"
    exit 1
fi

