#!/bin/bash

# SmartWatts Frontend Test Execution Script
# This script runs all frontend tests (unit, E2E, visual regression, load testing)

set -e

echo "🧪 SmartWatts Frontend Test Suite"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to run tests with error handling
run_test() {
    local test_name=$1
    local test_command=$2
    
    echo -e "${BLUE}Running ${test_name}...${NC}"
    if eval "$test_command"; then
        echo -e "${GREEN}✓ ${test_name} passed${NC}"
        return 0
    else
        echo -e "${YELLOW}✗ ${test_name} failed${NC}"
        return 1
    fi
}

# Check if we're in the frontend directory
if [ ! -f "package.json" ]; then
    echo "Error: Please run this script from the frontend directory"
    exit 1
fi

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
fi

# Parse command line arguments
RUN_UNIT=true
RUN_E2E=true
RUN_VISUAL=false
RUN_LOAD=false
RUN_COVERAGE=false
WATCH=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --unit-only)
            RUN_UNIT=true
            RUN_E2E=false
            ;;
        --e2e-only)
            RUN_UNIT=false
            RUN_E2E=true
            ;;
        --visual)
            RUN_VISUAL=true
            ;;
        --load)
            RUN_LOAD=true
            ;;
        --coverage)
            RUN_COVERAGE=true
            ;;
        --watch)
            WATCH=true
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: ./scripts/test-all.sh [--unit-only] [--e2e-only] [--visual] [--load] [--coverage] [--watch]"
            exit 1
            ;;
    esac
    shift
done

FAILED_TESTS=0

# Run unit tests
if [ "$RUN_UNIT" = true ]; then
    if [ "$WATCH" = true ]; then
        run_test "Unit Tests (Watch Mode)" "npm run test:watch" || FAILED_TESTS=$((FAILED_TESTS + 1))
    elif [ "$RUN_COVERAGE" = true ]; then
        run_test "Unit Tests with Coverage" "npm run test:coverage" || FAILED_TESTS=$((FAILED_TESTS + 1))
    else
        run_test "Unit Tests" "npm run test" || FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
fi

# Run E2E tests
if [ "$RUN_E2E" = true ]; then
    # Standard E2E tests
    run_test "E2E Tests" "npm run test:e2e" || FAILED_TESTS=$((FAILED_TESTS + 1))
    
    # Visual regression tests
    if [ "$RUN_VISUAL" = true ]; then
        run_test "Visual Regression Tests" "npx playwright test e2e/visual-regression" || FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    # Load testing
    if [ "$RUN_LOAD" = true ]; then
        run_test "Load Testing" "npx playwright test e2e/load-testing" || FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    # Edge case tests
    run_test "Edge Case Tests" "npx playwright test e2e/edge-cases" || FAILED_TESTS=$((FAILED_TESTS + 1))
fi

# Summary
echo ""
echo "=================================="
if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}✗ ${FAILED_TESTS} test suite(s) failed${NC}"
    exit 1
fi

