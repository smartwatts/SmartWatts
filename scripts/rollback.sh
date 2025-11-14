#!/bin/bash

# SmartWatts Rollback Script
# This script rolls back to the previous deployment version

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKUP_DIR="/var/backups/smartwatts"
ROLLBACK_VERSION=${1:-"previous"}
CONFIRM_ROLLBACK=${2:-"false"}

echo -e "${BLUE}🔄 SmartWatts Rollback Script${NC}"
echo "=============================================="
echo "Rollback Version: $ROLLBACK_VERSION"
echo "Backup Directory: $BACKUP_DIR"
echo ""

# Function to check if running as root
check_root() {
    if [ "$EUID" -ne 0 ]; then
        echo -e "${RED}❌ This script must be run as root for full rollback${NC}"
        echo "Please run: sudo $0 $@"
        exit 1
    fi
}

# Function to confirm rollback
confirm_rollback() {
    if [ "$CONFIRM_ROLLBACK" != "true" ]; then
        echo -e "${YELLOW}⚠️  WARNING: This will rollback the SmartWatts deployment${NC}"
        echo "This action may cause data loss and service downtime."
        echo ""
        read -p "Are you sure you want to continue? (yes/no): " -r
        if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
            echo -e "${BLUE}ℹ️  Rollback cancelled${NC}"
            exit 0
        fi
    fi
}

# Function to stop all services
stop_services() {
    echo -e "${BLUE}🛑 Stopping all services...${NC}"
    
    # Stop Docker Compose services
    if [ -f "docker-compose.yml" ]; then
        docker-compose down --remove-orphans
        echo -e "${GREEN}✅ Docker Compose services stopped${NC}"
    fi
    
    # Stop individual containers if they exist
    containers=(
        "smartwatts-postgres" "smartwatts-redis" "smartwatts-service-discovery"
        "smartwatts-api-gateway" "smartwatts-user-service" "smartwatts-energy-service"
        "smartwatts-device-service" "smartwatts-analytics-service" "smartwatts-billing-service"
        "smartwatts-facility-service" "smartwatts-feature-flag-service"
        "smartwatts-device-verification-service" "smartwatts-appliance-monitoring-service"
    )
    
    for container in "${containers[@]}"; do
        if docker ps -q -f name="$container" | grep -q .; then
            docker stop "$container" || true
            docker rm "$container" || true
            echo -e "${GREEN}✅ Stopped container: $container${NC}"
        fi
    done
}

# Function to backup current state
backup_current_state() {
    echo -e "${BLUE}💾 Backing up current state...${NC}"
    
    # Create backup directory
    mkdir -p "$BACKUP_DIR/rollback-$(date +%Y%m%d_%H%M%S)"
    local backup_path="$BACKUP_DIR/rollback-$(date +%Y%m%d_%H%M%S)"
    
    # Backup database
    if docker ps -q -f name="smartwatts-postgres" | grep -q .; then
        echo -e "${BLUE}📊 Backing up database...${NC}"
        docker exec smartwatts-postgres pg_dump -U postgres smartwatts > "$backup_path/database_backup.sql"
        echo -e "${GREEN}✅ Database backed up${NC}"
    fi
    
    # Backup configuration files
    echo -e "${BLUE}📁 Backing up configuration...${NC}"
    cp -r . "$backup_path/config_backup/" 2>/dev/null || true
    echo -e "${GREEN}✅ Configuration backed up${NC}"
    
    # Backup Docker volumes
    echo -e "${BLUE}🐳 Backing up Docker volumes...${NC}"
    docker run --rm -v smartwatts_postgres_data:/data -v "$backup_path":/backup alpine tar czf /backup/postgres_data.tar.gz -C /data .
    docker run --rm -v smartwatts_redis_data:/data -v "$backup_path":/backup alpine tar czf /backup/redis_data.tar.gz -C /data .
    echo -e "${GREEN}✅ Docker volumes backed up${NC}"
    
    echo -e "${GREEN}✅ Current state backed up to: $backup_path${NC}"
}

# Function to rollback database
rollback_database() {
    echo -e "${BLUE}🗄️  Rolling back database...${NC}"
    
    # Find the most recent backup
    local latest_backup=$(ls -t "$BACKUP_DIR" | head -n 1)
    if [ -z "$latest_backup" ]; then
        echo -e "${RED}❌ No backup found for rollback${NC}"
        return 1
    fi
    
    local backup_path="$BACKUP_DIR/$latest_backup"
    
    if [ -f "$backup_path/database_backup.sql" ]; then
        # Start PostgreSQL if not running
        if ! docker ps -q -f name="smartwatts-postgres" | grep -q .; then
            docker-compose up -d postgres
            sleep 10
        fi
        
        # Restore database
        docker exec -i smartwatts-postgres psql -U postgres -d smartwatts < "$backup_path/database_backup.sql"
        echo -e "${GREEN}✅ Database rolled back${NC}"
    else
        echo -e "${YELLOW}⚠️  No database backup found${NC}"
    fi
}

