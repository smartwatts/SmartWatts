#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Redeployment to Central US"
echo "=========================================="

RESOURCE_GROUP="sw-staging-rg"
LOCATION="centralus"
VM_NAME="sw-staging-vm"
TEMPLATE_FILE="infrastructure/bicep/main.bicep"
PARAM_FILE="infrastructure/bicep/params.staging.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo -e "${RED}❌ Azure CLI is not installed${NC}"
    exit 1
fi

# Check if logged in
if ! az account show &> /dev/null; then
    echo -e "${YELLOW}⚠️  Not logged in to Azure. Logging in...${NC}"
    az login
fi

echo -e "${GREEN}✅ Azure CLI ready${NC}"

# Check if resource group exists
if az group show --name $RESOURCE_GROUP &> /dev/null; then
    echo -e "${YELLOW}⚠️  Resource group $RESOURCE_GROUP already exists${NC}"
    read -p "Do you want to delete it and recreate? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}🗑️  Deleting resource group $RESOURCE_GROUP...${NC}"
        az group delete --name $RESOURCE_GROUP --yes --no-wait
        echo -e "${GREEN}✅ Resource group deletion initiated${NC}"
        echo -e "${YELLOW}⏳ Waiting for deletion to complete (this may take a few minutes)...${NC}"
        
        # Wait for deletion
        while az group show --name $RESOURCE_GROUP &> /dev/null; do
            echo -n "."
            sleep 10
        done
        echo ""
        echo -e "${GREEN}✅ Resource group deleted${NC}"
    else
        echo -e "${YELLOW}⚠️  Keeping existing resource group. Make sure it's in $LOCATION${NC}"
    fi
fi

# Create resource group if it doesn't exist
if ! az group show --name $RESOURCE_GROUP &> /dev/null; then
    echo -e "${GREEN}📦 Creating resource group $RESOURCE_GROUP in $LOCATION...${NC}"
    az group create \
        --name $RESOURCE_GROUP \
        --location $LOCATION \
        --output none
    echo -e "${GREEN}✅ Resource group created${NC}"
else
    CURRENT_LOCATION=$(az group show --name $RESOURCE_GROUP --query location -o tsv)
    if [ "$CURRENT_LOCATION" != "$LOCATION" ]; then
        echo -e "${RED}❌ Resource group exists in $CURRENT_LOCATION, but we need $LOCATION${NC}"
        echo -e "${YELLOW}⚠️  Please delete the resource group manually and run this script again${NC}"
        exit 1
    fi
fi

# Get SSH public key
SSH_PUBLIC_KEY=""
if [ -f ~/.ssh/smartwatts_staging.pub ]; then
    SSH_PUBLIC_KEY=$(cat ~/.ssh/smartwatts_staging.pub)
    echo -e "${GREEN}✅ Found SSH public key${NC}"
else
    echo -e "${YELLOW}⚠️  SSH public key not found at ~/.ssh/smartwatts_staging.pub${NC}"
    read -p "Enter SSH public key (or press Enter to skip): " SSH_PUBLIC_KEY
fi

# Get VM admin password
echo -e "${YELLOW}🔐 Enter VM admin password (or press Enter to generate one):${NC}"
read -s VM_PASSWORD
echo ""

# If no password provided, generate a secure one
if [ -z "$VM_PASSWORD" ]; then
    echo -e "${YELLOW}⚠️  No password provided. Generating a secure password...${NC}"
    VM_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-25)
    echo -e "${GREEN}✅ Generated password: $VM_PASSWORD${NC}"
    echo -e "${YELLOW}⚠️  IMPORTANT: Save this password! You'll need it to SSH into the VM.${NC}"
    echo ""
fi

# Check for existing deployments and handle them
echo -e "${YELLOW}🔍 Checking for existing deployments...${NC}"

# List all deployments and check for active ones
ACTIVE_DEPLOYMENTS=$(az deployment group list \
    --resource-group $RESOURCE_GROUP \
    --query "[?properties.provisioningState=='Running' || properties.provisioningState=='InProgress'].name" -o tsv 2>/dev/null || echo "")

