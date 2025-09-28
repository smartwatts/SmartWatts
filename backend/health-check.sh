#!/bin/bash

# SmartWatts Health Check Script
# This script checks the health of all services

echo "🏥 SmartWatts Health Check Starting..."
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check service health
check_service() {
    local service_name=$1
    local port=$2
    local endpoint=$3
    
    echo -n "Checking $service_name on port $port... "
    
    if curl -s -f "http://localhost:$port$endpoint" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ HEALTHY${NC}"
        return 0
    else
        echo -e "${RED}❌ UNHEALTHY${NC}"
        return 1
    fi
}

# Function to check service status
check_service_status() {
    local service_name=$1
    local port=$2
    
    echo -n "Checking $service_name status... "
    
    if curl -s "http://localhost:$port/actuator/health" | grep -q '"status":"UP"'; then
        echo -e "${GREEN}✅ UP${NC}"
        return 0
    else
        echo -e "${RED}❌ DOWN${NC}"
        return 1
    fi
}

# Check all services
echo "🔍 Checking Service Discovery..."
check_service "Eureka Server" 8761 "/eureka/apps"

echo ""
echo "🔍 Checking Core Services..."
check_service_status "API Gateway" 8080
check_service_status "User Service" 8081
check_service_status "Device Service" 8083
check_service_status "Analytics Service" 8084
check_service_status "Billing Service" 8085

echo ""
echo "🔍 Checking Infrastructure..."
check_service_status "PostgreSQL" 5432
check_service_status "Redis" 6379

echo ""
echo "🔍 Checking Additional Services..."
check_service_status "API Docs Service" 8086
check_service_status "Spring Boot Admin" 8087
check_service_status "Edge Gateway" 8088
check_service_status "Facility Service" 8089
check_service_status "Feature Flag Service" 8090
check_service_status "Device Verification Service" 8091
check_service_status "Appliance Monitoring Service" 8092

echo ""
echo "🔍 Checking Frontend..."
check_service "Frontend" 3001 "/"

echo ""
echo "========================================"
echo "🏥 Health Check Complete!"

# Summary
echo ""
echo "📊 Service Summary:"
echo "==================="
echo "• Service Discovery: $(curl -s http://localhost:8761/eureka/apps | grep -c '<name>' || echo '0') services registered"
echo "• API Gateway: $(curl -s http://localhost:8080/actuator/health | jq -r '.status' 2>/dev/null || echo 'UNKNOWN')"
echo "• Database: $(docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT COUNT(*) FROM users;" 2>/dev/null | grep -o '[0-9]*' | tail -1 || echo '0') users"
echo "• Redis: $(redis-cli -h localhost -p 6379 ping 2>/dev/null || echo 'DOWN')"

