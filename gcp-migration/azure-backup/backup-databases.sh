#!/bin/bash

###############################################################################
# Azure Database Backup Script
# 
# Purpose: Backup all 9 PostgreSQL databases from Azure VM to local storage
#          and Azure Blob Storage for GCP migration backup
#
# Usage: ./backup-databases.sh [environment] [vm-ip]
#   environment: staging (default) or production
#   vm-ip: Public IP of the Azure VM (optional, will query if not provided)
#
# Prerequisites:
#   - Azure CLI installed and configured
#   - SSH access to Azure VM
#   - PostgreSQL client tools installed locally
#   - Access to Azure Blob Storage
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
RESOURCE_GROUP="sw-${ENVIRONMENT}-rg"
VM_NAME="sw-${ENVIRONMENT}-vm"
VM_USER="azureuser"
BACKUP_DIR="gcp-migration/azure-backup/database-backups/${ENVIRONMENT}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_BASE_DIR="${BACKUP_DIR}/${TIMESTAMP}"

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

# PostgreSQL connection details (default, should be overridden)
POSTGRES_HOST="${2:-}"
POSTGRES_PORT="5432"
POSTGRES_USER="postgres"
POSTGRES_PASSWORD=""

# Azure Storage details
STORAGE_ACCOUNT_NAME="sw${ENVIRONMENT//-/}stg"
STORAGE_CONTAINER="backups"

# Create backup directory
mkdir -p "${BACKUP_BASE_DIR}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Azure Database Backup${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Backup Directory: ${BACKUP_BASE_DIR}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to get VM IP address
get_vm_ip() {
    if [ -z "$POSTGRES_HOST" ]; then
        echo -e "${YELLOW}Getting VM IP address...${NC}"
        POSTGRES_HOST=$(az vm show \
            --resource-group "${RESOURCE_GROUP}" \
            --name "${VM_NAME}" \
            --show-details \
            --query publicIps \
            -o tsv 2>/dev/null || echo "")
        
        if [ -z "$POSTGRES_HOST" ]; then
            echo -e "${RED}Error: Could not determine VM IP address${NC}"
            echo "Please provide the VM IP address as the second argument:"
            echo "  ./backup-databases.sh ${ENVIRONMENT} <vm-ip>"
            exit 1
        fi
    fi
    echo -e "${GREEN}✓ VM IP: ${POSTGRES_HOST}${NC}"
    echo ""
}

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    # Check Azure CLI
    if ! command -v az &> /dev/null; then
        echo -e "${RED}Error: Azure CLI not found${NC}"
        exit 1
    fi
    
    # Check PostgreSQL client
    if ! command -v pg_dump &> /dev/null; then
        echo -e "${RED}Error: pg_dump not found. Please install PostgreSQL client tools.${NC}"
        exit 1
    fi
    
    # Check SSH access
    if ! ssh -o ConnectTimeout=5 -o StrictHostKeyChecking=no "${VM_USER}@${POSTGRES_HOST}" "echo 'SSH connection successful'" &>/dev/null; then
        echo -e "${YELLOW}Warning: Could not establish SSH connection${NC}"
        echo "You may need to:"
        echo "1. Add your SSH key to the VM"
        echo "2. Check firewall rules"
        echo "3. Verify VM is running"
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Function to get PostgreSQL password
get_postgres_password() {
    if [ -z "$POSTGRES_PASSWORD" ]; then
        echo -e "${YELLOW}Getting PostgreSQL password...${NC}"
        
        # Try to get from environment variable
        if [ -f ".env" ] || [ -f "azure-deployment/.env" ]; then
            POSTGRES_PASSWORD=$(grep -E "^POSTGRES_PASSWORD=" .env azure-deployment/.env 2>/dev/null | head -1 | cut -d'=' -f2- || echo "")
        fi
        
        # If still empty, prompt user
        if [ -z "$POSTGRES_PASSWORD" ]; then
            echo -e "${YELLOW}PostgreSQL password not found in environment${NC}"
            read -sp "Enter PostgreSQL password: " POSTGRES_PASSWORD
            echo ""
        fi
    fi
    
    if [ -z "$POSTGRES_PASSWORD" ]; then
        echo -e "${RED}Error: PostgreSQL password is required${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ PostgreSQL password obtained${NC}"
    echo ""
}

