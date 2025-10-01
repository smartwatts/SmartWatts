#!/bin/bash

# Complete Raspberry Pi 5 First-Time Setup Script
# This script does EVERYTHING from a fresh Pi5 to ready for SmartWatts
# Run this on a fresh Raspberry Pi OS installation

set -e

echo "🍓 Complete Raspberry Pi 5 First-Time Setup"
echo "=========================================="
echo "This script will prepare your Pi5 from scratch for SmartWatts"
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    echo "❌ Please do not run this script as root"
    echo "Run as regular user, the script will use sudo when needed"
    exit 1
fi

# Check if running on Raspberry Pi
if ! grep -q "Raspberry Pi" /proc/cpuinfo 2>/dev/null; then
    echo "⚠️  Warning: This script is designed for Raspberry Pi"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to wait for user input
wait_for_user() {
    echo ""
    read -p "Press Enter to continue..." -r
    echo ""
}

echo "📋 Prerequisites Check"
echo "====================="
echo "This script will install:"
echo "  • Raspberry Pi OS updates"
echo "  • Docker & Docker Compose"
echo "  • Python 3.11+ with IoT libraries"
echo "  • Java 17"
echo "  • Node.js"
echo "  • MQTT Broker (Mosquitto)"
echo "  • PostgreSQL"
echo "  • Hardware interfaces (I2C, SPI, Serial)"
echo "  • Wireless monitoring tools"
echo "  • SmartWatts preparation"
echo ""
wait_for_user

# Update system
echo "🔄 Step 1: Updating Raspberry Pi OS..."
echo "====================================="
sudo apt update && sudo apt upgrade -y
echo "✅ System updated successfully"

# Install essential tools
echo ""
echo "📦 Step 2: Installing essential tools..."
echo "======================================"
sudo apt install -y \
    git \
    curl \
    wget \
    vim \
    htop \
    tree \
    unzip \
    jq \
    net-tools \
    iotop \
    nethogs \
    tmux \
    screen \
    rsync \
    ufw \
    python3-pip \
    python3-venv \
    python3-dev \
    build-essential \
    software-properties-common
echo "✅ Essential tools installed"

# Install Docker
echo ""
echo "🐳 Step 3: Installing Docker..."
echo "=============================="
if ! command_exists docker; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    echo "✅ Docker installed successfully"
else
    echo "✅ Docker already installed"
fi

# Install Docker Compose
echo ""
echo "🐳 Step 4: Installing Docker Compose..."
echo "======================================"
if ! command_exists docker-compose; then
    sudo apt install docker-compose -y
    echo "✅ Docker Compose installed successfully"
else
    echo "✅ Docker Compose already installed"
fi

# Install Python 3.11+
echo ""
echo "🐍 Step 5: Installing Python 3.11+..."
echo "===================================="
if ! python3 -c "import sys; exit(0 if sys.version_info >= (3, 11) else 1)" 2>/dev/null; then
    sudo add-apt-repository ppa:deadsnakes/ppa -y
    sudo apt update
    sudo apt install -y python3.11 python3.11-venv python3.11-pip python3.11-dev
    sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 1
    echo "✅ Python 3.11+ installed successfully"
else
    echo "✅ Python 3.11+ already installed"
fi

# Install Java 17
echo ""
echo "☕ Step 6: Installing Java 17..."
echo "==============================="
if ! java -version 2>&1 | grep -q "17"; then
    sudo apt install -y openjdk-17-jdk
    echo "✅ Java 17 installed successfully"
else
    echo "✅ Java 17 already installed"
fi

# Install Node.js
echo ""
echo "📦 Step 7: Installing Node.js..."
echo "==============================="
if ! command_exists node; then
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt install -y nodejs
    echo "✅ Node.js installed successfully"
else
    echo "✅ Node.js already installed"
fi

# Install MQTT Broker
echo ""
echo "📡 Step 8: Installing MQTT Broker (Mosquitto)..."
echo "==============================================="
if ! systemctl is-active --quiet mosquitto; then
    sudo apt install -y mosquitto mosquitto-clients
    sudo systemctl enable mosquitto
    sudo systemctl start mosquitto
    echo "✅ Mosquitto MQTT broker installed and started"
else
    echo "✅ Mosquitto MQTT broker already running"
fi

# Install PostgreSQL
echo ""
echo "🗄️  Step 9: Installing PostgreSQL..."
echo "==================================="
if ! systemctl is-active --quiet postgresql; then
    sudo apt install -y postgresql postgresql-contrib
    sudo systemctl enable postgresql
    sudo systemctl start postgresql
    echo "✅ PostgreSQL installed and started"
else
    echo "✅ PostgreSQL already running"
fi

