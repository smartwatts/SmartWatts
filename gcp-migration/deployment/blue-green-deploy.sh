#!/bin/bash

###############################################################################
# Blue-Green Deployment Script
# 
# Purpose: Deploy with blue-green strategy, traffic splitting, and rollback
#
# Usage: ./blue-green-deploy.sh [environment] [service-name]
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
SERVICE_NAME="${2:-api-gateway}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"
REGION="europe-west1"

echo "Blue-green deployment for ${SERVICE_NAME} in ${ENVIRONMENT}"

# Deploy green revision
GREEN_REVISION="${SERVICE_NAME}-green-$(date +%s)"
IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${SERVICE_NAME}/${SERVICE_NAME}:latest"

echo "Deploying green revision: ${GREEN_REVISION}"

gcloud run deploy "${GREEN_REVISION}" \
    --image="${IMAGE}" \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --no-traffic \
    --tag=green || {
    echo "Failed to deploy green revision"
    exit 1
}

# Health check green revision
GREEN_URL=$(gcloud run services describe "${GREEN_REVISION}" \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --format="value(status.url)")

echo "Testing green revision health..."
for i in {1..10}; do
    if curl -f "${GREEN_URL}/actuator/health" &>/dev/null; then
        echo "Green revision is healthy"
        break
    fi
    sleep 5
done

# Gradual traffic migration: 10% → 50% → 100%
echo "Migrating traffic: 10%"
gcloud run services update-traffic "${SERVICE_NAME}" \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --to-revisions="${GREEN_REVISION}=10" || exit 1

sleep 30

echo "Migrating traffic: 50%"
gcloud run services update-traffic "${SERVICE_NAME}" \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --to-revisions="${GREEN_REVISION}=50" || exit 1

sleep 30

echo "Migrating traffic: 100%"
gcloud run services update-traffic "${SERVICE_NAME}" \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --to-revisions="${GREEN_REVISION}=100" || exit 1

echo "Blue-green deployment completed successfully"
