# Raspberry Pi 5 Initial Setup Guide

## Overview

This guide walks you through setting up a fresh Raspberry Pi 5 with Raspberry Pi OS and all necessary dependencies for SmartWatts, without installing SmartWatts yet.

## What You'll Install

1. **Raspberry Pi OS 64-bit Lite** (recommended for headless operation)
2. **Docker & Docker Compose** (for containerized services)
3. **Python 3.11+** (for edge gateway and ML)
4. **MQTT Broker (Mosquitto)** (for IoT communication)
5. **PostgreSQL** (for database)
6. **Essential tools** (git, curl, wget, etc.)

## Prerequisites

- Raspberry Pi 5 (4GB or 8GB recommended)
- MicroSD card (32GB+ recommended, Class 10 or better)
- Power supply (27W USB-C PD recommended)
- Ethernet cable or WiFi access
- Computer for flashing SD card

## Step 1: Flash Raspberry Pi OS

### Download Raspberry Pi Imager
1. Go to https://www.raspberrypi.org/downloads/
2. Download **Raspberry Pi Imager** for your computer
3. Install and open the application

### Flash the OS
1. **Select OS**: Choose "Raspberry Pi OS Lite (64-bit)"
2. **Select Storage**: Choose your microSD card
3. **Configure** (gear icon):
   - **Hostname**: `smartwatts-pi` (or your preferred name)
   - **Enable SSH**: ✅ (with password authentication)
   - **Username**: `pi` (or your preferred username)
   - **Password**: Set a strong password
   - **WiFi**: Configure if using WiFi
   - **Locale**: Set your timezone and keyboard layout
4. **Write** the image to the SD card

## Step 2: Initial Pi Setup

### Boot and Connect
1. Insert the SD card into your Raspberry Pi 5
2. Connect power supply and Ethernet (or ensure WiFi is configured)
3. Wait for the Pi to boot (green LED should stop blinking)

### Connect via SSH
```bash
# Find the Pi's IP address (if you don't know it)
# Check your router's admin panel or use:
nmap -sn 192.168.1.0/24

# Connect via SSH
ssh pi@[PI_IP_ADDRESS]
# or
ssh pi@smartwatts-pi.local
```

### Initial System Update
```bash
# Update package lists
sudo apt update

# Upgrade all packages
sudo apt upgrade -y

# Install essential tools
sudo apt install -y git curl wget vim htop tree unzip

# Reboot to ensure all updates are applied
sudo reboot
```

## Step 3: Install Docker

### Install Docker
```bash
# Download and run Docker installation script
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to the docker group
sudo usermod -aG docker $USER

# Verify Docker installation
docker --version
docker run hello-world

# Clean up
rm get-docker.sh
```

### Install Docker Compose
```bash
# Install Docker Compose
sudo apt install docker-compose -y

# Verify installation
docker-compose --version
```

## Step 4: Install Python 3.11+

### Check Current Python Version
```bash
python3 --version
```

### Install Python 3.11 (if needed)
```bash
# Add deadsnakes PPA for newer Python versions
sudo apt install -y software-properties-common
sudo add-apt-repository ppa:deadsnakes/ppa -y
sudo apt update

# Install Python 3.11
sudo apt install -y python3.11 python3.11-venv python3.11-pip

# Set Python 3.11 as default (optional)
sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 1

# Verify installation
python3 --version
pip3 --version
```

## Step 5: Install MQTT Broker (Mosquitto)

### Install Mosquitto
```bash
# Install Mosquitto MQTT broker
sudo apt install -y mosquitto mosquitto-clients

# Enable and start the service
sudo systemctl enable mosquitto
sudo systemctl start mosquitto

# Check status
sudo systemctl status mosquitto
```

### Configure Mosquitto (Optional)
```bash
# Edit configuration
sudo nano /etc/mosquitto/mosquitto.conf

# Add these lines for basic security:
# allow_anonymous false
# password_file /etc/mosquitto/passwd

# Create password file (if you added the above config)
# sudo mosquitto_passwd -c /etc/mosquitto/passwd smartwatts
# sudo chmod 600 /etc/mosquitto/passwd

# Restart Mosquitto
sudo systemctl restart mosquitto
```

