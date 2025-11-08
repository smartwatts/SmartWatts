#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Local Testing Suite"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

FAILED_TESTS=0
PASSED_TESTS=0

# Function to run tests
run_tests() {
    local test_name="$1"
    local test_command="$2"
    
    echo -e "${GREEN}Running $test_name...${NC}"
    
    if eval "$test_command"; then
        echo -e "${GREEN}✓ $test_name PASSED${NC}"
        ((PASSED_TESTS++))
        return 0
    else
        echo -e "${RED}✗ $test_name FAILED${NC}"
        ((FAILED_TESTS++))
        return 1
    fi
}

# Frontend Unit Tests
echo -e "${GREEN}Step 1: Frontend Unit Tests${NC}"
cd frontend
run_tests "Frontend Unit Tests" "npm run test -- --coverage --ci"
cd ..

# Backend Unit Tests
echo -e "${GREEN}Step 2: Backend Unit Tests${NC}"
cd backend
for service in user-service energy-service device-service analytics-service billing-service; do
    if [ -d "$service" ] && [ -f "$service/gradlew" ]; then
        echo "Testing $service..."
        cd "$service"
        run_tests "$service Unit Tests" "./gradlew test"
        cd ..
    fi
done
cd ..

# Frontend Integration Tests
echo -e "${GREEN}Step 3: Frontend Integration Tests${NC}"
cd frontend
if [ -d "__tests__/integration" ]; then
    run_tests "Frontend Integration Tests" "npm run test -- __tests__/integration"
fi
cd ..

# Backend Integration Tests
echo -e "${GREEN}Step 4: Backend Integration Tests${NC}"
cd backend/integration-tests
if [ -f "pom.xml" ]; then
    run_tests "Backend Integration Tests" "mvn test"
fi
cd ../..

# E2E Tests (if staging is running)
echo -e "${GREEN}Step 5: E2E Tests${NC}"
if curl -f -s http://localhost:3000 > /dev/null 2>&1; then
    cd frontend
    run_tests "E2E Tests" "npm run test:e2e"
    cd ..
else
    echo -e "${YELLOW}⚠ Frontend not running. Skipping E2E tests.${NC}"
    echo -e "${YELLOW}   Start frontend with: cd frontend && npm run dev${NC}"
fi

echo ""
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi

