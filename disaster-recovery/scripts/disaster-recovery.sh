#!/bin/bash

# SmartWatts Disaster Recovery Script
# This script automates disaster recovery procedures for the SmartWatts platform

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$(dirname "$SCRIPT_DIR")")"
BACKUP_DIR="/var/backups/smartwatts"
RECOVERY_DIR="/opt/smartwatts/recovery"
LOG_FILE="/var/log/smartwatts/disaster-recovery.log"
CONFIG_FILE="$SCRIPT_DIR/disaster-recovery.conf"

# Recovery scenarios
SCENARIOS=("datacenter-failure" "database-corruption" "security-breach" "service-outage")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}" | tee -a "$LOG_FILE"
}

warn() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}" | tee -a "$LOG_FILE"
}

error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}" | tee -a "$LOG_FILE"
    exit 1
}

# Function to load configuration
load_config() {
    if [ -f "$CONFIG_FILE" ]; then
        source "$CONFIG_FILE"
        log "Configuration loaded from $CONFIG_FILE"
    else
        warn "Configuration file not found: $CONFIG_FILE"
        log "Using default configuration"
    fi
}

# Function to check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if required tools are installed
    local tools=("kubectl" "helm" "docker" "psql" "redis-cli" "aws" "terraform")
    
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            error "$tool is not installed. Please install it first."
        fi
    done
    
    # Check if backup directory exists
    if [ ! -d "$BACKUP_DIR" ]; then
        error "Backup directory not found: $BACKUP_DIR"
    fi
    
    # Check if recovery directory exists
    if [ ! -d "$RECOVERY_DIR" ]; then
        log "Creating recovery directory: $RECOVERY_DIR"
        mkdir -p "$RECOVERY_DIR"
    fi
    
    log "Prerequisites check completed"
}

# Function to create recovery directory structure
create_recovery_structure() {
    log "Creating recovery directory structure..."
    
    local dirs=(
        "$RECOVERY_DIR/infrastructure"
        "$RECOVERY_DIR/database"
        "$RECOVERY_DIR/applications"
        "$RECOVERY_DIR/configurations"
        "$RECOVERY_DIR/secrets"
        "$RECOVERY_DIR/logs"
        "$RECOVERY_DIR/scripts"
    )
    
    for dir in "${dirs[@]}"; do
        mkdir -p "$dir"
    done
    
    log "Recovery directory structure created"
}

# Function to assess disaster impact
assess_disaster_impact() {
    local scenario="$1"
    
    log "Assessing disaster impact for scenario: $scenario"
    
    case "$scenario" in
        "datacenter-failure")
            log "Scenario: Complete data center failure"
            log "Impact: All services unavailable"
            log "Priority: CRITICAL"
            ;;
        "database-corruption")
            log "Scenario: Database corruption"
            log "Impact: Data integrity compromised"
            log "Priority: CRITICAL"
            ;;
        "security-breach")
            log "Scenario: Security breach"
            log "Impact: Systems compromised"
            log "Priority: CRITICAL"
            ;;
        "service-outage")
            log "Scenario: Service outage"
            log "Impact: Limited service availability"
            log "Priority: HIGH"
            ;;
        *)
            error "Unknown scenario: $scenario"
            ;;
    esac
}

# Function to activate disaster recovery team
activate_recovery_team() {
    log "Activating disaster recovery team..."
    
    # Send notifications
    if command -v mail &> /dev/null; then
        echo "DISASTER RECOVERY ACTIVATED - $(date)" | mail -s "SmartWatts Disaster Recovery" admin@smartwatts.com
    fi
    
    # Send Slack notification if configured
    if [ -n "$SLACK_WEBHOOK" ]; then
        curl -X POST -H 'Content-type: application/json' \
            --data '{"text":"🚨 DISASTER RECOVERY ACTIVATED - SmartWatts Platform"}' \
            "$SLACK_WEBHOOK"
    fi
    
    log "Disaster recovery team activated"
}

# Function to restore infrastructure
restore_infrastructure() {
    log "Restoring infrastructure..."
    
    # Check if Kubernetes cluster is accessible
    if ! kubectl cluster-info &> /dev/null; then
        log "Kubernetes cluster not accessible, provisioning new cluster..."
        
        # Provision new cluster using Terraform
        cd "$RECOVERY_DIR/infrastructure"
        terraform init
        terraform plan -var="environment=recovery"
        terraform apply -auto-approve -var="environment=recovery"
        
        # Configure kubectl
        terraform output -raw kubeconfig > ~/.kube/config
    fi
    
    # Create namespace
    kubectl create namespace smartwatts --dry-run=client -o yaml | kubectl apply -f -
    
    log "Infrastructure restoration completed"
}

