#!/bin/bash

###############################################################################
# Deploy Services to Cloud Run
# 
# Purpose: Deploy all services to Cloud Run with health checks and dependency ordering
#
# Usage: ./deploy-services.sh [environment]
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

# Service deployment order (dependencies first)
SERVICES=(
    "service-discovery:8761"
    "api-gateway:8080"
    "user-service:8081"
    "energy-service:8082"
    "device-service:8083"
    "analytics-service:8084"
    "billing-service:8085"
    "edge-gateway:8088"
    "facility-service:8089"
    "feature-flag-service:8090"
    "device-verification-service:8091"
    "appliance-monitoring-service:8092"
    "notification-service:8093"
)

echo -e "${BLUE}Deploying services to Cloud Run (${ENVIRONMENT})...${NC}"

# Set project
gcloud config set project "${PROJECT_ID}"

# Deploy each service
for service_config in "${SERVICES[@]}"; do
    IFS=':' read -r service_name port <<< "$service_config"
    
    echo -e "${YELLOW}Deploying ${service_name}...${NC}"
    
    # Get image from Artifact Registry
    IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${service_name}/${service_name}:latest"
    
    # Deploy to Cloud Run
    gcloud run deploy "${service_name}" \
        --image="${IMAGE}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --platform=managed \
        --allow-unauthenticated \
        --service-account="cloud-run-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
        --set-env-vars="PORT=${port},SPRING_PROFILES_ACTIVE=cloudrun" \
        --set-secrets="POSTGRES_PASSWORD=postgres-password:latest,REDIS_PASSWORD=redis-password:latest" \
        --memory=1Gi \
        --cpu=1 \
        --timeout=300 \
        --max-instances=10 \
        --min-instances=1 \
        --port="${port}" || {
        echo -e "${RED}Failed to deploy ${service_name}${NC}"
        exit 1
    }
    
    # Wait for service to be ready
    echo -e "  Waiting for ${service_name} to be ready..."
    sleep 10
    
    # Health check
    SERVICE_URL=$(gcloud run services describe "${service_name}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --format="value(status.url)")
    
    if curl -f "${SERVICE_URL}/actuator/health" &>/dev/null; then
        echo -e "${GREEN}✓ ${service_name} deployed and healthy${NC}"
    else
        echo -e "${YELLOW}⚠ ${service_name} deployed but health check pending${NC}"
    fi
    
    echo ""
done

echo -e "${GREEN}All services deployed successfully!${NC}"
