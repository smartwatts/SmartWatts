#!/bin/bash

# Test database connection
echo "Testing database connection..."

# Test PostgreSQL connection
if docker exec smartwatts-postgres psql -U postgres -d smartwatts_analytics -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ PostgreSQL connection successful"
else
    echo "❌ PostgreSQL connection failed"
    exit 1
fi

# Test Redis connection
if docker exec smartwatts-redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis connection successful"
else
    echo "❌ Redis connection failed"
    exit 1
fi

echo "🎉 All database connections working!"
