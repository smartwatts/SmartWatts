# SmartWatts Azure Deployment Guide

## 🎯 Overview

This guide provides step-by-step instructions for deploying SmartWatts to Azure Free Tier using GitHub Actions, achieving **$0/month cost** with full functionality.

**Architecture**: Hybrid approach combining Spring Boot microservices on Azure VM with Azure cloud services (IoT Hub, Blob Storage, Static Web Apps, Application Insights).

---

## 📋 Prerequisites

### Required
1. **Azure Account** with free tier available (12-month free tier)
2. **GitHub Account** with repository access
3. **Azure CLI** installed locally (for initial setup)
4. **Docker** installed locally (for testing)

### Recommended
- Basic knowledge of Azure services
- Familiarity with GitHub Actions
- Understanding of Docker and Docker Compose

---

## 🚀 Quick Start

### Step 1: Azure Account Setup

1. **Create Azure Account** (if you don't have one)
   - Go to [Azure Free Account](https://azure.microsoft.com/free/)
   - Sign up with your email
   - Verify your identity

2. **Install Azure CLI**
   ```bash
   # macOS
   brew install azure-cli
   
   # Linux
   curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
   
   # Windows
   # Download from https://aka.ms/installazurecliwindows
   ```

3. **Login to Azure**
   ```bash
   az login
   az account set --subscription <your-subscription-id>
   ```

### Step 2: Create Azure Service Principals

Create service principals for GitHub Actions to authenticate with Azure:

```bash
# Get your subscription ID
SUBSCRIPTION_ID=$(az account show --query id -o tsv)
echo "Subscription ID: $SUBSCRIPTION_ID"

# Create staging service principal
az ad sp create-for-rbac \
  --name "smartwatts-staging-sp" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID \
  --sdk-auth \
  --output json > staging-sp.json

# Create production service principal
az ad sp create-for-rbac \
  --name "smartwatts-prod-sp" \
  --role contributor \
  --scopes /subscriptions/$SUBSCRIPTION_ID \
  --sdk-auth \
  --output json > prod-sp.json

# Display service principal info (save these!)
echo "=== Staging Service Principal ==="
cat staging-sp.json
echo ""
echo "=== Production Service Principal ==="
cat prod-sp.json
```

**Important**: Save the JSON output from both service principals. You'll need them for GitHub Secrets.

### Step 3: Generate SSH Keys

Generate SSH key pairs for VM access:

```bash
# Generate staging SSH key
ssh-keygen -t rsa -b 4096 -f ~/.ssh/smartwatts-staging -N ""

# Generate production SSH key
ssh-keygen -t rsa -b 4096 -f ~/.ssh/smartwatts-prod -N ""

# Display private keys (save these!)
echo "=== Staging SSH Private Key ==="
cat ~/.ssh/smartwatts-staging
echo ""
echo "=== Production SSH Private Key ==="
cat ~/.ssh/smartwatts-prod
```

**Important**: Save both private keys. You'll need them for GitHub Secrets.

### Step 4: Configure GitHub Secrets

After completing Azure Infrastructure Setup (above), configure the following secrets in your GitHub repository:

**GitHub → Settings → Secrets and variables → Actions → New repository secret**

#### Required Secrets for Staging (`main` branch)

1. **`AZURE_CREDENTIALS_STAGING`**
   - Copy the entire JSON content from `staging-sp.json` file created above
   - Paste as the secret value

2. **`VM_ADMIN_PASSWORD_STAGING`**
   - Use the same password you used when deploying infrastructure
   - Or create a new strong password (minimum 12 characters)

3. **`VM_SSH_PRIVATE_KEY_STAGING`**
   - Copy the entire private key from `~/.ssh/smartwatts-staging`
   - Include `-----BEGIN RSA PRIVATE KEY-----` and `-----END RSA PRIVATE KEY-----` lines

4. **`AZURE_STATIC_WEB_APPS_API_TOKEN_STAGING`**
   - Get from Step 4 in Azure Infrastructure Setup
   - Or: Azure Portal → Static Web Apps → sw-staging-dashboard → Manage deployment token

#### Required Secrets for Production (`prod` branch)

1. **`AZURE_CREDENTIALS_PROD`**
   - Copy the entire JSON content from `prod-sp.json` file created above

2. **`VM_ADMIN_PASSWORD_PROD`**
   - Use the same password you used when deploying infrastructure

3. **`VM_SSH_PRIVATE_KEY_PROD`**
   - Copy the entire private key from `~/.ssh/smartwatts-prod`

4. **`AZURE_STATIC_WEB_APPS_API_TOKEN_PROD`**
   - Get from Step 4 in Azure Infrastructure Setup
   - Or: Azure Portal → Static Web Apps → sw-prod-dashboard → Manage deployment token

---

## 📦 Azure Infrastructure Setup

### Step 1: Create Resource Groups

Create resource groups for staging and production environments:

```bash
# Create staging resource group
az group create \
  --name sw-staging-rg \
  --location westeurope

# Create production resource group
az group create \
  --name sw-prod-rg \
  --location westeurope

# Verify resource groups
az group list --query "[?name=='sw-staging-rg' || name=='sw-prod-rg']" --output table
```

### Step 2: Deploy Infrastructure via Bicep

#### Deploy Staging Environment

1. **Deploy Infrastructure**
   ```bash
   # Navigate to project root
   cd /path/to/mySmartWatts
   
   # Deploy staging infrastructure
   az deployment group create \
     --resource-group sw-staging-rg \
     --template-file infrastructure/bicep/main.bicep \
     --parameters @infrastructure/bicep/params.staging.json \
     --parameters vmAdminPassword="<your-strong-password-here>"
   ```

2. **Wait for Deployment** (takes 10-15 minutes)
   - Monitor progress in terminal
   - Or check Azure Portal → Resource Groups → sw-staging-rg → Deployments

3. **Get Deployment Outputs**
   ```bash
   # Get all outputs
   az deployment group show \
     --resource-group sw-staging-rg \
     --name main \
     --query properties.outputs \
     --output json
   
   # Get specific outputs
   VM_IP=$(az deployment group show \
     --resource-group sw-staging-rg \
     --name main \
     --query properties.outputs.vmPublicIpAddress.value -o tsv)
   
   IOT_HUB_CS=$(az deployment group show \
     --resource-group sw-staging-rg \
     --name main \
     --query properties.outputs.iotHubConnectionString.value -o tsv)
   
   STORAGE_CS=$(az deployment group show \
     --resource-group sw-staging-rg \
     --name main \
     --query properties.outputs.storageAccountConnectionString.value -o tsv)
   
   STATIC_WEB_URL=$(az deployment group show \
     --resource-group sw-staging-rg \
     --name main \
     --query properties.outputs.staticWebAppUrl.value -o tsv)
   
   APP_INSIGHTS_CS=$(az deployment group show \
     --resource-group sw-staging-rg \
     --name main \
     --query properties.outputs.appInsightsConnectionString.value -o tsv)
   
   echo "VM IP: $VM_IP"
   echo "IoT Hub Connection String: $IOT_HUB_CS"
   echo "Storage Connection String: $STORAGE_CS"
   echo "Static Web App URL: $STATIC_WEB_URL"
   echo "App Insights Connection String: $APP_INSIGHTS_CS"
   ```

#### Deploy Production Environment

1. **Deploy Infrastructure**
   ```bash
   # Deploy production infrastructure
   az deployment group create \
     --resource-group sw-prod-rg \
     --template-file infrastructure/bicep/main.bicep \
     --parameters @infrastructure/bicep/params.prod.json \
     --parameters vmAdminPassword="<your-strong-password-here>"
   ```

2. **Get Production Outputs**
   ```bash
   # Get production outputs
   az deployment group show \
     --resource-group sw-prod-rg \
     --name main \
     --query properties.outputs \
     --output json
   ```

### Step 3: Verify Azure Resources

1. **Check All Resources**
   ```bash
   # List all resources in staging
   az resource list \
     --resource-group sw-staging-rg \
     --output table
   
   # List all resources in production
   az resource list \
     --resource-group sw-prod-rg \
     --output table
   ```

2. **Verify Specific Resources**
   ```bash
   # Check VM
   az vm show \
     --resource-group sw-staging-rg \
     --name sw-staging-vm \
     --query "{Name:name, Status:powerState, IP:publicIps}" \
     --output table
   
   # Check IoT Hub
   az iot hub show \
     --resource-group sw-staging-rg \
     --name sw-staging-iothub \
     --query "{Name:name, Status:properties.state}" \
     --output table
   
   # Check Storage Account
   az storage account show \
     --resource-group sw-staging-rg \
     --name swstagingstorage \
     --query "{Name:name, Status:provisioningState}" \
     --output table
   
   # Check Static Web App
   az staticwebapp show \
     --resource-group sw-staging-rg \
     --name sw-staging-dashboard \
     --query "{Name:name, URL:defaultHostname}" \
     --output table
   
   # Check Application Insights
   az monitor app-insights component show \
     --resource-group sw-staging-rg \
     --app sw-staging-insights \
     --query "{Name:name, Status:provisioningState}" \
     --output table
   ```

### Step 4: Get Connection Strings and Secrets

#### IoT Hub Connection String

```bash
# Get IoT Hub connection string
az iot hub connection-string show \
  --hub-name sw-staging-iothub \
  --resource-group sw-staging-rg \
  --policy-name iothubowner \
  --output tsv

# Create device identity for edge gateway
az iot hub device-identity create \
  --hub-name sw-staging-iothub \
  --device-id edge-gateway-001 \
  --resource-group sw-staging-rg

# Get device connection string
az iot hub device-identity connection-string show \
  --hub-name sw-staging-iothub \
  --device-id edge-gateway-001 \
  --resource-group sw-staging-rg \
  --output tsv
```

#### Storage Account Connection String

```bash
# Get storage account connection string
az storage account show-connection-string \
  --resource-group sw-staging-rg \
  --name swstagingstorage \
  --output tsv

# Verify blob containers exist
az storage container list \
  --account-name swstagingstorage \
  --connection-string "$STORAGE_CS" \
  --output table
```

#### Static Web App Deployment Token

```bash
# Get Static Web App deployment token
az staticwebapp secrets list \
  --name sw-staging-dashboard \
  --resource-group sw-staging-rg \
  --query "properties.apiKey" \
  --output tsv
```

#### Application Insights Connection String

```bash
# Get Application Insights connection string
az monitor app-insights component show \
  --resource-group sw-staging-rg \
  --app sw-staging-insights \
  --query "connectionString" \
  --output tsv
```

### Step 5: Save Configuration for GitHub Secrets

Create a file to store your secrets (keep this secure, don't commit to git):

```bash
# Create secrets file (DO NOT COMMIT THIS FILE)
cat > azure-secrets.txt << EOF
# Staging Environment Secrets
AZURE_CREDENTIALS_STAGING=<service-principal-json>
VM_ADMIN_PASSWORD_STAGING=<your-vm-password>
VM_SSH_PRIVATE_KEY_STAGING=<your-ssh-private-key>
AZURE_STATIC_WEB_APPS_API_TOKEN_STAGING=<static-web-app-token>
IOT_HUB_CONNECTION_STRING_STAGING=<iot-hub-connection-string>
STORAGE_CONNECTION_STRING_STAGING=<storage-connection-string>
APP_INSIGHTS_CONNECTION_STRING_STAGING=<app-insights-connection-string>

# Production Environment Secrets
AZURE_CREDENTIALS_PROD=<service-principal-json>
VM_ADMIN_PASSWORD_PROD=<your-vm-password>
VM_SSH_PRIVATE_KEY_PROD=<your-ssh-private-key>
AZURE_STATIC_WEB_APPS_API_TOKEN_PROD=<static-web-app-token>
IOT_HUB_CONNECTION_STRING_PROD=<iot-hub-connection-string>
STORAGE_CONNECTION_STRING_PROD=<storage-connection-string>
APP_INSIGHTS_CONNECTION_STRING_PROD=<app-insights-connection-string>
EOF

# Secure the file
chmod 600 azure-secrets.txt
```

---

## 📦 Infrastructure Deployment via GitHub Actions

### Option 1: Deploy via GitHub Actions (Recommended)

**Prerequisites**: Complete Azure Infrastructure Setup (above) and configure GitHub Secrets (see Step 2 in Quick Start)

1. **Push to `main` branch** (staging deployment)
   ```bash
   git checkout main
   git push origin main
   ```

2. **Monitor deployment**
   - Go to GitHub → Actions tab
   - Watch the "Deploy Staging (main branch)" workflow
   - Wait for completion (typically 10-15 minutes)

3. **Push to `prod` branch** (production deployment)
   ```bash
   git checkout prod
   git push origin prod
   ```

### Option 2: Manual Deployment (Alternative)

If you prefer to deploy manually without GitHub Actions, follow the Application Deployment section below.

---

## 🔧 Application Deployment

### Deploy to Azure VM

1. **SSH into VM**
   ```bash
   # Get VM IP from deployment outputs
   VM_IP=$(az vm show -d --resource-group sw-staging-rg \
     --name sw-staging-vm --query publicIps -o tsv)
   
   ssh azureuser@$VM_IP
   ```

2. **Install Docker and Docker Compose**
   ```bash
   # Install Docker
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker $USER
   
   # Install Docker Compose
   sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
     -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   ```

3. **Clone Repository**
   ```bash
   git clone https://github.com/<your-org>/mySmartWatts.git
   cd mySmartWatts
   ```

4. **Configure Environment Variables**
   ```bash
   cd azure-deployment
   
   # Get connection strings from Azure (if not already saved)
   IOT_HUB_CS=$(az iot hub device-identity connection-string show \
     --hub-name sw-staging-iothub \
     --device-id edge-gateway-001 \
     --resource-group sw-staging-rg \
     --output tsv)
   
   STORAGE_CS=$(az storage account show-connection-string \
     --resource-group sw-staging-rg \
     --name swstagingstorage \
     --output tsv)
   
   # Create .env file
   cat > .env << EOF
   POSTGRES_PASSWORD=<your-postgres-password>
   IOT_HUB_CONNECTION_STRING=$IOT_HUB_CS
   STORAGE_CONNECTION_STRING=$STORAGE_CS
   NEXT_PUBLIC_API_URL=http://$VM_IP:8080
   EOF
   
   # Secure the .env file
   chmod 600 .env
   ```

5. **Deploy Application**
   ```bash
   docker-compose -f docker-compose.azure.yml up -d --build
   ```

6. **Verify Deployment**
   ```bash
   # Check services
   docker-compose -f docker-compose.azure.yml ps
   
   # Check logs
   docker-compose -f docker-compose.azure.yml logs -f
   ```

---

## 🌐 Frontend Deployment (Azure Static Web Apps)

### Automatic Deployment (via GitHub Actions)

The frontend is automatically deployed to Azure Static Web Apps when you push to `main` or `prod` branches.

### Manual Deployment

1. **Get Static Web App Deployment Token**
   ```bash
   az staticwebapp secrets list \
     --name sw-staging-dashboard \
     --resource-group sw-staging-rg
   ```

2. **Deploy Frontend**
   ```bash
   cd frontend
   npm install
   npm run build
   
   # Use Azure Static Web Apps CLI
   npm install -g @azure/static-web-apps-cli
   swa deploy .next --deployment-token <token>
   ```

---

## 🔌 Edge Gateway IoT Hub Integration

### Configure Edge Gateway

1. **Create Device Identity (if not already created)**
   ```bash
   # Create device identity for edge gateway
   az iot hub device-identity create \
     --hub-name sw-staging-iothub \
     --device-id edge-gateway-001 \
     --resource-group sw-staging-rg
   ```

2. **Get IoT Hub Connection String**
   ```bash
   az iot hub device-identity connection-string show \
     --hub-name sw-staging-iothub \
     --device-id edge-gateway-001 \
     --resource-group sw-staging-rg \
     --output tsv
   ```

3. **Update Edge Gateway Configuration**
   ```yaml
   # edge-gateway/config/edge-config.yml
   network:
     azure_iot_hub_connection_string: "HostName=xxx.azure-devices.net;DeviceId=xxx;SharedAccessKey=xxx"
   
   data_sync:
     use_azure_iot_hub: true
     azure_iot_hub_device_id: "edge-gateway-001"
   ```

4. **Install Azure IoT Hub SDK**
   ```bash
   cd edge-gateway
   pip install -r requirements.txt
   ```

5. **Test IoT Hub Connection**
   ```bash
   python -c "
   from azure.iot.device import IoTHubDeviceClient
   client = IoTHubDeviceClient.create_from_connection_string('<connection-string>')
   client.connect()
   print('Connected to IoT Hub')
   client.disconnect()
   "
   ```

---

## 💾 Blob Storage Configuration

### Database Backups

1. **Get Storage Connection String**
   ```bash
   # Get storage connection string
   export AZURE_STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
     --resource-group sw-staging-rg \
     --name swstagingstorage \
     --output tsv)
   
   export AZURE_BLOB_CONTAINER="backups"
   
   # Verify connection
   echo "Storage Connection String: $AZURE_STORAGE_CONNECTION_STRING"
   ```

2. **Run Backup**
   ```bash
   ./scripts/backup-database.sh
   ./scripts/backup-to-blob-storage.sh
   ```

3. **Schedule Automated Backups**
   ```bash
   # Add to crontab
   crontab -e
   
   # Daily backup at 2 AM
   0 2 * * * /path/to/scripts/backup-database.sh
   0 3 * * * /path/to/scripts/backup-to-blob-storage.sh
   ```

### Log Archival

1. **Get Storage Connection String**
   ```bash
   # Get storage connection string
   export AZURE_STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
     --resource-group sw-staging-rg \
     --name swstagingstorage \
     --output tsv)
   
   export AZURE_BLOB_LOG_CONTAINER="logs"
   export LOG_RETENTION_DAYS=90
   
   # Verify connection
   echo "Storage Connection String: $AZURE_STORAGE_CONNECTION_STRING"
   ```

2. **Run Log Archival**
   ```bash
   ./scripts/archive-logs-to-blob.sh
   ```

3. **Schedule Automated Archival**
   ```bash
   # Weekly log archival
   0 4 * * 0 /path/to/scripts/archive-logs-to-blob.sh
   ```

---

## ✅ Testing Procedures

### Health Checks

1. **API Gateway Health**
   ```bash
   curl http://<VM_IP>:8080/actuator/health
   ```

2. **Service Discovery**
   ```bash
   curl http://<VM_IP>:8761/eureka/apps
   ```

3. **Frontend**
   ```bash
   curl https://<static-web-app-url>
   ```

### End-to-End Testing

1. **Test User Registration**
   ```bash
   curl -X POST http://<VM_IP>:8080/api/v1/users/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"Test123!"}'
   ```

2. **Test Device Registration**
   ```bash
   curl -X POST http://<VM_IP>:8080/api/v1/devices \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"name":"Test Device","type":"SMART_METER"}'
   ```

3. **Test IoT Hub Message**
   ```bash
   # Send test message from edge gateway
   python edge-gateway/test_iot_hub.py
   ```

### Performance Testing

1. **Load Test**
   ```bash
   # Use JMeter or similar
   jmeter -n -t load-testing/smartwatts-load-test.jmx \
     -l results.jtl \
     -H <VM_IP> \
     -P 8080
   ```

2. **Monitor Resources**
   ```bash
   # Check VM metrics
   az vm show -d --resource-group sw-staging-rg \
     --name sw-staging-vm \
     --query "{CPU:osProfile,Memory:hardwareProfile}"
   ```

---

## 🔍 Monitoring & Troubleshooting

### Application Insights

1. **View Logs**
   - Go to Azure Portal → Application Insights → Logs
   - Query: `traces | where message contains "error"`

2. **View Metrics**
   - Go to Azure Portal → Application Insights → Metrics
   - Monitor: Request rate, Response time, Error rate

### VM Monitoring

1. **Check VM Status**
   ```bash
   az vm show -d --resource-group sw-staging-rg \
     --name sw-staging-vm
   ```

2. **View VM Metrics**
   ```bash
   az monitor metrics list \
     --resource /subscriptions/<sub-id>/resourceGroups/sw-staging-rg/providers/Microsoft.Compute/virtualMachines/sw-staging-vm \
     --metric "Percentage CPU"
   ```

### Log Access

1. **Application Logs**
   ```bash
   # SSH into VM
   ssh azureuser@<VM_IP>
   
   # View Docker logs
   docker-compose -f azure-deployment/docker-compose.azure.yml logs -f
   ```

2. **System Logs**
   ```bash
   # View system logs
   journalctl -u docker -f
   ```

### Common Issues

1. **Services Not Starting**
   - Check Docker logs: `docker-compose logs <service-name>`
   - Verify environment variables
   - Check PostgreSQL connection

2. **IoT Hub Connection Failed**
   - Verify connection string
   - Check device registration in IoT Hub
   - Verify network connectivity

3. **Blob Storage Upload Failed**
   - Verify connection string
   - Check container exists
   - Verify Azure CLI authentication

---

## 📊 Cost Monitoring

### Free Tier Limits

- **VM**: 750 hours/month (24/7 = 744 hours) ✅
- **IoT Hub**: 8,000 messages/day ✅
- **Blob Storage**: 5 GB ✅
- **Static Web Apps**: 100 GB bandwidth/month ✅
- **Application Insights**: 5 GB data ingestion/month ✅

### Monitor Costs

1. **Azure Cost Management**
   - Go to Azure Portal → Cost Management + Billing
   - Set up cost alerts
   - Monitor daily spending

2. **Check Usage**
   ```bash
   # Check VM usage
   az consumption usage list \
     --start-date $(date -d "1 month ago" +%Y-%m-%d) \
     --end-date $(date +%Y-%m-%d)
   ```

---

## 🔐 Security Best Practices

1. **Use Strong Passwords**
   - VM admin password: minimum 12 characters
   - PostgreSQL password: minimum 16 characters

2. **Enable Firewall Rules**
   - Restrict SSH access to specific IPs
   - Use Network Security Groups

3. **Rotate Secrets Regularly**
   - Update GitHub secrets quarterly
   - Rotate IoT Hub connection strings

4. **Enable Monitoring**
   - Set up Azure Security Center alerts
   - Monitor for suspicious activity

---

## 📚 Additional Resources

### Azure Documentation
- [Azure Free Tier](https://azure.microsoft.com/free/)
- [Azure IoT Hub](https://docs.microsoft.com/azure/iot-hub/)
- [Azure Blob Storage](https://docs.microsoft.com/azure/storage/blobs/)
- [Azure Static Web Apps](https://docs.microsoft.com/azure/static-web-apps/)
- [Application Insights](https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview)

### SmartWatts Documentation
- [Deployment Plan](AZURE_DEPLOYMENT_PLAN.md)
- [Hybrid Architecture](AZURE_HYBRID_ARCHITECTURE.md)
- [Scaling Guide](AZURE_SCALING_GUIDE.md)

---

## 🎉 Success Criteria

### Must Have ✅
- [x] All Azure resources deployed successfully
- [x] Spring Boot services running on VM
- [x] PostgreSQL container with 9 databases
- [x] Frontend deployed to Static Web Apps
- [x] IoT Hub integration working
- [x] Blob Storage backups configured
- [x] Application Insights monitoring active

### Should Have ✅
- [x] Automated backups scheduled
- [x] Log archival configured
- [x] Health checks passing
- [x] End-to-end testing successful

---

## 🚀 Next Steps

1. **Test Production Environment**
   - Run full test suite
   - Verify all features work
   - Monitor performance

2. **Configure Monitoring**
   - Set up Application Insights dashboards
   - Configure alerts
   - Monitor costs

3. **Scale as Needed**
   - Monitor resource usage
   - Upgrade VM size if needed
   - Scale services horizontally

---

**SmartWatts Azure Deployment - Ready for Production!** 🎯

**Total Estimated Cost: $0/month (Azure Free Tier)** 💰

