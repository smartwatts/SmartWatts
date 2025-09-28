#!/bin/bash

# SmartWatts Production Deployment Script
# This script deploys SmartWatts to production with all necessary configurations

echo "🚀 SmartWatts Production Deployment Starting..."
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT=${1:-production}
LOG_LEVEL=${2:-INFO}

echo -e "${BLUE}Environment: $ENVIRONMENT${NC}"
echo -e "${BLUE}Log Level: $LOG_LEVEL${NC}"
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check prerequisites
check_prerequisites() {
    echo "🔍 Checking prerequisites..."
    
    if ! command_exists docker; then
        echo -e "${RED}❌ Docker is not installed${NC}"
        exit 1
    fi
    
    if ! command_exists docker-compose; then
        echo -e "${RED}❌ Docker Compose is not installed${NC}"
        exit 1
    fi
    
    if ! command_exists jq; then
        echo -e "${YELLOW}⚠️  jq is not installed (recommended for JSON processing)${NC}"
    fi
    
    echo -e "${GREEN}✅ Prerequisites check passed${NC}"
    echo ""
}

# Function to create production environment file
create_production_env() {
    echo "📝 Creating production environment configuration..."
    
    cat > .env.production << EOF
# SmartWatts Production Environment Variables
POSTGRES_DB=smartwatts
POSTGRES_USER=smartwatts_prod
POSTGRES_PASSWORD=$(openssl rand -base64 32)
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=$(openssl rand -base64 32)

JWT_SECRET=$(openssl rand -base64 64)
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

OPENWEATHER_API_KEY=your_production_api_key_here

EUREKA_SERVER_URL=http://service-discovery:8761/eureka/

BCRYPT_ROUNDS=12

LOG_LEVEL=$LOG_LEVEL
LOG_FORMAT=json

SPRING_PROFILES_ACTIVE=production
EOF
    
    echo -e "${GREEN}✅ Production environment file created${NC}"
    echo ""
}

