# SmartWatts Edge Gateway Installation Guide
## Complete Step-by-Step Guide for Beginners

**Version:** 1.0  
**Date:** January 2025  
**Target Hardware:** R501 RK3588 Edge AI Box (and other edge devices)

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Hardware Requirements](#hardware-requirements)
3. [Software Requirements](#software-requirements)
4. [Installation Methods](#installation-methods)
5. [Method 1: Direct Installation (Recommended)](#method-1-direct-installation-recommended)
6. [Method 2: Docker Installation](#method-2-docker-installation)
7. [Configuration](#configuration)
8. [Testing the Installation](#testing-the-installation)
9. [Connecting Devices](#connecting-devices)
10. [Troubleshooting](#troubleshooting)
11. [Maintenance](#maintenance)
12. [Support](#support)

---

## Prerequisites

### What You Need to Know
- Basic understanding of Linux commands
- Access to your edge device (R501 RK3588 or similar)
- Internet connection for downloading software
- Basic understanding of energy monitoring concepts

### What You Don't Need to Know
- Advanced programming
- Complex system administration
- Deep networking knowledge

---

## Hardware Requirements

### Minimum Requirements
- **CPU:** 4 cores (ARM64 or x86_64)
- **RAM:** 4GB
- **Storage:** 32GB microSD card or eMMC
- **Network:** Ethernet or WiFi
- **Power:** 5V/3A power supply

### Recommended Requirements
- **CPU:** 8 cores (ARM64 or x86_64)
- **RAM:** 8GB
- **Storage:** 64GB+ microSD card or eMMC
- **Network:** Gigabit Ethernet
- **Power:** 5V/5A power supply

### Supported Devices
- ✅ R501 RK3588 Edge AI Box
- ✅ Raspberry Pi 4/5
- ✅ Orange Pi 5/5 Plus
- ✅ Jetson Nano/Orin
- ✅ Intel NUC
- ✅ BeagleBone
- ✅ Any Linux-based edge device

---

## Software Requirements

### Operating System
- **Ubuntu Server 20.04 LTS** or newer
- **Raspberry Pi OS** (for Raspberry Pi)
- **Any Debian-based Linux distribution**

### Required Software
- Python 3.11+
- Git
- Docker (optional)
- MQTT Broker (Mosquitto)
- SQLite3

---

## Installation Methods

You have two options for installation:

1. **Direct Installation** (Recommended for beginners)
2. **Docker Installation** (For advanced users)

---

## Method 1: Direct Installation (Recommended)

### Step 1: Prepare Your Edge Device

#### 1.1 Connect to Your Device
```bash
# If using SSH (recommended)
ssh username@your-device-ip

# If using direct connection
# Connect keyboard, mouse, and monitor to your device
```

#### 1.2 Update Your System
```bash
# Update package lists
sudo apt update

# Upgrade existing packages
sudo apt upgrade -y

# Install essential tools
sudo apt install -y curl wget git vim nano htop
```

#### 1.3 Check Your Hardware
```bash
# Check CPU information
lscpu

# Check memory
free -h

# Check storage
df -h

# Check network
ip addr show
```

**Expected Output:**
```
CPU: 8 cores (for R501 RK3588)
Memory: 8GB available
Storage: At least 32GB free space
Network: Ethernet or WiFi connected
```

### Step 2: Download SmartWatts Edge Gateway

#### 2.1 Create Installation Directory
```bash
# Create directory for SmartWatts
sudo mkdir -p /opt/smartwatts
sudo chown $USER:$USER /opt/smartwatts
cd /opt/smartwatts
```

#### 2.2 Download the Software
```bash
# Method A: If you have the files on your computer
# Copy the edge-gateway folder to your device using SCP:
# scp -r edge-gateway/ username@your-device-ip:/opt/smartwatts/

# Method B: If downloading from repository
git clone https://github.com/your-repo/smartwatts-edge-gateway.git
cd smartwatts-edge-gateway
```

#### 2.3 Verify Files
```bash
# Check that all files are present
ls -la

# You should see:
# - main.py
# - requirements.txt
# - config/
# - services/
# - deploy/
# - README.md
```

### Step 3: Run the Installation Script

#### 3.1 Make Script Executable
```bash
# Navigate to the edge-gateway directory
cd edge-gateway

# Make installation script executable
sudo chmod +x deploy/install.sh
```

#### 3.2 Run the Installation
```bash
# Run the installation script
sudo ./deploy/install.sh
```

**What the script does:**
- Installs all required software packages
- Creates a dedicated user account
- Sets up the MQTT broker
- Installs Python dependencies
- Creates systemd service
- Configures logging
- Sets up backup system

#### 3.3 Monitor Installation Progress
The installation will show progress messages like:
```
🚀 SmartWatts Edge Gateway Installation for R501 RK3588
============================================================
📦 Updating system packages...
📦 Installing system dependencies...
👤 Creating service user...
📁 Creating installation directory...
📥 Installing SmartWatts Edge Gateway...
🐍 Creating Python virtual environment...
📦 Installing Python dependencies...
📡 Configuring MQTT broker...
⚙️ Creating systemd service...
✅ SmartWatts Edge Gateway started successfully!
```

### Step 4: Verify Installation

#### 4.1 Check Service Status
```bash
# Check if the service is running
sudo systemctl status smartwatts-edge
```

**Expected Output:**
```
● smartwatts-edge.service - SmartWatts Edge Gateway
   Loaded: loaded (/etc/systemd/system/smartwatts-edge.service; enabled)
   Active: active (running) since [timestamp]
   Main PID: [process_id]
   Tasks: [number] (limit: [limit])
   Memory: [memory_usage]
   CGroup: /system.slice/smartwatts-edge.service
```

#### 4.2 Check API Health
```bash
# Test the API health endpoint
curl http://localhost:8080/api/v1/health
```

**Expected Output:**
```json
{
  "status": "healthy",
  "timestamp": "2025-01-XX...",
  "version": "1.0.0",
  "hardware": "R501_RK3588",
  "services": {
    "mqtt": true,
    "modbus": true,
    "storage": true,
    "device_discovery": true,
    "ai_inference": true,
    "data_sync": true
  }
}
```

#### 4.3 Check MQTT Broker
```bash
# Test MQTT broker
mosquitto_pub -h localhost -t test -m "Hello World"
mosquitto_sub -h localhost -t test
```

**Expected Output:**
```
Hello World
```

### Step 5: Access the Web Interface

#### 5.1 Open Web Browser
Open your web browser and navigate to:
- **API Health:** http://your-device-ip:8080/api/v1/health
- **API Status:** http://your-device-ip:8080/api/v1/status
- **Prometheus Metrics:** http://your-device-ip:9090/metrics

#### 5.2 Test API Endpoints
```bash
# Get system status
curl http://localhost:8080/api/v1/status

# Get device list
curl http://localhost:8080/api/v1/devices

# Get MQTT statistics
curl http://localhost:8080/api/v1/mqtt/stats
```

---

## Method 2: Docker Installation

### Step 1: Install Docker

#### 1.1 Install Docker
```bash
# Download and install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to docker group
sudo usermod -aG docker $USER

# Log out and log back in for changes to take effect
```

#### 1.2 Install Docker Compose
```bash
# Install Docker Compose
sudo apt install docker-compose-plugin
```

#### 1.3 Verify Docker Installation
```bash
# Check Docker version
docker --version

# Check Docker Compose version
docker compose version
```

### Step 2: Deploy with Docker

#### 2.1 Navigate to Deploy Directory
```bash
cd /opt/smartwatts/edge-gateway/deploy
```

#### 2.2 Start Services
```bash
# Start all services
docker compose up -d

# Check service status
docker compose ps
```

#### 2.3 Monitor Logs
```bash
# View logs
docker compose logs -f

# View specific service logs
docker compose logs -f edge-gateway
```

---

## Configuration

### Step 1: Basic Configuration

#### 1.1 Edit Configuration File
```bash
# Open configuration file
sudo nano /opt/smartwatts/config/edge-config.yml
```

#### 1.2 Update Network Settings
```yaml
network:
  host: "0.0.0.0"  # Listen on all interfaces
  port: 8080       # API port
  cloud_api_url: "https://api.smartwatts.com"  # Your cloud API
  cloud_api_key: "your-api-key-here"  # Your API key
```

#### 1.3 Update MQTT Settings
```yaml
mqtt:
  broker_host: "localhost"
  broker_port: 1883
  username: null  # Set if authentication required
  password: null  # Set if authentication required
```

### Step 2: Device Configuration

#### 2.1 Add Modbus Devices
```yaml
modbus:
  enabled: true
  devices:
    - name: "Solar Inverter"
      type: "inverter"
      address: 1
      protocol: "tcp"
      host: "192.168.1.101"  # Your inverter IP
      port: 502
      enabled: true
    - name: "Energy Meter"
      type: "meter"
      address: 2
      protocol: "rtu"
      port: "/dev/ttyUSB0"  # Serial port
      baudrate: 9600
      enabled: true
```

#### 2.2 Add MQTT Devices
MQTT devices are automatically discovered. No configuration needed.

### Step 3: AI Model Configuration

#### 3.1 Download AI Models
```bash
# Create models directory
sudo mkdir -p /opt/smartwatts/models

# Download models (if available)
# Place .tflite model files in this directory
```

#### 3.2 Configure AI Settings
```yaml
ai:
  enabled: true
  model_path: "/opt/smartwatts/models"
  inference_interval_seconds: 60
  batch_size: 32
  confidence_threshold: 0.8
```

### Step 4: Restart Services

#### 4.1 Restart Edge Gateway
```bash
# Restart the service
sudo systemctl restart smartwatts-edge

# Check status
sudo systemctl status smartwatts-edge
```

---

## Testing the Installation

### Step 1: Test API Endpoints

#### 1.1 Health Check
```bash
curl http://localhost:8080/api/v1/health
```

#### 1.2 System Status
```bash
curl http://localhost:8080/api/v1/status
```

#### 1.3 Device Discovery
```bash
curl http://localhost:8080/api/v1/devices/discovered
```

### Step 2: Test MQTT Communication

#### 2.1 Publish Test Message
```bash
# Publish a test message
curl -X POST http://localhost:8080/api/v1/mqtt/publish \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "test/energy",
    "message": {"power": 1000, "voltage": 240}
  }'
```

#### 2.2 Subscribe to Messages
```bash
# Subscribe to test topic
mosquitto_sub -h localhost -t "test/energy"
```

### Step 3: Test Data Storage

#### 3.1 Store Energy Reading
```bash
curl -X POST http://localhost:8080/api/v1/energy/readings \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "test_device_01",
    "device_type": "inverter",
    "power": 2500.5,
    "voltage": 240.2,
    "current": 10.4
  }'
```

#### 3.2 Retrieve Energy Readings
```bash
curl "http://localhost:8080/api/v1/energy/readings?device_id=test_device_01"
```

---

## Connecting Devices

### Step 1: Connect Modbus Devices

#### 1.1 Physical Connection
- Connect RS485/USB adapter to your device
- Connect Modbus devices to the adapter
- Note the device IP addresses and serial ports

#### 1.2 Configure in Edge Gateway
```yaml
modbus:
  devices:
    - name: "Your Inverter"
      type: "inverter"
      address: 1
      protocol: "tcp"
      host: "192.168.1.101"
      port: 502
      enabled: true
```

#### 1.3 Test Connection
```bash
# Test Modbus device
curl http://localhost:8080/api/v1/modbus/read/Your_Inverter
```

### Step 2: Connect MQTT Devices

#### 2.1 Configure Device
Configure your MQTT device to connect to:
- **Broker:** Your edge device IP
- **Port:** 1883
- **Topics:** smartwatts/energy/{device_id}/data

#### 2.2 Verify Connection
```bash
# Check discovered devices
curl http://localhost:8080/api/v1/devices/discovered
```

### Step 3: Connect Smart Plugs and Sensors

#### 3.1 WiFi Configuration
- Connect devices to your WiFi network
- Ensure they can reach your edge device

#### 3.2 MQTT Configuration
- Configure devices to publish to MQTT broker
- Use topic format: smartwatts/energy/{device_id}/data

---

## Troubleshooting

### Common Issues and Solutions

#### Issue 1: Service Won't Start
```bash
# Check service status
sudo systemctl status smartwatts-edge

# Check logs
sudo journalctl -u smartwatts-edge -f

# Common solutions:
sudo systemctl restart smartwatts-edge
sudo systemctl daemon-reload
```

#### Issue 2: API Not Responding
```bash
# Check if port is open
sudo netstat -tlnp | grep 8080

# Check firewall
sudo ufw status

# Allow port 8080
sudo ufw allow 8080
```

#### Issue 3: MQTT Broker Issues
```bash
# Check MQTT broker status
sudo systemctl status mosquitto

# Check MQTT logs
sudo journalctl -u mosquitto -f

# Restart MQTT broker
sudo systemctl restart mosquitto
```

#### Issue 4: Database Issues
```bash
# Check database file
ls -la /opt/smartwatts/data/

# Check database integrity
sqlite3 /opt/smartwatts/data/edge.db ".schema"

# Repair database if needed
sqlite3 /opt/smartwatts/data/edge.db ".recover" | sqlite3 /opt/smartwatts/data/edge_recovered.db
```

#### Issue 5: Permission Issues
```bash
# Fix ownership
sudo chown -R smartwatts:smartwatts /opt/smartwatts

# Fix permissions
sudo chmod -R 755 /opt/smartwatts
```

### Getting Help

#### Check Logs
```bash
# Edge Gateway logs
sudo journalctl -u smartwatts-edge -f

# MQTT broker logs
sudo journalctl -u mosquitto -f

# System logs
sudo journalctl -f
```

#### Monitor System Resources
```bash
# CPU and memory usage
htop

# Disk usage
df -h

# Network connections
netstat -tlnp
```

---

## Maintenance

### Daily Tasks

#### Check Service Status
```bash
# Quick status check
sudo systemctl status smartwatts-edge

# Detailed monitoring
/opt/smartwatts/monitor.sh
```

#### Check Logs
```bash
# Recent errors
sudo journalctl -u smartwatts-edge --since "1 hour ago" | grep ERROR

# Recent warnings
sudo journalctl -u smartwatts-edge --since "1 hour ago" | grep WARNING
```

### Weekly Tasks

#### Backup Data
```bash
# Manual backup
/opt/smartwatts/backup.sh

# Check backup status
ls -la /opt/smartwatts/backups/
```

#### Update System
```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Restart services
sudo systemctl restart smartwatts-edge
```

### Monthly Tasks

#### Clean Old Data
```bash
# Clean data older than 30 days
curl -X POST http://localhost:8080/api/v1/maintenance/cleanup?days=30
```

#### Check Disk Space
```bash
# Check disk usage
df -h

# Clean up if needed
sudo apt autoremove -y
sudo apt autoclean
```

### Update SmartWatts Edge Gateway

#### Update Code
```bash
# Navigate to installation directory
cd /opt/smartwatts

# Stop service
sudo systemctl stop smartwatts-edge

# Backup current installation
./backup.sh

# Update code (if using git)
git pull origin main

# Update dependencies
source venv/bin/activate
pip install -r requirements.txt

# Start service
sudo systemctl start smartwatts-edge
```

---

## Support

### Documentation
- **README:** /opt/smartwatts/README.md
- **API Documentation:** http://your-device-ip:8080/docs
- **Configuration:** /opt/smartwatts/config/edge-config.yml

### Monitoring Tools
- **System Monitor:** /opt/smartwatts/monitor.sh
- **Prometheus Metrics:** http://your-device-ip:9090/metrics
- **Grafana Dashboard:** http://your-device-ip:3000

### Getting Help
1. Check the troubleshooting section above
2. Review the logs for error messages
3. Check the system resources
4. Contact support with specific error messages

### Useful Commands
```bash
# Service management
sudo systemctl start smartwatts-edge
sudo systemctl stop smartwatts-edge
sudo systemctl restart smartwatts-edge
sudo systemctl status smartwatts-edge

# Log viewing
sudo journalctl -u smartwatts-edge -f
sudo journalctl -u mosquitto -f

# Monitoring
/opt/smartwatts/monitor.sh
htop
df -h
free -h

# API testing
curl http://localhost:8080/api/v1/health
curl http://localhost:8080/api/v1/status
```

---

## Conclusion

Congratulations! You have successfully installed and configured the SmartWatts Edge Gateway. Your edge device is now ready to:

- ✅ Monitor energy consumption
- ✅ Connect to smart devices
- ✅ Run AI inference
- ✅ Sync data with the cloud
- ✅ Provide real-time insights

The system is designed to be robust and self-maintaining, but regular monitoring and maintenance will ensure optimal performance.

For additional support or questions, please refer to the troubleshooting section or contact the support team.

---

**End of Installation Guide**