#!/bin/bash

# SmartWatts Infrastructure Deployment Script
# This script deploys the complete SmartWatts infrastructure with monitoring, SSL, and backup

set -e

# Configuration
PROJECT_DIR="/opt/smartwatts"
DOCKER_COMPOSE_FILE="docker-compose.infrastructure.yml"
ENV_FILE=".env"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

# Function to check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker first."
    fi
    
    # Check if Docker Compose is installed
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose first."
    fi
    
    # Check if OpenSSL is installed
    if ! command -v openssl &> /dev/null; then
        error "OpenSSL is not installed. Please install OpenSSL first."
    fi
    
    # Check if Java is installed (for certificate generation)
    if ! command -v java &> /dev/null; then
        warn "Java is not installed. Some certificate features may not work."
    fi
    
    log "Prerequisites check completed"
}

# Function to create project directory
create_project_directory() {
    log "Creating project directory..."
    
    sudo mkdir -p "$PROJECT_DIR"
    sudo chown -R $(whoami):$(whoami) "$PROJECT_DIR"
    
    # Copy project files
    cp -r . "$PROJECT_DIR/"
    cd "$PROJECT_DIR"
    
    log "Project directory created: $PROJECT_DIR"
}

# Function to generate SSL certificates
generate_ssl_certificates() {
    log "Generating SSL certificates..."
    
    if [ -f "ssl/generate-certificates.sh" ]; then
        chmod +x ssl/generate-certificates.sh
        ./ssl/generate-certificates.sh --generate-all
        log "SSL certificates generated successfully"
    else
        error "SSL certificate generation script not found"
    fi
}

# Function to create environment file
create_environment_file() {
    log "Creating environment file..."
    
    if [ ! -f "$ENV_FILE" ]; then
        cat > "$ENV_FILE" << EOF
# SmartWatts Infrastructure Environment Configuration

# Database Configuration
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=smartwatts
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=smartwatts123

# JWT Configuration
JWT_SECRET=smartwatts-jwt-secret-key-2024-nigeria-energy-monitoring
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# SSL Configuration
SSL_ENABLED=true
SSL_KEYSTORE_PASSWORD=smartwatts123
SSL_TRUSTSTORE_PASSWORD=smartwatts123

# Monitoring Configuration
PROMETHEUS_ENABLED=true
GRAFANA_ADMIN_PASSWORD=smartwatts123

# Backup Configuration
BACKUP_RETENTION_DAYS=30
BACKUP_DIR=/var/backups/smartwatts

# Optional: AWS S3 Configuration for cloud backup
# AWS_ACCESS_KEY_ID=your_access_key
# AWS_SECRET_ACCESS_KEY=your_secret_key
# AWS_S3_BUCKET=your-backup-bucket
# AWS_DEFAULT_REGION=us-east-1
EOF
        log "Environment file created: $ENV_FILE"
    else
        log "Environment file already exists: $ENV_FILE"
    fi
}

# Function to setup backup system
setup_backup_system() {
    log "Setting up backup system..."
    
    if [ -f "scripts/setup-backup-cron.sh" ]; then
        chmod +x scripts/setup-backup-cron.sh
        ./scripts/setup-backup-cron.sh
        log "Backup system setup completed"
    else
        warn "Backup setup script not found"
    fi
}

# Function to build Docker images
build_docker_images() {
    log "Building Docker images..."
    
    # Build API Gateway
    if [ -d "backend/api-gateway" ]; then
        log "Building API Gateway..."
        docker build -t smartwatts/api-gateway:latest backend/api-gateway/
    fi
    
    # Build User Service
    if [ -d "backend/user-service" ]; then
        log "Building User Service..."
        docker build -t smartwatts/user-service:latest backend/user-service/
    fi
    
    # Build Energy Service
    if [ -d "backend/energy-service" ]; then
        log "Building Energy Service..."
        docker build -t smartwatts/energy-service:latest backend/energy-service/
    fi
    
    # Build Device Service
    if [ -d "backend/device-service" ]; then
        log "Building Device Service..."
        docker build -t smartwatts/device-service:latest backend/device-service/
    fi
    
    # Build Analytics Service
    if [ -d "backend/analytics-service" ]; then
        log "Building Analytics Service..."
        docker build -t smartwatts/analytics-service:latest backend/analytics-service/
    fi
    
    # Build Billing Service
    if [ -d "backend/billing-service" ]; then
        log "Building Billing Service..."
        docker build -t smartwatts/billing-service:latest backend/billing-service/
    fi
    
    # Build Notification Service
    if [ -d "backend/notification-service" ]; then
        log "Building Notification Service..."
        docker build -t smartwatts/notification-service:latest backend/notification-service/
    fi
    
    # Build Edge Gateway Service
    if [ -d "backend/edge-gateway-service" ]; then
        log "Building Edge Gateway Service..."
        docker build -t smartwatts/edge-gateway-service:latest backend/edge-gateway-service/
    fi
    
    log "Docker images built successfully"
}

