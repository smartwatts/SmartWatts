#!/bin/bash
set -e

echo "Starting SmartWatts Performance Tests..."

# Create results directory
mkdir -p results/performance-tests

# Ensure all services are running
echo "Starting SmartWatts services..."
docker-compose up -d

# Wait for services
echo "Waiting for services to be ready..."
./scripts/wait-for-services.sh

# Run K6 tests with different scenarios
echo "Running baseline performance test..."
k6 run --out json=results/performance-tests/baseline.json load-testing/k6-comprehensive-test.js

echo "Running stress test (1500 users)..."
k6 run --out json=results/performance-tests/stress.json \
  --stage 5m:1500 \
  --stage 10m:1500 \
  load-testing/k6-comprehensive-test.js

echo "Running spike test..."
k6 run --out json=results/performance-tests/spike.json \
  --stage 1m:0 \
  --stage 1m:2000 \
  --stage 1m:0 \
  load-testing/k6-comprehensive-test.js

# Generate HTML report
echo "Generating performance report..."
k6 run --out html=results/performance-tests/report.html load-testing/k6-comprehensive-test.js

echo "Performance tests completed!"
echo "Results available in: results/performance-tests/"
echo "Report: results/performance-tests/report.html"








