#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Smoke Tests"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

FAILED_TESTS=0
PASSED_TESTS=0

# Test API Gateway
echo -n "Testing API Gateway health... "
if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED_TESTS++))
fi

# Test User Service
echo -n "Testing User Service health... "
if curl -f -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED_TESTS++))
fi

# Test Energy Service
echo -n "Testing Energy Service health... "
if curl -f -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED_TESTS++))
fi

# Test Device Service
echo -n "Testing Device Service health... "
if curl -f -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED_TESTS++))
fi

# Test Analytics Service
echo -n "Testing Analytics Service health... "
if curl -f -s http://localhost:8084/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED_TESTS++))
fi

# Test Billing Service
echo -n "Testing Billing Service health... "
if curl -f -s http://localhost:8085/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED_TESTS++))
fi

# Test Frontend
echo -n "Testing Frontend... "
if curl -f -s http://localhost:3000 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ PASSED${NC}"
    ((PASSED_TESTS++))
else
    echo -e "${RED}✗ FAILED${NC}"
    ((FAILED_TESTS++))
fi

echo ""
echo "=========================================="
echo "Smoke Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}All smoke tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some smoke tests failed.${NC}"
    exit 1
fi