if [ -n "$ACTIVE_DEPLOYMENTS" ]; then
    echo -e "${YELLOW}⚠️  Found active deployments:${NC}"
    echo "$ACTIVE_DEPLOYMENTS" | while read -r DEPLOYMENT_NAME; do
        echo "  - $DEPLOYMENT_NAME"
        DEPLOYMENT_STATE=$(az deployment group show \
            --resource-group $RESOURCE_GROUP \
            --name "$DEPLOYMENT_NAME" \
            --query "properties.provisioningState" -o tsv 2>/dev/null || echo "")
        
        if [ "$DEPLOYMENT_STATE" = "InProgress" ] || [ "$DEPLOYMENT_STATE" = "Running" ]; then
            echo -e "${YELLOW}  ⏳ Waiting for deployment '$DEPLOYMENT_NAME' to complete (max 5 minutes)...${NC}"
            if az deployment group wait \
                --resource-group $RESOURCE_GROUP \
                --name "$DEPLOYMENT_NAME" \
                --created \
                --timeout 300 2>/dev/null; then
                echo -e "${GREEN}  ✅ Deployment '$DEPLOYMENT_NAME' completed${NC}"
            else
                echo -e "${YELLOW}  ⚠️  Deployment '$DEPLOYMENT_NAME' timed out. Cancelling...${NC}"
                az deployment group cancel \
                    --resource-group $RESOURCE_GROUP \
                    --name "$DEPLOYMENT_NAME" 2>/dev/null || true
                sleep 10
            fi
        fi
    done
fi

# Build deployment command with unique name
DEPLOYMENT_NAME="main-$(date +%s)"
DEPLOY_CMD="az deployment group create \
    --resource-group $RESOURCE_GROUP \
    --name $DEPLOYMENT_NAME \
    --template-file $TEMPLATE_FILE \
    --parameters @$PARAM_FILE \
    --parameters vmAdminPassword=\"$VM_PASSWORD\""

if [ -n "$SSH_PUBLIC_KEY" ]; then
    DEPLOY_CMD="$DEPLOY_CMD --parameters vmSshPublicKey=\"$SSH_PUBLIC_KEY\""
    echo -e "${GREEN}✅ SSH key will be added to VM${NC}"
else
    echo -e "${YELLOW}⚠️  No SSH key provided - VM will use password authentication only${NC}"
fi

# Deploy infrastructure
echo -e "${GREEN}🚀 Deploying infrastructure to $LOCATION...${NC}"
echo -e "${YELLOW}⏳ This will take 10-15 minutes...${NC}"

