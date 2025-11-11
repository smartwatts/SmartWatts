# Deployment Troubleshooting Guide

## Quick Diagnosis

If you can't access your application, run the diagnostic script:

```bash
./scripts/diagnose-deployment.sh [VM_IP] [RESOURCE_GROUP] [WEB_APP_NAME]
```

Example:
```bash
./scripts/diagnose-deployment.sh 52.182.155.148 sw-staging-rg SmartWatts
```

## Common Issues

### 1. VM Services Not Running

**Symptoms:**
- `http://52.182.155.148:3000` returns connection refused
- `http://52.182.155.148` returns connection refused

**Diagnosis:**
```bash
# SSH into the VM
ssh azureuser@52.182.155.148

# Check if Docker is running
sudo systemctl status docker

# Check running containers
cd /home/azureuser/SmartWatts/azure-deployment
# or
cd /home/azureuser/smartwatts/azure-deployment

docker-compose ps
# or
docker compose ps
```

**Solution:**
```bash
# Start Docker if not running
sudo systemctl start docker
sudo systemctl enable docker

# Start all services
cd azure-deployment
export POSTGRES_PASSWORD="SmartWatts2024!"
docker-compose -f docker-compose.azure.yml up -d --build

# Check logs
docker-compose logs frontend
docker-compose logs api-gateway
```

### 2. NSG Rules Blocking Traffic

**Symptoms:**
- Port 3000 not accessible from outside
- Port 80 not accessible from outside

**Diagnosis:**
```bash
# Check NSG rules
az network nsg list --resource-group sw-staging-rg --query "[].name" -o tsv
az network nsg rule list --nsg-name <NSG_NAME> --resource-group sw-staging-rg --query "[?destinationPortRange=='3000' || destinationPortRanges[?contains(@, '3000')]]" -o table
```

**Solution:**
```bash
# Add port 3000 rule
az network nsg rule create \
  --resource-group sw-staging-rg \
  --nsg-name <NSG_NAME> \
  --name "AllowPort3000" \
  --priority 1003 \
  --access Allow \
  --protocol Tcp \
  --direction Inbound \
  --destination-port-ranges 3000

# Add port 80 rule (if missing)
az network nsg rule create \
  --resource-group sw-staging-rg \
  --nsg-name <NSG_NAME> \
  --name "AllowPort80" \
  --priority 1001 \
  --access Allow \
  --protocol Tcp \
  --direction Inbound \
  --destination-port-ranges 80
```

### 3. Nginx Not Running or Misconfigured

**Symptoms:**
- `http://52.182.155.148:3000` works but `http://52.182.155.148` doesn't

**Diagnosis:**
```bash
ssh azureuser@52.182.155.148
sudo systemctl status nginx
sudo nginx -t
```

**Solution:**
```bash
# Start Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# Check Nginx configuration
sudo cat /etc/nginx/sites-available/smartwatts

# Restart Nginx
sudo systemctl restart nginx
```

### 4. Frontend Container Not Starting

**Symptoms:**
- Port 3000 not responding
- Container exits immediately

**Diagnosis:**
```bash
ssh azureuser@52.182.155.148
cd azure-deployment
docker-compose logs frontend
docker ps -a | grep frontend
```

**Solution:**
```bash
# Rebuild and restart frontend
docker-compose stop frontend
docker-compose rm -f frontend
docker-compose build frontend
docker-compose up -d frontend

# Check logs
docker-compose logs -f frontend
```

### 5. Azure Web App Not Accessible

**Symptoms:**
- `https://smartwatts-hsfzfkcbgje7hham.centralus-01.azurewebsites.net/` returns error or blank page

**Diagnosis:**
```bash
# Check web app status
az webapp show --resource-group sw-staging-rg --name SmartWatts --query "{state:state,defaultHostName:defaultHostName}" -o table

# Check deployment status
az webapp deployment list --resource-group sw-staging-rg --name SmartWatts --query "[0].{status:status,message:message}" -o table
```

**Solution:**
```bash
# Start web app if stopped
az webapp start --resource-group sw-staging-rg --name SmartWatts

# Check logs
az webapp log tail --resource-group sw-staging-rg --name SmartWatts

# Restart web app
az webapp restart --resource-group sw-staging-rg --name SmartWatts
```

