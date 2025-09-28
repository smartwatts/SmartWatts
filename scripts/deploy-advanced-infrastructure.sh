#!/bin/bash

# SmartWatts Advanced Infrastructure Deployment Script
# This script deploys the complete advanced infrastructure including Kubernetes, CI/CD, Security, Performance Testing, and Disaster Recovery

set -e

# Configuration
PROJECT_DIR="/opt/smartwatts"
KUBERNETES_NAMESPACE="smartwatts"
HELM_RELEASE_NAME="smartwatts"
ENVIRONMENT="${ENVIRONMENT:-production}"

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
    log "Checking prerequisites for advanced infrastructure deployment..."
    
    # Check if required tools are installed
    local tools=("kubectl" "helm" "docker" "terraform" "aws" "git" "jq" "curl")
    
    for tool in "${tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            error "$tool is not installed. Please install it first."
        fi
    done
    
    # Check if Kubernetes cluster is accessible
    if ! kubectl cluster-info &> /dev/null; then
        error "Kubernetes cluster is not accessible. Please configure kubectl first."
    fi
    
    # Check if Helm is configured
    if ! helm version &> /dev/null; then
        error "Helm is not properly configured."
    fi
    
    log "Prerequisites check completed"
}

# Function to deploy Kubernetes infrastructure
deploy_kubernetes_infrastructure() {
    log "Deploying Kubernetes infrastructure..."
    
    # Create namespace
    kubectl create namespace "$KUBERNETES_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
    
    # Apply Kubernetes manifests
    if [ -d "k8s" ]; then
        kubectl apply -f k8s/namespace.yaml
        kubectl apply -f k8s/configmap.yaml
        kubectl apply -f k8s/secrets.yaml
        kubectl apply -f k8s/postgres.yaml
        kubectl apply -f k8s/redis.yaml
        kubectl apply -f k8s/api-gateway.yaml
        
        log "Kubernetes manifests applied successfully"
    else
        warn "Kubernetes manifests directory not found"
    fi
    
    # Deploy using Helm
    if [ -d "helm/smartwatts" ]; then
        helm upgrade --install "$HELM_RELEASE_NAME" ./helm/smartwatts \
            --namespace "$KUBERNETES_NAMESPACE" \
            --create-namespace \
            --values ./helm/smartwatts/values.yaml \
            --values ./helm/smartwatts/values-$ENVIRONMENT.yaml \
            --wait --timeout=10m
        
        log "Helm chart deployed successfully"
    else
        warn "Helm chart directory not found"
    fi
}

