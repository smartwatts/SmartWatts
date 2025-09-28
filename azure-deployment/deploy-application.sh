#!/bin/bash

# SmartWatts Application Deployment Script for Azure
# This script deploys the SmartWatts application to Azure VM

set -e

echo "🚀 Starting SmartWatts Application Deployment on Azure..."

# Check if configuration file exists
if [ ! -f "azure-config.env" ]; then
    echo "❌ azure-config.env not found. Please run setup-azure-infrastructure.sh first."
    exit 1
fi

# Load configuration
source azure-config.env

echo "📋 Deployment Configuration:"
echo "Resource Group: $AZURE_RESOURCE_GROUP"
echo "VM Public IP: $VM_PUBLIC_IP"
echo "SQL Server: $SQL_SERVER_NAME"
echo "SQL Database: $SQL_DB_NAME"
echo "IoT Hub: $IOT_HUB_NAME"
echo "Storage Account: $STORAGE_ACCOUNT_NAME"
echo ""

# Check if we're on the VM
if [ ! -f "/opt/smartwatts/azure-config.env" ]; then
    echo "📁 Setting up application directory..."
    sudo mkdir -p /opt/smartwatts
    sudo chown $USER:$USER /opt/smartwatts
    cp azure-config.env /opt/smartwatts/
fi

cd /opt/smartwatts

# Clone repository if not exists
if [ ! -d "smartwatts" ]; then
    echo "📥 Cloning SmartWatts repository..."
    git clone https://github.com/your-username/smartwatts.git
fi

cd smartwatts

# Copy Azure configuration files
echo "📝 Copying Azure configuration files..."
cp ../azure-deployment/docker-compose.azure.yml ./docker-compose.yml
cp ../azure-deployment/Dockerfile.azure ./Dockerfile.azure

# Copy Azure application configuration to all services
echo "⚙️ Configuring services for Azure..."
for service in backend/*/; do
    if [ -d "$service/src/main/resources" ]; then
        cp ../azure-deployment/application-azure.yml "$service/src/main/resources/"
        echo "✅ Configured $(basename $service)"
    fi
done

# Copy frontend Azure Dockerfile
cp ../azure-deployment/frontend/Dockerfile.azure ./frontend/Dockerfile.azure

# Build and start services
echo "🔨 Building and starting services..."

# Start infrastructure services first
echo "🚀 Starting infrastructure services..."
docker-compose up -d redis service-discovery

# Wait for service discovery to be ready
echo "⏳ Waiting for service discovery to be ready..."
sleep 30

# Start core services
echo "🚀 Starting core services..."
docker-compose up -d user-service energy-service device-service

# Wait for core services to be ready
echo "⏳ Waiting for core services to be ready..."
sleep 60

# Start analytics and billing services
echo "🚀 Starting analytics and billing services..."
docker-compose up -d analytics-service billing-service

# Wait for analytics services to be ready
echo "⏳ Waiting for analytics services to be ready..."
sleep 60

# Start remaining services
echo "🚀 Starting remaining services..."
docker-compose up -d notification-service edge-gateway feature-flag-service device-verification-service appliance-monitoring-service

# Wait for all services to be ready
echo "⏳ Waiting for all services to be ready..."
sleep 60

# Start frontend
echo "🚀 Starting frontend..."
docker-compose up -d frontend

# Wait for frontend to be ready
echo "⏳ Waiting for frontend to be ready..."
sleep 30

# Start API Gateway
echo "🚀 Starting API Gateway..."
docker-compose up -d api-gateway

# Wait for API Gateway to be ready
echo "⏳ Waiting for API Gateway to be ready..."
sleep 30

# Run health checks
echo "🏥 Running health checks..."
./health-check.sh

# Test application endpoints
echo "🧪 Testing application endpoints..."

# Test frontend
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ Frontend: http://$VM_PUBLIC_IP:3000"
else
    echo "❌ Frontend: Not responding"
fi

# Test API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ API Gateway: http://$VM_PUBLIC_IP:8080"
else
    echo "❌ API Gateway: Not responding"
fi

# Test service discovery
if curl -s http://localhost:8761/eureka/apps > /dev/null; then
    echo "✅ Service Discovery: http://$VM_PUBLIC_IP:8761"
else
    echo "❌ Service Discovery: Not responding"
fi

# Test individual services
services=("user-service:8081" "energy-service:8082" "device-service:8083" "analytics-service:8084" "billing-service:8085")
for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    if curl -s http://localhost:$port/actuator/health > /dev/null; then
        echo "✅ $name: http://$VM_PUBLIC_IP:$port"
    else
        echo "❌ $name: Not responding"
    fi
done

# Create systemd service
echo "⚙️ Creating systemd service..."
sudo systemctl daemon-reload
sudo systemctl enable smartwatts
sudo systemctl start smartwatts

# Setup log rotation
echo "📝 Setting up log rotation..."
sudo tee /etc/logrotate.d/smartwatts << EOF
/var/log/smartwatts/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 $USER $USER
    postrotate
        docker-compose restart
    endscript
}
EOF

# Setup monitoring
echo "📊 Setting up monitoring..."
sudo tee /opt/smartwatts/monitor.sh << EOF
#!/bin/bash
# SmartWatts Monitoring Script

echo "SmartWatts System Status - \$(date)"
echo "=================================="

# Check Docker
if systemctl is-active --quiet docker; then
    echo "✅ Docker: Running"
else
    echo "❌ Docker: Not running"
    sudo systemctl start docker
fi

# Check services
cd /opt/smartwatts/smartwatts
docker-compose ps

# Check resources
echo ""
echo "System Resources:"
echo "Memory: \$(free -h | grep Mem | awk '{print \$3\"/\"\$2}')"
echo "Disk: \$(df -h / | tail -1 | awk '{print \$3\"/\"\$2}')"
echo "CPU Load: \$(uptime | awk -F'load average:' '{print \$2}')"

# Check service health
echo ""
echo "Service Health:"
for service in user-service energy-service device-service analytics-service billing-service; do
    if docker-compose ps $service | grep -q "Up"; then
        echo "✅ $service: Running"
    else
        echo "❌ $service: Not running"
    fi
done
EOF

chmod +x /opt/smartwatts/monitor.sh

# Setup cron job for monitoring
echo "⏰ Setting up monitoring cron job..."
(crontab -l 2>/dev/null; echo "*/5 * * * * /opt/smartwatts/monitor.sh >> /var/log/smartwatts/monitor.log 2>&1") | crontab -