# Function to restore database
restore_database() {
    local scenario="$1"
    
    log "Restoring database for scenario: $scenario"
    
    case "$scenario" in
        "database-corruption")
            log "Restoring from latest backup due to corruption..."
            restore_database_from_backup
            ;;
        "datacenter-failure")
            log "Restoring from cloud backup due to datacenter failure..."
            restore_database_from_cloud
            ;;
        "security-breach")
            log "Restoring from clean backup due to security breach..."
            restore_database_from_clean_backup
            ;;
        *)
            log "Restoring from latest backup..."
            restore_database_from_backup
            ;;
    esac
}

# Function to restore database from backup
restore_database_from_backup() {
    log "Restoring database from local backup..."
    
    # Find latest backup
    local latest_backup=$(find "$BACKUP_DIR" -name "*.sql" -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -d' ' -f2-)
    
    if [ -z "$latest_backup" ]; then
        error "No database backup found in $BACKUP_DIR"
    fi
    
    log "Using backup: $latest_backup"
    
    # Restore database
    if [ -f "$latest_backup" ]; then
        # Create database if it doesn't exist
        psql -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -d postgres -c "CREATE DATABASE $POSTGRES_DB;" || true
        
        # Restore database
        pg_restore -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists "$latest_backup"
        
        log "Database restored successfully"
    else
        error "Backup file not found: $latest_backup"
    fi
}

# Function to restore database from cloud backup
restore_database_from_cloud() {
    log "Restoring database from cloud backup..."
    
    # Download latest backup from S3
    local s3_backup="s3://$AWS_S3_BUCKET/smartwatts/backups/"
    local latest_cloud_backup=$(aws s3 ls "$s3_backup" --recursive | sort | tail -1 | awk '{print $4}')
    
    if [ -z "$latest_cloud_backup" ]; then
        error "No cloud backup found in S3"
    fi
    
    log "Downloading cloud backup: $latest_cloud_backup"
    aws s3 cp "s3://$AWS_S3_BUCKET/$latest_cloud_backup" "$RECOVERY_DIR/database/cloud_backup.sql"
    
    # Restore from cloud backup
    restore_database_from_backup "$RECOVERY_DIR/database/cloud_backup.sql"
}

# Function to restore database from clean backup
restore_database_from_clean_backup() {
    log "Restoring database from clean backup..."
    
    # Find clean backup (before security incident)
    local clean_backup=$(find "$BACKUP_DIR" -name "*clean*.sql" -type f | head -1)
    
    if [ -z "$clean_backup" ]; then
        log "No clean backup found, using latest backup"
        restore_database_from_backup
    else
        log "Using clean backup: $clean_backup"
        restore_database_from_backup "$clean_backup"
    fi
}

# Function to restore applications
restore_applications() {
    log "Restoring applications..."
    
    # Deploy using Helm
    cd "$PROJECT_DIR"
    
    # Add Helm repositories
    helm repo add bitnami https://charts.bitnami.com/bitnami
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo add grafana https://grafana.github.io/helm-charts
    helm repo update
    
    # Deploy infrastructure services
    log "Deploying PostgreSQL..."
    helm upgrade --install postgresql bitnami/postgresql \
        --namespace smartwatts \
        --set auth.postgresPassword="$POSTGRES_PASSWORD" \
        --set auth.database="$POSTGRES_DB"
    
    log "Deploying Redis..."
    helm upgrade --install redis bitnami/redis \
        --namespace smartwatts \
        --set auth.password="$REDIS_PASSWORD"
    
    # Deploy monitoring
    log "Deploying Prometheus..."
    helm upgrade --install prometheus prometheus-community/prometheus \
        --namespace smartwatts \
        --set server.persistentVolume.enabled=true \
        --set server.persistentVolume.size=10Gi
    
    log "Deploying Grafana..."
    helm upgrade --install grafana grafana/grafana \
        --namespace smartwatts \
        --set adminPassword="$GRAFANA_ADMIN_PASSWORD" \
        --set persistence.enabled=true \
        --set persistence.size=5Gi
    
    # Deploy SmartWatts services
    log "Deploying SmartWatts services..."
    helm upgrade --install smartwatts ./helm/smartwatts \
        --namespace smartwatts \
        --set image.tag=latest \
        --values ./helm/smartwatts/values-production.yaml
    
    log "Applications restored successfully"
}

