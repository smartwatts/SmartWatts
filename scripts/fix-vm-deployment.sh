#!/bin/bash
# Script to fix VM deployment issues

set -e

RESOURCE_GROUP="${1:-sw-staging-rg}"
VM_NAME="${2:-sw-staging-vm}"
VM_IP="${3:-52.182.155.148}"

echo "🔧 SmartWatts VM Deployment Fix"
echo "=================================="
echo "Resource Group: $RESOURCE_GROUP"
echo "VM Name: $VM_NAME"
echo "VM IP: $VM_IP"
echo ""

# Check if Azure CLI is logged in
if ! az account show &>/dev/null; then
    echo "❌ Azure CLI is not logged in"
    echo "   Run: az login"
    exit 1
fi

echo "1️⃣ Checking VM status..."
VM_STATE=$(az vm show -d --resource-group $RESOURCE_GROUP --name $VM_NAME --query powerState -o tsv 2>/dev/null || echo "not-found")
if [ "$VM_STATE" != "VM running" ]; then
    echo "   Starting VM..."
    az vm start --resource-group $RESOURCE_GROUP --name $VM_NAME
    echo "   ⏳ Waiting 30 seconds for VM to start..."
    sleep 30
else
    echo "   ✅ VM is running"
fi

echo ""
echo "2️⃣ Checking NSG rules..."
NSG_NAME=$(az network nsg list --resource-group $RESOURCE_GROUP --query "[?contains(name, 'nsg')].name" -o tsv | head -1)
if [ -n "$NSG_NAME" ]; then
    echo "   Found NSG: $NSG_NAME"
    
    # Check and add port 22 (SSH)
    SSH_RULE=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='22' || destinationPortRanges[?contains(@, '22')]].name" -o tsv | head -1)
    if [ -z "$SSH_RULE" ]; then
        echo "   ⚠️  Port 22 (SSH) rule not found, adding..."
        az network nsg rule create \
            --resource-group $RESOURCE_GROUP \
            --nsg-name "$NSG_NAME" \
            --name "AllowSSH" \
            --priority 1000 \
            --access Allow \
            --protocol Tcp \
            --direction Inbound \
            --destination-port-ranges 22 \
            --source-address-prefixes "*" \
            --output none
        echo "   ✅ SSH rule added"
    else
        echo "   ✅ Port 22 (SSH) rule exists: $SSH_RULE"
    fi
    
    # Check and add port 3000
    PORT_3000_RULE=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='3000' || destinationPortRanges[?contains(@, '3000')]].name" -o tsv | head -1)
    if [ -z "$PORT_3000_RULE" ]; then
        echo "   ⚠️  Port 3000 rule not found, adding..."
        az network nsg rule create \
            --resource-group $RESOURCE_GROUP \
            --nsg-name "$NSG_NAME" \
            --name "AllowPort3000" \
            --priority 1003 \
            --access Allow \
            --protocol Tcp \
            --direction Inbound \
            --destination-port-ranges 3000 \
            --source-address-prefixes "*" \
            --output none
        echo "   ✅ Port 3000 rule added"
    else
        echo "   ✅ Port 3000 rule exists: $PORT_3000_RULE"
    fi
    
    # Check and add port 80
    PORT_80_RULE=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='80' || destinationPortRanges[?contains(@, '80')]].name" -o tsv | head -1)
    if [ -z "$PORT_80_RULE" ]; then
        echo "   ⚠️  Port 80 rule not found, adding..."
        az network nsg rule create \
            --resource-group $RESOURCE_GROUP \
            --nsg-name "$NSG_NAME" \
            --name "AllowPort80" \
            --priority 1001 \
            --access Allow \
            --protocol Tcp \
            --direction Inbound \
            --destination-port-ranges 80 \
            --source-address-prefixes "*" \
            --output none
        echo "   ✅ Port 80 rule added"
    else
        echo "   ✅ Port 80 rule exists: $PORT_80_RULE"
    fi
else
    echo "   ⚠️  NSG not found"
fi

echo ""
echo "3️⃣ Testing connectivity..."
if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/22" 2>/dev/null; then
    echo "   ✅ Port 22 (SSH) is accessible"
    SSH_ACCESSIBLE=true
else
    echo "   ❌ Port 22 (SSH) is not accessible"
    echo "   This may be due to:"
    echo "     - VM firewall blocking SSH"
    echo "     - NSG rule not yet propagated"
    echo "     - VM agent issues"
    SSH_ACCESSIBLE=false
fi

if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/3000" 2>/dev/null; then
    echo "   ✅ Port 3000 is accessible"
else
    echo "   ❌ Port 3000 is not accessible (services may not be running)"
fi

if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/80" 2>/dev/null; then
    echo "   ✅ Port 80 is accessible"
else
    echo "   ❌ Port 80 is not accessible (Nginx may not be running)"
fi

echo ""
echo "=================================="
echo "📋 Summary and Next Steps"
echo "=================================="

if [ "$SSH_ACCESSIBLE" = "true" ]; then
    echo ""
    echo "✅ SSH is accessible! You can now:"
    echo ""
    echo "1. SSH into the VM:"
    echo "   ssh azureuser@$VM_IP"
    echo ""
    echo "2. Check if repository exists:"
    echo "   ls -la /home/azureuser/"
    echo ""
    echo "3. Check Docker containers:"
    echo "   cd /home/azureuser/SmartWatts/azure-deployment"
    echo "   docker-compose ps"
    echo ""
    echo "4. Start services if needed:"
    echo "   export POSTGRES_PASSWORD='SmartWatts2024!'"
    echo "   docker-compose -f docker-compose.azure.yml up -d --build"
    echo ""
    echo "5. Check logs:"
    echo "   docker-compose logs frontend"
    echo "   docker-compose logs api-gateway"
else
    echo ""
    echo "⚠️  SSH is not accessible. Try these options:"
    echo ""
    echo "Option 1: Use Azure Portal"
    echo "   1. Go to: https://portal.azure.com"
    echo "   2. Navigate to: $RESOURCE_GROUP > $VM_NAME"
    echo "   3. Click 'Connect' > 'Run Command'"
    echo "   4. Run: docker ps"
    echo ""
    echo "Option 2: Check GitHub Actions logs"
    echo "   - Go to your repository's Actions tab"
    echo "   - Check the 'Deploy Application to VM' step"
    echo "   - See if deployment succeeded or failed"
    echo ""
    echo "Option 3: Wait and retry"
    echo "   - NSG rules may take a few minutes to propagate"
    echo "   - Try again in 2-3 minutes"
    echo ""
    echo "Option 4: Check VM boot diagnostics"
    echo "   az vm boot-diagnostics get-boot-log --resource-group $RESOURCE_GROUP --name $VM_NAME"
fi

echo ""
echo "🔍 To check if services are running (if SSH works):"
echo "   curl http://$VM_IP:3000"
echo "   curl http://$VM_IP:8080/api/v1/health"
echo "   curl http://$VM_IP"

