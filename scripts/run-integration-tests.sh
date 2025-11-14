#!/bin/bash
set -e

echo "Starting SmartWatts Integration Tests..."

# Create results directory
mkdir -p results/integration-tests

# Start test containers
echo "Starting test infrastructure..."
docker-compose -f docker-compose.test.yml up -d

# Wait for services to be ready
echo "Waiting for services to be ready..."
./scripts/wait-for-services.sh

# Run integration tests
echo "Running integration tests..."
cd backend/integration-tests

# Run tests with Maven
mvn clean verify -P integration-tests \
    -Denergy.service.url=http://localhost:8082 \
    -Dbilling.service.url=http://localhost:8083 \
    -Ddevice.service.url=http://localhost:8084 \
    -Danalytics.service.url=http://localhost:8085 \
    -Dapi.gateway.url=http://localhost:8080

# Generate test report
echo "Generating test report..."
mvn surefire-report:report

# Copy results
cp -r target/surefire-reports/* ../../results/integration-tests/

# Cleanup
echo "Cleaning up test containers..."
cd ../..
docker-compose -f docker-compose.test.yml down

echo "Integration tests completed!"
echo "Results available in: results/integration-tests/"
echo "Test report: results/integration-tests/surefire-reports/index.html"