# Function to restore configurations
restore_configurations() {
    log "Restoring configurations..."
    
    # Restore Kubernetes configurations
    if [ -d "$BACKUP_DIR/k8s" ]; then
        cp -r "$BACKUP_DIR/k8s"/* "$RECOVERY_DIR/configurations/"
        log "Kubernetes configurations restored"
    fi
    
    # Restore application configurations
    if [ -d "$BACKUP_DIR/configs" ]; then
        cp -r "$BACKUP_DIR/configs"/* "$RECOVERY_DIR/configurations/"
        log "Application configurations restored"
    fi
    
    # Restore SSL certificates
    if [ -d "$BACKUP_DIR/ssl" ]; then
        cp -r "$BACKUP_DIR/ssl"/* "$RECOVERY_DIR/secrets/"
        log "SSL certificates restored"
    fi
    
    log "Configurations restored successfully"
}

# Function to validate recovery
validate_recovery() {
    log "Validating recovery..."
    
    # Check Kubernetes cluster
    if kubectl cluster-info &> /dev/null; then
        log "✓ Kubernetes cluster is accessible"
    else
        error "✗ Kubernetes cluster is not accessible"
    fi
    
    # Check database connectivity
    if psql -h "$POSTGRES_HOST" -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT 1;" &> /dev/null; then
        log "✓ Database is accessible"
    else
        error "✗ Database is not accessible"
    fi
    
    # Check Redis connectivity
    if redis-cli -h "$REDIS_HOST" -a "$REDIS_PASSWORD" ping &> /dev/null; then
        log "✓ Redis is accessible"
    else
        error "✗ Redis is not accessible"
    fi
    
    # Check application services
    local services=("api-gateway" "user-service" "energy-service" "device-service")
    
    for service in "${services[@]}"; do
        if kubectl get deployment "$service" -n smartwatts &> /dev/null; then
            log "✓ $service is deployed"
        else
            warn "✗ $service is not deployed"
        fi
    done
    
    # Check service health
    local api_gateway_url="http://$(kubectl get service api-gateway-service -n smartwatts -o jsonpath='{.status.loadBalancer.ingress[0].ip}'):8080"
    
    if curl -s "$api_gateway_url/actuator/health" | grep -q "UP"; then
        log "✓ API Gateway is healthy"
    else
        warn "✗ API Gateway is not healthy"
    fi
    
    log "Recovery validation completed"
}

# Function to notify recovery completion
notify_recovery_completion() {
    local scenario="$1"
    local status="$2"
    
    log "Notifying recovery completion..."
    
    # Send email notification
    if command -v mail &> /dev/null; then
        echo "Disaster recovery completed for scenario: $scenario" | mail -s "SmartWatts Recovery Complete" admin@smartwatts.com
    fi
    
    # Send Slack notification
    if [ -n "$SLACK_WEBHOOK" ]; then
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"text\":\"✅ DISASTER RECOVERY COMPLETED - SmartWatts Platform - Status: $status\"}" \
            "$SLACK_WEBHOOK"
    fi
    
    log "Recovery completion notification sent"
}

# Function to run full disaster recovery
run_disaster_recovery() {
    local scenario="$1"
    
    log "Starting disaster recovery for scenario: $scenario"
    
    # Load configuration
    load_config
    
    # Check prerequisites
    check_prerequisites
    
    # Create recovery structure
    create_recovery_structure
    
    # Assess disaster impact
    assess_disaster_impact "$scenario"
    
    # Activate recovery team
    activate_recovery_team
    
    # Restore infrastructure
    restore_infrastructure
    
    # Restore database
    restore_database "$scenario"
    
    # Restore applications
    restore_applications
    
    # Restore configurations
    restore_configurations
    
    # Validate recovery
    validate_recovery
    
    # Notify completion
    notify_recovery_completion "$scenario" "SUCCESS"
    
    log "Disaster recovery completed successfully for scenario: $scenario"
}

# Function to test disaster recovery
test_disaster_recovery() {
    local scenario="$1"
    
    log "Testing disaster recovery for scenario: $scenario"
    
    # Run recovery in test mode
    export TEST_MODE=true
    run_disaster_recovery "$scenario"
    
    log "Disaster recovery test completed for scenario: $scenario"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS] SCENARIO"
    echo ""
    echo "Options:"
    echo "  --test              Run in test mode"
    echo "  --config FILE       Use custom configuration file"
    echo "  --help              Show this help message"
    echo ""
    echo "Scenarios:"
    for scenario in "${SCENARIOS[@]}"; do
        echo "  $scenario"
    done
    echo ""
    echo "Examples:"
    echo "  $0 datacenter-failure"
    echo "  $0 --test database-corruption"
    echo "  $0 --config /path/to/config.conf security-breach"
}

# Main function
main() {
    local test_mode=false
    local config_file=""
    local scenario=""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --test)
                test_mode=true
                shift
                ;;
            --config)
                config_file="$2"
                shift 2
                ;;
            --help|-h)
                show_usage
                exit 0
                ;;
            *)
                if [ -z "$scenario" ]; then
                    scenario="$1"
                else
                    error "Unknown argument: $1"
                fi
                shift
                ;;
        esac
    done
    
    # Validate scenario
    if [ -z "$scenario" ]; then
        error "Scenario is required"
    fi
    
    if [[ ! " ${SCENARIOS[@]} " =~ " ${scenario} " ]]; then
        error "Invalid scenario: $scenario"
    fi
    
    # Set configuration file
    if [ -n "$config_file" ]; then
        CONFIG_FILE="$config_file"
    fi
    
    # Run disaster recovery
    if [ "$test_mode" = true ]; then
        test_disaster_recovery "$scenario"
    else
        run_disaster_recovery "$scenario"
    fi
}

# Run main function
main "$@"
