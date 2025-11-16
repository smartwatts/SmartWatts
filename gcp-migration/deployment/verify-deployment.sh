#!/bin/bash

###############################################################################
# Verify Deployment
# 
# Purpose: Verify all services are deployed and healthy
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"
REGION="europe-west1"

SERVICES=(
    "api-gateway:8080"
    "user-service:8081"
    "energy-service:8082"
    "device-service:8083"
    "analytics-service:8084"
    "billing-service:8085"
)

echo "Verifying deployment for ${ENVIRONMENT}"

FAILED=0

for service_config in "${SERVICES[@]}"; do
    IFS=':' read -r service_name port <<< "$service_config"
    
    SERVICE_URL=$(gcloud run services describe "${service_name}" \
        --region="${REGION}" \
        --project="${PROJECT_ID}" \
        --format="value(status.url)" 2>/dev/null || echo "")
    
    if [ -z "$SERVICE_URL" ]; then
        echo "✗ ${service_name}: Not deployed"
        FAILED=$((FAILED + 1))
        continue
    fi
    
    if curl -f "${SERVICE_URL}/actuator/health" &>/dev/null; then
        echo "✓ ${service_name}: Healthy"
    else
        echo "✗ ${service_name}: Unhealthy"
        FAILED=$((FAILED + 1))
    fi
done

if [ $FAILED -eq 0 ]; then
    echo "All services verified successfully"
    exit 0
else
    echo "${FAILED} service(s) failed verification"
    exit 1
fi

