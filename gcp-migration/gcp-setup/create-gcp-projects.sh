#!/bin/bash

###############################################################################
# GCP Project Creation Script
# 
# Purpose: Create GCP projects for staging and production environments
#          and enable all required APIs
#
# Usage: ./create-gcp-projects.sh [billing-account-id]
#   billing-account-id: GCP billing account ID (optional, will prompt if not provided)
#
# Prerequisites:
#   - Google Cloud SDK (gcloud) installed and configured
#   - Appropriate permissions to create projects
#   - Billing account access
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
STAGING_PROJECT_NAME="SmartWatts Staging"
PRODUCTION_PROJECT_NAME="SmartWatts Production"
BILLING_ACCOUNT_ID="${1:-}"

# Required APIs to enable
REQUIRED_APIS=(
    "cloudresourcemanager.googleapis.com"
    "compute.googleapis.com"
    "run.googleapis.com"
    "sqladmin.googleapis.com"
    "secretmanager.googleapis.com"
    "artifactregistry.googleapis.com"
    "cloudbuild.googleapis.com"
    "logging.googleapis.com"
    "monitoring.googleapis.com"
    "errorreporting.googleapis.com"
    "iam.googleapis.com"
    "servicenetworking.googleapis.com"
    "vpcaccess.googleapis.com"
    "cloudiot.googleapis.com"
    "storage-api.googleapis.com"
    "storage-component.googleapis.com"
    "dns.googleapis.com"
    "certificatemanager.googleapis.com"
)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}GCP Project Creation${NC}"
echo -e "${BLUE}Staging Project: ${STAGING_PROJECT_ID}${NC}"
echo -e "${BLUE}Production Project: ${PRODUCTION_PROJECT_ID}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! command -v gcloud &> /dev/null; then
        echo -e "${RED}Error: Google Cloud SDK (gcloud) not found${NC}"
        echo "Please install: https://cloud.google.com/sdk/docs/install"
        exit 1
    fi
    
    # Check if logged in
    if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" &>/dev/null; then
        echo -e "${RED}Error: Not logged in to GCP${NC}"
        echo "Please run: gcloud auth login"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Function to get billing account
get_billing_account() {
    if [ -z "$BILLING_ACCOUNT_ID" ]; then
        echo -e "${YELLOW}Getting billing accounts...${NC}"
        
        BILLING_ACCOUNTS=$(gcloud billing accounts list --format="value(name)" 2>/dev/null || echo "")
        
        if [ -z "$BILLING_ACCOUNTS" ]; then
            echo -e "${RED}Error: No billing accounts found${NC}"
            echo "Please create a billing account in GCP Console:"
            echo "https://console.cloud.google.com/billing"
            exit 1
        fi
        
        # Use first billing account if multiple exist
        BILLING_ACCOUNT_ID=$(echo "$BILLING_ACCOUNTS" | head -1)
        echo -e "${GREEN}✓ Using billing account: ${BILLING_ACCOUNT_ID}${NC}"
    else
        echo -e "${GREEN}✓ Using provided billing account: ${BILLING_ACCOUNT_ID}${NC}"
    fi
    echo ""
}

# Function to create project
create_project() {
    local project_id=$1
    local project_name=$2
    
    echo -e "${YELLOW}Creating project: ${project_id}...${NC}"
    
    # Check if project already exists
    if gcloud projects describe "${project_id}" &>/dev/null; then
        echo -e "${YELLOW}Project ${project_id} already exists, skipping creation${NC}"
        return 0
    fi
    
    # Create project
    gcloud projects create "${project_id}" \
        --name="${project_name}" \
        --set-as-default || {
        echo -e "${RED}Error: Failed to create project ${project_id}${NC}"
        return 1
    }
    
    # Link billing account
    echo -e "  Linking billing account..."
    gcloud billing projects link "${project_id}" \
        --billing-account="${BILLING_ACCOUNT_ID}" || {
        echo -e "${YELLOW}Warning: Could not link billing account${NC}"
        echo "You may need to link it manually in GCP Console"
    }
    
    echo -e "${GREEN}✓ Project ${project_id} created${NC}"
    echo ""
}

