#!/bin/bash

# SmartWatts Quick Start Production Script
# This script quickly deploys SmartWatts with all production features

set -e

echo "🚀 SmartWatts Quick Start Production Deployment"
echo "=============================================="
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    echo "⚠️  Please don't run this script as root"
    echo "   Run as a regular user with sudo privileges"
    exit 1
fi

# Configuration
DOMAIN=${1:-"mysmartwatts.com"}
EMAIL=${2:-"admin@mysmartwatts.com"}

echo "📋 Configuration:"
echo "   Domain: $DOMAIN"
echo "   Email: $EMAIL"
echo ""

# Update system packages
echo "📦 Updating system packages..."
sudo apt-get update -y

# Install required packages
echo "🔧 Installing required packages..."
sudo apt-get install -y \
    docker.io \
    docker-compose \
    curl \
    wget \
    unzip \
    certbot \
    python3-certbot-nginx

# Start Docker
echo "🐳 Starting Docker..."
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Install k6 for load testing
echo "🧪 Installing k6 for load testing..."
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update -y
sudo apt-get install -y k6

# Clone or update SmartWatts repository
if [ -d "mySmartWatts" ]; then
    echo "📁 Updating existing SmartWatts repository..."
    cd mySmartWatts
    git pull
else
    echo "📁 Cloning SmartWatts repository..."
    git clone https://github.com/your-org/mySmartWatts.git
    cd mySmartWatts
fi

# Make scripts executable
echo "🔧 Making scripts executable..."
chmod +x scripts/*.sh
chmod +x load-testing/*.sh

# Generate SSL certificates
echo "🔐 Setting up SSL certificates..."
if [ "$DOMAIN" != "mysmartwatts.com" ]; then
    echo "   Using self-signed certificates for development"
    ./scripts/generate-ssl-certs.sh
else
    echo "   Will use Let's Encrypt for production SSL"
fi

# Deploy the complete platform
echo "🚀 Deploying SmartWatts platform..."
./scripts/deploy-production.sh

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 60

# Run basic health checks
echo "🏥 Running health checks..."

# Check if services are responding
services=(
    "API Gateway:http://localhost:8080/actuator/health"
    "User Service:http://localhost:8081/actuator/health"
    "Energy Service:http://localhost:8082/actuator/health"
    "Analytics Service:http://localhost:8084/actuator/health"
    "Frontend:http://localhost:3000"
    "Nginx:http://localhost"
    "Prometheus:http://localhost:9090"
    "Grafana:http://localhost:3001"
)

all_healthy=true

for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    url=$(echo $service | cut -d: -f2-)
    
    if curl -f -s "$url" > /dev/null 2>&1; then
        echo "   ✅ $name: Healthy"
    else
        echo "   ❌ $name: Unhealthy"
        all_healthy=false
    fi
done

echo ""

if [ "$all_healthy" = true ]; then
    echo "🎉 SmartWatts deployment completed successfully!"
    echo ""
    echo "🌐 Access URLs:"
    echo "   Main Application: http://localhost"
    echo "   API Gateway: http://localhost/api/"
    echo "   Prometheus: http://localhost:9090"
    echo "   Grafana: http://localhost:3001"
    echo ""
    echo "🔐 Default Credentials:"
    echo "   Grafana: admin / smartwatts2024"
    echo "   Application: admin@mysmartwatts.com / password"
    echo ""
    echo "📊 Next Steps:"
    echo "   1. Configure DNS to point $DOMAIN to this server"
    echo "   2. Set up SSL certificates for production"
    echo "   3. Configure monitoring alerts"
    echo "   4. Set up automated backups"
    echo "   5. Run load tests: cd load-testing && ./run-load-tests.sh"
    echo ""
    echo "🚀 SmartWatts is ready for production!"
else
    echo "⚠️  Some services are not healthy. Please check the logs:"
    echo "   docker-compose logs"
    echo "   Check individual service logs for issues"
fi

echo ""
echo "📋 For detailed deployment information, see: deployment-summary.md"
