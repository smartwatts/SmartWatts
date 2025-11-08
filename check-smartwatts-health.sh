#!/bin/bash

# SmartWatts Health Check Script
# Checks the health of all services and provides detailed status

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[⚠]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

echo "🔍 SmartWatts Health Check"
echo "=========================="

# Check Docker containers
print_status "Checking Docker containers..."
if docker compose ps | grep -q "Up"; then
    print_success "Docker containers are running"
    docker compose ps
else
    print_error "No Docker containers are running"
    exit 1
fi

echo ""

# Check individual services
services=(
    "PostgreSQL:5432"
    "Redis:6379"
    "Service Discovery:8761"
    "User Service:8081"
    "API Gateway:8080"
    "Energy Service:8082"
    "Device Service:8083"
    "Analytics Service:8084"
    "Billing Service:8085"
    "Appliance Monitoring:8087"
)

print_status "Checking service endpoints..."

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    
    if curl -s "http://localhost:$port" > /dev/null 2>&1 || \
       curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1 || \
       curl -s "http://localhost:$port/health" > /dev/null 2>&1; then
        print_success "$name (port $port) is responding"
    else
        print_error "$name (port $port) is not responding"
    fi
done

echo ""

# Check authentication
print_status "Testing authentication..."
auth_response=$(curl -s -X POST "http://localhost:8081/api/v1/users/login" \
    -H "Content-Type: application/json" \
    -d '{"usernameOrEmail": "admin@mysmartwatts.com", "password": "password"}')

if echo "$auth_response" | grep -q "accessToken"; then
    print_success "Authentication is working"
    token=$(echo "$auth_response" | jq -r '.accessToken' 2>/dev/null)
    echo "🔑 Current token: ${token:0:50}..."
else
    print_error "Authentication failed"
    echo "Response: $auth_response"
fi

echo ""

# Check API Gateway proxy
print_status "Testing API Gateway proxy..."
proxy_response=$(curl -s "http://localhost:3000/api/proxy?service=user&path=/api/v1/accounts/stats" \
    -H "Authorization: Bearer $token" 2>/dev/null)

if echo "$proxy_response" | grep -q "totalAccounts"; then
    print_success "API Gateway proxy is working"
    echo "📊 Accounts stats: $proxy_response"
else
    print_error "API Gateway proxy is not working"
    echo "Response: $proxy_response"
fi

echo ""

# Check frontend
print_status "Checking frontend..."
if curl -s "http://localhost:3000" > /dev/null 2>&1; then
    print_success "Frontend is accessible"
else
    print_error "Frontend is not accessible"
fi

echo ""
echo "=========================="
print_status "Health check complete!"