# Configure PostgreSQL
echo ""
echo "🔧 Step 10: Configuring PostgreSQL..."
echo "===================================="
sudo -u postgres psql -c "CREATE USER smartwatts WITH PASSWORD 'smartwatts123';" 2>/dev/null || echo "User smartwatts already exists"
sudo -u postgres psql -c "CREATE DATABASE smartwatts_platform OWNER smartwatts;" 2>/dev/null || echo "Database smartwatts_platform already exists"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE smartwatts_platform TO smartwatts;"
echo "✅ PostgreSQL configured for SmartWatts"

# Enable hardware interfaces
echo ""
echo "🔌 Step 11: Enabling hardware interfaces..."
echo "========================================="
sudo raspi-config nonint do_i2c 0
sudo raspi-config nonint do_spi 0
sudo raspi-config nonint do_serial 0
echo "✅ I2C, SPI, and Serial interfaces enabled"

# Install Python packages
echo ""
echo "🐍 Step 12: Installing Python IoT packages..."
echo "============================================"
pip3 install --user \
    numpy \
    pandas \
    paho-mqtt \
    pyserial \
    requests \
    flask \
    fastapi \
    uvicorn \
    pymodbus \
    crcmod \
    RPi.GPIO \
    smbus2 \
    spidev \
    adafruit-circuitpython-ads1x15 \
    adafruit-circuitpython-mcp230xx \
    adafruit-circuitpython-pca9685 \
    adafruit-circuitpython-ssd1306
echo "✅ Python IoT packages installed"

# Install wireless tools
echo ""
echo "📡 Step 13: Installing wireless monitoring tools..."
echo "================================================="
sudo apt install -y \
    iw \
    wireless-tools \
    wpasupplicant \
    hostapd \
    dnsmasq
echo "✅ Wireless tools installed"

# Create SmartWatts directories
echo ""
echo "📁 Step 14: Creating SmartWatts directories..."
echo "============================================"
sudo mkdir -p /opt/smartwatts/{data,logs,config,backups,models,devices}
sudo chown -R $USER:$USER /opt/smartwatts
mkdir -p /opt/smartwatts/data/{postgres,redis,edge-gateway,models,devices}
echo "✅ SmartWatts directories created"

# Configure MQTT
echo ""
echo "📡 Step 15: Configuring MQTT for wireless communication..."
echo "======================================================="
sudo tee /etc/mosquitto/conf.d/smartwatts.conf << 'EOF'
# SmartWatts MQTT Configuration
port 1883
listener 1883 0.0.0.0
allow_anonymous true
persistence true
persistence_location /var/lib/mosquitto/
log_dest file /var/log/mosquitto/mosquitto.log
log_type error
log_type warning
log_type notice
log_type information
connection_messages true
log_timestamp true
EOF

sudo systemctl restart mosquitto
echo "✅ MQTT configured for wireless access"

# Configure firewall
echo ""
echo "🔒 Step 16: Configuring firewall..."
echo "================================="
sudo ufw --force enable
sudo ufw allow ssh
sudo ufw allow 1883/tcp comment 'MQTT'
sudo ufw allow 8080/tcp comment 'API Gateway (future)'
sudo ufw allow 8088/tcp comment 'Edge Gateway (future)'
sudo ufw allow 3000/tcp comment 'Frontend (future)'
echo "✅ Firewall configured"

# Create management scripts
echo ""
echo "🔧 Step 17: Creating management scripts..."
echo "======================================="

# System check script
cat > /opt/smartwatts/check-system.sh << 'EOF'
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
echo "  WiFi Signal: $(iwconfig 2>/dev/null | grep -o 'Signal level=[^ ]*' | cut -d= -f2 || echo 'Not connected')"

echo ""
echo "Hardware Interfaces:"
echo "  I2C: $(ls /dev/i2c-* 2>/dev/null | wc -l) devices"
echo "  SPI: $(ls /dev/spi-* 2>/dev/null | wc -l) devices"
echo "  Serial: $(ls /dev/ttyUSB* /dev/ttyACM* 2>/dev/null | wc -l) devices"

echo ""
echo "MQTT Test:"
mosquitto_pub -h localhost -t "smartwatts/test" -m "System check $(date)" 2>/dev/null && echo "✅ MQTT working" || echo "❌ MQTT not working"

echo ""
echo "PostgreSQL Test:"
psql -h localhost -U smartwatts -d smartwatts_platform -c "SELECT 'PostgreSQL working' as status;" 2>/dev/null && echo "✅ PostgreSQL working" || echo "❌ PostgreSQL not working"

echo ""
echo "✅ System check complete!"
EOF

# Wireless monitoring script
cat > /opt/smartwatts/monitor-wireless.sh << 'EOF'
#!/bin/bash
echo "📡 Wireless Network Status"
echo "========================="
echo "WiFi Interface: $(iwconfig 2>/dev/null | grep -o '^[a-zA-Z0-9]*' | head -1)"
echo "Signal Strength: $(iwconfig 2>/dev/null | grep -o 'Signal level=[^ ]*' | cut -d= -f2)"
echo "IP Address: $(hostname -I | awk '{print $1}')"
echo "Gateway: $(ip route | grep default | awk '{print $3}')"
echo "DNS: $(cat /etc/resolv.conf | grep nameserver | awk '{print $2}' | head -1)"
echo ""
echo "MQTT Broker Status:"
systemctl is-active mosquitto
echo ""
echo "PostgreSQL Status:"
systemctl is-active postgresql
EOF

