#!/bin/bash

echo "🚀 Starting SmartWatts Backend Services for Integration Testing"
echo "================================================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ docker-compose.yml not found. Please run this script from the project root."
    exit 1
fi

echo "📦 Building and starting backend services..."
docker-compose up --build -d

echo "⏳ Waiting for services to start..."
sleep 30

echo "🔍 Checking service health..."

# Check each service
services=("user-service" "energy-service" "device-service" "analytics-service" "billing-service")

for service in "${services[@]}"; do
    echo "Checking $service..."
    if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ $service is healthy"
    else
        echo "❌ $service is not responding"
    fi
done

echo ""
echo "🌐 Backend services are running at:"
echo "   - API Gateway: http://localhost:8080"
echo "   - User Service: http://localhost:8081"
echo "   - Energy Service: http://localhost:8082"
echo "   - Device Service: http://localhost:8083"
echo "   - Analytics Service: http://localhost:8084"
echo "   - Billing Service: http://localhost:8085"
echo ""
echo "🎯 Frontend can be accessed at: http://localhost:3000"
echo "📊 Test integration at: http://localhost:3000/test-integration"
echo ""
echo "To stop services, run: docker-compose down" 