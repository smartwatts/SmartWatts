#!/bin/bash

###############################################################################
# Verify Service Account Key
# 
# Purpose: Check which service account a JSON key file belongs to
#
# Usage: ./verify-service-account-key.sh <path-to-key.json>
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

if [ $# -lt 1 ]; then
    echo -e "${RED}Error: Key file path is required${NC}"
    echo "Usage: $0 <path-to-key.json>"
    echo "Example: $0 /path/to/service-account-key.json"
    exit 1
fi

KEY_FILE="$1"

if [ ! -f "$KEY_FILE" ]; then
    echo -e "${RED}Error: Key file not found: ${KEY_FILE}${NC}"
    exit 1
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Service Account Key Verification${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Extract service account email from key
SERVICE_ACCOUNT_EMAIL=$(cat "$KEY_FILE" | grep -o '"client_email": "[^"]*"' | cut -d'"' -f4)

if [ -z "$SERVICE_ACCOUNT_EMAIL" ]; then
    echo -e "${RED}Error: Could not extract service account email from key file${NC}"
    exit 1
fi

echo -e "${GREEN}Service Account Email: ${SERVICE_ACCOUNT_EMAIL}${NC}"
echo ""

# Extract project ID
PROJECT_ID=$(cat "$KEY_FILE" | grep -o '"project_id": "[^"]*"' | cut -d'"' -f4)
echo -e "Project ID: ${PROJECT_ID}"
echo ""

# Check if this is the cloud-build-sa
if [[ "$SERVICE_ACCOUNT_EMAIL" == *"cloud-build-sa"* ]]; then
    echo -e "${GREEN}✓ This is the cloud-build-sa service account${NC}"
    echo -e "${GREEN}✓ This key should work for building and pushing Docker images${NC}"
else
    echo -e "${YELLOW}⚠ This is NOT the cloud-build-sa service account${NC}"
    echo -e "${YELLOW}⚠ For building and pushing images, you need cloud-build-sa@${PROJECT_ID}.iam.gserviceaccount.com${NC}"
    echo ""
    echo "To create a key for cloud-build-sa, run:"
    echo "  gcloud iam service-accounts keys create cloud-build-sa-key.json \\"
    echo "    --iam-account=cloud-build-sa@${PROJECT_ID}.iam.gserviceaccount.com \\"
    echo "    --project=${PROJECT_ID}"
fi

echo ""

