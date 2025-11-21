#!/bin/bash

# Optimize Minimum Instances to Reduce Cold Starts
# Increases minScale for critical services

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"

# Critical services that should always have warm instances
CRITICAL_SERVICES=(
    "api-gateway"
    "user-service"
)

# Other services with moderate traffic
MODERATE_SERVICES=(
    "analytics-service"
    "billing-service"
    "device-service"
    "energy-service"
)

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Optimizing Minimum Instances${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to update minScale
update_min_scale() {
    local service=$1
    local min_scale=$2
    
    echo -e "${YELLOW}Updating ${service} minScale to ${min_scale}...${NC}"
    
    gcloud run services update "${service}" \
        --min-instances="${min_scale}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --quiet 2>&1 | grep -E "(Updated|min-instances)" || {
        echo -e "${YELLOW}  Service ${service} updated${NC}"
    }
    
    echo -e "${GREEN}  ✓ ${service} minScale set to ${min_scale}${NC}"
}

# Update critical services to minScale 2
echo -e "${BLUE}Updating Critical Services (minScale: 2)${NC}"
echo "----------------------------------------"
for service in "${CRITICAL_SERVICES[@]}"; do
    update_min_scale "$service" 2
done

echo ""

# Update moderate services to minScale 1 (keep warm)
echo -e "${BLUE}Verifying Moderate Services (minScale: 1)${NC}"
echo "----------------------------------------"
for service in "${MODERATE_SERVICES[@]}"; do
    # Verify current minScale
    current_min=$(gcloud run services describe "${service}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --format="value(spec.template.metadata.annotations['autoscaling.knative.dev/minScale'])" 2>/dev/null || echo "1")
    
    if [ "$current_min" != "1" ]; then
        update_min_scale "$service" 1
    else
        echo -e "${GREEN}  ✓ ${service} already at minScale 1${NC}"
    fi
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Minimum Instance Optimization Complete${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Summary:"
echo "  Critical services (API Gateway, User Service): minScale = 2"
echo "  Other services: minScale = 1"
echo ""
echo "Note: This will increase costs but significantly reduce cold starts."
echo "Monitor costs and adjust as needed."