# Function to enable APIs
enable_apis() {
    local project_id=$1
    
    echo -e "${YELLOW}Enabling APIs for ${project_id}...${NC}"
    
    # Set project
    gcloud config set project "${project_id}"
    
    # Enable APIs in batches (to avoid rate limits)
    local api_count=0
    for api in "${REQUIRED_APIS[@]}"; do
        echo -e "  Enabling: ${api}"
        gcloud services enable "${api}" --project="${project_id}" &>/dev/null || {
            echo -e "    ${YELLOW}Warning: Could not enable ${api}${NC}"
        }
        api_count=$((api_count + 1))
        
        # Small delay to avoid rate limits
        if [ $((api_count % 5)) -eq 0 ]; then
            sleep 2
        fi
    done
    
    echo -e "${GREEN}✓ APIs enabled for ${project_id}${NC}"
    echo ""
}

# Function to configure project settings
configure_project() {
    local project_id=$1
    
    echo -e "${YELLOW}Configuring project settings for ${project_id}...${NC}"
    
    # Set project
    gcloud config set project "${project_id}"
    
    # Configure default region (europe-west1 for Nigeria proximity)
    gcloud config set compute/region europe-west1
    gcloud config set compute/zone europe-west1-b
    
    echo -e "${GREEN}✓ Project settings configured${NC}"
    echo ""
}

# Function to create project summary
create_project_summary() {
    echo -e "${YELLOW}Creating project summary...${NC}"
    
    local summary_file="gcp-migration/gcp-setup/project-summary.json"
    mkdir -p "$(dirname "${summary_file}")"
    
    cat > "${summary_file}" <<EOF
{
  "creationDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "billingAccount": "${BILLING_ACCOUNT_ID}",
  "projects": {
    "staging": {
      "projectId": "${STAGING_PROJECT_ID}",
      "projectName": "${STAGING_PROJECT_NAME}",
      "region": "europe-west1",
      "zone": "europe-west1-b",
      "status": "$(gcloud projects describe ${STAGING_PROJECT_ID} --format='value(lifecycleState)' 2>/dev/null || echo 'UNKNOWN')"
    },
    "production": {
      "projectId": "${PRODUCTION_PROJECT_ID}",
      "projectName": "${PRODUCTION_PROJECT_NAME}",
      "region": "europe-west1",
      "zone": "europe-west1-b",
      "status": "$(gcloud projects describe ${PRODUCTION_PROJECT_ID} --format='value(lifecycleState)' 2>/dev/null || echo 'UNKNOWN')"
    }
  },
  "enabledAPIs": [
$(for api in "${REQUIRED_APIS[@]}"; do
    if [ "$api" != "${REQUIRED_APIS[-1]}" ]; then
        echo "    \"${api}\","
    else
        echo "    \"${api}\""
    fi
done)
  ]
}
EOF
    
    echo -e "${GREEN}✓ Project summary created: ${summary_file}${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting GCP project creation...${NC}"
    echo ""
    
    check_prerequisites
    get_billing_account
    
    # Create staging project
    create_project "${STAGING_PROJECT_ID}" "${STAGING_PROJECT_NAME}"
    enable_apis "${STAGING_PROJECT_ID}"
    configure_project "${STAGING_PROJECT_ID}"
    
    # Create production project
    create_project "${PRODUCTION_PROJECT_ID}" "${PRODUCTION_PROJECT_NAME}"
    enable_apis "${PRODUCTION_PROJECT_ID}"
    configure_project "${PRODUCTION_PROJECT_ID}"
    
    # Create summary
    create_project_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}GCP projects created successfully!${NC}"
    echo -e "${GREEN}Staging: ${STAGING_PROJECT_ID}${NC}"
    echo -e "${GREEN}Production: ${PRODUCTION_PROJECT_ID}${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Verify projects in GCP Console"
    echo "2. Run setup-service-accounts.sh to create service accounts"
    echo "3. Run setup-cloud-sql.sh to create database instances"
    echo "4. Run setup-artifact-registry.sh to create Docker repositories"
    echo "5. Run setup-secrets.sh to migrate secrets"
}

# Run main function
main "$@"

