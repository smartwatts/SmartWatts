# SmartWatts Azure Free Tier Deployment

## 🎯 Overview

This directory contains all the necessary files and scripts to deploy SmartWatts to Azure Free Tier, achieving **$0/month cost** while maintaining full functionality.

## 📋 What's Included

### Infrastructure Scripts
- `setup-azure-infrastructure.sh` - Creates all Azure resources
- `setup-vm.sh` - Configures the Azure VM
- `deploy-application.sh` - Deploys the application

### Configuration Files
- `docker-compose.azure.yml` - Docker Compose for Azure deployment
- `Dockerfile.azure` - Multi-stage Dockerfile for services
- `application-azure.yml` - Azure-specific application configuration

### Database Migration
- `sql-migration/01-create-databases.sql` - Azure SQL Database schema

### Documentation
- `AZURE_DEPLOYMENT_PLAN.md` - Comprehensive deployment plan
- `azure-iot-integration.md` - Azure IoT Hub integration guide
- `README.md` - This file

## 🚀 Quick Start

### Prerequisites
1. **Azure Account** with free tier available
2. **Azure CLI** installed and configured
3. **SSH access** to the Azure VM

### Step 1: Create Azure Infrastructure
```bash
# Make scripts executable
chmod +x *.sh

# Create all Azure resources
./setup-azure-infrastructure.sh
```

### Step 2: Configure VM
```bash
# SSH into the VM
ssh azureuser@<VM_PUBLIC_IP>

# Run VM setup
./setup-vm.sh
```

### Step 3: Deploy Application
```bash
# Deploy SmartWatts
./deploy-application.sh
```

## 🏗️ Architecture

### Azure Resources (Hybrid Architecture)
- **VM**: B1s (1 vCPU, 1 GB RAM) - 750 hours/month free
  - Runs: Spring Boot services + PostgreSQL + Redis + Frontend
- **PostgreSQL**: Docker container on VM (9 databases)
  - Uses VM disk space (30 GB SSD)
  - No separate database cost
- **IoT Hub**: Free tier - 8,000 messages/day (optional)
- **Storage Account**: 5 GB free (optional)

### Application Stack
- **Frontend**: Next.js 14 + React 18
- **Backend**: Spring Boot 3.x microservices (13 services)
- **Database**: PostgreSQL 15 (Docker container on VM)
- **Cache**: Redis (Docker container on VM)
- **IoT**: Azure IoT Hub (optional, for device ingestion)
- **Storage**: Azure Blob Storage (optional, for file storage)

**Hybrid Approach**: Keep production-ready Spring Boot services, use Azure free tier for IoT and storage.

## 💰 Cost Analysis

### Free Tier Usage
- **VM**: 750 hours/month (24/7 = 744 hours) ✅
  - Runs all services: Spring Boot + PostgreSQL + Redis + Frontend
- **PostgreSQL**: Included in VM (uses VM disk space) ✅
  - 9 databases, estimated ~10-20 GB total
- **IoT Hub**: 8,000 messages/day (optional) ✅
- **Blob Storage**: 5 GB (optional) ✅

**Total Cost**: $0/month

**Note**: PostgreSQL runs on the VM in Docker, same as current setup. No database migration needed.

## 🔧 Configuration

### Environment Variables
All configuration is managed through `azure-config.env`:

```bash
# Azure Configuration
AZURE_RESOURCE_GROUP=smartwatts-rg
VM_PUBLIC_IP=<VM_IP>
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
IOT_HUB_CONNECTION_STRING=<IOT_HUB_CONNECTION_STRING>  # Optional
STORAGE_CONNECTION_STRING=<STORAGE_CONNECTION_STRING>  # Optional
```

### Service Configuration
Each service uses `application-azure.yml` for Azure-specific settings:
- Database connection to PostgreSQL (same as current setup)
- Redis configuration
- Azure IoT Hub integration (optional, for edge gateway)
- Performance optimizations for B1s VM

## 📊 Monitoring

### Health Checks
- **Application**: `./health-check.sh`
- **System**: `./monitor.sh`
- **Logs**: `docker-compose logs -f`

