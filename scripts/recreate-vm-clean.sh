#!/bin/bash
# Clean VM recreation script - deletes and recreates VM when all recovery methods fail
# This is a last resort when VM is in an unrecoverable state

set -e

RESOURCE_GROUP="${1:-sw-staging-rg}"
VM_NAME="${2:-sw-staging-vm}"
LOCATION="${3:-centralus}"
VM_ADMIN_USERNAME="${4:-azureuser}"

echo "🔄 SmartWatts VM Clean Recreation"
echo "=================================="
echo "Resource Group: $RESOURCE_GROUP"
echo "VM Name: $VM_NAME"
echo "Location: $LOCATION"
echo ""
echo "⚠️  WARNING: This will DELETE and RECREATE the VM"
echo "⚠️  All data on the VM will be LOST"
echo ""
read -p "Are you sure you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "❌ Operation cancelled"
    exit 1
fi

# Check if Azure CLI is logged in
if ! az account show &>/dev/null; then
    echo "❌ Azure CLI is not logged in"
    echo "   Run: az login"
    exit 1
fi

# Find SSH key (check multiple locations)
SSH_PRIVATE_KEY=""
SSH_PUBLIC_KEY=""

# Check for private key in common locations
if [ -f ~/.ssh/id_rsa ]; then
    SSH_PRIVATE_KEY=~/.ssh/id_rsa
elif [ -f ~/.ssh/id_ed25519 ]; then
    SSH_PRIVATE_KEY=~/.ssh/id_ed25519
elif [ -n "$VM_SSH_PRIVATE_KEY_STAGING" ]; then
    # If provided as environment variable (from GitHub Secrets)
    echo "$VM_SSH_PRIVATE_KEY_STAGING" > /tmp/temp_ssh_key
    chmod 600 /tmp/temp_ssh_key
    SSH_PRIVATE_KEY=/tmp/temp_ssh_key
fi

# Extract public key
if [ -n "$SSH_PRIVATE_KEY" ] && [ -f "$SSH_PRIVATE_KEY" ]; then
    # Try to get public key from .pub file first
    if [ -f "${SSH_PRIVATE_KEY}.pub" ]; then
        SSH_PUBLIC_KEY=$(cat "${SSH_PRIVATE_KEY}.pub")
    else
        # Generate public key from private key
        SSH_PUBLIC_KEY=$(ssh-keygen -y -f "$SSH_PRIVATE_KEY" 2>/dev/null || echo "")
    fi
elif [ -f ~/.ssh/id_rsa.pub ]; then
    # If only public key exists, use it (but warn)
    SSH_PUBLIC_KEY=$(cat ~/.ssh/id_rsa.pub)
    echo "⚠️  Warning: Only public key found. Using it, but you'll need the private key to SSH later."
elif [ -n "$VM_SSH_PUBLIC_KEY_STAGING" ]; then
    # Use public key from environment variable
    SSH_PUBLIC_KEY="$VM_SSH_PUBLIC_KEY_STAGING"
fi

if [ -z "$SSH_PUBLIC_KEY" ]; then
    echo "❌ Could not find SSH public key"
    echo ""
    echo "Options:"
    echo "1. Generate a new SSH key pair:"
    echo "   ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ''"
    echo ""
    echo "2. Set environment variable:"
    echo "   export VM_SSH_PUBLIC_KEY_STAGING='your-public-key-here'"
    echo ""
    echo "3. Or provide the public key file at ~/.ssh/id_rsa.pub"
    exit 1
fi

echo "✅ Using SSH public key: $(echo "$SSH_PUBLIC_KEY" | awk '{print $3}')"

