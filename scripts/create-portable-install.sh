#!/bin/bash

# SmartWatts Portable Installation Creator
# Creates a plug-and-play flash drive installation

set -e

echo "Creating SmartWatts Portable Installation..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_header() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if flash drive is mounted
check_flash_drive() {
    print_header "Checking for flash drive..."
    
    if [ -z "$1" ]; then
        echo "Usage: $0 /path/to/flash/drive"
        echo "Example: $0 /media/usb"
        exit 1
    fi
    
    FLASH_DRIVE="$1"
    
    if [ ! -d "$FLASH_DRIVE" ]; then
        print_error "Flash drive not found at $FLASH_DRIVE"
        exit 1
    fi
    
    print_status "Flash drive found at $FLASH_DRIVE"
}

# Create portable directory structure
create_portable_structure() {
    print_header "Creating portable directory structure..."
    
    PORTABLE_DIR="$FLASH_DRIVE/smartwatts-portable"
    
    # Create main directories
    mkdir -p "$PORTABLE_DIR"/{bin,config,data,logs,scripts,docs}
    mkdir -p "$PORTABLE_DIR"/data/{postgres,redis,mqtt}
    mkdir -p "$PORTABLE_DIR"/config/{edge-gateway,mqtt}
    
    print_status "Directory structure created"
}

# Copy application files
copy_application_files() {
    print_header "Copying application files..."
    
    # Copy edge gateway
    cp -r backend/edge-gateway "$PORTABLE_DIR/"
    
    # Copy configuration files
    cp config/edge-gateway.yml "$PORTABLE_DIR/config/"
    cp config/mosquitto.conf "$PORTABLE_DIR/config/mqtt/"
    
    # Copy Docker Compose file
    cp docker-compose.hardware.yml "$PORTABLE_DIR/"
    
    # Copy documentation
    cp HARDWARE_INTEGRATION_GUIDE.md "$PORTABLE_DIR/docs/"
    cp HARDWARE_QUICK_START.md "$PORTABLE_DIR/docs/"
    
    print_status "Application files copied"
}

# Create portable startup script
create_startup_script() {
    print_header "Creating portable startup script..."
    
    cat > "$PORTABLE_DIR/start-smartwatts.sh" << 'EOF'
#!/bin/bash

# SmartWatts Portable Startup Script
# This script starts SmartWatts from a flash drive

set -e

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "Starting SmartWatts Portable Installation..."
echo "============================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker first."
    echo "On Windows: Start Docker Desktop"
    echo "On Mac: Start Docker Desktop"
    echo "On Linux: sudo systemctl start docker"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 17 or later."
    echo "Download from: https://adoptium.net/"
    exit 1
fi

# Set up environment variables
export SMARTWATTS_DATA_DIR="$SCRIPT_DIR/data"
export SMARTWATTS_CONFIG_DIR="$SCRIPT_DIR/config"
export SMARTWATTS_LOGS_DIR="$SCRIPT_DIR/logs"

# Create data directories
mkdir -p "$SMARTWATTS_DATA_DIR"/{postgres,redis,mqtt}
mkdir -p "$SMARTWATTS_LOGS_DIR"

# Set permissions
chmod -R 755 "$SMARTWATTS_DATA_DIR"
chmod -R 755 "$SMARTWATTS_LOGS_DIR"

# Start services
echo "Starting SmartWatts services..."
docker-compose -f docker-compose.hardware.yml up -d

# Wait for services to start
echo "Waiting for services to start..."
sleep 30

# Check service health
echo "Checking service health..."

# Check API Gateway
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "API Gateway: UP"
else
    echo "API Gateway: DOWN"
fi

# Check Edge Gateway
if curl -s http://localhost:8088/actuator/health | grep -q "UP"; then
    echo "Edge Gateway: UP"
else
    echo "Edge Gateway: DOWN"
fi

# Check Frontend
if curl -s http://localhost:3000 | grep -q "SmartWatts"; then
    echo "Frontend: UP"
else
    echo "Frontend: DOWN"
fi

echo ""
echo "SmartWatts is now running!"
echo "=========================="
echo "Dashboard: http://localhost:3000"
echo "API Gateway: http://localhost:8080"
echo "Edge Gateway: http://localhost:8088"
echo "MQTT Broker: localhost:1883"
echo ""
echo "To stop SmartWatts, run: ./stop-smartwatts.sh"
echo "To view logs, run: ./view-logs.sh"
EOF

    chmod +x "$PORTABLE_DIR/start-smartwatts.sh"
    
    print_status "Startup script created"
}

