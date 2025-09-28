#!/bin/bash

# SmartWatts Hardware Deployment Script
# This script helps you deploy SmartWatts to real hardware

set -e

echo "🚀 SmartWatts Hardware Deployment Script"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Check if running as root
check_root() {
    if [[ $EUID -eq 0 ]]; then
        print_error "This script should not be run as root"
        exit 1
    fi
}

# Check system requirements
check_requirements() {
    print_header "Checking system requirements..."
    
    # Check OS
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        print_status "Linux detected: $(uname -a)"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        print_status "macOS detected: $(uname -a)"
    else
        print_error "Unsupported operating system: $OSTYPE"
        exit 1
    fi
    
    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_status "Java found: $JAVA_VERSION"
    else
        print_error "Java not found. Please install Java 17 or later"
        exit 1
    fi
    
    # Check Docker
    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | cut -d',' -f1)
        print_status "Docker found: $DOCKER_VERSION"
    else
        print_warning "Docker not found. Some features may not work"
    fi
    
    # Check serial ports
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        SERIAL_PORTS=$(ls /dev/ttyUSB* /dev/ttyACM* 2>/dev/null || echo "None")
        print_status "Serial ports found: $SERIAL_PORTS"
    fi
}

# Install dependencies
install_dependencies() {
    print_header "Installing dependencies..."
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Update package list
        print_status "Updating package list..."
        sudo apt update
        
        # Install Java 17
        if ! command -v java &> /dev/null; then
            print_status "Installing Java 17..."
            sudo apt install -y openjdk-17-jdk
        fi
        
        # Install Docker
        if ! command -v docker &> /dev/null; then
            print_status "Installing Docker..."
            curl -fsSL https://get.docker.com -o get-docker.sh
            sudo sh get-docker.sh
            sudo usermod -aG docker $USER
            rm get-docker.sh
        fi
        
        # Install Docker Compose
        if ! command -v docker-compose &> /dev/null; then
            print_status "Installing Docker Compose..."
            sudo apt install -y docker-compose
        fi
        
        # Install serial port tools
        print_status "Installing serial port tools..."
        sudo apt install -y minicom screen
        
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # Check if Homebrew is installed
        if ! command -v brew &> /dev/null; then
            print_status "Installing Homebrew..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        fi
        
        # Install Java
        if ! command -v java &> /dev/null; then
            print_status "Installing Java 17..."
            brew install openjdk@17
        fi
        
        # Install Docker
        if ! command -v docker &> /dev/null; then
            print_status "Installing Docker..."
            brew install --cask docker
        fi
    fi
}

# Configure serial ports
configure_serial_ports() {
    print_header "Configuring serial ports..."
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Add user to dialout group
        print_status "Adding user to dialout group..."
        sudo usermod -aG dialout $USER
        
        # Set permissions for serial ports
        print_status "Setting serial port permissions..."
        for port in /dev/ttyUSB* /dev/ttyACM*; do
            if [[ -e "$port" ]]; then
                sudo chmod 666 "$port"
                print_status "Set permissions for $port"
            fi
        done
        
        # Enable serial port on Raspberry Pi
        if [[ -f /boot/config.txt ]]; then
            print_status "Configuring Raspberry Pi serial port..."
            if ! grep -q "enable_uart=1" /boot/config.txt; then
                echo "enable_uart=1" | sudo tee -a /boot/config.txt
                print_warning "Serial port enabled. Reboot required."
            fi
        fi
    fi
}

# Build SmartWatts
build_smartwatts() {
    print_header "Building SmartWatts..."
    
    # Build edge gateway
    print_status "Building edge gateway..."
    cd backend/edge-gateway
    ./gradlew build -x test
    
    # Build other services
    print_status "Building other services..."
    cd ../..
    docker-compose -f backend/docker-compose.yml build
    
    print_status "SmartWatts build completed"
}

# Configure hardware
configure_hardware() {
    print_header "Configuring hardware integration..."
    
    # Create hardware configuration
    print_status "Creating hardware configuration..."
    
    # Example configuration for SMA Sunny Boy
    cat > backend/edge-gateway/src/main/resources/application-hardware.yml << EOF
# Hardware-specific configuration
rs485:
  devices:
    sma-sunny-boy:
      port: "/dev/ttyUSB0"
      baud-rate: 9600
      unit-id: 1
      start-address: 40000
      register-count: 20
      device-type: "SOLAR_INVERTER"
      manufacturer: "SMA"
      model: "Sunny Boy 3.0"
      enabled: true
    fronius-symo:
      port: "/dev/ttyUSB1"
      baud-rate: 19200
      unit-id: 1
      start-address: 50000
      register-count: 25
      device-type: "SOLAR_INVERTER"
      manufacturer: "Fronius"
      model: "Symo 10.0-3-M"
      enabled: true
    smart-meter:
      port: "/dev/ttyUSB2"
      baud-rate: 9600
      unit-id: 1
      start-address: 1000
      register-count: 10
      device-type: "SMART_METER"
      manufacturer: "Landis+Gyr"
      model: "E650"
      enabled: true

mqtt:
  broker:
    url: "tcp://localhost:1883"
  devices:
    temperature-sensor:
      topic: "sensors/temp/001/data"
      device-type: "TEMPERATURE_SENSOR"
      enabled: true
    power-meter:
      topic: "meters/power/001/data"
      device-type: "POWER_METER"
      enabled: true
EOF

    print_status "Hardware configuration created"
}

