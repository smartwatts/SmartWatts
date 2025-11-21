#!/bin/bash

###############################################################################
# Run Flyway Migrations for Cloud SQL
# 
# Purpose: Run Flyway database migrations directly against Cloud SQL
#          using existing migration files from the codebase
#
# Usage: ./run-flyway-migrations.sh [environment]
#   environment: staging (default) or production
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

ENVIRONMENT="${1:-staging}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"

# Cloud SQL connection (via Cloud SQL Proxy)
CLOUD_SQL_HOST="127.0.0.1"
CLOUD_SQL_PORT="${CLOUD_SQL_PORT:-5433}"
CLOUD_SQL_USER="postgres"

# Service to database name mapping
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

# Services to migrate
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

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Running Flyway Migrations for Cloud SQL${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! lsof -i :"${CLOUD_SQL_PORT}" > /dev/null 2>&1; then
        echo -e "${RED}Error: Cloud SQL Proxy is not running on port ${CLOUD_SQL_PORT}${NC}"
        echo -e "${YELLOW}Please start it with:${NC}"
        echo -e "  cloud-sql-proxy smartwatts-${ENVIRONMENT}:europe-west1:smartwatts-${ENVIRONMENT}-db --port=${CLOUD_SQL_PORT}"
        exit 1
    fi
    
    if ! command -v psql &> /dev/null; then
        echo -e "${RED}Error: psql not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Get Cloud SQL password
get_cloud_sql_password() {
    echo -e "${YELLOW}Getting Cloud SQL password...${NC}"
    
    # Try Secret Manager first
    if command -v gcloud &> /dev/null; then
        PASSWORD=$(gcloud secrets versions access latest \
            --secret="postgres-root-password" \
            --project="${PROJECT_ID}" 2>/dev/null || echo "")
        
        if [ -n "$PASSWORD" ]; then
            CLOUD_SQL_PASSWORD="$PASSWORD"
            echo -e "${GREEN}✓ Password retrieved from Secret Manager${NC}"
            echo ""
            return 0
        fi
    fi
    
    # Fallback to prompt
    read -sp "Enter Cloud SQL root password: " CLOUD_SQL_PASSWORD
    echo ""
    
    if [ -z "$CLOUD_SQL_PASSWORD" ]; then
        echo -e "${RED}Error: Password is required${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Password obtained${NC}"
    echo ""
}

# Verify database exists
verify_database() {
    local db_name=$1
    
    PGPASSWORD="${CLOUD_SQL_PASSWORD}" psql \
        --host="${CLOUD_SQL_HOST}" \
        --port="${CLOUD_SQL_PORT}" \
        --username="${CLOUD_SQL_USER}" \
        --dbname="postgres" \
        --tuples-only \
        --command="SELECT 1 FROM pg_database WHERE datname='${db_name}';" 2>/dev/null | grep -q 1 || {
        echo -e "${RED}Error: Database ${db_name} does not exist${NC}"
        return 1
    }
}

# Run Flyway migration for a service
run_flyway_migration() {
    local service_name=$1
    local db_name=$(get_database_name "$service_name")
    
    if [ -z "$db_name" ]; then
        echo -e "${RED}Error: No database mapping for service ${service_name}${NC}"
        return 1
    fi
    
    echo -e "${BLUE}Migrating ${service_name} → ${db_name}...${NC}"
    
    # Verify database exists
    if ! verify_database "${db_name}"; then
        return 1
    fi
    
    # Check if service directory exists
    if [ ! -d "backend/${service_name}" ]; then
        echo -e "${YELLOW}  Warning: Service directory not found, skipping${NC}"
        return 0
    fi
    
    # Check if migrations directory exists
    if [ ! -d "backend/${service_name}/src/main/resources/db/migration" ]; then
        echo -e "${YELLOW}  Warning: No migrations found for ${service_name}, skipping${NC}"
        return 0
    fi
    
    # Navigate to service directory
    cd "backend/${service_name}" || {
        echo -e "${RED}  Error: Could not navigate to service directory${NC}"
        return 1
    }
    
    # Check if gradlew exists
    if [ ! -f "gradlew" ]; then
        echo -e "${YELLOW}  Warning: gradlew not found, skipping${NC}"
        cd - > /dev/null
        return 0
    fi
    
    # Make gradlew executable
    chmod +x gradlew 2>/dev/null || true
    
    # Run Flyway migration
    echo -e "  Running Flyway migration..."
    ./gradlew flywayMigrate \
        -Dflyway.url="jdbc:postgresql://${CLOUD_SQL_HOST}:${CLOUD_SQL_PORT}/${db_name}" \
        -Dflyway.user="${CLOUD_SQL_USER}" \
        -Dflyway.password="${CLOUD_SQL_PASSWORD}" \
        --no-daemon > /tmp/flyway-${service_name}.log 2>&1
    
    if [ $? -eq 0 ]; then
        echo -e "  ${GREEN}✓ Migration completed for ${service_name}${NC}"
        cd - > /dev/null
        return 0
    else
        echo -e "  ${RED}✗ Migration failed for ${service_name}${NC}"
        echo -e "  ${YELLOW}Check log: /tmp/flyway-${service_name}.log${NC}"
        cd - > /dev/null
        return 1
    fi
}

# Main execution
main() {
    echo -e "${BLUE}Starting Flyway migrations...${NC}"
    echo ""
    
    check_prerequisites
    get_cloud_sql_password
    
    # Test connection
    echo -e "${YELLOW}Testing Cloud SQL connection...${NC}"
    PGPASSWORD="${CLOUD_SQL_PASSWORD}" psql \
        --host="${CLOUD_SQL_HOST}" \
        --port="${CLOUD_SQL_PORT}" \
        --username="${CLOUD_SQL_USER}" \
        --dbname="postgres" \
        --command="SELECT version();" > /dev/null 2>&1 || {
        echo -e "${RED}Error: Could not connect to Cloud SQL${NC}"
        echo -e "${YELLOW}Please verify:${NC}"
        echo -e "  1. Cloud SQL Proxy is running on port ${CLOUD_SQL_PORT}"
        echo -e "  2. Password is correct"
        exit 1
    }
    echo -e "${GREEN}✓ Connection successful${NC}"
    echo ""
    
    # Run migrations for each service
    local success_count=0
    local fail_count=0
    
    for service_name in "${SERVICES[@]}"; do
        if run_flyway_migration "${service_name}"; then
            success_count=$((success_count + 1))
        else
            fail_count=$((fail_count + 1))
        fi
    done
    
    # Summary
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Flyway Migrations Completed!${NC}"
    echo -e "${GREEN}Successful: ${success_count}/${#SERVICES[@]}${NC}"
    if [ $fail_count -gt 0 ]; then
        echo -e "${RED}Failed: ${fail_count}/${#SERVICES[@]}${NC}"
    fi
    echo -e "${GREEN}========================================${NC}"
    
    if [ $fail_count -gt 0 ]; then
        exit 1
    fi
}

# Run main function
main "$@"

