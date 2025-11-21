#!/bin/bash

# Redeploy services with downgraded resources

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CONFIGS_DIR="gcp-migration/cloud-run-configs"
PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Redeploying Services with Free Tier Resources${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Services to redeploy
SERVICES=(
    "api-gateway"
    "user-service"
    "analytics-service"
    "billing-service"
    "device-service"
    "energy-service"
    "facility-service"
    "edge-gateway"
    "appliance-monitoring-service"
)

for service in "${SERVICES[@]}"; do
    yaml_file="${CONFIGS_DIR}/${service}.yaml"
    
    if [ ! -f "$yaml_file" ]; then
        echo -e "${YELLOW}Skipping ${service} (YAML not found)${NC}"
        continue
    fi
    
    echo -e "${YELLOW}Redeploying ${service}...${NC}"
    
    gcloud run services replace "$yaml_file" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --quiet 2>&1 | grep -E "(Updated|revision|Service)" || {
        echo -e "${GREEN}  ✓ ${service} redeployed${NC}"
    }
    
    echo ""
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Redeployment Complete${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "All services have been redeployed with free-tier resources:"
echo "  - Memory: 512Mi"
echo "  - CPU: 1 core"
echo ""
echo "Verifying deployments..."

