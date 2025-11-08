# Azure Deployment Plan - Comprehensive Review

## Review Date: Current
## Status: Pre-Implementation Review

---

## ✅ Plan Completeness Check

### Required Azure Services Status

| Service | Status | Notes |
|---------|--------|-------|
| **Azure VM (B1s)** | ✅ Required | Runs Spring Boot + PostgreSQL + Redis + Eureka |
| **PostgreSQL** | ✅ Required | Docker container on VM (9 databases) |
| **Azure IoT Hub** | ✅ Required | Device ingestion (8,000 messages/day) |
| **Azure Blob Storage** | ✅ Required | Database backups + log archival (5 GB) |
| **Azure Static Web Apps** | ✅ Required | Frontend hosting |
| **Application Insights** | ⚠️ **MISSING** | Currently marked as optional, should be REQUIRED |

---

## 🔍 Critical Dependencies Analysis

### Spring Boot Service Dependencies

#### ✅ Covered in Plan
- **PostgreSQL** (9 databases) - Running on VM ✅
- **Redis** - Running on VM ✅
- **Eureka Service Discovery** - Running on VM ✅

#### ⚠️ Missing from Plan (Need Configuration)
- **JWT Secrets** - Required for authentication
  - Need: `JWT_SECRET` environment variable
  - Risk: Services won't authenticate without this
  - Action: Add to environment variables section

- **External API Keys** - Required for some services
  - **OpenWeather API** - Used by Analytics Service
    - Need: `OPENWEATHER_API_KEY` environment variable
    - Risk: Weather features won't work
  - **SendGrid** - Used by Notification Service (email)
    - Need: `SENDGRID_API_KEY` environment variable
    - Risk: Email notifications won't work
  - **Twilio** - Used by Notification Service (SMS)
    - Need: `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN` environment variables
    - Risk: SMS notifications won't work
  - **Sentry** - Optional error tracking
    - Need: `SENTRY_DSN` environment variable (optional)

#### ⚠️ Memory Constraints (Critical)
- **VM Memory**: 1 GB RAM total
- **Services Running**: 13 Spring Boot services + PostgreSQL + Redis + Eureka + Frontend
- **Risk**: **HIGH** - 1GB is insufficient for all services
- **Current JVM Settings**: `-Xmx1g -Xms512m` per service (from docker-compose.yml)
- **Problem**: If each service uses 512MB-1GB, we need ~6-13GB minimum
- **Action Required**: 
  - Optimize JVM heap sizes for B1s VM
  - Reduce heap to ~64-128MB per service
  - Consider running fewer services initially
  - Or upgrade to B2s (4GB RAM) if free tier allows

---

## 🔧 Configuration Changes Required

### 1. Edge Gateway Changes (REQUIRED)

**Current State:**
- Uses HTTP REST API (`cloud_api_url`) for cloud sync
- Sends data to `/api/v1/sync/{table_name}` endpoint

**Required Changes:**
- Add Azure IoT Hub MQTT client
- Modify `data_sync.py` to publish to IoT Hub
- **Backward Compatibility**: Keep HTTP REST API as fallback option
- **Risk**: If IoT Hub integration fails, edge gateway should still work

**Files to Modify:**
- `edge-gateway/services/data_sync.py` - Add IoT Hub MQTT publishing
- `edge-gateway/requirements.txt` - Add `azure-iot-device` package
- `edge-gateway/config/edge-config.yml` - Add IoT Hub connection string

### 2. Frontend Changes (REQUIRED)

**Current State:**
- Uses `NEXT_PUBLIC_API_URL` (defaults to `http://localhost:8080`)
- Has proxy routes in `next.config.js` for `/api/proxy`

