#!/bin/bash

# SmartWatts Production Environment Generation Script
# This script generates a secure production .env file with random passwords

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
OUTPUT_FILE=${1:-.env.production}
TEMPLATE_FILE="env.template"

echo -e "${BLUE}🔐 SmartWatts Production Environment Generator${NC}"
echo "=============================================="
echo ""

# Check if template exists
if [ ! -f "$TEMPLATE_FILE" ]; then
    echo -e "${RED}❌ Template file '$TEMPLATE_FILE' not found${NC}"
    exit 1
fi

# Function to generate random password
generate_password() {
    local length=${1:-16}
    openssl rand -base64 $length | tr -d "=+/" | cut -c1-$length
}

# Function to generate JWT secret
generate_jwt_secret() {
    openssl rand -base64 64 | tr -d "=+/"
}

# Function to generate API key placeholder
generate_api_key_placeholder() {
    echo "REPLACE_WITH_ACTUAL_API_KEY_$(openssl rand -hex 8)"
}

echo -e "${BLUE}🔑 Generating secure passwords and secrets...${NC}"

# Generate secure values
POSTGRES_PASSWORD=$(generate_password 20)
REDIS_PASSWORD=$(generate_password 20)
JWT_SECRET=$(generate_jwt_secret)
SSL_KEYSTORE_PASSWORD=$(generate_password 24)
SSL_TRUSTSTORE_PASSWORD=$(generate_password 24)
GRAFANA_ADMIN_PASSWORD=$(generate_password 20)
OPENWEATHER_API_KEY=$(generate_api_key_placeholder)

echo -e "${GREEN}✅ Generated secure passwords${NC}"

# Create production environment file
cat > "$OUTPUT_FILE" << EOF
# SmartWatts Production Environment Configuration
# Generated on $(date)
# DO NOT commit this file to version control

# ===========================================
# Database Configuration
# ===========================================
POSTGRES_DB=smartwatts
POSTGRES_USER=smartwatts_prod
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=$REDIS_PASSWORD

# ===========================================
# JWT Configuration
# ===========================================
JWT_SECRET=$JWT_SECRET
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# ===========================================
# API Keys
# ===========================================
OPENWEATHER_API_KEY=$OPENWEATHER_API_KEY

# ===========================================
# Service Discovery
# ===========================================
EUREKA_SERVER_URL=http://service-discovery:8761/eureka/

# ===========================================
# Security
# ===========================================
BCRYPT_ROUNDS=12

# ===========================================
# Logging
# ===========================================
LOG_LEVEL=WARN
LOG_FORMAT=json

# ===========================================
# Production Settings
# ===========================================
SPRING_PROFILES_ACTIVE=production

# ===========================================
# SSL Configuration
# ===========================================
SSL_ENABLED=true
SSL_KEYSTORE_PASSWORD=$SSL_KEYSTORE_PASSWORD
SSL_TRUSTSTORE_PASSWORD=$SSL_TRUSTSTORE_PASSWORD

# ===========================================
# Monitoring
# ===========================================
PROMETHEUS_ENABLED=true
GRAFANA_ADMIN_PASSWORD=$GRAFANA_ADMIN_PASSWORD

# ===========================================
# Backup Configuration
# ===========================================
BACKUP_RETENTION_DAYS=30
BACKUP_DIR=/var/backups/smartwatts

# ===========================================
# Cloud Storage (Optional)
# ===========================================
# AWS_ACCESS_KEY_ID=your_access_key
# AWS_SECRET_ACCESS_KEY=your_secret_key
# AWS_S3_BUCKET=your-backup-bucket
# AWS_DEFAULT_REGION=us-east-1
EOF

echo -e "${GREEN}✅ Production environment file created: $OUTPUT_FILE${NC}"
echo ""

# Create a secure backup of the generated file
BACKUP_FILE="${OUTPUT_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
cp "$OUTPUT_FILE" "$BACKUP_FILE"
echo -e "${BLUE}📁 Backup created: $BACKUP_FILE${NC}"

echo ""
echo -e "${YELLOW}⚠️  IMPORTANT SECURITY NOTES:${NC}"
echo "1. Store this file securely and never commit it to version control"
echo "2. Update OPENWEATHER_API_KEY with your actual API key"
echo "3. Consider using a secrets management service for production"
echo "4. Rotate passwords regularly"
echo "5. The backup file contains the same sensitive data - secure it appropriately"
echo ""

echo -e "${BLUE}🔍 Next steps:${NC}"
echo "1. Review the generated file: $OUTPUT_FILE"
echo "2. Update OPENWEATHER_API_KEY with your actual key"
echo "3. Run validation: ./scripts/validate-env.sh $OUTPUT_FILE"
echo "4. Deploy with: docker-compose --env-file $OUTPUT_FILE up -d"
echo ""

echo -e "${GREEN}✅ Production environment generation completed${NC}"










