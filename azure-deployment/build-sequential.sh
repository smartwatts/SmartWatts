#!/bin/bash
# Sequential Docker build script for Azure deployment
# Builds services in dependency order to optimize resource usage on B1s VM

set -e

COMPOSE_FILE="docker-compose.azure.yml"
START_TIME=$(date +%s)
TOTAL_SERVICES=0
BUILT_SERVICES=0

# Enable BuildKit for better caching
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_build() {
    local service=$1
    local start_time=$2
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    BUILT_SERVICES=$((BUILT_SERVICES + 1))
    local progress=$((BUILT_SERVICES * 100 / TOTAL_SERVICES))
    echo -e "${GREEN}✓${NC} Built $service in ${duration}s (Progress: ${progress}%)"
}

build_service() {
    local service=$1
    local start_time=$(date +%s)
    echo -e "${BLUE}Building $service...${NC} (Started at $(date '+%H:%M:%S'))"
    
    if docker-compose -f $COMPOSE_FILE build --progress=plain $service 2>&1 | tee /tmp/build-$service.log; then
        log_build $service $start_time
        return 0
    else
        echo -e "${YELLOW}Error: Failed to build $service${NC}"
        echo "Last 20 lines of build log:"
        tail -20 /tmp/build-$service.log
        return 1
    fi
}

# Create shared Gradle cache volume
echo "Creating shared Gradle cache volume..."
docker volume create gradle-cache 2>/dev/null || echo "Gradle cache volume already exists"

# Pre-download Gradle 8.5 distribution
echo "Pre-downloading Gradle 8.5 distribution..."
GRADLE_VERSION="8.5"
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_CACHE_DIR="/tmp/gradle-cache"

mkdir -p $GRADLE_CACHE_DIR
if [ ! -f "$GRADLE_CACHE_DIR/gradle-${GRADLE_VERSION}-bin.zip" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    curl -L -o "$GRADLE_CACHE_DIR/gradle-${GRADLE_VERSION}-bin.zip" "$GRADLE_DIST_URL" || {
        echo "Warning: Could not pre-download Gradle, builds will download it"
    }
else
    echo "Gradle ${GRADLE_VERSION} already downloaded"
fi

# Pull infrastructure images first (quick, no build needed)
echo -e "${BLUE}Pulling infrastructure images...${NC}"
docker-compose -f $COMPOSE_FILE pull postgres redis 2>/dev/null || true

# Count total services to build
TOTAL_SERVICES=$(grep -E "^\s+[a-z-]+:" $COMPOSE_FILE | grep -v "postgres\|redis" | wc -l)
echo -e "${BLUE}Total services to build: $TOTAL_SERVICES${NC}"
echo ""

# Build services in dependency order
# 1. Core services (no dependencies on other services)
build_service "service-discovery"

# 2. Services that depend on service-discovery
build_service "api-gateway"
build_service "user-service"

# 3. Application services (can build in any order, but sequential for resource management)
build_service "energy-service"
build_service "device-service"
build_service "analytics-service"
build_service "billing-service"
build_service "facility-service"
build_service "notification-service"
build_service "feature-flag-service"
build_service "device-verification-service"
build_service "appliance-monitoring-service"
build_service "edge-gateway"

# 4. Frontend (independent, build last)
build_service "frontend"

# Calculate total build time
END_TIME=$(date +%s)
TOTAL_DURATION=$((END_TIME - START_TIME))
MINUTES=$((TOTAL_DURATION / 60))
SECONDS=$((TOTAL_DURATION % 60))

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}All services built successfully!${NC}"
echo -e "${GREEN}Total build time: ${MINUTES}m ${SECONDS}s${NC}"
echo -e "${GREEN}========================================${NC}"

# Start all containers
echo ""
echo -e "${BLUE}Starting all containers...${NC}"
docker-compose -f $COMPOSE_FILE up -d

echo -e "${GREEN}Deployment complete!${NC}"

