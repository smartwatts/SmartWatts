#!/bin/bash

# Start SmartWatts Services
echo "🚀 Starting SmartWatts Services..."

# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/smartwatts_analytics
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
export SPRING_PROFILES_ACTIVE=default

# Start service discovery first
echo "Starting Service Discovery..."
cd backend/service-discovery
./gradlew bootRun &
SERVICE_DISCOVERY_PID=$!

# Wait for service discovery to start
echo "Waiting for Service Discovery to start..."
sleep 30

# Start analytics service
echo "Starting Analytics Service..."
cd ../analytics-service
./gradlew bootRun &
ANALYTICS_PID=$!

# Wait for analytics service to start
echo "Waiting for Analytics Service to start..."
sleep 60

# Test the service
echo "Testing Analytics Service..."
if curl -s http://localhost:8084/actuator/health > /dev/null; then
    echo "✅ Analytics Service is running!"
    echo "Health check: http://localhost:8084/actuator/health"
else
    echo "❌ Analytics Service failed to start"
fi

echo "🎉 Services started successfully!"
echo "Service Discovery PID: $SERVICE_DISCOVERY_PID"
echo "Analytics Service PID: $ANALYTICS_PID"
