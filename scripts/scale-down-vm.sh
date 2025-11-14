#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts VM Scale-Down to Free Tier"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
RESOURCE_GROUP="${AZURE_RESOURCE_GROUP:-sw-staging-rg}"
VM_NAME="${VM_NAME:-sw-staging-vm}"
TARGET_SIZE="Standard_B1s"  # 1 vCPU, 1GB RAM - Free tier

echo -e "${BLUE}Configuration:${NC}"
echo "  Resource Group: $RESOURCE_GROUP"
echo "  VM Name: $VM_NAME"
echo "  Target Size: $TARGET_SIZE (1 vCPU, 1GB RAM - Free tier)"
echo ""

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo -e "${RED}❌ Azure CLI is not installed${NC}"
    echo "Please install Azure CLI: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi

# Check if logged in
if ! az account show &> /dev/null; then
    echo -e "${YELLOW}⚠️  Not logged in to Azure. Logging in...${NC}"
    az login
fi

echo -e "${GREEN}✅ Azure CLI ready${NC}"
echo ""

# Check if VM exists
echo -e "${BLUE}Checking if VM exists...${NC}"
if ! az vm show --resource-group "$RESOURCE_GROUP" --name "$VM_NAME" >/dev/null 2>&1; then
    echo -e "${RED}❌ Error: VM '$VM_NAME' not found in resource group '$RESOURCE_GROUP'${NC}"
    exit 1
fi