echo ""
echo "1️⃣ Checking current VM state..."
if az vm show --resource-group $RESOURCE_GROUP --name $VM_NAME &>/dev/null; then
    echo "   VM exists. Getting details..."
    
    # Get VM IP before deletion (for reference)
    VM_IP=$(az network public-ip show \
        --resource-group $RESOURCE_GROUP \
        --name "${VM_NAME}-pip" \
        --query "ipAddress" -o tsv 2>/dev/null || echo "")
    
    if [ -n "$VM_IP" ]; then
        echo "   Current VM IP: $VM_IP"
    fi
    
    echo ""
    echo "2️⃣ Deleting VM and associated resources (this may take 2-3 minutes)..."
    
    # Delete VM (this will also delete the OS disk, but not the NIC or Public IP)
    az vm delete \
        --resource-group $RESOURCE_GROUP \
        --name $VM_NAME \
        --yes \
        --output none
    
    echo "   ⏳ Waiting for VM deletion to complete..."
    MAX_WAIT=180
    ELAPSED=0
    while az vm show --resource-group $RESOURCE_GROUP --name $VM_NAME &>/dev/null && [ $ELAPSED -lt $MAX_WAIT ]; do
        echo -n "."
        sleep 5
        ELAPSED=$((ELAPSED + 5))
    done
    echo ""
    
    if az vm show --resource-group $RESOURCE_GROUP --name $VM_NAME &>/dev/null; then
        echo "   ❌ VM deletion timed out"
        echo "   Please check Azure Portal and delete manually if needed"
        exit 1
    else
        echo "   ✅ VM deleted successfully"
    fi
    
    # Delete network interface (NIC) - it may still be holding the public IP
    echo "   Deleting network interface..."
    NIC_NAMES=$(az network nic list --resource-group $RESOURCE_GROUP --query "[?contains(name, '$VM_NAME')].name" -o tsv 2>/dev/null || echo "")
    if [ -n "$NIC_NAMES" ]; then
        for NIC_NAME in $NIC_NAMES; do
            echo "   Deleting NIC: $NIC_NAME"
            az network nic delete \
                --resource-group $RESOURCE_GROUP \
                --name $NIC_NAME \
                --output none 2>/dev/null || echo "   ⚠️  NIC $NIC_NAME may already be deleted"
        done
        echo "   ✅ Network interfaces deleted"
        sleep 10  # Wait for NIC deletion to complete
    else
        echo "   ✅ No network interfaces found to delete"
    fi
else
    echo "   VM does not exist - will create new one"
    # Even if VM doesn't exist, check for orphaned NICs
    echo "   Checking for orphaned network interfaces..."
    NIC_NAMES=$(az network nic list --resource-group $RESOURCE_GROUP --query "[?contains(name, '$VM_NAME')].name" -o tsv 2>/dev/null || echo "")
    if [ -n "$NIC_NAMES" ]; then
        for NIC_NAME in $NIC_NAMES; do
            echo "   Deleting orphaned NIC: $NIC_NAME"
            az network nic delete \
                --resource-group $RESOURCE_GROUP \
                --name $NIC_NAME \
                --output none 2>/dev/null || echo "   ⚠️  NIC $NIC_NAME may already be deleted"
        done
        echo "   ✅ Orphaned network interfaces deleted"
        sleep 10
    fi
fi

echo ""
echo "3️⃣ Checking/creating required resources..."

# Delete any remaining NICs that might be holding the public IP
echo "   Cleaning up any remaining network interfaces..."
NIC_NAMES=$(az network nic list --resource-group $RESOURCE_GROUP --query "[?contains(name, '$VM_NAME')].name" -o tsv 2>/dev/null || echo "")
if [ -n "$NIC_NAMES" ]; then
    for NIC_NAME in $NIC_NAMES; do
        echo "   Deleting NIC: $NIC_NAME"
        az network nic delete \
            --resource-group $RESOURCE_GROUP \
            --name $NIC_NAME \
            --output none 2>/dev/null || echo "   ⚠️  NIC $NIC_NAME may already be deleted"
    done
    echo "   ✅ Network interfaces cleaned up"
    sleep 10  # Wait for NIC deletion to complete
fi

# Check if public IP exists and is available
PUBLIC_IP_EXISTS=false
if az network public-ip show --resource-group $RESOURCE_GROUP --name "${VM_NAME}-pip" &>/dev/null; then
    PUBLIC_IP_EXISTS=true
    # Check if public IP is still attached
    IP_ALLOCATION=$(az network public-ip show \
        --resource-group $RESOURCE_GROUP \
        --name "${VM_NAME}-pip" \
        --query "ipConfiguration.id" -o tsv 2>/dev/null || echo "None")
    
    if [ -n "$IP_ALLOCATION" ] && [ "$IP_ALLOCATION" != "None" ]; then
        echo "   ⚠️  Public IP is still attached, waiting for disassociation..."
        # Wait a bit more for Azure to release it
        sleep 15
        # Try one more time to delete any remaining NICs
        NIC_NAMES=$(az network nic list --resource-group $RESOURCE_GROUP --query "[?contains(name, '$VM_NAME')].name" -o tsv 2>/dev/null || echo "")
        if [ -n "$NIC_NAMES" ]; then
            for NIC_NAME in $NIC_NAMES; do
                az network nic delete --resource-group $RESOURCE_GROUP --name "$NIC_NAME" --output none 2>/dev/null || true
            done
            sleep 10
        fi
    fi
    echo "   ✅ Public IP exists and should be available"
