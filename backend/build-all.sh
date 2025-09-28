#!/bin/bash

# SmartWatts Backend Build Script
# This script builds all microservices

set -e

echo "🏗️  Building SmartWatts Backend Services..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to build a service
build_service() {
    local service_name=$1
    local service_path=$2
    
    echo -e "${YELLOW}📦 Building $service_name...${NC}"
    
    if [ -d "$service_path" ]; then
        cd "$service_path"
        
        # Check if gradlew exists
        if [ -f "gradlew" ]; then
            chmod +x gradlew
            ./gradlew clean build -x test
            echo -e "${GREEN}✅ $service_name built successfully${NC}"
        else
            echo -e "${RED}❌ gradlew not found in $service_path${NC}"
            return 1
        fi
        
        cd ..
    else
        echo -e "${RED}❌ Service directory not found: $service_path${NC}"
        return 1
    fi
}

# Function to run tests for a service
test_service() {
    local service_name=$1
    local service_path=$2
    
    echo -e "${YELLOW}🧪 Testing $service_name...${NC}"
    
    if [ -d "$service_path" ]; then
        cd "$service_path"
        
        if [ -f "gradlew" ]; then
            chmod +x gradlew
            ./gradlew test
            echo -e "${GREEN}✅ $service_name tests passed${NC}"
        else
            echo -e "${RED}❌ gradlew not found in $service_path${NC}"
            return 1
        fi
        
        cd ..
    else
        echo -e "${RED}❌ Service directory not found: $service_path${NC}"
        return 1
    fi
}

# Main build process
main() {
    echo "🚀 Starting SmartWatts Backend Build Process..."
    echo "================================================"
    
    # Array of services to build
    services=(
        "service-discovery:service-discovery"
        "api-gateway:api-gateway"
        "user-service:user-service"
        "energy-service:energy-service"
        "device-service:device-service"
        "analytics-service:analytics-service"
        "billing-service:billing-service"
        "facility-service:facility-service"
        "edge-gateway:edge-gateway"
    )
    
    local failed_services=()
    
    # Build each service
    for service in "${services[@]}"; do
        IFS=':' read -r service_name service_path <<< "$service"
        
        if build_service "$service_name" "$service_path"; then
            echo -e "${GREEN}✅ $service_name build completed${NC}"
        else
            echo -e "${RED}❌ $service_name build failed${NC}"
            failed_services+=("$service_name")
        fi
        
        echo "----------------------------------------"
    done
    
    # Report results
    echo "================================================"
    echo "🏁 Build Process Complete!"
    
    if [ ${#failed_services[@]} -eq 0 ]; then
        echo -e "${GREEN}🎉 All services built successfully!${NC}"
    else
        echo -e "${RED}❌ The following services failed to build:${NC}"
        for service in "${failed_services[@]}"; do
            echo -e "${RED}   - $service${NC}"
        done
        exit 1
    fi
}

# Function to run all tests
run_all_tests() {
    echo "🧪 Running all tests..."
    echo "================================================"
    
    services=(
        "service-discovery:service-discovery"
        "api-gateway:api-gateway"
        "user-service:user-service"
        "energy-service:energy-service"
        "device-service:device-service"
        "analytics-service:analytics-service"
        "billing-service:billing-service"
        "facility-service:facility-service"
        "edge-gateway:edge-gateway"
    )
    
    local failed_tests=()
    
    for service in "${services[@]}"; do
        IFS=':' read -r service_name service_path <<< "$service"
        
        if test_service "$service_name" "$service_path"; then
            echo -e "${GREEN}✅ $service_name tests passed${NC}"
        else
            echo -e "${RED}❌ $service_name tests failed${NC}"
            failed_tests+=("$service_name")
        fi
        
        echo "----------------------------------------"
    done
    
    echo "================================================"
    if [ ${#failed_tests[@]} -eq 0 ]; then
        echo -e "${GREEN}🎉 All tests passed!${NC}"
    else
        echo -e "${RED}❌ The following services failed tests:${NC}"
        for service in "${failed_tests[@]}"; do
            echo -e "${RED}   - $service${NC}"
        done
        exit 1
    fi
}

# Function to clean all services
clean_all() {
    echo "🧹 Cleaning all services..."
    echo "================================================"
    
    services=(
        "service-discovery:service-discovery"
        "api-gateway:api-gateway"
        "user-service:user-service"
        "energy-service:energy-service"
        "device-service:device-service"
        "analytics-service:analytics-service"
        "billing-service:billing-service"
        "facility-service:facility-service"
        "edge-gateway:edge-gateway"
    )
    
    for service in "${services[@]}"; do
        IFS=':' read -r service_name service_path <<< "$service"
        
        if [ -d "$service_path" ]; then
            cd "$service_path"
            if [ -f "gradlew" ]; then
                chmod +x gradlew
                ./gradlew clean
                echo -e "${GREEN}✅ $service_name cleaned${NC}"
            fi
            cd ..
        fi
    done
    
    echo -e "${GREEN}🎉 All services cleaned!${NC}"
}

# Parse command line arguments
case "${1:-build}" in
    "build")
        main
        ;;
    "test")
        run_all_tests
        ;;
    "clean")
        clean_all
        ;;
    "all")
        clean_all
        main
        run_all_tests
        ;;
    *)
        echo "Usage: $0 {build|test|clean|all}"
        echo "  build - Build all services (default)"
        echo "  test  - Run all tests"
        echo "  clean - Clean all services"
        echo "  all   - Clean, build, and test all services"
        exit 1
        ;;
esac 