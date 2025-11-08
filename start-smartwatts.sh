#!/bin/bash

# SmartWatts Production-Ready Development Setup
# This script ensures all services start properly with health checks

set -e  # Exit on any error

echo "🚀 Starting SmartWatts Production-Ready Development Environment"
echo "================================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a service is healthy
check_service_health() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    print_status "Checking health of $service_name on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1 || \
           curl -s "http://localhost:$port/health" > /dev/null 2>&1 || \
           curl -s "http://localhost:$port" > /dev/null 2>&1; then
            print_success "$service_name is healthy"
            return 0
        fi
        
        print_status "Attempt $attempt/$max_attempts - $service_name not ready yet, waiting..."
        sleep 2
        ((attempt++))
    done
    
    print_error "$service_name failed to start after $max_attempts attempts"
    return 1
}

# Function to get fresh admin token
get_admin_token() {
    print_status "Getting fresh admin authentication token..."
    
    local max_attempts=10
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        local response=$(curl -s -X POST "http://localhost:8081/api/v1/users/login" \
            -H "Content-Type: application/json" \
            -d '{"usernameOrEmail": "admin@mysmartwatts.com", "password": "password"}' 2>/dev/null)
        
        if echo "$response" | grep -q "accessToken"; then
            local token=$(echo "$response" | jq -r '.accessToken' 2>/dev/null)
            if [ "$token" != "null" ] && [ -n "$token" ]; then
                print_success "Admin token obtained successfully"
                echo "🔑 Admin Token: $token"
                echo "📧 Email: admin@mysmartwatts.com"
                echo "🔒 Password: password"
                return 0
            fi
        fi
        
        print_status "Attempt $attempt/$max_attempts - Token request failed, retrying..."
        sleep 3
        ((attempt++))
    done
    
    print_error "Failed to get admin token after $max_attempts attempts"
    return 1
}

# Step 1: Clean up any existing containers
print_status "Cleaning up existing SmartWatts containers..."
docker compose down --remove-orphans 2>/dev/null || true

# Step 2: Start core infrastructure
print_status "Starting core infrastructure (PostgreSQL, Redis, Service Discovery)..."
docker compose up -d postgres redis service-discovery

# Step 3: Wait for core services
print_status "Waiting for core services to be ready..."
sleep 10

# Step 4: Start user service
print_status "Starting User Service..."
docker compose up -d user-service

# Step 5: Check user service health
if ! check_service_health "User Service" 8081; then
    print_error "User Service failed to start properly"
    exit 1
fi

# Step 6: Start API Gateway
print_status "Starting API Gateway..."
docker compose up -d api-gateway

# Step 7: Check API Gateway health
if ! check_service_health "API Gateway" 8080; then
    print_error "API Gateway failed to start properly"
    exit 1
fi

# Step 8: Start remaining services
print_status "Starting remaining services..."
docker compose up -d energy-service device-service analytics-service billing-service appliance-monitoring-service

# Step 9: Get admin token
if ! get_admin_token; then
    print_warning "Could not get admin token automatically"
    print_status "You may need to login manually at http://localhost:3000"
fi

# Step 10: Display status
echo ""
echo "================================================================"
print_success "SmartWatts Development Environment Started Successfully!"
echo "================================================================"
echo ""
echo "🌐 Frontend URL: http://localhost:3000"
echo "🔧 Admin Login: admin@mysmartwatts.com / password"
echo "📊 Service Discovery: http://localhost:8761"
echo "🗄️  Database: localhost:5432"
echo ""
echo "📋 Service Status:"
docker compose ps
echo ""
print_status "To stop all services: docker compose down"
print_status "To view logs: docker compose logs -f [service-name]"
echo ""
print_success "Ready for development! 🎉"
