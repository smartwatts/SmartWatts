# Azure to GCP Service Mapping

Complete mapping of Azure services to GCP equivalents for SmartWatts migration.

---

## Service Mappings

| Azure Service | GCP Equivalent | Migration Notes |
|--------------|----------------|-----------------|
| **Azure VM (B1s)** | **Cloud Run** | Serverless containers, auto-scaling |
| **PostgreSQL (Docker on VM)** | **Cloud SQL PostgreSQL** | Managed service, high availability |
| **Azure IoT Hub (F1)** | **Cloud IoT Core** or **MQTT Broker on Cloud Run** | Device connectivity |
| **Azure Blob Storage** | **Cloud Storage** | Object storage with lifecycle policies |
| **Azure Static Web Apps** | **Cloud Run (Frontend)** | Next.js standalone deployment |
| **Application Insights** | **Cloud Monitoring + Cloud Logging** | Comprehensive observability |
| **Azure Key Vault** | **Secret Manager** | Secure secret storage |
| **Azure Resource Groups** | **GCP Projects** | Resource organization |
| **Network Security Groups** | **Cloud Armor + VPC Firewall** | Network security |
| **Public IP Address** | **Cloud Run URL** | Automatic HTTPS endpoint |

---

## Environment Variable Mappings

### Database Configuration

| Azure Variable | GCP Equivalent | Notes |
|---------------|----------------|-------|
| `POSTGRES_HOST=postgres` | `SPRING_DATASOURCE_URL` with Cloud SQL connection | Use Cloud SQL Proxy or direct connection |
| `POSTGRES_PORT=5432` | Included in connection string | Cloud SQL uses Unix socket or proxy |
| `POSTGRES_USER=postgres` | `SPRING_DATASOURCE_USERNAME` | From Secret Manager |
| `POSTGRES_PASSWORD` | `SPRING_DATASOURCE_PASSWORD` | From Secret Manager |

**GCP Connection String Format**:
```
jdbc:postgresql:///DATABASE_NAME?cloudSqlInstance=PROJECT_ID:REGION:INSTANCE_NAME&socketFactory=com.google.cloud.sql.postgres.SocketFactory
```

### Redis Configuration

| Azure Variable | GCP Equivalent | Notes |
|---------------|----------------|-------|
| `REDIS_HOST=redis` | `SPRING_DATA_REDIS_HOST` | Use Cloud Memorystore or Redis on Cloud Run |
| `REDIS_PORT=6379` | `SPRING_DATA_REDIS_PORT` | Standard Redis port |
| `REDIS_PASSWORD` | `SPRING_DATA_REDIS_PASSWORD` | From Secret Manager |

### Azure IoT Hub

| Azure Variable | GCP Equivalent | Notes |
|---------------|----------------|-------|
| `IOT_HUB_CONNECTION_STRING` | Cloud IoT Core device credentials | Or MQTT broker connection |
| `IOT_HUB_DEVICE_ID` | Cloud IoT Core device ID | Device registration |

### Azure Storage

| Azure Variable | GCP Equivalent | Notes |
|---------------|----------------|-------|
| `STORAGE_CONNECTION_STRING` | `GOOGLE_APPLICATION_CREDENTIALS` or service account | Use Cloud Storage client libraries |
| `AZURE_BLOB_CONTAINER` | Cloud Storage bucket name | Bucket organization |

### Application Insights

| Azure Variable | GCP Equivalent | Notes |
|---------------|----------------|-------|
| `APP_INSIGHTS_CONNECTION_STRING` | Cloud Monitoring automatically enabled | No explicit connection needed |
| `APP_INSIGHTS_INSTRUMENTATION_KEY` | Not needed | Cloud Run integrates automatically |

---

## Connection String Conversions

### PostgreSQL

**Azure Format**:
```
jdbc:postgresql://postgres:5432/smartwatts_users
```

**GCP Format**:
```
jdbc:postgresql:///smartwatts_users?cloudSqlInstance=smartwatts-staging:europe-west1:smartwatts-staging-db&socketFactory=com.google.cloud.sql.postgres.SocketFactory
```

### Storage

**Azure Format**:
```
DefaultEndpointsProtocol=https;AccountName=swstagingstg;AccountKey=KEY;EndpointSuffix=core.windows.net
```

**GCP Format**:
```
gs://smartwatts-staging-bucket/
```
(Use Cloud Storage client libraries with service account)

---

## Secret Migration

### Azure Key Vault → GCP Secret Manager

| Azure Secret Name | GCP Secret Name | Migration Method |
|------------------|-----------------|------------------|
| `postgres-password` | `postgres-password` | Manual migration via setup-secrets.sh |
| `jwt-secret-key` | `jwt-secret-key` | Manual migration |
| `sendgrid-api-key` | `sendgrid-api-key` | Manual migration |
| `twilio-auth-token` | `twilio-auth-token` | Manual migration |

**Access Pattern**:
- **Azure**: `az keyvault secret show --vault-name VAULT --name SECRET`
- **GCP**: Environment variable or file mount in Cloud Run

---

## Feature Parity Comparison

| Feature | Azure Implementation | GCP Implementation | Status |
|---------|---------------------|-------------------|--------|
| **Auto-scaling** | Manual VM scaling | Cloud Run auto-scaling | ✅ Better |
| **Load Balancing** | Azure Load Balancer | Cloud Run built-in | ✅ Better |
| **SSL/TLS** | Manual certificate management | Google-managed certificates | ✅ Better |
| **Monitoring** | Application Insights | Cloud Monitoring | ✅ Equivalent |
| **Logging** | Application Insights | Cloud Logging | ✅ Equivalent |
| **Backups** | Manual blob storage | Cloud SQL automated backups | ✅ Better |
| **High Availability** | Manual setup | Cloud SQL HA + multi-region | ✅ Better |
| **Cost** | ~$0.30/month (free tier) | Pay-per-use (scales to zero) | ⚠️ Variable |

---

## Migration Checklist

- [ ] All Azure services mapped to GCP equivalents
- [ ] Environment variables updated
- [ ] Connection strings converted
- [ ] Secrets migrated to Secret Manager
- [ ] Application code updated for GCP services
- [ ] Feature parity verified
- [ ] Performance tested
- [ ] Cost analysis completed

---

**Mapping Document Status**: Complete  
**Last Updated**: November 2025
