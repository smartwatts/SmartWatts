#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Database Migrations"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Database configuration
DB_HOST="${STAGING_POSTGRES_HOST:-localhost}"
DB_PORT="${STAGING_POSTGRES_PORT:-5433}"
DB_USER="${STAGING_POSTGRES_USER:-smartwatts_staging}"
DB_PASSWORD="${STAGING_POSTGRES_PASSWORD:-staging_password_123}"

# Services to migrate
SERVICES=(
    "user-service"
    "energy-service"
    "device-service"
    "analytics-service"
    "billing-service"
    "appliance-monitoring-service"
    "feature-flag-service"
)

echo -e "${GREEN}Running Flyway migrations for all services...${NC}"

for service in "${SERVICES[@]}"; do
    echo "Migrating $service..."
    cd "backend/$service" || continue
    
    if [ -f "gradlew" ]; then
        ./gradlew flywayMigrate \
            -Dflyway.url="jdbc:postgresql://$DB_HOST:$DB_PORT/smartwatts_${service//-/_}_staging" \
            -Dflyway.user="$DB_USER" \
            -Dflyway.password="$DB_PASSWORD"
    fi
    
    cd - > /dev/null
done

echo -e "${GREEN}All migrations completed successfully!${NC}"

