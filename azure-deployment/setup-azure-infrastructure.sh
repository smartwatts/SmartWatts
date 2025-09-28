#!/bin/bash

# SmartWatts Azure Free Tier Infrastructure Setup
# This script creates all necessary Azure resources for SmartWatts deployment

set -e

# Configuration
RESOURCE_GROUP="smartwatts-rg"
LOCATION="eastus"
VM_NAME="smartwatts-vm"
VM_SIZE="Standard_B1s"
SQL_SERVER_NAME="smartwatts-sql-server"
SQL_DB_NAME="smartwatts-db"
SQL_ADMIN_USER="smartwattsadmin"
SQL_ADMIN_PASSWORD="SmartWatts2025!"
IOT_HUB_NAME="smartwatts-iot-hub"
STORAGE_ACCOUNT_NAME="smartwattsstorage$(date +%s)"
VM_USERNAME="azureuser"

echo "🚀 Starting SmartWatts Azure Infrastructure Setup..."

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    echo "❌ Azure CLI is not installed. Please install it first."
    echo "Visit: https://docs.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
fi

# Check if logged in to Azure
if ! az account show &> /dev/null; then
    echo "❌ Not logged in to Azure. Please run 'az login' first."
    exit 1
fi

echo "✅ Azure CLI is ready"

# Create Resource Group
echo "📦 Creating Resource Group..."
az group create \
    --name $RESOURCE_GROUP \
    --location $LOCATION \
    --output table

echo "✅ Resource Group created: $RESOURCE_GROUP"

# Create Storage Account
echo "💾 Creating Storage Account..."
az storage account create \
    --resource-group $RESOURCE_GROUP \
    --name $STORAGE_ACCOUNT_NAME \
    --location $LOCATION \
    --sku Standard_LRS \
    --output table

echo "✅ Storage Account created: $STORAGE_ACCOUNT_NAME"

# Create SQL Server
echo "🗄️ Creating Azure SQL Server..."
az sql server create \
    --resource-group $RESOURCE_GROUP \
    --name $SQL_SERVER_NAME \
    --admin-user $SQL_ADMIN_USER \
    --admin-password $SQL_ADMIN_PASSWORD \
    --location $LOCATION \
    --output table

echo "✅ SQL Server created: $SQL_SERVER_NAME"

# Create SQL Database
echo "🗄️ Creating Azure SQL Database..."
az sql db create \
    --resource-group $RESOURCE_GROUP \
    --server $SQL_SERVER_NAME \
    --name $SQL_DB_NAME \
    --service-objective Basic \
    --max-size 250GB \
    --output table

echo "✅ SQL Database created: $SQL_DB_NAME"

# Configure SQL Server Firewall
echo "🔥 Configuring SQL Server Firewall..."
az sql server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --server $SQL_SERVER_NAME \
    --name "AllowAzureServices" \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0 \
    --output table

echo "✅ SQL Server Firewall configured"

# Create IoT Hub
echo "🌐 Creating Azure IoT Hub..."
az iot hub create \
    --resource-group $RESOURCE_GROUP \
    --name $IOT_HUB_NAME \
    --sku F1 \
    --partition-count 2 \
    --output table

echo "✅ IoT Hub created: $IOT_HUB_NAME"

# Create VM
echo "🖥️ Creating Virtual Machine..."
az vm create \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --image Ubuntu2004 \
    --size $VM_SIZE \
    --admin-username $VM_USERNAME \
    --generate-ssh-keys \
    --public-ip-sku Standard \
    --output table

echo "✅ Virtual Machine created: $VM_NAME"

# Get VM Public IP
echo "🔍 Getting VM Public IP..."
VM_PUBLIC_IP=$(az vm show \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --show-details \
    --query publicIps \
    --output tsv)

echo "✅ VM Public IP: $VM_PUBLIC_IP"

# Create NSG Rules
echo "🔒 Creating Network Security Group Rules..."
az vm open-port \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --port 22 \
    --priority 1000

az vm open-port \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --port 80 \
    --priority 1001

az vm open-port \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --port 443 \
    --priority 1002

az vm open-port \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --port 3000 \
    --priority 1003

echo "✅ Network Security Group Rules created"

# Get SQL Server Connection String
echo "🔗 Getting SQL Server Connection String..."
SQL_CONNECTION_STRING=$(az sql db show-connection-string \
    --server $SQL_SERVER_NAME \
    --name $SQL_DB_NAME \
    --client ado.net \
    --output tsv)

echo "✅ SQL Connection String retrieved"

# Get IoT Hub Connection String
echo "🔗 Getting IoT Hub Connection String..."
IOT_HUB_CONNECTION_STRING=$(az iot hub connection-string show \
    --resource-group $RESOURCE_GROUP \
    --hub-name $IOT_HUB_NAME \
    --output tsv)

echo "✅ IoT Hub Connection String retrieved"

# Get Storage Account Connection String
echo "🔗 Getting Storage Account Connection String..."
STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
    --resource-group $RESOURCE_GROUP \
    --name $STORAGE_ACCOUNT_NAME \
    --output tsv)

echo "✅ Storage Account Connection String retrieved"

# Create configuration file
echo "📝 Creating configuration file..."
cat > azure-config.env << EOF
# Azure Configuration
AZURE_RESOURCE_GROUP=$RESOURCE_GROUP
AZURE_LOCATION=$LOCATION
VM_PUBLIC_IP=$VM_PUBLIC_IP
VM_USERNAME=$VM_USERNAME

# Database Configuration
SQL_SERVER_NAME=$SQL_SERVER_NAME
SQL_DB_NAME=$SQL_DB_NAME
SQL_ADMIN_USER=$SQL_ADMIN_USER
SQL_ADMIN_PASSWORD=$SQL_ADMIN_PASSWORD
SQL_CONNECTION_STRING=$SQL_CONNECTION_STRING

# IoT Hub Configuration
IOT_HUB_NAME=$IOT_HUB_NAME
IOT_HUB_CONNECTION_STRING=$IOT_HUB_CONNECTION_STRING

# Storage Configuration
STORAGE_ACCOUNT_NAME=$STORAGE_ACCOUNT_NAME
STORAGE_CONNECTION_STRING=$STORAGE_CONNECTION_STRING

# Application Configuration
FRONTEND_URL=http://$VM_PUBLIC_IP:3000
API_GATEWAY_URL=http://$VM_PUBLIC_IP:8080
EOF

echo "✅ Configuration file created: azure-config.env"

# Display summary
echo ""
echo "🎉 Azure Infrastructure Setup Complete!"
echo "======================================"
echo "Resource Group: $RESOURCE_GROUP"
echo "VM Name: $VM_NAME"
echo "VM Public IP: $VM_PUBLIC_IP"
echo "SQL Server: $SQL_SERVER_NAME"
echo "SQL Database: $SQL_DB_NAME"
echo "IoT Hub: $IOT_HUB_NAME"
echo "Storage Account: $STORAGE_ACCOUNT_NAME"
echo ""
echo "Next Steps:"
echo "1. SSH into VM: ssh $VM_USERNAME@$VM_PUBLIC_IP"
echo "2. Run VM setup script: ./setup-vm.sh"
echo "3. Deploy application: ./deploy-application.sh"
echo ""
echo "Configuration saved to: azure-config.env"
