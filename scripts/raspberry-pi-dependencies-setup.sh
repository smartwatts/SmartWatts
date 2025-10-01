#!/bin/bash

# Raspberry Pi 5 Dependencies Setup Script
# Prepares Raspberry Pi for SmartWatts installation (without installing SmartWatts)
# Run this script to get your Pi ready for when SmartWatts is ready

set -e

echo "🍓 Raspberry Pi 5 Dependencies Setup"
echo "===================================="
echo "This script prepares your Raspberry Pi for SmartWatts installation"
echo "SmartWatts will NOT be installed - only dependencies and system setup"
echo ""

# Check if running on Raspberry Pi
if ! grep -q "Raspberry Pi" /proc/cpuinfo 2>/dev/null; then
    echo "⚠️  Warning: This script is designed for Raspberry Pi"
    echo "   Continue anyway? (y/N)"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo "Exiting..."
        exit 1
    fi
fi

# Update system
echo "📦 Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install essential tools
echo "🔧 Installing essential tools..."
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
    software-properties-common

# Install Docker
echo "🐳 Installing Docker..."
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
echo "🐳 Installing Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    sudo apt install docker-compose -y
    echo "✅ Docker Compose installed successfully"
else
    echo "✅ Docker Compose already installed"
fi

# Install Python 3.11+
echo "🐍 Installing Python 3.11+..."
if ! command -v python3.11 &> /dev/null; then
    sudo add-apt-repository ppa:deadsnakes/ppa -y
    sudo apt update
    sudo apt install -y python3.11 python3.11-venv python3.11-pip python3.11-dev
    echo "✅ Python 3.11 installed successfully"
else
    echo "✅ Python 3.11 already installed"
fi

# Install Python packages for ML and IoT
echo "📚 Installing Python packages..."
pip3 install --user \
    numpy \
    pandas \
    paho-mqtt \
    pyserial \
    requests \
    flask \
    fastapi \
    uvicorn \
    tensorflow-lite \
    opencv-python \
    pillow \
    scikit-learn

# Install MQTT broker
echo "📡 Installing Mosquitto MQTT broker..."
if ! systemctl is-active --quiet mosquitto; then
    sudo apt install mosquitto mosquitto-clients -y
    sudo systemctl enable mosquitto
    sudo systemctl start mosquitto
    echo "✅ Mosquitto MQTT broker installed and started"
else
    echo "✅ Mosquitto MQTT broker already running"
fi

# Install PostgreSQL
echo "🗄️  Installing PostgreSQL..."
if ! systemctl is-active --quiet postgresql; then
    sudo apt install postgresql postgresql-contrib -y
    sudo systemctl enable postgresql
    sudo systemctl start postgresql
    echo "✅ PostgreSQL installed and started"
else
    echo "✅ PostgreSQL already running"
fi

# Create SmartWatts user and database
echo "👤 Setting up SmartWatts database user..."
sudo -u postgres psql -c "CREATE USER smartwatts WITH PASSWORD 'smartwatts123';" 2>/dev/null || echo "User smartwatts already exists"
sudo -u postgres psql -c "CREATE DATABASE smartwatts_platform OWNER smartwatts;" 2>/dev/null || echo "Database smartwatts_platform already exists"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE smartwatts_platform TO smartwatts;"

# Install Java 17 (for Spring Boot services)
echo "☕ Installing Java 17..."
if ! command -v java &> /dev/null || ! java --version 2>&1 | grep -q "17"; then
    sudo apt install -y openjdk-17-jdk
    echo "✅ Java 17 installed successfully"
else
    echo "✅ Java 17 already installed"
fi

# Install Node.js (for frontend)
echo "🟢 Installing Node.js..."
if ! command -v node &> /dev/null; then
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt install -y nodejs
    echo "✅ Node.js installed successfully"
else
    echo "✅ Node.js already installed"
fi

# Enable hardware interfaces
echo "🔌 Enabling hardware interfaces..."
sudo raspi-config nonint do_i2c 0
sudo raspi-config nonint do_spi 0
sudo raspi-config nonint do_serial 0
echo "✅ I2C, SPI, and Serial interfaces enabled"

# Create SmartWatts user and directories
echo "📁 Creating SmartWatts directories..."
sudo useradd -m -s /bin/bash smartwatts 2>/dev/null || echo "User smartwatts already exists"
sudo usermod -aG docker smartwatts
sudo usermod -aG dialout smartwatts
sudo usermod -aG i2c smartwatts
sudo usermod -aG spi smartwatts

