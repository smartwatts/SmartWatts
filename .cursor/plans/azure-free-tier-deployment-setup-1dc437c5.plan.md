<!-- 1dc437c5-8e51-476c-80fa-4448d6a2bc4b 3ac60537-1b3c-4c23-8e95-c19a282c42e1 -->
# Azure Free Tier Deployment Implementation Plan

## Executive Summary

This plan implements a complete Azure deployment infrastructure optimized for Azure's 12-month free tier, using a **hybrid architecture** that:

- **Keeps** production-ready Spring Boot microservices (13 services) on Azure VM
- **Keeps** PostgreSQL (9 databases) on Azure VM in Docker container
- **Adds** Azure IoT Hub (REQUIRED) for device ingestion
- **Adds** Azure Blob Storage (REQUIRED) for database backups and log archival
- **Adds** Azure Static Web Apps (REQUIRED) for frontend hosting

**NO Azure Functions or CosmosDB needed** - we keep existing Spring Boot services and PostgreSQL.

The deployment uses GitHub Actions for CI/CD with branch-based environments (main → staging, prod → production).

## Architecture Recommendations

### 1. Architecture Approach: **Hybrid Architecture (Spring Boot + PostgreSQL on VM)**

**Recommendation**: Keep existing Spring Boot microservices and PostgreSQL on Azure VM, add Azure free tier services. This approach:

- **No migration needed** - Keep production-ready Spring Boot services (13 services)
- **No database migration** - Keep PostgreSQL on VM (9 databases, same as current setup)
- **Zero rewrite effort** - All existing code works as-is
- **Cost-effective** - $0/month using Azure free tier
- **Low risk** - Proven services, no untested rewrites

**Azure Free Tier Services to Use**:

- **Azure VM (B1s)**: 750 free hours/month - Runs Spring Boot + PostgreSQL + Redis + Frontend
- **PostgreSQL**: Docker container on VM (uses VM disk space, $0 cost)
- **Azure IoT Hub (Free Tier)**: 8,000 messages/day - **REQUIRED** for device ingestion
- **Azure Blob Storage (Free Tier)**: 5 GB storage - **REQUIRED** for database backups and log archival
- **Azure Static Web Apps (Free Tier)**: Free hosting - **REQUIRED** for frontend hosting
- **Application Insights (Free Tier)**: 5GB data ingestion/month - **REQUIRED** for application monitoring
- **Azure Key Vault (Free Tier)**: 10,000 read + 1,000 write operations/month (optional)

### 2. API Implementation: **Keep Existing Spring Boot Services**

**Recommendation**: Keep all existing Spring Boot microservices unchanged. This approach:

- **Production-ready** - All 13 services are 100% complete and tested
- **No rewrite needed** - All business logic already implemented
- **Proven architecture** - Already working in production
- **Cost-effective** - Runs on free tier VM

**Services to Keep (13 Microservices)**:

- API Gateway (Port 8080)
- User Service (Port 8081)
- Energy Service (Port 8082)
- Device Service (Port 8083)
- Analytics Service (Port 8084)
- Billing Service (Port 8085)
- API Docs Service (Port 8086)
- Spring Boot Admin (Port 8087)
- Edge Gateway (Port 8088)
- Facility Service (Port 8089)
- Feature Flag Service (Port 8090)
- Device Verification (Port 8091)
- Appliance Monitoring (Port 8092)

### 3. Database Strategy: **Keep PostgreSQL on VM**

**Recommendation**: Keep PostgreSQL running on Azure VM in Docker container (same as current setup). Reasons:

- **No migration needed** - Same PostgreSQL setup as current deployment
- **Zero cost** - Uses VM disk space (included in free tier)
- **Proven setup** - Already working with all 9 databases
- **No compatibility issues** - All Spring Boot services already configured

**Database Strategy**:

- Keep PostgreSQL 15 on VM (Docker container)
- Keep all 9 databases: smartwatts_users, smartwatts_energy, smartwatts_devices, smartwatts_analytics, smartwatts_billing, smartwatts_facility360, smartwatts_feature_flags, smartwatts_device_verification, smartwatts_appliance_monitoring
- No schema changes needed
- No data migration needed

### 4. Frontend Deployment: **Deploy to Azure Static Web Apps (REQUIRED)**

**Recommendation**: Deploy Next.js frontend to Azure Static Web Apps. This approach:

- **REQUIRED** - Frontend hosting on Azure free tier
- Already works in Next.js
- Can deploy to Azure Static Web Apps as-is
- Simpler deployment (one frontend app)
- No additional configuration needed

### 5. Edge Gateway Integration: **Azure IoT Hub Integration (REQUIRED)**

**Recommendation**: **REQUIRED** - Modify edge gateway to use Azure IoT Hub MQTT protocol. Current gateway:

