#!/bin/bash

###############################################################################
# Generate Cloud Run Optimized Dockerfiles
# 
# Purpose: Generate Dockerfile.cloudrun for all services based on a template
#
# Usage: ./generate-cloudrun-dockerfiles.sh
#
###############################################################################

set -euo pipefail

# Service configurations: service-name:port (space-separated pairs)
SERVICES=(
    "api-gateway:8080"
    "user-service:8081"
    "energy-service:8082"
    "device-service:8083"
    "analytics-service:8084"
    "billing-service:8085"
    "service-discovery:8761"
    "edge-gateway:8088"
    "facility-service:8089"
    "feature-flag-service:8090"
    "device-verification-service:8091"
    "appliance-monitoring-service:8092"
    "notification-service:8093"
)

# Template for Cloud Run Dockerfile
generate_dockerfile() {
    local service_name=$1
    local port=$2
    local dockerfile_path="backend/${service_name}/Dockerfile.cloudrun"
    
    cat > "${dockerfile_path}" <<EOF
# Multi-stage build for Spring Boot application optimized for Cloud Run
# This Dockerfile is optimized for GCP Cloud Run with:
# - Non-root user execution
# - Minimal base image (Alpine)
# - Health check support
# - Graceful shutdown handling
# - Cloud Run environment variable support

# Builder stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy gradle files
COPY gradlew .
RUN mkdir -p gradle/wrapper
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Extract application layers for better caching
RUN java -Djarmode=layertools -jar build/libs/*.jar extract

# Runtime stage - using Alpine for minimal size with shell support
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy extracted layers
COPY --from=builder --chown=spring:spring /app/dependencies/ ./
COPY --from=builder --chown=spring:spring /app/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /app/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /app/application/ ./

# Install curl for health checks
RUN apk add --no-cache curl

# Switch to non-root user
USER spring:spring

# Expose port (Cloud Run uses PORT environment variable)
EXPOSE ${port}

# Set JVM options optimized for Cloud Run
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \\
    CMD curl -f http://localhost:\${PORT:-${port}}/actuator/health || exit 1

# Cloud Run requires the application to listen on the PORT environment variable
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
EOF

    echo "Generated: ${dockerfile_path}"
}

# Generate Dockerfiles for all services
main() {
    echo "Generating Cloud Run optimized Dockerfiles..."
    echo ""
    
    for service_config in "${SERVICES[@]}"; do
        IFS=':' read -r service_name port <<< "$service_config"
        
        # Check if service directory exists
        if [ ! -d "backend/${service_name}" ]; then
            echo "Warning: Service directory backend/${service_name} not found, skipping"
            continue
        fi
        
        generate_dockerfile "${service_name}" "${port}"
    done
    
    echo ""
    echo "All Dockerfiles generated successfully!"
}

main "$@"

