#!/bin/bash

###############################################################################
# Update Database Connection Strings Script
# 
# Purpose: Update Spring Boot application.yml files with Cloud SQL connection strings
#
# Usage: ./update-connection-strings.sh [environment] [cloud-sql-instance]
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"
CLOUD_SQL_INSTANCE="${2:-}"

# Get Cloud SQL instance if not provided
if [ -z "$CLOUD_SQL_INSTANCE" ]; then
    CLOUD_SQL_INSTANCE=$(gcloud sql instances describe "smartwatts-${ENVIRONMENT}-db" \
        --project="${PROJECT_ID}" \
        --format="value(connectionName)" 2>/dev/null || echo "")
fi

# Database mapping
declare -A DB_MAP=(
    ["user-service"]="smartwatts_users"
    ["energy-service"]="smartwatts_energy"
    ["device-service"]="smartwatts_devices"
    ["analytics-service"]="smartwatts_analytics"
    ["billing-service"]="smartwatts_billing"
    ["facility-service"]="smartwatts_facility360"
    ["feature-flag-service"]="smartwatts_feature_flags"
    ["device-verification-service"]="smartwatts_device_verification"
    ["appliance-monitoring-service"]="smartwatts_appliance_monitoring"
)

for service in "${!DB_MAP[@]}"; do
    db_name="${DB_MAP[$service]}"
    config_file="backend/${service}/src/main/resources/application-cloudrun.yml"
    
    mkdir -p "$(dirname "$config_file")"
    
    cat > "$config_file" <<EOF
spring:
  datasource:
    url: jdbc:postgresql:///${db_name}?cloudSqlInstance=${CLOUD_SQL_INSTANCE}&socketFactory=com.google.cloud.sql.postgres.SocketFactory
    username: \${POSTGRES_USER:postgres}
    password: \${POSTGRES_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
EOF
    
    echo "Updated: $config_file"
done

echo "Connection strings updated for Cloud SQL"

