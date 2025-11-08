#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Environment Deployment"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
STAGING_ENV_FILE=".env.staging"
DOCKER_COMPOSE_FILE="docker-compose.staging.yml"

# Check if staging environment file exists
if [ ! -f "$STAGING_ENV_FILE" ]; then
    echo -e "${YELLOW}Warning: $STAGING_ENV_FILE not found. Creating from template...${NC}"
    cp env.template "$STAGING_ENV_FILE"
    echo -e "${YELLOW}Please update $STAGING_ENV_FILE with staging-specific values${NC}"
fi

# Load environment variables
if [ -f "$STAGING_ENV_FILE" ]; then
    export $(cat "$STAGING_ENV_FILE" | grep -v '^#' | xargs)
fi

echo -e "${GREEN}Step 1: Building all services...${NC}"
docker-compose -f "$DOCKER_COMPOSE_FILE" build

echo -e "${GREEN}Step 2: Starting staging infrastructure...${NC}"
docker-compose -f "$DOCKER_COMPOSE_FILE" up -d postgres-staging redis-staging

echo -e "${GREEN}Waiting for infrastructure to be ready...${NC}"
sleep 10

echo -e "${GREEN}Step 3: Starting service discovery...${NC}"
docker-compose -f "$DOCKER_COMPOSE_FILE" up -d service-discovery-staging

echo -e "${GREEN}Waiting for service discovery to be ready...${NC}"
sleep 15

echo -e "${GREEN}Step 4: Starting API Gateway...${NC}"
docker-compose -f "$DOCKER_COMPOSE_FILE" up -d api-gateway-staging

echo -e "${GREEN}Step 5: Starting all microservices...${NC}"
docker-compose -f "$DOCKER_COMPOSE_FILE" up -d

echo -e "${GREEN}Step 6: Waiting for all services to be healthy...${NC}"
sleep 30

echo -e "${GREEN}Step 7: Running health checks...${NC}"
./scripts/health-check-staging.sh

echo -e "${GREEN}Step 8: Running database migrations...${NC}"
./scripts/migrate-staging.sh

echo -e "${GREEN}Step 9: Seeding test data...${NC}"
./scripts/seed-staging-data.sh

echo -e "${GREEN}Step 10: Running smoke tests...${NC}"
./scripts/smoke-tests-staging.sh

echo -e "${GREEN}=========================================="
echo "Staging deployment completed successfully!"
echo "=========================================="
echo -e "${NC}"

# Display service URLs
echo "Service URLs:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Service Discovery: http://localhost:8762"
echo "  - User Service: http://localhost:8081"
echo "  - Energy Service: http://localhost:8082"
echo "  - Device Service: http://localhost:8083"
echo "  - Analytics Service: http://localhost:8084"
echo "  - Billing Service: http://localhost:8085"
echo "  - Appliance Monitoring: http://localhost:8087"
echo "  - Feature Flag Service: http://localhost:8090"

