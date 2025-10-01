#!/bin/bash
# SmartWatts Edge Gateway Installation Script for R501 RK3588
# Complete installation and setup script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
INSTALL_DIR="/opt/smartwatts"
SERVICE_USER="smartwatts"
PYTHON_VERSION="3.11"
NODE_VERSION="18"

echo -e "${BLUE}🚀 SmartWatts Edge Gateway Installation for R501 RK3588${NC}"
echo "============================================================"

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}❌ Please run as root (use sudo)${NC}"
    exit 1
fi

# Update system packages
echo -e "${YELLOW}📦 Updating system packages...${NC}"
apt update && apt upgrade -y

# Install system dependencies
echo -e "${YELLOW}📦 Installing system dependencies...${NC}"
apt install -y \
    python3.11 \
    python3.11-venv \
    python3.11-dev \
    python3-pip \
    git \
    curl \
    wget \
    unzip \
    build-essential \
    cmake \
    pkg-config \
    libssl-dev \
    libffi-dev \
    libjpeg-dev \
    libopenblas-dev \
    liblapack-dev \
    libhdf5-dev \
    pkg-config \
    libhdf5-serial-dev \
    libatlas-base-dev \
    gfortran \
    libopenmpi-dev \
    libopenmpi3 \
    mosquitto \
    mosquitto-clients \
    sqlite3 \
    htop \
    vim \
    nano \
    net-tools \
    nmap \
    i2c-tools \
    spi-tools \
    python3-serial \
    python3-paho-mqtt \
    python3-pymodbus \
    python3-sqlalchemy \
    python3-alembic \
    python3-psutil \
    python3-yaml \
    python3-dotenv \
    python3-cryptography \
    python3-bcrypt \
    python3-httpx \
    python3-websockets \
    python3-structlog \
    python3-prometheus-client \
    python3-tensorflow-lite \
    python3-numpy \
    python3-scikit-learn \
    python3-pandas \
    python3-pytz

# Create service user
echo -e "${YELLOW}👤 Creating service user...${NC}"
if ! id "$SERVICE_USER" &>/dev/null; then
    useradd -r -s /bin/false -d "$INSTALL_DIR" "$SERVICE_USER"
fi

# Create installation directory
echo -e "${YELLOW}📁 Creating installation directory...${NC}"
mkdir -p "$INSTALL_DIR"
mkdir -p "$INSTALL_DIR/data"
mkdir -p "$INSTALL_DIR/logs"
mkdir -p "$INSTALL_DIR/models"
mkdir -p "$INSTALL_DIR/config"
mkdir -p "$INSTALL_DIR/backups"

# Set permissions
chown -R "$SERVICE_USER:$SERVICE_USER" "$INSTALL_DIR"
chmod 755 "$INSTALL_DIR"