- Uses HTTP REST API for cloud sync (`cloud_api_url` in config)
- Already supports MQTT broker (local)
- **Must** publish to Azure IoT Hub via MQTT for device ingestion

**Required Changes**:

- Add Azure IoT Hub MQTT client to edge gateway
- Update `data_sync.py` to publish to IoT Hub
- Maintain local MQTT broker for device communication
- Add IoT Hub connection string configuration

**Note**: IoT Hub integration is **REQUIRED** for device ingestion.

### 6. Azure Blob Storage: **REQUIRED for Backups and Logs**

**Recommendation**: **REQUIRED** - Use Azure Blob Storage for:

- **Database Backups** (REQUIRED) - Store PostgreSQL backups off-VM for disaster recovery
- **Log File Archival** (REQUIRED) - Archive application logs for long-term retention
- **ML Model Storage** (Optional) - Store TensorFlow Lite models for edge gateway updates
- **Report Generation** (Optional) - Store generated PDF/CSV reports
- **Configuration Backups** (Optional) - Store configuration snapshots

**Benefits**:

- Disaster recovery protection (database backups off-VM)
- Compliance (log file retention)
- Disk space management (free up VM disk)
- Cost-effective (5 GB free tier)

### 7. Repository Structure: **Keep Existing Structure**

**Recommendation**: Keep existing repository structure. No new directories needed:

```
smartwatts/
├── frontend/          # Next.js dashboard (deploy to Static Web Apps - REQUIRED)
├── backend/            # Spring Boot microservices (deploy to VM)
│   ├── api-gateway/
│   ├── user-service/
│   ├── energy-service/
│   └── ... (13 services)
├── edge-gateway/     # Python edge gateway (IoT Hub integration - REQUIRED)
├── infrastructure/    # NEW: Bicep templates for Azure resources
│   └── bicep/
│       ├── main.bicep
│       ├── params.staging.json
│       └── params.prod.json
├── .github/workflows/ # NEW: GitHub Actions
│   ├── deploy-staging.yml
│   └── deploy-prod.yml
└── azure-deployment/  # Existing Azure deployment scripts
```

**Key Points**:

- **No `api/` directory needed** - We keep Spring Boot services, not Azure Functions
- **No CosmosDB migration** - We keep PostgreSQL on VM
- **No Azure Functions code** - All APIs handled by Spring Boot services

## Implementation Tasks

### Phase 1: Infrastructure Setup

1. Create Bicep templates for Azure resources (VM, IoT Hub - REQUIRED, Blob Storage - REQUIRED, Static Web Apps - REQUIRED, Application Insights - REQUIRED, Key Vault)
2. Create parameter files for staging and production environments
3. Set up GitHub Actions workflows for branch-based deployment
4. Configure GitHub secrets for Azure credentials

### Phase 2: VM Deployment Setup

1. Create Azure VM deployment scripts (B1s free tier)
2. Configure Docker Compose for Azure VM deployment
3. Set up PostgreSQL container configuration (9 databases)
4. Configure Spring Boot services for Azure VM
5. Add Application Insights logging (optional)

### Phase 3: Frontend Deployment (REQUIRED)

1. Configure Next.js frontend for Azure Static Web Apps deployment
2. Update frontend API client to use Azure VM endpoints
3. Configure environment variables for staging/production
4. Set up Static Web Apps deployment workflow

### Phase 4: Edge Gateway Integration (REQUIRED)

1. Add Azure IoT Hub Python SDK to edge gateway dependencies
2. Modify `data_sync.py` to support IoT Hub MQTT publishing
3. Update edge gateway configuration for IoT Hub connection
4. Test edge gateway → IoT Hub → Spring Boot services → PostgreSQL flow

### Phase 5: Blob Storage Integration (REQUIRED)

1. Configure Azure Blob Storage for database backups
2. Update backup scripts to upload to Blob Storage
3. Configure log file archival to Blob Storage
4. Set up automated backup and archival workflows
5. Test backup and restore procedures

### Phase 6: CI/CD Pipeline

1. Create GitHub Actions workflow for staging (main branch)
2. Create GitHub Actions workflow for production (prod branch)
3. Configure Azure VM deployment via GitHub Actions
4. Configure Azure Static Web Apps deployment
5. Configure Azure Blob Storage backup workflows
6. Add infrastructure deployment steps (Bicep)

### Phase 7: Documentation & Testing

1. Create deployment documentation
2. Document required GitHub secrets
3. Create testing procedures for each environment
4. Document IoT Hub integration
5. Document Blob Storage backup and archival procedures

## Key Files to Create/Modify

### New Files

