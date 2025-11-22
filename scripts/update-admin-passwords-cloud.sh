#!/bin/bash

###############################################################################
# Update Admin User Passwords in Cloud SQL
# 
# Purpose: Update password hashes for admin users to match "password"
#          Also sets is_email_verified to true
#
# Usage: ./update-admin-passwords-cloud.sh [environment]
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
echo -e "${BLUE}Update Admin User Passwords${NC}"
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
    echo -e "${RED}Error: Could not retrieve password from Secret Manager${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Password retrieved from Secret Manager${NC}"
echo ""

# Password hash for "password" (BCrypt)
PASSWORD_HASH='$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'

# Create SQL update statements
SQL_UPDATE="
-- Update admin user password and ensure email is verified
UPDATE users 
SET 
    password = '${PASSWORD_HASH}',
    is_email_verified = true,
    updated_at = NOW()
WHERE email = 'admin@mysmartwatts.com';

-- Update superadmin user password and ensure email is verified
UPDATE users 
SET 
    password = '${PASSWORD_HASH}',
    is_email_verified = true,
    updated_at = NOW()
WHERE email = 'su_admin@mysmartwatts.com';

-- Verify the updates
SELECT 
    username, 
    email, 
    role, 
    is_active,
    is_email_verified,
    LEFT(password, 20) || '...' as password_hash_preview,
    updated_at
FROM users 
WHERE email IN ('admin@mysmartwatts.com', 'su_admin@mysmartwatts.com')
ORDER BY role, email;
"

echo -e "${YELLOW}Updating admin user passwords...${NC}"
echo ""

# Try using psql with Cloud SQL Proxy if available
if command -v psql &> /dev/null && lsof -i :5433 > /dev/null 2>&1; then
    echo -e "${GREEN}Using Cloud SQL Proxy connection...${NC}"
    PGPASSWORD="$DB_PASSWORD" psql -h 127.0.0.1 -p 5433 -U postgres -d "${DB_NAME}" -c "$SQL_UPDATE"
    echo ""
    echo -e "${GREEN}✓ Passwords updated successfully!${NC}"
else
    echo -e "${YELLOW}Cloud SQL Proxy not running.${NC}"
    echo -e "${YELLOW}Please run this SQL manually:${NC}"
    echo ""
    echo "gcloud sql connect ${INSTANCE_NAME} --user=postgres --database=${DB_NAME} --project=${PROJECT_ID}"
    echo ""
    echo "Then execute:"
    echo "$SQL_UPDATE"
    echo ""
    echo -e "${YELLOW}Or start Cloud SQL Proxy and run this script again:${NC}"
    echo "cloud-sql-proxy ${PROJECT_ID}:europe-west1:${INSTANCE_NAME} --port=5433"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Password Update Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Both admin users now have password: password"
echo ""
echo "Admin Account:"
echo "  Email: admin@mysmartwatts.com"
echo "  Password: password"
echo ""
echo "Super Admin Account:"
echo "  Email: su_admin@mysmartwatts.com"
echo "  Password: password"
echo ""