# Function to backup database schema
backup_database_schema() {
    local db_name=$1
    local schema_file="${BACKUP_BASE_DIR}/${db_name}_schema.sql"
    
    echo -e "  Backing up schema for ${db_name}..."
    
    PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump \
        --host="${POSTGRES_HOST}" \
        --port="${POSTGRES_PORT}" \
        --username="${POSTGRES_USER}" \
        --dbname="${db_name}" \
        --schema-only \
        --no-owner \
        --no-privileges \
        --file="${schema_file}" 2>/dev/null || {
        echo -e "    ${RED}Error: Failed to backup schema for ${db_name}${NC}"
        return 1
    }
    
    echo -e "    ${GREEN}✓ Schema backed up to ${schema_file}${NC}"
}

# Function to backup database data
backup_database_data() {
    local db_name=$1
    local data_file="${BACKUP_BASE_DIR}/${db_name}_data.sql"
    
    echo -e "  Backing up data for ${db_name}..."
    
    PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump \
        --host="${POSTGRES_HOST}" \
        --port="${POSTGRES_PORT}" \
        --username="${POSTGRES_USER}" \
        --dbname="${db_name}" \
        --data-only \
        --no-owner \
        --no-privileges \
        --file="${data_file}" 2>/dev/null || {
        echo -e "    ${RED}Error: Failed to backup data for ${db_name}${NC}"
        return 1
    }
    
    echo -e "    ${GREEN}✓ Data backed up to ${data_file}${NC}"
}

# Function to backup complete database
backup_database_complete() {
    local db_name=$1
    local complete_file="${BACKUP_BASE_DIR}/${db_name}_complete.sql"
    
    echo -e "  Backing up complete database ${db_name}..."
    
    PGPASSWORD="${POSTGRES_PASSWORD}" pg_dump \
        --host="${POSTGRES_HOST}" \
        --port="${POSTGRES_PORT}" \
        --username="${POSTGRES_USER}" \
        --dbname="${db_name}" \
        --no-owner \
        --no-privileges \
        --file="${complete_file}" 2>/dev/null || {
        echo -e "    ${RED}Error: Failed to backup complete database ${db_name}${NC}"
        return 1
    }
    
    # Compress the backup
    gzip -f "${complete_file}" || {
        echo -e "    ${YELLOW}Warning: Could not compress backup${NC}"
    }
    
    echo -e "    ${GREEN}✓ Complete database backed up to ${complete_file}.gz${NC}"
}

# Function to validate backup integrity
validate_backup() {
    local db_name=$1
    local schema_file="${BACKUP_BASE_DIR}/${db_name}_schema.sql"
    local data_file="${BACKUP_BASE_DIR}/${db_name}_data.sql"
    local complete_file="${BACKUP_BASE_DIR}/${db_name}_complete.sql.gz"
    
    local errors=0
    
    # Check schema file
    if [ ! -f "$schema_file" ] || [ ! -s "$schema_file" ]; then
        echo -e "    ${RED}Error: Schema backup file is missing or empty${NC}"
        errors=$((errors + 1))
    fi
    
    # Check data file
    if [ ! -f "$data_file" ] || [ ! -s "$data_file" ]; then
        echo -e "    ${YELLOW}Warning: Data backup file is missing or empty (may be normal for empty database)${NC}"
    fi
    
    # Check complete file
    if [ ! -f "$complete_file" ] || [ ! -s "$complete_file" ]; then
        echo -e "    ${RED}Error: Complete backup file is missing or empty${NC}"
        errors=$((errors + 1))
    fi
    
    return $errors
}

