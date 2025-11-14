#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts VM Scale-Up for Faster Builds"
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
TARGET_SIZE="Standard_D4s_v3"  # 4 vCPU, 16GB RAM

echo -e "${BLUE}Configuration:${NC}"
echo "  Resource Group: $RESOURCE_GROUP"
echo "  VM Name: $VM_NAME"
echo "  Target Size: $TARGET_SIZE (4 vCPU, 16GB RAM)"
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

# Display cost information
echo ""
echo -e "${YELLOW}💰 Cost Information:${NC}"
echo "  Current size: $CURRENT_SIZE (Free tier if B1s)"
echo "  Target size: $TARGET_SIZE (~£96/month, ~£3-4 per day)"
echo "  Estimated build time improvement: 8-10x faster (20-30 min vs 60+ min)"
echo ""

# Confirm scaling
read -p "Do you want to scale up to $TARGET_SIZE? (y/N): " -n 1 -r
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
echo -e "${BLUE}Scaling VM to $TARGET_SIZE...${NC}"
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
echo "✅ VM Scale-Up Complete!"
echo "==========================================${NC}"
echo ""
echo "VM Details:"
echo "  Name: $VM_NAME"
echo "  Size: $NEW_SIZE (4 vCPU, 16GB RAM)"
echo "  State: Running"
if [ -n "$VM_IP" ]; then
    echo "  IP: $VM_IP"
fi
echo ""
echo -e "${YELLOW}💰 Cost Estimate:${NC}"
echo "  ~£3-4 per day while running at this size"
echo "  Remember to scale down after deployment to save credit!"
echo ""
echo -e "${BLUE}Next Steps:${NC}"
echo "  1. Run your deployment workflow"
echo "  2. Builds should complete in 20-30 minutes (vs 60+ minutes)"
echo "  3. After deployment, run: ./scripts/scale-down-vm.sh"
echo ""

