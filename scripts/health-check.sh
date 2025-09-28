#!/bin/bash

echo "🔍 SmartWatts Health Check"
echo "========================="

# Check Docker services
echo "Checking Docker services..."
docker-compose -f docker-compose.yml ps

echo ""
echo "Checking service health..."

# Check API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ API Gateway: UP"
else
    echo "❌ API Gateway: DOWN"
fi

# Check Dashboard
if curl -s http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Dashboard: UP"
else
    echo "❌ Dashboard: DOWN"
fi

# Check Edge Gateway
if curl -s http://localhost:8088/actuator/health > /dev/null 2>&1; then
    echo "✅ Edge Gateway: UP"
else
    echo "❌ Edge Gateway: DOWN"
fi

echo ""
echo "Health check complete!"