# Device discovery script
cat > /opt/smartwatts/discover-devices.sh << 'EOF'
#!/bin/bash
echo "🔍 Smart Meter and Inverter Discovery"
echo "===================================="
echo "Scanning for RS485 devices..."
ls /dev/ttyUSB* /dev/ttyACM* 2>/dev/null | while read device; do
    echo "Found device: $device"
done

echo ""
echo "Scanning for I2C devices..."
i2cdetect -y 1 2>/dev/null | grep -v "^   " | grep -v "^$" | while read line; do
    echo "I2C device found: $line"
done

echo ""
echo "Scanning for SPI devices..."
ls /dev/spi* 2>/dev/null | while read device; do
    echo "Found SPI device: $device"
done

echo ""
echo "MQTT Topics (listening for 5 seconds)..."
timeout 5 mosquitto_sub -h localhost -t "smartwatts/+/+" -v 2>/dev/null || echo "No MQTT messages received"
EOF

# System optimization script
cat > /opt/smartwatts/optimize-system.sh << 'EOF'
#!/bin/bash
echo "⚡ Optimizing Raspberry Pi for SmartWatts"
echo "======================================="

# Increase file descriptor limits
echo "Setting file descriptor limits..."
echo "* soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "* hard nofile 65536" | sudo tee -a /etc/security/limits.conf

# Optimize memory settings
echo "Optimizing memory settings..."
echo "vm.swappiness=10" | sudo tee -a /etc/sysctl.conf
echo "vm.vfs_cache_pressure=50" | sudo tee -a /etc/sysctl.conf

# Apply settings
sudo sysctl -p

# Set CPU governor to performance
echo "Setting CPU governor to performance..."
echo 'performance' | sudo tee /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor

echo "✅ System optimization complete!"
EOF

# Startup script
cat > /opt/smartwatts/startup.sh << 'EOF'
#!/bin/bash
# SmartWatts Pi5 Startup Script
echo "🍓 SmartWatts Pi5 Starting..."
echo "============================="

# Start MQTT broker
sudo systemctl start mosquitto

# Start PostgreSQL
sudo systemctl start postgresql

# Start Docker
sudo systemctl start docker

echo "✅ All services started!"
echo "Pi5 is ready for SmartWatts installation"
echo "IP Address: $(hostname -I | awk '{print $1}')"
EOF

# Make all scripts executable
chmod +x /opt/smartwatts/*.sh
echo "✅ Management scripts created"

# Run system optimization
echo ""
echo "⚡ Step 18: Running system optimization..."
echo "======================================="
/opt/smartwatts/optimize-system.sh

# Add startup script to crontab
echo ""
echo "⏰ Step 19: Setting up auto-startup..."
echo "===================================="
(crontab -l 2>/dev/null; echo "@reboot /opt/smartwatts/startup.sh") | crontab -
echo "✅ Auto-startup configured"

# Final system check
echo ""
echo "🔍 Step 20: Running final system check..."
echo "======================================="
/opt/smartwatts/check-system.sh

echo ""
echo "🎉 COMPLETE SETUP FINISHED!"
echo "=========================="
echo ""
echo "Your Raspberry Pi 5 is now fully prepared for SmartWatts!"
echo ""
echo "📡 Wireless Capabilities:"
echo "  • MQTT Broker: $(hostname -I | awk '{print $1}'):1883"
echo "  • PostgreSQL: $(hostname -I | awk '{print $1}'):5432"
echo "  • Docker & Docker Compose ready"
echo "  • Python 3.11+ with IoT libraries"
echo "  • Hardware interfaces enabled"
echo ""
echo "🔧 Management Scripts:"
echo "  • System Check: /opt/smartwatts/check-system.sh"
echo "  • Wireless Monitor: /opt/smartwatts/monitor-wireless.sh"
echo "  • Device Discovery: /opt/smartwatts/discover-devices.sh"
echo "  • System Optimization: /opt/smartwatts/optimize-system.sh"
echo ""
echo "🌐 Access Information:"
echo "  • IP Address: $(hostname -I | awk '{print $1}')"
echo "  • Hostname: $(hostname)"
echo "  • SSH: ssh $(whoami)@$(hostname -I | awk '{print $1}')"
echo ""
echo "📱 Next Steps:"
echo "  1. Connect your smart meter or inverter via RS485/USB"
echo "  2. Test MQTT communication"
echo "  3. When ready, install SmartWatts using the portable installer"
echo ""
echo "🔍 Test your setup:"
echo "  /opt/smartwatts/check-system.sh"
echo ""
echo "⚠️  Important: You may need to log out and back in for Docker group changes to take effect."
echo ""
echo "✅ Your Pi5 is ready for SmartWatts!"




