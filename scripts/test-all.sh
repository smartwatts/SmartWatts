#!/bin/bash

# SmartWatts Complete Test Suite
# This script runs all tests (frontend and backend)

set -e

echo "🧪 SmartWatts Complete Test Suite"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the project root
if [ ! -d "frontend" ] || [ ! -d "backend" ]; then
    echo "Error: Please run this script from the project root directory"
    exit 1
fi

# Parse command line arguments
RUN_FRONTEND=true
RUN_BACKEND=true
RUN_COVERAGE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --frontend-only)
            RUN_FRONTEND=true
            RUN_BACKEND=false
            ;;
        --backend-only)
            RUN_FRONTEND=false
            RUN_BACKEND=true
            ;;
        --coverage)
            RUN_COVERAGE=true
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: ./scripts/test-all.sh [--frontend-only] [--backend-only] [--coverage]"
            exit 1
            ;;
    esac
    shift
done

FAILED=0

# Run frontend tests
if [ "$RUN_FRONTEND" = true ]; then
    echo -e "${BLUE}Running Frontend Tests...${NC}"
    cd frontend
    if [ "$RUN_COVERAGE" = true ]; then
        if ./scripts/test-all.sh --coverage; then
            echo -e "${GREEN}✓ Frontend tests passed${NC}"
        else
            echo -e "${YELLOW}✗ Frontend tests failed${NC}"
            FAILED=$((FAILED + 1))
        fi
    else
        if ./scripts/test-all.sh; then
            echo -e "${GREEN}✓ Frontend tests passed${NC}"
        else
            echo -e "${YELLOW}✗ Frontend tests failed${NC}"
            FAILED=$((FAILED + 1))
        fi
    fi
    cd ..
fi

# Run backend tests
if [ "$RUN_BACKEND" = true ]; then
    echo -e "${BLUE}Running Backend Tests...${NC}"
    cd backend
    if [ "$RUN_COVERAGE" = true ]; then
        if ./scripts/test-all.sh --coverage; then
            echo -e "${GREEN}✓ Backend tests passed${NC}"
        else
            echo -e "${YELLOW}✗ Backend tests failed${NC}"
            FAILED=$((FAILED + 1))
        fi
    else
        if ./scripts/test-all.sh; then
            echo -e "${GREEN}✓ Backend tests passed${NC}"
        else
            echo -e "${YELLOW}✗ Backend tests failed${NC}"
            FAILED=$((FAILED + 1))
        fi
    fi
    cd ..
fi

# Summary
echo ""
echo "=================================="
if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${YELLOW}✗ Some tests failed${NC}"
    exit 1
fi