### Logs
- **Application**: `/var/log/smartwatts/`
- **Docker**: `docker-compose logs`
- **System**: `/var/log/syslog`

### Metrics
- **Azure Portal**: Built-in metrics
- **Application**: Prometheus endpoints
- **Custom**: SmartWatts dashboards

## 🔒 Security

### Network Security
- **NSG Rules**: Minimal open ports
- **Firewall**: UFW configured
- **SSL/TLS**: Let's Encrypt certificates

### Application Security
- **JWT Authentication**: Secure token system
- **CORS**: Properly configured
- **Input Validation**: All inputs validated

### Database Security
- **Azure SQL Firewall**: VM IP only
- **Encryption**: TLS 1.2+ required
- **Authentication**: Strong passwords

## 🚨 Troubleshooting

### Common Issues

#### 1. Services Not Starting
```bash
# Check Docker status
sudo systemctl status docker

# Check service logs
docker-compose logs <service-name>

# Restart services
sudo systemctl restart smartwatts
```

#### 2. Database Connection Issues
```bash
# Check PostgreSQL container status
docker ps | grep postgres

# Check PostgreSQL logs
docker logs postgres

# Test PostgreSQL connection
docker exec -it postgres psql -U postgres -d smartwatts_users

# List all databases
docker exec -it postgres psql -U postgres -c "\l"
```

#### 3. IoT Hub Issues
```bash
# Check device status
az iot hub device-identity show \
  --hub-name smartwatts-iot-hub \
  --device-id smartwatts-gateway

# Monitor messages
az iot hub monitor-events \
  --hub-name smartwatts-iot-hub
```

### Debug Commands
```bash
# System status
./health-check.sh

# Resource usage
htop

# Network connectivity
ping google.com

# Service discovery
curl http://localhost:8761/eureka/apps
```

## 📈 Scaling

### Vertical Scaling
- Upgrade to B2s (2 vCPU, 4 GB RAM)
- Increase VM disk size if PostgreSQL databases grow
- Optimize PostgreSQL configuration for better performance

### Horizontal Scaling
- Add more VMs behind load balancer
- Use Azure Container Instances
- Implement Azure Functions

## 🔄 Maintenance

### Daily Tasks
- Monitor system health
- Check log files
- Verify service status

### Weekly Tasks
- Review performance metrics
- Check backup status
- Update security patches

### Monthly Tasks
- Review costs
- Optimize performance
- Plan scaling

## 📚 Additional Resources

### Azure Documentation
- [Azure Free Tier](https://azure.microsoft.com/en-us/free/)
- [Azure SQL Database](https://docs.microsoft.com/en-us/azure/azure-sql/)
- [Azure IoT Hub](https://docs.microsoft.com/en-us/azure/iot-hub/)

### SmartWatts Documentation
- [Consumer-Grade Features](../CONSUMER_GRADE_FEATURES.md)
- [Phase 1 Implementation](../PHASE1_IMPLEMENTATION_SUMMARY.md)
- [API Documentation](../backend/api-docs-service/)

## 🎉 Success Criteria

### Must Have
- [ ] All services running on Azure VM
- [ ] PostgreSQL container running with 9 databases
- [ ] Spring Boot services connected to PostgreSQL
- [ ] Frontend accessible via public IP
- [ ] All consumer-grade features functional
- [ ] Azure IoT Hub integrated (optional, for edge gateway)

### Should Have
- [ ] SSL certificates configured
- [ ] Monitoring and alerting set up
- [ ] Backup strategy implemented
- [ ] Performance optimized

### Could Have
- [ ] Auto-scaling configured
- [ ] CDN integration
- [ ] Advanced monitoring dashboards
- [ ] Disaster recovery plan

## 🚀 Next Steps

1. **Deploy to Azure** using provided scripts
2. **Test all features** end-to-end
3. **Monitor performance** and optimize
4. **Scale as needed** for production

**SmartWatts will be live on Azure Free Tier with zero monthly costs!** 🎯

## 📞 Support

For issues or questions:
1. Check the troubleshooting section
2. Review the logs
3. Check Azure Portal for resource status
4. Contact the development team

---

**SmartWatts - Revolutionizing Energy Monitoring in Nigeria and Africa** 🇳🇬⚡
