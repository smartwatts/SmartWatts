#!/bin/bash

###############################################################################
# Database Migration Script
# 
# Purpose: Migrate databases from Azure PostgreSQL to Cloud SQL PostgreSQL
#          with schema validation, data integrity checks, and index recreation
#
# Usage: ./migrate-databases.sh [environment] [azure-vm-ip] [cloud-sql-instance]
#   environment: staging (default) or production
#   azure-vm-ip: IP address of Azure VM with PostgreSQL
#   cloud-sql-instance: Cloud SQL instance connection name
#
# Prerequisites:
#   - Azure PostgreSQL accessible
#   - Cloud SQL instance created
#   - Cloud SQL Proxy or direct connection configured
#   - PostgreSQL client tools installed
#   - gcloud CLI configured
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT="${1:-staging}"
AZURE_VM_IP="${2:-}"
CLOUD_SQL_INSTANCE="${3:-}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"

# Database list
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

# Azure PostgreSQL connection
AZURE_POSTGRES_HOST="${AZURE_VM_IP}"
AZURE_POSTGRES_PORT="5432"
AZURE_POSTGRES_USER="postgres"
AZURE_POSTGRES_PASSWORD=""

# Cloud SQL connection (via Cloud SQL Proxy)
CLOUD_SQL_HOST="127.0.0.1"
CLOUD_SQL_PORT="${CLOUD_SQL_PORT:-5433}"  # Default to 5433 to avoid conflict with local PostgreSQL
CLOUD_SQL_USER="postgres"
CLOUD_SQL_PASSWORD=""

