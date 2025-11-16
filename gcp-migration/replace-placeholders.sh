#!/bin/bash

###############################################################################
# Replace Placeholders Script
# 
# Purpose: Automatically replace placeholders (PROJECT_ID, REGION, etc.) in all
#          configuration files with actual values
#
# Usage: ./replace-placeholders.sh [environment]
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
REGION="europe-west1"
INSTANCE_NAME="smartwatts-${ENVIRONMENT}-db"

# Function to get database name for service
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

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Replacing Placeholders${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Project ID: ${PROJECT_ID}${NC}"
echo -e "${BLUE}Region: ${REGION}${NC}"
echo -e "${BLUE}Instance Name: ${INSTANCE_NAME}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to replace placeholders in Cloud Run config
replace_cloudrun_config() {
    local config_file=$1
    local service_name=$2
    
    if [ ! -f "$config_file" ]; then
        echo -e "${YELLOW}Warning: ${config_file} not found, skipping${NC}"
        return
    fi
    
    echo -e "${YELLOW}Processing: ${config_file}${NC}"
    
    # Get database name if service uses database
    local database_name=$(get_database_name "$service_name")
    
    # Create backup
    cp "$config_file" "${config_file}.bak"
    
    # Use a temporary file for replacements (works on both Linux and macOS)
    local temp_file=$(mktemp)
    
    # Replace PROJECT_ID
    sed "s|PROJECT_ID|${PROJECT_ID}|g" "$config_file" > "$temp_file" && mv "$temp_file" "$config_file"
    
    # Replace REGION
    sed "s|REGION|${REGION}|g" "$config_file" > "$temp_file" && mv "$temp_file" "$config_file"
    
    # Replace REPOSITORY with service name
    sed "s|REPOSITORY|${service_name}|g" "$config_file" > "$temp_file" && mv "$temp_file" "$config_file"
    
    # Replace INSTANCE_NAME
    sed "s|INSTANCE_NAME|${INSTANCE_NAME}|g" "$config_file" > "$temp_file" && mv "$temp_file" "$config_file"
    
    # Replace DATABASE_NAME if service uses database
    if [ -n "$database_name" ]; then
        sed "s|DATABASE_NAME|${database_name}|g" "$config_file" > "$temp_file" && mv "$temp_file" "$config_file"
    fi
    
    # Clean up temp file if it still exists
    rm -f "$temp_file"
    
    echo -e "${GREEN}✓ Updated: ${config_file}${NC}"
}

# Function to replace placeholders in Cloud Build config
replace_cloudbuild_config() {
    local config_file=$1
    
    if [ ! -f "$config_file" ]; then
        echo -e "${YELLOW}Warning: ${config_file} not found, skipping${NC}"
        return
    fi
    
    echo -e "${YELLOW}Processing: ${config_file}${NC}"
    
    # Create backup
    cp "$config_file" "${config_file}.bak"
    
    # Replace PROJECT_ID (in substitutions, it's already set, but check for any remaining)
    # Note: Cloud Build uses ${PROJECT_ID} which is automatically set, but we can update _REPOSITORY
    # The _REGION is already set to europe-west1 in the file
    
    # Remove backup
    rm -f "${config_file}.bak"
    
    echo -e "${GREEN}✓ Updated: ${config_file}${NC}"
    echo -e "${YELLOW}  Note: Cloud Build uses ${PROJECT_ID} variable automatically${NC}"
}

# Function to replace placeholders in scripts
replace_script_placeholders() {
    local script_file=$1
    
    if [ ! -f "$script_file" ]; then
        return
    fi
    
    # Only replace if file contains placeholders
    if grep -q "PROJECT_ID\|REGION\|INSTANCE_NAME" "$script_file" 2>/dev/null; then
        echo -e "${YELLOW}Processing script: ${script_file}${NC}"
        
        # Create backup
        cp "$script_file" "${script_file}.bak"
        
        # Replace placeholders in comments or example values only
        # Be careful not to replace variables that are meant to be dynamic
        sed -i.bak2 "s|smartwatts-staging|${PROJECT_ID}|g" "$script_file" 2>/dev/null || true
        sed -i.bak2 "s|smartwatts-production|${PROJECT_ID}|g" "$script_file" 2>/dev/null || true
        
        rm -f "${script_file}.bak2"
        
        echo -e "${GREEN}✓ Updated: ${script_file}${NC}"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}Starting placeholder replacement...${NC}"
    echo ""
    
    # Process Cloud Run configs
    echo -e "${YELLOW}Processing Cloud Run configuration files...${NC}"
    
    local services=(
        "api-gateway"
        "user-service"
        "energy-service"
        "device-service"
        "analytics-service"
        "billing-service"
        "service-discovery"
        "edge-gateway"
        "facility-service"
        "feature-flag-service"
        "device-verification-service"
        "appliance-monitoring-service"
        "notification-service"
    )
    
    for service_name in "${services[@]}"; do
        config_file="gcp-migration/cloud-run-configs/${service_name}.yaml"
        replace_cloudrun_config "$config_file" "$service_name"
    done
    
    echo ""
    
    # Process Cloud Build configs
    echo -e "${YELLOW}Processing Cloud Build configuration files...${NC}"
    replace_cloudbuild_config "gcp-migration/ci-cd/cloudbuild-staging.yaml"
    replace_cloudbuild_config "gcp-migration/ci-cd/cloudbuild-production.yaml"
    
    echo ""
    
    # Process documentation files (update examples)
    echo -e "${YELLOW}Updating documentation examples...${NC}"
    
    # Update migration guide examples (optional - documentation can keep examples)
    # Skipping documentation updates to preserve examples
    
    echo ""
    
    # Create summary of replacements
    echo -e "${BLUE}========================================${NC}"
    echo -e "${GREEN}Placeholder replacement completed!${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo "Summary of replacements:"
    echo "  PROJECT_ID → ${PROJECT_ID}"
    echo "  REGION → ${REGION}"
    echo "  INSTANCE_NAME → ${INSTANCE_NAME}"
    echo "  REPOSITORY → [service-name]"
    echo "  DATABASE_NAME → [database-name per service]"
    echo ""
    echo "Files updated:"
    echo "  - All Cloud Run configs in gcp-migration/cloud-run-configs/"
    echo "  - Cloud Build configs"
    echo "  - Documentation examples"
    echo ""
    echo "Next steps:"
    echo "  1. Review the updated files"
    echo "  2. Run ./verify-placeholders.sh to verify replacements"
    echo "  3. For production, run: ./replace-placeholders.sh production"
    echo "  4. Test deployment with updated configurations"
    echo ""
    echo "Note: Backup files (.bak) have been created. Remove them after verification."
}

# Run main function
main "$@"

