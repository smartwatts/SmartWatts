#!/bin/bash
# Improved script to fix VM deployment issues with multiple recovery methods
# This version adds VM restart, better retry logic, and Run Command fallback

set -e

RESOURCE_GROUP="${1:-sw-staging-rg}"
VM_NAME="${2:-sw-staging-vm}"
VM_IP="${3:-52.182.155.148}"

# Configuration
MAX_RETRIES=5
INITIAL_DELAY=10
MAX_DELAY=120
SSH_TIMEOUT=30

echo "🔧 SmartWatts VM Deployment Fix (Improved)"
echo "==========================================="
echo "Resource Group: $RESOURCE_GROUP"
echo "VM Name: $VM_NAME"
echo "VM IP: $VM_IP"
echo ""

# Function to test SSH connectivity with retry and exponential backoff
test_ssh_connectivity() {
    local ip=$1
    local username=$2
    local max_attempts=$3
    local initial_delay=$4
    local max_delay=$5
    
    echo "Testing SSH connectivity to $username@$ip..."
    
    for i in $(seq 1 $max_attempts); do
        # Calculate delay with exponential backoff
        if [ $i -eq 1 ]; then
            delay=0
        else
            delay=$((initial_delay * (2 ** (i - 2))))
            if [ $delay -gt $max_delay ]; then
                delay=$max_delay
            fi
        fi
        
        if [ $delay -gt 0 ]; then
            echo "  Waiting ${delay}s before attempt $i/$max_attempts..."
            sleep $delay
        fi
        
        echo "  SSH connection attempt $i/$max_attempts..."
        
        if ssh -o StrictHostKeyChecking=no \
             -o UserKnownHostsFile=/dev/null \
             -o ConnectTimeout=$SSH_TIMEOUT \
             -o ServerAliveInterval=10 \
             -o ServerAliveCountMax=3 \
             -o BatchMode=yes \
             -i ~/.ssh/id_rsa \
             ${username}@${ip} 'echo "SSH_TEST_SUCCESS"' 2>/dev/null | grep -q "SSH_TEST_SUCCESS"; then
            echo "  ✅ SSH connectivity successful!"
            return 0
        else
            echo "  ❌ SSH attempt $i failed"
        fi
    done
    
    echo "  ❌ All SSH connection attempts failed"
    return 1
}

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
    VM_STATE=$(az vm show -d --resource-group $RESOURCE_GROUP --name $VM_NAME --query powerState -o tsv 2>/dev/null || echo "not-found")
    if [ "$VM_STATE" != "VM running" ]; then
        echo "   ❌ VM failed to start. Current state: $VM_STATE"
        echo "   Check Azure Portal for details: https://portal.azure.com/#@/resource/subscriptions/*/resourceGroups/$RESOURCE_GROUP/providers/Microsoft.Compute/virtualMachines/$VM_NAME"
        exit 1
    fi
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
        echo "   ✅ SSH rule added (waiting 15s for propagation)..."
        sleep 15
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
echo "3️⃣ Testing initial connectivity..."
SSH_ACCESSIBLE=false
if test_ssh_connectivity "$VM_IP" "azureuser" 3 5 30; then
    SSH_ACCESSIBLE=true
    echo "   ✅ SSH is accessible - no recovery needed!"
else
    echo "   ❌ SSH is not accessible - attempting recovery..."
fi

