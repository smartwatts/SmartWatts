#!/bin/bash

###############################################################################
# Secret Manager Setup Script
# 
# Purpose: Migrate secrets from Azure Key Vault to GCP Secret Manager
#          Create secrets for database passwords, JWT keys, API keys, etc.
#
# Usage: ./setup-secrets.sh [azure-backup-path]
#   azure-backup-path: Path to Azure configuration backup (optional)
#
# Prerequisites:
#   - GCP projects created
#   - Secret Manager API enabled
#   - Azure backup files available (optional)
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
STAGING_PROJECT_ID="smartwatts-staging"
PRODUCTION_PROJECT_ID="smartwatts-production"
AZURE_BACKUP_PATH="${1:-gcp-migration/azure-backup/configuration-backups}"

# Secret names (standard naming convention)
SECRET_NAMES=(
    "postgres-password"
    "postgres-root-password"
    "redis-password"
    "jwt-secret-key"
    "jwt-refresh-secret-key"
    "sendgrid-api-key"
    "twilio-account-sid"
    "twilio-auth-token"
    "openweather-api-key"
    "iot-hub-connection-string"
    "storage-connection-string"
    "app-insights-connection-string"
)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Secret Manager Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! command -v gcloud &> /dev/null; then
        echo -e "${RED}Error: Google Cloud SDK (gcloud) not found${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Function to create secret
create_secret() {
    local project_id=$1
    local secret_name=$2
    local secret_value=$3
    
    echo -e "  Creating secret: ${secret_name}"
    
    # Check if secret already exists
    if gcloud secrets describe "${secret_name}" \
        --project="${project_id}" &>/dev/null; then
        echo -e "    ${YELLOW}Secret ${secret_name} already exists${NC}"
        read -p "    Update existing secret? (y/N): " update_secret
        if [ "${update_secret}" != "y" ] && [ "${update_secret}" != "Y" ]; then
            echo -e "    ${YELLOW}Skipping ${secret_name}${NC}"
            return 0
        fi
        
        # Delete existing secret version (create new version)
        echo "${secret_value}" | gcloud secrets versions add "${secret_name}" \
            --project="${project_id}" \
            --data-file=- &>/dev/null || {
            echo -e "    ${RED}Error: Failed to update secret ${secret_name}${NC}"
            return 1
        }
        echo -e "    ${GREEN}✓ Secret ${secret_name} updated${NC}"
        return 0
    fi
    
    # Create secret
    echo "${secret_value}" | gcloud secrets create "${secret_name}" \
        --project="${project_id}" \
        --data-file=- \
        --replication-policy="automatic" || {
        echo -e "    ${RED}Error: Failed to create secret ${secret_name}${NC}"
        return 1
    }
    
    echo -e "    ${GREEN}✓ Secret ${secret_name} created${NC}"
}

# Function to create secret from user input
create_secret_interactive() {
    local project_id=$1
    local secret_name=$2
    local secret_description=$3
    
    echo -e "${YELLOW}Creating secret: ${secret_name}${NC}"
    echo -e "  Description: ${secret_description}"
    
    # Prompt for secret value
    read -sp "  Enter value for ${secret_name}: " secret_value
    echo ""
    
    if [ -z "$secret_value" ]; then
        echo -e "    ${YELLOW}Skipping ${secret_name} (empty value)${NC}"
        return 0
    fi
    
    create_secret "${project_id}" "${secret_name}" "${secret_value}"
}

# Function to load secrets from Azure backup
load_secrets_from_azure() {
    local project_id=$1
    local environment=$2
    
    echo -e "${YELLOW}Loading secrets from Azure backup...${NC}"
    
    # Find latest backup
    local backup_dir=$(find "${AZURE_BACKUP_PATH}/${environment}" -type d -name "20*" | sort -r | head -1)
    
    if [ -z "$backup_dir" ]; then
        echo -e "${YELLOW}No Azure backup found, will create secrets interactively${NC}"
        return 1
    fi
    
    echo -e "  Found backup: ${backup_dir}"
    
    # Load connection strings
    local connection_strings_file="${backup_dir}/connection-strings/connection-strings.json"
    if [ -f "$connection_strings_file" ]; then
        # Extract and create secrets from connection strings
        if command -v jq &> /dev/null; then
            local iot_hub_cs=$(jq -r '.connectionStrings.iotHub // empty' "$connection_strings_file")
            local storage_cs=$(jq -r '.connectionStrings.storageAccount // empty' "$connection_strings_file")
            local app_insights_cs=$(jq -r '.connectionStrings.applicationInsights // empty' "$connection_strings_file")
            
            [ -n "$iot_hub_cs" ] && create_secret "${project_id}" "iot-hub-connection-string" "${iot_hub_cs}"
            [ -n "$storage_cs" ] && create_secret "${project_id}" "storage-connection-string" "${storage_cs}"
            [ -n "$app_insights_cs" ] && create_secret "${project_id}" "app-insights-connection-string" "${app_insights_cs}"
        fi
    fi
    
    # Load secrets from Key Vault backup (if available)
    local secrets_file="${backup_dir}/secrets/retrieve-secrets.sh"
    if [ -f "$secrets_file" ]; then
        echo -e "${YELLOW}  Note: Run retrieve-secrets.sh to get Key Vault secrets${NC}"
    fi
    
    return 0
}

