# SmartWatts Portable Installation Guide

## Overview

This guide shows you how to create a single flash drive that contains everything needed to install and run SmartWatts on any compatible system. The flash drive will work as a complete portable installation package.

## Flash Drive Requirements

### Minimum Specifications
- **Capacity**: 32GB or larger (recommended 64GB+)
- **Type**: USB 3.0 or higher for better performance
- **Format**: FAT32 or exFAT (for cross-platform compatibility)

### Recommended Setup
- **64GB USB 3.0 Flash Drive**
- **High-speed read/write** for better performance
- **Reliable brand** (SanDisk, Samsung, Kingston)

## Flash Drive Structure

```
SmartWatts-Portable/
├── installer/
│   ├── install.sh                    # Main installation script
│   ├── install.bat                   # Windows installation script
│   ├── install-raspberry-pi.sh       # Raspberry Pi specific installer
│   └── requirements.txt              # Python dependencies
├── docker/
│   ├── docker-compose.yml            # Main Docker Compose file
│   ├── docker-compose.hardware.yml   # Hardware integration setup
│   └── docker-compose.edge.yml       # Edge gateway setup
├── config/
│   ├── environment.env               # Environment configuration
│   ├── database.env                  # Database configuration
│   └── edge-gateway.env              # Edge gateway configuration
├── scripts/
│   ├── start-smartwatts.sh           # Start all services
│   ├── stop-smartwatts.sh            # Stop all services
│   ├── health-check.sh               # System health check
│   └── backup-data.sh                # Data backup script
├── docs/
│   ├── README.md                     # Quick start guide
│   ├── Hardware_Integration_Guide.md # Hardware setup
│   └── Troubleshooting.md            # Common issues
└── data/
    ├── postgres/                     # Database data (created on first run)
    ├── redis/                        # Redis data (created on first run)
    └── edge-gateway/                 # Edge gateway data (created on first run)
```

## Installation Process

### Step 1: Prepare the Flash Drive

1. **Format the flash drive**:
   ```bash
   # On Linux/Mac
   sudo mkfs.exfat -n "SmartWatts" /dev/sdX
   
   # On Windows
   # Use Disk Management to format as exFAT
   ```

2. **Create the directory structure**:
   ```bash
   mkdir -p SmartWatts-Portable/{installer,docker,config,scripts,docs,data}
   ```

### Step 2: Copy SmartWatts Files

1. **Copy the entire SmartWatts project** to the flash drive:
   ```bash
   cp -r /path/to/mySmartWatts/* SmartWatts-Portable/
   ```

2. **Create portable installation scripts** (see below)

### Step 3: Create Installation Scripts

#### Main Installation Script (`install.sh`)

```bash
#!/bin/bash

# SmartWatts Portable Installation Script
# This script installs SmartWatts on any compatible system

set -e

echo "🚀 SmartWatts Portable Installation"
echo "=================================="

# Check system requirements
check_requirements() {
    echo "Checking system requirements..."
    
    # Check if Docker is installed
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker is not installed. Please install Docker first."
        echo "Visit: https://docs.docker.com/get-docker/"
        exit 1
    fi
    
    # Check if Docker Compose is installed
    if ! command -v docker-compose &> /dev/null; then
        echo "❌ Docker Compose is not installed. Please install Docker Compose first."
        echo "Visit: https://docs.docker.com/compose/install/"
        exit 1
    fi
    
    # Check available disk space (minimum 10GB)
    available_space=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
    if [ "$available_space" -lt 10 ]; then
        echo "❌ Insufficient disk space. At least 10GB required."
        exit 1
    fi
    
    echo "✅ System requirements met"
}

# Install SmartWatts
install_smartwatts() {
    echo "Installing SmartWatts..."
    
    # Create data directories
    mkdir -p data/{postgres,redis,edge-gateway}
    
    # Set permissions
    chmod +x scripts/*.sh
    
    # Copy environment files
    cp config/environment.env .env
    
    # Start services
    echo "Starting SmartWatts services..."
    docker-compose -f docker/docker-compose.yml up -d
    
    echo "✅ SmartWatts installed successfully!"
}

# Main installation
main() {
    check_requirements
    install_smartwatts
    
    echo ""
    echo "🎉 Installation Complete!"
    echo "========================"
    echo "SmartWatts is now running on:"
    echo "  • Dashboard: http://localhost:3000"
    echo "  • API Gateway: http://localhost:8080"
    echo "  • Edge Gateway: http://localhost:8088"
    echo ""
    echo "To start/stop services:"
    echo "  • Start: ./scripts/start-smartwatts.sh"
    echo "  • Stop: ./scripts/stop-smartwatts.sh"
    echo "  • Health Check: ./scripts/health-check.sh"
}

main "$@"
```

