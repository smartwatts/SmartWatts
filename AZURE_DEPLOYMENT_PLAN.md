# SmartWatts Azure Free Tier Deployment Plan

## 🎯 **Deployment Overview**

**Target**: Azure Free Tier (Hybrid Architecture)  
**Cost**: $0/month  
**Architecture**: Spring Boot Microservices + Azure Services  
**Resources**: B1s VM, PostgreSQL (on VM), IoT Hub, Blob Storage  

---

## 🏗️ **Hybrid Architecture Approach**

This deployment uses a **hybrid approach** that combines:
- **Spring Boot Microservices** (production-ready, 13 services) - Running on Azure VM
- **PostgreSQL** (9 databases) - Running on Azure VM in Docker
- **Azure IoT Hub** - For device ingestion (free tier)
- **Azure Blob Storage** - For file storage (free tier)
- **Azure Static Web Apps** - For frontend hosting (optional, free tier)

**Why Hybrid?**
- Keep production-ready Spring Boot services (no rewrite needed)
- Use Azure free tier services for IoT and storage
- Zero migration effort (keep existing PostgreSQL setup)
- Best of both worlds: proven Spring Boot + Azure cloud services

---

## 📋 **Azure Free Tier Resources**

### **Compute**
- **VM Size**: B1s (1 vCPU, 1 GB RAM)
- **Hours**: 750 free hours/month
- **OS**: Ubuntu 20.04 LTS
- **Storage**: 30 GB SSD
- **Usage**: Runs Spring Boot services + PostgreSQL + Redis + Frontend

### **Database**
- **Service**: PostgreSQL 15 (Docker container on VM)
- **Databases**: 9 databases (smartwatts_users, smartwatts_energy, etc.)
- **Storage**: Uses VM disk space (30 GB SSD)
- **Cost**: $0/month (included in VM free tier)

### **IoT Services**
- **Service**: Azure IoT Hub
- **Tier**: Free (F1)
- **Messages**: 8,000/day
- **Devices**: 2 free

### **Storage**
- **Service**: Azure Blob Storage
- **Tier**: Hot
- **Capacity**: 5 GB free
- **Transactions**: 20,000 free/month

---

## 🏗️ **Architecture Design**

### **Hybrid Architecture - Single VM Deployment**
```
┌─────────────────────────────────────┐
│           Azure B1s VM              │
│  ┌─────────────────────────────────┐ │
│  │        Docker Compose           │ │
│  │  ┌─────────────────────────────┐ │ │
│  │  │     Frontend (Next.js)      │ │ │
│  │  │        Port 3000            │ │ │
│  │  └─────────────────────────────┘ │ │
│  │  ┌─────────────────────────────┐ │ │
│  │  │   API Gateway (Spring)      │ │ │
│  │  │        Port 8080            │ │ │
│  │  └─────────────────────────────┘ │ │
│  │  ┌─────────────────────────────┐ │ │
│  │  │   Microservices (Spring)    │ │ │
│  │  │   Ports 8081-8092           │ │ │
│  │  │   (13 services)             │ │ │
│  │  └─────────────────────────────┘ │ │
│  │  ┌─────────────────────────────┐ │ │
│  │  │   PostgreSQL Container      │ │ │
│  │  │   (9 databases)             │ │ │
│  │  │   Port 5432                 │ │ │
│  │  └─────────────────────────────┘ │ │
│  │  ┌─────────────────────────────┐ │ │
│  │  │   Redis Cache               │ │ │
│  │  │        Port 6379            │ │ │
│  │  └─────────────────────────────┘ │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│         Azure IoT Hub (Free)       │
│   For device ingestion              │
│   8,000 messages/day               │
└─────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│       Azure Blob Storage (Free)     │
│   File storage & backups             │
│   5 GB free                          │
└─────────────────────────────────────┘
```

**Key Points:**
- **PostgreSQL runs on VM** (no migration needed)
- **Spring Boot services unchanged** (production-ready)
- **Azure IoT Hub** for device ingestion (optional enhancement)
- **Azure Blob Storage** for file storage (optional enhancement)

---

## 🔧 **Implementation Steps**

### **Phase 1: Azure Infrastructure Setup**
1. **Create Resource Group**
2. **Deploy B1s VM with Ubuntu 20.04**
3. **Set up Azure IoT Hub** (optional, for device ingestion)
4. **Create Storage Account** (optional, for file storage)

### **Phase 2: Application Deployment**
1. **Install Docker on VM**
2. **Deploy application containers** (Spring Boot + PostgreSQL + Redis + Frontend)
3. **Configure PostgreSQL** (9 databases, same as current setup)
4. **Configure Azure IoT Hub integration** (optional, for edge gateway)
5. **Set up monitoring and logging**

**Note**: No database migration needed - PostgreSQL runs on VM in Docker container, same as current setup.

### **Phase 4: Testing & Optimization**
1. **Test all services**
2. **Verify consumer-grade features**
3. **Performance optimization**
4. **Security hardening**

---

## 💰 **Cost Analysis**

### **Free Tier Usage**
- **VM**: 750 hours/month (24/7 = 744 hours) ✅
  - Runs: Spring Boot services + PostgreSQL + Redis + Frontend
- **PostgreSQL**: Included in VM (uses VM disk space) ✅
  - 9 databases, estimated ~10-20 GB total
- **IoT Hub**: 8,000 messages/day (optional) ✅
  - For edge gateway device ingestion