# Function to get database statistics
get_database_stats() {
    local db_name=$1
    local stats_file="${BACKUP_BASE_DIR}/${db_name}_stats.txt"
    
    echo -e "  Getting statistics for ${db_name}..."
    
    PGPASSWORD="${POSTGRES_PASSWORD}" psql \
        --host="${POSTGRES_HOST}" \
        --port="${POSTGRES_PORT}" \
        --username="${POSTGRES_USER}" \
        --dbname="${db_name}" \
        --command="
            SELECT 
                schemaname,
                tablename,
                pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
            FROM pg_tables
            WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
            ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
        " > "${stats_file}" 2>/dev/null || {
        echo -e "    ${YELLOW}Warning: Could not get database statistics${NC}"
    }
}

# Function to upload to Azure Blob Storage
upload_to_blob_storage() {
    echo -e "${YELLOW}Uploading backups to Azure Blob Storage...${NC}"
    
    # Get storage account key
    STORAGE_KEY=$(az storage account keys list \
        --resource-group "${RESOURCE_GROUP}" \
        --account-name "${STORAGE_ACCOUNT_NAME}" \
        --query '[0].value' -o tsv 2>/dev/null || echo "")
    
    if [ -z "$STORAGE_KEY" ]; then
        echo -e "${YELLOW}Warning: Could not get storage account key. Skipping blob upload.${NC}"
        return
    fi
    
    # Create container if it doesn't exist
    az storage container create \
        --name "${STORAGE_CONTAINER}" \
        --account-name "${STORAGE_ACCOUNT_NAME}" \
        --account-key "${STORAGE_KEY}" \
        --public-access off \
        &>/dev/null || true
    
    # Upload backup directory
    az storage blob upload-batch \
        --destination "${STORAGE_CONTAINER}" \
        --source "${BACKUP_BASE_DIR}" \
        --account-name "${STORAGE_ACCOUNT_NAME}" \
        --account-key "${STORAGE_KEY}" \
        --destination-path "database-backups/${TIMESTAMP}" \
        &>/dev/null || {
        echo -e "${YELLOW}Warning: Could not upload to blob storage${NC}"
        return
    }
    
    echo -e "${GREEN}✓ Backups uploaded to blob storage${NC}"
    echo ""
}