# Create directory structure
sudo mkdir -p /opt/smartwatts/{data,logs,config,backups,models}
sudo chown -R smartwatts:smartwatts /opt/smartwatts
mkdir -p /opt/smartwatts/data/{postgres,redis,edge-gateway,models}

# Configure system optimizations
echo "⚡ Applying system optimizations..."
# Increase file descriptor limits
echo "* soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "* hard nofile 65536" | sudo tee -a /etc/security/limits.conf

# Optimize memory settings
echo "vm.swappiness=10" | sudo tee -a /etc/sysctl.conf
echo "vm.vfs_cache_pressure=50" | sudo tee -a /etc/sysctl.conf

# Apply settings
sudo sysctl -p

# Configure log rotation
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

# Configure firewall (optional)
echo "🔥 Configuring firewall..."
sudo ufw --force enable
sudo ufw allow ssh
sudo ufw allow 3000  # Frontend
sudo ufw allow 8080  # API Gateway
sudo ufw allow 8088  # Edge Gateway
sudo ufw allow 1883  # MQTT
sudo ufw allow 5432  # PostgreSQL
echo "✅ Firewall configured"

# Create system check script
echo "🔍 Creating system check script..."
sudo tee /opt/smartwatts/check-system.sh << 'EOF'
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
echo "  Temperature: $(vcgencmd measure_temp 2>/dev/null || echo 'N/A')"

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
echo "SmartWatts Directories:"
echo "  Data: $(ls -la /opt/smartwatts/data/ 2>/dev/null | wc -l) items"
echo "  Logs: $(ls -la /opt/smartwatts/logs/ 2>/dev/null | wc -l) items"
echo "  Config: $(ls -la /opt/smartwatts/config/ 2>/dev/null | wc -l) items"

echo ""
echo "✅ System check complete!"
EOF

sudo chmod +x /opt/smartwatts/check-system.sh

# Test all components
echo "🧪 Testing all components..."

# Test Docker
echo "Testing Docker..."
docker run hello-world > /dev/null 2>&1 && echo "✅ Docker working" || echo "❌ Docker test failed"

# Test MQTT
echo "Testing MQTT..."
mosquitto_pub -h localhost -t "test/setup" -m "Raspberry Pi setup complete" > /dev/null 2>&1
mosquitto_sub -h localhost -t "test/setup" -C 1 > /dev/null 2>&1 && echo "✅ MQTT working" || echo "❌ MQTT test failed"

# Test PostgreSQL
echo "Testing PostgreSQL..."
psql -h localhost -U smartwatts -d smartwatts_platform -c "SELECT 'PostgreSQL working' as status;" > /dev/null 2>&1 && echo "✅ PostgreSQL working" || echo "❌ PostgreSQL test failed"

# Test Python
echo "Testing Python..."
python3 -c "import paho.mqtt.client, numpy, pandas, serial; print('✅ Python packages working')" 2>/dev/null || echo "❌ Python test failed"

# Test Java
echo "Testing Java..."
java -version > /dev/null 2>&1 && echo "✅ Java working" || echo "❌ Java test failed"

# Get Pi IP address
PI_IP=$(hostname -I | awk '{print $1}')

echo ""
echo "🎉 Raspberry Pi Setup Complete!"
echo "=============================="
echo ""
echo "Your Raspberry Pi is now ready for SmartWatts installation!"
echo ""
echo "System Information:"
echo "  IP Address: $PI_IP"
echo "  Hostname: $(hostname)"
echo "  Architecture: $(uname -m)"
echo ""
echo "Installed Components:"
echo "  ✅ Docker & Docker Compose"
echo "  ✅ Python 3.11+ with ML libraries"
echo "  ✅ MQTT Broker (Mosquitto)"
echo "  ✅ PostgreSQL Database"
echo "  ✅ Java 17 (OpenJDK)"
echo "  ✅ Node.js 18.x"
echo "  ✅ Hardware interfaces (I2C, SPI, Serial)"
echo "  ✅ SmartWatts directories created"
echo "  ✅ System optimizations applied"
echo ""
echo "Next Steps:"
echo "  1. When SmartWatts is ready, copy the files to this Pi"
echo "  2. Run the SmartWatts installation script"
echo "  3. Connect your energy monitoring hardware"
echo ""
echo "Useful Commands:"
echo "  • Check system: /opt/smartwatts/check-system.sh"
echo "  • Check services: systemctl status mosquitto postgresql docker"
echo "  • Check hardware: ls /dev/i2c-* /dev/spi-* /dev/ttyUSB*"
echo ""
echo "Note: You may need to log out and back in for group changes to take effect."
echo ""
echo "Your Raspberry Pi is ready for SmartWatts! 🍓⚡"




