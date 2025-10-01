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
    mkdir -p data/{postgres,redis,edge-gateway,backups}
    
    # Set permissions
    chmod +x scripts/*.sh
    
    # Copy environment files if they don't exist
    if [ ! -f .env ]; then
        cp config/environment.env .env
        echo "✅ Environment configuration created"
    fi
    
    # Start services
    echo "Starting SmartWatts services..."
    docker-compose -f docker-compose.yml up -d
    
    # Wait for services to be ready
    echo "Waiting for services to start..."
    sleep 30
    
    # Check service health
    echo "Checking service health..."
    ./scripts/health-check.sh
    
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
    echo "To manage services:"
    echo "  • Start: ./scripts/start-smartwatts.sh"
    echo "  • Stop: ./scripts/stop-smartwatts.sh"
    echo "  • Health Check: ./scripts/health-check.sh"
    echo "  • Backup: ./scripts/backup-data.sh"
    echo ""
    echo "For hardware integration, see:"
    echo "  • docs/Hardware_Integration_Guide.md"
    echo "  • docs/Raspberry_Pi_5_Setup_Guide.md"
}

main "$@"




