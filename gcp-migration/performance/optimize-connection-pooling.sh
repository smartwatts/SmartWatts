#!/bin/bash

# Optimize Database Connection Pooling
# Updates application-cloudrun.yml files with optimized HikariCP settings

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BACKEND_DIR="backend"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Optimizing Connection Pooling${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Services that use databases
DB_SERVICES=(
    "user-service"
    "billing-service"
    "analytics-service"
    "device-service"
    "energy-service"
    "facility-service"
    "feature-flag-service"
)

# Optimized HikariCP settings
POOL_CONFIG=$(cat <<'EOF'
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      pool-name: SmartWattsHikariPool
EOF
)

for service in "${DB_SERVICES[@]}"; do
    config_file="${BACKEND_DIR}/${service}/src/main/resources/application-cloudrun.yml"
    
    if [ ! -f "$config_file" ]; then
        echo -e "${YELLOW}Skipping ${service} (config file not found)${NC}"
        continue
    fi
    
    echo -e "${YELLOW}Updating ${service}...${NC}"
    
    # Check if hikari config already exists
    if grep -q "hikari:" "$config_file"; then
        # Update existing hikari config
        # This is a simplified update - in production, use yq or similar tool
        echo -e "${GREEN}  ✓ ${service} already has HikariCP configuration${NC}"
        echo -e "${YELLOW}  Review and update manually if needed${NC}"
    else
        # Add hikari config
        echo "" >> "$config_file"
        echo "# Optimized HikariCP Connection Pool Settings" >> "$config_file"
        echo "$POOL_CONFIG" >> "$config_file"
        echo -e "${GREEN}  ✓ Added HikariCP configuration to ${service}${NC}"
    fi
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Connection Pooling Optimization Complete${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Configuration added:"
echo "  - maximum-pool-size: 20"
echo "  - minimum-idle: 10"
echo "  - connection-timeout: 30000ms"
echo "  - idle-timeout: 600000ms (10 minutes)"
echo "  - max-lifetime: 1800000ms (30 minutes)"
echo ""
echo "Note: Rebuild and redeploy services for changes to take effect."

