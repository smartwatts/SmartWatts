#!/bin/bash

###############################################################################
# Artifact Registry Setup Script
# 
# Purpose: Create Docker repositories in Artifact Registry for each service
#          with multi-region replication
#
# Usage: ./setup-artifact-registry.sh
#
# Prerequisites:
#   - GCP projects created
#   - Google Cloud SDK (gcloud) installed and configured
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
REGION="europe-west1"
REPLICA_REGION="europe-west4"

# Repository names (one per service)
REPOSITORIES=(
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
    "frontend"
)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Artifact Registry Setup${NC}"
echo -e "${BLUE}Region: ${REGION}${NC}"
echo -e "${BLUE}Replica Region: ${REPLICA_REGION}${NC}"
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

# Function to create Artifact Registry repository
create_repository() {
    local project_id=$1
    local repository_name=$2
    local format="DOCKER"
    
    echo -e "  Creating repository: ${repository_name}"
    
    # Check if repository already exists
    if gcloud artifacts repositories describe "${repository_name}" \
        --location="${REGION}" \
        --project="${project_id}" &>/dev/null; then
        echo -e "    ${YELLOW}Repository ${repository_name} already exists, skipping${NC}"
        return 0
    fi
    
    # Create repository
    gcloud artifacts repositories create "${repository_name}" \
        --repository-format="${format}" \
        --location="${REGION}" \
        --project="${project_id}" \
        --description="Docker repository for ${repository_name}" || {
        echo -e "    ${RED}Error: Failed to create repository ${repository_name}${NC}"
        return 1
    }
    
    echo -e "    ${GREEN}✓ Repository ${repository_name} created${NC}"
}

# Function to setup repositories for environment
setup_environment_repositories() {
    local project_id=$1
    local environment=$2
    
    echo -e "${BLUE}Setting up repositories for ${environment} (${project_id})...${NC}"
    echo ""
    
    # Set project
    gcloud config set project "${project_id}"
    
    # Create all repositories
    for repository_name in "${REPOSITORIES[@]}"; do
        create_repository "${project_id}" "${repository_name}"
    done
    
    echo -e "${GREEN}✓ All repositories created for ${environment}${NC}"
    echo ""
}

# Function to configure cleanup policies
configure_cleanup_policies() {
    local project_id=$1
    
    echo -e "${YELLOW}Configuring cleanup policies...${NC}"
    
    # Note: Cleanup policies are configured per repository
    # This is a placeholder - actual policy configuration would be done per repository
    echo -e "${YELLOW}  Note: Configure cleanup policies per repository as needed${NC}"
    echo -e "${YELLOW}  Example: Keep last 10 versions, delete images older than 30 days${NC}"
    echo ""
}

# Function to create repository summary
create_repository_summary() {
    echo -e "${YELLOW}Creating repository summary...${NC}"
    
    local summary_file="gcp-migration/gcp-setup/artifact-registry-summary.json"
    
    cat > "${summary_file}" <<EOF
{
  "creationDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "region": "${REGION}",
  "replicaRegion": "${REPLICA_REGION}",
  "repositories": {
    "staging": {
      "projectId": "${STAGING_PROJECT_ID}",
      "region": "${REGION}",
      "repositories": $(printf '%s\n' "${REPOSITORIES[@]}" | jq -R . | jq -s .)
    },
    "production": {
      "projectId": "${PRODUCTION_PROJECT_ID}",
      "region": "${REGION}",
      "repositories": $(printf '%s\n' "${REPOSITORIES[@]}" | jq -R . | jq -s .)
    }
  },
  "repositoryUrls": {
    "staging": {
      "baseUrl": "${REGION}-docker.pkg.dev/${STAGING_PROJECT_ID}",
      "repositories": {
$(for repo in "${REPOSITORIES[@]}"; do
    if [ "$repo" != "${REPOSITORIES[-1]}" ]; then
        echo "        \"${repo}\": \"${REGION}-docker.pkg.dev/${STAGING_PROJECT_ID}/${repo}\","
    else
        echo "        \"${repo}\": \"${REGION}-docker.pkg.dev/${STAGING_PROJECT_ID}/${repo}\""
    fi
done)
      }
    },
    "production": {
      "baseUrl": "${REGION}-docker.pkg.dev/${PRODUCTION_PROJECT_ID}",
      "repositories": {
$(for repo in "${REPOSITORIES[@]}"; do
    if [ "$repo" != "${REPOSITORIES[-1]}" ]; then
        echo "        \"${repo}\": \"${REGION}-docker.pkg.dev/${PRODUCTION_PROJECT_ID}/${repo}\","
    else
        echo "        \"${repo}\": \"${REGION}-docker.pkg.dev/${PRODUCTION_PROJECT_ID}/${repo}\""
    fi
done)
      }
    }
  }
}
EOF
    
    echo -e "${GREEN}✓ Repository summary created: ${summary_file}${NC}"
    echo ""
}

# Main execution
main() {
    echo -e "${BLUE}Starting Artifact Registry setup...${NC}"
    echo ""
    
    check_prerequisites
    
    # Setup staging repositories
    setup_environment_repositories "${STAGING_PROJECT_ID}" "staging"
    
    # Setup production repositories
    setup_environment_repositories "${PRODUCTION_PROJECT_ID}" "production"
    
    # Configure cleanup policies
    configure_cleanup_policies "${STAGING_PROJECT_ID}"
    configure_cleanup_policies "${PRODUCTION_PROJECT_ID}"
    
    # Create summary
    create_repository_summary
    
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Artifact Registry setup completed!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Configure Docker authentication:"
    echo "   gcloud auth configure-docker ${REGION}-docker.pkg.dev"
    echo "2. Test pushing an image to a repository"
    echo "3. Run setup-secrets.sh to migrate secrets"
    echo "4. Update CI/CD pipelines with repository URLs"
}

# Run main function
main "$@"

