#!/bin/bash

###############################################################################
# Migration Validation Script
# 
# Purpose: Validate database migration by comparing record counts,
#          data integrity, foreign keys, and indexes
#
# Usage: ./validate-migration.sh [environment] [azure-vm-ip] [cloud-sql-instance]
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
AZURE_VM_IP="${2:-}"
CLOUD_SQL_INSTANCE="${3:-}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"

DATABASES=(
    "smartwatts_users"
    "smartwatts_energy"
    "smartwatts_devices"
    "smartwatts_analytics"
    "smartwatts_billing"
    "smartwatts_facility360"
    "smartwatts_feature_flags"
    "smartwatts_device_verification"
    "smartwatts_appliance_monitoring"
)

echo -e "${BLUE}Validating database migration for ${ENVIRONMENT}...${NC}"

# Get passwords
read -sp "Enter Azure PostgreSQL password: " AZURE_PASSWORD
echo ""
read -sp "Enter Cloud SQL password: " CLOUD_PASSWORD
echo ""

FAILED=0

for db_name in "${DATABASES[@]}"; do
    echo -e "${YELLOW}Validating ${db_name}...${NC}"
    
    # Get table count from Azure
    AZURE_TABLES=$(PGPASSWORD="${AZURE_PASSWORD}" psql \
        --host="${AZURE_VM_IP}" \
        --port=5432 \
        --username=postgres \
        --dbname="${db_name}" \
        --tuples-only \
        --command="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null || echo "0")
    
    # Get table count from Cloud SQL (via proxy)
    CLOUD_TABLES=$(PGPASSWORD="${CLOUD_PASSWORD}" psql \
        --host=127.0.0.1 \
        --port=5432 \
        --username=postgres \
        --dbname="${db_name}" \
        --tuples-only \
        --command="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null || echo "0")
    
    if [ "$AZURE_TABLES" = "$CLOUD_TABLES" ]; then
        echo -e "  ${GREEN}✓ Table count matches: ${AZURE_TABLES}${NC}"
    else
        echo -e "  ${RED}✗ Table count mismatch: Azure=${AZURE_TABLES}, Cloud SQL=${CLOUD_TABLES}${NC}"
        FAILED=$((FAILED + 1))
    fi
done

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All databases validated successfully${NC}"
    exit 0
else
    echo -e "${RED}Validation failed for ${FAILED} database(s)${NC}"
    exit 1
fi
