#!/bin/bash

###############################################################################
# Create Admin User Script
# 
# Purpose: Create a new admin user with ROLE_ADMIN (basic admin privileges)
#          This allows you to have both ROLE_ADMIN and ROLE_ENTERPRISE_ADMIN users
#
# Usage: ./create-admin-user.sh [environment]
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

# Database configuration based on environment
if [ "$ENVIRONMENT" = "production" ]; then
    DB_HOST="${PRODUCTION_POSTGRES_HOST:-localhost}"
    DB_PORT="${PRODUCTION_POSTGRES_PORT:-5432}"
    DB_USER="${PRODUCTION_POSTGRES_USER:-postgres}"
    DB_PASSWORD="${PRODUCTION_POSTGRES_PASSWORD}"
    DB_NAME="smartwatts_users"
else
    DB_HOST="${STAGING_POSTGRES_HOST:-localhost}"
    DB_PORT="${STAGING_POSTGRES_PORT:-5433}"
    DB_USER="${STAGING_POSTGRES_USER:-smartwatts_staging}"
    DB_PASSWORD="${STAGING_POSTGRES_PASSWORD:-staging_password_123}"
    DB_NAME="smartwatts_users_staging"
fi

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Create Admin User (ROLE_ADMIN)${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if database connection details are available
if [ -z "$DB_PASSWORD" ] && [ "$ENVIRONMENT" = "production" ]; then
    echo -e "${RED}Error: PRODUCTION_POSTGRES_PASSWORD environment variable is required for production${NC}"
    exit 1
fi

echo -e "${YELLOW}Connecting to database...${NC}"
echo "Host: ${DB_HOST}"
echo "Port: ${DB_PORT}"
echo "Database: ${DB_NAME}"
echo "User: ${DB_USER}"
echo ""

# Check database connection
if ! PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c '\q' 2>/dev/null; then
    echo -e "${RED}Error: Cannot connect to database${NC}"
    echo "Please check your database connection settings"
    exit 1
fi

echo -e "${GREEN}✓ Database connection successful${NC}"
echo ""

# Check existing admin users
echo -e "${YELLOW}Checking existing admin users...${NC}"
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
SELECT id, username, email, role, is_active 
FROM users 
WHERE role IN ('ROLE_ADMIN', 'ROLE_ENTERPRISE_ADMIN')
ORDER BY role, email;
" 2>/dev/null || true
echo ""

# Create the new admin user
echo -e "${YELLOW}Creating new admin user with ROLE_ADMIN...${NC}"
CREATE_USER_QUERY="
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
    '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Admin',
    'Basic',
    'ROLE_ADMIN',
    true,
    true,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;
"

if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "$CREATE_USER_QUERY" 2>/dev/null; then
    echo -e "${GREEN}✓ Admin user creation query executed${NC}"
else
    echo -e "${RED}Error: Failed to create admin user${NC}"
    exit 1
fi

# Verify the user was created
echo ""
echo -e "${YELLOW}Verifying admin user creation...${NC}"
NEW_USER=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT email, role FROM users WHERE email = 'admin@mysmartwatts.com' AND role = 'ROLE_ADMIN';" 2>/dev/null | xargs)

if [ -n "$NEW_USER" ]; then
    echo -e "${GREEN}✓ Admin user created successfully!${NC}"
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Admin User Created${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Email: admin@mysmartwatts.com"
    echo "Username: admin"
    echo "Password: password"
    echo "Role: ROLE_ADMIN (Basic Admin)"
    echo ""
    echo -e "${YELLOW}All Admin Users:${NC}"
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
    SELECT 
        username, 
        email, 
        role, 
        is_active
    FROM users 
    WHERE role IN ('ROLE_ADMIN', 'ROLE_ENTERPRISE_ADMIN')
    ORDER BY role, email;
    " 2>/dev/null || true
else
    echo -e "${YELLOW}⚠ User may already exist or creation failed${NC}"
    echo "Checking if user already exists..."
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT email, username, role FROM users WHERE email = 'admin@mysmartwatts.com' AND role = 'ROLE_ADMIN';" 2>/dev/null || true
fi

echo ""
echo -e "${GREEN}Script completed!${NC}"

