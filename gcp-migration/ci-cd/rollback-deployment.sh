#!/bin/bash

###############################################################################
# Rollback Deployment
# 
# Purpose: Rollback to previous Cloud Run revision on failure
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
SERVICE_NAME="${2:-api-gateway}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"
REGION="europe-west1"

echo "Rolling back ${SERVICE_NAME} in ${ENVIRONMENT}"

# Get previous revision
PREVIOUS_REVISION=$(gcloud run revisions list \
    --service="${SERVICE_NAME}" \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --limit=2 \
    --format="value(name)" | tail -1)

if [ -z "$PREVIOUS_REVISION" ]; then
    echo "No previous revision found"
    exit 1
fi

# Route all traffic to previous revision
gcloud run services update-traffic "${SERVICE_NAME}" \
    --region="${REGION}" \
    --project="${PROJECT_ID}" \
    --to-revisions="${PREVIOUS_REVISION}=100" || {
    echo "Failed to rollback"
    exit 1
}

echo "Rollback completed to revision: ${PREVIOUS_REVISION}"

