#!/bin/bash

###############################################################################
# Check Admin Users in Cloud SQL
# 
# Purpose: Check if admin users exist and verify their details
#
# Usage: ./check-admin-users-cloud.sh [environment]
#   environment: staging (default) or production
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
    INSTANCE_NAME="${GCP_INSTANCE_NAME:-smartwatts-production-db}"
    DB_NAME="smartwatts_users"
else
    PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
    INSTANCE_NAME="${GCP_INSTANCE_NAME:-smartwatts-staging-db}"
    DB_NAME="smartwatts_users_staging"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Check Admin Users in Cloud SQL${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Project: ${PROJECT_ID}${NC}"
echo -e "${BLUE}Instance: ${INSTANCE_NAME}${NC}"
echo -e "${BLUE}Database: ${DB_NAME}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo -e "${RED}Error: gcloud CLI is not installed${NC}"
    exit 1
fi

# Set the project
gcloud config set project "${PROJECT_ID}" 2>/dev/null || {
    echo -e "${YELLOW}Warning: Could not set project. Continuing...${NC}"
}

# Try to get password from Secret Manager
echo -e "${YELLOW}Getting database password from Secret Manager...${NC}"
DB_PASSWORD=$(gcloud secrets versions access latest \
    --secret="postgres-password" \
    --project="${PROJECT_ID}" 2>/dev/null || \
    gcloud secrets versions access latest \
    --secret="postgres-root-password" \
    --project="${PROJECT_ID}" 2>/dev/null || echo "")

if [ -z "$DB_PASSWORD" ]; then
    echo -e "${YELLOW}Password not found in Secret Manager.${NC}"
    echo -e "${YELLOW}You can manually connect with:${NC}"
    echo ""
    echo "  gcloud sql connect ${INSTANCE_NAME} --user=postgres --database=${DB_NAME} --project=${PROJECT_ID}"
    echo ""
    echo "Then run this SQL:"
    echo ""
    echo "  SELECT id, username, email, role, is_active, is_email_verified, LEFT(password, 20) || '...' as password_hash_preview, created_at, updated_at"
    echo "  FROM users"
    echo "  WHERE email IN ('admin@mysmartwatts.com', 'su_admin@mysmartwatts.com')"
    echo "     OR username IN ('admin', 'superadmin')"
    echo "  ORDER BY role, email;"
    echo ""
    exit 0
fi

echo -e "${GREEN}✓ Password retrieved from Secret Manager${NC}"
echo ""

# Create SQL query
SQL_QUERY="
SELECT 
    id,
    username, 
    email, 
    role, 
    is_active,
    is_email_verified,
    LEFT(password, 20) || '...' as password_hash_preview,
    created_at,
    updated_at
FROM users 
WHERE email IN ('admin@mysmartwatts.com', 'su_admin@mysmartwatts.com')
   OR username IN ('admin', 'superadmin')
ORDER BY role, email;
"

echo -e "${YELLOW}Checking for admin users...${NC}"
echo ""

# Try using psql with Cloud SQL Proxy if available, otherwise use gcloud sql execute
if command -v psql &> /dev/null && lsof -i :5433 > /dev/null 2>&1; then
    echo -e "${GREEN}Using Cloud SQL Proxy connection...${NC}"
    PGPASSWORD="$DB_PASSWORD" psql -h 127.0.0.1 -p 5433 -U postgres -d "${DB_NAME}" -c "$SQL_QUERY"
elif command -v psql &> /dev/null; then
    echo -e "${YELLOW}Cloud SQL Proxy not running on port 5433.${NC}"
    echo -e "${YELLOW}Starting temporary connection...${NC}"
    echo ""
    # Use gcloud sql execute (if available) or provide manual instructions
    echo -e "${BLUE}Please run this SQL manually:${NC}"
    echo ""
    echo "gcloud sql connect ${INSTANCE_NAME} --user=postgres --database=${DB_NAME} --project=${PROJECT_ID}"
    echo ""
    echo "Then execute:"
    echo "$SQL_QUERY"
    echo ""
else
    echo -e "${YELLOW}psql not found. Using gcloud sql execute...${NC}"
    # Create a temporary SQL file and execute it
    SQL_FILE=$(mktemp)
    echo "$SQL_QUERY" > "$SQL_FILE"
    
    # Try to execute using gcloud (may require password prompt)
    echo -e "${BLUE}Executing SQL query...${NC}"
    echo -e "${YELLOW}Note: You may be prompted for the database password${NC}"
    echo ""
    
    gcloud sql connect "${INSTANCE_NAME}" \
        --user=postgres \
        --database="${DB_NAME}" \
        --project="${PROJECT_ID}" \
        --quiet < "$SQL_FILE" 2>&1 || {
        echo ""
        echo -e "${YELLOW}Automated execution failed. Please run manually:${NC}"
        echo ""
        echo "gcloud sql connect ${INSTANCE_NAME} --user=postgres --database=${DB_NAME} --project=${PROJECT_ID}"
        echo ""
        echo "Password: [use the password from Secret Manager]"
        echo ""
        echo "Then paste this SQL:"
        echo "$SQL_QUERY"
    }
    
    rm -f "$SQL_FILE"
fi

echo ""
echo -e "${GREEN}Check complete!${NC}"
echo ""