# Function to setup secrets for environment
setup_environment_secrets() {
    local project_id=$1
    local environment=$2
    
    echo -e "${BLUE}Setting up secrets for ${environment} (${project_id})...${NC}"
    echo ""
    
    # Set project
    gcloud config set project "${project_id}"
    
    # Try to load from Azure backup
    if ! load_secrets_from_azure "${project_id}" "${environment}"; then
        echo -e "${YELLOW}Creating secrets interactively...${NC}"
        echo ""
    fi
    
    # Create required secrets interactively
    echo -e "${YELLOW}Creating required secrets...${NC}"
    echo ""
    
    create_secret_interactive "${project_id}" "postgres-password" "PostgreSQL database password"
    create_secret_interactive "${project_id}" "postgres-root-password" "PostgreSQL root password"
    create_secret_interactive "${project_id}" "redis-password" "Redis cache password"
    create_secret_interactive "${project_id}" "jwt-secret-key" "JWT token signing key"
    create_secret_interactive "${project_id}" "jwt-refresh-secret-key" "JWT refresh token signing key"
    create_secret_interactive "${project_id}" "sendgrid-api-key" "SendGrid API key for email"
    create_secret_interactive "${project_id}" "twilio-account-sid" "Twilio Account SID"
    create_secret_interactive "${project_id}" "twilio-auth-token" "Twilio Auth Token"
    create_secret_interactive "${project_id}" "openweather-api-key" "OpenWeatherMap API key"
    
    # Optional secrets (can be skipped)
    echo ""
    echo -e "${YELLOW}Optional secrets (press Enter to skip):${NC}"
    read -p "Create IoT Hub connection string? (y/N): " create_iot
    [ "${create_iot}" = "y" ] || [ "${create_iot}" = "Y" ] && \
        create_secret_interactive "${project_id}" "iot-hub-connection-string" "Azure IoT Hub connection string"
    
    read -p "Create storage connection string? (y/N): " create_storage
    [ "${create_storage}" = "y" ] || [ "${create_storage}" = "Y" ] && \
        create_secret_interactive "${project_id}" "storage-connection-string" "Azure Storage connection string"
    
    read -p "Create App Insights connection string? (y/N): " create_app_insights
    [ "${create_app_insights}" = "y" ] || [ "${create_app_insights}" = "Y" ] && \
        create_secret_interactive "${project_id}" "app-insights-connection-string" "Application Insights connection string"
    
    echo -e "${GREEN}✓ Secrets setup complete for ${environment}${NC}"
    echo ""
}

# Function to create secret summary
create_secret_summary() {
    echo -e "${YELLOW}Creating secret summary...${NC}"
    
    local summary_file="gcp-migration/gcp-setup/secret-manager-summary.json"
    
    # List secrets for each project
    local staging_secrets=$(gcloud secrets list --project="${STAGING_PROJECT_ID}" --format="value(name)" 2>/dev/null | jq -R . | jq -s . || echo "[]")
    local production_secrets=$(gcloud secrets list --project="${PRODUCTION_PROJECT_ID}" --format="value(name)" 2>/dev/null | jq -R . | jq -s . || echo "[]")
    
    cat > "${summary_file}" <<EOF
{
  "creationDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "secrets": {
    "staging": {
      "projectId": "${STAGING_PROJECT_ID}",
      "secretNames": ${staging_secrets}
    },
    "production": {
      "projectId": "${PRODUCTION_PROJECT_ID}",
      "secretNames": ${production_secrets}
    }
  },
  "secretNamingConvention": {
    "format": "lowercase-with-hyphens",
    "examples": [
      "postgres-password",
      "jwt-secret-key",
      "sendgrid-api-key"
    ]
  },
  "access": {
    "note": "Secrets are accessed via Secret Manager API or environment variables in Cloud Run",
    "cloudRunAccess": "Secrets can be mounted as environment variables or files in Cloud Run services"
  }
}
EOF
    
    echo -e "${GREEN}✓ Secret summary created: ${summary_file}${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting Secret Manager setup...${NC}"
    echo ""
    
    check_prerequisites
    
    # Setup staging secrets
    setup_environment_secrets "${STAGING_PROJECT_ID}" "staging"
    
    # Setup production secrets
    setup_environment_secrets "${PRODUCTION_PROJECT_ID}" "production"
    
    # Create summary
    create_secret_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Secret Manager setup completed!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Verify secrets in GCP Console: Secret Manager"
    echo "2. Update Cloud Run service configurations to reference secrets"
    echo "3. Test secret access from Cloud Run services"
    echo "4. Document secret usage in application configurations"
    echo ""
    echo "Security Notes:"
    echo "- Secrets are stored encrypted in Secret Manager"
    echo "- Access is controlled via IAM roles"
    echo "- Secrets can be versioned and rotated"
    echo "- Never commit secret values to version control"
}

# Run main function
main "$@"

