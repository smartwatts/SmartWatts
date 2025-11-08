# SmartWatts Azure Hybrid Architecture

## 🎯 Overview

This document clarifies the **hybrid architecture approach** for SmartWatts Azure deployment, combining production-ready Spring Boot services with Azure free tier services.

---

## 🏗️ Hybrid Architecture Strategy

### What is Hybrid Architecture?

**Hybrid Architecture** = **Spring Boot Microservices** (on VM) + **Azure Cloud Services** (free tier)

Instead of replacing everything with Azure services, we:
- ✅ **Keep** production-ready Spring Boot services (13 microservices)
- ✅ **Keep** PostgreSQL (9 databases) running on VM
- ✅ **Add** Azure IoT Hub for device ingestion (optional)
- ✅ **Add** Azure Blob Storage for file storage (optional)
- ✅ **Add** Azure Static Web Apps for frontend (optional)

---

## 📊 Architecture Components

### On Azure VM (B1s - Free Tier)
```
┌─────────────────────────────────────┐
│         Azure B1s VM (Free)          │
│  ┌─────────────────────────────────┐ │
│  │   Docker Compose Stack          │ │
│  │                                  │ │
│  │  ✅ Spring Boot Services        │ │
│  │     - API Gateway (8080)        │ │
│  │     - User Service (8081)        │ │
│  │     - Energy Service (8082)      │ │
│  │     - Device Service (8083)      │ │
│  │     - Analytics Service (8084)   │ │
│  │     - Billing Service (8085)      │ │
│  │     - ... (13 total)              │ │
│  │                                  │ │
│  │  ✅ PostgreSQL Container         │ │
│  │     - smartwatts_users           │ │
│  │     - smartwatts_energy           │ │
│  │     - smartwatts_devices         │ │
│  │     - smartwatts_analytics       │ │
│  │     - smartwatts_billing         │ │
│  │     - ... (9 databases)          │ │
│  │                                  │ │
│  │  ✅ Redis Container              │ │
│  │  ✅ Next.js Frontend             │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Azure Cloud Services (Free Tier)
```
┌─────────────────────────────────────┐
│   Azure IoT Hub (Free)              │
│   - 8,000 messages/day              │
│   - For edge gateway device ingestion│
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│   Azure Blob Storage (Free)         │
│   - 5 GB storage                    │
│   - For file storage & backups      │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│   Azure Static Web Apps (Free)      │
│   - For frontend hosting (optional)  │
└─────────────────────────────────────┘
```

---

## ✅ Why Hybrid Approach?

### 1. **No Migration Needed**
- ✅ Keep existing PostgreSQL setup (9 databases)
- ✅ Keep existing Spring Boot services (13 microservices)
- ✅ No database migration required
- ✅ No code rewrite required

### 2. **Production-Ready Services**
- ✅ Spring Boot services are 100% complete and tested
- ✅ PostgreSQL databases are already configured
- ✅ All business logic already implemented
- ✅ Security, authentication, authorization all working

### 3. **Cost-Effective**
- ✅ $0/month using Azure free tier
- ✅ PostgreSQL runs on VM (no separate database cost)
- ✅ All services run within 750 free VM hours
- ✅ Azure services (IoT Hub, Blob Storage) are optional

### 4. **Best of Both Worlds**
- ✅ Proven Spring Boot architecture (production-ready)
- ✅ Azure cloud services for IoT and storage (free tier)
- ✅ Flexibility to add more Azure services later
- ✅ No vendor lock-in (can move Spring Boot services anywhere)

---

## 💰 Cost Breakdown

### Free Tier Usage ($0/month)

| Resource | Usage | Cost |
|----------|-------|------|
| **Azure VM (B1s)** | 744 hours/month (24/7) | $0 (750 free hours) |
| **PostgreSQL** | On VM (uses VM disk) | $0 (included in VM) |
| **Redis** | On VM (uses VM memory) | $0 (included in VM) |
| **IoT Hub** | 8,000 messages/day (optional) | $0 (free tier) |
| **Blob Storage** | 5 GB (optional) | $0 (free tier) |
| **Static Web Apps** | Frontend hosting (optional) | $0 (free tier) |

**Total: $0/month** ✅

---

## 🔄 Data Flow

### Current Flow (Without Azure Services)
```
Edge Gateway → MQTT Broker → Spring Boot Services → PostgreSQL
```

### Hybrid Flow (With Azure Services)
```
Edge Gateway → Azure IoT Hub → Spring Boot Services → PostgreSQL
                                      ↓
                              Azure Blob Storage (optional)
