#!/bin/bash

# Implement Redis Caching Configuration
# Adds Spring Cache configuration to services

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BACKEND_DIR="backend"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Implementing Redis Caching${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Services that should use caching
CACHEABLE_SERVICES=(
    "user-service"
    "device-service"
    "feature-flag-service"
    "analytics-service"
)

# Cache configuration
CACHE_CONFIG=$(cat <<'EOF'
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "smartwatts:cache:"
EOF
)

for service in "${CACHEABLE_SERVICES[@]}"; do
    config_file="${BACKEND_DIR}/${service}/src/main/resources/application-cloudrun.yml"
    
    if [ ! -f "$config_file" ]; then
        echo -e "${YELLOW}Skipping ${service} (config file not found)${NC}"
        continue
    fi
    
    echo -e "${YELLOW}Updating ${service}...${NC}"
    
    # Check if cache config already exists
    if grep -q "cache:" "$config_file"; then
        echo -e "${GREEN}  ✓ ${service} already has cache configuration${NC}"
        echo -e "${YELLOW}  Review and update manually if needed${NC}"
    else
        # Add cache config
        echo "" >> "$config_file"
        echo "# Redis Cache Configuration" >> "$config_file"
        echo "$CACHE_CONFIG" >> "$config_file"
        echo -e "${GREEN}  ✓ Added cache configuration to ${service}${NC}"
    fi
done

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Cache Configuration Complete${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Next steps:"
echo "  1. Add @EnableCaching to service configuration classes"
echo "  2. Add @Cacheable annotations to read methods"
echo "  3. Add @CacheEvict annotations to write methods"
echo "  4. Rebuild and redeploy services"
echo ""
echo "Example annotations:"
echo "  @Cacheable(value = \"users\", key = \"#id\")"
echo "  @CacheEvict(value = \"users\", key = \"#user.id\")"

