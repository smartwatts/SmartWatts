# Log Aggregation Setup

## Overview

This document describes the log aggregation setup for SmartWatts using Loki, Promtail, and Grafana. Centralized log aggregation enables efficient log collection, storage, and analysis.

## Architecture

### Components
1. **Loki**: Log aggregation system
2. **Promtail**: Log shipper that collects logs and sends them to Loki
3. **Grafana**: Visualization and query interface for logs

## Setup

### 1. Docker Compose Configuration

#### Location
`monitoring/docker-compose.monitoring.yml`

#### Services
- **Loki**: Log aggregation server (port 3100)
- **Promtail**: Log shipper
- **Grafana**: Visualization (port 3000)

### 2. Loki Configuration

#### Location
`monitoring/loki/loki-config.yml`

#### Features
- File system storage
- 24-hour index period
- 168-hour retention period
- Alertmanager integration

### 3. Promtail Configuration

#### Location
`monitoring/promtail/promtail-config.yml`

#### Features
- System log collection
- SmartWatts service log collection
- Docker container log collection
- Automatic log discovery

### 4. Grafana Configuration

#### Location
`monitoring/grafana/datasources/prometheus.yml`

#### Features
- Loki datasource configuration
- Prometheus datasource configuration
- Default datasource selection

## Deployment

### Start Log Aggregation Stack
```bash
cd monitoring
docker-compose -f docker-compose.monitoring.yml up -d
```

### Verify Services
```bash
# Check Loki
curl http://localhost:3100/ready

# Check Promtail
curl http://localhost:9080/ready

# Check Grafana
curl http://localhost:3000/api/health
```

## Log Collection

### Service Logs
- **Location**: `/var/log/smartwatts/**/*.log`
- **Format**: JSON structured logs
- **Collection**: Automatic via Promtail

### Docker Logs
- **Location**: Docker container logs
- **Format**: Standard Docker log format
- **Collection**: Automatic via Promtail Docker SD

### System Logs
- **Location**: `/var/log/*.log`
- **Format**: Standard system log format
- **Collection**: Automatic via Promtail

## Log Queries

### LogQL Examples

#### Query All Logs
```logql
{job="smartwatts"}
```

#### Query by Service
```logql
{job="smartwatts", service="user-service"}
```

#### Query by Log Level
```logql
{job="smartwatts"} |= "ERROR"
```

#### Query by Time Range
```logql
{job="smartwatts"} [5m]
```

#### Aggregate Logs
```logql
sum(count_over_time({job="smartwatts"}[1m]))
```

## Grafana Dashboards

### Log Dashboard
- **Purpose**: View and search logs
- **Features**:
  - Log search
  - Log filtering
  - Log level filtering
  - Time range selection

### Service Dashboard
- **Purpose**: Monitor service logs
- **Features**:
  - Service-specific logs
  - Error rate monitoring
  - Log volume monitoring

## Best Practices

### 1. Structured Logging
- Use JSON format for logs
- Include relevant context
- Use consistent log levels

### 2. Log Levels
- **ERROR**: Critical errors
- **WARN**: Warning conditions
- **INFO**: Informational messages
- **DEBUG**: Detailed debugging

### 3. Log Retention
- Configure appropriate retention periods
- Archive old logs
- Compress log files

### 4. Log Rotation
- Rotate logs regularly
- Limit log file sizes
- Clean up old log files

## Monitoring

### Log Volume
- Monitor log volume per service
- Alert on excessive log volume
- Optimize log verbosity

### Log Errors
- Monitor error log rates
- Alert on error spikes
- Track error trends

## Summary

### ✅ Setup Status
- **Loki**: Configured and running ✅
- **Promtail**: Configured and running ✅
- **Grafana**: Configured and running ✅
- **Log Collection**: Automatic ✅
- **Log Queries**: LogQL support ✅

### Status
**✅ LOG AGGREGATION SETUP COMPLETE**

Centralized log aggregation is fully configured with Loki, Promtail, and Grafana for efficient log collection, storage, and analysis.