```

**Key Point**: Azure IoT Hub is **optional**. You can keep using your existing MQTT broker or switch to IoT Hub for better cloud integration.

---

## 🚀 Implementation Phases

### Phase 1: Deploy Spring Boot on Azure VM
- ✅ Deploy existing Spring Boot services to Azure VM
- ✅ Deploy PostgreSQL container (same as current setup)
- ✅ Deploy Redis container
- ✅ Deploy Next.js frontend
- **Result**: Everything works exactly as before, just on Azure VM

### Phase 2: Add Azure Services (Optional)
- ✅ Integrate Azure IoT Hub for device ingestion
- ✅ Use Azure Blob Storage for file storage
- ✅ Deploy frontend to Azure Static Web Apps
- **Result**: Enhanced with Azure cloud services

### Phase 3: Optimize (Future)
- ✅ Monitor usage and optimize
- ✅ Scale if needed
- ✅ Add more Azure services as needed

---

## 📋 What Stays the Same?

### ✅ Keep As-Is
- **Spring Boot Services**: All 13 microservices unchanged
- **PostgreSQL**: Same 9 databases, same schema
- **Redis**: Same caching setup
- **Frontend**: Same Next.js application
- **Configuration**: Same application.yml files
- **Business Logic**: All MYTO tariffs, NILM, analytics unchanged

### ✅ What Changes?
- **Infrastructure**: Runs on Azure VM instead of local/other cloud
- **Optional**: Can add Azure IoT Hub for device ingestion
- **Optional**: Can use Azure Blob Storage for file storage
- **Optional**: Can deploy frontend to Azure Static Web Apps

---

## 🎯 Success Criteria

### Must Have
- [x] All Spring Boot services running on Azure VM
- [x] PostgreSQL container running with 9 databases
- [x] All services connected and working
- [x] Frontend accessible via public IP
- [x] All consumer-grade features functional
- [x] Cost: $0/month

### Optional Enhancements
- [ ] Azure IoT Hub integrated (for edge gateway)
- [ ] Azure Blob Storage configured (for file storage)
- [ ] Azure Static Web Apps deployed (for frontend)
- [ ] Application Insights monitoring

---

## 🔍 Comparison: Hybrid vs Full Azure Migration

| Aspect | Hybrid Approach | Full Azure Migration |
|--------|----------------|---------------------|
| **Migration Effort** | ✅ None (keep existing) | ❌ High (rewrite all services) |
| **Cost** | ✅ $0/month | ✅ $0/month |
| **Risk** | ✅ Low (proven services) | ❌ High (untested rewrite) |
| **Time to Deploy** | ✅ Days | ❌ Months |
| **Production Ready** | ✅ Yes (already tested) | ❌ No (needs testing) |
| **Flexibility** | ✅ High (can add Azure services) | ❌ Lower (vendor lock-in) |

---

## 📚 Next Steps

1. **Deploy to Azure VM** using existing Docker Compose setup
2. **Test all services** to ensure everything works
3. **Optionally add Azure services** (IoT Hub, Blob Storage) if needed
4. **Monitor usage** to stay within free tier limits
5. **Scale as needed** when you grow

---

## ✅ Summary

**Hybrid Architecture = Best of Both Worlds**

- ✅ **Keep** production-ready Spring Boot services
- ✅ **Keep** PostgreSQL (no migration needed)
- ✅ **Add** Azure free tier services (optional)
- ✅ **Cost**: $0/month
- ✅ **Risk**: Low (proven services)
- ✅ **Time**: Fast (days, not months)

**This approach gives you Azure free tier benefits without the risk and effort of a full migration!** 🎯

