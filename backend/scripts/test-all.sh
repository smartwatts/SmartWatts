#!/bin/bash

# SmartWatts Backend Test Execution Script
# This script runs all backend tests across all services

set -e

echo "🧪 SmartWatts Backend Test Suite"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if we're in the backend directory
if [ ! -d "user-service" ]; then
    echo "Error: Please run this script from the backend directory"
    exit 1
fi

# Parse command line arguments
RUN_UNIT=true
RUN_INTEGRATION=false
RUN_COVERAGE=false
SERVICE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --unit-only)
            RUN_UNIT=true
            RUN_INTEGRATION=false
            ;;
        --integration-only)
            RUN_UNIT=false
            RUN_INTEGRATION=true
            ;;
        --coverage)
            RUN_COVERAGE=true
            ;;
        --service)
            SERVICE=$2
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: ./scripts/test-all.sh [--unit-only] [--integration-only] [--coverage] [--service SERVICE_NAME]"
            exit 1
            ;;
    esac
    shift
done

# List of all services
SERVICES=(
    "user-service"
    "device-service"
    "energy-service"
    "analytics-service"
    "billing-service"
    "facility-service"
    "feature-flag-service"
    "appliance-monitoring-service"
    "edge-gateway"
    "api-gateway"
    "api-docs-service"
    "device-verification-service"
)

# Function to run tests for a service
run_service_tests() {
    local service=$1
    local service_dir="$service"
    
    if [ ! -d "$service_dir" ]; then
        echo -e "${YELLOW}⚠ Service $service not found, skipping...${NC}"
        return 0
    fi
    
    echo -e "${BLUE}Testing $service...${NC}"
    cd "$service_dir"
    
    # Check if it's a Gradle project
    if [ -f "build.gradle" ]; then
        if [ "$RUN_COVERAGE" = true ]; then
            if ./gradlew test jacocoTestReport; then
                echo -e "${GREEN}✓ $service tests passed with coverage${NC}"
                cd ..
                return 0
            else
                echo -e "${RED}✗ $service tests failed${NC}"
                cd ..
                return 1
            fi
        else
            if ./gradlew test; then
                echo -e "${GREEN}✓ $service tests passed${NC}"
                cd ..
                return 0
            else
                echo -e "${RED}✗ $service tests failed${NC}"
                cd ..
                return 1
            fi
        fi
    else
        echo -e "${YELLOW}⚠ $service is not a Gradle project, skipping...${NC}"
        cd ..
        return 0
    fi
}

FAILED_SERVICES=0

# Run tests for specific service or all services
if [ -n "$SERVICE" ]; then
    run_service_tests "$SERVICE" || FAILED_SERVICES=$((FAILED_SERVICES + 1))
else
    for service in "${SERVICES[@]}"; do
        run_service_tests "$service" || FAILED_SERVICES=$((FAILED_SERVICES + 1))
    done
fi

# Summary
echo ""
echo "=================================="
if [ $FAILED_SERVICES -eq 0 ]; then
    echo -e "${GREEN}✓ All service tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ ${FAILED_SERVICES} service(s) failed${NC}"
    exit 1
fi

