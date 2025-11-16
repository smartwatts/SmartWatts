#!/bin/bash

###############################################################################
# Azure Configuration Backup Script
# 
# Purpose: Export all environment variables, application configurations,
#          and secrets from Azure Key Vault for GCP migration backup
#
# Usage: ./backup-configurations.sh [environment]
#   environment: staging (default) or production
#
# Prerequisites:
#   - Azure CLI installed and configured
#   - Logged in to Azure (az login)
#   - Appropriate permissions to read Key Vault secrets
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
KEY_VAULT_NAME="sw-${ENVIRONMENT}-kv"
BACKUP_DIR="gcp-migration/azure-backup/configuration-backups/${ENVIRONMENT}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_BASE_DIR="${BACKUP_DIR}/${TIMESTAMP}"

# Create backup directory structure
mkdir -p "${BACKUP_BASE_DIR}"/{env-vars,app-configs,secrets,connection-strings}

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Azure Configuration Backup${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Backup Directory: ${BACKUP_BASE_DIR}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! command -v az &> /dev/null; then
        echo -e "${RED}Error: Azure CLI not found${NC}"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}Error: jq not found. Please install jq for JSON processing.${NC}"
        exit 1
    fi
    
    if ! az account show &>/dev/null; then
        echo -e "${RED}Error: Not logged in to Azure. Please run 'az login'${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Function to backup environment variables from files
backup_env_variables() {
    echo -e "${YELLOW}Backing up environment variables...${NC}"
    
    local env_files=(
        ".env"
        "azure-deployment/.env"
        "env.template"
        "backend/env.example"
    )
    
    local found=false
    
    for env_file in "${env_files[@]}"; do
        if [ -f "$env_file" ]; then
            echo -e "  Found: ${env_file}"
            cp "$env_file" "${BACKUP_BASE_DIR}/env-vars/$(basename ${env_file})"
            found=true
        fi
    done
    
    # Also check docker-compose files for environment variables
    local compose_files=(
        "docker-compose.yml"
        "azure-deployment/docker-compose.azure.yml"
    )
    
    for compose_file in "${compose_files[@]}"; do
        if [ -f "$compose_file" ]; then
            echo -e "  Extracting env vars from: ${compose_file}"
            # Extract environment variables from docker-compose files
            grep -E "^\s*[A-Z_]+:" "$compose_file" > "${BACKUP_BASE_DIR}/env-vars/$(basename ${compose_file}).env" 2>/dev/null || true
        fi
    done
    
    if [ "$found" = true ]; then
        echo -e "${GREEN}✓ Environment variables backed up${NC}"
    else
        echo -e "${YELLOW}Warning: No environment variable files found${NC}"
    fi
    echo ""
}

# Function to backup application configurations
backup_app_configurations() {
    echo -e "${YELLOW}Backing up application configurations...${NC}"
    
    # Backup application.yml files from all services
    local config_files=(
        "backend/api-gateway/src/main/resources/application.yml"
        "backend/user-service/src/main/resources/application.yml"
        "backend/energy-service/src/main/resources/application.yml"
        "backend/device-service/src/main/resources/application.yml"
        "backend/analytics-service/src/main/resources/application.yml"
        "backend/billing-service/src/main/resources/application.yml"
        "backend/service-discovery/src/main/resources/application.yml"
        "backend/edge-gateway/src/main/resources/application.yml"
        "backend/facility-service/src/main/resources/application.yml"
        "backend/feature-flag-service/src/main/resources/application.yml"
        "backend/device-verification-service/src/main/resources/application.yml"
        "backend/appliance-monitoring-service/src/main/resources/application.yml"
        "backend/notification-service/src/main/resources/application.yml"
        "azure-deployment/application-azure.yml"
    )
    
    local found=false
    
    for config_file in "${config_files[@]}"; do
        if [ -f "$config_file" ]; then
            echo -e "  Backing up: ${config_file}"
            local service_name=$(echo "$config_file" | sed -E 's|.*/([^/]+)/.*|\1|' || basename "$config_file")
            mkdir -p "${BACKUP_BASE_DIR}/app-configs/${service_name}"
            cp "$config_file" "${BACKUP_BASE_DIR}/app-configs/${service_name}/$(basename ${config_file})"
            found=true
        fi
    done
    
    if [ "$found" = true ]; then
        echo -e "${GREEN}✓ Application configurations backed up${NC}"
    else
        echo -e "${YELLOW}Warning: No application configuration files found${NC}"
    fi
    echo ""
}

