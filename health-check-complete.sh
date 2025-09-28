#!/bin/bash

# Comprehensive SmartWatts Health Check
echo "SmartWatts Complete Health Check - $(date)"
echo "=============================================="

# Check Docker
if docker ps > /dev/null 2>&1; then
    echo "✅ Docker: Running"
else
    echo "❌ Docker: Not running"
    exit 1
fi

# Check PostgreSQL
if docker exec smartwatts-postgres psql -U postgres -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ PostgreSQL: Running and accessible"
else
    echo "❌ PostgreSQL: Not accessible"
fi

# Check Redis
if docker exec smartwatts-redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis: Running and accessible"
else
    echo "❌ Redis: Not accessible"
fi

# Check MQTT
if docker exec smartwatts-mosquitto mosquitto_pub -h localhost -t test -m "test" > /dev/null 2>&1; then
    echo "✅ MQTT: Running and accessible"
else
    echo "❌ MQTT: Not accessible"
fi

# Check databases
echo ""
echo "Database Status:"
for db in smartwatts_users smartwatts_energy smartwatts_devices smartwatts_analytics smartwatts_billing smartwatts_notifications smartwatts_edge; do
    if docker exec smartwatts-postgres psql -U postgres -d $db -c "SELECT 1;" > /dev/null 2>&1; then
        echo "✅ $db: Accessible"
    else
        echo "❌ $db: Not accessible"
    fi
done

# Check system resources
echo ""
echo "System Resources:"
echo "Memory: $(free -h | grep Mem | awk '{print $3"/"$2}')"
echo "Disk: $(df -h / | tail -1 | awk '{print $3"/"$2}')"
echo "CPU Load: $(uptime | awk -F'load average:' '{print $2}')"

# Check running services
echo ""
echo "Running Services:"
if pgrep -f "gradle.*bootRun" > /dev/null; then
    echo "✅ Spring Boot services: Running"
else
    echo "❌ Spring Boot services: Not running"
fi

if pgrep -f "next.*dev" > /dev/null; then
    echo "✅ Next.js frontend: Running"
else
    echo "❌ Next.js frontend: Not running"
fi

echo ""
echo "🎯 SmartWatts Status: Ready for 100% completion!"
