#!/bin/bash

###############################################################################
# Update Admin User Role Script
# 
# Purpose: Update admin user role from ROLE_ADMIN to ROLE_ENTERPRISE_ADMIN
#          This fixes the admin dashboard redirect issue
#
# Usage: ./update-admin-role.sh [environment]
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
echo -e "${BLUE}Update Admin User Role${NC}"
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

# Check current role
echo -e "${YELLOW}Checking current admin user role...${NC}"
CURRENT_ROLE=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT role FROM users WHERE email = 'su_admin@mysmartwatts.com' OR username = 'superadmin' LIMIT 1;" 2>/dev/null | xargs)

if [ -z "$CURRENT_ROLE" ]; then
    echo -e "${YELLOW}⚠ Admin user not found with email 'su_admin@mysmartwatts.com' or username 'superadmin'${NC}"
    echo "Searching for admin users..."
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT email, username, role FROM users WHERE email LIKE '%admin%' OR username LIKE '%admin%';" 2>/dev/null
    echo ""
    echo -e "${YELLOW}Please specify the exact email or username to update:${NC}"
    read -p "Email or username: " USER_IDENTIFIER
    
    if [ -z "$USER_IDENTIFIER" ]; then
        echo -e "${RED}Error: No user identifier provided${NC}"
        exit 1
    fi
    
    UPDATE_QUERY="UPDATE users SET role = 'ROLE_ENTERPRISE_ADMIN', updated_at = NOW() WHERE email = '$USER_IDENTIFIER' OR username = '$USER_IDENTIFIER';"
else
    echo -e "Current role: ${CURRENT_ROLE}"
    echo ""
    
    if [ "$CURRENT_ROLE" = "ROLE_ENTERPRISE_ADMIN" ]; then
        echo -e "${GREEN}✓ Admin user already has ROLE_ENTERPRISE_ADMIN${NC}"
        echo "No update needed."
        exit 0
    fi
    
    UPDATE_QUERY="UPDATE users SET role = 'ROLE_ENTERPRISE_ADMIN', updated_at = NOW() WHERE email = 'su_admin@mysmartwatts.com' OR username = 'superadmin';"
fi

# Update the role
echo -e "${YELLOW}Updating admin user role to ROLE_ENTERPRISE_ADMIN...${NC}"
if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "$UPDATE_QUERY" 2>/dev/null; then
    echo -e "${GREEN}✓ Role updated successfully${NC}"
else
    echo -e "${RED}Error: Failed to update role${NC}"
    exit 1
fi

# Verify the update
echo ""
echo -e "${YELLOW}Verifying update...${NC}"
UPDATED_ROLE=$(PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT role FROM users WHERE email = 'su_admin@mysmartwatts.com' OR username = 'superadmin' LIMIT 1;" 2>/dev/null | xargs)

if [ "$UPDATED_ROLE" = "ROLE_ENTERPRISE_ADMIN" ]; then
    echo -e "${GREEN}✓ Verification successful${NC}"
    echo -e "Updated role: ${UPDATED_ROLE}"
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Admin role update completed!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "The admin user now has ROLE_ENTERPRISE_ADMIN and should be redirected to the admin dashboard."
    echo "Please log out and log back in for the changes to take effect."
else
    echo -e "${RED}Error: Role update verification failed${NC}"
    echo "Current role: ${UPDATED_ROLE}"
    exit 1
fi

