#!/bin/bash

# SmartWatts Kubernetes Secrets Generation Script
# This script generates secure Kubernetes secrets for production deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
OUTPUT_FILE=${1:-k8s/secrets-production.yaml}
NAMESPACE=${2:-smartwatts}

echo -e "${BLUE}🔐 SmartWatts Kubernetes Secrets Generator${NC}"
echo "=============================================="
echo ""

# Function to generate random password
generate_password() {
    local length=${1:-20}
    openssl rand -base64 $length | tr -d "=+/" | cut -c1-$length
}

# Function to generate JWT secret
generate_jwt_secret() {
    openssl rand -base64 64 | tr -d "=+/"
}

# Function to base64 encode
base64_encode() {
    echo -n "$1" | base64
}

echo -e "${BLUE}🔑 Generating secure secrets...${NC}"

# Generate secure values
POSTGRES_USER="smartwatts_prod"
POSTGRES_PASSWORD=$(generate_password 24)
REDIS_PASSWORD=$(generate_password 24)
JWT_SECRET=$(generate_jwt_secret)
SSL_KEYSTORE_PASSWORD=$(generate_password 32)
SSL_TRUSTSTORE_PASSWORD=$(generate_password 32)
GRAFANA_ADMIN_PASSWORD=$(generate_password 24)

echo -e "${GREEN}✅ Generated secure secrets${NC}"

# Create Kubernetes secrets file
cat > "$OUTPUT_FILE" << EOF
apiVersion: v1
kind: Secret
metadata:
  name: smartwatts-secrets
  namespace: $NAMESPACE
  labels:
    app.kubernetes.io/name: smartwatts
    app.kubernetes.io/component: secrets
type: Opaque
data:
  # Database credentials (base64 encoded)
  POSTGRES_USER: $(base64_encode "$POSTGRES_USER")
  POSTGRES_PASSWORD: $(base64_encode "$POSTGRES_PASSWORD")
  
  # Redis password
  REDIS_PASSWORD: $(base64_encode "$REDIS_PASSWORD")
  
  # JWT secrets
  JWT_SECRET: $(base64_encode "$JWT_SECRET")
  
  # SSL keystore passwords
  SSL_KEYSTORE_PASSWORD: $(base64_encode "$SSL_KEYSTORE_PASSWORD")
  SSL_TRUSTSTORE_PASSWORD: $(base64_encode "$SSL_TRUSTSTORE_PASSWORD")
  
  # Grafana admin password
  GRAFANA_ADMIN_PASSWORD: $(base64_encode "$GRAFANA_ADMIN_PASSWORD")
  
  # API keys and external service credentials
  AWS_ACCESS_KEY_ID: ""  # Set via external secret management
  AWS_SECRET_ACCESS_KEY: ""  # Set via external secret management
  SENDGRID_API_KEY: ""  # Set via external secret management
  TWILIO_ACCOUNT_SID: ""  # Set via external secret management
  TWILIO_AUTH_TOKEN: ""  # Set via external secret management
---
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
  namespace: $NAMESPACE
  labels:
    app.kubernetes.io/name: postgres
    app.kubernetes.io/component: database
type: Opaque
data:
  POSTGRES_USER: $(base64_encode "$POSTGRES_USER")
  POSTGRES_PASSWORD: $(base64_encode "$POSTGRES_PASSWORD")
  POSTGRES_DB: $(base64_encode "smartwatts")
---
apiVersion: v1
kind: Secret
metadata:
  name: redis-secret
  namespace: $NAMESPACE
  labels:
    app.kubernetes.io/name: redis
    app.kubernetes.io/component: cache
type: Opaque
data:
  REDIS_PASSWORD: $(base64_encode "$REDIS_PASSWORD")
---
apiVersion: v1
kind: Secret
metadata:
  name: tls-secret
  namespace: $NAMESPACE
  labels:
    app.kubernetes.io/name: smartwatts
    app.kubernetes.io/component: tls
type: kubernetes.io/tls
data:
  # These will be populated by cert-manager or manually
  tls.crt: ""
  tls.key: ""
EOF

echo -e "${GREEN}✅ Kubernetes secrets file created: $OUTPUT_FILE${NC}"
echo ""

# Create a secure backup of the generated file
BACKUP_FILE="${OUTPUT_FILE}.backup.$(date +%Y%m%d_%H%M%S)"
cp "$OUTPUT_FILE" "$BACKUP_FILE"
echo -e "${BLUE}📁 Backup created: $BACKUP_FILE${NC}"

echo ""
echo -e "${YELLOW}⚠️  IMPORTANT SECURITY NOTES:${NC}"
echo "1. Store this file securely and never commit it to version control"
echo "2. Consider using a secrets management service (HashiCorp Vault, AWS Secrets Manager)"
echo "3. Rotate secrets regularly"
echo "4. The backup file contains the same sensitive data - secure it appropriately"
echo ""

echo -e "${BLUE}🔍 Next steps:${NC}"
echo "1. Review the generated file: $OUTPUT_FILE"
echo "2. Apply secrets to Kubernetes: kubectl apply -f $OUTPUT_FILE"
echo "3. Verify secrets: kubectl get secrets -n $NAMESPACE"
echo "4. Update your deployments to reference these secrets"
echo ""

echo -e "${GREEN}✅ Kubernetes secrets generation completed${NC}"