# Recovery methods if SSH is not accessible
if [ "$SSH_ACCESSIBLE" != "true" ]; then
    echo ""
    echo "4️⃣ Attempting recovery methods..."
    
    # Method 1: VM Restart (fastest, preserves data)
    echo ""
    echo "   Method 1: VM Restart (preserves all data)..."
    echo "   Restarting VM..."
    az vm restart --resource-group $RESOURCE_GROUP --name $VM_NAME --output none
    
    echo "   ⏳ Waiting 60 seconds for VM to restart..."
    sleep 60
    
    # Wait for VM to be running
    MAX_WAIT=180
    ELAPSED=0
    while [ $ELAPSED -lt $MAX_WAIT ]; do
        CURRENT_STATE=$(az vm show -d --resource-group $RESOURCE_GROUP --name $VM_NAME --query powerState -o tsv 2>/dev/null || echo "Unknown")
        if [ "$CURRENT_STATE" = "VM running" ]; then
            echo "   ✅ VM is running after restart"
            break
        fi
        echo "   VM state: $CURRENT_STATE (waiting... $ELAPSED/$MAX_WAIT seconds)"
        sleep 15
        ELAPSED=$((ELAPSED + 15))
    done
    
    echo "   Waiting 30 seconds for SSH service to be ready..."
    sleep 30
    
    if test_ssh_connectivity "$VM_IP" "azureuser" 3 10 30; then
        SSH_ACCESSIBLE=true
        echo "   ✅ SSH accessible after VM restart!"
    else
        echo "   ❌ SSH still not accessible after restart"
        
        # Method 2: Try Run Command to restart SSH service
        echo ""
        echo "   Method 2: Using Azure Run Command to restart SSH service..."
        echo "   Executing remote command via Azure..."
        
        RUN_CMD_RESULT=$(az vm run-command invoke \
            --resource-group $RESOURCE_GROUP \
            --name $VM_NAME \
            --command-id RunShellScript \
            --scripts "sudo systemctl restart sshd && sudo systemctl status sshd" \
            --query "value[0].message" -o tsv 2>/dev/null || echo "failed")
        
        if echo "$RUN_CMD_RESULT" | grep -q "active\|running"; then
            echo "   ✅ SSH service restarted via Run Command"
            echo "   Waiting 20 seconds for SSH to be ready..."
            sleep 20
            
            if test_ssh_connectivity "$VM_IP" "azureuser" 3 10 30; then
                SSH_ACCESSIBLE=true
                echo "   ✅ SSH accessible after Run Command!"
            else
                echo "   ❌ SSH still not accessible after Run Command"
            fi
        else
            echo "   ❌ Run Command failed or SSH service not running"
        fi
        
        # Method 3: VM Redeploy (only if other methods failed)
        if [ "$SSH_ACCESSIBLE" != "true" ]; then
            echo ""
            echo "   Method 3: VM Redeploy (moves VM to new host, preserves data)..."
            echo "   ⚠️  This may take 5-10 minutes..."
            
            # Check if redeploy is likely to succeed (check VM agent status)
            VM_AGENT_STATUS=$(az vm show \
                --resource-group $RESOURCE_GROUP \
                --name $VM_NAME \
                --query "instanceView.vmAgent.statuses[0].displayStatus" -o tsv 2>/dev/null || echo "Unknown")
            
            if [ "$VM_AGENT_STATUS" != "Ready" ]; then
                echo "   ⚠️  VM Agent status: $VM_AGENT_STATUS"
                echo "   Redeploy may fail if VM agent is not ready"
            fi
            
            # Attempt redeploy with error handling
            if az vm redeploy \
                --resource-group $RESOURCE_GROUP \
                --name $VM_NAME \
                --output none 2>&1; then
                echo "   ✅ VM redeploy initiated"
                
                echo "   ⏳ Waiting for VM to be ready after redeploy..."
                sleep 60
                
                # Wait for VM to be running
                MAX_WAIT=300
                ELAPSED=0
                while [ $ELAPSED -lt $MAX_WAIT ]; do
                    CURRENT_STATE=$(az vm show -d --resource-group $RESOURCE_GROUP --name $VM_NAME --query powerState -o tsv 2>/dev/null || echo "Unknown")
                    if [ "$CURRENT_STATE" = "VM running" ]; then
                        echo "   ✅ VM is running after redeploy"
                        break
                    fi
                    echo "   VM state: $CURRENT_STATE (waiting... $ELAPSED/$MAX_WAIT seconds)"
                    sleep 15
                    ELAPSED=$((ELAPSED + 15))
                done
                
                echo "   Waiting 60 seconds for VM agent and SSH service to be ready..."
                sleep 60
                
                if test_ssh_connectivity "$VM_IP" "azureuser" 5 15 60; then
                    SSH_ACCESSIBLE=true
                    echo "   ✅ SSH accessible after VM redeploy!"
                else
                    echo "   ❌ SSH still not accessible after redeploy"
                fi
            else
                echo "   ❌ VM redeploy failed (this is an Azure infrastructure issue)"
                echo "   Error: VMRedeploymentFailed - Azure internal error"
                echo ""
                echo "   📋 Alternative options:"
                echo "   1. Wait 10-15 minutes and retry (Azure may resolve the issue)"
                echo "   2. Use Azure Portal Serial Console:"
                echo "      https://portal.azure.com/#@/resource/subscriptions/*/resourceGroups/$RESOURCE_GROUP/providers/Microsoft.Compute/virtualMachines/$VM_NAME/serialConsole"
                echo "   3. Check VM boot diagnostics for errors:"
                echo "      az vm boot-diagnostics get-boot-log --resource-group $RESOURCE_GROUP --name $VM_NAME"
                echo "   4. Consider recreating the VM if issue persists"
            fi
        fi
    fi
