#!/bin/bash

###############################################################################
# Azure ARM Template Export Script
# 
# Purpose: Export all Azure Resource Manager (ARM) templates and Bicep files
#          for backup before GCP migration
#
# Usage: ./export-arm-templates.sh [environment]
#   environment: staging (default) or production
#
# Prerequisites:
#   - Azure CLI installed and configured
#   - Logged in to Azure (az login)
#   - Appropriate permissions to read resources
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
EXPORT_DIR="gcp-migration/azure-backup/exports/${ENVIRONMENT}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
EXPORT_BASE_DIR="${EXPORT_DIR}/${TIMESTAMP}"

# Create export directory
mkdir -p "${EXPORT_BASE_DIR}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Azure ARM Template Export${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Resource Group: ${RESOURCE_GROUP}${NC}"
echo -e "${BLUE}Export Directory: ${EXPORT_BASE_DIR}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check if resource group exists
check_resource_group() {
    echo -e "${YELLOW}Checking resource group existence...${NC}"
    if ! az group show --name "${RESOURCE_GROUP}" &>/dev/null; then
        echo -e "${RED}Error: Resource group '${RESOURCE_GROUP}' does not exist${NC}"
        echo "Please verify the resource group name and your Azure subscription."
        exit 1
    fi
    echo -e "${GREEN}✓ Resource group found${NC}"
    echo ""
}

# Function to export resource group template
export_resource_group_template() {
    echo -e "${YELLOW}Exporting resource group template...${NC}"
    az group export \
        --name "${RESOURCE_GROUP}" \
        --output-file "${EXPORT_BASE_DIR}/resource-group-template.json" \
        --skip-resource-name-params \
        --skip-all-params || {
        echo -e "${RED}Warning: Failed to export resource group template${NC}"
        echo "This may be normal if resources were created manually."
    }
    echo -e "${GREEN}✓ Resource group template exported${NC}"
    echo ""
}

# Function to export individual resource templates
export_individual_resources() {
    echo -e "${YELLOW}Exporting individual resource templates...${NC}"
    
    # Get all resources in the resource group
    RESOURCES=$(az resource list --resource-group "${RESOURCE_GROUP}" --output json)
    
    if [ -z "$RESOURCES" ] || [ "$RESOURCES" == "[]" ]; then
        echo -e "${YELLOW}No resources found in resource group${NC}"
        return
    fi
    
    # Create resources directory
    mkdir -p "${EXPORT_BASE_DIR}/resources"
    
    # Export each resource
    echo "$RESOURCES" | jq -r '.[] | "\(.type)|\(.name)"' | while IFS='|' read -r resource_type resource_name; do
        echo -e "  Exporting: ${resource_name} (${resource_type})"
        
        # Convert resource type to directory structure
        RESOURCE_DIR=$(echo "${resource_type}" | tr '/' '_')
        mkdir -p "${EXPORT_BASE_DIR}/resources/${RESOURCE_DIR}"
        
        # Export resource template
        az resource show \
            --resource-group "${RESOURCE_GROUP}" \
            --resource-type "${resource_type}" \
            --name "${resource_name}" \
            --output json > "${EXPORT_BASE_DIR}/resources/${RESOURCE_DIR}/${resource_name}.json" 2>/dev/null || {
            echo -e "    ${YELLOW}Warning: Could not export ${resource_name}${NC}"
        }
    done
    
    echo -e "${GREEN}✓ Individual resource templates exported${NC}"
    echo ""
}

