# SmartWatts Azure Architecture Documentation

**Document Version**: 1.0  
**Date**: November 2025  
**Purpose**: Complete documentation of current Azure infrastructure for GCP migration backup

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Resource Inventory](#resource-inventory)
3. [Service Dependencies](#service-dependencies)
4. [Network Topology](#network-topology)
5. [Configuration Files](#configuration-files)
6. [Environment Variables](#environment-variables)
7. [Security Groups](#security-groups)
8. [Connection Strings](#connection-strings)
9. [Cost Analysis](#cost-analysis)

---

## Architecture Overview

### Current Deployment Model

SmartWatts is deployed on Azure using a **hybrid architecture** approach:

- **Compute**: Azure Virtual Machine (B1s - Free Tier)
- **Database**: PostgreSQL 15 running in Docker container on VM
- **IoT**: Azure IoT Hub (F1 - Free Tier)
- **Storage**: Azure Blob Storage (Standard_LRS - Free Tier)
- **Frontend**: Azure Static Web Apps (Free Tier)
- **Monitoring**: Azure Application Insights
- **Secrets**: Azure Key Vault (Standard tier)

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Azure Subscription                        │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         Resource Group: sw-staging-rg                │  │
│  │         Location: westeurope                          │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐ │  │
│  │  │  Virtual Machine: sw-staging-vm                  │ │  │
│  │  │  Size: Standard_B1s (1 vCPU, 1 GB RAM)           │ │  │
│  │  │  OS: Ubuntu 20.04 LTS                            │ │  │
│  │  │  Disk: 30 GB SSD                                 │ │  │
│  │  │                                                    │ │  │
│  │  │  ┌─────────────────────────────────────────────┐ │ │  │
│  │  │  │  Docker Compose Stack                        │ │ │  │
│  │  │  │                                              │ │ │  │
│  │  │  │  • PostgreSQL 15 (9 databases)              │ │ │  │
│  │  │  │  • Redis 7 (caching)                        │ │ │  │
│  │  │  │  • Service Discovery (Eureka) - Port 8761   │ │ │  │
│  │  │  │  • API Gateway - Port 8080                   │ │ │  │
│  │  │  │  • User Service - Port 8081                  │ │ │  │
│  │  │  │  • Energy Service - Port 8082                │ │ │  │
│  │  │  │  • Device Service - Port 8083                │ │ │  │
│  │  │  │  • Analytics Service - Port 8084             │ │ │  │
│  │  │  │  • Billing Service - Port 8085               │ │ │  │
│  │  │  │  • API Docs Service - Port 8086               │ │ │  │
│  │  │  │  • Spring Boot Admin - Port 8087              │ │ │  │
│  │  │  │  • Edge Gateway - Port 8088                  │ │ │  │
│  │  │  │  • Facility Service - Port 8089              │ │ │  │
│  │  │  │  • Feature Flag Service - Port 8090          │ │ │  │
│  │  │  │  • Device Verification - Port 8091           │ │ │  │
│  │  │  │  • Appliance Monitoring - Port 8092          │ │ │  │
│  │  │  │  • Notification Service - Port 8093           │ │ │  │
│  │  │  └─────────────────────────────────────────────┘ │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐ │  │
│  │  │  Network Interface: sw-staging-vm-nic            │ │  │
│  │  │  Public IP: sw-staging-vm-pip                    │ │  │
│  │  │  Private IP: 10.0.1.x (Dynamic)                  │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐ │  │
│  │  │  Network Security Group: sw-staging-vm-nsg      │ │  │
│  │  │  Rules: SSH (22), HTTP (80), HTTPS (443),        │ │  │
│  │  │         API Gateway (8080)                      │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐ │  │
│  │  │  Virtual Network: sw-staging-vnet                │ │  │
│  │  │  Address Space: 10.0.0.0/16                      │ │  │
│  │  │  Subnet: default (10.0.1.0/24)                   │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Azure IoT Hub: sw-staging-iothub                     │  │
│  │  Tier: F1 (Free)                                       │  │
│  │  Messages: 8,000/day                                  │  │
│  │  Devices: 2 free                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Storage Account: swstagingstg                         │  │
│  │  Type: StorageV2 (Standard_LRS)                        │  │
│  │  Containers:                                           │  │
│  │    • backups (database backups)                       │  │
│  │    • logs (application logs)                          │  │
│  │    • ml-models (ML model storage)                     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Static Web App: sw-staging-dashboard                │  │
│  │  Tier: Free                                            │  │
│  │  URL: https://sw-staging-dashboard.azurestaticapps.net│ │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Application Insights: sw-staging-insights            │  │
│  │  Type: web                                             │  │
│  │  Ingestion: 5 GB/month (free tier)                     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Key Vault: sw-staging-kv                            │  │
│  │  Tier: Standard                                       │  │
│  │  Secrets: Database passwords, JWT keys, API keys    │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## Resource Inventory

### Compute Resources

| Resource Name | Type | Size/Tier | Location | Purpose |
|--------------|------|-----------|----------|---------|
| `sw-staging-vm` | Virtual Machine | Standard_B1s | westeurope | Hosts all Spring Boot services |
| `sw-staging-vm-nic` | Network Interface | Standard | westeurope | VM network interface |
| `sw-staging-vm-pip` | Public IP Address | Standard | westeurope | Public IP for VM access |
| `sw-staging-vm-osdisk` | Managed Disk | Standard_LRS, 30 GB | westeurope | VM OS disk |

### Network Resources

| Resource Name | Type | Configuration | Purpose |
|--------------|------|---------------|---------|
| `sw-staging-vnet` | Virtual Network | 10.0.0.0/16 | Isolated network for VM |
| `sw-staging-vnet/default` | Subnet | 10.0.1.0/24 | VM subnet |
| `sw-staging-vm-nsg` | Network Security Group | 4 rules | Firewall rules for VM |

### Database Resources

| Database Name | Type | Location | Purpose |
|--------------|------|----------|---------|
| `smartwatts_users` | PostgreSQL 15 | VM (Docker) | User management and authentication |
| `smartwatts_energy` | PostgreSQL 15 | VM (Docker) | Energy consumption data |
| `smartwatts_devices` | PostgreSQL 15 | VM (Docker) | IoT device management |
| `smartwatts_analytics` | PostgreSQL 15 | VM (Docker) | Analytics and insights |
| `smartwatts_billing` | PostgreSQL 15 | VM (Docker) | Billing and tariff data |
| `smartwatts_facility360` | PostgreSQL 15 | VM (Docker) | Facility management |
| `smartwatts_feature_flags` | PostgreSQL 15 | VM (Docker) | Feature flag management |
| `smartwatts_device_verification` | PostgreSQL 15 | VM (Docker) | Device verification |
| `smartwatts_appliance_monitoring` | PostgreSQL 15 | VM (Docker) | Appliance monitoring |

### IoT Resources

| Resource Name | Type | Tier | Configuration |
|--------------|------|------|---------------|
| `sw-staging-iothub` | IoT Hub | F1 (Free) | 8,000 messages/day, 2 devices |

### Storage Resources

| Resource Name | Type | Tier | Containers |
|--------------|------|------|------------|
| `swstagingstg` | Storage Account | Standard_LRS | backups, logs, ml-models |

### Application Resources

| Resource Name | Type | Tier | Purpose |
|--------------|------|------|---------|
| `sw-staging-dashboard` | Static Web App | Free | Frontend hosting |
| `sw-staging-insights` | Application Insights | Free | Application monitoring |
| `sw-staging-kv` | Key Vault | Standard | Secrets management |

---

## Service Dependencies

### Service Communication Flow

```
Internet
  │
  ├─→ Azure Static Web App (Frontend)
  │     │
  │     └─→ API Gateway (Port 8080)
  │           │
  │           ├─→ Service Discovery (Eureka - Port 8761)
  │           │
  │           ├─→ User Service (Port 8081)
  │           │     └─→ PostgreSQL: smartwatts_users
  │           │
  │           ├─→ Energy Service (Port 8082)
  │           │     └─→ PostgreSQL: smartwatts_energy
  │           │
  │           ├─→ Device Service (Port 8083)
  │           │     └─→ PostgreSQL: smartwatts_devices
  │           │
  │           ├─→ Analytics Service (Port 8084)
  │           │     └─→ PostgreSQL: smartwatts_analytics
  │           │
  │           ├─→ Billing Service (Port 8085)
  │           │     └─→ PostgreSQL: smartwatts_billing
  │           │
  │           └─→ [Other Services...]
  │
  └─→ Edge Gateway (Port 8088)
        │
        └─→ Azure IoT Hub
              └─→ Storage Account (Blob Storage)
```

### Service Startup Order

1. **PostgreSQL** - Database must be ready first
2. **Redis** - Cache service
3. **Service Discovery (Eureka)** - Service registry
4. **API Gateway** - Depends on Eureka
5. **All Microservices** - Register with Eureka, connect to databases
6. **Edge Gateway** - Connects to IoT Hub

### External Dependencies

- **SendGrid** - Email notifications
- **Twilio** - SMS notifications
- **OpenWeatherMap** - Weather data for analytics
- **Azure IoT Hub** - Device data ingestion
- **Azure Blob Storage** - File storage and backups

---

## Network Topology

### Virtual Network Configuration

- **Address Space**: 10.0.0.0/16
- **Subnet**: default (10.0.1.0/24)
- **DNS Servers**: Azure default
- **Peering**: None

### Network Security Group Rules

| Priority | Name | Direction | Protocol | Port | Source | Destination |
|----------|------|-----------|----------|------|--------|-------------|
| 1000 | SSH | Inbound | TCP | 22 | * | * |
| 1010 | HTTP | Inbound | TCP | 80 | * | * |
| 1020 | HTTPS | Inbound | TCP | 443 | * | * |
| 1030 | API-Gateway | Inbound | TCP | 8080 | * | * |

### Public IP Configuration

- **Allocation Method**: Static
- **SKU**: Standard
- **Version**: IPv4
- **DNS Name**: None (using IP directly)

---

## Configuration Files

### Infrastructure as Code

- **Bicep Template**: `infrastructure/bicep/main.bicep`
- **Staging Parameters**: `infrastructure/bicep/params.staging.json`
- **Production Parameters**: `infrastructure/bicep/params.prod.json`

### Docker Compose

- **Main Compose**: `docker-compose.yml`
- **Azure Compose**: `azure-deployment/docker-compose.azure.yml`

### Application Configuration

- **Environment Template**: `env.template`
- **Azure Config**: `azure-deployment/application-azure.yml`

---

## Environment Variables

### VM-Level Environment Variables

```bash
# Database Configuration
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<stored-in-key-vault>
POSTGRES_MULTIPLE_DATABASES=smartwatts_users,smartwatts_energy,smartwatts_devices,smartwatts_analytics,smartwatts_billing,smartwatts_facility360,smartwatts_feature_flags,smartwatts_device_verification,smartwatts_appliance_monitoring

# Redis Configuration
REDIS_PASSWORD=<stored-in-key-vault>

# Azure IoT Hub
IOT_HUB_CONNECTION_STRING=<from-iot-hub>
IOT_HUB_DEVICE_ID=edge-gateway-001

# Azure Storage
STORAGE_CONNECTION_STRING=<from-storage-account>
AZURE_BLOB_CONTAINER=backups

# Application Insights
APP_INSIGHTS_CONNECTION_STRING=<from-app-insights>
APP_INSIGHTS_INSTRUMENTATION_KEY=<from-app-insights>
```

### Service-Specific Environment Variables

Each Spring Boot service uses:
- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/<database-name>`
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery:8761/eureka/`
- Service-specific API keys (SendGrid, Twilio, OpenWeatherMap)

---

## Security Groups

### Network Security Group Details

**Name**: `sw-staging-vm-nsg`

**Inbound Rules**:
1. **SSH (Port 22)**
   - Priority: 1000
   - Source: * (should be restricted in production)
   - Destination: *
   - Protocol: TCP

2. **HTTP (Port 80)**
   - Priority: 1010
   - Source: *
   - Destination: *
   - Protocol: TCP

3. **HTTPS (Port 443)**
   - Priority: 1020
   - Source: *
   - Destination: *
   - Protocol: TCP

4. **API Gateway (Port 8080)**
   - Priority: 1030
   - Source: *
   - Destination: *
   - Protocol: TCP

**Outbound Rules**: Default (Allow all)

### Key Vault Access Policies

- **VM Managed Identity**: Read access to secrets
- **Service Principals**: Read access for CI/CD

---

## Connection Strings

### Database Connection Strings

**Format**: `jdbc:postgresql://postgres:5432/<database-name>`

**Databases**:
- `jdbc:postgresql://postgres:5432/smartwatts_users`
- `jdbc:postgresql://postgres:5432/smartwatts_energy`
- `jdbc:postgresql://postgres:5432/smartwatts_devices`
- `jdbc:postgresql://postgres:5432/smartwatts_analytics`
- `jdbc:postgresql://postgres:5432/smartwatts_billing`
- `jdbc:postgresql://postgres:5432/smartwatts_facility360`
- `jdbc:postgresql://postgres:5432/smartwatts_feature_flags`
- `jdbc:postgresql://postgres:5432/smartwatts_device_verification`
- `jdbc:postgresql://postgres:5432/smartwatts_appliance_monitoring`

### Azure IoT Hub Connection String

**Format**: `HostName=<hub-name>.azure-devices.net;DeviceId=<device-id>;SharedAccessKey=<key>`

**Device**: `edge-gateway-001`

### Storage Account Connection String

**Format**: `DefaultEndpointsProtocol=https;AccountName=<account>;AccountKey=<key>;EndpointSuffix=core.windows.net`

### Application Insights Connection String

**Format**: `<connection-string-from-portal>`

---

## Cost Analysis

### Current Monthly Costs (Free Tier)

| Service | Tier | Monthly Cost | Usage |
|---------|------|--------------|-------|
| Virtual Machine | B1s | $0 | 750 hours/month (24/7 = 744 hours) |
| PostgreSQL | Included | $0 | Runs on VM |
| IoT Hub | F1 | $0 | 8,000 messages/day |
| Blob Storage | Standard_LRS | $0 | 5 GB free |
| Static Web App | Free | $0 | 100 GB bandwidth/month |
| Application Insights | Free | $0 | 5 GB data ingestion/month |
| Key Vault | Standard | $0.03/secret/month | ~10 secrets |

**Total Estimated Cost**: ~$0.30/month (Key Vault only)

### Resource Limits

- **VM**: 750 hours/month (within free tier)
- **IoT Hub**: 8,000 messages/day
- **Storage**: 5 GB free
- **Static Web App**: 100 GB bandwidth/month
- **Application Insights**: 5 GB data ingestion/month

---

## Backup and Recovery

### Current Backup Strategy

1. **Database Backups**: Daily backups to Azure Blob Storage
2. **Configuration Backups**: Exported to Blob Storage
3. **ARM Templates**: Version controlled in Git
4. **Application Logs**: Archived to Blob Storage

### Recovery Procedures

See `azure-restoration-guide.md` for detailed restoration procedures.

---

## Migration Readiness Checklist

- [x] All resources documented
- [x] Architecture diagram created
- [x] Connection strings documented
- [x] Environment variables cataloged
- [x] Network topology mapped
- [x] Security groups documented
- [x] Cost analysis completed
- [x] Backup procedures documented

---

**Document Status**: Complete  
**Last Updated**: November 2025  
**Next Review**: Before migration execution

