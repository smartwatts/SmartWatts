#!/bin/bash

###############################################################################
# Build and Push Docker Images
# 
# Purpose: Build and push all service images to Artifact Registry
#
# Usage: ./build-and-push-images.sh [environment]
#
###############################################################################

set -euo pipefail

ENVIRONMENT="${1:-staging}"
PROJECT_ID="smartwatts-${ENVIRONMENT}"
REGION="europe-west1"

SERVICES=(
    "api-gateway"
    "user-service"
    "energy-service"
    "device-service"
    "analytics-service"
    "billing-service"
    "service-discovery"
    "edge-gateway"
    "facility-service"
    "feature-flag-service"
    "device-verification-service"
    "appliance-monitoring-service"
    "notification-service"
)

echo "Building and pushing images for ${ENVIRONMENT}"

# Verify authentication
echo "Verifying authentication..."
gcloud auth list

# Configure Docker authentication
echo "Configuring Docker for Artifact Registry..."
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

# Explicitly authenticate Docker with access token
echo "Authenticating Docker with access token..."
gcloud auth print-access-token | docker login -u oauth2accesstoken --password-stdin "${REGION}-docker.pkg.dev" || {
    echo "Error: Failed to authenticate Docker"
    exit 1
}

# Build and push each service
for service_name in "${SERVICES[@]}"; do
    echo "Building ${service_name}..."
    
    IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/${service_name}/${service_name}:latest"
    
    # Build image
    docker build \
        -f "backend/${service_name}/Dockerfile.cloudrun" \
        -t "${IMAGE}" \
        "backend/${service_name}" || {
        echo "Failed to build ${service_name}"
        exit 1
    }
    
    # Push image
    docker push "${IMAGE}" || {
        echo "Failed to push ${service_name}"
        exit 1
    }
    
    echo "✓ ${service_name} built and pushed"
done

echo "All images built and pushed successfully"

