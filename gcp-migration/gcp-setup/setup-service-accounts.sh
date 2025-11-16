#!/bin/bash

###############################################################################
# GCP Service Account Setup Script
# 
# Purpose: Create service accounts for staging and production environments
#          with appropriate IAM roles for Cloud Run, Cloud SQL, Secret Manager, etc.
#
# Usage: ./setup-service-accounts.sh
#
# Prerequisites:
#   - GCP projects created (run create-gcp-projects.sh first)
#   - Google Cloud SDK (gcloud) installed and configured
#   - Appropriate permissions to create service accounts and assign roles
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

# Service account names
CLOUD_RUN_SA="cloud-run-sa"
CLOUD_SQL_SA="cloud-sql-sa"
CLOUD_BUILD_SA="cloud-build-sa"
SECRET_MANAGER_SA="secret-manager-sa"

# IAM roles for Cloud Run service account
CLOUD_RUN_ROLES=(
    "roles/run.invoker"
    "roles/secretmanager.secretAccessor"
    "roles/cloudsql.client"
    "roles/storage.objectViewer"
    "roles/logging.logWriter"
    "roles/monitoring.metricWriter"
    "roles/errorreporting.writer"
)

# IAM roles for Cloud SQL service account
CLOUD_SQL_ROLES=(
    "roles/cloudsql.client"
    "roles/secretmanager.secretAccessor"
)

# IAM roles for Cloud Build service account
CLOUD_BUILD_ROLES=(
    "roles/run.admin"
    "roles/iam.serviceAccountUser"
    "roles/artifactregistry.writer"
    "roles/secretmanager.secretAccessor"
    "roles/cloudsql.client"
    "roles/storage.admin"
)

# IAM roles for Secret Manager service account
SECRET_MANAGER_ROLES=(
    "roles/secretmanager.secretAccessor"
    "roles/secretmanager.viewer"
)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}GCP Service Account Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! command -v gcloud &> /dev/null; then
        echo -e "${RED}Error: Google Cloud SDK (gcloud) not found${NC}"
        exit 1
    fi
    
    # Verify projects exist
    if ! gcloud projects describe "${STAGING_PROJECT_ID}" &>/dev/null; then
        echo -e "${RED}Error: Staging project ${STAGING_PROJECT_ID} not found${NC}"
        echo "Please run create-gcp-projects.sh first"
        exit 1
    fi
    
    if ! gcloud projects describe "${PRODUCTION_PROJECT_ID}" &>/dev/null; then
        echo -e "${RED}Error: Production project ${PRODUCTION_PROJECT_ID} not found${NC}"
        echo "Please run create-gcp-projects.sh first"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Prerequisites checked${NC}"
    echo ""
}

# Function to create service account
create_service_account() {
    local project_id=$1
    local sa_name=$2
    local sa_display_name=$3
    
    echo -e "${YELLOW}Creating service account: ${sa_name} in ${project_id}...${NC}"
    
    # Check if service account already exists
    if gcloud iam service-accounts describe "${sa_name}@${project_id}.iam.gserviceaccount.com" \
        --project="${project_id}" &>/dev/null; then
        echo -e "${YELLOW}Service account ${sa_name} already exists, skipping creation${NC}"
        return 0
    fi
    
    # Create service account
    gcloud iam service-accounts create "${sa_name}" \
        --project="${project_id}" \
        --display-name="${sa_display_name}" \
        --description="Service account for ${sa_display_name}" || {
        echo -e "${RED}Error: Failed to create service account ${sa_name}${NC}"
        return 1
    }
    
    echo -e "${GREEN}✓ Service account ${sa_name} created${NC}"
    echo ""
}

# Function to grant IAM roles
grant_iam_roles() {
    local project_id=$1
    local sa_name=$2
    local sa_email="${sa_name}@${project_id}.iam.gserviceaccount.com"
    shift 2
    local roles=("$@")
    
    echo -e "${YELLOW}Granting IAM roles to ${sa_name}...${NC}"
    
    for role in "${roles[@]}"; do
        echo -e "  Granting: ${role}"
        gcloud projects add-iam-policy-binding "${project_id}" \
            --member="serviceAccount:${sa_email}" \
            --role="${role}" \
            --condition=None &>/dev/null || {
            echo -e "    ${YELLOW}Warning: Could not grant role ${role}${NC}"
        }
    done
    
    echo -e "${GREEN}✓ IAM roles granted to ${sa_name}${NC}"
    echo ""
}

# Function to create service account key
create_service_account_key() {
    local project_id=$1
    local sa_name=$2
    local sa_email="${sa_name}@${project_id}.iam.gserviceaccount.com"
    local key_dir="gcp-migration/gcp-setup/service-account-keys/${project_id}"
    
    echo -e "${YELLOW}Creating service account key for ${sa_name}...${NC}"
    
    mkdir -p "${key_dir}"
    
    # Create key
    gcloud iam service-accounts keys create "${key_dir}/${sa_name}-key.json" \
        --iam-account="${sa_email}" \
        --project="${project_id}" || {
        echo -e "${YELLOW}Warning: Could not create service account key${NC}"
        echo "You may need to create it manually or use Workload Identity instead"
        return 1
    }
    
    # Secure the key file
    chmod 600 "${key_dir}/${sa_name}-key.json"
    
    echo -e "${GREEN}✓ Service account key created: ${key_dir}/${sa_name}-key.json${NC}"
    echo -e "${YELLOW}  WARNING: Keep this key file secure! Do not commit to version control.${NC}"
    echo ""
}

