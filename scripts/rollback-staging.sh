#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Environment Rollback"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

DOCKER_COMPOSE_FILE="docker-compose.staging.yml"

read -p "Are you sure you want to rollback the staging environment? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo -e "${YELLOW}Rollback cancelled.${NC}"
    exit 0
fi

echo -e "${YELLOW}Stopping all staging services...${NC}"
docker-compose -f "$DOCKER_COMPOSE_FILE" down

echo -e "${YELLOW}Removing staging volumes...${NC}"
read -p "Do you want to remove staging volumes? This will delete all staging data. (yes/no): " remove_volumes

if [ "$remove_volumes" == "yes" ]; then
    docker-compose -f "$DOCKER_COMPOSE_FILE" down -v
    echo -e "${RED}Staging volumes removed.${NC}"
else
    echo -e "${GREEN}Staging volumes preserved.${NC}"
fi

echo -e "${GREEN}Rollback completed!${NC}"

