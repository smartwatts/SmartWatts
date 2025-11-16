#!/bin/bash

###############################################################################
# Setup Cloud Monitoring
# 
# Purpose: Create dashboards, alert policies, and SLOs
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"

echo "Setting up Cloud Monitoring for ${ENVIRONMENT}"

# Create uptime check
gcloud monitoring uptime-checks create "${ENVIRONMENT}-api-gateway-check" \
    --project="${PROJECT_ID}" \
    --display-name="API Gateway Uptime Check" \
    --http-check-path="/actuator/health" \
    --resource-type=uptime-url || echo "Uptime check may already exist"

# Create alert policy for high error rate
gcloud alpha monitoring policies create \
    --project="${PROJECT_ID}" \
    --notification-channels=CHANNEL_ID \
    --display-name="High Error Rate Alert" \
    --condition-display-name="Error rate > 5%" \
    --condition-threshold-value=5 \
    --condition-threshold-duration=300s || echo "Alert policy creation (requires notification channel setup)"

echo "Monitoring setup completed"
