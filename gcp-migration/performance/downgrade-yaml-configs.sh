#!/bin/bash

# Update YAML configs to free-tier resources
# Then redeploy services

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
echo -e "${BLUE}Updating YAML Configs for Free Tier${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Services to update
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
    
    echo -e "${YELLOW}Updating ${service}.yaml...${NC}"
    
    # Update memory limits to 512Mi
    sed -i.bak 's/memory: 2Gi/memory: 512Mi/g' "$yaml_file"
    sed -i.bak 's/memory: 1Gi/memory: 512Mi/g' "$yaml_file"
    
    # Update memory requests to 512Mi (must be <= limits)
    sed -i.bak 's/memory: "2Gi"/memory: "512Mi"/g' "$yaml_file"
    sed -i.bak 's/memory: "1Gi"/memory: "512Mi"/g' "$yaml_file"
    
    # Update CPU limits to 1
    sed -i.bak 's/cpu: "2"/cpu: "1"/g' "$yaml_file"
    
    # Update CPU requests to 1
    sed -i.bak 's/cpu: "1"/cpu: "1"/g' "$yaml_file"  # Already 1, but ensure consistency
    
    # Remove backup files
    rm -f "${yaml_file}.bak"
    
    echo -e "${GREEN}  ✓ ${service}.yaml updated${NC}"
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}YAML Configs Updated${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Next step: Redeploy services using the updated YAML files"
echo "Run: ./gcp-migration/performance/redeploy-with-downgraded-resources.sh"

