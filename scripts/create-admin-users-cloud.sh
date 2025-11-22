#!/bin/bash

###############################################################################
# Create Admin Users in Cloud SQL
# 
# Purpose: Create admin and super admin users in GCP Cloud SQL database
#          Uses gcloud sql connect for direct database access
#
# Usage: ./create-admin-users-cloud.sh [environment]
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
echo -e "${BLUE}Create Admin Users in Cloud SQL${NC}"
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

# Password hash for "password" (BCrypt)
PASSWORD_HASH='$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'

# Create SQL file
SQL_FILE=$(mktemp)
cat > "$SQL_FILE" <<EOF
-- Create admin user (ROLE_ADMIN)
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
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@mysmartwatts.com',
    '${PASSWORD_HASH}',
    'Admin',
    'User',
    'ROLE_ADMIN',
    true,
    true,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE
SET 
    password = EXCLUDED.password,
    role = EXCLUDED.role,
    is_active = true,
    is_email_verified = true,
    updated_at = NOW();

-- Create super admin user (ROLE_ENTERPRISE_ADMIN)
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
VALUES (
    gen_random_uuid(),
    'superadmin',
    'su_admin@mysmartwatts.com',
    '${PASSWORD_HASH}',
    'Super',
    'Admin',
    'ROLE_ENTERPRISE_ADMIN',
    true,
    true,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE
SET 
    password = EXCLUDED.password,
    role = EXCLUDED.role,
    is_active = true,
    is_email_verified = true,
    updated_at = NOW();

-- Verify users were created
SELECT 
    username, 
    email, 
    role, 
    is_active,
    is_email_verified
FROM users 
WHERE email IN ('admin@mysmartwatts.com', 'su_admin@mysmartwatts.com')
ORDER BY role, email;
EOF

echo -e "${YELLOW}SQL commands prepared. Connecting to Cloud SQL...${NC}"
echo ""
echo -e "${BLUE}You will be prompted for the database user password.${NC}"
echo -e "${BLUE}Default user is usually 'postgres'${NC}"
echo ""

# Execute SQL using gcloud sql connect
echo -e "${YELLOW}Executing SQL commands...${NC}"
gcloud sql connect "${INSTANCE_NAME}" \
    --user=postgres \
    --database="${DB_NAME}" \
    --project="${PROJECT_ID}" \
    --quiet <<< "$(cat "$SQL_FILE")"

# Clean up
rm -f "$SQL_FILE"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Admin Users Created/Updated!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Login Credentials:"
echo ""
echo "Admin Account:"
echo "  Email: admin@mysmartwatts.com"
echo "  Password: password"
echo "  Role: ROLE_ADMIN"
echo ""
echo "Super Admin Account:"
echo "  Email: su_admin@mysmartwatts.com"
echo "  Password: password"
echo "  Role: ROLE_ENTERPRISE_ADMIN"
echo ""
echo -e "${GREEN}You can now log in at: https://frontend-3daykcsw5a-ew.a.run.app/login${NC}"
echo ""

