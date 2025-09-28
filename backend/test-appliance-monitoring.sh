#!/bin/bash

echo "Testing Appliance Monitoring Service..."
echo "======================================"

BASE_URL="http://localhost:8092/api/v1/appliance-monitoring"

echo "1. Testing Health Endpoint..."
curl -s "$BASE_URL/health" | jq '.'

echo -e "\n2. Testing Appliance Types Endpoint..."
curl -s "$BASE_URL/appliance-types" | jq '.'

echo -e "\n3. Testing Swagger UI..."
curl -s -I "http://localhost:8092/swagger-ui/index.html" | head -1

echo -e "\n4. Testing Service Status..."
echo "Service Discovery: $(curl -s http://localhost:8761/actuator/health | jq -r '.status' 2>/dev/null || echo 'Not accessible')"
echo "Appliance Monitoring Service: $(curl -s $BASE_URL/health | jq -r '.status' 2>/dev/null || echo 'Not accessible')"

echo -e "\n======================================"
echo "Appliance Monitoring Service Test Complete!"