- **Blob Storage**: 5 GB (optional) ✅
  - For file storage and backups

### **Potential Overages**
- **VM**: $0.0052/hour if over 750 hours
- **IoT Hub**: $0.10/1M messages if over 8,000/day (if used)
- **Blob Storage**: $0.0184/GB/month if over 5 GB (if used)

**Total Estimated Cost**: $0/month (within free tier limits)

**Note**: PostgreSQL runs on the VM, so no separate database cost. All services run within the 750 free VM hours.

---

## 🚀 **Deployment Scripts**

### **1. Azure CLI Commands**
```bash
# Create resource group
az group create --name smartwatts-rg --location eastus

# Create VM
az vm create \
  --resource-group smartwatts-rg \
  --name smartwatts-vm \
  --image Ubuntu2004 \
  --size Standard_B1s \
  --admin-username azureuser \
  --generate-ssh-keys

# Create IoT Hub (optional, for device ingestion)
az iot hub create \
  --resource-group smartwatts-rg \
  --name smartwatts-iot-hub \
  --sku F1

# Create Storage Account (optional, for file storage)
az storage account create \
  --resource-group smartwatts-rg \
  --name smartwattsstorage \
  --sku Standard_LRS

# Note: PostgreSQL will run in Docker container on the VM
# No separate database service needed - uses existing PostgreSQL setup
```

### **2. VM Setup Script**
```bash
#!/bin/bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Node.js (for frontend)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install Java 17 (for backend)
sudo apt install openjdk-17-jdk -y

# Install Git
sudo apt install git -y

# Clone repository
git clone https://github.com/your-username/smartwatts.git
cd smartwatts
```

---

## 🔐 **Security Configuration**

### **Network Security**
- **NSG Rules**: Allow ports 22 (SSH), 80 (HTTP), 443 (HTTPS), 3000 (Frontend)
- **Firewall**: UFW configured for minimal open ports
- **SSL/TLS**: Let's Encrypt certificates for HTTPS

### **Database Security**
- **PostgreSQL Firewall**: Restrict to VM internal network only
- **Connection Encryption**: TLS 1.2+ required
- **Authentication**: PostgreSQL authentication with strong passwords
- **Network Isolation**: PostgreSQL only accessible from within VM

### **Application Security**
- **JWT Tokens**: Secure token generation and validation
- **CORS**: Properly configured for production
- **Input Validation**: All inputs validated and sanitized

---

## 📊 **Monitoring & Logging**

### **Azure Monitor**
- **VM Metrics**: CPU, Memory, Disk usage
- **PostgreSQL Metrics**: Connection count, query performance (via application logs)
- **IoT Hub Metrics**: Message count, device count (if used)

### **Application Logging**
- **Log Files**: Centralized logging to Azure Blob Storage
- **Error Tracking**: Application Insights integration
- **Performance**: Response time monitoring

---

## 🎯 **Success Criteria**

### **Must Have**
- [ ] All services running on Azure VM
- [ ] PostgreSQL container running with 9 databases
- [ ] Spring Boot services connected to PostgreSQL
- [ ] Frontend accessible via public IP
- [ ] All consumer-grade features functional
- [ ] Azure IoT Hub integrated (optional, for edge gateway)

### **Should Have**
- [ ] SSL certificates configured
- [ ] Monitoring and alerting set up
- [ ] Backup strategy implemented
- [ ] Performance optimized for B1s VM

### **Could Have**
- [ ] Auto-scaling configured
- [ ] CDN integration for static assets
- [ ] Advanced monitoring dashboards
- [ ] Disaster recovery plan

---

## 🚨 **Risk Mitigation**

### **Resource Constraints**
- **Memory**: 1 GB RAM - optimize JVM heap sizes
- **CPU**: 1 vCPU - use efficient algorithms
- **Storage**: 30 GB - regular cleanup and optimization

### **Free Tier Limits**
- **VM Hours**: Monitor usage to stay within 750 hours
- **VM Disk Space**: Monitor PostgreSQL database size (30 GB total)
- **IoT Messages**: Optimize message frequency (if using IoT Hub)
- **Blob Storage**: Compress and clean up old files (if using Blob Storage)

---

## 📈 **Scaling Strategy**

### **Vertical Scaling**
- Upgrade to B2s (2 vCPU, 4 GB RAM) if needed
- Increase VM disk size if PostgreSQL databases grow

### **Horizontal Scaling**
- Add more VMs behind load balancer
- Use Azure Container Instances for microservices
- Implement Azure Functions for serverless components

---

## 🎉 **Expected Outcomes**

### **Immediate Benefits**
- **Zero Cost**: Full deployment within Azure Free Tier
- **Global Access**: Public IP with worldwide accessibility
- **Scalability**: Easy to upgrade when needed
- **Reliability**: Azure's enterprise-grade infrastructure

### **Long-term Benefits**
- **Cost Control**: Predictable scaling costs
- **Maintenance**: Automated updates and monitoring
- **Security**: Enterprise-grade security features
- **Integration**: Easy integration with other Azure services

---

## 🚀 **Next Steps**

1. **Create Azure Account** and set up free tier
2. **Deploy Infrastructure** using provided scripts
3. **Migrate Application** to Azure environment
4. **Test and Optimize** for production use
5. **Monitor and Maintain** ongoing operations

**SmartWatts will be live on Azure Free Tier with zero monthly costs!** 🎯