# Create stop script
create_stop_script() {
    print_header "Creating stop script..."
    
    cat > "$PORTABLE_DIR/stop-smartwatts.sh" << 'EOF'
#!/bin/bash

# SmartWatts Portable Stop Script

echo "Stopping SmartWatts services..."

# Stop Docker Compose services
docker-compose -f docker-compose.hardware.yml down

echo "SmartWatts stopped successfully!"
EOF

    chmod +x "$PORTABLE_DIR/stop-smartwatts.sh"
    
    print_status "Stop script created"
}

# Create log viewer script
create_log_script() {
    print_header "Creating log viewer script..."
    
    cat > "$PORTABLE_DIR/view-logs.sh" << 'EOF'
#!/bin/bash

# SmartWatts Portable Log Viewer

echo "SmartWatts Log Viewer"
echo "===================="
echo "1. View all logs"
echo "2. View API Gateway logs"
echo "3. View Edge Gateway logs"
echo "4. View MQTT logs"
echo "5. Exit"
echo ""

read -p "Select option (1-5): " choice

case $choice in
    1)
        docker-compose -f docker-compose.hardware.yml logs -f
        ;;
    2)
        docker logs smartwatts-api-gateway -f
        ;;
    3)
        docker logs smartwatts-edge-gateway -f
        ;;
    4)
        docker logs smartwatts-mqtt -f
        ;;
    5)
        exit 0
        ;;
    *)
        echo "Invalid option"
        exit 1
        ;;
esac
EOF

    chmod +x "$PORTABLE_DIR/view-logs.sh"
    
    print_status "Log viewer script created"
}

# Create Windows batch files
create_windows_scripts() {
    print_header "Creating Windows batch files..."
    
    # Start script for Windows
    cat > "$PORTABLE_DIR/start-smartwatts.bat" << 'EOF'
@echo off
echo Starting SmartWatts Portable Installation...
echo =============================================

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed. Please install Java 17 or later.
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Set up environment variables
set SMARTWATTS_DATA_DIR=%~dp0data
set SMARTWATTS_CONFIG_DIR=%~dp0config
set SMARTWATTS_LOGS_DIR=%~dp0logs

REM Create data directories
if not exist "%SMARTWATTS_DATA_DIR%\postgres" mkdir "%SMARTWATTS_DATA_DIR%\postgres"
if not exist "%SMARTWATTS_DATA_DIR%\redis" mkdir "%SMARTWATTS_DATA_DIR%\redis"
if not exist "%SMARTWATTS_DATA_DIR%\mqtt" mkdir "%SMARTWATTS_DATA_DIR%\mqtt"
if not exist "%SMARTWATTS_LOGS_DIR%" mkdir "%SMARTWATTS_LOGS_DIR%"

REM Start services
echo Starting SmartWatts services...
docker-compose -f docker-compose.hardware.yml up -d

REM Wait for services to start
echo Waiting for services to start...
timeout /t 30 /nobreak >nul

REM Check service health
echo Checking service health...

REM Check API Gateway
curl -s http://localhost:8080/actuator/health | findstr "UP" >nul
if %errorlevel% equ 0 (
    echo API Gateway: UP
) else (
    echo API Gateway: DOWN
)

REM Check Edge Gateway
curl -s http://localhost:8088/actuator/health | findstr "UP" >nul
if %errorlevel% equ 0 (
    echo Edge Gateway: UP
) else (
    echo Edge Gateway: DOWN
)

REM Check Frontend
curl -s http://localhost:3000 | findstr "SmartWatts" >nul
if %errorlevel% equ 0 (
    echo Frontend: UP
) else (
    echo Frontend: DOWN
)

echo.
echo SmartWatts is now running!
echo ==========================
echo Dashboard: http://localhost:3000
echo API Gateway: http://localhost:8080
echo Edge Gateway: http://localhost:8088
echo MQTT Broker: localhost:1883
echo.
echo To stop SmartWatts, run: stop-smartwatts.bat
echo To view logs, run: view-logs.bat
echo.
pause
EOF

    # Stop script for Windows
    cat > "$PORTABLE_DIR/stop-smartwatts.bat" << 'EOF'
@echo off
echo Stopping SmartWatts services...

docker-compose -f docker-compose.hardware.yml down

echo SmartWatts stopped successfully!
pause
EOF

    print_status "Windows batch files created"
}

