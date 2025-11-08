# Service/Infrastructure Fixes - January 2025

## Issues Fixed

### 1. Database Connection Issues ✅

**Problem**: Services were using incorrect database names in `docker-compose.yml`

**Fixed Services**:
- **Energy Service**: Changed from `smartwatts` to `smartwatts_energy`
- **Analytics Service**: Changed from `smartwatts` to `smartwatts_analytics`
- **Device Service**: Changed from `smartwatts` to `smartwatts_devices`
- **Billing Service**: Changed from `smartwatts` to `smartwatts_billing`
- **User Service**: Already correct (`smartwatts_users`)
- **Facility Service**: Already correct (`smartwatts_facility360`)

**Changes Made**:
- Updated `SPRING_DATASOURCE_URL` in `docker-compose.yml` for all services
- Added explicit `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` environment variables
- Added Eureka connection to all services via `JAVA_OPTS`

### 2. API Gateway Redis Connection ✅

**Problem**: API Gateway was using incorrect Redis configuration path (`spring.redis.host` instead of `spring.data.redis.host`)

**Fix**: Updated `backend/api-gateway/src/main/resources/application-docker.yml`:
```yaml
# Before (WRONG):
spring:
  redis:
    host: redis

# After (CORRECT):
spring:
  data:
    redis:
      host: redis
```

**Why**: Spring Cloud Gateway uses Spring Data Redis, which requires the `spring.data.redis.*` configuration path.

### 3. Billing Service Flyway ✅

**Problem**: Flyway was disabled in Billing Service, causing migration issues

**Fix**: Enabled Flyway in `backend/billing-service/src/main/resources/application.yml`:
```yaml
flyway:
  enabled: true  # Changed from false
  baseline-on-migrate: true
  locations: classpath:db/migration
  validate-on-migrate: true
```

### 4. Service Discovery (Eureka) ✅

**Problem**: Some services weren't connecting to Eureka

**Fix**: Added Eureka connection to all services via `JAVA_OPTS`:
```yaml
command: ["java", "-Xmx1g", "-Xms512m", "-Deureka.client.serviceUrl.defaultZone=http://service-discovery:8761/eureka/", "-jar", "/app/app.jar"]
```

## How to Start Services

### Option 1: Use the startup script (Recommended)
```bash
cd backend
./start-all-services.sh
```

### Option 2: Manual startup
```bash
cd backend

# Start infrastructure first
docker-compose up -d postgres redis service-discovery

# Wait for infrastructure to be ready
sleep 15

# Start all services
docker-compose up -d

# Check service health
docker-compose ps
```

## Verification

### Check Service Health
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# Energy Service
curl http://localhost:8082/actuator/health

# Analytics Service
curl http://localhost:8084/actuator/health

# Billing Service
curl http://localhost:8085/actuator/health

# Facility Service
curl http://localhost:8089/actuator/health
```

### Check Eureka Registrations
```bash
# View Eureka dashboard
open http://localhost:8761

# Or check via API
curl http://localhost:8761/eureka/apps | grep -o '<name>[^<]*</name>'
```

### Check Redis Connection (API Gateway)
```bash
curl http://localhost:8080/actuator/health | jq .components.redis
# Should return: {"status": "UP", "details": {"version": "7.4.5"}}
```

## Expected Service Status

After fixes, all services should:
- ✅ Connect to PostgreSQL successfully
- ✅ Register with Eureka service discovery
- ✅ API Gateway connects to Redis
- ✅ All services respond to health checks
- ✅ Flyway migrations run successfully

## Troubleshooting

### If services still fail to start:

1. **Check Docker logs**:
   ```bash
   docker-compose logs [service-name]
   ```

2. **Check database connectivity**:
   ```bash
   docker exec smartwatts-postgres psql -U postgres -c "\l"
   ```

3. **Check Redis connectivity**:
   ```bash
   docker exec smartwatts-redis redis-cli ping
   ```

4. **Check Eureka**:
   ```bash
   curl http://localhost:8761
   ```

5. **Rebuild services if needed**:
   ```bash
   docker-compose build [service-name]
   docker-compose up -d [service-name]
   ```

## Next Steps

1. ✅ All database connection issues fixed
2. ✅ API Gateway Redis connection fixed
3. ✅ Billing Service Flyway enabled
4. ✅ Eureka connections added to all services
5. ⏳ Start services and verify they're running
6. ⏳ Test inter-service communication
7. ⏳ Verify all services register with Eureka