**Required Changes:**
- Update `NEXT_PUBLIC_API_URL` to point to Azure VM public IP
- Ensure Static Web Apps supports Next.js API routes
- **Risk**: Static Web Apps may not support Next.js API routes
- **Action**: May need to remove API routes or use Azure Functions (but we're not using Functions)

**Files to Modify:**
- `frontend/utils/api-client.ts` - Already uses `NEXT_PUBLIC_API_URL` ✅
- `frontend/next.config.js` - May need Static Web Apps configuration
- Environment variables for staging/production

### 3. Spring Boot Services Changes

**Required Environment Variables:**
```bash
# Database (Already configured ✅)
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<secure_password>

# Redis (Already configured ✅)
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379

# Eureka (Already configured ✅)
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-discovery:8761/eureka/

# JWT (MISSING - Need to add)
JWT_SECRET=<secure_secret_key>

# External APIs (MISSING - Need to add)
OPENWEATHER_API_KEY=<api_key>
SENDGRID_API_KEY=<api_key>
TWILIO_ACCOUNT_SID=<account_sid>
TWILIO_AUTH_TOKEN=<auth_token>

# Application Insights (MISSING - Need to add)
APPLICATIONINSIGHTS_CONNECTION_STRING=<connection_string>
```

---

## 🚨 Potential Breaking Changes

### 1. Memory Constraints (CRITICAL)
- **Issue**: 1GB RAM for 13 services + PostgreSQL + Redis is insufficient
- **Impact**: Services may crash or fail to start
- **Solution**: 
  - Optimize JVM heap sizes (reduce to 64-128MB per service)
  - Or upgrade VM size (but may cost money)
  - Or run fewer services initially

### 2. Edge Gateway IoT Hub Integration
- **Issue**: Changing from HTTP REST to IoT Hub MQTT
- **Impact**: If integration fails, edge gateway won't sync data
- **Solution**: 
  - Keep HTTP REST API as fallback
  - Make IoT Hub integration optional initially
  - Test thoroughly before making it required

### 3. Frontend Static Web Apps Limitations
- **Issue**: Static Web Apps may not support Next.js API routes
- **Impact**: Frontend API proxy routes may not work
- **Solution**: 
  - Test Static Web Apps with Next.js
  - May need to call Azure VM directly (CORS configuration needed)
  - Or use Azure Functions for API routes (but we're avoiding Functions)

### 4. Network Security
- **Issue**: Need to expose ports on Azure VM
- **Impact**: Security risk if not configured properly
- **Solution**: 
  - Configure NSG rules properly
  - Only expose necessary ports (22, 80, 443, 8080)
  - Use Azure Load Balancer or Application Gateway for HTTPS

### 5. Service Discovery (Eureka)
- **Issue**: Eureka needs to be accessible by all services
- **Impact**: Services won't discover each other
- **Solution**: 
  - Ensure Eureka runs on VM
  - Configure service URLs correctly
  - Test service discovery

---

## 📋 Missing Items in Plan

### 1. Application Insights (REQUIRED - Currently Optional)
- **Status**: Marked as optional in plan, but user wants it REQUIRED
- **Action**: Update plan to mark Application Insights as REQUIRED
- **Configuration**: Need to add instrumentation keys to all Spring Boot services

### 2. Environment Variables Documentation
- **Missing**: Complete list of required environment variables
- **Action**: Add comprehensive environment variables section

### 3. Memory Optimization Strategy
- **Missing**: How to optimize 13 services for 1GB RAM VM
- **Action**: Add memory optimization section with JVM heap size recommendations

### 4. Network Security Configuration
- **Missing**: Detailed NSG rules and firewall configuration
- **Action**: Add network security section with specific port requirements

### 5. Service Startup Order
- **Missing**: Dependency order for service startup
- **Action**: Document startup sequence (PostgreSQL → Redis → Eureka → Services)

### 6. Health Checks and Monitoring
- **Missing**: How to monitor services on Azure VM
- **Action**: Add monitoring section with Application Insights dashboards

### 7. Backup and Restore Procedures
- **Missing**: Detailed backup and restore procedures for Blob Storage
- **Action**: Add backup/restore documentation

### 8. CORS Configuration
- **Missing**: CORS configuration for frontend to call Azure VM APIs
- **Action**: Add CORS configuration section

### 9. SSL/TLS Configuration
- **Missing**: How to configure HTTPS for Azure VM
- **Action**: Add SSL/TLS configuration section

### 10. External API Dependencies
- **Missing**: Documentation for OpenWeather, SendGrid, Twilio API keys
- **Action**: Add external API configuration section

---

## ✅ What's Correct in Plan

1. ✅ Hybrid approach (Spring Boot + PostgreSQL on VM)
2. ✅ No Azure Functions or CosmosDB needed
3. ✅ PostgreSQL on VM (no migration needed)
4. ✅ IoT Hub for device ingestion (REQUIRED)
5. ✅ Blob Storage for backups (REQUIRED)
6. ✅ Static Web Apps for frontend (REQUIRED)
7. ✅ GitHub Actions for CI/CD
8. ✅ Branch-based deployment (main → staging, prod → production)

---

## 🔧 Required Plan Updates

### 1. Mark Application Insights as REQUIRED
- Update all references from "optional" to "REQUIRED"
- Add Application Insights configuration steps
- Add instrumentation key setup

### 2. Add Memory Optimization Section
- Document JVM heap size optimization for B1s VM
- Provide recommended heap sizes per service
- Add monitoring for memory usage

### 3. Add Environment Variables Section
- Complete list of required environment variables
- JWT secrets configuration
- External API keys configuration
- Application Insights connection strings

### 4. Add Network Security Section
- NSG rules for Azure VM
- Port requirements (22, 80, 443, 8080)
- CORS configuration
- Firewall rules

### 5. Add Service Dependencies Section
- Service startup order
- Dependency requirements
- Health check endpoints

### 6. Add External API Dependencies Section
- OpenWeather API configuration
- SendGrid configuration
- Twilio configuration
- Optional: Sentry configuration

### 7. Add Edge Gateway Migration Strategy
- Keep HTTP REST API as fallback
- Gradual migration to IoT Hub
- Testing procedures

### 8. Add Static Web Apps Limitations
- Document Next.js API routes limitations
- Alternative solutions
- CORS configuration for direct API calls

---

## 🎯 Recommendations

### Immediate Actions
1. **Update plan** to mark Application Insights as REQUIRED
2. **Add memory optimization** strategy for B1s VM
3. **Document all environment variables** required
4. **Add network security** configuration details
5. **Add external API dependencies** documentation

### Before Implementation
1. **Test memory requirements** - Verify if 1GB is sufficient
2. **Test Static Web Apps** with Next.js - Verify API routes work
3. **Test IoT Hub integration** - Verify edge gateway can publish
4. **Test Application Insights** - Verify telemetry collection
5. **Test backup/restore** - Verify Blob Storage integration

### Risk Mitigation
1. **Start with fewer services** if memory is insufficient
2. **Keep HTTP REST API** as fallback for edge gateway
3. **Monitor memory usage** closely
4. **Have rollback plan** ready
5. **Test in staging** before production

---

## 📝 Summary

The plan is **mostly complete** but needs:
1. ✅ Application Insights marked as REQUIRED (not optional)
2. ⚠️ Memory optimization strategy for 1GB VM
3. ⚠️ Complete environment variables documentation
4. ⚠️ Network security configuration details
5. ⚠️ External API dependencies documentation
6. ⚠️ Edge gateway migration strategy (with fallback)
7. ⚠️ Static Web Apps limitations and solutions

**Overall Assessment**: Plan is **good** but needs these additions before implementation to avoid breaking changes.