# Clone or copy SmartWatts Edge Gateway
echo -e "${YELLOW}📥 Installing SmartWatts Edge Gateway...${NC}"
if [ -d "edge-gateway" ]; then
    cp -r edge-gateway/* "$INSTALL_DIR/"
else
    echo -e "${RED}❌ Edge gateway source not found. Please ensure edge-gateway directory exists.${NC}"
    exit 1
fi

# Set ownership
chown -R "$SERVICE_USER:$SERVICE_USER" "$INSTALL_DIR"

# Create Python virtual environment
echo -e "${YELLOW}🐍 Creating Python virtual environment...${NC}"
cd "$INSTALL_DIR"
python3.11 -m venv venv
source venv/bin/activate

# Install Python dependencies
echo -e "${YELLOW}📦 Installing Python dependencies...${NC}"
pip install --upgrade pip
pip install -r requirements.txt

# Install TensorFlow Lite (if not already installed)
echo -e "${YELLOW}🤖 Installing TensorFlow Lite...${NC}"
pip install tensorflow-lite

# Configure MQTT broker
echo -e "${YELLOW}📡 Configuring MQTT broker...${NC}"
cat > /etc/mosquitto/conf.d/smartwatts.conf << EOF
# SmartWatts MQTT Configuration
listener 1883
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

# Start and enable MQTT broker
systemctl enable mosquitto
systemctl start mosquitto

# Create systemd service
echo -e "${YELLOW}⚙️ Creating systemd service...${NC}"
cat > /etc/systemd/system/smartwatts-edge.service << EOF
[Unit]
Description=SmartWatts Edge Gateway
After=network.target mosquitto.service
Wants=mosquitto.service

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$INSTALL_DIR
Environment=PATH=$INSTALL_DIR/venv/bin
ExecStart=$INSTALL_DIR/venv/bin/python main.py
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

# Security settings
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=$INSTALL_DIR

[Install]
WantedBy=multi-user.target
EOF

# Create logrotate configuration
echo -e "${YELLOW}📝 Creating logrotate configuration...${NC}"
cat > /etc/logrotate.d/smartwatts << EOF
$INSTALL_DIR/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 $SERVICE_USER $SERVICE_USER
    postrotate
        systemctl reload smartwatts-edge
    endscript
}
EOF

# Create backup script
echo -e "${YELLOW}💾 Creating backup script...${NC}"
cat > "$INSTALL_DIR/backup.sh" << 'EOF'
#!/bin/bash
# SmartWatts Edge Gateway Backup Script

BACKUP_DIR="/opt/smartwatts/backups"
DATA_DIR="/opt/smartwatts/data"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="smartwatts_backup_$TIMESTAMP.tar.gz"

echo "Creating backup: $BACKUP_FILE"

# Create backup
tar -czf "$BACKUP_DIR/$BACKUP_FILE" -C "$DATA_DIR" .

# Keep only last 30 backups
cd "$BACKUP_DIR"
ls -t smartwatts_backup_*.tar.gz | tail -n +31 | xargs -r rm

echo "Backup completed: $BACKUP_FILE"
EOF

chmod +x "$INSTALL_DIR/backup.sh"

# Create cron job for backups
echo -e "${YELLOW}⏰ Setting up backup cron job...${NC}"
echo "0 2 * * * $INSTALL_DIR/backup.sh" | crontab -u "$SERVICE_USER" -

# Create monitoring script
echo -e "${YELLOW}📊 Creating monitoring script...${NC}"
cat > "$INSTALL_DIR/monitor.sh" << 'EOF'
#!/bin/bash
# SmartWatts Edge Gateway Monitoring Script

echo "=== SmartWatts Edge Gateway Status ==="
echo "Timestamp: $(date)"
echo

echo "Service Status:"
systemctl status smartwatts-edge --no-pager -l
echo

echo "MQTT Broker Status:"
systemctl status mosquitto --no-pager -l
echo

echo "Disk Usage:"
df -h /opt/smartwatts
echo

echo "Memory Usage:"
free -h
echo

echo "CPU Usage:"
top -bn1 | grep "Cpu(s)"
echo

echo "Recent Logs:"
journalctl -u smartwatts-edge --since "1 hour ago" --no-pager -l | tail -20
EOF

chmod +x "$INSTALL_DIR/monitor.sh"

# Create update script
echo -e "${YELLOW}🔄 Creating update script...${NC}"
cat > "$INSTALL_DIR/update.sh" << 'EOF'
#!/bin/bash
# SmartWatts Edge Gateway Update Script

set -e

echo "Updating SmartWatts Edge Gateway..."

# Stop service
systemctl stop smartwatts-edge

# Backup current installation
./backup.sh

# Update code (assuming git repository)
if [ -d ".git" ]; then
    git pull origin main
else
    echo "Not a git repository. Manual update required."
    exit 1
fi

# Update Python dependencies
source venv/bin/activate
pip install -r requirements.txt

# Restart service
systemctl start smartwatts-edge

echo "Update completed successfully!"
EOF

chmod +x "$INSTALL_DIR/update.sh"

# Enable and start service
echo -e "${YELLOW}🚀 Starting SmartWatts Edge Gateway...${NC}"
systemctl daemon-reload
systemctl enable smartwatts-edge
systemctl start smartwatts-edge

# Wait for service to start
sleep 5

# Check service status
if systemctl is-active --quiet smartwatts-edge; then
    echo -e "${GREEN}✅ SmartWatts Edge Gateway started successfully!${NC}"
else
    echo -e "${RED}❌ Failed to start SmartWatts Edge Gateway${NC}"
    echo "Check logs with: journalctl -u smartwatts-edge -f"
    exit 1
fi

# Display installation summary
echo
echo -e "${GREEN}🎉 Installation completed successfully!${NC}"
echo "============================================================"
echo "Installation Directory: $INSTALL_DIR"
echo "Service User: $SERVICE_USER"
echo "Configuration: $INSTALL_DIR/config/edge-config.yml"
echo "Logs: $INSTALL_DIR/logs/"
echo "Data: $INSTALL_DIR/data/"
echo "Models: $INSTALL_DIR/models/"
echo
echo "Service Commands:"
echo "  Start:   systemctl start smartwatts-edge"
echo "  Stop:    systemctl stop smartwatts-edge"
echo "  Restart: systemctl restart smartwatts-edge"
echo "  Status:  systemctl status smartwatts-edge"
echo "  Logs:    journalctl -u smartwatts-edge -f"
echo
echo "Monitoring:"
echo "  Monitor: $INSTALL_DIR/monitor.sh"
echo "  Backup:  $INSTALL_DIR/backup.sh"
echo "  Update:  $INSTALL_DIR/update.sh"
echo
echo "API Endpoints:"
echo "  Health:  http://localhost:8080/api/v1/health"
echo "  Status:  http://localhost:8080/api/v1/status"
echo "  Metrics: http://localhost:9090/metrics"
echo
echo "MQTT Broker:"
echo "  Host: localhost"
echo "  Port: 1883"
echo "  Test: mosquitto_pub -h localhost -t test -m 'Hello World'"
echo
echo -e "${GREEN}🚀 SmartWatts Edge Gateway is ready for use!${NC}"