else
    echo "   Creating public IP..."
    az network public-ip create \
        --resource-group $RESOURCE_GROUP \
        --name "${VM_NAME}-pip" \
        --location $LOCATION \
        --sku Standard \
        --allocation-method Static \
        --output none
    echo "   ✅ Public IP created"
fi

# Check if NSG exists
NSG_NAME=$(az network nsg list --resource-group $RESOURCE_GROUP --query "[?contains(name, 'nsg')].name" -o tsv | head -1)
if [ -z "$NSG_NAME" ]; then
    echo "   Creating NSG..."
    NSG_NAME="${VM_NAME}-nsg"
    az network nsg create \
        --resource-group $RESOURCE_GROUP \
        --name $NSG_NAME \
        --location $LOCATION \
        --output none
    echo "   ✅ NSG created"
else
    echo "   ✅ NSG exists: $NSG_NAME"
fi

# Ensure SSH rule exists
SSH_RULE=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='22' || destinationPortRanges[?contains(@, '22')]].name" -o tsv | head -1)
if [ -z "$SSH_RULE" ]; then
    echo "   Adding SSH rule to NSG..."
    az network nsg rule create \
        --resource-group $RESOURCE_GROUP \
        --nsg-name "$NSG_NAME" \
        --name "SSH" \
        --priority 1000 \
        --access Allow \
        --protocol Tcp \
        --direction Inbound \
        --destination-port-ranges 22 \
        --source-address-prefixes "*" \
        --output none
    echo "   ✅ SSH rule added"
else
    echo "   ✅ SSH rule exists"
fi

echo ""
echo "4️⃣ Creating new VM with proper SSH configuration..."
echo "   This will take 3-5 minutes..."

# Create VM with explicit SSH key and Custom Script Extension
az vm create \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --location $LOCATION \
    --image "Canonical:0001-com-ubuntu-server-focal:20_04-lts-gen2:latest" \
    --size Standard_B1s \
    --admin-username $VM_ADMIN_USERNAME \
    --ssh-key-values "$SSH_PUBLIC_KEY" \
    --public-ip-address "${VM_NAME}-pip" \
    --nsg $NSG_NAME \
    --output none

echo "   ✅ VM created"

echo ""
echo "5️⃣ Configuring VM with Custom Script Extension to ensure SSH works..."
# This extension ensures SSH service is installed, enabled, and started
az vm extension set \
    --resource-group $RESOURCE_GROUP \
    --vm-name $VM_NAME \
    --name CustomScript \
    --publisher Microsoft.Azure.Extensions \
    --version 2.1 \
    --settings '{"commandToExecute":"apt-get update -qq && apt-get install -y openssh-server && systemctl enable sshd && systemctl enable ssh && systemctl start sshd && systemctl start ssh && sleep 2 && systemctl status sshd || systemctl status ssh"}' \
    --output none

echo "   ✅ Custom Script Extension configured"
echo "   ⏳ Waiting 30 seconds for extension to run..."
sleep 30

echo ""
echo "6️⃣ Opening required ports..."
# Check and add port 22 (SSH) if not exists
SSH_RULE=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='22' || destinationPortRanges[?contains(@, '22')]].name" -o tsv | head -1)
if [ -z "$SSH_RULE" ]; then
    echo "   Adding port 22 (SSH) rule..."
    az vm open-port \
        --resource-group $RESOURCE_GROUP \
        --name $VM_NAME \
        --port 22 \
        --priority 1000 \
        --output none
    echo "   ✅ Port 22 rule added"
else
    echo "   ✅ Port 22 (SSH) rule already exists: $SSH_RULE"
fi

# Check and add port 80 if not exists
PORT_80_RULE=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='80' || destinationPortRanges[?contains(@, '80')]].name" -o tsv | head -1)
if [ -z "$PORT_80_RULE" ]; then
    echo "   Adding port 80 rule..."
    az vm open-port \
        --resource-group $RESOURCE_GROUP \
        --name $VM_NAME \
        --port 80 \
        --priority 1001 \
        --output none
    echo "   ✅ Port 80 rule added"
else
    echo "   ✅ Port 80 rule already exists: $PORT_80_RULE"
fi

# Check and add port 3000 if not exists
PORT_3000_RULE=$(az network nsg rule list --nsg-name "$NSG_NAME" --resource-group $RESOURCE_GROUP --query "[?destinationPortRange=='3000' || destinationPortRanges[?contains(@, '3000')]].name" -o tsv | head -1)
if [ -z "$PORT_3000_RULE" ]; then
    echo "   Adding port 3000 rule..."
    az vm open-port \
        --resource-group $RESOURCE_GROUP \
        --name $VM_NAME \
        --port 3000 \
        --priority 1003 \
        --output none
    echo "   ✅ Port 3000 rule added"
