#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Pre-Launch Validation"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

FAILED_CHECKS=0
PASSED_CHECKS=0

# Function to run check
run_check() {
    local check_name="$1"
    local check_command="$2"
    
    echo -n "Checking $check_name... "
    
    if eval "$check_command" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ PASSED${NC}"
        ((PASSED_CHECKS++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC}"
        ((FAILED_CHECKS++))
        return 1
    fi
}

# Function to run check with output
run_check_with_output() {
    local check_name="$1"
    local check_command="$2"
    
    echo "Checking $check_name..."
    
    if eval "$check_command"; then
        echo -e "${GREEN}✓ $check_name PASSED${NC}"
        ((PASSED_CHECKS++))
        return 0
    else
        echo -e "${RED}✗ $check_name FAILED${NC}"
        ((FAILED_CHECKS++))
        return 1
    fi
}

echo -e "${GREEN}Step 1: Running Frontend Unit Tests...${NC}"
cd frontend
if npm run test -- --coverage --ci > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Frontend unit tests passed${NC}"
    ((PASSED_CHECKS++))
else
    echo -e "${RED}✗ Frontend unit tests failed${NC}"
    ((FAILED_CHECKS++))
fi

# Check coverage
COVERAGE=$(npm run test:coverage 2>/dev/null | grep -oP 'All files\s+\|\s+\K\d+\.\d+' | head -1 || echo "0")
if (( $(echo "$COVERAGE >= 100" | bc -l) )); then
    echo -e "${GREEN}✓ Frontend coverage: ${COVERAGE}% (target: 100%+)${NC}"
    ((PASSED_CHECKS++))
else
    echo -e "${RED}✗ Frontend coverage: ${COVERAGE}% (target: 100%+)${NC}"
    ((FAILED_CHECKS++))
fi
cd ..

echo -e "${GREEN}Step 2: Running Backend Unit Tests...${NC}"
cd backend
for service in user-service energy-service device-service analytics-service billing-service; do
    if [ -d "$service" ] && [ -f "$service/gradlew" ]; then
        echo "Testing $service..."
        cd "$service"
        if ./gradlew test > /dev/null 2>&1; then
            echo -e "${GREEN}✓ $service unit tests passed${NC}"
            ((PASSED_CHECKS++))
        else
            echo -e "${RED}✗ $service unit tests failed${NC}"
            ((FAILED_CHECKS++))
        fi
        cd ..
    fi
done
cd ..

echo -e "${GREEN}Step 3: Validating Staging Environment Health...${NC}"
if [ -f "scripts/health-check-staging.sh" ]; then
    if ./scripts/health-check-staging.sh > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Staging environment healthy${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${RED}✗ Staging environment unhealthy${NC}"
        ((FAILED_CHECKS++))
    fi
else
    echo -e "${YELLOW}⚠ Health check script not found${NC}"
fi

echo -e "${GREEN}Step 4: Checking Environment Variables...${NC}"
REQUIRED_VARS=(
    "POSTGRES_PASSWORD"
    "JWT_SECRET"
    "SENTRY_DSN"
)

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        echo -e "${YELLOW}⚠ $var not set${NC}"
    else
        echo -e "${GREEN}✓ $var is set${NC}"
        ((PASSED_CHECKS++))
    fi
done

echo -e "${GREEN}Step 5: Validating Database Migrations...${NC}"
if [ -f "scripts/migrate-staging.sh" ]; then
    echo -e "${GREEN}✓ Migration script exists${NC}"
    ((PASSED_CHECKS++))
else
    echo -e "${RED}✗ Migration script not found${NC}"
    ((FAILED_CHECKS++))
fi

echo -e "${GREEN}Step 6: Checking Sentry Configuration...${NC}"
if [ -n "$SENTRY_DSN" ]; then
    echo -e "${GREEN}✓ Sentry DSN configured${NC}"
    ((PASSED_CHECKS++))
else
    echo -e "${YELLOW}⚠ Sentry DSN not configured${NC}"
fi

# Check if Sentry is configured in backend services
if grep -r "sentry" backend/*/build.gradle > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Sentry configured in backend services${NC}"
    ((PASSED_CHECKS++))
else
    echo -e "${RED}✗ Sentry not configured in backend services${NC}"
    ((FAILED_CHECKS++))
fi

echo ""
echo "=========================================="
echo "Validation Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED_CHECKS${NC}"
echo -e "${RED}Failed: $FAILED_CHECKS${NC}"
echo ""

if [ $FAILED_CHECKS -eq 0 ]; then
    echo -e "${GREEN}All validation checks passed! Ready for production deployment.${NC}"
    exit 0
else
    echo -e "${RED}Some validation checks failed. Please resolve before production deployment.${NC}"
    exit 1
fi

