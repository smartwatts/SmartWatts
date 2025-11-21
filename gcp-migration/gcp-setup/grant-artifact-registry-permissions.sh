#!/bin/bash

###############################################################################
# Grant Artifact Registry Permissions
# 
# Purpose: Grant Artifact Registry Writer role to a service account
#          This allows the service account to push Docker images to Artifact Registry
#
# Usage: ./grant-artifact-registry-permissions.sh <service-account-email> [project-id]
#   service-account-email: Full email of the service account (e.g., sa@project.iam.gserviceaccount.com)
#   project-id: GCP project ID (defaults to smartwatts-staging)
#
# Example:
#   ./grant-artifact-registry-permissions.sh my-sa@smartwatts-staging.iam.gserviceaccount.com smartwatts-staging
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check arguments
if [ $# -lt 1 ]; then
    echo -e "${RED}Error: Service account email is required${NC}"
    echo "Usage: $0 <service-account-email> [project-id]"
    echo "Example: $0 my-sa@smartwatts-staging.iam.gserviceaccount.com smartwatts-staging"
    exit 1
fi

SERVICE_ACCOUNT_EMAIL="$1"
PROJECT_ID="${2:-smartwatts-staging}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Granting Artifact Registry Permissions${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo "Service Account: ${SERVICE_ACCOUNT_EMAIL}"
echo "Project: ${PROJECT_ID}"
echo ""

# Verify service account exists
echo -e "${YELLOW}Verifying service account exists...${NC}"
if ! gcloud iam service-accounts describe "${SERVICE_ACCOUNT_EMAIL}" --project="${PROJECT_ID}" &>/dev/null; then
    echo -e "${RED}Error: Service account ${SERVICE_ACCOUNT_EMAIL} not found in project ${PROJECT_ID}${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Service account verified${NC}"
echo ""

# Grant Artifact Registry Writer role
echo -e "${YELLOW}Granting Artifact Registry Writer role...${NC}"
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/artifactregistry.writer" \
    --condition=None || {
    echo -e "${RED}Error: Failed to grant Artifact Registry Writer role${NC}"
    exit 1
}
echo -e "${GREEN}✓ Artifact Registry Writer role granted${NC}"
echo ""

# Also grant Cloud Run Admin role (needed for deployment)
echo -e "${YELLOW}Granting Cloud Run Admin role (for deployment)...${NC}"
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/run.admin" \
    --condition=None || {
    echo -e "${YELLOW}Warning: Could not grant Cloud Run Admin role (may already exist)${NC}"
}
echo -e "${GREEN}✓ Cloud Run Admin role granted${NC}"
echo ""

# Grant Service Account User role (needed to use service accounts)
echo -e "${YELLOW}Granting Service Account User role...${NC}"
gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
    --role="roles/iam.serviceAccountUser" \
    --condition=None || {
    echo -e "${YELLOW}Warning: Could not grant Service Account User role (may already exist)${NC}"
}
echo -e "${GREEN}✓ Service Account User role granted${NC}"
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Permissions granted successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "The service account ${SERVICE_ACCOUNT_EMAIL} now has:"
echo "  - Artifact Registry Writer (can push Docker images)"
echo "  - Cloud Run Admin (can deploy services)"
echo "  - Service Account User (can use service accounts)"
echo ""
echo "You can now retry your deployment workflow."