# Create backup script
echo "💾 Creating backup script..."
sudo tee /opt/smartwatts/backup.sh << EOF
#!/bin/bash
# SmartWatts Backup Script

BACKUP_DIR="/opt/smartwatts/backups"
DATE=\$(date +%Y%m%d_%H%M%S)

mkdir -p \$BACKUP_DIR

# Backup application data
tar -czf \$BACKUP_DIR/smartwatts_\$DATE.tar.gz /opt/smartwatts/smartwatts

# Backup logs
tar -czf \$BACKUP_DIR/logs_\$DATE.tar.gz /var/log/smartwatts

# Cleanup old backups (keep last 7 days)
find \$BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "Backup completed: \$BACKUP_DIR/smartwatts_\$DATE.tar.gz"
EOF

chmod +x /opt/smartwatts/backup.sh

# Setup backup cron job
echo "⏰ Setting up backup cron job..."
(crontab -l 2>/dev/null; echo "0 2 * * * /opt/smartwatts/backup.sh >> /var/log/smartwatts/backup.log 2>&1") | crontab -

echo ""
echo "🎉 SmartWatts Application Deployment Complete!"
echo "=============================================="
echo ""
echo "🌐 Application URLs:"
echo "Frontend: http://$VM_PUBLIC_IP:3000"
echo "API Gateway: http://$VM_PUBLIC_IP:8080"
echo "Service Discovery: http://$VM_PUBLIC_IP:8761"
echo ""
echo "🔧 Management Commands:"
echo "Check status: ./health-check.sh"
echo "View logs: docker-compose logs -f"
echo "Restart: sudo systemctl restart smartwatts"
echo "Monitor: /opt/smartwatts/monitor.sh"
echo "Backup: /opt/smartwatts/backup.sh"
echo ""
echo "📊 Monitoring:"
echo "System logs: /var/log/smartwatts/"
echo "Application logs: docker-compose logs"
echo "Health checks: Every 5 minutes"
echo "Backups: Daily at 2 AM"
echo ""
echo "✅ SmartWatts is now running on Azure Free Tier!"
echo "🎯 Cost: $0/month (within free tier limits)"
