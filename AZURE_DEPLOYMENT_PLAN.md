# SmartWatts Azure Free Tier Deployment Plan

## 🎯 **Deployment Overview**

**Target**: Azure Free Tier (Barebones)  
**Cost**: $0/month  
**Resources**: B1s VM, Azure SQL, IoT Hub, Blob Storage  

---

## 📋 **Azure Free Tier Resources**

### **Compute**
- **VM Size**: B1s (1 vCPU, 1 GB RAM)
- **Hours**: 750 free hours/month
- **OS**: Ubuntu 20.04 LTS
- **Storage**: 30 GB SSD

### **Database**
- **Service**: Azure SQL Database
- **Tier**: Basic (S0)
- **Storage**: 250 GB free
- **DTU**: 10 DTUs

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

### **Single VM Deployment**
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
│  │  │   Ports 8081-8088           │ │ │
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
│        Azure SQL Database           │
│     (Replaces PostgreSQL)           │
└─────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│         Azure IoT Hub               │
│    (Replaces MQTT Broker)           │
└─────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────┐
│       Azure Blob Storage            │
│    (File Storage & Backups)         │
└─────────────────────────────────────┘
```

---

## 🔧 **Implementation Steps**

### **Phase 1: Azure Infrastructure Setup**
1. **Create Resource Group**
2. **Deploy B1s VM with Ubuntu 20.04**
3. **Create Azure SQL Database**
4. **Set up Azure IoT Hub**
5. **Create Storage Account**

### **Phase 2: Database Migration**
1. **Convert PostgreSQL schemas to Azure SQL**
2. **Update connection strings**
3. **Migrate data (if any)**
4. **Test database connectivity**

### **Phase 3: Application Deployment**
1. **Install Docker on VM**
2. **Deploy application containers**
3. **Configure Azure IoT Hub integration**
4. **Set up monitoring and logging**

### **Phase 4: Testing & Optimization**
1. **Test all services**
2. **Verify consumer-grade features**
3. **Performance optimization**
4. **Security hardening**

---

## 💰 **Cost Analysis**

### **Free Tier Usage**
- **VM**: 750 hours/month (24/7 = 744 hours) ✅
- **SQL Database**: 250 GB (estimated usage: ~50 GB) ✅
- **IoT Hub**: 8,000 messages/day (6 sites × 1 msg/65s = ~8,000/day) ✅
- **Blob Storage**: 5 GB (estimated usage: ~2 GB) ✅

### **Potential Overages**
- **VM**: $0.0052/hour if over 750 hours
- **SQL Database**: $0.0208/GB/month if over 250 GB
- **IoT Hub**: $0.10/1M messages if over 8,000/day
- **Blob Storage**: $0.0184/GB/month if over 5 GB

**Total Estimated Cost**: $0/month (within free tier limits)

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

# Create SQL Database
az sql server create \
  --resource-group smartwatts-rg \
  --name smartwatts-sql-server \
  --admin-user smartwattsadmin \
  --admin-password SmartWatts2025!

az sql db create \
  --resource-group smartwatts-rg \
  --server smartwatts-sql-server \
  --name smartwatts-db \
  --service-objective Basic

# Create IoT Hub
az iot hub create \
  --resource-group smartwatts-rg \
  --name smartwatts-iot-hub \
  --sku F1

# Create Storage Account
az storage account create \
  --resource-group smartwatts-rg \
  --name smartwattsstorage \
  --sku Standard_LRS
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
- **Azure SQL Firewall**: Restrict to VM IP only
- **Connection Encryption**: TLS 1.2+ required
- **Authentication**: SQL authentication with strong passwords

### **Application Security**
- **JWT Tokens**: Secure token generation and validation
- **CORS**: Properly configured for production
- **Input Validation**: All inputs validated and sanitized

---

## 📊 **Monitoring & Logging**

### **Azure Monitor**
- **VM Metrics**: CPU, Memory, Disk usage
- **SQL Metrics**: DTU usage, connection count
- **IoT Hub Metrics**: Message count, device count

### **Application Logging**
- **Log Files**: Centralized logging to Azure Blob Storage
- **Error Tracking**: Application Insights integration
- **Performance**: Response time monitoring

---

## 🎯 **Success Criteria**

### **Must Have**
- [ ] All services running on Azure VM
- [ ] Azure SQL Database connected and working
- [ ] Azure IoT Hub integrated for device communication
- [ ] Frontend accessible via public IP
- [ ] All consumer-grade features functional

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
- **SQL Storage**: Monitor database size
- **IoT Messages**: Optimize message frequency
- **Blob Storage**: Compress and clean up old files

---

## 📈 **Scaling Strategy**

### **Vertical Scaling**
- Upgrade to B2s (2 vCPU, 4 GB RAM) if needed
- Upgrade SQL Database tier if required

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