# Get current VM size
CURRENT_SIZE=$(az vm show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$VM_NAME" \
    --query "hardwareProfile.vmSize" \
    -o tsv 2>/dev/null || echo "")

if [ -z "$CURRENT_SIZE" ]; then
    echo -e "${RED}❌ Error: Could not get current VM size${NC}"
    exit 1
fi

echo -e "${GREEN}Current VM size: $CURRENT_SIZE${NC}"

# Check if already at target size
if [ "$CURRENT_SIZE" = "$TARGET_SIZE" ]; then
    echo -e "${YELLOW}⚠️  VM is already at target size ($TARGET_SIZE)${NC}"
    echo "No scaling needed."
    exit 0
fi

# Get VM IP for service check
VM_IP=$(az network public-ip show \
    --resource-group "$RESOURCE_GROUP" \
    --name "${VM_NAME}-pip" \
    --query "ipAddress" \
    -o tsv 2>/dev/null || echo "")

# Optional: Check if services are running
if [ -n "$VM_IP" ]; then
    echo ""
    echo -e "${BLUE}Checking if services are running...${NC}"
    read -p "Do you want to verify services are running before scaling down? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Checking backend API..."
        if curl -s -f -m 5 "http://$VM_IP:8080/api/v1/health" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Backend API is responding${NC}"
        else
            echo -e "${YELLOW}⚠️  Backend API is not responding (may still be starting)${NC}"
        fi
        
        echo "Checking frontend..."
        if curl -s -f -m 5 "http://$VM_IP:3000" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ Frontend is responding${NC}"
        else
            echo -e "${YELLOW}⚠️  Frontend is not responding (may still be starting)${NC}"
        fi
    fi
fi

# Display cost information
echo ""
echo -e "${YELLOW}💰 Cost Information:${NC}"
echo "  Current size: $CURRENT_SIZE"
echo "  Target size: $TARGET_SIZE (Free tier - 750 hours/month)"
echo "  Cost savings: ~£96/month (if currently on D4s_v3)"
echo ""

# Confirm scaling
echo -e "${YELLOW}⚠️  WARNING: Scaling down will:${NC}"
echo "  1. Stop the VM (services will be unavailable during resize)"
echo "  2. Resize to free tier (B1s)"
echo "  3. Restart the VM"
echo ""
read -p "Do you want to scale down to $TARGET_SIZE? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}Scaling cancelled${NC}"
    exit 0
fi

# Check VM state
echo -e "${BLUE}Checking VM state...${NC}"
VM_STATE=$(az vm show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$VM_NAME" \
    --show-details \
    --query "powerState" \
    -o tsv 2>/dev/null || echo "Unknown")

echo "VM State: $VM_STATE"

# VM must be stopped to resize
if [ "$VM_STATE" != "VM deallocated" ] && [ "$VM_STATE" != "VM stopped" ]; then
    echo -e "${YELLOW}⚠️  VM is running. Stopping VM (required for resize)...${NC}"
    echo "Services will be unavailable during this process."
    az vm deallocate \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --output none
    
    echo "Waiting for VM to stop..."
    MAX_WAIT=300
    ELAPSED=0
    while [ $ELAPSED -lt $MAX_WAIT ]; do
        CURRENT_STATE=$(az vm show \
            --resource-group "$RESOURCE_GROUP" \
            --name "$VM_NAME" \
            --show-details \
            --query "powerState" \
            -o tsv 2>/dev/null || echo "Unknown")
        
        if [ "$CURRENT_STATE" = "VM deallocated" ] || [ "$CURRENT_STATE" = "VM stopped" ]; then
            echo -e "${GREEN}✅ VM stopped${NC}"
            break
        fi
        
        echo "Waiting for VM to stop... ($ELAPSED/$MAX_WAIT seconds)"
        sleep 10
        ELAPSED=$((ELAPSED + 10))
    done
    
    if [ "$CURRENT_STATE" != "VM deallocated" ] && [ "$CURRENT_STATE" != "VM stopped" ]; then
        echo -e "${RED}❌ Error: VM did not stop within timeout${NC}"
        exit 1
    fi
fi

# Resize VM
echo ""
echo -e "${BLUE}Scaling VM to $TARGET_SIZE (free tier)...${NC}"
echo "This may take 5-10 minutes..."

if az vm resize \
    --resource-group "$RESOURCE_GROUP" \
    --name "$VM_NAME" \
    --size "$TARGET_SIZE" \
    --output none; then
    echo -e "${GREEN}✅ VM resize initiated${NC}"
else
    echo -e "${RED}❌ Error: Failed to resize VM${NC}"
    exit 1
fi

# Wait for resize to complete
echo "Waiting for resize to complete..."
MAX_WAIT=600
ELAPSED=0
while [ $ELAPSED -lt $MAX_WAIT ]; do
    RESIZE_STATE=$(az vm show \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --query "provisioningState" \
        -o tsv 2>/dev/null || echo "Unknown")
    
    if [ "$RESIZE_STATE" = "Succeeded" ]; then
        echo -e "${GREEN}✅ VM resize completed${NC}"
        break
    fi
    
    echo "Resize in progress... ($ELAPSED/$MAX_WAIT seconds)"
    sleep 15
    ELAPSED=$((ELAPSED + 15))
done

# Verify new size
NEW_SIZE=$(az vm show \
    --resource-group "$RESOURCE_GROUP" \
    --name "$VM_NAME" \
    --query "hardwareProfile.vmSize" \
    -o tsv 2>/dev/null || echo "")

if [ "$NEW_SIZE" = "$TARGET_SIZE" ]; then
    echo -e "${GREEN}✅ VM successfully scaled to $TARGET_SIZE${NC}"
else
    echo -e "${RED}❌ Error: VM size is $NEW_SIZE, expected $TARGET_SIZE${NC}"
    exit 1
fi

# Start VM
echo ""
echo -e "${BLUE}Starting VM...${NC}"
az vm start \
    --resource-group "$RESOURCE_GROUP" \
    --name "$VM_NAME" \
    --output none

echo "Waiting for VM to start..."
MAX_WAIT=300
ELAPSED=0
while [ $ELAPSED -lt $MAX_WAIT ]; do
    CURRENT_STATE=$(az vm show \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --show-details \
        --query "powerState" \
        -o tsv 2>/dev/null || echo "Unknown")
    
    if [ "$CURRENT_STATE" = "VM running" ]; then
        echo -e "${GREEN}✅ VM is running${NC}"
        break
    fi
    
    echo "Waiting for VM to start... ($ELAPSED/$MAX_WAIT seconds)"
    sleep 10
    ELAPSED=$((ELAPSED + 10))
done

# Get VM IP
VM_IP=$(az network public-ip show \
    --resource-group "$RESOURCE_GROUP" \
    --name "${VM_NAME}-pip" \
    --query "ipAddress" \
    -o tsv 2>/dev/null || echo "")

echo ""
echo -e "${GREEN}=========================================="
echo "✅ VM Scale-Down Complete!"
echo "==========================================${NC}"
echo ""
echo "VM Details:"
echo "  Name: $VM_NAME"
echo "  Size: $NEW_SIZE (1 vCPU, 1GB RAM - Free tier)"
echo "  State: Running"
if [ -n "$VM_IP" ]; then
    echo "  IP: $VM_IP"
fi
echo ""
echo -e "${GREEN}💰 Cost Savings:${NC}"
echo "  VM is now on free tier (750 hours/month free)"
echo "  No ongoing charges for VM compute"
echo ""
echo -e "${YELLOW}⚠️  Note:${NC}"
echo "  Services may take a few minutes to fully restart"
echo "  Build times will be slower on B1s (60+ minutes)"
echo "  Use scale-up script when you need faster builds"
echo ""