### 6. Services Starting But Not Ready

**Symptoms:**
- Containers are running but not responding
- Health checks failing

**Diagnosis:**
```bash
ssh azureuser@52.182.155.148
cd azure-deployment

# Check all container statuses
docker-compose ps

# Check specific service logs
docker-compose logs postgres
docker-compose logs redis
docker-compose logs service-discovery
docker-compose logs api-gateway
docker-compose logs frontend
```

**Solution:**
```bash
# Wait for services to be ready (they may need time to start)
# Check service discovery
curl http://localhost:8761/eureka/apps

# Check API Gateway
curl http://localhost:8080/actuator/health

# Check Frontend
curl http://localhost:3000
```

## Manual Deployment Steps

If automatic deployment failed, you can deploy manually:

```bash
# 1. SSH into VM
ssh azureuser@52.182.155.148

# 2. Navigate to repository
cd /home/azureuser/SmartWatts
# or
cd /home/azureuser/smartwatts

# 3. Navigate to deployment directory
cd azure-deployment

# 4. Set environment variables
export POSTGRES_PASSWORD="SmartWatts2024!"

# 5. Start services
docker-compose -f docker-compose.azure.yml up -d --build

# 6. Check status
docker-compose ps

# 7. Check logs
docker-compose logs -f
```

## Port Configuration

Expected ports:
- **22**: SSH (always open)
- **80**: HTTP (Nginx proxy to frontend)
- **443**: HTTPS (if SSL configured)
- **3000**: Frontend (Next.js)
- **8080**: API Gateway
- **8081-8092**: Backend services
- **8761**: Service Discovery (Eureka)

## Network Security Group Rules

Required NSG rules:
```bash
# Port 22 (SSH)
az vm open-port --resource-group sw-staging-rg --name sw-staging-vm --port 22 --priority 1000

# Port 80 (HTTP)
az vm open-port --resource-group sw-staging-rg --name sw-staging-vm --port 80 --priority 1001

# Port 443 (HTTPS)
az vm open-port --resource-group sw-staging-rg --name sw-staging-vm --port 443 --priority 1002

# Port 3000 (Frontend)
az vm open-port --resource-group sw-staging-rg --name sw-staging-vm --port 3000 --priority 1003
```

## Checking Service Health

### From Local Machine
```bash
# Frontend
curl -I http://52.182.155.148:3000
curl -I http://52.182.155.148

# Backend API
curl http://52.182.155.148:8080/api/v1/health

# Service Discovery
curl http://52.182.155.148:8761/eureka/apps
```

### From VM
```bash
ssh azureuser@52.182.155.148

# Check all services
curl http://localhost:3000
curl http://localhost:8080/actuator/health
curl http://localhost:8761/eureka/apps
```

## Useful Commands

### Docker Commands
```bash
# List all containers
docker ps -a

# View logs
docker-compose logs -f [service-name]

# Restart a service
docker-compose restart [service-name]

# Rebuild and restart
docker-compose up -d --build [service-name]

# Stop all services
docker-compose down

# Remove all containers and volumes
docker-compose down -v
```

### Azure CLI Commands
```bash
# Check VM status
az vm show -d --resource-group sw-staging-rg --name sw-staging-vm --query powerState -o tsv

# Start VM
az vm start --resource-group sw-staging-rg --name sw-staging-vm

# Check NSG rules
az network nsg rule list --nsg-name <NSG_NAME> --resource-group sw-staging-rg -o table

# Check Web App status
az webapp show --resource-group sw-staging-rg --name SmartWatts --query state -o tsv
```

## Getting Help

If you're still having issues:

1. **Check GitHub Actions logs**: Look at the deployment workflow logs for errors
2. **Check VM logs**: SSH into the VM and check Docker logs
3. **Check Azure Portal**: Verify resources are running and configured correctly
4. **Run diagnostic script**: `./scripts/diagnose-deployment.sh`

## Expected URLs

After successful deployment, you should be able to access:

- **VM Frontend (direct)**: `http://52.182.155.148:3000`
- **VM Frontend (via Nginx)**: `http://52.182.155.148`
- **VM Backend API**: `http://52.182.155.148:8080`
- **Azure Web App**: `https://smartwatts-hsfzfkcbgje7hham.centralus-01.azurewebsites.net`

