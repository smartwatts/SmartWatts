#!/bin/bash

# SmartWatts VM Setup Script for Azure
# This script configures the Azure VM for SmartWatts deployment

set -e

echo "🚀 Setting up SmartWatts VM on Azure..."

# Update system
echo "📦 Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install essential packages
echo "🔧 Installing essential packages..."
sudo apt install -y curl wget git unzip software-properties-common apt-transport-https ca-certificates gnupg lsb-release

# Install Docker
echo "🐳 Installing Docker..."
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
rm get-docker.sh

# Install Docker Compose
echo "🐳 Installing Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Node.js 18
echo "📦 Installing Node.js 18..."
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# Install Java 17
echo "☕ Installing Java 17..."
sudo apt install -y openjdk-17-jdk

# Install Maven
echo "📦 Installing Maven..."
sudo apt install -y maven

# Install Azure CLI
echo "☁️ Installing Azure CLI..."
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# Install Azure IoT CLI
echo "🌐 Installing Azure IoT CLI..."
sudo apt install -y azure-iot-cli-extension

# Install Nginx
echo "🌐 Installing Nginx..."
sudo apt install -y nginx

# Configure Nginx
echo "⚙️ Configuring Nginx..."
sudo tee /etc/nginx/sites-available/smartwatts << EOF
server {
    listen 80;
    server_name _;

    # Frontend
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }

    # API Gateway
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/smartwatts /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl enable nginx
sudo systemctl start nginx

# Create application directory
echo "📁 Creating application directory..."
sudo mkdir -p /opt/smartwatts
sudo chown $USER:$USER /opt/smartwatts

# Create systemd service for SmartWatts
echo "⚙️ Creating systemd service..."
sudo tee /etc/systemd/system/smartwatts.service << EOF
[Unit]
Description=SmartWatts Application
After=network.target

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/smartwatts
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
User=$USER
Group=$USER

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable smartwatts

# Create log directory
echo "📁 Creating log directory..."
sudo mkdir -p /var/log/smartwatts
sudo chown $USER:$USER /var/log/smartwatts

# Install monitoring tools
echo "📊 Installing monitoring tools..."
sudo apt install -y htop iotop nethogs

# Configure firewall
echo "🔥 Configuring firewall..."
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 3000/tcp
sudo ufw --force enable

# Create swap file (1GB)
echo "💾 Creating swap file..."
sudo fallocate -l 1G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Optimize system for B1s VM
echo "⚡ Optimizing system for B1s VM..."
echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
echo 'vm.vfs_cache_pressure=50' | sudo tee -a /etc/sysctl.conf

# Create startup script
echo "📝 Creating startup script..."
tee /opt/smartwatts/start-smartwatts.sh << EOF
#!/bin/bash
cd /opt/smartwatts
source azure-config.env
docker-compose up -d
EOF

chmod +x /opt/smartwatts/start-smartwatts.sh

# Create health check script
echo "🏥 Creating health check script..."
tee /opt/smartwatts/health-check.sh << EOF
#!/bin/bash
echo "SmartWatts Health Check - \$(date)"
echo "=================================="

# Check Docker
if systemctl is-active --quiet docker; then
    echo "✅ Docker: Running"
else
    echo "❌ Docker: Not running"
fi

# Check Nginx
if systemctl is-active --quiet nginx; then
    echo "✅ Nginx: Running"
else
    echo "❌ Nginx: Not running"
fi

# Check Frontend
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ Frontend: Running"
else
    echo "❌ Frontend: Not running"
fi

# Check API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ API Gateway: Running"
else
    echo "❌ API Gateway: Not running"
fi

# Check system resources
echo ""
echo "System Resources:"
echo "Memory: \$(free -h | grep Mem | awk '{print \$3\"/\"\$2}')"
echo "Disk: \$(df -h / | tail -1 | awk '{print \$3\"/\"\$2}')"
echo "CPU Load: \$(uptime | awk -F'load average:' '{print \$2}')"
EOF

chmod +x /opt/smartwatts/health-check.sh

echo ""
echo "✅ VM setup complete!"
echo "===================="
echo "Next steps:"
echo "1. Clone SmartWatts repository to /opt/smartwatts"
echo "2. Copy azure-config.env to /opt/smartwatts/"
echo "3. Run: ./deploy-application.sh"
echo ""
echo "Useful commands:"
echo "- Check status: ./health-check.sh"
echo "- View logs: docker-compose logs -f"
echo "- Restart: sudo systemctl restart smartwatts"