#### Windows Installation Script (`install.bat`)

```batch
@echo off
echo SmartWatts Portable Installation
echo ================================

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker is not installed. Please install Docker Desktop first.
    echo Visit: https://docs.docker.com/desktop/windows/install/
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker Compose is not installed. Please install Docker Compose first.
    pause
    exit /b 1
)

echo Creating data directories...
mkdir data\postgres 2>nul
mkdir data\redis 2>nul
mkdir data\edge-gateway 2>nul

echo Copying environment files...
copy config\environment.env .env

echo Starting SmartWatts services...
docker-compose -f docker\docker-compose.yml up -d

echo.
echo Installation Complete!
echo =====================
echo SmartWatts is now running on:
echo   • Dashboard: http://localhost:3000
echo   • API Gateway: http://localhost:8080
echo   • Edge Gateway: http://localhost:8088
echo.
echo To start/stop services:
echo   • Start: scripts\start-smartwatts.bat
echo   • Stop: scripts\stop-smartwatts.bat
echo   • Health Check: scripts\health-check.bat
pause
```

#### Raspberry Pi Installation Script (`install-raspberry-pi.sh`)

```bash
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
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
echo "Installing Docker Compose..."
sudo apt install docker-compose -y

# Install Python dependencies
echo "Installing Python dependencies..."
pip3 install -r installer/requirements.txt

# Install MQTT broker
echo "Installing Mosquitto MQTT broker..."
sudo apt install mosquitto mosquitto-clients -y
sudo systemctl enable mosquitto
sudo systemctl start mosquitto

# Install PostgreSQL
echo "Installing PostgreSQL..."
sudo apt install postgresql postgresql-contrib -y
sudo systemctl enable postgresql
sudo systemctl start postgresql

# Create SmartWatts user and database
echo "Setting up database..."
sudo -u postgres psql -c "CREATE USER smartwatts WITH PASSWORD 'smartwatts123';"
sudo -u postgres psql -c "CREATE DATABASE smartwatts_platform OWNER smartwatts;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE smartwatts_platform TO smartwatts;"

# Create data directories
mkdir -p data/{postgres,redis,edge-gateway}

# Set permissions
chmod +x scripts/*.sh

# Copy environment files
cp config/environment.env .env
cp config/edge-gateway.env .env.edge

# Start services
echo "Starting SmartWatts services..."
docker-compose -f docker/docker-compose.edge.yml up -d

echo "✅ SmartWatts installed successfully on Raspberry Pi!"
echo "Dashboard available at: http://$(hostname -I | awk '{print $1}'):3000"
```

### Step 4: Create Management Scripts

#### Start Script (`scripts/start-smartwatts.sh`)

```bash
#!/bin/bash

echo "🚀 Starting SmartWatts Platform..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start services
docker-compose -f docker/docker-compose.yml up -d

echo "✅ SmartWatts Platform started!"
echo "Dashboard: http://localhost:3000"
echo "API Gateway: http://localhost:8080"
echo "Edge Gateway: http://localhost:8088"
```

#### Stop Script (`scripts/stop-smartwatts.sh`)

```bash
#!/bin/bash

echo "🛑 Stopping SmartWatts Platform..."

# Stop services
docker-compose -f docker/docker-compose.yml down

echo "✅ SmartWatts Platform stopped!"
```

#### Health Check Script (`scripts/health-check.sh`)

```bash
#!/bin/bash

echo "🔍 SmartWatts Health Check"
echo "========================="

# Check Docker services
echo "Checking Docker services..."
docker-compose -f docker/docker-compose.yml ps

echo ""
echo "Checking service health..."

# Check API Gateway
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ API Gateway: UP"
else
    echo "❌ API Gateway: DOWN"
fi

# Check Dashboard
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ Dashboard: UP"
else
    echo "❌ Dashboard: DOWN"
fi

# Check Edge Gateway
if curl -s http://localhost:8088/actuator/health > /dev/null; then
    echo "✅ Edge Gateway: UP"
else
    echo "❌ Edge Gateway: DOWN"
fi

echo ""
echo "Health check complete!"
```