# Function to rollback Docker volumes
rollback_volumes() {
    echo -e "${BLUE}🐳 Rolling back Docker volumes...${NC}"
    
    # Find the most recent backup
    local latest_backup=$(ls -t "$BACKUP_DIR" | head -n 1)
    if [ -z "$latest_backup" ]; then
        echo -e "${RED}❌ No backup found for rollback${NC}"
        return 1
    fi
    
    local backup_path="$BACKUP_DIR/$latest_backup"
    
    # Stop services first
    docker-compose down
    
    # Restore PostgreSQL volume
    if [ -f "$backup_path/postgres_data.tar.gz" ]; then
        docker run --rm -v smartwatts_postgres_data:/data -v "$backup_path":/backup alpine tar xzf /backup/postgres_data.tar.gz -C /data
        echo -e "${GREEN}✅ PostgreSQL volume restored${NC}"
    fi
    
    # Restore Redis volume
    if [ -f "$backup_path/redis_data.tar.gz" ]; then
        docker run --rm -v smartwatts_redis_data:/data -v "$backup_path":/backup alpine tar xzf /backup/redis_data.tar.gz -C /data
        echo -e "${GREEN}✅ Redis volume restored${NC}"
    fi
}

# Function to rollback configuration
rollback_configuration() {
    echo -e "${BLUE}⚙️  Rolling back configuration...${NC}"
    
    # Find the most recent backup
    local latest_backup=$(ls -t "$BACKUP_DIR" | head -n 1)
    if [ -z "$latest_backup" ]; then
        echo -e "${RED}❌ No backup found for rollback${NC}"
        return 1
    fi
    
    local backup_path="$BACKUP_DIR/$latest_backup"
    
    if [ -d "$backup_path/config_backup" ]; then
        # Restore configuration files
        cp -r "$backup_path/config_backup/"* . 2>/dev/null || true
        echo -e "${GREEN}✅ Configuration restored${NC}"
    else
        echo -e "${YELLOW}⚠️  No configuration backup found${NC}"
    fi
}

# Function to rollback to Git commit
rollback_git() {
    echo -e "${BLUE}📝 Rolling back to Git commit...${NC}"
    
    if [ "$ROLLBACK_VERSION" = "previous" ]; then
        # Rollback to previous commit
        git reset --hard HEAD~1
        echo -e "${GREEN}✅ Rolled back to previous Git commit${NC}"
    else
        # Rollback to specific commit
        git reset --hard "$ROLLBACK_VERSION"
        echo -e "${GREEN}✅ Rolled back to Git commit: $ROLLBACK_VERSION${NC}"
    fi
}

# Function to restart services
restart_services() {
    echo -e "${BLUE}🚀 Restarting services...${NC}"
    
    # Start services
    docker-compose up -d
    
    # Wait for services to start
    echo -e "${BLUE}⏳ Waiting for services to start...${NC}"
    sleep 30
    
    # Run health check
    echo -e "${BLUE}🏥 Running health check...${NC}"
    if ./scripts/health-check-all.sh; then
        echo -e "${GREEN}✅ All services are healthy${NC}"
    else
        echo -e "${RED}❌ Some services are not healthy${NC}"
        echo "Please check the logs: docker-compose logs"
        return 1
    fi
}

# Function to show rollback options
show_rollback_options() {
    echo -e "${BLUE}📋 Available rollback options:${NC}"
    echo "1. Full rollback (database + volumes + configuration)"
    echo "2. Database only rollback"
    echo "3. Configuration only rollback"
    echo "4. Git commit rollback"
    echo "5. Docker volumes rollback"
    echo ""
    read -p "Select rollback option (1-5): " -r option
    
    case $option in
        1)
            echo -e "${BLUE}🔄 Performing full rollback...${NC}"
            rollback_database
            rollback_volumes
            rollback_configuration
            restart_services
            ;;
        2)
            echo -e "${BLUE}🔄 Performing database rollback...${NC}"
            rollback_database
            restart_services
            ;;
        3)
            echo -e "${BLUE}🔄 Performing configuration rollback...${NC}"
            rollback_configuration
            restart_services
            ;;
        4)
            echo -e "${BLUE}🔄 Performing Git rollback...${NC}"
            rollback_git
            restart_services
            ;;
        5)
            echo -e "${BLUE}🔄 Performing Docker volumes rollback...${NC}"
            rollback_volumes
            restart_services
            ;;
        *)
            echo -e "${RED}❌ Invalid option${NC}"
            exit 1
            ;;
    esac
}

# Main execution
main() {
    echo -e "${BLUE}🔍 Checking prerequisites...${NC}"
    
    # Check if Docker is running
    if ! docker info >/dev/null 2>&1; then
        echo -e "${RED}❌ Docker is not running${NC}"
        exit 1
    fi
    
    # Check if backup directory exists
    if [ ! -d "$BACKUP_DIR" ]; then
        echo -e "${YELLOW}⚠️  Backup directory not found: $BACKUP_DIR${NC}"
        echo "Creating backup directory..."
        mkdir -p "$BACKUP_DIR"
    fi
    
    # Confirm rollback
    confirm_rollback
    
    # Backup current state
    backup_current_state
    
    # Stop services
    stop_services
    
    # Show rollback options
    show_rollback_options
    
    echo ""
    echo "=============================================="
    echo -e "${GREEN}✅ Rollback completed successfully${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Verify all services are running: ./scripts/health-check-all.sh"
    echo "2. Check application logs: docker-compose logs -f"
    echo "3. Test critical functionality"
    echo "4. Monitor system performance"
    echo ""
    echo "If issues persist, you can rollback again or restore from a different backup."
}

# Run main function
main "$@"









