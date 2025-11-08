# Load Testing Guide

## Overview

This directory contains load testing scripts and configurations for SmartWatts. Load testing helps ensure the system can handle expected traffic and identify performance bottlenecks.

## Tools

### JMeter
- **Version**: 5.6.2 or later
- **Location**: `jmeter/smartwatts-load-test.jmx`
- **Purpose**: API load testing

### K6 (Alternative)
- **Version**: Latest
- **Location**: `k6/` (if configured)
- **Purpose**: Modern load testing with JavaScript

## Test Scenarios

### 1. User Registration Load Test
- **Threads**: 50
- **Ramp-up**: 60 seconds
- **Loops**: 10 per thread
- **Endpoint**: `POST /api/v1/users/register`
- **Purpose**: Test user registration under load

### 2. User Login Load Test
- **Threads**: 100
- **Ramp-up**: 120 seconds
- **Loops**: 100 per thread
- **Endpoint**: `POST /api/v1/users/login`
- **Purpose**: Test authentication performance

### 3. Rate Limiting Test
- **Threads**: 10
- **Ramp-up**: 10 seconds
- **Loops**: 200 per thread
- **Endpoint**: `GET /api/v1/users/profile`
- **Purpose**: Verify rate limiting functionality

## Running Tests

### Prerequisites
1. Install JMeter:
   ```bash
   # macOS
   brew install jmeter
   
   # Linux
   sudo apt-get install jmeter
   
   # Or download from https://jmeter.apache.org/download_jmeter.cgi
   ```

2. Start SmartWatts services:
   ```bash
   cd backend
   docker-compose up -d
   ```

### Run JMeter Test
```bash
# Run test plan
jmeter -n -t load-testing/jmeter/smartwatts-load-test.jmx \
  -l load-testing/results/results.jtl \
  -e -o load-testing/results/html-report \
  -J API_GATEWAY_URL=http://localhost:8080

# View results
open load-testing/results/html-report/index.html
```

### Run with GUI (for development)
```bash
jmeter -t load-testing/jmeter/smartwatts-load-test.jmx
```

## Performance Targets

### Response Times
- **P50 (Median)**: < 200ms
- **P95**: < 500ms
- **P99**: < 1000ms

### Throughput
- **User Registration**: > 100 requests/second
- **User Login**: > 200 requests/second
- **API Gateway**: > 500 requests/second

### Error Rate
- **Target**: < 0.1%
- **Acceptable**: < 1%

### Resource Usage
- **CPU**: < 70% average
- **Memory**: < 80% average
- **Database Connections**: < 80% of pool size

## Monitoring

### Metrics to Monitor
1. **Response Times**
   - Average response time
   - Min/Max response time
   - Percentiles (P50, P95, P99)

2. **Throughput**
   - Requests per second
   - Transactions per second

3. **Error Rate**
   - HTTP error codes (4xx, 5xx)
   - Timeout errors
   - Connection errors

4. **Resource Usage**
   - CPU usage
   - Memory usage
   - Database connections
   - Network I/O

### Tools
- JMeter Listeners
- Grafana Dashboards
- Prometheus Metrics
- Application Logs

## Test Results

### Location
- **JMeter Results**: `load-testing/results/`
- **HTML Reports**: `load-testing/results/html-report/`
- **JTL Files**: `load-testing/results/*.jtl`

### Report Generation
```bash
# Generate HTML report from JTL file
jmeter -g load-testing/results/results.jtl \
  -o load-testing/results/html-report
```

## Continuous Integration

### GitHub Actions
```yaml
name: Load Test
on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM
  workflow_dispatch:

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Load Test
        run: |
          jmeter -n -t load-testing/jmeter/smartwatts-load-test.jmx \
            -l results.jtl \
            -e -o html-report
      - name: Upload Results
        uses: actions/upload-artifact@v3
        with:
          name: load-test-results
          path: html-report/
```

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Verify services are running
   - Check API Gateway URL
   - Verify firewall rules

2. **Timeout Errors**
   - Increase timeout values
   - Check database connection pool
   - Verify network latency

3. **Out of Memory**
   - Reduce thread count
   - Increase JMeter heap size: `-Xmx4g`
   - Use distributed testing

4. **Rate Limiting**
   - Adjust rate limit configuration
   - Use different IP addresses
   - Increase rate limit thresholds

## Best Practices

1. **Start Small**
   - Begin with low thread counts
   - Gradually increase load
   - Monitor system resources

2. **Test Realistic Scenarios**
   - Use realistic data
   - Test actual user workflows
   - Include think times

3. **Monitor Everything**
   - Application metrics
   - Infrastructure metrics
   - Database metrics
   - Network metrics

4. **Document Results**
   - Save test results
   - Document configuration
   - Track performance trends

## References

- [JMeter Documentation](https://jmeter.apache.org/usermanual/)
- [JMeter Best Practices](https://jmeter.apache.org/usermanual/best-practices.html)
- [Load Testing Guide](https://www.blazemeter.com/blog/load-testing-guide)