## Usage Instructions

### For Any System

1. **Insert the flash drive**
2. **Navigate to the flash drive**:
   ```bash
   cd /path/to/flash/drive/SmartWatts-Portable
   ```

3. **Run the installation script**:
   ```bash
   # Linux/Mac
   chmod +x installer/install.sh
   ./installer/install.sh
   
   # Windows
   installer\install.bat
   ```

4. **Access SmartWatts**:
   - Dashboard: http://localhost:3000
   - API Gateway: http://localhost:8080
   - Edge Gateway: http://localhost:8088

### For Raspberry Pi

1. **Insert the flash drive**
2. **Run the Raspberry Pi installer**:
   ```bash
   chmod +x installer/install-raspberry-pi.sh
   ./installer/install-raspberry-pi.sh
   ```

3. **Access via network**:
   - Dashboard: http://[PI_IP_ADDRESS]:3000
   - API Gateway: http://[PI_IP_ADDRESS]:8080
   - Edge Gateway: http://[PI_IP_ADDRESS]:8088

## Configuration

### Environment Variables

Create `config/environment.env`:

```env
# Database Configuration
POSTGRES_DB=smartwatts_platform
POSTGRES_USER=smartwatts
POSTGRES_PASSWORD=smartwatts123
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# MQTT Configuration
MQTT_BROKER_HOST=localhost
MQTT_BROKER_PORT=1883
MQTT_BROKER_USERNAME=
MQTT_BROKER_PASSWORD=

# Edge Gateway Configuration
EDGE_GATEWAY_PORT=8088
EDGE_ML_MODELS_PATH=./data/edge-gateway/models
EDGE_DEVICE_DATA_PATH=./data/edge-gateway/devices

# API Gateway Configuration
API_GATEWAY_PORT=8080
API_GATEWAY_RATE_LIMIT=100
API_GATEWAY_BURST_CAPACITY=200

# Frontend Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_EDGE_GATEWAY_URL=http://localhost:8088
```

## Troubleshooting

### Common Issues

1. **Docker not running**:
   ```bash
   # Start Docker service
   sudo systemctl start docker
   ```

2. **Port conflicts**:
   ```bash
   # Check what's using the ports
   sudo netstat -tulpn | grep :3000
   sudo netstat -tulpn | grep :8080
   ```

3. **Permission issues**:
   ```bash
   # Fix permissions
   chmod +x scripts/*.sh
   sudo chown -R $USER:$USER data/
   ```

4. **Database connection issues**:
   ```bash
   # Check PostgreSQL status
   sudo systemctl status postgresql
   ```

### Logs

Check service logs:
```bash
# All services
docker-compose -f docker/docker-compose.yml logs

# Specific service
docker-compose -f docker/docker-compose.yml logs api-gateway
docker-compose -f docker/docker-compose.yml logs edge-gateway
```

## Backup and Restore

### Backup Data

```bash
./scripts/backup-data.sh
```

This creates a backup in `data/backups/` with timestamp.

### Restore Data

```bash
# Restore from backup
docker-compose -f docker/docker-compose.yml down
cp -r data/backups/[BACKUP_DATE]/* data/
docker-compose -f docker/docker-compose.yml up -d
```

## Security Considerations

1. **Change default passwords** in environment files
2. **Use HTTPS** in production environments
3. **Enable firewall** rules for exposed ports
4. **Regular backups** of data directory
5. **Keep Docker images updated**

## Support

For issues and support:
1. Check the troubleshooting section
2. Review service logs
3. Check system requirements
4. Ensure all dependencies are installed

## Conclusion

This portable installation allows you to:
- **Install SmartWatts anywhere** with a single flash drive
- **No internet required** after initial setup
- **Complete offline functionality** for edge deployments
- **Easy backup and restore** capabilities
- **Cross-platform compatibility** (Linux, Windows, Mac, Raspberry Pi)

The flash drive becomes your complete SmartWatts deployment package that can be used on any compatible system!

---

**Version**: 1.0.0  
**Last Updated**: January 2025  
**Compatibility**: Linux, Windows, Mac, Raspberry Pi 5


