# Raspberry Pi 5 Quick Setup for SmartWatts

## Overview

This guide provides a quick setup process for installing SmartWatts on your Raspberry Pi 5. The setup includes all necessary components for a production-ready edge computing device.

## What You'll Install

- **Raspberry Pi OS 64-bit Lite** - Lightweight operating system
- **Docker + Docker Compose** - Containerization platform
- **Python 3.11 Environment** - Python runtime and packages
- **MQTT Broker (Mosquitto)** - IoT communication
- **PostgreSQL Database** - Data storage
- **SmartWatts Platform** - Complete energy monitoring system

## Quick Start (30 Minutes)

### Step 1: Flash Raspberry Pi OS

1. Download [Raspberry Pi Imager](https://www.raspberrypi.org/downloads/)
2. Select "Raspberry Pi OS Lite (64-bit)"
3. Click the gear icon to configure:
   - Enable SSH
   - Set username: `pi`
   - Set password: `raspberry` (change later)
   - Configure WiFi (optional)
4. Flash to microSD card

### Step 2: Boot and Connect

1. Insert microSD card into Pi 5
2. Connect power supply and boot
3. Connect via SSH:
   ```bash
   ssh pi@<pi-ip-address>
   ```

### Step 3: Run Automated Installation

```bash
# Download and run the installation script
wget https://raw.githubusercontent.com/your-repo/smartwatts/main/scripts/install-raspberry-pi5.sh
chmod +x install-raspberry-pi5.sh
./install-raspberry-pi5.sh
```

### Step 4: Reboot and Start

```bash
# Reboot the system
sudo reboot

# After reboot, start SmartWatts
cd ~/smartwatts
./start-smartwatts.sh
```

### Step 5: Access Dashboard

Open your browser and go to:
- **Dashboard**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Edge Gateway**: http://localhost:8088

## Manual Installation (If Automated Fails)

### 1. Update System
```bash
sudo apt update && sudo apt upgrade -y
```

### 2. Install Docker
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker pi
rm get-docker.sh
```

### 3. Install Dependencies
```bash
sudo apt install -y docker-compose postgresql mosquitto python3.11 python3.11-pip
```

### 4. Configure Services
```bash
# Start PostgreSQL
sudo systemctl enable postgresql
sudo systemctl start postgresql

# Start MQTT
sudo systemctl enable mosquitto
sudo systemctl start mosquitto
```

### 5. Install SmartWatts
```bash
cd ~
git clone https://github.com/your-repo/smartwatts.git
cd smartwatts
chmod +x scripts/*.sh
```

## Hardware Configuration

### Connect Your Hardware

1. **Solar Inverters**: Connect RS485 to USB adapter
2. **Smart Meters**: Connect via RS485 or Ethernet
3. **Sensors**: Connect via MQTT or USB

### Configure Devices

Edit the configuration file:
```bash
nano ~/smartwatts/config/edge-gateway-pi.yml
```

Example for SMA Sunny Boy Inverter:
```yaml
rs485:
  devices:
    sma-inverter:
      port: "/dev/ttyUSB0"
      baud-rate: 9600
      unit-id: 1
      start-address: 40000
      register-count: 20
      device-type: "SOLAR_INVERTER"
      manufacturer: "SMA"
      model: "Sunny Boy"
      enabled: true
```

## Management Commands

### Start/Stop Services
```bash
# Start SmartWatts
./start-smartwatts.sh

# Stop SmartWatts
./stop-smartwatts.sh

# Check status
./status-smartwatts.sh
```

### Update System
```bash
# Update SmartWatts
./update-smartwatts.sh

# Backup data
./backup-smartwatts.sh
```

### View Logs
```bash
# View all logs
docker-compose -f docker-compose.hardware.yml logs -f

# View specific service logs
docker logs smartwatts-edge-gateway -f
```

## Troubleshooting

### Common Issues

**Docker Permission Denied**:
```bash
sudo usermod -aG docker pi
newgrp docker
```

**Serial Port Not Found**:
```bash
sudo chmod 666 /dev/ttyUSB0
sudo usermod -aG dialout pi
```

**Database Connection Failed**:
```bash
sudo systemctl restart postgresql
```

**MQTT Not Working**:
```bash
sudo systemctl restart mosquitto
```

### Check Service Status
```bash
# Check all services
sudo systemctl status docker postgresql mosquitto smartwatts-edge

# Check Docker containers
docker ps

# Check ports
netstat -tlnp | grep -E ':(1883|3000|8080|8088)'
```

## Performance Optimization

### Increase Swap Space
```bash
sudo dphys-swapfile swapoff
sudo sed -i 's/CONF_SWAPSIZE=100/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile
sudo dphys-swapfile setup
sudo dphys-swapfile swapon
```

### Monitor Performance
```bash
# System resources
htop

# Disk usage
df -h

# Memory usage
free -h

# Docker stats
docker stats
```

## Security Configuration

### Configure Firewall
```bash
sudo ufw enable
sudo ufw allow ssh
sudo ufw allow 1883  # MQTT
sudo ufw allow 8080  # API Gateway
sudo ufw allow 8088  # Edge Gateway
sudo ufw allow 3000  # Frontend
```

### Change Default Password
```bash
passwd
```

### Secure SSH
```bash
sudo nano /etc/ssh/sshd_config
# Set: PermitRootLogin no
sudo systemctl restart ssh
```

## What's Included

### SmartWatts Services
- **API Gateway** (Port 8080) - HTTP routing and security
- **Edge Gateway** (Port 8088) - Hardware integration
- **User Service** (Port 8081) - User management
- **Energy Service** (Port 8082) - Energy data processing
- **Device Service** (Port 8083) - Device management
- **Analytics Service** (Port 8084) - Data analytics
- **Billing Service** (Port 8085) - Billing and tariffs
- **Frontend** (Port 3000) - Web dashboard

### Hardware Support
- **RS485/Modbus RTU** - Solar inverters, smart meters
- **Modbus TCP** - Network devices
- **MQTT** - IoT sensors and devices
- **Serial Communication** - Direct hardware access

### Features
- **Real-time monitoring** - Live energy data
- **ML-powered insights** - TensorFlow Lite inference
- **Device discovery** - Automatic hardware detection
- **Offline-first** - Works without internet
- **Scalable** - Add multiple devices
- **Production-ready** - Enterprise-grade reliability

## Next Steps

1. **Connect your hardware** using the configuration examples
2. **Test the integration** using the provided test methods
3. **Monitor the dashboard** to see your data
4. **Scale up** by adding more devices
5. **Set up backups** using the backup script

Your Raspberry Pi 5 is now ready to run SmartWatts with full hardware integration capabilities!

## Support

- **Documentation**: Check the full setup guide
- **Issues**: GitHub Issues
- **Community**: SmartWatts Discord
- **Email**: support@mysmartwatts.com

Version: 1.0.0
Last Updated: January 2025