- `infrastructure/bicep/main.bicep` - Main Bicep template (VM, IoT Hub - REQUIRED, Blob Storage - REQUIRED, Static Web Apps - REQUIRED)
- `infrastructure/bicep/params.staging.json` - Staging parameters
- `infrastructure/bicep/params.prod.json` - Production parameters
- `.github/workflows/deploy-staging.yml` - Staging deployment workflow
- `.github/workflows/deploy-prod.yml` - Production deployment workflow
- `azure-deployment/docker-compose.azure.yml` - Docker Compose for Azure VM
- `azure-deployment/application-azure.yml` - Azure-specific Spring Boot config
- `scripts/backup-to-blob-storage.sh` - Backup script for Blob Storage
- `scripts/archive-logs-to-blob.sh` - Log archival script for Blob Storage
- `AZURE_DEPLOYMENT_GUIDE.md` - Deployment documentation

### Modified Files (REQUIRED)

- `edge-gateway/services/data_sync.py` - Add IoT Hub MQTT support (REQUIRED)
- `edge-gateway/requirements.txt` - Add Azure IoT Hub SDK (REQUIRED)
- `edge-gateway/config/edge-config.yml` - Add IoT Hub configuration (REQUIRED)
- `scripts/backup-database.sh` - Update to upload backups to Blob Storage (REQUIRED)
- `frontend/utils/api-client.ts` - Update API base URL for Azure VM
- `frontend/next.config.js` - Configure for Static Web Apps (REQUIRED)

## Cost Optimization Strategy

### Free Tier Limits (12 months)

- **VM (B1s)**: 750 hours/month (24/7 = 744 hours) ✅
- **PostgreSQL**: Included in VM (uses VM disk space)
- **IoT Hub**: 8,000 messages/day (REQUIRED) ✅
- **Blob Storage**: 5 GB (REQUIRED - for backups and logs) ✅
- **Static Web Apps**: 100GB bandwidth/month (REQUIRED) ✅
- **Key Vault**: 10K reads + 1K writes/month (optional)
- **App Insights**: 5GB data/month (optional)

### Monitoring Strategy

- Set up Azure Cost Alerts to stay within free tier
- Monitor VM usage (CPU, memory, disk)
- Monitor PostgreSQL database size (stay within 30 GB VM disk)
- Monitor IoT Hub message count (stay within 8,000/day)
- Monitor Blob Storage usage (stay within 5 GB)
- Use Application Insights for application monitoring (optional)

## Success Criteria

1. ✅ All Azure resources deploy successfully via Bicep
2. ✅ GitHub Actions workflows deploy to staging on `main` branch push
3. ✅ GitHub Actions workflows deploy to production on `prod` branch push
4. ✅ Spring Boot services deploy to Azure VM successfully
5. ✅ PostgreSQL container runs with all 9 databases
6. ✅ Frontend deploys to Azure Static Web Apps (REQUIRED)
7. ✅ Edge gateway publishes to Azure IoT Hub (REQUIRED)
8. ✅ Database backups upload to Azure Blob Storage (REQUIRED)
9. ✅ Log files archive to Azure Blob Storage (REQUIRED)
10. ✅ All services stay within Azure free tier limits ($0/month)
11. ✅ End-to-end data flow: Edge Gateway → IoT Hub → Spring Boot Services → PostgreSQL → Frontend

## Next Steps After Implementation

1. Test end-to-end data flow with real edge gateway
2. Monitor Azure usage to ensure free tier compliance
3. Set up Application Insights dashboards (optional)
4. Document operational procedures
5. Optimize VM resource usage based on actual usage

## Important Notes

- **NO Azure Functions needed** - Spring Boot services handle all APIs
- **NO CosmosDB needed** - PostgreSQL on VM handles all database needs
- **NO database migration needed** - Keep existing PostgreSQL setup
- **NO code rewrite needed** - All existing Spring Boot services work as-is
- **Hybrid approach** - Best of both worlds: proven Spring Boot + Azure cloud services

### To-dos

- [ ] Create Bicep infrastructure templates (main.bicep, params.staging.json, params.prod.json) for IoT Hub, Functions, CosmosDB, Static Web Apps, Key Vault, App Insights
- [ ] Create GitHub Actions workflows (deploy-staging.yml, deploy-prod.yml) for branch-based deployment
- [ ] Create api/ directory structure with Python Azure Functions (iot_processor, energy_api, device_api, user_api, analytics_api, billing_api)
- [ ] Implement IoT Hub message processor function that receives messages and stores in CosmosDB
- [ ] Implement core API functions (energy, device, user, analytics, billing) with CosmosDB integration
- [ ] Extract public site from frontend/pages/index.tsx to separate public-site/ directory
- [ ] Configure Next.js frontend for Azure Static Web Apps deployment and update API client
- [ ] Modify edge gateway data_sync.py to support Azure IoT Hub MQTT publishing
- [ ] Update edge gateway configuration and requirements.txt for IoT Hub integration
- [ ] Create AZURE_DEPLOYMENT_GUIDE.md with setup instructions, GitHub secrets configuration, and testing procedures