# Function to create backup summary
create_backup_summary() {
    echo -e "${YELLOW}Creating backup summary...${NC}"
    
    SUMMARY_FILE="${BACKUP_BASE_DIR}/backup-summary.json"
    
    local total_size=0
    local success_count=0
    local fail_count=0
    
    # Calculate totals
    for db_name in "${DATABASES[@]}"; do
        local complete_file="${BACKUP_BASE_DIR}/${db_name}_complete.sql.gz"
        if [ -f "$complete_file" ]; then
            total_size=$((total_size + $(stat -f%z "$complete_file" 2>/dev/null || stat -c%s "$complete_file" 2>/dev/null || echo 0))))
            success_count=$((success_count + 1))
        else
            fail_count=$((fail_count + 1))
        fi
    done
    
    cat > "${SUMMARY_FILE}" <<EOF
{
  "backupDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "environment": "${ENVIRONMENT}",
  "postgresHost": "${POSTGRES_HOST}",
  "postgresPort": "${POSTGRES_PORT}",
  "databases": [
$(for db_name in "${DATABASES[@]}"; do
    local complete_file="${BACKUP_BASE_DIR}/${db_name}_complete.sql.gz"
    local schema_file="${BACKUP_BASE_DIR}/${db_name}_schema.sql"
    local data_file="${BACKUP_BASE_DIR}/${db_name}_data.sql"
    local stats_file="${BACKUP_BASE_DIR}/${db_name}_stats.txt"
    
    local size=0
    local has_schema=false
    local has_data=false
    local has_complete=false
    
    [ -f "$complete_file" ] && size=$(stat -f%z "$complete_file" 2>/dev/null || stat -c%s "$complete_file" 2>/dev/null || echo 0) && has_complete=true
    [ -f "$schema_file" ] && has_schema=true
    [ -f "$data_file" ] && has_data=true
    
    echo "    {"
    echo "      \"name\": \"${db_name}\","
    echo "      \"backupSize\": ${size},"
    echo "      \"hasSchema\": ${has_schema},"
    echo "      \"hasData\": ${has_data},"
    echo "      \"hasComplete\": ${has_complete},"
    echo "      \"hasStats\": $([ -f "$stats_file" ] && echo "true" || echo "false")"
    if [ "$db_name" != "${DATABASES[-1]}" ]; then
        echo "    },"
    else
        echo "    }"
    fi
done)
  ],
  "summary": {
    "totalDatabases": ${#DATABASES[@]},
    "successfulBackups": ${success_count},
    "failedBackups": ${fail_count},
    "totalSize": ${total_size}
  }
}
EOF
    
    echo -e "${GREEN}✓ Backup summary created${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting database backup...${NC}"
    echo ""
    
    # Get VM IP if not provided
    get_vm_ip
    
    # Check prerequisites
    check_prerequisites
    
    # Get PostgreSQL password
    get_postgres_password
    
    # Test database connection
    echo -e "${YELLOW}Testing database connection...${NC}"
    if ! PGPASSWORD="${POSTGRES_PASSWORD}" psql \
        --host="${POSTGRES_HOST}" \
        --port="${POSTGRES_PORT}" \
        --username="${POSTGRES_USER}" \
        --dbname="postgres" \
        --command="SELECT version();" &>/dev/null; then
        echo -e "${RED}Error: Could not connect to PostgreSQL${NC}"
        echo "Please verify:"
        echo "1. VM is running"
        echo "2. PostgreSQL is accessible from this machine"
        echo "3. Firewall rules allow connection"
        echo "4. Credentials are correct"
        exit 1
    fi
    echo -e "${GREEN}✓ Database connection successful${NC}"
    echo ""
    
    # Backup each database
    echo -e "${YELLOW}Backing up databases...${NC}"
    echo ""
    
    local success_count=0
    local fail_count=0
    
    for db_name in "${DATABASES[@]}"; do
        echo -e "${BLUE}Processing: ${db_name}${NC}"
        
        # Check if database exists
        if ! PGPASSWORD="${POSTGRES_PASSWORD}" psql \
            --host="${POSTGRES_HOST}" \
            --port="${POSTGRES_PORT}" \
            --username="${POSTGRES_USER}" \
            --dbname="postgres" \
            --command="SELECT 1 FROM pg_database WHERE datname='${db_name}';" \
            --tuples-only | grep -q 1; then
            echo -e "  ${YELLOW}Warning: Database ${db_name} does not exist, skipping${NC}"
            fail_count=$((fail_count + 1))
            continue
        fi
        
        # Backup schema, data, and complete
        backup_database_schema "${db_name}" || fail_count=$((fail_count + 1))
        backup_database_data "${db_name}" || true  # Data backup failure is not critical
        backup_database_complete "${db_name}" || fail_count=$((fail_count + 1))
        get_database_stats "${db_name}" || true
        
        # Validate backup
        if validate_backup "${db_name}"; then
            success_count=$((success_count + 1))
        else
            fail_count=$((fail_count + 1))
        fi
        
        echo ""
    done
    
    # Upload to blob storage
    upload_to_blob_storage
    
    # Create summary
    create_backup_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Database backup completed!${NC}"
    echo -e "${GREEN}Successful: ${success_count}/${#DATABASES[@]}${NC}"
    echo -e "${GREEN}Failed: ${fail_count}/${#DATABASES[@]}${NC}"
    echo -e "${GREEN}Backup directory: ${BACKUP_BASE_DIR}${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    if [ $fail_count -gt 0 ]; then
        echo -e "${YELLOW}Warning: Some backups failed. Please review the errors above.${NC}"
        exit 1
    fi
}

# Run main function
main "$@"

