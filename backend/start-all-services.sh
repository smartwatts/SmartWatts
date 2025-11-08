#!/bin/bash

# SmartWatts Service Startup Script
# This script starts all services and verifies they're running properly

set -e

echo "🚀 Starting SmartWatts Services..."

# Change to backend directory
cd "$(dirname "$0")"

# Check if Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start infrastructure services first
echo "📦 Starting infrastructure services (PostgreSQL, Redis, Eureka)..."
docker-compose up -d postgres redis service-discovery

# Wait for infrastructure to be ready
echo "⏳ Waiting for infrastructure services to be ready..."
sleep 15

# Check PostgreSQL
echo "🔍 Checking PostgreSQL..."
if docker exec smartwatts-postgres psql -U postgres -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ PostgreSQL is ready"
else
    echo "❌ PostgreSQL is not ready"
    exit 1
fi

# Check Redis
echo "🔍 Checking Redis..."
if docker exec smartwatts-redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis is ready"
else
    echo "❌ Redis is not ready"
    exit 1
fi

# Check Eureka
echo "🔍 Checking Eureka..."
if curl -s http://localhost:8761 > /dev/null 2>&1; then
    echo "✅ Eureka is ready"
else
    echo "❌ Eureka is not ready"
    exit 1
fi

# Start all application services
echo "🚀 Starting all application services..."
docker-compose up -d

# Wait for services to start
echo "⏳ Waiting for services to start (this may take a few minutes)..."
sleep 30

# Check service health
echo "🔍 Checking service health..."

# Function to check service health
check_service() {
    local service_name=$1
    local port=$2
    
    if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo "✅ $service_name is healthy (port $port)"
        return 0
    else
        echo "⚠️  $service_name is not responding (port $port)"
        return 1
    fi
}

# Check each service
check_service "API Gateway" 8080
check_service "User Service" 8081
check_service "Energy Service" 8082
check_service "Device Service" 8083
check_service "Analytics Service" 8084
check_service "Billing Service" 8085
check_service "Facility Service" 8089

# Check Eureka registrations
echo ""
echo "📊 Checking Eureka service registrations..."
if curl -s http://localhost:8761/eureka/apps | grep -q "application"; then
    echo "✅ Services are registering with Eureka"
    curl -s http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<name>//;s/<\/name>//' | sort | uniq
else
    echo "⚠️  No services registered with Eureka yet (this may take a few more minutes)"
fi

echo ""
echo "✅ Service startup complete!"
echo ""
echo "📋 Service URLs:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Eureka Dashboard: http://localhost:8761"
echo "  - Spring Boot Admin: http://localhost:9090"
echo ""
echo "To view logs: docker-compose logs -f [service-name]"
echo "To stop all services: docker-compose down"

