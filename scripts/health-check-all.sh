#!/bin/bash

# SmartWatts Comprehensive Health Check Script
# This script verifies all services are running and healthy

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL=${1:-http://localhost:8080}
TIMEOUT=${2:-30}
HEALTH_CHECK_FAILED=0

echo -e "${BLUE}🏥 SmartWatts Health Check${NC}"
echo "=============================================="
echo "Base URL: $BASE_URL"
echo "Timeout: ${TIMEOUT}s"
echo ""

# Function to check HTTP endpoint
check_endpoint() {
    local name="$1"
    local url="$2"
    local expected_status="${3:-200}"
    
    echo -n "Checking $name... "
    
    if curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "$url" | grep -q "$expected_status"; then
        echo -e "${GREEN}✅ PASS${NC}"
        return 0
    else
        echo -e "${RED}❌ FAIL${NC}"
        HEALTH_CHECK_FAILED=1
        return 1
    fi
}

# Function to check Docker container
check_container() {
    local name="$1"
    local container_name="$2"
    
    echo -n "Checking $name container... "
    
    if docker ps --format "table {{.Names}}" | grep -q "^${container_name}$"; then
        if docker ps --format "table {{.Status}}" --filter "name=${container_name}" | grep -q "Up"; then
            echo -e "${GREEN}✅ PASS${NC}"
            return 0
        else
            echo -e "${RED}❌ FAIL (Container not running)${NC}"
            HEALTH_CHECK_FAILED=1
            return 1
        fi
    else
        echo -e "${RED}❌ FAIL (Container not found)${NC}"
        HEALTH_CHECK_FAILED=1
        return 1
    fi
}

# Function to check database connection
check_database() {
    local name="$1"
    local host="$2"
    local port="$3"
    local database="$4"
    
    echo -n "Checking $name database... "
    
    if command -v pg_isready >/dev/null 2>&1; then
        if pg_isready -h "$host" -p "$port" -d "$database" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ PASS${NC}"
            return 0
        else
            echo -e "${RED}❌ FAIL (Database not accessible)${NC}"
            HEALTH_CHECK_FAILED=1
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  SKIP (pg_isready not available)${NC}"
        return 0
    fi
}

# Function to check Redis connection
check_redis() {
    local name="$1"
    local host="$2"
    local port="$3"
    
    echo -n "Checking $name Redis... "
    
    if command -v redis-cli >/dev/null 2>&1; then
        if redis-cli -h "$host" -p "$port" ping >/dev/null 2>&1; then
            echo -e "${GREEN}✅ PASS${NC}"
            return 0
        else
            echo -e "${RED}❌ FAIL (Redis not accessible)${NC}"
            HEALTH_CHECK_FAILED=1
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  SKIP (redis-cli not available)${NC}"
        return 0
    fi
}

echo -e "${BLUE}🐳 Docker Container Health Checks${NC}"
echo "----------------------------------------"

# Check all Docker containers
check_container "PostgreSQL" "smartwatts-postgres"
check_container "Redis" "smartwatts-redis"
check_container "Eureka Service Discovery" "smartwatts-service-discovery"
check_container "API Gateway" "smartwatts-api-gateway"
check_container "User Service" "smartwatts-user-service"
check_container "Energy Service" "smartwatts-energy-service"
check_container "Device Service" "smartwatts-device-service"
check_container "Analytics Service" "smartwatts-analytics-service"
check_container "Billing Service" "smartwatts-billing-service"
check_container "Facility Service" "smartwatts-facility-service"
check_container "Feature Flag Service" "smartwatts-feature-flag-service"
check_container "Device Verification Service" "smartwatts-device-verification-service"
check_container "Appliance Monitoring Service" "smartwatts-appliance-monitoring-service"

echo ""

echo -e "${BLUE}🗄️  Database Health Checks${NC}"
echo "----------------------------------------"

# Check database connections
check_database "PostgreSQL" "localhost" "5432" "smartwatts"

echo ""

echo -e "${BLUE}🔴 Redis Health Checks${NC}"
echo "----------------------------------------"

# Check Redis connections
check_redis "Redis" "localhost" "6379"

echo ""

echo -e "${BLUE}🌐 Service Health Checks${NC}"
echo "----------------------------------------"

# Check service discovery
check_endpoint "Eureka Service Discovery" "http://localhost:8761/actuator/health" "200"

# Check API Gateway
check_endpoint "API Gateway" "$BASE_URL/actuator/health" "200"

# Check individual microservices
check_endpoint "User Service" "$BASE_URL/api/users/health" "200"
check_endpoint "Energy Service" "$BASE_URL/api/energy/health" "200"
check_endpoint "Device Service" "$BASE_URL/api/devices/health" "200"
check_endpoint "Analytics Service" "$BASE_URL/api/analytics/health" "200"
check_endpoint "Billing Service" "$BASE_URL/api/billing/health" "200"
check_endpoint "Facility Service" "$BASE_URL/api/facility/health" "200"
check_endpoint "Feature Flag Service" "$BASE_URL/api/feature-flags/health" "200"
check_endpoint "Device Verification Service" "$BASE_URL/api/device-verification/health" "200"
check_endpoint "Appliance Monitoring Service" "$BASE_URL/api/appliance-monitoring/health" "200"

echo ""

echo -e "${BLUE}🔗 Service Discovery Checks${NC}"
echo "----------------------------------------"

# Check Eureka service registration
echo -n "Checking Eureka service registration... "
if curl -s "$BASE_URL/eureka/apps" | grep -q "SMARTWATTS"; then
    echo -e "${GREEN}✅ PASS${NC}"
else
    echo -e "${RED}❌ FAIL (Services not registered with Eureka)${NC}"
    HEALTH_CHECK_FAILED=1
fi

echo ""

echo -e "${BLUE}📊 API Gateway Routing Checks${NC}"
echo "----------------------------------------"

# Check API Gateway routing
check_endpoint "API Gateway - User Service Route" "$BASE_URL/api/users/actuator/health" "200"
check_endpoint "API Gateway - Energy Service Route" "$BASE_URL/api/energy/actuator/health" "200"
check_endpoint "API Gateway - Device Service Route" "$BASE_URL/api/devices/actuator/health" "200"

echo ""

echo -e "${BLUE}🎨 Frontend Health Checks${NC}"
echo "----------------------------------------"

# Check frontend
check_endpoint "Frontend" "http://localhost:3000" "200"

# Check PWA manifest
check_endpoint "PWA Manifest" "http://localhost:3000/manifest.json" "200"

# Check service worker
check_endpoint "Service Worker" "http://localhost:3000/service-worker.js" "200"

echo ""

echo -e "${BLUE}🔒 Security Health Checks${NC}"
echo "----------------------------------------"

# Check for HTTPS (if configured)
if [[ "$BASE_URL" == https://* ]]; then
    echo -n "Checking HTTPS configuration... "
    if curl -s -k "$BASE_URL/actuator/health" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}"
    else
        echo -e "${RED}❌ FAIL (HTTPS not working)${NC}"
        HEALTH_CHECK_FAILED=1
    fi
else
    echo -e "${YELLOW}⚠️  SKIP (HTTPS not configured)${NC}"
fi

echo ""

echo -e "${BLUE}📈 Performance Health Checks${NC}"
echo "----------------------------------------"

# Check response times
echo -n "Checking API Gateway response time... "
response_time=$(curl -s -o /dev/null -w "%{time_total}" --max-time $TIMEOUT "$BASE_URL/actuator/health")
if (( $(echo "$response_time < 2.0" | bc -l) )); then
    echo -e "${GREEN}✅ PASS (${response_time}s)${NC}"
else
    echo -e "${YELLOW}⚠️  SLOW (${response_time}s)${NC}"
fi

echo ""

echo -e "${BLUE}🧪 Integration Health Checks${NC}"
echo "----------------------------------------"

# Check critical API endpoints
check_endpoint "User Authentication" "$BASE_URL/api/users/auth/status" "200"
check_endpoint "Energy Data" "$BASE_URL/api/energy/readings" "200"
check_endpoint "Device List" "$BASE_URL/api/devices" "200"
check_endpoint "Analytics Data" "$BASE_URL/api/analytics/summary" "200"

echo ""

# Final Results
echo "=============================================="
if [ $HEALTH_CHECK_FAILED -eq 1 ]; then
    echo -e "${RED}❌ Health check FAILED${NC}"
    echo ""
    echo "Some services are not healthy. Please check the logs:"
    echo "1. Docker logs: docker-compose logs"
    echo "2. Service logs: docker-compose logs [service-name]"
    echo "3. Database logs: docker-compose logs postgres"
    echo "4. Redis logs: docker-compose logs redis"
    echo ""
    echo "Common issues:"
    echo "1. Services not starting: Check memory and CPU resources"
    echo "2. Database connection issues: Check PostgreSQL configuration"
    echo "3. Redis connection issues: Check Redis configuration"
    echo "4. Service discovery issues: Check Eureka configuration"
    echo "5. API Gateway issues: Check routing configuration"
    exit 1
else
    echo -e "${GREEN}✅ Health check PASSED${NC}"
    echo ""
    echo "All services are healthy and running properly."
    echo ""
    echo "Service URLs:"
    echo "- Frontend: http://localhost:3000"
    echo "- API Gateway: $BASE_URL"
    echo "- Service Discovery: http://localhost:8761"
    echo "- Database: localhost:5432"
    echo "- Redis: localhost:6379"
    echo ""
    echo "You can now access the SmartWatts platform!"
    exit 0
fi









