#!/bin/bash
set -e

echo "Deploying SmartWatts Monitoring Stack..."

# Create network if not exists
docker network create smartwatts-network 2>/dev/null || true

# Deploy monitoring stack
cd monitoring
docker-compose -f docker-compose.monitoring.yml up -d

# Wait for services
echo "Waiting for Prometheus..."
until curl -s http://localhost:9090/-/healthy > /dev/null; do
  sleep 2
done

echo "Waiting for Grafana..."
until curl -s http://localhost:3000/api/health > /dev/null; do
  sleep 2
done

echo "Waiting for Loki..."
until curl -s http://localhost:3100/ready > /dev/null; do
  sleep 2
done

echo "Monitoring stack deployed successfully!"
echo "Prometheus: http://localhost:9090"
echo "Grafana: http://localhost:3000 (admin/admin)"
echo "Loki: http://localhost:3100"