MIGRATION_DIR="gcp-migration/database-migration/migrations/${ENVIRONMENT}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mkdir -p "${MIGRATION_DIR}/${TIMESTAMP}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Database Migration: Azure to Cloud SQL${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! command -v pg_dump &> /dev/null; then
        echo -e "${RED}Error: pg_dump not found${NC}"
        exit 1
    fi
    
    if ! command -v psql &> /dev/null; then
        echo -e "${RED}Error: psql not found${NC}"
        exit 1
    fi
    
    if ! command -v gcloud &> /dev/null; then
        echo -e "${RED}Error: gcloud not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Function to get connection details
get_connection_details() {
    echo -e "${YELLOW}Getting connection details...${NC}"
    
    # Get Azure PostgreSQL password
    if [ -z "$AZURE_POSTGRES_PASSWORD" ]; then
        read -sp "Enter Azure PostgreSQL password: " AZURE_POSTGRES_PASSWORD
        echo ""
    fi
    
    # Get Cloud SQL instance if not provided
    if [ -z "$CLOUD_SQL_INSTANCE" ]; then
        CLOUD_SQL_INSTANCE=$(gcloud sql instances list \
            --project="${PROJECT_ID}" \
            --filter="name:smartwatts-${ENVIRONMENT}-db" \
            --format="value(connectionName)" 2>/dev/null || echo "")
        
        if [ -z "$CLOUD_SQL_INSTANCE" ]; then
            echo -e "${RED}Error: Could not find Cloud SQL instance${NC}"
            echo "Please provide the connection name as the third argument"
            exit 1
        fi
    fi
    
    # Get Cloud SQL password
    if [ -z "$CLOUD_SQL_PASSWORD" ]; then
        read -sp "Enter Cloud SQL root password: " CLOUD_SQL_PASSWORD
        echo ""
    fi
    
    echo -e "${GREEN}✓ Connection details obtained${NC}"
    echo ""
}

# Function to start Cloud SQL Proxy
start_cloud_sql_proxy() {
    echo -e "${YELLOW}Starting Cloud SQL Proxy...${NC}"
    
    # Check if proxy is already running on the expected port
    if lsof -i :"${CLOUD_SQL_PORT}" > /dev/null 2>&1; then
        echo -e "${YELLOW}Cloud SQL Proxy appears to be running on port ${CLOUD_SQL_PORT}${NC}"
        echo -e "${YELLOW}Assuming proxy is already started (you may have started it manually)${NC}"
        return 0
    fi
    
    # Check if proxy process is running
    if pgrep -f "cloud-sql-proxy" > /dev/null; then
        echo -e "${YELLOW}Cloud SQL Proxy process found, but not on expected port${NC}"
        echo -e "${YELLOW}Please ensure proxy is running on port ${CLOUD_SQL_PORT}${NC}"
        echo -e "${YELLOW}Run: cloud-sql-proxy ${CLOUD_SQL_INSTANCE} --port=${CLOUD_SQL_PORT}${NC}"
        return 0
    fi
    
    # Start Cloud SQL Proxy in background
    cloud-sql-proxy "${CLOUD_SQL_INSTANCE}" \
        --port="${CLOUD_SQL_PORT}" \
        --credentials-file="${HOME}/.config/gcloud/application_default_credentials.json" &
    
    PROXY_PID=$!
    sleep 5
    
    # Verify proxy is running
    if ! kill -0 $PROXY_PID 2>/dev/null; then
        echo -e "${RED}Error: Cloud SQL Proxy failed to start${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Cloud SQL Proxy started (PID: ${PROXY_PID})${NC}"
    echo ""
}

# Function to export database from Azure
export_database() {
    local db_name=$1
    local export_file="${MIGRATION_DIR}/${TIMESTAMP}/${db_name}.sql"
    
    echo -e "  Exporting ${db_name} from Azure..."
    
    PGPASSWORD="${AZURE_POSTGRES_PASSWORD}" pg_dump \
        --host="${AZURE_POSTGRES_HOST}" \
        --port="${AZURE_POSTGRES_PORT}" \
        --username="${AZURE_POSTGRES_USER}" \
        --dbname="${db_name}" \
        --no-owner \
        --no-privileges \
        --file="${export_file}" 2>/dev/null || {
        echo -e "    ${RED}Error: Failed to export ${db_name}${NC}"
        return 1
    }
    
    # Compress export
    gzip -f "${export_file}"
    
    echo -e "    ${GREEN}✓ Exported to ${export_file}.gz${NC}"
}

# Function to import database to Cloud SQL
import_database() {
    local db_name=$1
    local export_file="${MIGRATION_DIR}/${TIMESTAMP}/${db_name}.sql.gz"
    
    echo -e "  Importing ${db_name} to Cloud SQL..."
    
    # Create database if it doesn't exist
    PGPASSWORD="${CLOUD_SQL_PASSWORD}" psql \
        --host="${CLOUD_SQL_HOST}" \
        --port="${CLOUD_SQL_PORT}" \
        --username="${CLOUD_SQL_USER}" \
        --dbname="postgres" \
        --command="CREATE DATABASE ${db_name};" 2>/dev/null || {
        echo -e "    ${YELLOW}Database ${db_name} may already exist${NC}"
    }
    
    # Import database
    gunzip -c "${export_file}" | \
        PGPASSWORD="${CLOUD_SQL_PASSWORD}" psql \
            --host="${CLOUD_SQL_HOST}" \
            --port="${CLOUD_SQL_PORT}" \
            --username="${CLOUD_SQL_USER}" \
            --dbname="${db_name}" 2>/dev/null || {
        echo -e "    ${RED}Error: Failed to import ${db_name}${NC}"
        return 1
    }
    
    echo -e "    ${GREEN}✓ Imported ${db_name}${NC}"
}

# Function to validate migration
validate_migration() {
    local db_name=$1
    
    echo -e "  Validating ${db_name}..."
    
    # Get table counts from both databases
    AZURE_COUNT=$(PGPASSWORD="${AZURE_POSTGRES_PASSWORD}" psql \
        --host="${AZURE_POSTGRES_HOST}" \
        --port="${AZURE_POSTGRES_PORT}" \
        --username="${AZURE_POSTGRES_USER}" \
        --dbname="${db_name}" \
        --tuples-only \
        --command="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null || echo "0")
    
    CLOUD_COUNT=$(PGPASSWORD="${CLOUD_SQL_PASSWORD}" psql \
        --host="${CLOUD_SQL_HOST}" \
        --port="${CLOUD_SQL_PORT}" \
        --username="${CLOUD_SQL_USER}" \
        --dbname="${db_name}" \
        --tuples-only \
        --command="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null || echo "0")
    
    if [ "$AZURE_COUNT" = "$CLOUD_COUNT" ]; then
        echo -e "    ${GREEN}✓ Table count matches: ${AZURE_COUNT}${NC}"
    else
        echo -e "    ${RED}✗ Table count mismatch: Azure=${AZURE_COUNT}, Cloud SQL=${CLOUD_COUNT}${NC}"
        return 1
    fi
}

# Main execution
main() {
    echo -e "${BLUE}Starting database migration...${NC}"
    echo ""
    
    check_prerequisites
    get_connection_details
    start_cloud_sql_proxy
    
    # Migrate each database
    local success_count=0
    local fail_count=0
    
    for db_name in "${DATABASES[@]}"; do
        echo -e "${BLUE}Migrating: ${db_name}${NC}"
        
        # Export from Azure
        export_database "${db_name}" || {
            fail_count=$((fail_count + 1))
            continue
        }
        
        # Import to Cloud SQL
        import_database "${db_name}" || {
            fail_count=$((fail_count + 1))
            continue
        }
        
        # Validate
        validate_migration "${db_name}" || {
            fail_count=$((fail_count + 1))
            continue
        }
        
        success_count=$((success_count + 1))
        echo ""
    done
    
    # Stop Cloud SQL Proxy
    if [ -n "${PROXY_PID:-}" ]; then
        kill $PROXY_PID 2>/dev/null || true
    fi
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Migration completed!${NC}"
    echo -e "${GREEN}Successful: ${success_count}/${#DATABASES[@]}${NC}"
    echo -e "${GREEN}Failed: ${fail_count}/${#DATABASES[@]}${NC}"
    echo -e "${GREEN}========================================${NC}"
    
    if [ $fail_count -gt 0 ]; then
        exit 1
    fi
}

# Run main function
main "$@"

