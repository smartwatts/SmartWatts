#!/bin/bash

# SmartWatts Load Testing Script
# This script runs comprehensive load tests on all SmartWatts endpoints

set -e

echo "🚀 Starting SmartWatts Load Testing..."

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo "❌ k6 is not installed. Please install k6 first:"
    echo "   macOS: brew install k6"
    echo "   Ubuntu: sudo apt-get install k6"
    echo "   Or download from: https://k6.io/docs/getting-started/installation/"
    exit 1
fi

# Configuration
BASE_URL=${BASE_URL:-"http://localhost:3000"}
TEST_DURATION=${TEST_DURATION:-"10m"}
MAX_USERS=${MAX_USERS:-50}

echo "📊 Test Configuration:"
echo "   Base URL: $BASE_URL"
echo "   Duration: $TEST_DURATION"
echo "   Max Users: $MAX_USERS"
echo ""

# Create results directory
mkdir -p load-testing/results
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RESULTS_DIR="load-testing/results/test_$TIMESTAMP"
mkdir -p "$RESULTS_DIR"

echo "📁 Results will be saved to: $RESULTS_DIR"
echo ""

# Test 1: Basic Load Test
echo "🔥 Running Basic Load Test..."
k6 run \
  --out json="$RESULTS_DIR/basic_load_test.json" \
  --env BASE_URL="$BASE_URL" \
  load-testing/k6-load-test.js

# Test 2: Stress Test
echo "💥 Running Stress Test..."
k6 run \
  --out json="$RESULTS_DIR/stress_test.json" \
  --env BASE_URL="$BASE_URL" \
  -e STAGES="[{duration: '1m', target: 10}, {duration: '2m', target: 50}, {duration: '2m', target: 100}, {duration: '2m', target: 200}, {duration: '1m', target: 0}]" \
  load-testing/k6-load-test.js

# Test 3: Spike Test
echo "⚡ Running Spike Test..."
k6 run \
  --out json="$RESULTS_DIR/spike_test.json" \
  --env BASE_URL="$BASE_URL" \
  -e STAGES="[{duration: '1m', target: 10}, {duration: '30s', target: 200}, {duration: '1m', target: 10}, {duration: '30s', target: 200}, {duration: '1m', target: 10}]" \
  load-testing/k6-load-test.js

# Test 4: Soak Test
echo "🕐 Running Soak Test..."
k6 run \
  --out json="$RESULTS_DIR/soak_test.json" \
  --env BASE_URL="$BASE_URL" \
  -e STAGES="[{duration: '2m', target: 20}, {duration: '30m', target: 20}, {duration: '2m', target: 0}]" \
  load-testing/k6-load-test.js

# Generate summary report
echo "📈 Generating Load Test Summary..."

cat > "$RESULTS_DIR/summary.md" << EOF
# SmartWatts Load Test Results

**Test Date:** $(date)
**Base URL:** $BASE_URL
**Test Duration:** $TEST_DURATION
**Max Users:** $MAX_USERS

## Test Scenarios

### 1. Basic Load Test
- **Purpose:** Normal load testing
- **Duration:** 10 minutes
- **Max Users:** 50
- **Results:** basic_load_test.json

### 2. Stress Test
- **Purpose:** Find breaking point
- **Duration:** 8 minutes
- **Max Users:** 200
- **Results:** stress_test.json

### 3. Spike Test
- **Purpose:** Test sudden traffic spikes
- **Duration:** 4 minutes
- **Max Users:** 200 (spikes)
- **Results:** spike_test.json

### 4. Soak Test
- **Purpose:** Test system stability over time
- **Duration:** 34 minutes
- **Max Users:** 20 (sustained)
- **Results:** soak_test.json

## Key Metrics to Review

- **Response Time (p95):** Should be < 2 seconds
- **Error Rate:** Should be < 1%
- **Throughput:** Requests per second
- **Resource Usage:** CPU, Memory, Database connections

## Next Steps

1. Review JSON results files for detailed metrics
2. Check system resources during tests
3. Identify bottlenecks and optimize
4. Re-run tests after optimizations
5. Set up continuous load testing

EOF

echo "✅ Load testing completed!"
echo "📊 Results saved to: $RESULTS_DIR"
echo ""
echo "📋 Summary Report: $RESULTS_DIR/summary.md"
echo ""
echo "🔍 To analyze results:"
echo "   - View JSON files for detailed metrics"
echo "   - Check Grafana dashboards for system metrics"
echo "   - Review application logs for errors"
echo ""
echo "🚀 SmartWatts is ready for production load testing!"