# Function to start infrastructure
start_infrastructure() {
    log "Starting SmartWatts infrastructure..."
    
    # Start infrastructure services
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d
    
    log "Infrastructure started successfully"
}

# Function to wait for services
wait_for_services() {
    log "Waiting for services to be ready..."
    
    # Wait for PostgreSQL
    log "Waiting for PostgreSQL..."
    until docker exec smartwatts-postgres pg_isready -U postgres; do
        sleep 2
    done
    
    # Wait for Redis
    log "Waiting for Redis..."
    until docker exec smartwatts-redis redis-cli ping; do
        sleep 2
    done
    
    # Wait for Eureka
    log "Waiting for Eureka..."
    until curl -f http://localhost:8761/actuator/health; do
        sleep 5
    done
    
    # Wait for API Gateway
    log "Waiting for API Gateway..."
    until curl -f http://localhost:8080/actuator/health; do
        sleep 5
    done
    
    log "All services are ready"
}

# Function to show service status
show_service_status() {
    log "SmartWatts Infrastructure Status:"
    echo ""
    
    # Show Docker containers
    echo "Docker Containers:"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo ""
    
    # Show service health
    echo "Service Health:"
    echo "  - API Gateway: http://localhost:8080/actuator/health"
    echo "  - User Service: http://localhost:8081/actuator/health"
    echo "  - Energy Service: http://localhost:8082/actuator/health"
    echo "  - Device Service: http://localhost:8083/actuator/health"
    echo "  - Analytics Service: http://localhost:8084/actuator/health"
    echo "  - Billing Service: http://localhost:8085/actuator/health"
    echo "  - Notification Service: http://localhost:8086/actuator/health"
    echo "  - Edge Gateway Service: http://localhost:8088/actuator/health"
    echo ""
    
    # Show monitoring URLs
    echo "Monitoring URLs:"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Grafana: http://localhost:3001 (admin/smartwatts123)"
    echo "  - Eureka: http://localhost:8761"
    echo ""
    
    # Show API documentation URLs
    echo "API Documentation:"
    echo "  - API Gateway: http://localhost:8080/swagger-ui.html"
    echo "  - User Service: http://localhost:8081/swagger-ui.html"
    echo "  - Energy Service: http://localhost:8082/swagger-ui.html"
    echo "  - Device Service: http://localhost:8083/swagger-ui.html"
    echo "  - Analytics Service: http://localhost:8084/swagger-ui.html"
    echo "  - Billing Service: http://localhost:8085/swagger-ui.html"
    echo "  - Notification Service: http://localhost:8086/swagger-ui.html"
    echo "  - Edge Gateway Service: http://localhost:8088/swagger-ui.html"
    echo ""
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --deploy          Deploy complete infrastructure"
    echo "  --start           Start infrastructure services"
    echo "  --stop            Stop infrastructure services"
    echo "  --restart         Restart infrastructure services"
    echo "  --status          Show service status"
    echo "  --logs            Show service logs"
    echo "  --clean           Clean up infrastructure"
    echo "  --help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --deploy"
    echo "  $0 --status"
    echo "  $0 --logs"
}

# Function to show logs
show_logs() {
    log "Showing service logs..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f
}

# Function to stop infrastructure
stop_infrastructure() {
    log "Stopping SmartWatts infrastructure..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" down
    log "Infrastructure stopped"
}

# Function to clean infrastructure
clean_infrastructure() {
    log "Cleaning up SmartWatts infrastructure..."
    
    # Stop and remove containers
    docker-compose -f "$DOCKER_COMPOSE_FILE" down -v
    
    # Remove images
    docker rmi smartwatts/api-gateway:latest 2>/dev/null || true
    docker rmi smartwatts/user-service:latest 2>/dev/null || true
    docker rmi smartwatts/energy-service:latest 2>/dev/null || true
    docker rmi smartwatts/device-service:latest 2>/dev/null || true
    docker rmi smartwatts/analytics-service:latest 2>/dev/null || true
    docker rmi smartwatts/billing-service:latest 2>/dev/null || true
    docker rmi smartwatts/notification-service:latest 2>/dev/null || true
    docker rmi smartwatts/edge-gateway-service:latest 2>/dev/null || true
    
    # Remove volumes
    docker volume prune -f
    
    log "Infrastructure cleaned up"
}

# Main function
main() {
    case "${1:-}" in
        --deploy)
            check_prerequisites
            create_project_directory
            generate_ssl_certificates
            create_environment_file
            setup_backup_system
            build_docker_images
            start_infrastructure
            wait_for_services
            show_service_status
            ;;
        --start)
            start_infrastructure
            wait_for_services
            show_service_status
            ;;
        --stop)
            stop_infrastructure
            ;;
        --restart)
            stop_infrastructure
            start_infrastructure
            wait_for_services
            show_service_status
            ;;
        --status)
            show_service_status
            ;;
        --logs)
            show_logs
            ;;
        --clean)
            clean_infrastructure
            ;;
        --help|-h)
            show_usage
            ;;
        *)
            show_usage
            ;;
    esac
}

# Run main function
main "$@"
