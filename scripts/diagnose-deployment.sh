#!/bin/bash
# Diagnostic script to check deployment status

set -e

VM_IP="${1:-52.182.155.148}"
RESOURCE_GROUP="${2:-sw-staging-rg}"
APP_NAME="${3:-SmartWatts}"

echo "🔍 SmartWatts Deployment Diagnostics"
echo "======================================"
echo "VM IP: $VM_IP"
echo "Resource Group: $RESOURCE_GROUP"
echo "Web App: $APP_NAME"
echo ""

# Check Azure login
echo "1️⃣ Checking Azure login..."
if az account show &>/dev/null; then
    echo "✅ Azure CLI is logged in"
    SUBSCRIPTION=$(az account show --query name -o tsv)
    echo "   Subscription: $SUBSCRIPTION"
else
    echo "❌ Azure CLI is not logged in"
    echo "   Run: az login"
    exit 1
fi

# Check VM status
echo ""
echo "2️⃣ Checking VM status..."
VM_STATE=$(az vm show -d --resource-group $RESOURCE_GROUP --name sw-staging-vm --query powerState -o tsv 2>/dev/null || echo "not-found")
if [ "$VM_STATE" == "VM running" ]; then
    echo "✅ VM is running"
else
    echo "❌ VM is not running (State: $VM_STATE)"
    echo "   Starting VM..."
    az vm start --resource-group $RESOURCE_GROUP --name sw-staging-vm
    echo "   ⏳ Waiting 30 seconds for VM to start..."
    sleep 30
fi

# Check NSG rules
echo ""
echo "3️⃣ Checking NSG rules..."
NSG_NAME=$(az network nsg list --resource-group $RESOURCE_GROUP --query "[?contains(name, 'nsg')].name" -o tsv | head -1)
if [ -n "$NSG_NAME" ]; then
    echo "✅ Found NSG: $NSG_NAME"
    echo "   Checking port 3000 rule..."
    PORT_3000=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='3000' || destinationPortRanges[?contains(@, '3000')]].{Name:name,Port:destinationPortRange,Access:access}" -o table 2>/dev/null || echo "Not found")
    if echo "$PORT_3000" | grep -q "3000"; then
        echo "   ✅ Port 3000 is allowed"
    else
        echo "   ⚠️  Port 3000 rule not found or not allowing access"
        echo "   Adding port 3000 rule..."
        az network nsg rule create \
            --resource-group $RESOURCE_GROUP \
            --nsg-name "$NSG_NAME" \
            --name "AllowPort3000" \
            --priority 1003 \
            --access Allow \
            --protocol Tcp \
            --direction Inbound \
            --destination-port-ranges 3000
    fi
    
    echo "   Checking port 80 rule..."
    PORT_80=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='80' || destinationPortRanges[?contains(@, '80')]].{Name:name,Port:destinationPortRange,Access:access}" -o table 2>/dev/null || echo "Not found")
    if echo "$PORT_80" | grep -q "80"; then
        echo "   ✅ Port 80 is allowed"
    else
        echo "   ⚠️  Port 80 rule not found"
    fi
else
    echo "⚠️  NSG not found"
fi

# Check VM connectivity
echo ""
echo "4️⃣ Checking VM connectivity..."
if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/22" 2>/dev/null; then
    echo "✅ Port 22 (SSH) is accessible"
else
    echo "❌ Port 22 (SSH) is not accessible"
    echo "   Check NSG rules and VM firewall"
fi

if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/3000" 2>/dev/null; then
    echo "✅ Port 3000 is accessible"
else
    echo "❌ Port 3000 is not accessible"
fi

if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/80" 2>/dev/null; then
    echo "✅ Port 80 is accessible"
else
    echo "❌ Port 80 is not accessible"
fi