# Retry deployment up to 3 times
MAX_RETRIES=3
RETRY_COUNT=0
DEPLOYMENT_SUCCESS=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    echo -e "${YELLOW}Deployment attempt $((RETRY_COUNT + 1))/$MAX_RETRIES...${NC}"
    
    # Capture deployment output to check for "DeploymentActive" status
    DEPLOY_OUTPUT=$(eval "$DEPLOY_CMD" 2>&1)
    DEPLOY_EXIT_CODE=$?
    
    # Check if deployment command succeeded
    if [ $DEPLOY_EXIT_CODE -eq 0 ]; then
        DEPLOYMENT_SUCCESS=true
        break
    fi
    
    # Check if output contains "DeploymentActive" - this means deployment is running, not failed
    if echo "$DEPLOY_OUTPUT" | grep -q "DeploymentActive"; then
        echo -e "${YELLOW}⚠️  Deployment is already active (in progress)${NC}"
        
        # Extract deployment name from error message
        # Error format: "deployment with resource id '.../deployments/main-1762816371'"
        # Use sed for portability (works on macOS and Linux)
        ACTIVE_DEPLOYMENT_NAME=$(echo "$DEPLOY_OUTPUT" | sed -n "s/.*deployments\/\([^']*\).*/\1/p" | head -1)
        
        if [ -z "$ACTIVE_DEPLOYMENT_NAME" ]; then
            # Fallback: try to extract from the deployment name we just created
            ACTIVE_DEPLOYMENT_NAME="$DEPLOYMENT_NAME"
        fi
        
        if [ -n "$ACTIVE_DEPLOYMENT_NAME" ]; then
            echo -e "${YELLOW}⏳ Waiting for deployment '$ACTIVE_DEPLOYMENT_NAME' to complete (max 20 minutes)...${NC}"
            
            # Wait for deployment to complete
            if az deployment group wait \
                --resource-group $RESOURCE_GROUP \
                --name "$ACTIVE_DEPLOYMENT_NAME" \
                --created \
                --timeout 1200 2>/dev/null; then
                # Check final status
                FINAL_STATE=$(az deployment group show \
                    --resource-group $RESOURCE_GROUP \
                    --name "$ACTIVE_DEPLOYMENT_NAME" \
                    --query "properties.provisioningState" -o tsv 2>/dev/null || echo "")
                
                if [ "$FINAL_STATE" = "Succeeded" ]; then
                    echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
                    DEPLOYMENT_SUCCESS=true
                    break
                else
                    echo -e "${RED}❌ Deployment completed with state: $FINAL_STATE${NC}"
                    # Show error details if available
                    az deployment group show \
                        --resource-group $RESOURCE_GROUP \
                        --name "$ACTIVE_DEPLOYMENT_NAME" \
                        --query "properties.error" -o json 2>/dev/null || true
                    exit 1
                fi
            else
                echo -e "${RED}❌ Deployment wait timed out${NC}"
                exit 1
            fi
        else
            echo -e "${RED}❌ Could not extract deployment name from error message${NC}"
            echo "$DEPLOY_OUTPUT"
            exit 1
        fi
    else
        # Actual failure - check if we should retry
        DEPLOYMENT_ERROR=$?
        RETRY_COUNT=$((RETRY_COUNT + 1))
        
        # Check if it's a retryable error (network issues, transient errors)
        # Non-retryable: validation errors, authentication errors, etc.
        if echo "$DEPLOY_OUTPUT" | grep -qE "(InvalidTemplate|InvalidParameter|Unauthorized|Forbidden)"; then
            echo -e "${RED}❌ Non-retryable error detected:${NC}"
            echo "$DEPLOY_OUTPUT"
            exit 1
        fi
        
        if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
            echo -e "${YELLOW}⚠️  Deployment failed (exit code: $DEPLOYMENT_ERROR), retrying in 10 seconds...${NC}"
            echo "$DEPLOY_OUTPUT" | tail -5
            sleep 10
        else
            echo -e "${RED}❌ Deployment failed after $MAX_RETRIES attempts${NC}"
            echo "$DEPLOY_OUTPUT"
            exit 1
        fi
    fi
done

if [ "$DEPLOYMENT_SUCCESS" = "true" ]; then
    echo -e "${GREEN}✅ Infrastructure deployed successfully!${NC}"
    
    # Get VM IP
    VM_IP=$(az network public-ip show \
        --resource-group $RESOURCE_GROUP \
        --name ${VM_NAME}-pip \
        --query "ipAddress" -o tsv 2>/dev/null || echo "")
    
    if [ -n "$VM_IP" ]; then
        echo ""
        echo -e "${GREEN}=========================================="
        echo -e "Deployment Complete!"
        echo -e "==========================================${NC}"
        echo ""
        echo -e "${GREEN}VM IP: $VM_IP${NC}"
        echo ""
        echo -e "${YELLOW}Next steps:${NC}"
        echo "1. SSH into VM:"
        if [ -f ~/.ssh/smartwatts_staging ]; then
            echo "   ssh -i ~/.ssh/smartwatts_staging azureuser@$VM_IP"
        else
            echo "   ssh azureuser@$VM_IP"
            echo "   (Use password authentication)"
        fi
        echo ""
        echo "2. Deploy application:"
        echo "   git clone https://github.com/<your-org>/mySmartWatts.git"
        echo "   cd mySmartWatts/azure-deployment"
        echo "   docker-compose -f docker-compose.azure.yml up -d --build"
        echo ""
    else
        echo -e "${YELLOW}⚠️  Could not retrieve VM IP. Check Azure Portal.${NC}"
    fi
else
    echo -e "${RED}❌ Deployment failed${NC}"
    exit 1
fi