### Test MQTT
```bash
# Test MQTT broker
mosquitto_pub -h localhost -t "test/topic" -m "Hello MQTT"
mosquitto_sub -h localhost -t "test/topic"
```

## Step 6: Install PostgreSQL

### Install PostgreSQL
```bash
# Install PostgreSQL
sudo apt install -y postgresql postgresql-contrib

# Enable and start the service
sudo systemctl enable postgresql
sudo systemctl start postgresql

# Check status
sudo systemctl status postgresql
```

### Configure PostgreSQL
```bash
# Switch to postgres user
sudo -u postgres psql

# Create SmartWatts user and database
CREATE USER smartwatts WITH PASSWORD 'smartwatts123';
CREATE DATABASE smartwatts_platform OWNER smartwatts;
GRANT ALL PRIVILEGES ON DATABASE smartwatts_platform TO smartwatts;

# Exit PostgreSQL
\q
```

### Test PostgreSQL
```bash
# Test connection
psql -h localhost -U smartwatts -d smartwatts_platform -c "SELECT version();"
```

## Step 7: Install Additional Dependencies

### Install Java 17 (for Spring Boot services)
```bash
# Install OpenJDK 17
sudo apt install -y openjdk-17-jdk

# Verify installation
java --version
javac --version
```

### Install Node.js (for frontend)
```bash
# Install Node.js 18.x
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node --version
npm --version
```

### Install Additional Tools
```bash
# Install additional useful tools
sudo apt install -y \
    jq \
    net-tools \
    iotop \
    nethogs \
    tmux \
    screen \
    rsync \
    ufw

# Install Python packages for ML and IoT
pip3 install --user \
    numpy \
    pandas \
    paho-mqtt \
    pyserial \
    requests \
    flask \
    fastapi \
    uvicorn
```

## Step 8: Configure System Settings

### Enable I2C and SPI (for hardware interfaces)
```bash
# Enable I2C and SPI
sudo raspi-config

# Navigate to:
# 3 Interface Options
#   P4 I2C -> Enable
#   P5 SPI -> Enable
#   P6 Serial Port -> Enable (for RS485)

# Or use command line:
sudo raspi-config nonint do_i2c 0
sudo raspi-config nonint do_spi 0
sudo raspi-config nonint do_serial 0
```

### Configure Firewall (Optional)
```bash
# Enable UFW firewall
sudo ufw enable

# Allow SSH
sudo ufw allow ssh

# Allow SmartWatts ports (for later)
sudo ufw allow 3000  # Frontend
sudo ufw allow 8080  # API Gateway
sudo ufw allow 8088  # Edge Gateway
sudo ufw allow 1883  # MQTT

# Check status
sudo ufw status
```

### Set Static IP (Optional but Recommended)
```bash
# Edit network configuration
sudo nano /etc/dhcpcd.conf

# Add at the end (adjust for your network):
# interface eth0
# static ip_address=192.168.1.100/24
# static routers=192.168.1.1
# static domain_name_servers=192.168.1.1 8.8.8.8

# Restart networking
sudo systemctl restart dhcpcd
```

## Step 9: Create SmartWatts User and Directories

### Create SmartWatts User
```bash
# Create dedicated user for SmartWatts
sudo useradd -m -s /bin/bash smartwatts
sudo usermod -aG docker smartwatts
sudo usermod -aG dialout smartwatts  # For serial port access

# Set password
sudo passwd smartwatts
```

### Create Directory Structure
```bash
# Create SmartWatts directories
sudo mkdir -p /opt/smartwatts/{data,logs,config,backups}
sudo chown -R smartwatts:smartwatts /opt/smartwatts

# Create data subdirectories
mkdir -p /opt/smartwatts/data/{postgres,redis,edge-gateway,models}
```

## Step 10: Verify Installation

