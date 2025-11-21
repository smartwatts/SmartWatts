#!/bin/bash

###############################################################################
# Run SQL Migrations Directly
# 
# Purpose: Run SQL migration files directly using psql (bypassing Flyway)
#
###############################################################################

set -euo pipefail

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ENVIRONMENT="${1:-staging}"
CLOUD_SQL_HOST="127.0.0.1"
CLOUD_SQL_PORT="${CLOUD_SQL_PORT:-5433}"
CLOUD_SQL_USER="postgres"
CLOUD_SQL_PASSWORD="${2:-rojfig-bexsi4-hoHbef}"

# Service to database mapping
get_database_name() {
    local service_name=$1
    case "$service_name" in
        "user-service") echo "smartwatts_users" ;;
        "energy-service") echo "smartwatts_energy" ;;
        "device-service") echo "smartwatts_devices" ;;
        "analytics-service") echo "smartwatts_analytics" ;;
        "billing-service") echo "smartwatts_billing" ;;
        "facility-service") echo "smartwatts_facility360" ;;
        "feature-flag-service") echo "smartwatts_feature_flags" ;;
        "device-verification-service") echo "smartwatts_device_verification" ;;
        "appliance-monitoring-service") echo "smartwatts_appliance_monitoring" ;;
        *) echo "" ;;
    esac
}

SERVICES=(
    "user-service"
    "energy-service"
    "device-service"
    "analytics-service"
    "billing-service"
    "facility-service"
    "feature-flag-service"
    "device-verification-service"
    "appliance-monitoring-service"
)

echo -e "${BLUE}Running SQL migrations directly...${NC}"
echo ""

success_count=0
fail_count=0

for service_name in "${SERVICES[@]}"; do
    db_name=$(get_database_name "$service_name")
    migration_dir="backend/${service_name}/src/main/resources/db/migration"
    
    if [ ! -d "$migration_dir" ]; then
        echo -e "${YELLOW}Skipping ${service_name}: No migrations directory${NC}"
        continue
    fi
    
    echo -e "${BLUE}Migrating ${service_name} → ${db_name}...${NC}"
    
    # Get migration files in order
    migration_files=$(find "$migration_dir" -name "*.sql" | sort)
    
    if [ -z "$migration_files" ]; then
        echo -e "${YELLOW}  No SQL files found${NC}"
        continue
    fi
    
    # Run each migration file
    for sql_file in $migration_files; do
        filename=$(basename "$sql_file")
        echo -e "  Running ${filename}..."
        
        PGPASSWORD="${CLOUD_SQL_PASSWORD}" psql \
            --host="${CLOUD_SQL_HOST}" \
            --port="${CLOUD_SQL_PORT}" \
            --username="${CLOUD_SQL_USER}" \
            --dbname="${db_name}" \
            --file="${sql_file}" > /tmp/migration-${service_name}-${filename}.log 2>&1
        
        if [ $? -eq 0 ]; then
            echo -e "    ${GREEN}✓ ${filename}${NC}"
        else
            echo -e "    ${RED}✗ ${filename} failed${NC}"
            echo -e "    ${YELLOW}Check: /tmp/migration-${service_name}-${filename}.log${NC}"
            fail_count=$((fail_count + 1))
            continue 2
        fi
    done
    
    echo -e "  ${GREEN}✓ ${service_name} completed${NC}"
    success_count=$((success_count + 1))
    echo ""
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Migrations Completed!${NC}"
echo -e "${GREEN}Successful: ${success_count}/${#SERVICES[@]}${NC}"
if [ $fail_count -gt 0 ]; then
    echo -e "${RED}Failed: ${fail_count}${NC}"
fi
echo -e "${GREEN}========================================${NC}"

