#!/bin/bash

# SmartWatts Production Deployment for mysmartwatts.com
# Quick deployment script with your domain configuration

set -e

echo "🚀 Deploying SmartWatts to mysmartwatts.com"
echo "==========================================="
echo ""

# Configuration
DOMAIN="mysmartwatts.com"
EMAIL="admin@mysmartwatts.com"

echo "📋 Configuration:"
echo "   Domain: $DOMAIN"
echo "   Email: $EMAIL"
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
    echo "⚠️  Please don't run this script as root"
    echo "   Run as a regular user with sudo privileges"
    exit 1
fi

# Check prerequisites
echo "🔍 Checking prerequisites..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose and try again."
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p nginx/ssl
mkdir -p monitoring/grafana/provisioning/datasources
mkdir -p monitoring/grafana/provisioning/dashboards
mkdir -p monitoring/grafana/dashboards
mkdir -p load-testing/results
mkdir -p logs

# Generate SSL certificates for development
echo "🔐 Generating SSL certificates for mysmartwatts.com..."
./scripts/generate-ssl-certs.sh

# Create Docker network
echo "🌐 Creating Docker network..."
docker network create smartwatts-network 2>/dev/null || echo "   Network already exists"

# Deploy backend services
echo "🔧 Deploying backend services..."
cd backend
docker-compose up -d postgres redis eureka
echo "   Waiting for infrastructure services to start..."
sleep 30

docker-compose up -d
echo "   Waiting for all services to start..."
sleep 60

cd ..

# Deploy monitoring stack
echo "📊 Deploying monitoring stack..."
cd monitoring
docker-compose up -d
echo "   Waiting for monitoring services to start..."
sleep 30

cd ..

# Deploy load balancer
echo "⚖️  Deploying load balancer..."
cd nginx
docker-compose up -d
echo "   Waiting for load balancer to start..."
sleep 10

cd ..

# Start frontend
echo "🎨 Starting frontend..."
cd frontend
npm install
npm run build
PORT=3000 npm start &
FRONTEND_PID=$!
echo "   Frontend started with PID: $FRONTEND_PID"

cd ..

# Wait for all services to be ready
echo "⏳ Waiting for all services to be ready..."
sleep 30

# Run health checks
echo "🏥 Running health checks..."

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
    echo "   Main Application: https://$DOMAIN"
    echo "   API Gateway: https://$DOMAIN/api/"
    echo "   Prometheus: http://localhost:9090"
    echo "   Grafana: http://localhost:3001"
    echo ""
    echo "🔐 Default Credentials:"
    echo "   Grafana: admin / smartwatts2024"
    echo "   Application: admin@mysmartwatts.com / password"
    echo ""
    echo "📋 Next Steps:"
    echo "   1. Configure DNS: Point $DOMAIN to this server's IP"
    echo "   2. Set up SSL certificates for production:"
    echo "      - Update nginx.conf to use Let's Encrypt certificates"
    echo "      - Run: docker-compose -f nginx/docker-compose.yml up certbot"
    echo "   3. Configure monitoring alerts"
    echo "   4. Set up automated backups"
    echo "   5. Run load tests: cd load-testing && ./run-load-tests.sh"
    echo ""
    echo "🚀 SmartWatts is ready for production at https://$DOMAIN!"
else
    echo "⚠️  Some services are not healthy. Please check the logs:"
    echo "   docker-compose logs"
    echo "   Check individual service logs for issues"
fi

echo ""
echo "📋 For detailed deployment information, see: PRODUCTION_DEPLOYMENT.md"
