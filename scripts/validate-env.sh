#!/bin/bash

# SmartWatts Environment Validation Script
# This script validates that all required environment variables are set
# and checks for weak/default passwords

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENV_FILE=${1:-.env}
WEAK_PASSWORDS=("postgres" "postgres123" "smartwatts123" "demo-key" "CHANGE_ME" "password" "123456" "admin" "test")
MIN_PASSWORD_LENGTH=12
MIN_JWT_SECRET_LENGTH=64

echo -e "${BLUE}🔍 SmartWatts Environment Validation${NC}"
echo "=============================================="
echo ""

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}❌ Environment file '$ENV_FILE' not found${NC}"
    echo "Please copy env.template to .env and configure your values"
    exit 1
fi

# Source the environment file
set -a
source "$ENV_FILE"
set +a

echo -e "${BLUE}📋 Validating environment variables...${NC}"
echo ""

# Function to check if variable is set
check_var() {
    local var_name=$1
    local var_value=${!var_name}
    
    if [ -z "$var_value" ]; then
        echo -e "${RED}❌ $var_name is not set${NC}"
        return 1
    else
        echo -e "${GREEN}✅ $var_name is set${NC}"
        return 0
    fi
}

# Function to check password strength
check_password_strength() {
    local var_name=$1
    local var_value=${!var_name}
    
    # Check for weak passwords
    for weak_pass in "${WEAK_PASSWORDS[@]}"; do
        if [ "$var_value" = "$weak_pass" ]; then
            echo -e "${RED}❌ $var_name uses weak password: '$weak_pass'${NC}"
            return 1
        fi
    done
    
    # Check minimum length
    if [ ${#var_value} -lt $MIN_PASSWORD_LENGTH ]; then
        echo -e "${RED}❌ $var_name is too short (minimum $MIN_PASSWORD_LENGTH characters)${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ $var_name meets strength requirements${NC}"
    return 0
}

# Function to check JWT secret strength
check_jwt_secret() {
    local var_name=$1
    local var_value=${!var_name}
    
    # Check for weak JWT secrets
    for weak_pass in "${WEAK_PASSWORDS[@]}"; do
        if [ "$var_value" = "$weak_pass" ]; then
            echo -e "${RED}❌ $var_name uses weak secret: '$weak_pass'${NC}"
            return 1
        fi
    done
    
    # Check minimum length for JWT secret
    if [ ${#var_value} -lt $MIN_JWT_SECRET_LENGTH ]; then
        echo -e "${RED}❌ $var_name is too short (minimum $MIN_JWT_SECRET_LENGTH characters)${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✅ $var_name meets strength requirements${NC}"
    return 0
}

# Track validation results
VALIDATION_FAILED=0

echo -e "${BLUE}Required Environment Variables:${NC}"
echo "----------------------------------------"

# Database configuration
check_var "POSTGRES_DB" || VALIDATION_FAILED=1
check_var "POSTGRES_USER" || VALIDATION_FAILED=1
check_var "POSTGRES_PASSWORD" || VALIDATION_FAILED=1
check_var "POSTGRES_HOST" || VALIDATION_FAILED=1
check_var "POSTGRES_PORT" || VALIDATION_FAILED=1

# Redis configuration
check_var "REDIS_HOST" || VALIDATION_FAILED=1
check_var "REDIS_PORT" || VALIDATION_FAILED=1
check_var "REDIS_PASSWORD" || VALIDATION_FAILED=1

# JWT configuration
check_var "JWT_SECRET" || VALIDATION_FAILED=1
check_var "JWT_EXPIRATION" || VALIDATION_FAILED=1
check_var "JWT_REFRESH_EXPIRATION" || VALIDATION_FAILED=1

# Service discovery
check_var "EUREKA_SERVER_URL" || VALIDATION_FAILED=1

# Security
check_var "BCRYPT_ROUNDS" || VALIDATION_FAILED=1

# Logging
check_var "LOG_LEVEL" || VALIDATION_FAILED=1
check_var "LOG_FORMAT" || VALIDATION_FAILED=1

# Production settings
check_var "SPRING_PROFILES_ACTIVE" || VALIDATION_FAILED=1

echo ""
echo -e "${BLUE}Password Strength Validation:${NC}"
echo "----------------------------------------"

# Check password strength
check_password_strength "POSTGRES_PASSWORD" || VALIDATION_FAILED=1
check_password_strength "REDIS_PASSWORD" || VALIDATION_FAILED=1

# Check JWT secret strength
check_jwt_secret "JWT_SECRET" || VALIDATION_FAILED=1

echo ""
echo -e "${BLUE}Optional Environment Variables:${NC}"
echo "----------------------------------------"

# Optional variables (warn if not set but don't fail)
if [ -z "$OPENWEATHER_API_KEY" ]; then
    echo -e "${YELLOW}⚠️  OPENWEATHER_API_KEY is not set (optional)${NC}"
else
    echo -e "${GREEN}✅ OPENWEATHER_API_KEY is set${NC}"
fi

if [ -z "$SSL_KEYSTORE_PASSWORD" ]; then
    echo -e "${YELLOW}⚠️  SSL_KEYSTORE_PASSWORD is not set (optional for production)${NC}"
else
    echo -e "${GREEN}✅ SSL_KEYSTORE_PASSWORD is set${NC}"
fi

if [ -z "$GRAFANA_ADMIN_PASSWORD" ]; then
    echo -e "${YELLOW}⚠️  GRAFANA_ADMIN_PASSWORD is not set (optional for production)${NC}"
else
    echo -e "${GREEN}✅ GRAFANA_ADMIN_PASSWORD is set${NC}"
fi

echo ""
echo "=============================================="

if [ $VALIDATION_FAILED -eq 1 ]; then
    echo -e "${RED}❌ Environment validation FAILED${NC}"
    echo ""
    echo "Please fix the issues above before proceeding with deployment."
    echo ""
    echo "Common fixes:"
    echo "1. Copy env.template to .env and update values"
    echo "2. Use strong passwords (minimum 12 characters)"
    echo "3. Use a strong JWT secret (minimum 64 characters)"
    echo "4. Avoid default/weak passwords like 'postgres', 'password', etc."
    exit 1
else
    echo -e "${GREEN}✅ Environment validation PASSED${NC}"
    echo ""
    echo "All required environment variables are set and meet security requirements."
    echo "You can proceed with deployment."
    exit 0
fi










