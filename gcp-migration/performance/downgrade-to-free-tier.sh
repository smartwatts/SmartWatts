#!/bin/bash

# Downgrade Cloud Run Resources to Free Tier
# Reduces memory and CPU to minimize costs

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
REGION="${GCP_REGION:-europe-west1}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Downgrading to Free Tier${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Services list - all will be downgraded to 512Mi memory and 1 CPU
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
    "device-verification-service"
    "feature-flag-service"
    "notification-service"
    "service-discovery"
)

# Free-tier resource allocations
MEMORY="512Mi"
CPU="1"

# Function to update service resources
update_service_resources() {
    local service=$1
    local memory=$2
    local cpu=$3
    
    echo -e "${YELLOW}Downgrading ${service}...${NC}"
    echo -e "  Memory: ${memory}"
    echo -e "  CPU: ${cpu}"
    
    gcloud run services update "${service}" \
        --memory="${memory}" \
        --cpu="${cpu}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --quiet 2>&1 | grep -E "(Updated|memory|cpu)" || {
        echo -e "${GREEN}  ✓ ${service} updated${NC}"
    }
    
    echo ""
}

# Downgrade all services
echo -e "${BLUE}Downgrading Cloud Run Services${NC}"
echo "----------------------------------------"

for service in "${SERVICES[@]}"; do
    update_service_resources "$service" "$MEMORY" "$CPU"
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Downgrade Complete${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "All services have been downgraded to free-tier friendly resources:"
echo "  - Memory: 512Mi (down from 1-2Gi)"
echo "  - CPU: 1 core (down from 2 cores)"
echo "  - minScale: 1 (unchanged)"
echo ""
echo "Note: Cloud SQL is already on free tier (db-f1-micro)"
echo ""
echo "Monitor services to ensure they still perform well with reduced resources."

