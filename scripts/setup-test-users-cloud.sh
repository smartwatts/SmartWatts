#!/bin/bash

###############################################################################
# Setup Test Users in Cloud SQL
# 
# Purpose: 
#   1. Update su_admin password to SuperAdmin123!
#   2. Create missing test users (test@mysmartwatts.com, household@smartwatts.com)
#
# Usage: ./setup-test-users-cloud.sh [environment]
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
    API_URL="https://api-gateway-3daykcsw5a-ew.a.run.app"
else
    PROJECT_ID="${GCP_PROJECT_ID:-smartwatts-staging}"
    INSTANCE_NAME="${GCP_INSTANCE_NAME:-smartwatts-staging-db}"
    DB_NAME="smartwatts_users_staging"
    API_URL="https://api-gateway-3daykcsw5a-ew.a.run.app"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Setup Test Users${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}Project: ${PROJECT_ID}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# BCrypt hashes (generated with Python bcrypt)
# Note: Using $2b$ format (compatible with Spring's BCryptPasswordEncoder)
SU_ADMIN_HASH='$2b$12$d3X6mP/iKaeceaLrzVuepOKd5IpiczkU91BLNlKBVztAj9Vrr/p/W'
HOUSEHOLD_HASH='$2b$12$bdbIAQMF53X2ZPjbGMFx.u21AxOTxZJK7o53W8HeVr2EyKziqz0qi'
PASSWORD_HASH='$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'  # for "password"

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
    echo -e "${YELLOW}You'll need to provide it manually.${NC}"
    echo ""
fi

# Create SQL script
SQL_SCRIPT="
-- Update su_admin password to SuperAdmin123!
UPDATE users 
SET 
    password = '${SU_ADMIN_HASH}',
    is_email_verified = true,
    updated_at = NOW()
WHERE email = 'su_admin@mysmartwatts.com';

-- Create/Update test user (test@mysmartwatts.com)
-- First, update if email exists
UPDATE users 
SET 
    username = 'testuser',
    password = '${PASSWORD_HASH}',
    first_name = 'Test',
    last_name = 'User',
    role = 'ROLE_USER',
    is_active = true,
    is_email_verified = true,
    updated_at = NOW()
WHERE email = 'test@mysmartwatts.com';

-- Update if username exists but different email (fix conflict)
UPDATE users 
SET 
    email = 'test@mysmartwatts.com',
    password = '${PASSWORD_HASH}',
    first_name = 'Test',
    last_name = 'User',
    role = 'ROLE_USER',
    is_active = true,
    is_email_verified = true,
    updated_at = NOW()
WHERE username = 'testuser' AND email != 'test@mysmartwatts.com';

-- Insert only if user doesn't exist (neither email nor username match)
INSERT INTO users (
    id, 
    username, 
    email, 
    password, 
    first_name, 
    last_name, 
    role, 
    is_active, 
    is_email_verified,
    created_at, 
    updated_at
)
SELECT 
    gen_random_uuid(),
    'testuser',
    'test@mysmartwatts.com',
    '${PASSWORD_HASH}',
    'Test',
    'User',
    'ROLE_USER',
    true,
    true,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users 
    WHERE email = 'test@mysmartwatts.com' OR username = 'testuser'
);

-- Create/Update household demo user
-- First, update if email exists
UPDATE users 
SET 
    username = 'household',
    password = '${HOUSEHOLD_HASH}',
    first_name = 'Household',
    last_name = 'Demo',
    role = 'ROLE_USER',
    is_active = true,
    is_email_verified = true,
    updated_at = NOW()
WHERE email = 'household@smartwatts.com';

-- Update if username exists but different email
UPDATE users 
SET 
    email = 'household@smartwatts.com',
    password = '${HOUSEHOLD_HASH}',
    first_name = 'Household',
    last_name = 'Demo',
    role = 'ROLE_USER',
    is_active = true,
    is_email_verified = true,
    updated_at = NOW()
WHERE username = 'household' AND email != 'household@smartwatts.com';

-- Insert only if user doesn't exist
INSERT INTO users (
    id, 
    username, 
    email, 
    password, 
    first_name, 
    last_name, 
    role, 
    is_active, 
    is_email_verified,
    created_at, 
    updated_at
)
SELECT 
    gen_random_uuid(),
    'household',
    'household@smartwatts.com',
    '${HOUSEHOLD_HASH}',
    'Household',
    'Demo',
    'ROLE_USER',
    true,
    true,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users 
    WHERE email = 'household@smartwatts.com' OR username = 'household'
);

-- Verify all users
SELECT 
    username, 
    email, 
    role, 
    is_active,
    is_email_verified
FROM users 
WHERE email IN ('admin@mysmartwatts.com', 'su_admin@mysmartwatts.com', 'test@mysmartwatts.com', 'household@smartwatts.com')
ORDER BY email;
"

echo -e "${YELLOW}SQL script prepared.${NC}"
echo ""

# Try using psql with Cloud SQL Proxy if available
if [ -n "$DB_PASSWORD" ] && command -v psql &> /dev/null && lsof -i :5433 > /dev/null 2>&1; then
    echo -e "${GREEN}Using Cloud SQL Proxy connection...${NC}"
    PGPASSWORD="$DB_PASSWORD" psql -h 127.0.0.1 -p 5433 -U postgres -d "${DB_NAME}" -c "$SQL_SCRIPT"
    echo ""
    echo -e "${GREEN}✓ Users updated/created successfully!${NC}"
else
    echo -e "${YELLOW}Cloud SQL Proxy not running or password not available.${NC}"
    echo -e "${YELLOW}Please run this SQL manually:${NC}"
    echo ""
    echo "gcloud sql connect ${INSTANCE_NAME} --user=postgres --database=${DB_NAME} --project=${PROJECT_ID}"
    echo ""
    echo "Then execute:"
    echo "$SQL_SCRIPT"
    echo ""
    echo -e "${BLUE}Or start Cloud SQL Proxy first:${NC}"
    echo "cloud-sql-proxy ${PROJECT_ID}:europe-west1:${INSTANCE_NAME} --port=5433"
    echo ""
    echo "Then run this script again."
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Test Users Setup Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "User Credentials:"
echo ""
echo "Admin:"
echo "  Email: admin@mysmartwatts.com"
echo "  Password: password"
echo ""
echo "Super Admin:"
echo "  Email: su_admin@mysmartwatts.com"
echo "  Password: SuperAdmin123!"
echo ""
echo "Test User:"
echo "  Email: test@mysmartwatts.com"
echo "  Password: password"
echo ""
echo "Household Demo:"
echo "  Email: household@smartwatts.com"
echo "  Password: Household123!"
echo ""