# Function to create production Docker Compose override
create_production_override() {
    echo "🐳 Creating production Docker Compose override..."
    
    cat > docker-compose.prod.yml << EOF
version: '3.8'

services:
  postgres:
    environment:
      POSTGRES_PASSWORD: \${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backups:/backups
    restart: unless-stopped
    
  redis:
    environment:
      REDIS_PASSWORD: \${REDIS_PASSWORD}
    restart: unless-stopped
    
  api-gateway:
    environment:
      SPRING_PROFILES_ACTIVE: production
      LOG_LEVEL: \${LOG_LEVEL}
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
          
  user-service:
    environment:
      SPRING_PROFILES_ACTIVE: production
      LOG_LEVEL: \${LOG_LEVEL}
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

volumes:
  postgres_data:
    driver: local
  redis_data:
    driver: local
EOF
    
    echo -e "${GREEN}✅ Production Docker Compose override created${NC}"
    echo ""
}

# Function to create production monitoring configuration
create_monitoring_config() {
    echo "📊 Creating production monitoring configuration..."
    
    mkdir -p monitoring/prometheus
    mkdir -p monitoring/grafana
    
    cat > monitoring/prometheus/prometheus.yml << EOF
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "rules/*.yml"

scrape_configs:
  - job_name: 'smartwatts-services'
    static_configs:
      - targets: ['api-gateway:8080', 'user-service:8081', 'device-service:8083']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
EOF
    
    echo -e "${GREEN}✅ Monitoring configuration created${NC}"
    echo ""
}

# Function to create backup script
create_backup_script() {
    echo "💾 Creating database backup script..."
    
    cat > backup-database.sh << 'EOF'
#!/bin/bash

# Database backup script for SmartWatts
BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="smartwatts_backup_$DATE.sql"

echo "Creating database backup: $BACKUP_FILE"

docker exec smartwatts-postgres pg_dump -U postgres smartwatts > "$BACKUP_DIR/$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Backup created successfully: $BACKUP_FILE"
    # Keep only last 7 days of backups
    find "$BACKUP_DIR" -name "smartwatts_backup_*.sql" -mtime +7 -delete
else
    echo "❌ Backup failed"
    exit 1
fi
EOF
    
    chmod +x backup-database.sh
    echo -e "${GREEN}✅ Backup script created${NC}"
    echo ""
}

# Function to create health check script
create_health_check_script() {
    echo "🏥 Creating production health check script..."
    
    cat > health-check-prod.sh << 'EOF'
#!/bin/bash

# Production health check script
echo "🏥 SmartWatts Production Health Check"
echo "===================================="

# Check if all services are running
SERVICES=("smartwatts-postgres" "smartwatts-redis" "smartwatts-service-discovery" "smartwatts-api-gateway" "smartwatts-user-service")

for service in "${SERVICES[@]}"; do
    if docker ps --format "table {{.Names}}" | grep -q "$service"; then
        echo "✅ $service is running"
    else
        echo "❌ $service is not running"
    fi
done

# Check API Gateway health
echo ""
echo "🔍 API Gateway Health:"
curl -s http://localhost:8080/actuator/health | jq . || echo "API Gateway not responding"

# Check database connectivity
echo ""
echo "🔍 Database Health:"
docker exec smartwatts-postgres psql -U postgres -d smartwatts -c "SELECT COUNT(*) FROM users;" 2>/dev/null || echo "Database not accessible"

# Check Redis connectivity
echo ""
echo "🔍 Redis Health:"
docker exec smartwatts-redis redis-cli ping 2>/dev/null || echo "Redis not accessible"
EOF
    
    chmod +x health-check-prod.sh
    echo -e "${GREEN}✅ Production health check script created${NC}"
    echo ""
}

# Function to deploy services
deploy_services() {
    echo "🚀 Deploying SmartWatts services..."
    
    # Stop existing services
    echo "Stopping existing services..."
    docker-compose down
    
    # Build and start services
    echo "Building and starting services..."
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.production up -d --build
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Services deployed successfully${NC}"
    else
        echo -e "${RED}❌ Service deployment failed${NC}"
        exit 1
    fi
    echo ""
}

# Function to run health checks
run_health_checks() {
    echo "🏥 Running health checks..."
    
    # Wait for services to start
    echo "Waiting for services to start..."
    sleep 30
    
    # Run health check script
    ./health-check-prod.sh
    
    echo ""
}

# Function to create production README
create_production_readme() {
    echo "📚 Creating production documentation..."
    
    cat > PRODUCTION_README.md << 'EOF'
# SmartWatts Production Deployment

## Overview
This document provides instructions for deploying SmartWatts to production.

## Prerequisites
- Docker and Docker Compose installed
- At least 4GB RAM available
- 20GB disk space for data and logs
- OpenWeather API key (optional)

## Quick Start
1. Run the deployment script:
   ```bash
   ./deploy-production.sh production INFO
   ```

2. Check service health:
   ```bash
   ./health-check-prod.sh
   ```

## Environment Variables
All production environment variables are configured in `.env.production`.

## Monitoring
- API Gateway: http://localhost:8080/actuator/health
- Spring Boot Admin: http://localhost:8087
- Eureka Dashboard: http://localhost:8761

## Backup
Run database backup:
```bash
./backup-database.sh
```

## Logs
- Application logs: `logs/` directory
- Docker logs: `docker-compose logs [service-name]`

## Security
- Change all default passwords in production
- Use strong JWT secrets
- Enable SSL/TLS in production
- Configure firewall rules

## Troubleshooting
1. Check service status: `docker ps`
2. View logs: `docker-compose logs [service-name]`
3. Run health check: `./health-check-prod.sh`
4. Restart services: `docker-compose restart [service-name]`
EOF
    
    echo -e "${GREEN}✅ Production documentation created${NC}"
    echo ""
}

# Main execution
main() {
    check_prerequisites
    create_production_env
    create_production_override
    create_monitoring_config
    create_backup_script
    create_health_check_script
    deploy_services
    run_health_checks
    create_production_readme
    
    echo "🎉 SmartWatts Production Deployment Complete!"
    echo "=============================================="
    echo ""
    echo "Next steps:"
    echo "1. Update API keys in .env.production"
    echo "2. Configure SSL/TLS certificates"
    echo "3. Set up monitoring and alerting"
    echo "4. Configure backup schedules"
    echo "5. Review security settings"
    echo ""
    echo "Access points:"
    echo "- Frontend: http://localhost:3001"
    echo "- API Gateway: http://localhost:8080"
    echo "- Admin Dashboard: http://localhost:8087"
    echo "- Service Discovery: http://localhost:8761"
}

# Run main function
main "$@"

