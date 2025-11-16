#!/bin/bash

###############################################################################
# Setup Cloud Logging
# 
# Purpose: Configure log aggregation, metrics, and error tracking
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"

echo "Setting up Cloud Logging for ${ENVIRONMENT}"

# Create log-based metrics
gcloud logging metrics create "${ENVIRONMENT}_error_count" \
    --project="${PROJECT_ID}" \
    --description="Count of error logs" \
    --log-filter='severity>=ERROR' || echo "Metric may already exist"

# Export logs to BigQuery (optional)
# gcloud logging sinks create "${ENVIRONMENT}-logs-sink" \
#     bigquery.googleapis.com/projects/${PROJECT_ID}/datasets/logs \
#     --log-filter='resource.type="cloud_run_revision"'

echo "Logging setup completed"