# Create README for portable installation
create_portable_readme() {
    print_header "Creating portable README..."
    
    cat > "$PORTABLE_DIR/README.txt" << 'EOF'
SmartWatts Portable Installation
================================

This is a portable installation of SmartWatts that can run from any computer with Docker installed.

Requirements:
- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Java 17 or later
- 4GB RAM minimum
- 2GB free disk space

Quick Start:
1. Make sure Docker is running
2. Run start-smartwatts.sh (Linux/Mac) or start-smartwatts.bat (Windows)
3. Open http://localhost:3000 in your browser

Hardware Integration:
1. Connect your hardware (inverters, meters, etc.)
2. Edit config/edge-gateway.yml with your device settings
3. Restart the edge gateway

Configuration:
- Main config: config/edge-gateway.yml
- MQTT config: config/mqtt/mosquitto.conf
- Data storage: data/
- Logs: logs/

Troubleshooting:
- Check logs: ./view-logs.sh (Linux/Mac) or view-logs.bat (Windows)
- Restart services: ./stop-smartwatts.sh && ./start-smartwatts.sh
- Check Docker: docker ps

Support:
- Documentation: docs/
- GitHub: https://github.com/your-repo/smartwatts
- Email: support@mysmartwatts.com

Version: 1.0.0
Last Updated: January 2025
EOF

    print_status "Portable README created"
}

# Create installer script
create_installer() {
    print_header "Creating installer script..."
    
    cat > "$PORTABLE_DIR/install.sh" << 'EOF'
#!/bin/bash

# SmartWatts Portable Installer
# Installs SmartWatts on the current system

set -e

echo "SmartWatts Portable Installer"
echo "============================="

# Check if running as root
if [[ $EUID -eq 0 ]]; then
    echo "Error: This script should not be run as root"
    exit 1
fi

# Check OS
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "Linux detected"
    OS="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    echo "macOS detected"
    OS="macos"
else
    echo "Error: Unsupported operating system: $OSTYPE"
    exit 1
fi

# Install Docker if not present
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    if [[ "$OS" == "linux" ]]; then
        curl -fsSL https://get.docker.com -o get-docker.sh
        sudo sh get-docker.sh
        sudo usermod -aG docker $USER
        rm get-docker.sh
    elif [[ "$OS" == "macos" ]]; then
        echo "Please install Docker Desktop from https://www.docker.com/products/docker-desktop"
        exit 1
    fi
fi

# Install Java if not present
if ! command -v java &> /dev/null; then
    echo "Installing Java 17..."
    if [[ "$OS" == "linux" ]]; then
        sudo apt update
        sudo apt install -y openjdk-17-jdk
    elif [[ "$OS" == "macos" ]]; then
        brew install openjdk@17
    fi
fi

echo "Installation completed!"
echo "Please log out and log back in, then run ./start-smartwatts.sh"
EOF

    chmod +x "$PORTABLE_DIR/install.sh"
    
    print_status "Installer script created"
}

# Main function
main() {
    if [ $# -eq 0 ]; then
        echo "Usage: $0 /path/to/flash/drive"
        echo "Example: $0 /media/usb"
        exit 1
    fi
    
    check_flash_drive "$1"
    create_portable_structure
    copy_application_files
    create_startup_script
    create_stop_script
    create_log_script
    create_windows_scripts
    create_portable_readme
    create_installer
    
    print_status "Portable installation created successfully!"
    print_status "Location: $PORTABLE_DIR"
    print_status ""
    print_status "To use:"
    print_status "1. Eject the flash drive safely"
    print_status "2. Insert into any computer with Docker"
    print_status "3. Run start-smartwatts.sh (Linux/Mac) or start-smartwatts.bat (Windows)"
}

# Run main function
main "$@"