else
    echo "   ✅ Port 3000 rule already exists: $PORT_3000_RULE"
fi

echo "   ✅ Ports configured"

echo ""
echo "7️⃣ Getting new VM IP..."
NEW_VM_IP=$(az network public-ip show \
    --resource-group $RESOURCE_GROUP \
    --name "${VM_NAME}-pip" \
    --query "ipAddress" -o tsv)

echo "   ✅ New VM IP: $NEW_VM_IP"

echo ""
echo "8️⃣ Waiting for VM to be fully ready (60 seconds)..."
sleep 60

echo ""
echo "9️⃣ Testing SSH connectivity..."
SSH_SUCCESS=false

# Determine which SSH key to use for testing
SSH_KEY_FOR_TEST=""
if [ -n "$SSH_PRIVATE_KEY" ] && [ -f "$SSH_PRIVATE_KEY" ]; then
    SSH_KEY_FOR_TEST="-i $SSH_PRIVATE_KEY"
elif [ -f ~/.ssh/id_rsa ]; then
    SSH_KEY_FOR_TEST="-i ~/.ssh/id_rsa"
elif [ -f ~/.ssh/id_ed25519 ]; then
    SSH_KEY_FOR_TEST="-i ~/.ssh/id_ed25519"
fi

if [ -n "$SSH_KEY_FOR_TEST" ]; then
    for i in {1..5}; do
        echo "   SSH attempt $i/5..."
        if ssh $SSH_KEY_FOR_TEST \
            -o StrictHostKeyChecking=no \
            -o UserKnownHostsFile=/dev/null \
            -o ConnectTimeout=30 \
            -o BatchMode=yes \
            ${VM_ADMIN_USERNAME}@${NEW_VM_IP} 'echo "SSH_SUCCESS"' 2>/dev/null | grep -q "SSH_SUCCESS"; then
            SSH_SUCCESS=true
            echo "   ✅ SSH connectivity successful!"
            break
        else
            echo "   ⏳ Waiting 15 seconds before retry..."
            sleep 15
        fi
    done
else
    echo "   ⚠️  No SSH private key available for testing"
    echo "   VM created but SSH test skipped"
    echo "   You can test manually: ssh ${VM_ADMIN_USERNAME}@${NEW_VM_IP}"
fi

echo ""
echo "==========================================="
echo "📋 Summary"
echo "==========================================="

if [ "$SSH_SUCCESS" = "true" ]; then
    echo ""
    echo "✅ VM recreation successful!"
    echo ""
    echo "New VM IP: $NEW_VM_IP"
    echo ""
    echo "Next steps:"
    echo "1. SSH into VM:"
    if [ -n "$SSH_KEY_FOR_TEST" ]; then
        echo "   ssh $SSH_KEY_FOR_TEST ${VM_ADMIN_USERNAME}@${NEW_VM_IP}"
    else
        echo "   ssh ${VM_ADMIN_USERNAME}@${NEW_VM_IP}"
        echo "   (Use your SSH private key)"
    fi
    echo ""
    echo "2. Run VM setup:"
    echo "   cd azure-deployment"
    echo "   bash setup-vm.sh"
    echo ""
    echo "3. Deploy application:"
    echo "   bash deploy-application.sh"
    echo ""
    echo "Or let GitHub Actions handle the deployment automatically."
else
    echo ""
    echo "⚠️  VM recreated but SSH test failed"
    echo ""
    echo "New VM IP: $NEW_VM_IP"
    echo ""
    echo "Troubleshooting:"
    echo "1. Wait 2-3 minutes and try SSH manually:"
    if [ -n "$SSH_KEY_FOR_TEST" ]; then
        echo "   ssh $SSH_KEY_FOR_TEST ${VM_ADMIN_USERNAME}@${NEW_VM_IP}"
    else
        echo "   ssh ${VM_ADMIN_USERNAME}@${NEW_VM_IP}"
        echo "   (Use your SSH private key)"
    fi
    echo ""
    echo "2. Check VM extension status:"
    echo "   az vm extension list --resource-group $RESOURCE_GROUP --vm-name $VM_NAME"
    echo ""
    echo "3. Check boot diagnostics:"
    echo "   az vm boot-diagnostics get-boot-log --resource-group $RESOURCE_GROUP --name $VM_NAME"
    echo ""
    echo "4. Use Azure Portal Serial Console if SSH still doesn't work"
fi

