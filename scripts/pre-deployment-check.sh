#!/bin/bash

# SmartWatts Pre-Deployment Validation Script
# This script performs comprehensive validation before deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENV_FILE=${1:-.env}
VALIDATION_FAILED=0

echo -e "${BLUE}🔍 SmartWatts Pre-Deployment Validation${NC}"
echo "=============================================="
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to log validation result
log_validation() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✅ $test_name: $message${NC}"
    elif [ "$status" = "WARN" ]; then
        echo -e "${YELLOW}⚠️  $test_name: $message${NC}"
    else
        echo -e "${RED}❌ $test_name: $message${NC}"
        VALIDATION_FAILED=1
    fi
}

# 1. Environment Variables Validation
echo -e "${BLUE}📋 Validating Environment Variables...${NC}"
if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
    
    # Check for required variables
    required_vars=(
        "POSTGRES_DB" "POSTGRES_USER" "POSTGRES_PASSWORD" "POSTGRES_HOST" "POSTGRES_PORT"
        "REDIS_HOST" "REDIS_PORT" "REDIS_PASSWORD"
        "JWT_SECRET" "JWT_EXPIRATION" "JWT_REFRESH_EXPIRATION"
        "EUREKA_SERVER_URL" "BCRYPT_ROUNDS" "LOG_LEVEL" "LOG_FORMAT" "SPRING_PROFILES_ACTIVE"
    )
    
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            log_validation "Environment Variables" "FAIL" "Required variable $var is not set"
        else
            log_validation "Environment Variables" "PASS" "$var is set"
        fi
    done
    
    # Check for weak passwords
    weak_passwords=("postgres" "postgres123" "smartwatts123" "demo-key" "CHANGE_ME" "password" "123456")
    for weak_pass in "${weak_passwords[@]}"; do
        if [ "$POSTGRES_PASSWORD" = "$weak_pass" ]; then
            log_validation "Password Security" "FAIL" "POSTGRES_PASSWORD uses weak password: $weak_pass"
        fi
        if [ "$REDIS_PASSWORD" = "$weak_pass" ]; then
            log_validation "Password Security" "FAIL" "REDIS_PASSWORD uses weak password: $weak_pass"
        fi
        if [ "$JWT_SECRET" = "$weak_pass" ]; then
            log_validation "Password Security" "FAIL" "JWT_SECRET uses weak secret: $weak_pass"
        fi
    done
    
    # Check password length
    if [ ${#POSTGRES_PASSWORD} -lt 12 ]; then
        log_validation "Password Security" "FAIL" "POSTGRES_PASSWORD is too short (minimum 12 characters)"
    fi
    if [ ${#REDIS_PASSWORD} -lt 12 ]; then
        log_validation "Password Security" "FAIL" "REDIS_PASSWORD is too short (minimum 12 characters)"
    fi
    if [ ${#JWT_SECRET} -lt 64 ]; then
        log_validation "Password Security" "FAIL" "JWT_SECRET is too short (minimum 64 characters)"
    fi
    
else
    log_validation "Environment Variables" "FAIL" "Environment file $ENV_FILE not found"
fi

echo ""

# 2. Docker and Docker Compose Validation
echo -e "${BLUE}🐳 Validating Docker Environment...${NC}"
if command_exists docker; then
    log_validation "Docker" "PASS" "Docker is installed"
    
    # Check if Docker is running
    if docker info >/dev/null 2>&1; then
        log_validation "Docker" "PASS" "Docker daemon is running"
    else
        log_validation "Docker" "FAIL" "Docker daemon is not running"
    fi
else
    log_validation "Docker" "FAIL" "Docker is not installed"
fi

if command_exists docker-compose; then
    log_validation "Docker Compose" "PASS" "Docker Compose is installed"
else
    log_validation "Docker Compose" "FAIL" "Docker Compose is not installed"
fi

echo ""

# 3. Frontend Build Validation
echo -e "${BLUE}🎨 Validating Frontend Build...${NC}"
if [ -d "frontend" ]; then
    cd frontend
    
    # Check if package.json exists
    if [ -f "package.json" ]; then
        log_validation "Frontend" "PASS" "package.json found"
        
        # Check if node_modules exists
        if [ -d "node_modules" ]; then
            log_validation "Frontend" "PASS" "Dependencies installed"
        else
            log_validation "Frontend" "WARN" "Dependencies not installed (run npm install)"
        fi
        
        # Try to build frontend
        if npm run build >/dev/null 2>&1; then
            log_validation "Frontend Build" "PASS" "Frontend builds successfully"
        else
            log_validation "Frontend Build" "FAIL" "Frontend build failed"
        fi
    else
        log_validation "Frontend" "FAIL" "package.json not found"
    fi
    
    cd ..
else
    log_validation "Frontend" "FAIL" "Frontend directory not found"
fi

echo ""

# 4. Backend Services Validation
echo -e "${BLUE}⚙️  Validating Backend Services...${NC}"
backend_services=(
    "user-service" "energy-service" "device-service" "analytics-service"
    "billing-service" "api-gateway" "service-discovery" "edge-gateway"
    "facility-service" "feature-flag-service" "device-verification-service"
    "appliance-monitoring-service"
)

for service in "${backend_services[@]}"; do
    if [ -d "backend/$service" ] || [ -d "$service" ]; then
        log_validation "Backend Service" "PASS" "$service directory found"
        
        # Check for Dockerfile
        if [ -f "backend/$service/Dockerfile" ] || [ -f "$service/Dockerfile" ]; then
            log_validation "Backend Service" "PASS" "$service Dockerfile found"
        else
            log_validation "Backend Service" "WARN" "$service Dockerfile not found"
        fi
    else
        log_validation "Backend Service" "FAIL" "$service directory not found"
    fi
done

echo ""

# 5. Database Migration Validation
echo -e "${BLUE}🗄️  Validating Database Migrations...${NC}"
if [ -d "backend" ]; then
    # Check for Flyway migrations
    migration_dirs=$(find backend -name "db" -type d 2>/dev/null | wc -l)
    if [ "$migration_dirs" -gt 0 ]; then
        log_validation "Database Migrations" "PASS" "Found $migration_dirs migration directories"
    else
        log_validation "Database Migrations" "WARN" "No migration directories found"
    fi
else
    log_validation "Database Migrations" "FAIL" "Backend directory not found"
fi

echo ""

# 6. Security Validation
echo -e "${BLUE}🔒 Validating Security Configuration...${NC}"

# Check for hardcoded credentials
if grep -r "postgres123\|smartwatts123\|demo-key" . --exclude-dir=node_modules --exclude-dir=.git --exclude="*.log" >/dev/null 2>&1; then
    log_validation "Security" "FAIL" "Hardcoded credentials found in codebase"
else
    log_validation "Security" "PASS" "No hardcoded credentials found"
fi

# Check for .env files in git
if git ls-files | grep -E "\.env$" >/dev/null 2>&1; then
    log_validation "Security" "FAIL" ".env files found in git (should be in .gitignore)"
else
    log_validation "Security" "PASS" "No .env files in git"
fi

echo ""

# 7. SSL/TLS Configuration
echo -e "${BLUE}🔐 Validating SSL/TLS Configuration...${NC}"
if [ -d "ssl" ] && [ -f "ssl/cert.pem" ] && [ -f "ssl/key.pem" ]; then
    log_validation "SSL/TLS" "PASS" "SSL certificates found"
    
    # Check certificate validity
    if openssl x509 -in ssl/cert.pem -text -noout >/dev/null 2>&1; then
        log_validation "SSL/TLS" "PASS" "SSL certificate is valid"
    else
        log_validation "SSL/TLS" "FAIL" "SSL certificate is invalid"
    fi
else
    log_validation "SSL/TLS" "WARN" "SSL certificates not found (required for production)"
fi

echo ""

# 8. Monitoring and Logging
echo -e "${BLUE}📊 Validating Monitoring Configuration...${NC}"
if [ -f "docker-compose.monitoring.yml" ]; then
    log_validation "Monitoring" "PASS" "Monitoring configuration found"
else
    log_validation "Monitoring" "WARN" "Monitoring configuration not found"
fi

if [ -f "backend/logback-spring.xml" ]; then
    log_validation "Logging" "PASS" "Logging configuration found"
else
    log_validation "Logging" "WARN" "Logging configuration not found"
fi

echo ""

# 9. Testing Validation
echo -e "${BLUE}🧪 Validating Test Configuration...${NC}"
if [ -f "frontend/jest.config.js" ]; then
    log_validation "Frontend Testing" "PASS" "Jest configuration found"
else
    log_validation "Frontend Testing" "WARN" "Jest configuration not found"
fi

if [ -f "frontend/playwright.config.ts" ]; then
    log_validation "E2E Testing" "PASS" "Playwright configuration found"
else
    log_validation "E2E Testing" "WARN" "Playwright configuration not found"
fi

# Check for test files
test_files=$(find . -name "*.test.*" -o -name "*.spec.*" | wc -l)
if [ "$test_files" -gt 0 ]; then
    log_validation "Testing" "PASS" "Found $test_files test files"
else
    log_validation "Testing" "WARN" "No test files found"
fi

echo ""

# 10. PWA Validation
echo -e "${BLUE}📱 Validating PWA Configuration...${NC}"
if [ -f "frontend/public/manifest.json" ]; then
    log_validation "PWA Manifest" "PASS" "PWA manifest found"
else
    log_validation "PWA Manifest" "FAIL" "PWA manifest not found"
fi

if [ -f "frontend/public/service-worker.js" ]; then
    log_validation "PWA Service Worker" "PASS" "Service worker found"
else
    log_validation "PWA Service Worker" "FAIL" "Service worker not found"
fi

# Check for PWA icons
icon_files=$(find frontend/public/icons -name "*.png" 2>/dev/null | wc -l)
if [ "$icon_files" -gt 0 ]; then
    log_validation "PWA Icons" "PASS" "Found $icon_files PWA icons"
else
    log_validation "PWA Icons" "WARN" "PWA icons not found"
fi

echo ""

# Final Results
echo "=============================================="
if [ $VALIDATION_FAILED -eq 1 ]; then
    echo -e "${RED}❌ Pre-deployment validation FAILED${NC}"
    echo ""
    echo "Please fix the issues above before proceeding with deployment."
    echo ""
    echo "Common fixes:"
    echo "1. Run: ./scripts/validate-env.sh $ENV_FILE"
    echo "2. Install missing dependencies: npm install"
    echo "3. Build frontend: cd frontend && npm run build"
    echo "4. Generate SSL certificates: ./ssl/generate-certificates.sh"
    echo "5. Run tests: npm test"
    exit 1
else
    echo -e "${GREEN}✅ Pre-deployment validation PASSED${NC}"
    echo ""
    echo "All validation checks passed. You can proceed with deployment."
    echo ""
    echo "Next steps:"
    echo "1. Run: docker-compose up -d"
    echo "2. Verify all services are running: ./scripts/health-check-all.sh"
    echo "3. Monitor deployment: docker-compose logs -f"
    exit 0
fi







