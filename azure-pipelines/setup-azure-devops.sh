#!/bin/bash

# SmartWatts Azure DevOps Setup Script
# This script helps set up the Azure DevOps pipeline for SmartWatts

set -e

echo "🚀 Setting up Azure DevOps pipeline for SmartWatts..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Azure CLI is installed
check_azure_cli() {
    print_status "Checking Azure CLI installation..."
    if ! command -v az &> /dev/null; then
        print_error "Azure CLI is not installed. Please install it first:"
        echo "https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
        exit 1
    fi
    print_success "Azure CLI is installed"
}

# Check if user is logged in to Azure
check_azure_login() {
    print_status "Checking Azure login status..."
    if ! az account show &> /dev/null; then
        print_warning "Not logged in to Azure. Please run: az login"
        exit 1
    fi
    print_success "Logged in to Azure"
}

# Create Azure Container Registry
create_acr() {
    print_status "Creating Azure Container Registry..."
    
    # Get subscription ID
    SUBSCRIPTION_ID=$(az account show --query id -o tsv)
    print_status "Using subscription: $SUBSCRIPTION_ID"
    
    # Create resource group if it doesn't exist
    RESOURCE_GROUP="smartwatts-rg"
    LOCATION="eastus"
    
    print_status "Creating resource group: $RESOURCE_GROUP"
    az group create --name $RESOURCE_GROUP --location $LOCATION --output none
    
    # Create ACR
    ACR_NAME="smartwattsacr$(date +%s)"
    print_status "Creating ACR: $ACR_NAME"
    az acr create \
        --resource-group $RESOURCE_GROUP \
        --name $ACR_NAME \
        --sku Basic \
        --admin-enabled true \
        --output none
    
    print_success "ACR created: $ACR_NAME"
    echo "ACR Login Server: $ACR_NAME.azurecr.io"
    
    # Get ACR credentials
    ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username -o tsv)
    ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query passwords[0].value -o tsv)
    
    echo ""
    print_status "ACR Credentials:"
    echo "Username: $ACR_USERNAME"
    echo "Password: $ACR_PASSWORD"
    echo ""
    print_warning "Save these credentials for Azure DevOps service connection!"
}

# Create Azure Kubernetes Service
create_aks() {
    print_status "Creating Azure Kubernetes Service..."
    
    RESOURCE_GROUP="smartwatts-rg"
    AKS_NAME="smartwatts-aks"
    NODE_COUNT=3
    NODE_SIZE="Standard_D2s_v3"
    
    print_status "Creating AKS cluster: $AKS_NAME"
    az aks create \
        --resource-group $RESOURCE_GROUP \
        --name $AKS_NAME \
        --node-count $NODE_COUNT \
        --node-vm-size $NODE_SIZE \
        --enable-addons monitoring \
        --generate-ssh-keys \
        --output none
    
    print_success "AKS cluster created: $AKS_NAME"
    
    # Get AKS credentials
    print_status "Getting AKS credentials..."
    az aks get-credentials --resource-group $RESOURCE_GROUP --name $AKS_NAME --overwrite-existing
    
    print_success "AKS credentials configured"
}

# Create Azure Key Vault
create_key_vault() {
    print_status "Creating Azure Key Vault..."
    
    RESOURCE_GROUP="smartwatts-rg"
    KEY_VAULT_NAME="smartwatts-kv-$(date +%s)"
    
    print_status "Creating Key Vault: $KEY_VAULT_NAME"
    az keyvault create \
        --name $KEY_VAULT_NAME \
        --resource-group $RESOURCE_GROUP \
        --location eastus \
        --output none
    
    print_success "Key Vault created: $KEY_VAULT_NAME"
    
    # Add secrets
    print_status "Adding secrets to Key Vault..."
    az keyvault secret set --vault-name $KEY_VAULT_NAME --name "postgres-password" --value "SmartWatts2024!" --output none
    az keyvault secret set --vault-name $KEY_VAULT_NAME --name "redis-password" --value "Redis2024!" --output none
    az keyvault secret set --vault-name $KEY_VAULT_NAME --name "grafana-password" --value "Grafana2024!" --output none
    
    print_success "Secrets added to Key Vault"
}

# Generate Helm values
generate_helm_values() {
    print_status "Generating Helm values with Azure resources..."
    
    # Update values.yaml with Azure resources
    cat > helm/smartwatts/values-azure.yaml << EOF
# Azure-specific values for SmartWatts
global:
  imageRegistry: smartwattsacr.azurecr.io
  imagePullSecrets:
    - name: smartwatts-acr-secret

# Azure-specific environment variables
env:
  POSTGRES_HOST: postgresql
  POSTGRES_PORT: 5432
  POSTGRES_DB: smartwatts
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: SmartWatts2024!
  REDIS_HOST: redis-master
  REDIS_PORT: 6379
  REDIS_PASSWORD: Redis2024!
  EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://service-discovery:8761/eureka/
  JAVA_OPTS: "-Xmx512m -Xms256m"

# Azure monitoring
monitoring:
  enabled: true
  prometheus:
    enabled: true
  grafana:
    enabled: true
    adminPassword: "Grafana2024!"
EOF
    
    print_success "Helm values generated: helm/smartwatts/values-azure.yaml"
}

# Main setup function
main() {
    echo "🎯 SmartWatts Azure DevOps Setup"
    echo "================================"
    echo ""
    
    # Check prerequisites
    check_azure_cli
    check_azure_login
    
    echo ""
    print_status "Starting Azure resource creation..."
    echo ""
    
    # Create Azure resources
    create_acr
    echo ""
    create_aks
    echo ""
    create_key_vault
    echo ""
    generate_helm_values
    
    echo ""
    print_success "Azure resources created successfully!"
    echo ""
    print_status "Next steps:"
    echo "1. Go to Azure DevOps and create a new project"
    echo "2. Create service connections for ACR and AKS"
    echo "3. Create the pipeline using azure-pipelines/azure-pipelines.yml"
    echo "4. Configure environment variables and secrets"
    echo "5. Run the pipeline!"
    echo ""
    print_status "For detailed instructions, see: azure-pipelines/README.md"
}

# Run main function
main "$@"
