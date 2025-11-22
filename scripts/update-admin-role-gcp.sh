#!/bin/bash

###############################################################################
# Update Admin User Role Script for GCP Cloud SQL
# 
# Purpose: Update admin user role from ROLE_ADMIN to ROLE_ENTERPRISE_ADMIN
#          This fixes the admin dashboard redirect issue
#          Uses GCP Cloud SQL Proxy or direct connection
#
# Usage: ./update-admin-role-gcp.sh [environment] [instance-name]
#   environment: staging (default) or production
#   instance-name: GCP Cloud SQL instance name (optional, will prompt if not provided)
#
# Prerequisites:
#   - gcloud CLI installed and authenticated
#   - Cloud SQL Proxy installed (if using proxy) OR direct connection enabled
#
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

ENVIRONMENT="${1:-staging}"

# GCP Configuration
if [ "$ENVIRONMENT" = "production" ]; then
    PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-production}"
    DB_NAME="smartwatts_users"
    INSTANCE_NAME="${2:-smartwatts-production-db}"
else
    PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
    DB_NAME="smartwatts_users_staging"
    INSTANCE_NAME="${2:-smartwatts-staging-db}"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Update Admin User Role (GCP Cloud SQL)${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Project: ${PROJECT_ID}${NC}"
echo -e "${BLUE}Instance: ${INSTANCE_NAME}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo -e "${RED}Error: gcloud CLI is not installed${NC}"
    echo "Please install Google Cloud SDK: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Set the project
echo -e "${YELLOW}Setting GCP project to ${PROJECT_ID}...${NC}"
gcloud config set project "${PROJECT_ID}" 2>/dev/null || {
    echo -e "${YELLOW}Warning: Could not set project. Continuing...${NC}"
}

# Check if instance exists
echo -e "${YELLOW}Checking Cloud SQL instance...${NC}"
if ! gcloud sql instances describe "${INSTANCE_NAME}" --project="${PROJECT_ID}" &>/dev/null; then
    echo -e "${RED}Error: Cloud SQL instance '${INSTANCE_NAME}' not found in project '${PROJECT_ID}'${NC}"
    echo ""
    echo "Available instances:"
    gcloud sql instances list --project="${PROJECT_ID}" 2>/dev/null || echo "Could not list instances"
    exit 1
fi

echo -e "${GREEN}✓ Cloud SQL instance found${NC}"
echo ""

# Get connection name
CONNECTION_NAME=$(gcloud sql instances describe "${INSTANCE_NAME}" --project="${PROJECT_ID}" --format="value(connectionName)" 2>/dev/null)
echo "Connection name: ${CONNECTION_NAME}"
echo ""

# Check current role using gcloud sql connect or Cloud SQL Proxy
echo -e "${YELLOW}Checking current admin user role...${NC}"
echo "Note: This requires database user credentials"
echo ""

# Method 1: Try using gcloud sql connect (interactive)
echo -e "${YELLOW}Option 1: Using gcloud sql connect (interactive)${NC}"
echo "Run the following command to connect and update:"
echo ""
echo "  gcloud sql connect ${INSTANCE_NAME} --user=<db-user> --database=${DB_NAME}"
echo ""
echo "Then run this SQL:"
echo ""
echo "  UPDATE users SET role = 'ROLE_ENTERPRISE_ADMIN', updated_at = NOW()"
echo "  WHERE email = 'su_admin@mysmartwatts.com' OR username = 'superadmin';"
echo ""
echo "  SELECT email, username, role FROM users"
echo "  WHERE email = 'su_admin@mysmartwatts.com' OR username = 'superadmin';"
echo ""

# Method 2: Using Cloud SQL Proxy
echo -e "${YELLOW}Option 2: Using Cloud SQL Proxy${NC}"
echo "If you have Cloud SQL Proxy running, you can use the update script:"
echo ""
echo "  ./scripts/update-admin-role.sh ${ENVIRONMENT}"
echo ""

# Method 3: Direct SQL execution via gcloud (if possible)
echo -e "${YELLOW}Option 3: Direct SQL execution${NC}"
echo "You can also execute SQL directly if you have the credentials:"
echo ""
echo "  gcloud sql connect ${INSTANCE_NAME} --user=<db-user> --database=${DB_NAME} --quiet <<EOF"
echo "  UPDATE users SET role = 'ROLE_ENTERPRISE_ADMIN', updated_at = NOW()"
echo "  WHERE email = 'su_admin@mysmartwatts.com' OR username = 'superadmin';"
echo "  EOF"
echo ""

# Provide SQL file location
echo -e "${GREEN}SQL file available at: scripts/update-admin-role.sql${NC}"
echo ""

