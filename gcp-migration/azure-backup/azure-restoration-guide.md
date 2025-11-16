# Azure Restoration Guide

**Document Version**: 1.0  
**Date**: November 2025  
**Purpose**: Step-by-step guide to restore SmartWatts Azure infrastructure from backups

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Restoration Scenarios](#restoration-scenarios)
4. [Infrastructure Restoration](#infrastructure-restoration)
5. [Database Restoration](#database-restoration)
6. [Configuration Restoration](#configuration-restoration)
7. [Application Deployment](#application-deployment)
8. [Verification Steps](#verification-steps)
9. [Rollback Procedures](#rollback-procedures)
10. [Troubleshooting](#troubleshooting)

---

## Overview

This guide provides comprehensive procedures to restore the SmartWatts Azure infrastructure from backups created during the GCP migration process. Use this guide if you need to:

- Rollback from GCP migration
- Restore Azure environment after accidental deletion
- Recreate Azure infrastructure in a new subscription
- Recover from disaster scenarios

### Restoration Time Estimates

| Component | Estimated Time |
|-----------|----------------|
| Resource Group & Infrastructure | 15-20 minutes |
| Database Restoration | 30-60 minutes (depending on data size) |
| Configuration Restoration | 10-15 minutes |
| Application Deployment | 10-15 minutes |
| **Total** | **65-110 minutes** |

---

## Prerequisites

### Required Tools

- Azure CLI (latest version)
- PostgreSQL client tools (`psql`, `pg_restore`)
- SSH client
- jq (for JSON processing)
- Access to backup files from `gcp-migration/azure-backup/`

### Required Access

- Azure subscription with appropriate permissions
- Contributor role on target resource group
- SSH access to VM (if restoring to existing VM)
- Access to backup storage location

### Required Information

Before starting, ensure you have:

- [ ] Azure subscription ID
- [ ] Resource group name (or create new)
- [ ] Region for deployment (e.g., westeurope)
- [ ] VM admin password
- [ ] PostgreSQL password
- [ ] Backup files location
- [ ] Connection strings from backup

---

## Restoration Scenarios

### Scenario 1: Complete Infrastructure Restoration

**Use Case**: Restore entire Azure infrastructure from scratch

**Steps**:
1. Restore infrastructure using ARM/Bicep templates
2. Restore databases
3. Restore configurations
4. Deploy applications

**Estimated Time**: 90-110 minutes

### Scenario 2: Database-Only Restoration

**Use Case**: Restore databases to existing infrastructure

**Steps**:
1. Restore databases from backup files
2. Verify database integrity
3. Update application configurations if needed

**Estimated Time**: 30-60 minutes

### Scenario 3: Configuration-Only Restoration

**Use Case**: Restore application configurations and secrets

**Steps**:
1. Restore Key Vault secrets
2. Restore application.yml files
3. Restore environment variables
4. Restart services

**Estimated Time**: 15-20 minutes

### Scenario 4: Partial Rollback from GCP

**Use Case**: Rollback specific services from GCP to Azure

**Steps**:
1. Restore Azure infrastructure (if needed)
2. Restore databases
3. Deploy specific services to Azure
4. Configure traffic routing

**Estimated Time**: 60-90 minutes

---

## Infrastructure Restoration

### Step 1: Prepare Restoration Environment

```bash
# Set environment variables
export ENVIRONMENT="staging"  # or "production"
export RESOURCE_GROUP="sw-${ENVIRONMENT}-rg"
export LOCATION="westeurope"
export SUBSCRIPTION_ID="<your-subscription-id>"

# Login to Azure
az login
az account set --subscription "${SUBSCRIPTION_ID}"

# Verify subscription
az account show
```

### Step 2: Create Resource Group

```bash
# Create resource group if it doesn't exist
az group create \
  --name "${RESOURCE_GROUP}" \
  --location "${LOCATION}"

# Verify resource group
az group show --name "${RESOURCE_GROUP}"
```

### Step 3: Restore Infrastructure from Bicep Templates

```bash
# Navigate to backup directory
cd gcp-migration/azure-backup/exports/${ENVIRONMENT}/<timestamp>/bicep

# Review Bicep template
cat main.bicep

# Review parameters
cat params.${ENVIRONMENT}.json

# Deploy infrastructure
az deployment group create \
  --resource-group "${RESOURCE_GROUP}" \
  --template-file main.bicep \
  --parameters @params.${ENVIRONMENT}.json \
  --parameters vmAdminPassword="<your-strong-password>"

# Wait for deployment (15-20 minutes)
# Monitor progress
az deployment group show \
  --resource-group "${RESOURCE_GROUP}" \
  --name main \
  --query properties.provisioningState
```

### Step 4: Get Deployment Outputs

```bash
# Get all outputs
az deployment group show \
  --resource-group "${RESOURCE_GROUP}" \
  --name main \
  --query properties.outputs \
  --output json > deployment-outputs.json

# Extract specific values
VM_IP=$(az deployment group show \
  --resource-group "${RESOURCE_GROUP}" \
  --name main \
  --query properties.outputs.vmPublicIpAddress.value -o tsv)

IOT_HUB_CS=$(az deployment group show \
  --resource-group "${RESOURCE_GROUP}" \
  --name main \
  --query properties.outputs.iotHubConnectionString.value -o tsv)

STORAGE_CS=$(az deployment group show \
  --resource-group "${RESOURCE_GROUP}" \
  --name main \
  --query properties.outputs.storageAccountConnectionString.value -o tsv)

echo "VM IP: ${VM_IP}"
echo "IoT Hub Connection String: ${IOT_HUB_CS}"
echo "Storage Connection String: ${STORAGE_CS}"
```

### Step 5: Verify Infrastructure

```bash
# List all resources
az resource list \
  --resource-group "${RESOURCE_GROUP}" \
  --output table

# Verify VM is running
az vm show \
  --resource-group "${RESOURCE_GROUP}" \
  --name "sw-${ENVIRONMENT}-vm" \
  --show-details \
  --query "{Name:name, Status:powerState, IP:publicIps}"

# Verify IoT Hub
az iot hub show \
  --resource-group "${RESOURCE_GROUP}" \
  --name "sw-${ENVIRONMENT}-iothub" \
  --query "{Name:name, Status:properties.state}"

# Verify Storage Account
az storage account show \
  --resource-group "${RESOURCE_GROUP}" \
  --name "sw${ENVIRONMENT//-/}stg" \
  --query "{Name:name, Status:provisioningState}"
```

---

## Database Restoration

### Step 1: Prepare Database Restoration

```bash
# Set variables
export VM_IP="<vm-ip-address>"
export VM_USER="azureuser"
export POSTGRES_HOST="${VM_IP}"
export POSTGRES_PORT="5432"
export POSTGRES_USER="postgres"
export POSTGRES_PASSWORD="<postgres-password>"

# Navigate to backup directory
cd gcp-migration/azure-backup/database-backups/${ENVIRONMENT}/<timestamp>
```

### Step 2: Verify Backup Files

```bash
# List backup files
ls -lh

# Check backup summary
cat backup-summary.json | jq '.'

# Verify backup integrity
for db in smartwatts_users smartwatts_energy smartwatts_devices \
          smartwatts_analytics smartwatts_billing smartwatts_facility360 \
          smartwatts_feature_flags smartwatts_device_verification \
          smartwatts_appliance_monitoring; do
  if [ -f "${db}_complete.sql.gz" ]; then
    echo "✓ ${db} backup found"
  else
    echo "✗ ${db} backup missing"
  fi
done
```

### Step 3: Restore Databases

#### Option A: Restore Complete Databases (Recommended)

```bash
# Restore each database
for db in smartwatts_users smartwatts_energy smartwatts_devices \
          smartwatts_analytics smartwatts_billing smartwatts_facility360 \
          smartwatts_feature_flags smartwatts_device_verification \
          smartwatts_appliance_monitoring; do
  
  echo "Restoring ${db}..."
  
  # Decompress and restore
  gunzip -c "${db}_complete.sql.gz" | \
    PGPASSWORD="${POSTGRES_PASSWORD}" psql \
      --host="${POSTGRES_HOST}" \
      --port="${POSTGRES_PORT}" \
      --username="${POSTGRES_USER}" \
      --dbname="postgres" \
      --command="DROP DATABASE IF EXISTS ${db};" \
      --command="CREATE DATABASE ${db};" \
      --dbname="${db}"
  
  if [ $? -eq 0 ]; then
    echo "✓ ${db} restored successfully"
  else
    echo "✗ Failed to restore ${db}"
  fi
done
```

#### Option B: Restore Schema and Data Separately

```bash
# Restore schema first
for db in smartwatts_users smartwatts_energy smartwatts_devices \
          smartwatts_analytics smartwatts_billing smartwatts_facility360 \
          smartwatts_feature_flags smartwatts_device_verification \
          smartwatts_appliance_monitoring; do
  
  echo "Restoring schema for ${db}..."
  
  # Create database
  PGPASSWORD="${POSTGRES_PASSWORD}" psql \
    --host="${POSTGRES_HOST}" \
    --port="${POSTGRES_PORT}" \
    --username="${POSTGRES_USER}" \
    --dbname="postgres" \
    --command="DROP DATABASE IF EXISTS ${db};" \
    --command="CREATE DATABASE ${db};"
  
  # Restore schema
  PGPASSWORD="${POSTGRES_PASSWORD}" psql \
    --host="${POSTGRES_HOST}" \
    --port="${POSTGRES_PORT}" \
    --username="${POSTGRES_USER}" \
    --dbname="${db}" \
    --file="${db}_schema.sql"
  
  # Restore data
  if [ -f "${db}_data.sql" ]; then
    PGPASSWORD="${POSTGRES_PASSWORD}" psql \
      --host="${POSTGRES_HOST}" \
      --port="${POSTGRES_PORT}" \
      --username="${POSTGRES_USER}" \
      --dbname="${db}" \
      --file="${db}_data.sql"
  fi
  
  echo "✓ ${db} restored"
done
```

### Step 4: Verify Database Restoration

```bash
# Verify all databases exist
PGPASSWORD="${POSTGRES_PASSWORD}" psql \
  --host="${POSTGRES_HOST}" \
  --port="${POSTGRES_PORT}" \
  --username="${POSTGRES_USER}" \
  --dbname="postgres" \
  --command="SELECT datname FROM pg_database WHERE datname LIKE 'smartwatts%' ORDER BY datname;"

# Verify table counts
for db in smartwatts_users smartwatts_energy smartwatts_devices \
          smartwatts_analytics smartwatts_billing smartwatts_facility360 \
          smartwatts_feature_flags smartwatts_device_verification \
          smartwatts_appliance_monitoring; do
  
  echo "Checking ${db}..."
  TABLE_COUNT=$(PGPASSWORD="${POSTGRES_PASSWORD}" psql \
    --host="${POSTGRES_HOST}" \
    --port="${POSTGRES_PORT}" \
    --username="${POSTGRES_USER}" \
    --dbname="${db}" \
    --tuples-only \
    --command="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';")
  
  echo "  Tables: ${TABLE_COUNT}"
done
```

---

## Configuration Restoration

### Step 1: Restore Key Vault Secrets

```bash
# Navigate to secrets backup
cd gcp-migration/azure-backup/configuration-backups/${ENVIRONMENT}/<timestamp>/secrets

# List secrets
cat secret-names.txt

# Restore each secret (if you have the values)
# WARNING: This requires secret values. Use retrieve-secrets.sh if needed.
while IFS= read -r secret_name; do
  echo "Restoring secret: ${secret_name}"
  # az keyvault secret set \
  #   --vault-name "sw-${ENVIRONMENT}-kv" \
  #   --name "${secret_name}" \
  #   --value "<secret-value>"
done < secret-names.txt
```

### Step 2: Restore Application Configurations

```bash
# Navigate to app configs backup
cd gcp-migration/azure-backup/configuration-backups/${ENVIRONMENT}/<timestamp>/app-configs

# Restore application.yml files to services
for service_dir in */; do
  service_name=$(basename "${service_dir}")
  echo "Restoring config for ${service_name}..."
  
  # Copy configuration file
  cp "${service_dir}"/*.yml "backend/${service_name}/src/main/resources/" 2>/dev/null || {
    echo "  Warning: Could not restore config for ${service_name}"
  }
done
```

### Step 3: Restore Environment Variables

```bash
# Navigate to env vars backup
cd gcp-migration/azure-backup/configuration-backups/${ENVIRONMENT}/<timestamp>/env-vars

# Restore .env file
if [ -f ".env" ]; then
  cp .env ../../../../../../azure-deployment/.env
  echo "✓ Environment variables restored"
fi
```

### Step 4: Restore Connection Strings

```bash
# Navigate to connection strings backup
cd gcp-migration/azure-backup/configuration-backups/${ENVIRONMENT}/<timestamp>/connection-strings

# Review connection strings
cat connection-strings.json | jq '.'

# Update application configurations with connection strings
# (Manual step - update each service's application.yml)
```

---

## Application Deployment

### Step 1: SSH into VM

```bash
# SSH into VM
ssh "${VM_USER}@${VM_IP}"

# Verify Docker is running
sudo docker ps

# Verify Docker Compose is available
docker-compose --version
```

### Step 2: Clone Repository

```bash
# On VM, clone repository
git clone https://github.com/<your-org>/mySmartWatts.git
cd mySmartWatts
```

### Step 3: Configure Environment

```bash
# Copy environment file
cp azure-deployment/.env .env

# Update connection strings in .env
nano .env

# Verify environment variables
cat .env
```

### Step 4: Deploy Services

```bash
# Navigate to deployment directory
cd azure-deployment

# Deploy all services
docker-compose -f docker-compose.azure.yml up -d --build

# Monitor deployment
docker-compose -f docker-compose.azure.yml ps
docker-compose -f docker-compose.azure.yml logs -f
```

### Step 5: Verify Services

```bash
# Check service health
curl http://localhost:8080/actuator/health
curl http://localhost:8761/eureka/apps

# Check individual services
for port in 8081 8082 8083 8084 8085; do
  echo "Checking service on port ${port}..."
  curl -s http://localhost:${port}/actuator/health | jq '.' || echo "Service not responding"
done
```

---

## Verification Steps

### Infrastructure Verification

```bash
# Verify all resources exist
az resource list --resource-group "${RESOURCE_GROUP}" --output table

# Verify VM is running
az vm show --resource-group "${RESOURCE_GROUP}" --name "sw-${ENVIRONMENT}-vm" --query powerState

# Verify network connectivity
ping -c 3 "${VM_IP}"
```

### Database Verification

```bash
# Verify database connectivity
PGPASSWORD="${POSTGRES_PASSWORD}" psql \
  --host="${POSTGRES_HOST}" \
  --port="${POSTGRES_PORT}" \
  --username="${POSTGRES_USER}" \
  --dbname="postgres" \
  --command="SELECT version();"

# Verify data integrity
# (Run application-specific verification queries)
```

### Application Verification

```bash
# Test API endpoints
curl http://${VM_IP}:8080/api/v1/health
curl http://${VM_IP}:8080/api/v1/users/health

# Test frontend
curl https://sw-${ENVIRONMENT}-dashboard.azurestaticapps.net
```

---

## Rollback Procedures

### Rollback from GCP Migration

If you need to rollback from GCP to Azure:

1. **Stop GCP Services** (if running)
   ```bash
   # Stop Cloud Run services
   gcloud run services list --project smartwatts-production
   # Manually stop services via GCP Console or CLI
   ```

2. **Restore Azure Infrastructure** (follow Infrastructure Restoration section)

3. **Restore Databases** (follow Database Restoration section)

4. **Update DNS/Load Balancer** to point back to Azure

5. **Verify Services** (follow Verification Steps section)

### Partial Rollback

For partial rollback of specific services:

1. Identify services to rollback
2. Restore only those services' databases
3. Deploy services to Azure
4. Configure traffic splitting (if needed)

---

## Troubleshooting

### Common Issues

#### Issue 1: VM Not Accessible

**Symptoms**: Cannot SSH into VM

**Solutions**:
```bash
# Check VM status
az vm show --resource-group "${RESOURCE_GROUP}" --name "sw-${ENVIRONMENT}-vm" --query powerState

# Start VM if stopped
az vm start --resource-group "${RESOURCE_GROUP}" --name "sw-${ENVIRONMENT}-vm"

# Check NSG rules
az network nsg rule list --resource-group "${RESOURCE_GROUP}" --nsg-name "sw-${ENVIRONMENT}-vm-nsg" --output table

# Verify public IP
az vm show --resource-group "${RESOURCE_GROUP}" --name "sw-${ENVIRONMENT}-vm" --show-details --query publicIps
```

#### Issue 2: Database Connection Failed

**Symptoms**: Cannot connect to PostgreSQL

**Solutions**:
```bash
# Verify PostgreSQL is running on VM
ssh "${VM_USER}@${VM_IP}" "sudo docker ps | grep postgres"

# Check PostgreSQL logs
ssh "${VM_USER}@${VM_IP}" "sudo docker logs smartwatts-postgres"

# Verify network connectivity
telnet "${POSTGRES_HOST}" "${POSTGRES_PORT}"
```

#### Issue 3: Services Not Starting

**Symptoms**: Docker containers failing to start

**Solutions**:
```bash
# Check Docker logs
ssh "${VM_USER}@${VM_IP}" "cd mySmartWatts/azure-deployment && docker-compose logs"

# Check resource constraints
ssh "${VM_USER}@${VM_IP}" "free -h && df -h"

# Restart services
ssh "${VM_USER}@${VM_IP}" "cd mySmartWatts/azure-deployment && docker-compose restart"
```

#### Issue 4: Missing Backup Files

**Symptoms**: Cannot find backup files

**Solutions**:
- Check backup directory: `gcp-migration/azure-backup/`
- Verify backup was completed successfully
- Check backup summary files
- Restore from Azure Blob Storage if available

### Getting Help

If restoration fails:

1. Review error messages carefully
2. Check Azure Portal for resource status
3. Review backup summary files
4. Consult Azure documentation
5. Contact Azure support if needed

---

## Restoration Checklist

Use this checklist to ensure complete restoration:

### Pre-Restoration
- [ ] Backup files available and verified
- [ ] Azure subscription access confirmed
- [ ] Required tools installed
- [ ] Prerequisites met

### Infrastructure
- [ ] Resource group created
- [ ] Infrastructure deployed via Bicep
- [ ] All resources verified
- [ ] Network connectivity confirmed

### Database
- [ ] Backup files verified
- [ ] All databases restored
- [ ] Database integrity verified
- [ ] Connection strings updated

### Configuration
- [ ] Key Vault secrets restored
- [ ] Application configs restored
- [ ] Environment variables restored
- [ ] Connection strings updated

### Application
- [ ] Services deployed
- [ ] Health checks passing
- [ ] API endpoints responding
- [ ] Frontend accessible

### Post-Restoration
- [ ] All services verified
- [ ] Monitoring configured
- [ ] Documentation updated
- [ ] Team notified

---

**Document Status**: Complete  
**Last Updated**: November 2025  
**Next Review**: Before migration execution