# Start MQTT broker
start_mqtt_broker() {
    print_header "Starting MQTT broker..."
    
    # Start MQTT broker
    docker run -d \
        --name smartwatts-mqtt \
        -p 1883:1883 \
        -p 9001:9001 \
        eclipse-mosquitto:2.0
    
    print_status "MQTT broker started on port 1883"
}

# Deploy SmartWatts
deploy_smartwatts() {
    print_header "Deploying SmartWatts..."
    
    # Start services
    print_status "Starting SmartWatts services..."
    docker-compose -f backend/docker-compose.yml up -d
    
    # Wait for services to start
    print_status "Waiting for services to start..."
    sleep 30
    
    # Check service health
    print_status "Checking service health..."
    
    # Check API Gateway
    if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
        print_status "API Gateway: UP"
    else
        print_error "API Gateway: DOWN"
    fi
    
    # Check Edge Gateway
    if curl -s http://localhost:8088/actuator/health | grep -q "UP"; then
        print_status "Edge Gateway: UP"
    else
        print_error "Edge Gateway: DOWN"
    fi
    
    # Check Frontend
    if curl -s http://localhost:3000 | grep -q "SmartWatts"; then
        print_status "Frontend: UP"
    else
        print_error "Frontend: DOWN"
    fi
}

# Test hardware integration
test_hardware() {
    print_header "Testing hardware integration..."
    
    # Test serial ports
    print_status "Testing serial ports..."
    for port in /dev/ttyUSB* /dev/ttyACM*; do
        if [[ -e "$port" ]]; then
            print_status "Found serial port: $port"
        fi
    done
    
    # Test MQTT
    print_status "Testing MQTT broker..."
    if docker exec smartwatts-mqtt mosquitto_pub -h localhost -t "test/topic" -m "Hello MQTT"; then
        print_status "MQTT broker: Working"
    else
        print_error "MQTT broker: Not working"
    fi
    
    # Test device discovery
    print_status "Testing device discovery..."
    if curl -s http://localhost:8088/api/v1/devices/discovered | grep -q "devices"; then
        print_status "Device discovery: Working"
    else
        print_warning "Device discovery: No devices found (this is normal if no hardware is connected)"
    fi
}

# Create systemd service
create_systemd_service() {
    print_header "Creating systemd service..."
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Create systemd service file
        sudo tee /etc/systemd/system/smartwatts-edge.service > /dev/null << EOF
[Unit]
Description=SmartWatts Edge Gateway
After=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=$(pwd)/backend/edge-gateway
ExecStart=/usr/bin/java -jar build/libs/edge-gateway-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
Environment=SPRING_PROFILES_ACTIVE=hardware

[Install]
WantedBy=multi-user.target
EOF

        # Enable service
        sudo systemctl daemon-reload
        sudo systemctl enable smartwatts-edge
        
        print_status "Systemd service created and enabled"
    fi
}

# Main deployment function
main() {
    print_header "Starting SmartWatts hardware deployment..."
    
    check_root
    check_requirements
    install_dependencies
    configure_serial_ports
    build_smartwatts
    configure_hardware
    start_mqtt_broker
    deploy_smartwatts
    test_hardware
    create_systemd_service
    
    print_status "SmartWatts hardware deployment completed!"
    print_status "Access the dashboard at: http://localhost:3000"
    print_status "API Gateway health: http://localhost:8080/actuator/health"
    print_status "Edge Gateway health: http://localhost:8088/actuator/health"
    print_status "MQTT broker: localhost:1883"
    
    print_warning "Next steps:"
    print_warning "1. Connect your hardware (inverters, meters, etc.)"
    print_warning "2. Update the hardware configuration in application-hardware.yml"
    print_warning "3. Restart the edge gateway: sudo systemctl restart smartwatts-edge"
    print_warning "4. Check the logs: sudo journalctl -u smartwatts-edge -f"
}

# Run main function
main "$@"


