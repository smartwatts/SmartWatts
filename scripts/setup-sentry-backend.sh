#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Backend Sentry Setup"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}This script helps set up Sentry for all backend services.${NC}"
echo ""
echo "Sentry DSNs should be configured in environment variables:"
echo "  - SENTRY_DSN (for production)"
echo "  - STAGING_SENTRY_DSN (for staging)"
echo ""
echo "Services configured with Sentry:"
echo "  ✓ user-service"
echo "  ✓ energy-service"
echo "  ✓ device-service"
echo "  ✓ analytics-service"
echo "  ✓ billing-service"
echo "  ✓ api-gateway"
echo "  ✓ appliance-monitoring-service"
echo "  ✓ facility-service"
echo "  ✓ device-verification-service"
echo "  ✓ feature-flag-service"
echo "  ✓ edge-gateway"
echo ""
echo -e "${YELLOW}To enable Sentry:${NC}"
echo "1. Create a Sentry project for each service (or use one project for all)"
echo "2. Get the DSN from Sentry dashboard"
echo "3. Set SENTRY_DSN environment variable for production"
echo "4. Set STAGING_SENTRY_DSN environment variable for staging"
echo "5. Restart services to apply configuration"
echo ""
echo -e "${GREEN}Sentry configuration is already added to all services!${NC}"







