#!/bin/bash

###############################################################################
# Test Migration
# 
# Purpose: Post-migration validation and functionality tests
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"

echo "Testing migration for ${ENVIRONMENT}"

# Get API Gateway URL
API_URL=$(gcloud run services describe api-gateway \
    --region=europe-west1 \
    --project="${PROJECT_ID}" \
    --format="value(status.url)" 2>/dev/null || echo "")

if [ -z "$API_URL" ]; then
    echo "Error: API Gateway not found"
    exit 1
fi

# Test endpoints
echo "Testing API endpoints..."

# Health check
curl -f "${API_URL}/actuator/health" || exit 1

# User registration test
curl -X POST "${API_URL}/api/v1/users/register" \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"Test123!"}' || echo "Registration test (may fail if user exists)"

echo "Migration tests completed"

