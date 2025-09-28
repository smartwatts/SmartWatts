#!/bin/bash

# SmartWatts Raspberry Pi 5 Automated Installation Script
# This script automates the complete setup process for SmartWatts on Raspberry Pi 5

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if running on Raspberry Pi
check_raspberry_pi() {
    print_header "Checking Raspberry Pi system..."
    
    if ! grep -q "Raspberry Pi" /proc/cpuinfo; then
        print_warning "This script is designed for Raspberry Pi. Continue anyway? (y/N)"
        read -r response
        if [[ ! "$response" =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
    
    # Check architecture
    ARCH=$(uname -m)
    if [[ "$ARCH" != "aarch64" ]]; then
        print_warning "Expected aarch64 architecture, found $ARCH"
    fi
    
    print_status "Raspberry Pi system detected"
}

# Update system packages
update_system() {
    print_header "Updating system packages..."
    
    sudo apt update
    sudo apt upgrade -y
    
    print_status "System updated successfully"
}

# Install required packages
install_packages() {
    print_header "Installing required packages..."
    
    sudo apt install -y \
        curl \
        wget \
        git \
        vim \
        htop \
        tree \
        unzip \
        software-properties-common \
        apt-transport-https \
        ca-certificates \
        gnupg \
        lsb-release \
        python3.11 \
        python3.11-pip \
        python3.11-venv \
        python3.11-dev \
        python3-numpy \
        python3-pandas \
        python3-matplotlib \
        python3-scipy \
        python3-sklearn \
        python3-serial \
        python3-paho-mqtt \
        postgresql \
        postgresql-contrib \
        mosquitto \
        mosquitto-clients
    
    print_status "Required packages installed"
}

# Install Docker
install_docker() {
    print_header "Installing Docker..."
    
    if command -v docker &> /dev/null; then
        print_status "Docker already installed"
        return
    fi
    
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
    
    # Install Docker Compose
    sudo apt install -y docker-compose
    
    print_status "Docker installed successfully"
}

# Configure PostgreSQL
configure_postgresql() {
    print_header "Configuring PostgreSQL..."
    
    # Start PostgreSQL
    sudo systemctl enable postgresql
    sudo systemctl start postgresql
    
    # Create databases and user
    sudo -u postgres psql << EOF
CREATE DATABASE smartwatts;
CREATE DATABASE smartwatts_users;
CREATE DATABASE smartwatts_energy;
CREATE DATABASE smartwatts_devices;
CREATE DATABASE smartwatts_analytics;
CREATE DATABASE smartwatts_billing;
CREATE DATABASE smartwatts_facility360;

CREATE USER smartwatts WITH PASSWORD 'smartwatts123';
GRANT ALL PRIVILEGES ON DATABASE smartwatts TO smartwatts;
GRANT ALL PRIVILEGES ON DATABASE smartwatts_users TO smartwatts;
GRANT ALL PRIVILEGES ON DATABASE smartwatts_energy TO smartwatts;
GRANT ALL PRIVILEGES ON DATABASE smartwatts_devices TO smartwatts;
GRANT ALL PRIVILEGES ON DATABASE smartwatts_analytics TO smartwatts;
GRANT ALL PRIVILEGES ON DATABASE smartwatts_billing TO smartwatts;
GRANT ALL PRIVILEGES ON DATABASE smartwatts_facility360 TO smartwatts;
\q
EOF
    
    print_status "PostgreSQL configured successfully"
}

# Configure MQTT Broker
configure_mosquitto() {
    print_header "Configuring MQTT Broker..."
    
    # Create Mosquitto configuration
    sudo tee /etc/mosquitto/mosquitto.conf > /dev/null << EOF
listener 1883
protocol mqtt

listener 9001
protocol websockets

allow_anonymous true
persistence true
persistence_location /var/lib/mosquitto/

log_dest file /var/log/mosquitto/mosquitto.log
log_type error
log_type warning
log_type notice
log_type information
EOF
    
    # Start Mosquitto
    sudo systemctl enable mosquitto
    sudo systemctl start mosquitto
    
    print_status "MQTT Broker configured successfully"
}

# Configure serial port access
configure_serial_ports() {
    print_header "Configuring serial port access..."
    
    # Add user to dialout group
    sudo usermod -aG dialout $USER
    
    # Create udev rules for USB devices
    sudo tee /etc/udev/rules.d/99-usb-serial.rules > /dev/null << EOF
SUBSYSTEM=="tty", ATTRS{idVendor}=="0403", ATTRS{idProduct}=="6001", SYMLINK+="ttyUSB%n", MODE="0666"
SUBSYSTEM=="tty", ATTRS{idVendor}=="1a86", ATTRS{idProduct}=="7523", SYMLINK+="ttyUSB%n", MODE="0666"
SUBSYSTEM=="tty", ATTRS{idVendor}=="10c4", ATTRS{idProduct}=="ea60", SYMLINK+="ttyUSB%n", MODE="0666"
EOF
    
    # Reload udev rules
    sudo udevadm control --reload-rules
    sudo udevadm trigger
    
    print_status "Serial port access configured"
}

# Install SmartWatts
install_smartwatts() {
    print_header "Installing SmartWatts..."
    
    # Clone repository if not exists
    if [ ! -d "/home/$USER/smartwatts" ]; then
        cd /home/$USER
        git clone https://github.com/your-repo/smartwatts.git
    fi
    
    cd /home/$USER/smartwatts
    
    # Set permissions
    sudo chown -R $USER:$USER /home/$USER/smartwatts
    chmod +x scripts/*.sh
    
    # Create Raspberry Pi specific configuration
    if [ ! -f "config/edge-gateway-pi.yml" ]; then
        cp config/edge-gateway.yml config/edge-gateway-pi.yml
        
        # Update configuration for Raspberry Pi
        sed -i 's/active: hardware/active: pi/' config/edge-gateway-pi.yml
        sed -i 's/localhost:8761/192.168.1.100:8761/' config/edge-gateway-pi.yml
    fi
    
    print_status "SmartWatts installed successfully"
}

# Create Python virtual environment
create_python_env() {
    print_header "Creating Python virtual environment..."
    
    cd /home/$USER/smartwatts
    
    # Create virtual environment
    python3.11 -m venv smartwatts-env
    source smartwatts-env/bin/activate
    
    # Install Python dependencies
    pip install --upgrade pip
    pip install \
        paho-mqtt \
        pyserial \
        numpy \
        pandas \
        matplotlib \
        scikit-learn \
        requests \
        flask \
        fastapi \
        uvicorn \
        docker \
        psycopg2-binary
    
    print_status "Python environment created successfully"
}

# Configure systemd services
configure_systemd() {
    print_header "Configuring systemd services..."
    
    # Create SmartWatts service
    sudo tee /etc/systemd/system/smartwatts-edge.service > /dev/null << EOF
[Unit]
Description=SmartWatts Edge Gateway
After=network.target docker.service postgresql.service mosquitto.service
Requires=docker.service postgresql.service mosquitto.service

[Service]
Type=simple
User=$USER
WorkingDirectory=/home/$USER/smartwatts
ExecStart=/usr/bin/docker-compose -f docker-compose.hardware.yml up
Restart=always
RestartSec=10
Environment=SMARTWATTS_CONFIG_DIR=/home/$USER/smartwatts/config

[Install]
WantedBy=multi-user.target
EOF
    
    # Enable services
    sudo systemctl daemon-reload
    sudo systemctl enable smartwatts-edge
    
    print_status "Systemd services configured"
}

# Configure firewall
configure_firewall() {
    print_header "Configuring firewall..."
    
    # Install UFW if not present
    if ! command -v ufw &> /dev/null; then
        sudo apt install -y ufw
    fi
    
    # Configure firewall rules
    sudo ufw --force enable
    sudo ufw allow ssh
    sudo ufw allow 1883  # MQTT
    sudo ufw allow 8080  # API Gateway
    sudo ufw allow 8088  # Edge Gateway
    sudo ufw allow 3000  # Frontend
    sudo ufw allow 5432  # PostgreSQL
    
    print_status "Firewall configured"
}

# Create management scripts
create_management_scripts() {
    print_header "Creating management scripts..."
    
    cd /home/$USER/smartwatts
    
    # Create start script
    cat > start-smartwatts.sh << 'EOF'
#!/bin/bash
cd /home/pi/smartwatts
docker-compose -f docker-compose.hardware.yml up -d
echo "SmartWatts started. Access dashboard at http://localhost:3000"
EOF
    
    # Create stop script
    cat > stop-smartwatts.sh << 'EOF'
#!/bin/bash
cd /home/pi/smartwatts
docker-compose -f docker-compose.hardware.yml down
echo "SmartWatts stopped"
EOF
    
    # Create status script
    cat > status-smartwatts.sh << 'EOF'
#!/bin/bash
echo "SmartWatts Status:"
echo "=================="
echo "Docker: $(systemctl is-active docker)"
echo "PostgreSQL: $(systemctl is-active postgresql)"
echo "MQTT: $(systemctl is-active mosquitto)"
echo "SmartWatts: $(systemctl is-active smartwatts-edge)"
echo ""
echo "Services:"
docker-compose -f docker-compose.hardware.yml ps
EOF
    
    # Create update script
    cat > update-smartwatts.sh << 'EOF'
#!/bin/bash
cd /home/pi/smartwatts
git pull origin main
docker-compose -f docker-compose.hardware.yml down
docker-compose -f docker-compose.hardware.yml pull
docker-compose -f docker-compose.hardware.yml up -d
echo "SmartWatts updated successfully"
EOF
    
    # Create backup script
    cat > backup-smartwatts.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/home/pi/backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# Backup databases
pg_dump -h localhost -U smartwatts smartwatts > "$BACKUP_DIR/smartwatts.sql"
pg_dump -h localhost -U smartwatts smartwatts_users > "$BACKUP_DIR/smartwatts_users.sql"
pg_dump -h localhost -U smartwatts smartwatts_energy > "$BACKUP_DIR/smartwatts_energy.sql"

# Backup configuration
cp -r /home/pi/smartwatts/config "$BACKUP_DIR/"

echo "Backup completed: $BACKUP_DIR"
EOF
    
    # Make scripts executable
    chmod +x *.sh
    
    print_status "Management scripts created"
}

# Optimize system performance
optimize_system() {
    print_header "Optimizing system performance..."
    
    # Increase swap space
    sudo dphys-swapfile swapoff
    sudo sed -i 's/CONF_SWAPSIZE=100/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile
    sudo dphys-swapfile setup
    sudo dphys-swapfile swapon
    
    # Optimize Docker
    sudo tee /etc/docker/daemon.json > /dev/null << EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "storage-driver": "overlay2"
}
EOF
    
    # Optimize PostgreSQL
    sudo tee -a /etc/postgresql/15/main/postgresql.conf > /dev/null << EOF

# SmartWatts optimizations
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
EOF
    
    # Restart services
    sudo systemctl restart docker
    sudo systemctl restart postgresql
    
    print_status "System optimized"
}

# Test installation
test_installation() {
    print_header "Testing installation..."
    
    # Test Docker
    if docker --version &> /dev/null; then
        print_status "Docker: OK"
    else
        print_error "Docker: FAILED"
    fi
    
    # Test PostgreSQL
    if sudo systemctl is-active postgresql &> /dev/null; then
        print_status "PostgreSQL: OK"
    else
        print_error "PostgreSQL: FAILED"
    fi
    
    # Test MQTT
    if sudo systemctl is-active mosquitto &> /dev/null; then
        print_status "MQTT: OK"
    else
        print_error "MQTT: FAILED"
    fi
    
    # Test MQTT communication
    if mosquitto_pub -h localhost -t "test/pi" -m "Test" &> /dev/null; then
        print_status "MQTT Communication: OK"
    else
        print_error "MQTT Communication: FAILED"
    fi
    
    # Test database connection
    if psql -h localhost -U smartwatts -d smartwatts -c "SELECT 1;" &> /dev/null; then
        print_status "Database Connection: OK"
    else
        print_error "Database Connection: FAILED"
    fi
    
    print_status "Installation testing completed"
}

# Main installation function
main() {
    print_header "Starting SmartWatts Raspberry Pi 5 Installation"
    echo "========================================================"
    
    check_raspberry_pi
    update_system
    install_packages
    install_docker
    configure_postgresql
    configure_mosquitto
    configure_serial_ports
    install_smartwatts
    create_python_env
    configure_systemd
    configure_firewall
    create_management_scripts
    optimize_system
    test_installation
    
    print_header "Installation Completed Successfully!"
    echo "============================================="
    echo ""
    echo "SmartWatts is now installed on your Raspberry Pi 5"
    echo ""
    echo "Next steps:"
    echo "1. Reboot your Raspberry Pi: sudo reboot"
    echo "2. After reboot, start SmartWatts: ./start-smartwatts.sh"
    echo "3. Access the dashboard: http://localhost:3000"
    echo "4. Configure your hardware in config/edge-gateway-pi.yml"
    echo ""
    echo "Management commands:"
    echo "- Start: ./start-smartwatts.sh"
    echo "- Stop: ./stop-smartwatts.sh"
    echo "- Status: ./status-smartwatts.sh"
    echo "- Update: ./update-smartwatts.sh"
    echo "- Backup: ./backup-smartwatts.sh"
    echo ""
    echo "For support, check the documentation in docs/"
}

# Run main function
main "$@"


