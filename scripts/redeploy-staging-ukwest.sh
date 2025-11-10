#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Redeployment to UK West"
echo "=========================================="

RESOURCE_GROUP="sw-staging-rg"
LOCATION="ukwest"
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
echo -e "${YELLOW}🔐 Enter VM admin password:${NC}"
read -s VM_PASSWORD
echo ""

# Build deployment command
DEPLOY_CMD="az deployment group create \
    --resource-group $RESOURCE_GROUP \
    --name main \
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

if eval "$DEPLOY_CMD"; then
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