# Function to setup service accounts for environment
setup_environment_service_accounts() {
    local project_id=$1
    local environment=$2
    
    echo -e "${BLUE}Setting up service accounts for ${environment} (${project_id})...${NC}"
    echo ""
    
    # Set project
    gcloud config set project "${project_id}"
    
    # Create Cloud Run service account
    create_service_account "${project_id}" "${CLOUD_RUN_SA}" "Cloud Run Service Account"
    grant_iam_roles "${project_id}" "${CLOUD_RUN_SA}" "${CLOUD_RUN_ROLES[@]}"
    create_service_account_key "${project_id}" "${CLOUD_RUN_SA}"
    
    # Create Cloud SQL service account
    create_service_account "${project_id}" "${CLOUD_SQL_SA}" "Cloud SQL Service Account"
    grant_iam_roles "${project_id}" "${CLOUD_SQL_SA}" "${CLOUD_SQL_ROLES[@]}"
    
    # Create Cloud Build service account
    create_service_account "${project_id}" "${CLOUD_BUILD_SA}" "Cloud Build Service Account"
    grant_iam_roles "${project_id}" "${CLOUD_BUILD_SA}" "${CLOUD_BUILD_ROLES[@]}"
    
    # Create Secret Manager service account
    create_service_account "${project_id}" "${SECRET_MANAGER_SA}" "Secret Manager Service Account"
    grant_iam_roles "${project_id}" "${SECRET_MANAGER_SA}" "${SECRET_MANAGER_ROLES[@]}"
    
    echo -e "${GREEN}✓ Service accounts setup complete for ${environment}${NC}"
    echo ""
}

# Function to create service account summary
create_service_account_summary() {
    echo -e "${YELLOW}Creating service account summary...${NC}"
    
    local summary_file="gcp-migration/gcp-setup/service-account-summary.json"
    
    cat > "${summary_file}" <<EOF
{
  "creationDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "serviceAccounts": {
    "staging": {
      "projectId": "${STAGING_PROJECT_ID}",
      "cloudRun": {
        "email": "${CLOUD_RUN_SA}@${STAGING_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${CLOUD_RUN_ROLES[@]}" | jq -R . | jq -s .)
      },
      "cloudSql": {
        "email": "${CLOUD_SQL_SA}@${STAGING_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${CLOUD_SQL_ROLES[@]}" | jq -R . | jq -s .)
      },
      "cloudBuild": {
        "email": "${CLOUD_BUILD_SA}@${STAGING_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${CLOUD_BUILD_ROLES[@]}" | jq -R . | jq -s .)
      },
      "secretManager": {
        "email": "${SECRET_MANAGER_SA}@${STAGING_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${SECRET_MANAGER_ROLES[@]}" | jq -R . | jq -s .)
      }
    },
    "production": {
      "projectId": "${PRODUCTION_PROJECT_ID}",
      "cloudRun": {
        "email": "${CLOUD_RUN_SA}@${PRODUCTION_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${CLOUD_RUN_ROLES[@]}" | jq -R . | jq -s .)
      },
      "cloudSql": {
        "email": "${CLOUD_SQL_SA}@${PRODUCTION_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${CLOUD_SQL_ROLES[@]}" | jq -R . | jq -s .)
      },
      "cloudBuild": {
        "email": "${CLOUD_BUILD_SA}@${PRODUCTION_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${CLOUD_BUILD_ROLES[@]}" | jq -R . | jq -s .)
      },
      "secretManager": {
        "email": "${SECRET_MANAGER_SA}@${PRODUCTION_PROJECT_ID}.iam.gserviceaccount.com",
        "roles": $(printf '%s\n' "${SECRET_MANAGER_ROLES[@]}" | jq -R . | jq -s .)
      }
    }
  }
}
EOF
    
    echo -e "${GREEN}✓ Service account summary created: ${summary_file}${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting service account setup...${NC}"
    echo ""
    
    check_prerequisites
    
    # Setup staging service accounts
    setup_environment_service_accounts "${STAGING_PROJECT_ID}" "staging"
    
    # Setup production service accounts
    setup_environment_service_accounts "${PRODUCTION_PROJECT_ID}" "production"
    
    # Create summary
    create_service_account_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Service account setup completed!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Review service account keys in gcp-migration/gcp-setup/service-account-keys/"
    echo "2. Secure service account keys (do not commit to version control)"
    echo "3. Run setup-cloud-sql.sh to create database instances"
    echo "4. Run setup-artifact-registry.sh to create Docker repositories"
    echo "5. Run setup-secrets.sh to migrate secrets"
    echo ""
    echo "Security Notes:"
    echo "- Service account keys are stored in gcp-migration/gcp-setup/service-account-keys/"
    echo "- These keys have sensitive permissions. Keep them secure!"
    echo "- Consider using Workload Identity for production instead of keys"
}

# Run main function
main "$@"