# Function to backup Key Vault secrets
backup_key_vault_secrets() {
    echo -e "${YELLOW}Backing up Key Vault secrets...${NC}"
    
    # Check if Key Vault exists
    if ! az keyvault show --name "${KEY_VAULT_NAME}" --resource-group "${RESOURCE_GROUP}" &>/dev/null; then
        echo -e "${YELLOW}Warning: Key Vault '${KEY_VAULT_NAME}' not found${NC}"
        echo "Skipping Key Vault secrets backup."
        echo ""
        return
    fi
    
    # List all secrets (names only, not values for security)
    SECRET_LIST=$(az keyvault secret list \
        --vault-name "${KEY_VAULT_NAME}" \
        --output json 2>/dev/null || echo "[]")
    
    if [ -z "$SECRET_LIST" ] || [ "$SECRET_LIST" == "[]" ]; then
        echo -e "${YELLOW}No secrets found in Key Vault${NC}"
        echo ""
        return
    fi
    
    # Create secrets list (names only)
    echo "$SECRET_LIST" | jq -r '.[].name' > "${BACKUP_BASE_DIR}/secrets/secret-names.txt"
    
    # Export secret metadata (without values)
    echo "$SECRET_LIST" | jq '.' > "${BACKUP_BASE_DIR}/secrets/secret-metadata.json"
    
    # Create a script to retrieve secret values (requires manual execution)
    cat > "${BACKUP_BASE_DIR}/secrets/retrieve-secrets.sh" <<'EOF'
#!/bin/bash
# Script to retrieve Key Vault secret values
# WARNING: This will expose secret values. Use with caution.
# 
# Usage: ./retrieve-secrets.sh <key-vault-name>
#
set -euo pipefail

KEY_VAULT_NAME="${1:-sw-staging-kv}"

echo "Retrieving secrets from ${KEY_VAULT_NAME}..."
echo "WARNING: This will display secret values!"
echo ""

while IFS= read -r secret_name; do
    echo "Retrieving: ${secret_name}"
    az keyvault secret show \
        --vault-name "${KEY_VAULT_NAME}" \
        --name "${secret_name}" \
        --query "{name:name, value:value}" \
        --output json >> secrets-with-values.json 2>/dev/null || {
        echo "  Warning: Could not retrieve ${secret_name}"
    }
done < secret-names.txt

echo ""
echo "Secrets retrieved. Check secrets-with-values.json"
echo "WARNING: This file contains sensitive data. Secure it appropriately!"
EOF
    
    chmod +x "${BACKUP_BASE_DIR}/secrets/retrieve-secrets.sh"
    
    local secret_count=$(echo "$SECRET_LIST" | jq '. | length')
    echo -e "${GREEN}✓ Found ${secret_count} secrets in Key Vault${NC}"
    echo -e "${YELLOW}  Note: Secret values are not exported for security.${NC}"
    echo -e "${YELLOW}  Use retrieve-secrets.sh to get values if needed.${NC}"
    echo ""
}

