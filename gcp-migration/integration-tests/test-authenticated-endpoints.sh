#!/bin/bash

# Test Authenticated Endpoints
# This script tests endpoints that require authentication
# Note: You'll need a valid JWT token to run these tests

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"

# Get service URLs
API_GATEWAY_URL=$(gcloud run services describe "api-gateway" --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)" 2>/dev/null)
USER_SERVICE_URL=$(gcloud run services describe "user-service" --region="${REGION}" --project="${PROJECT_ID}" --format="value(status.url)" 2>/dev/null)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Authenticated Endpoint Tests${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if JWT token is provided
JWT_TOKEN="${JWT_TOKEN:-}"

if [ -z "$JWT_TOKEN" ]; then
    echo -e "${YELLOW}Note: JWT_TOKEN not provided. Testing authentication flow...${NC}"
    echo ""
    
    # Test user registration endpoint (should be public)
    echo -e "${BLUE}Test 1: User Registration Endpoint${NC}"
    echo "----------------------------------------"
    
    if [ -n "$API_GATEWAY_URL" ]; then
        echo -n "  Testing POST /api/v1/users/register... "
        HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 -X POST \
            -H "Content-Type: application/json" \
            -d '{"email":"test@example.com","password":"Test123!@#","firstName":"Test","lastName":"User"}' \
            "${API_GATEWAY_URL}/api/v1/users/register" -o /dev/null 2>&1)
        
        if [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "400" ] || [ "$HTTP_CODE" = "409" ]; then
            echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE - endpoint is accessible)"
        else
            echo -e "${YELLOW}UNEXPECTED${NC} (HTTP $HTTP_CODE)"
        fi
    fi
    
    echo ""
    echo -e "${BLUE}Test 2: User Login Endpoint${NC}"
    echo "----------------------------------------"
    
    if [ -n "$API_GATEWAY_URL" ]; then
        echo -n "  Testing POST /api/v1/users/login... "
        HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 -X POST \
            -H "Content-Type: application/json" \
            -d '{"email":"test@example.com","password":"Test123!@#"}' \
            "${API_GATEWAY_URL}/api/v1/users/login" -o /dev/null 2>&1)
        
        if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "404" ]; then
            echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE - endpoint is accessible)"
        else
            echo -e "${YELLOW}UNEXPECTED${NC} (HTTP $HTTP_CODE)"
        fi
    fi
    
    echo ""
    echo -e "${YELLOW}To test authenticated endpoints:${NC}"
    echo "  1. Register a user: POST ${API_GATEWAY_URL}/api/v1/users/register"
    echo "  2. Login: POST ${API_GATEWAY_URL}/api/v1/users/login"
    echo "  3. Extract JWT token from response"
    echo "  4. Set JWT_TOKEN environment variable and re-run this script"
    echo ""
    echo -e "${YELLOW}Example:${NC}"
    echo "  export JWT_TOKEN=\"your-jwt-token-here\""
    echo "  ./test-authenticated-endpoints.sh"
    
else
    echo -e "${GREEN}JWT Token provided. Testing authenticated endpoints...${NC}"
    echo ""
    
    # Test authenticated endpoints
    echo -e "${BLUE}Test 1: Authenticated User Endpoints${NC}"
    echo "----------------------------------------"
    
    if [ -n "$API_GATEWAY_URL" ]; then
        echo -n "  Testing GET /api/v1/users (with auth)... "
        HTTP_CODE=$(curl -s -w "%{http_code}" --max-time 10 \
            -H "Authorization: Bearer ${JWT_TOKEN}" \
            "${API_GATEWAY_URL}/api/v1/users" -o /dev/null 2>&1)
        
        if [ "$HTTP_CODE" = "200" ]; then
            echo -e "${GREEN}PASSED${NC} (HTTP $HTTP_CODE)"
        else
            echo -e "${RED}FAILED${NC} (HTTP $HTTP_CODE - token may be invalid or expired)"
        fi
    fi
    
    echo ""
    echo -e "${BLUE}Test 2: Other Authenticated Endpoints${NC}"
    echo "----------------------------------------"
    
    # Add more authenticated endpoint tests here
    echo -e "${YELLOW}  Add more endpoint tests as needed${NC}"
fi

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Complete${NC}"
echo -e "${BLUE}========================================${NC}"