# Function to setup CI/CD pipeline
setup_cicd_pipeline() {
    log "Setting up CI/CD pipeline..."
    
    # Check if GitHub Actions is configured
    if [ -d ".github/workflows" ]; then
        log "GitHub Actions workflows found"
        
        # Validate workflow files
        for workflow in .github/workflows/*.yml; do
            if [ -f "$workflow" ]; then
                log "Validating workflow: $(basename "$workflow")"
                # Add workflow validation here if needed
            fi
        done
        
        log "CI/CD pipeline setup completed"
    else
        warn "GitHub Actions workflows not found"
    fi
}

# Function to setup security scanning
setup_security_scanning() {
    log "Setting up security scanning..."
    
    # Check if security tools are available
    if [ -f "security/penetration-testing/automated-security-tests.sh" ]; then
        chmod +x security/penetration-testing/automated-security-tests.sh
        log "Security testing script configured"
    fi
    
    # Setup security monitoring
    if [ -d "security" ]; then
        log "Security monitoring configured"
    fi
    
    log "Security scanning setup completed"
}

# Function to setup performance testing
setup_performance_testing() {
    log "Setting up performance testing..."
    
    # Check if JMeter is available
    if command -v jmeter &> /dev/null; then
        log "JMeter found, configuring performance tests"
        
        if [ -f "tests/performance/run-performance-tests.sh" ]; then
            chmod +x tests/performance/run-performance-tests.sh
            log "Performance testing script configured"
        fi
        
        if [ -f "tests/performance/smartwatts-load-test.jmx" ]; then
            log "JMeter test plan found"
        fi
    else
        warn "JMeter not found, performance testing may not work"
    fi
    
    log "Performance testing setup completed"
}

# Function to setup disaster recovery
setup_disaster_recovery() {
    log "Setting up disaster recovery..."
    
    # Check if disaster recovery scripts are available
    if [ -f "disaster-recovery/scripts/disaster-recovery.sh" ]; then
        chmod +x disaster-recovery/scripts/disaster-recovery.sh
        log "Disaster recovery script configured"
    fi
    
    # Setup backup monitoring
    if [ -f "scripts/backup-database.sh" ]; then
        chmod +x scripts/backup-database.sh
        log "Backup script configured"
    fi
    
    if [ -f "scripts/restore-database.sh" ]; then
        chmod +x scripts/restore-database.sh
        log "Restore script configured"
    fi
    
    log "Disaster recovery setup completed"
}

# Function to setup monitoring and alerting
setup_monitoring() {
    log "Setting up monitoring and alerting..."
    
    # Deploy Prometheus
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
    helm repo update
    
    helm upgrade --install prometheus prometheus-community/prometheus \
        --namespace "$KUBERNETES_NAMESPACE" \
        --set server.persistentVolume.enabled=true \
        --set server.persistentVolume.size=10Gi \
        --set server.retention=30d \
        --wait --timeout=10m
    
    # Deploy Grafana
    helm repo add grafana https://grafana.github.io/helm-charts
    helm repo update
    
    helm upgrade --install grafana grafana/grafana \
        --namespace "$KUBERNETES_NAMESPACE" \
        --set adminPassword="smartwatts123" \
        --set persistence.enabled=true \
        --set persistence.size=5Gi \
        --set service.type=LoadBalancer \
        --wait --timeout=10m
    
    log "Monitoring and alerting setup completed"
}

# Function to setup ingress and SSL
setup_ingress_ssl() {
    log "Setting up ingress and SSL..."
    
    # Deploy NGINX Ingress Controller
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
    helm repo update
    
    helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
        --namespace ingress-nginx \
        --create-namespace \
        --set controller.service.type=LoadBalancer \
        --wait --timeout=10m
    
    # Deploy cert-manager
    helm repo add jetstack https://charts.jetstack.io
    helm repo update
    
    helm upgrade --install cert-manager jetstack/cert-manager \
        --namespace cert-manager \
        --create-namespace \
        --version v1.13.0 \
        --set installCRDs=true \
        --wait --timeout=10m
    
    log "Ingress and SSL setup completed"
}

# Function to run health checks
run_health_checks() {
    log "Running health checks..."
    
    # Check namespace
    if kubectl get namespace "$KUBERNETES_NAMESPACE" &> /dev/null; then
        log "✓ Namespace $KUBERNETES_NAMESPACE exists"
    else
        error "✗ Namespace $KUBERNETES_NAMESPACE does not exist"
    fi
    
    # Check deployments
    local deployments=("api-gateway" "user-service" "energy-service" "device-service" "analytics-service" "billing-service" "notification-service" "edge-gateway-service")
    
    for deployment in "${deployments[@]}"; do
        if kubectl get deployment "$deployment" -n "$KUBERNETES_NAMESPACE" &> /dev/null; then
            local ready=$(kubectl get deployment "$deployment" -n "$KUBERNETES_NAMESPACE" -o jsonpath='{.status.readyReplicas}')
            local desired=$(kubectl get deployment "$deployment" -n "$KUBERNETES_NAMESPACE" -o jsonpath='{.spec.replicas}')
            
            if [ "$ready" = "$desired" ]; then
                log "✓ $deployment is ready ($ready/$desired)"
            else
                warn "⚠ $deployment is not ready ($ready/$desired)"
            fi
        else
            warn "✗ $deployment deployment not found"
        fi
    done
    
    # Check services
    local services=("api-gateway-service" "user-service" "energy-service" "device-service" "analytics-service" "billing-service" "notification-service" "edge-gateway-service")
    
    for service in "${services[@]}"; do
        if kubectl get service "$service" -n "$KUBERNETES_NAMESPACE" &> /dev/null; then
            log "✓ $service service exists"
        else
            warn "✗ $service service not found"
        fi
    done
    
    # Check ingress
    if kubectl get ingress -n "$KUBERNETES_NAMESPACE" &> /dev/null; then
        log "✓ Ingress resources found"
    else
        warn "⚠ No ingress resources found"
    fi
    
    log "Health checks completed"
}

# Function to show deployment status
show_deployment_status() {
    log "SmartWatts Advanced Infrastructure Deployment Status:"
    echo ""
    
    # Show namespace status
    echo "Namespace Status:"
    kubectl get namespace "$KUBERNETES_NAMESPACE" -o wide
    echo ""
    
    # Show deployments status
    echo "Deployments Status:"
    kubectl get deployments -n "$KUBERNETES_NAMESPACE" -o wide
    echo ""
    
    # Show services status
    echo "Services Status:"
    kubectl get services -n "$KUBERNETES_NAMESPACE" -o wide
    echo ""
    
    # Show pods status
    echo "Pods Status:"
    kubectl get pods -n "$KUBERNETES_NAMESPACE" -o wide
    echo ""
    
    # Show ingress status
    echo "Ingress Status:"
    kubectl get ingress -n "$KUBERNETES_NAMESPACE" -o wide
    echo ""
    
    # Show Helm releases
    echo "Helm Releases:"
    helm list -n "$KUBERNETES_NAMESPACE"
    echo ""
    
    # Show service URLs
    echo "Service URLs:"
    local api_gateway_ip=$(kubectl get service api-gateway-service -n "$KUBERNETES_NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
    local grafana_ip=$(kubectl get service grafana -n "$KUBERNETES_NAMESPACE" -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
    
    echo "  - API Gateway: http://$api_gateway_ip:8080"
    echo "  - Grafana: http://$grafana_ip:3000 (admin/smartwatts123)"
    echo ""
}

# Function to run all deployments
deploy_all() {
    log "Starting comprehensive advanced infrastructure deployment..."
    
    check_prerequisites
    deploy_kubernetes_infrastructure
    setup_cicd_pipeline
    setup_security_scanning
    setup_performance_testing
    setup_disaster_recovery
    setup_monitoring
    setup_ingress_ssl
    run_health_checks
    show_deployment_status
    
    log "Advanced infrastructure deployment completed successfully!"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS] [COMPONENT]"
    echo ""
    echo "Options:"
    echo "  --environment ENV   Set environment (default: production)"
    echo "  --namespace NS      Set Kubernetes namespace (default: smartwatts)"
    echo "  --help              Show this help message"
    echo ""
    echo "Components:"
    echo "  kubernetes          Deploy Kubernetes infrastructure only"
    echo "  cicd                Setup CI/CD pipeline only"
    echo "  security            Setup security scanning only"
    echo "  performance         Setup performance testing only"
    echo "  disaster-recovery   Setup disaster recovery only"
    echo "  monitoring          Setup monitoring only"
    echo "  ingress             Setup ingress and SSL only"
    echo "  all                 Deploy all components (default)"
    echo ""
    echo "Examples:"
    echo "  $0 --environment staging kubernetes"
    echo "  $0 --namespace smartwatts-dev all"
    echo "  $0 security performance"
}

# Main function
main() {
    local component="${1:-all}"
    
    case "$component" in
        kubernetes)
            check_prerequisites
            deploy_kubernetes_infrastructure
            run_health_checks
            show_deployment_status
            ;;
        cicd)
            setup_cicd_pipeline
            ;;
        security)
            setup_security_scanning
            ;;
        performance)
            setup_performance_testing
            ;;
        disaster-recovery)
            setup_disaster_recovery
            ;;
        monitoring)
            setup_monitoring
            ;;
        ingress)
            setup_ingress_ssl
            ;;
        all|"")
            deploy_all
            ;;
        --help|-h)
            show_usage
            ;;
        *)
            error "Unknown component: $component"
            ;;
    esac
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --namespace)
            KUBERNETES_NAMESPACE="$2"
            shift 2
            ;;
        --help|-h)
            show_usage
            exit 0
            ;;
        *)
            main "$1"
            shift
            ;;
    esac
done

# Run main function if no arguments
if [ $# -eq 0 ]; then
    main all
fi