# Check Azure Web App
echo ""
echo "5️⃣ Checking Azure Web App status..."
WEB_APP_STATE=$(az webapp show --resource-group $RESOURCE_GROUP --name $APP_NAME --query state -o tsv 2>/dev/null || echo "not-found")
if [ "$WEB_APP_STATE" == "Running" ]; then
    echo "✅ Web App is running"
    WEB_APP_URL=$(az webapp show --resource-group $RESOURCE_GROUP --name $APP_NAME --query defaultHostName -o tsv 2>/dev/null)
    echo "   URL: https://$WEB_APP_URL"
    
    # Check if web app is responding
    if curl -s -o /dev/null -w "%{http_code}" "https://$WEB_APP_URL" | grep -q "200\|301\|302"; then
        echo "   ✅ Web App is responding"
    else
        echo "   ⚠️  Web App is not responding (may still be starting)"
    fi
else
    echo "❌ Web App is not running (State: $WEB_APP_STATE)"
    if [ "$WEB_APP_STATE" != "not-found" ]; then
        echo "   Starting Web App..."
        az webapp start --resource-group $RESOURCE_GROUP --name $APP_NAME
    fi
fi

# Check VM services (requires SSH)
echo ""
echo "6️⃣ Checking VM services (requires SSH key)..."
if [ -f ~/.ssh/id_rsa ] || [ -f ~/.ssh/id_ed25519 ]; then
    echo "   Attempting to check Docker containers on VM..."
    SSH_KEY=""
    if [ -f ~/.ssh/id_rsa ]; then
        SSH_KEY="-i ~/.ssh/id_rsa"
    elif [ -f ~/.ssh/id_ed25519 ]; then
        SSH_KEY="-i ~/.ssh/id_ed25519"
    fi
    
    # Check if Docker is running
    DOCKER_STATUS=$(ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 $SSH_KEY azureuser@$VM_IP "sudo systemctl is-active docker" 2>/dev/null || echo "unknown")
    if [ "$DOCKER_STATUS" == "active" ]; then
        echo "   ✅ Docker is running"
        
        # Check containers
        CONTAINERS=$(ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 $SSH_KEY azureuser@$VM_IP "cd /home/azureuser/SmartWatts/azure-deployment 2>/dev/null || cd /home/azureuser/smartwatts/azure-deployment 2>/dev/null; docker-compose ps 2>/dev/null || docker compose ps 2>/dev/null" || echo "")
        if [ -n "$CONTAINERS" ]; then
            echo "   Container status:"
            echo "$CONTAINERS" | head -20
        else
            echo "   ⚠️  Could not get container status"
        fi
        
        # Check if frontend container is running
        FRONTEND_RUNNING=$(ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 $SSH_KEY azureuser@$VM_IP "docker ps --filter 'name=frontend' --format '{{.Status}}' 2>/dev/null" || echo "")
        if [ -n "$FRONTEND_RUNNING" ]; then
            echo "   ✅ Frontend container is running"
        else
            echo "   ❌ Frontend container is not running"
        fi
        
        # Check Nginx
        NGINX_STATUS=$(ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 $SSH_KEY azureuser@$VM_IP "sudo systemctl is-active nginx" 2>/dev/null || echo "unknown")
        if [ "$NGINX_STATUS" == "active" ]; then
            echo "   ✅ Nginx is running"
        else
            echo "   ⚠️  Nginx is not running (Status: $NGINX_STATUS)"
        fi
    else
        echo "   ⚠️  Docker is not running (Status: $DOCKER_STATUS)"
    fi
else
    echo "   ⚠️  SSH key not found, skipping VM service checks"
    echo "   To check manually: ssh azureuser@$VM_IP"
fi

echo ""
echo "======================================"
echo "📋 Summary"
echo "======================================"
echo "VM IP: $VM_IP"
echo "  - Try: http://$VM_IP:3000 (direct)"
echo "  - Try: http://$VM_IP (via Nginx)"
echo ""
echo "Azure Web App:"
if [ -n "$WEB_APP_URL" ]; then
    echo "  - Try: https://$WEB_APP_URL"
else
    echo "  - Web App URL not found"
fi
echo ""
echo "🔧 Next Steps:"
echo "1. If VM services are not running, check deployment logs"
echo "2. If NSG rules are missing, they should be added automatically"
echo "3. Check container logs: ssh azureuser@$VM_IP 'cd azure-deployment && docker-compose logs frontend'"
echo "4. Check Nginx logs: ssh azureuser@$VM_IP 'sudo tail -f /var/log/nginx/error.log'"