### Run System Check
```bash
# Create a system check script
cat > check_system.sh << 'EOF'
#!/bin/bash

echo "🔍 Raspberry Pi 5 System Check"
echo "=============================="

echo "System Information:"
echo "  OS: $(lsb_release -d | cut -f2)"
echo "  Kernel: $(uname -r)"
echo "  Architecture: $(uname -m)"
echo "  Uptime: $(uptime -p)"
echo "  Memory: $(free -h | awk 'NR==2{print $3 "/" $2}')"
echo "  Disk: $(df -h / | awk 'NR==2{print $3 "/" $2 " (" $5 " used)"}')"
echo "  Temperature: $(vcgencmd measure_temp)"

echo ""
echo "Installed Software:"
echo "  Docker: $(docker --version 2>/dev/null || echo 'Not installed')"
echo "  Docker Compose: $(docker-compose --version 2>/dev/null || echo 'Not installed')"
echo "  Python: $(python3 --version 2>/dev/null || echo 'Not installed')"
echo "  Java: $(java --version 2>/dev/null | head -n1 || echo 'Not installed')"
echo "  Node.js: $(node --version 2>/dev/null || echo 'Not installed')"
echo "  PostgreSQL: $(psql --version 2>/dev/null || echo 'Not installed')"

echo ""
echo "Services Status:"
echo "  Mosquitto: $(systemctl is-active mosquitto 2>/dev/null || echo 'Not running')"
echo "  PostgreSQL: $(systemctl is-active postgresql 2>/dev/null || echo 'Not running')"
echo "  Docker: $(systemctl is-active docker 2>/dev/null || echo 'Not running')"

echo ""
echo "Network:"
echo "  IP Address: $(hostname -I | awk '{print $1}')"
echo "  Hostname: $(hostname)"

echo ""
echo "Hardware Interfaces:"
echo "  I2C: $(ls /dev/i2c-* 2>/dev/null | wc -l) devices"
echo "  SPI: $(ls /dev/spi-* 2>/dev/null | wc -l) devices"
echo "  Serial: $(ls /dev/ttyUSB* /dev/ttyACM* 2>/dev/null | wc -l) devices"

echo ""
echo "✅ System check complete!"
EOF

chmod +x check_system.sh
./check_system.sh
```

## Step 11: Performance Optimization

### Optimize for SmartWatts
```bash
# Increase file descriptor limits
echo "* soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "* hard nofile 65536" | sudo tee -a /etc/security/limits.conf

# Optimize memory settings
echo "vm.swappiness=10" | sudo tee -a /etc/sysctl.conf
echo "vm.vfs_cache_pressure=50" | sudo tee -a /etc/sysctl.conf

# Apply settings
sudo sysctl -p
```

### Configure Log Rotation
```bash
# Create logrotate configuration for SmartWatts
sudo tee /etc/logrotate.d/smartwatts << 'EOF'
/opt/smartwatts/logs/*.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    notifempty
    create 644 smartwatts smartwatts
}
EOF
```

## Step 12: Final Verification

### Test All Components
```bash
# Test Docker
docker run hello-world

# Test MQTT
mosquitto_pub -h localhost -t "test/setup" -m "Raspberry Pi setup complete"
mosquitto_sub -h localhost -t "test/setup" -C 1

# Test PostgreSQL
psql -h localhost -U smartwatts -d smartwatts_platform -c "SELECT 'PostgreSQL working' as status;"

# Test Python
python3 -c "import paho.mqtt.client; print('Python MQTT client available')"

# Test Java
java -version
```

## Next Steps

Your Raspberry Pi 5 is now ready for SmartWatts installation! The system has:

✅ **Raspberry Pi OS 64-bit Lite**  
✅ **Docker & Docker Compose**  
✅ **Python 3.11+ with ML libraries**  
✅ **MQTT Broker (Mosquitto)**  
✅ **PostgreSQL Database**  
✅ **Java 17 for Spring Boot**  
✅ **Node.js for frontend**  
✅ **Hardware interfaces enabled**  
✅ **Optimized for SmartWatts**  

When you're ready to install SmartWatts, you can:
1. Copy the SmartWatts files to the Pi
2. Run the Raspberry Pi installation script
3. Connect your energy monitoring hardware

## Troubleshooting

### Common Issues

**Docker permission denied:**
```bash
# Log out and back in, or run:
newgrp docker
```

**MQTT not working:**
```bash
# Check Mosquitto status
sudo systemctl status mosquitto
sudo journalctl -u mosquitto
```

**PostgreSQL connection failed:**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql
sudo -u postgres psql -c "SELECT 1;"
```

**Serial port access denied:**
```bash
# Add user to dialout group
sudo usermod -aG dialout $USER
# Log out and back in
```

---

**Your Raspberry Pi 5 is now ready for SmartWatts!** 🍓⚡
