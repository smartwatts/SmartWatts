#!/bin/bash

###############################################################################
# Generate Cloud Run Configuration Files
# 
# Purpose: Generate Cloud Run service YAML configurations for all services
#
# Usage: ./generate-cloudrun-configs.sh
#
###############################################################################

set -euo pipefail

# Service configurations: service-name:port:memory:cpu
SERVICES=(
    "api-gateway:8080:2Gi:2"
    "user-service:8081:1Gi:1"
    "energy-service:8082:1Gi:1"
    "device-service:8083:1Gi:1"
    "analytics-service:8084:1Gi:1"
    "billing-service:8085:1Gi:1"
    "service-discovery:8761:512Mi:1"
    "edge-gateway:8088:1Gi:1"
    "facility-service:8089:1Gi:1"
    "feature-flag-service:8090:512Mi:1"
    "device-verification-service:8091:512Mi:1"
    "appliance-monitoring-service:8092:1Gi:1"
    "notification-service:8093:512Mi:1"
)

# Function to get database name for service
get_database_name() {
    local service_name=$1
    case "$service_name" in
        "user-service") echo "smartwatts_users" ;;
        "energy-service") echo "smartwatts_energy" ;;
        "device-service") echo "smartwatts_devices" ;;
        "analytics-service") echo "smartwatts_analytics" ;;
        "billing-service") echo "smartwatts_billing" ;;
        "facility-service") echo "smartwatts_facility360" ;;
        "feature-flag-service") echo "smartwatts_feature_flags" ;;
        "device-verification-service") echo "smartwatts_device_verification" ;;
        "appliance-monitoring-service") echo "smartwatts_appliance_monitoring" ;;
        *) echo "" ;;
    esac
}

generate_cloudrun_config() {
    local service_name=$1
    local port=$2
    local memory=$3
    local cpu=$4
    local config_file="gcp-migration/cloud-run-configs/${service_name}.yaml"
    
    # Get database name if service uses database
    local database_name=$(get_database_name "$service_name")
    
    # Determine if service needs database
    local needs_database="false"
    if [ -n "$database_name" ]; then
        needs_database="true"
    fi
    
    cat > "${config_file}" <<EOF
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: ${service_name}
  annotations:
    run.googleapis.com/ingress: all
    run.googleapis.com/execution-environment: gen2
spec:
  template:
    metadata:
      annotations:
        autoscaling.knative.dev/minScale: "1"
        autoscaling.knative.dev/maxScale: "10"
        run.googleapis.com/cpu-throttling: "false"
        run.googleapis.com/execution-environment: gen2
    spec:
      containerConcurrency: 80
      timeoutSeconds: 300
      serviceAccountName: cloud-run-sa@PROJECT_ID.iam.gserviceaccount.com
      containers:
      - image: REGION-docker.pkg.dev/PROJECT_ID/REPOSITORY/${service_name}:latest
        ports:
        - name: http1
          containerPort: ${port}
        env:
        - name: PORT
          value: "${port}"
        - name: SPRING_PROFILES_ACTIVE
          value: "cloudrun"
$(if [ "$needs_database" = "true" ]; then
cat <<DBENV
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql:///DATABASE_NAME?cloudSqlInstance=PROJECT_ID:REGION:INSTANCE_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: postgres-username
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-password
              key: password
DBENV
fi)
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: "http://service-discovery:8761/eureka/"
        - name: SPRING_DATA_REDIS_HOST
          value: "redis-service"
        - name: SPRING_DATA_REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: redis-password
              key: password
        resources:
          limits:
            cpu: "${cpu}"
            memory: ${memory}
          requests:
            cpu: "$(echo "$cpu / 2" | bc)"
            memory: "$(echo "$memory" | sed 's/Gi//' | awk '{print $1/2"Gi"}')"
        startupProbe:
          httpGet:
            path: /actuator/health
            port: ${port}
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: ${port}
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
$(if [ "$needs_database" = "true" ]; then
cat <<CLOUDSQL
      cloudSqlInstances:
      - PROJECT_ID:REGION:INSTANCE_NAME
CLOUDSQL
fi)
EOF

    echo "Generated: ${config_file}"
}

# Main execution
main() {
    echo "Generating Cloud Run configuration files..."
    echo ""
    
    mkdir -p gcp-migration/cloud-run-configs
    
    for service_config in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port memory cpu <<< "$service_config"
        generate_cloudrun_config "${service_name}" "${port}" "${memory}" "${cpu}"
    done
    
    echo ""
    echo "All Cloud Run configurations generated successfully!"
    echo ""
    echo "Note: Replace the following placeholders in the generated files:"
    echo "  - PROJECT_ID: Your GCP project ID"
    echo "  - REGION: Your GCP region (e.g., europe-west1)"
    echo "  - REPOSITORY: Your Artifact Registry repository name"
    echo "  - INSTANCE_NAME: Your Cloud SQL instance name"
    echo "  - DATABASE_NAME: Database name for each service"
}

main "$@"

