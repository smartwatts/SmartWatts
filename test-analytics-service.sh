#!/bin/bash

echo "Starting Analytics Service Integration Test..."

# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/smartwatts_analytics
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
export SPRING_PROFILES_ACTIVE=default

# Change to analytics service directory
cd backend/analytics-service

echo "Building analytics service..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Starting analytics service..."
java -jar build/libs/analytics-service-0.0.1-SNAPSHOT.jar

echo "Analytics service test completed."