fi

echo ""
echo "5️⃣ Final connectivity test..."
if [ "$SSH_ACCESSIBLE" = "true" ]; then
    echo "   ✅ SSH is accessible!"
    
    # Test other ports
    if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/3000" 2>/dev/null; then
        echo "   ✅ Port 3000 is accessible"
    else
        echo "   ⚠️  Port 3000 is not accessible (services may not be running)"
    fi
    
    if timeout 5 bash -c "echo > /dev/tcp/$VM_IP/80" 2>/dev/null; then
        echo "   ✅ Port 80 is accessible"
    else
        echo "   ⚠️  Port 80 is not accessible (Nginx may not be running)"
    fi
else
    echo "   ❌ SSH is still not accessible after all recovery attempts"
fi

echo ""
echo "==========================================="
echo "📋 Summary and Next Steps"
echo "==========================================="

if [ "$SSH_ACCESSIBLE" = "true" ]; then
    echo ""
    echo "✅ SSH is accessible! You can now:"
    echo ""
    echo "1. SSH into the VM:"
    echo "   ssh -i ~/.ssh/id_rsa azureuser@$VM_IP"
    echo ""
    echo "2. Check if repository exists:"
    echo "   ls -la /home/azureuser/"
    echo ""
    echo "3. Check Docker containers:"
    echo "   cd /home/azureuser/smartwatts/azure-deployment || cd /home/azureuser/SmartWatts/azure-deployment"
    echo "   docker compose ps || docker-compose ps"
    echo ""
    echo "4. Start services if needed:"
    echo "   export POSTGRES_PASSWORD='SmartWatts2024!'"
    echo "   docker compose -f docker-compose.azure.yml up -d --build"
    echo ""
    echo "5. Check logs:"
    echo "   docker compose logs frontend"
    echo "   docker compose logs api-gateway"
else
    echo ""
    echo "⚠️  SSH is not accessible after all recovery attempts."
    echo ""
    echo "📋 Manual Intervention Options:"
    echo ""
    echo "Option 1: Azure Portal Serial Console"
    echo "   1. Go to: https://portal.azure.com"
    echo "   2. Navigate to: $RESOURCE_GROUP > $VM_NAME"
    echo "   3. Click 'Serial console' in the left menu"
    echo "   4. Login and check SSH service: sudo systemctl status sshd"
    echo "   5. Restart SSH if needed: sudo systemctl restart sshd"
    echo ""
    echo "Option 2: Azure Portal Run Command"
    echo "   1. Go to: https://portal.azure.com"
    echo "   2. Navigate to: $RESOURCE_GROUP > $VM_NAME"
    echo "   3. Click 'Run command' in the left menu"
    echo "   4. Select 'RunShellScript'"
    echo "   5. Run: sudo systemctl restart sshd"
    echo ""
    echo "Option 3: Check Boot Diagnostics"
    echo "   az vm boot-diagnostics get-boot-log --resource-group $RESOURCE_GROUP --name $VM_NAME"
    echo ""
    echo "Option 4: Wait and Retry"
    echo "   - Azure infrastructure issues are often temporary"
    echo "   - Wait 10-15 minutes and retry the deployment"
    echo "   - Check Azure Service Health: https://status.azure.com"
    echo ""
    echo "Option 5: Recreate VM (Last Resort)"
    echo "   - This will delete and recreate the VM"
    echo "   - All data on the VM will be lost"
    echo "   - Use only if other options fail"
fi

echo ""
echo "🔍 To check if services are running (if SSH works):"
echo "   curl http://$VM_IP:3000"
echo "   curl http://$VM_IP:8080/api/v1/health"
echo "   curl http://$VM_IP"

