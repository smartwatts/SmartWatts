#!/bin/bash
# Setup custom domain and SSL certificates
set -euo pipefail
DOMAIN="${1:-}"
echo "Setting up custom domain: ${DOMAIN}"