# Function to backup connection strings
backup_connection_strings() {
    echo -e "${YELLOW}Backing up connection strings...${NC}"
    
    local connection_strings_file="${BACKUP_BASE_DIR}/connection-strings/connection-strings.json"
    
    # Initialize connection strings object
    cat > "${connection_strings_file}" <<EOF
{
  "backupDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "environment": "${ENVIRONMENT}",
  "connectionStrings": {
EOF
    
    # Get IoT Hub connection string
    echo -e "  Getting IoT Hub connection string..."
    IOT_HUB_NAME="sw-${ENVIRONMENT}-iothub"
    if az iot hub show --name "${IOT_HUB_NAME}" --resource-group "${RESOURCE_GROUP}" &>/dev/null; then
        IOT_HUB_CS=$(az iot hub connection-string show \
            --hub-name "${IOT_HUB_NAME}" \
            --resource-group "${RESOURCE_GROUP}" \
            --policy-name iothubowner \
            --query connectionString -o tsv 2>/dev/null || echo "")
        
        if [ -n "$IOT_HUB_CS" ]; then
            echo "    \"iotHub\": \"${IOT_HUB_CS}\"," >> "${connection_strings_file}"
            echo -e "    ${GREEN}✓ IoT Hub connection string retrieved${NC}"
        fi
    fi
    
    # Get Storage Account connection string
    echo -e "  Getting Storage Account connection string..."
    STORAGE_ACCOUNT_NAME="sw${ENVIRONMENT//-/}stg"
    if az storage account show --name "${STORAGE_ACCOUNT_NAME}" --resource-group "${RESOURCE_GROUP}" &>/dev/null; then
        STORAGE_CS=$(az storage account show-connection-string \
            --resource-group "${RESOURCE_GROUP}" \
            --name "${STORAGE_ACCOUNT_NAME}" \
            --query connectionString -o tsv 2>/dev/null || echo "")
        
        if [ -n "$STORAGE_CS" ]; then
            echo "    \"storageAccount\": \"${STORAGE_CS}\"," >> "${connection_strings_file}"
            echo -e "    ${GREEN}✓ Storage Account connection string retrieved${NC}"
        fi
    fi
    
    # Get Application Insights connection string
    echo -e "  Getting Application Insights connection string..."
    APP_INSIGHTS_NAME="sw-${ENVIRONMENT}-insights"
    if az monitor app-insights component show \
        --resource-group "${RESOURCE_GROUP}" \
        --app "${APP_INSIGHTS_NAME}" &>/dev/null; then
        APP_INSIGHTS_CS=$(az monitor app-insights component show \
            --resource-group "${RESOURCE_GROUP}" \
            --app "${APP_INSIGHTS_NAME}" \
            --query connectionString -o tsv 2>/dev/null || echo "")
        
        if [ -n "$APP_INSIGHTS_CS" ]; then
            echo "    \"applicationInsights\": \"${APP_INSIGHTS_CS}\"," >> "${connection_strings_file}"
            echo -e "    ${GREEN}✓ Application Insights connection string retrieved${NC}"
        fi
    fi
    
    # Get Static Web App deployment token
    echo -e "  Getting Static Web App deployment token..."
    STATIC_WEB_APP_NAME="sw-${ENVIRONMENT}-dashboard"
    if az staticwebapp show --name "${STATIC_WEB_APP_NAME}" --resource-group "${RESOURCE_GROUP}" &>/dev/null; then
        STATIC_WEB_TOKEN=$(az staticwebapp secrets list \
            --name "${STATIC_WEB_APP_NAME}" \
            --resource-group "${RESOURCE_GROUP}" \
            --query properties.apiKey -o tsv 2>/dev/null || echo "")
        
        if [ -n "$STATIC_WEB_TOKEN" ]; then
            echo "    \"staticWebAppToken\": \"${STATIC_WEB_TOKEN}\"," >> "${connection_strings_file}"
            echo -e "    ${GREEN}✓ Static Web App token retrieved${NC}"
        fi
    fi
    
    # Close JSON object
    sed -i '' '$ s/,$//' "${connection_strings_file}" 2>/dev/null || sed -i '$ s/,$//' "${connection_strings_file}"
    echo "  }" >> "${connection_strings_file}"
    echo "}" >> "${connection_strings_file}"
    
    # Format JSON
    jq '.' "${connection_strings_file}" > "${connection_strings_file}.tmp" && mv "${connection_strings_file}.tmp" "${connection_strings_file}" 2>/dev/null || true
    
    echo -e "${GREEN}✓ Connection strings backed up${NC}"
    echo ""
}

# Function to create configuration mapping document
create_configuration_mapping() {
    echo -e "${YELLOW}Creating configuration mapping document...${NC}"
    
    local mapping_file="${BACKUP_BASE_DIR}/configuration-mapping.md"
    
    cat > "${mapping_file}" <<EOF
# SmartWatts Configuration Mapping

**Environment**: ${ENVIRONMENT}  
**Backup Date**: $(date -u +%Y-%m-%dT%H:%M:%SZ)

## Overview

This document maps Azure configuration to GCP equivalents for migration purposes.

## Environment Variables

### Database Configuration
- \`POSTGRES_USER\`: PostgreSQL username
- \`POSTGRES_PASSWORD\`: PostgreSQL password (stored in Key Vault)
- \`POSTGRES_MULTIPLE_DATABASES\`: Comma-separated list of database names

**GCP Equivalent**: Cloud SQL PostgreSQL instance with multiple databases

### Redis Configuration
- \`REDIS_PASSWORD\`: Redis password (stored in Key Vault)

**GCP Equivalent**: Cloud Memorystore for Redis or Redis on Cloud Run

### Azure IoT Hub
- \`IOT_HUB_CONNECTION_STRING\`: IoT Hub connection string
- \`IOT_HUB_DEVICE_ID\`: Device identifier

**GCP Equivalent**: Cloud IoT Core or MQTT broker on Cloud Run

### Azure Storage
- \`STORAGE_CONNECTION_STRING\`: Storage account connection string
- \`AZURE_BLOB_CONTAINER\`: Blob container name

**GCP Equivalent**: Cloud Storage buckets

### Application Insights
- \`APP_INSIGHTS_CONNECTION_STRING\`: Application Insights connection string
- \`APP_INSIGHTS_INSTRUMENTATION_KEY\`: Instrumentation key

**GCP Equivalent**: Cloud Monitoring and Cloud Logging

## Connection Strings

See \`connection-strings/connection-strings.json\` for all connection strings.

## Secrets

See \`secrets/secret-names.txt\` for list of secrets in Key Vault.

**GCP Equivalent**: Secret Manager

## Application Configurations

All application.yml files are backed up in \`app-configs/\` directory.

## Migration Notes

1. **Database**: Migrate to Cloud SQL PostgreSQL
2. **Secrets**: Migrate to Secret Manager
3. **Storage**: Migrate to Cloud Storage
4. **Monitoring**: Migrate to Cloud Monitoring
5. **IoT**: Consider Cloud IoT Core or MQTT broker

EOF
    
    echo -e "${GREEN}✓ Configuration mapping document created${NC}"
    echo ""
}

# Function to create backup summary
create_backup_summary() {
    echo -e "${YELLOW}Creating backup summary...${NC}"
    
    local summary_file="${BACKUP_BASE_DIR}/backup-summary.json"
    
    local env_var_count=$(find "${BACKUP_BASE_DIR}/env-vars" -type f 2>/dev/null | wc -l | tr -d ' ')
    local config_count=$(find "${BACKUP_BASE_DIR}/app-configs" -type f 2>/dev/null | wc -l | tr -d ' ')
    local secret_count=$(cat "${BACKUP_BASE_DIR}/secrets/secret-names.txt" 2>/dev/null | wc -l | tr -d ' ' || echo "0")
    
    cat > "${summary_file}" <<EOF
{
  "backupDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "environment": "${ENVIRONMENT}",
  "resourceGroup": "${RESOURCE_GROUP}",
  "keyVaultName": "${KEY_VAULT_NAME}",
  "summary": {
    "environmentVariableFiles": ${env_var_count},
    "applicationConfigFiles": ${config_count},
    "secretsInKeyVault": ${secret_count},
    "connectionStrings": $(jq '.connectionStrings | length' "${BACKUP_BASE_DIR}/connection-strings/connection-strings.json" 2>/dev/null || echo "0")
  },
  "backupLocation": "${BACKUP_BASE_DIR}"
}
EOF
    
    echo -e "${GREEN}✓ Backup summary created${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting configuration backup...${NC}"
    echo ""
    
    check_prerequisites
    backup_env_variables
    backup_app_configurations
    backup_key_vault_secrets
    backup_connection_strings
    create_configuration_mapping
    create_backup_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Configuration backup completed!${NC}"
    echo -e "${GREEN}Backup directory: ${BACKUP_BASE_DIR}${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Review backed up configurations"
    echo "2. Verify all connection strings are captured"
    echo "3. If needed, run retrieve-secrets.sh to get secret values"
    echo "4. Use configuration-mapping.md for GCP migration planning"
}

# Run main function
main "$@"

