#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Health Checks"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Service URLs
SERVICES=(
    "http://localhost:8080/actuator/health:API Gateway"
    "http://localhost:8762/actuator/health:Service Discovery"
    "http://localhost:8081/actuator/health:User Service"
    "http://localhost:8082/actuator/health:Energy Service"
    "http://localhost:8083/actuator/health:Device Service"
    "http://localhost:8084/actuator/health:Analytics Service"
    "http://localhost:8085/actuator/health:Billing Service"
    "http://localhost:8087/actuator/health:Appliance Monitoring Service"
    "http://localhost:8090/actuator/health:Feature Flag Service"
)

FAILED_SERVICES=()

for service in "${SERVICES[@]}"; do
    IFS=':' read -r url name <<< "$service"
    echo -n "Checking $name... "
    
    if curl -f -s "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Healthy${NC}"
    else
        echo -e "${RED}✗ Unhealthy${NC}"
        FAILED_SERVICES+=("$name")
    fi
done

if [ ${#FAILED_SERVICES[@]} -eq 0 ]; then
    echo -e "\n${GREEN}All services are healthy!${NC}"
    exit 0
else
    echo -e "\n${RED}The following services are unhealthy:${NC}"
    for service in "${FAILED_SERVICES[@]}"; do
        echo -e "${RED}  - $service${NC}"
    done
    exit 1
fi

