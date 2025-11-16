#!/bin/bash

###############################################################################
# Manage SSL Certificates
# 
# Purpose: Manage Google-managed SSL certificates
#
###############################################################################

set -euo pipefail

DOMAIN="${1:-}"
PROJECT_ID="${2:-smartwatts-production}"

if [ -z "$DOMAIN" ]; then
    echo "Usage: ./manage-ssl-certificates.sh <domain> [project-id]"
    exit 1
fi

echo "Managing SSL certificates for ${DOMAIN}"

# Create managed SSL certificate
gcloud compute ssl-certificates create "${DOMAIN}-cert" \
    --project="${PROJECT_ID}" \
    --domains="${DOMAIN}" \
    --global || echo "Certificate may already exist"

echo "SSL certificate created. Update load balancer to use this certificate."

