#!/bin/bash

# SmartWatts Raspberry Pi Installation Script
# Optimized for Raspberry Pi 5 with ARM64 architecture

set -e

echo "🍓 SmartWatts Raspberry Pi Installation"
echo "======================================"

# Update system
echo "Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Docker
echo "Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    echo "✅ Docker installed successfully"
else
    echo "✅ Docker already installed"
fi

# Install Docker Compose
echo "Installing Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    sudo apt install docker-compose -y
    echo "✅ Docker Compose installed successfully"
else
    echo "✅ Docker Compose already installed"
fi

# Install Python dependencies
echo "Installing Python dependencies..."
if [ -f requirements.txt ]; then
    pip3 install -r requirements.txt
    echo "✅ Python dependencies installed"
else
    echo "⚠️  requirements.txt not found, skipping Python dependencies"
fi

# Install MQTT broker
echo "Installing Mosquitto MQTT broker..."
if ! systemctl is-active --quiet mosquitto; then
    sudo apt install mosquitto mosquitto-clients -y
    sudo systemctl enable mosquitto
    sudo systemctl start mosquitto
    echo "✅ Mosquitto MQTT broker installed and started"
else
    echo "✅ Mosquitto MQTT broker already running"
fi

# Install PostgreSQL
echo "Installing PostgreSQL..."
if ! systemctl is-active --quiet postgresql; then
    sudo apt install postgresql postgresql-contrib -y
    sudo systemctl enable postgresql
    sudo systemctl start postgresql
    echo "✅ PostgreSQL installed and started"
else
    echo "✅ PostgreSQL already running"
fi

# Create SmartWatts user and database
echo "Setting up database..."
sudo -u postgres psql -c "CREATE USER smartwatts WITH PASSWORD 'smartwatts123';" 2>/dev/null || echo "User smartwatts already exists"
sudo -u postgres psql -c "CREATE DATABASE smartwatts_platform OWNER smartwatts;" 2>/dev/null || echo "Database smartwatts_platform already exists"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE smartwatts_platform TO smartwatts;"

# Create data directories
mkdir -p data/{postgres,redis,edge-gateway,backups}

# Set permissions
chmod +x scripts/*.sh

# Copy environment files
if [ ! -f .env ]; then
    cp config/environment.env .env
    echo "✅ Environment configuration created"
fi

# Get Raspberry Pi IP address
PI_IP=$(hostname -I | awk '{print $1}')

# Update environment for Raspberry Pi
echo "Updating configuration for Raspberry Pi..."
sed -i "s/localhost/$PI_IP/g" .env

# Start services
echo "Starting SmartWatts services..."
docker-compose -f docker-compose.yml up -d

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 45

# Check service health
echo "Checking service health..."
./scripts/health-check.sh

echo "✅ SmartWatts installed successfully on Raspberry Pi!"
echo ""
echo "🎉 Installation Complete!"
echo "========================"
echo "SmartWatts is now running on:"
echo "  • Dashboard: http://$PI_IP:3000"
echo "  • API Gateway: http://$PI_IP:8080"
echo "  • Edge Gateway: http://$PI_IP:8088"
echo ""
echo "To manage services:"
echo "  • Start: ./scripts/start-smartwatts.sh"
echo "  • Stop: ./scripts/stop-smartwatts.sh"
echo "  • Health Check: ./scripts/health-check.sh"
echo "  • Backup: ./scripts/backup-data.sh"
echo ""
echo "For hardware integration, see:"
echo "  • docs/Hardware_Integration_Guide.md"
echo "  • docs/Raspberry_Pi_5_Setup_Guide.md"
echo ""
echo "Note: You may need to log out and back in for Docker group changes to take effect."


