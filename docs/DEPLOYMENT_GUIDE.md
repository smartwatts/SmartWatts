# SmartWatts Deployment Guide

## Overview

This guide provides comprehensive instructions for deploying SmartWatts to production environments. It covers deployment to Docker, Kubernetes, and cloud platforms.

## Prerequisites

### Required Tools
- Docker 20.10+
- Docker Compose 2.0+
- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Redis 7+

### Required Accounts
- Docker Hub account (for container registry)
- Cloud provider account (AWS/Azure/GCP)
- Domain name (for production deployment)

## Environment Setup

### 1. Environment Variables

#### Copy Template
```bash
cp env.template .env
```

#### Configure Variables
```bash
# Database
POSTGRES_DB=smartwatts
POSTGRES_USER=smartwatts_user
POSTGRES_PASSWORD=<strong-password>
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=<strong-password>

# JWT
JWT_SECRET=<generate-with-openssl-rand-base64-32>
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Email (SendGrid)
SENDGRID_API_KEY=<your-sendgrid-api-key>
SENDGRID_FROM_EMAIL=info@mysmartwatts.com
SENDGRID_FROM_NAME=SmartWatts

# SMS (Twilio)
TWILIO_ACCOUNT_SID=<your-twilio-account-sid>
TWILIO_AUTH_TOKEN=<your-twilio-auth-token>
TWILIO_FROM_NUMBER=<your-twilio-from-number>

# CORS
CORS_ALLOWED_ORIGINS=https://mysmartwatts.com,https://app.mysmartwatts.com

# Sentry
SENTRY_DSN=<your-sentry-dsn>
SENTRY_ENVIRONMENT=production
SENTRY_RELEASE=smartwatts@1.0.0

# Monitoring
GRAFANA_ADMIN_PASSWORD=<strong-password>
```

### 2. Generate Secrets

#### JWT Secret
```bash
openssl rand -base64 32
```

#### Database Password
```bash
openssl rand -base64 24
```

#### Redis Password
```bash
openssl rand -base64 24
```

## Docker Deployment

### 1. Build Images

#### Build All Services
```bash
cd backend
./build-all.sh
```

#### Build Individual Service
```bash
cd backend/user-service
./gradlew build
docker build -t smartwatts/user-service:latest .
```

### 2. Start Services

#### Start All Services
```bash
cd backend
docker-compose up -d
```

#### Start Specific Service
```bash
docker-compose up -d user-service
```

### 3. Verify Deployment

#### Check Service Health
```bash
curl http://localhost:8080/actuator/health
```

#### Check Service Logs
```bash
docker-compose logs -f user-service
```

## Kubernetes Deployment

### 1. Create Namespace
```bash
kubectl create namespace smartwatts
```

### 2. Create Secrets
```bash
kubectl create secret generic smartwatts-secrets \
  --from-env-file=.env \
  -n smartwatts
```

### 3. Deploy Services
```bash
kubectl apply -f k8s/ -n smartwatts
```

### 4. Verify Deployment
```bash
kubectl get pods -n smartwatts
kubectl get services -n smartwatts
```

## Cloud Deployment

### AWS Deployment

#### 1. Create ECS Cluster
```bash
aws ecs create-cluster --cluster-name smartwatts
```

#### 2. Create Task Definition
```bash
aws ecs register-task-definition --cli-input-json file://ecs-task-definition.json
```

#### 3. Create Service
```bash
aws ecs create-service \
  --cluster smartwatts \
  --service-name smartwatts-api \
  --task-definition smartwatts:1 \
  --desired-count 2
```

### Azure Deployment

#### 1. Create Resource Group
```bash
az group create --name smartwatts-rg --location eastus
```

#### 2. Create Container Registry
```bash
az acr create --resource-group smartwatts-rg --name smartwattsregistry --sku Basic
```

#### 3. Deploy Services
```bash
az container create \
  --resource-group smartwatts-rg \
  --name smartwatts-api \
  --image smartwattsregistry.azurecr.io/user-service:latest \
  --dns-name-label smartwatts-api \
  --ports 8081
```

## Monitoring Deployment

### 1. Start Monitoring Stack
```bash
cd monitoring
docker-compose -f docker-compose.monitoring.yml up -d
```

### 2. Access Grafana
- **URL**: http://localhost:3000
- **Username**: admin
- **Password**: (from GRAFANA_ADMIN_PASSWORD)

### 3. Configure Dashboards
- Import Prometheus datasource
- Import Loki datasource
- Create service dashboards

## Health Checks

### 1. Service Health
```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8081/actuator/health

# Energy Service
curl http://localhost:8082/actuator/health
```

### 2. Database Health
```bash
docker exec -it smartwatts-postgres psql -U postgres -c "SELECT 1"
```

### 3. Redis Health
```bash
docker exec -it smartwatts-redis redis-cli ping
```

## Troubleshooting

### Common Issues

#### 1. Service Not Starting
- Check logs: `docker-compose logs service-name`
- Verify environment variables
- Check port conflicts

#### 2. Database Connection Issues
- Verify database is running
- Check connection string
- Verify credentials

#### 3. Redis Connection Issues
- Verify Redis is running
- Check connection configuration
- Verify network connectivity

## Rollback Procedures

### 1. Rollback Service
```bash
docker-compose up -d --no-deps service-name
```

### 2. Rollback Database Migration
```bash
cd backend/service-name
./gradlew flywayRepair
./gradlew flywayMigrate
```

## Backup and Recovery

### 1. Database Backup
```bash
docker exec smartwatts-postgres pg_dump -U postgres smartwatts > backup.sql
```

### 2. Database Restore
```bash
docker exec -i smartwatts-postgres psql -U postgres smartwatts < backup.sql
```

## Security Checklist

### Pre-Deployment
- ✅ All secrets in environment variables
- ✅ Strong passwords generated
- ✅ CORS configured correctly
- ✅ Rate limiting enabled
- ✅ Authentication required
- ✅ HTTPS enabled
- ✅ Security headers configured

### Post-Deployment
- ✅ Health checks passing
- ✅ Monitoring configured
- ✅ Logging configured
- ✅ Error tracking configured
- ✅ Backup procedures tested

## Summary

### ✅ Deployment Status
- **Docker**: Ready for deployment ✅
- **Kubernetes**: Ready for deployment ✅
- **Cloud**: Ready for deployment ✅
- **Monitoring**: Configured ✅
- **Security**: Hardened ✅

### Status
**✅ DEPLOYMENT READY**

SmartWatts is ready for production deployment with comprehensive deployment documentation and procedures.