# Function to export Bicep templates
export_bicep_templates() {
    echo -e "${YELLOW}Exporting Bicep templates...${NC}"
    
    BICEP_DIR="infrastructure/bicep"
    if [ -d "$BICEP_DIR" ]; then
        mkdir -p "${EXPORT_BASE_DIR}/bicep"
        cp -r "${BICEP_DIR}"/* "${EXPORT_BASE_DIR}/bicep/" 2>/dev/null || true
        echo -e "${GREEN}✓ Bicep templates copied${NC}"
    else
        echo -e "${YELLOW}Warning: Bicep directory not found${NC}"
    fi
    echo ""
}

# Function to export network security groups
export_network_security_groups() {
    echo -e "${YELLOW}Exporting Network Security Groups...${NC}"
    
    NSG_LIST=$(az network nsg list --resource-group "${RESOURCE_GROUP}" --output json)
    
    if [ -n "$NSG_LIST" ] && [ "$NSG_LIST" != "[]" ]; then
        mkdir -p "${EXPORT_BASE_DIR}/network-security-groups"
        
        echo "$NSG_LIST" | jq -r '.[].name' | while read -r nsg_name; do
            echo -e "  Exporting NSG: ${nsg_name}"
            
            # Export NSG rules
            az network nsg rule list \
                --resource-group "${RESOURCE_GROUP}" \
                --nsg-name "${nsg_name}" \
                --output json > "${EXPORT_BASE_DIR}/network-security-groups/${nsg_name}-rules.json" 2>/dev/null || {
                echo -e "    ${YELLOW}Warning: Could not export NSG rules for ${nsg_name}${NC}"
            }
            
            # Export NSG configuration
            az network nsg show \
                --resource-group "${RESOURCE_GROUP}" \
                --name "${nsg_name}" \
                --output json > "${EXPORT_BASE_DIR}/network-security-groups/${nsg_name}.json" 2>/dev/null || {
                echo -e "    ${YELLOW}Warning: Could not export NSG ${nsg_name}${NC}"
            }
        done
        
        echo -e "${GREEN}✓ Network Security Groups exported${NC}"
    else
        echo -e "${YELLOW}No Network Security Groups found${NC}"
    fi
    echo ""
}

# Function to export Application Insights configuration
export_application_insights() {
    echo -e "${YELLOW}Exporting Application Insights configuration...${NC}"
    
    APP_INSIGHTS_LIST=$(az monitor app-insights component show \
        --resource-group "${RESOURCE_GROUP}" \
        --app "sw-${ENVIRONMENT}-insights" \
        --output json 2>/dev/null || echo "[]")
    
    if [ -n "$APP_INSIGHTS_LIST" ] && [ "$APP_INSIGHTS_LIST" != "[]" ]; then
        mkdir -p "${EXPORT_BASE_DIR}/application-insights"
        
        echo "$APP_INSIGHTS_LIST" | jq '.' > "${EXPORT_BASE_DIR}/application-insights/sw-${ENVIRONMENT}-insights.json" 2>/dev/null || {
            echo -e "${YELLOW}Warning: Could not export Application Insights${NC}"
        }
        
        # Export connection string and instrumentation key
        CONNECTION_STRING=$(echo "$APP_INSIGHTS_LIST" | jq -r '.connectionString // empty')
        INSTRUMENTATION_KEY=$(echo "$APP_INSIGHTS_LIST" | jq -r '.instrumentationKey // empty')
        
        if [ -n "$CONNECTION_STRING" ]; then
            echo "{\"connectionString\": \"${CONNECTION_STRING}\", \"instrumentationKey\": \"${INSTRUMENTATION_KEY}\"}" | jq '.' > \
                "${EXPORT_BASE_DIR}/application-insights/connection-info.json"
        fi
        
        echo -e "${GREEN}✓ Application Insights configuration exported${NC}"
    else
        echo -e "${YELLOW}No Application Insights found${NC}"
    fi
    echo ""
}

# Function to export IoT Hub configuration
export_iot_hub() {
    echo -e "${YELLOW}Exporting IoT Hub configuration...${NC}"
    
    IOT_HUB_NAME="sw-${ENVIRONMENT}-iothub"
    
    if az iot hub show --name "${IOT_HUB_NAME}" --resource-group "${RESOURCE_GROUP}" &>/dev/null; then
        mkdir -p "${EXPORT_BASE_DIR}/iot-hub"
        
        # Export IoT Hub configuration
        az iot hub show \
            --name "${IOT_HUB_NAME}" \
            --resource-group "${RESOURCE_GROUP}" \
            --output json > "${EXPORT_BASE_DIR}/iot-hub/hub-config.json" 2>/dev/null || {
            echo -e "${YELLOW}Warning: Could not export IoT Hub configuration${NC}"
        }
        
        # Export device identities
        az iot hub device-identity list \
            --hub-name "${IOT_HUB_NAME}" \
            --resource-group "${RESOURCE_GROUP}" \
            --output json > "${EXPORT_BASE_DIR}/iot-hub/devices.json" 2>/dev/null || {
            echo -e "${YELLOW}Warning: Could not export device identities${NC}"
        }
        
        echo -e "${GREEN}✓ IoT Hub configuration exported${NC}"
    else
        echo -e "${YELLOW}IoT Hub not found${NC}"
    fi
    echo ""
}

# Function to export storage account configuration
export_storage_account() {
    echo -e "${YELLOW}Exporting Storage Account configuration...${NC}"
    
    STORAGE_ACCOUNT_NAME="sw${ENVIRONMENT//-/}stg"
    
    if az storage account show --name "${STORAGE_ACCOUNT_NAME}" --resource-group "${RESOURCE_GROUP}" &>/dev/null; then
        mkdir -p "${EXPORT_BASE_DIR}/storage-account"
        
        # Export storage account configuration
        az storage account show \
            --name "${STORAGE_ACCOUNT_NAME}" \
            --resource-group "${RESOURCE_GROUP}" \
            --output json > "${EXPORT_BASE_DIR}/storage-account/account-config.json" 2>/dev/null || {
            echo -e "${YELLOW}Warning: Could not export storage account configuration${NC}"
        }
        
        # Export container list
        STORAGE_KEY=$(az storage account keys list \
            --resource-group "${RESOURCE_GROUP}" \
            --account-name "${STORAGE_ACCOUNT_NAME}" \
            --query '[0].value' -o tsv)
        
        if [ -n "$STORAGE_KEY" ]; then
            az storage container list \
                --account-name "${STORAGE_ACCOUNT_NAME}" \
                --account-key "${STORAGE_KEY}" \
                --output json > "${EXPORT_BASE_DIR}/storage-account/containers.json" 2>/dev/null || {
                echo -e "${YELLOW}Warning: Could not export container list${NC}"
            }
        fi
        
        echo -e "${GREEN}✓ Storage Account configuration exported${NC}"
    else
        echo -e "${YELLOW}Storage Account not found${NC}"
    fi
    echo ""
}

# Function to create export summary
create_export_summary() {
    echo -e "${YELLOW}Creating export summary...${NC}"
    
    SUMMARY_FILE="${EXPORT_BASE_DIR}/export-summary.json"
    
    cat > "${SUMMARY_FILE}" <<EOF
{
  "exportDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "environment": "${ENVIRONMENT}",
  "resourceGroup": "${RESOURCE_GROUP}",
  "subscriptionId": "$(az account show --query id -o tsv)",
  "subscriptionName": "$(az account show --query name -o tsv)",
  "exportedResources": {
    "resourceGroupTemplate": "$([ -f "${EXPORT_BASE_DIR}/resource-group-template.json" ] && echo "yes" || echo "no")",
    "individualResources": "$([ -d "${EXPORT_BASE_DIR}/resources" ] && echo "yes" || echo "no")",
    "bicepTemplates": "$([ -d "${EXPORT_BASE_DIR}/bicep" ] && echo "yes" || echo "no")",
    "networkSecurityGroups": "$([ -d "${EXPORT_BASE_DIR}/network-security-groups" ] && echo "yes" || echo "no")",
    "applicationInsights": "$([ -d "${EXPORT_BASE_DIR}/application-insights" ] && echo "yes" || echo "no")",
    "iotHub": "$([ -d "${EXPORT_BASE_DIR}/iot-hub" ] && echo "yes" || echo "no")",
    "storageAccount": "$([ -d "${EXPORT_BASE_DIR}/storage-account" ] && echo "yes" || echo "no")"
  }
}
EOF
    
    echo -e "${GREEN}✓ Export summary created${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting ARM template export...${NC}"
    echo ""
    
    # Check prerequisites
    if ! command -v az &> /dev/null; then
        echo -e "${RED}Error: Azure CLI not found. Please install Azure CLI.${NC}"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}Error: jq not found. Please install jq for JSON processing.${NC}"
        exit 1
    fi
    
    # Check Azure login
    if ! az account show &>/dev/null; then
        echo -e "${RED}Error: Not logged in to Azure. Please run 'az login'${NC}"
        exit 1
    fi
    
    # Execute export functions
    check_resource_group
    export_resource_group_template
    export_individual_resources
    export_bicep_templates
    export_network_security_groups
    export_application_insights
    export_iot_hub
    export_storage_account
    create_export_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Export completed successfully!${NC}"
    echo -e "${GREEN}Export directory: ${EXPORT_BASE_DIR}${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Review exported templates in ${EXPORT_BASE_DIR}"
    echo "2. Verify all resources were exported correctly"
    echo "3. Store exports in a secure location"
    echo "4. Use these templates for Azure restoration if needed"
}

# Run main function
main "$@"

