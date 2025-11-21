# SmartWatts Monitoring Setup Guide

## Overview

This guide explains how to set up comprehensive monitoring for SmartWatts on GCP Cloud Run using native GCP Cloud Monitoring, Cloud Logging, and Error Reporting.

## Prerequisites

- GCP Project with billing enabled
- `gcloud` CLI installed and configured
- Appropriate IAM permissions (Monitoring Admin, Logs Writer)

## Monitoring Components

### 1. Cloud Logging

Cloud Logging is automatically enabled for Cloud Run services. All logs are automatically collected.

**View Logs:**
```bash
# View logs for a specific service
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=api-gateway" \
    --project=smartwatts-staging \
    --limit=50
```

**Create Log-Based Metrics:**
```bash
# Error count metric
gcloud logging metrics create staging_error_count \
    --project=smartwatts-staging \
    --description="Count of error logs" \
    --log-filter='severity>=ERROR AND resource.type="cloud_run_revision"'

# HTTP 5xx errors
gcloud logging metrics create staging_http_5xx_count \
    --project=smartwatts-staging \
    --description="Count of HTTP 5xx errors" \
    --log-filter='httpRequest.status>=500 AND resource.type="cloud_run_revision"'

# HTTP 4xx errors
gcloud logging metrics create staging_http_4xx_count \
    --project=smartwatts-staging \
    --description="Count of HTTP 4xx errors" \
    --log-filter='httpRequest.status>=400 AND httpRequest.status<500 AND resource.type="cloud_run_revision"'
```

### 2. Cloud Monitoring

#### Uptime Checks

Uptime checks can be created via the GCP Console or API:

**Via Console:**
1. Go to: https://console.cloud.google.com/monitoring/uptime
2. Click "Create Uptime Check"
3. Configure:
   - Name: `staging-{service-name}-uptime`
   - Resource type: URL
   - URL: `https://{service-url}/actuator/health`
   - Check interval: 60 seconds
   - Timeout: 10 seconds

**Services to Monitor:**
- api-gateway: https://api-gateway-3daykcsw5a-ew.a.run.app/actuator/health
- user-service: https://user-service-3daykcsw5a-ew.a.run.app/actuator/health
- energy-service: https://energy-service-3daykcsw5a-ew.a.run.app/actuator/health
- device-service: https://device-service-3daykcsw5a-ew.a.run.app/actuator/health
- analytics-service: https://analytics-service-3daykcsw5a-ew.a.run.app/actuator/health
- billing-service: https://billing-service-3daykcsw5a-ew.a.run.app/actuator/health
- notification-service: https://notification-service-3daykcsw5a-ew.a.run.app/actuator/health
- edge-gateway: https://edge-gateway-3daykcsw5a-ew.a.run.app/actuator/health
- facility-service: https://facility-service-3daykcsw5a-ew.a.run.app/actuator/health
- feature-flag-service: https://feature-flag-service-3daykcsw5a-ew.a.run.app/actuator/health
- device-verification-service: https://device-verification-service-3daykcsw5a-ew.a.run.app/actuator/health
- appliance-monitoring-service: https://appliance-monitoring-service-3daykcsw5a-ew.a.run.app/actuator/health
- service-discovery: https://service-discovery-3daykcsw5a-ew.a.run.app/actuator/health

#### Alert Policies

Create alert policies via the GCP Console:

1. Go to: https://console.cloud.google.com/monitoring/alerting
2. Click "Create Policy"
3. Configure conditions based on:

**High Error Rate Alert:**
- Metric: `logging.googleapis.com/user/staging_error_count`
- Condition: Rate > 5 errors/minute
- Duration: 5 minutes
- Notification: Email/Slack

**Service Down Alert:**
- Metric: Uptime check failure
- Condition: Uptime check fails
- Duration: 2 minutes
- Notification: Email/Slack (Critical)

**High Response Time Alert:**
- Metric: `run.googleapis.com/request_latencies`
- Condition: 95th percentile > 2 seconds
- Duration: 5 minutes
- Notification: Email (Warning)

**High Memory Usage Alert:**
- Metric: `run.googleapis.com/container/memory/utilizations`
- Condition: Memory > 80%
- Duration: 5 minutes
- Notification: Email (Warning)

**High CPU Usage Alert:**
- Metric: `run.googleapis.com/container/cpu/utilizations`
- Condition: CPU > 80%
- Duration: 5 minutes
- Notification: Email (Warning)

### 3. Error Reporting

Error Reporting is automatically enabled for Cloud Run. Errors are automatically collected and grouped.

**View Errors:**
- Console: https://console.cloud.google.com/errors?project=smartwatts-staging

**Configure Error Alerts:**
1. Go to Error Reporting
2. Click on an error group
3. Click "Set up alerting"
4. Configure notification channel

### 4. Dashboards

Create custom dashboards via the GCP Console:

1. Go to: https://console.cloud.google.com/monitoring/dashboards
2. Click "Create Dashboard"
3. Add widgets for:
   - Service health (uptime checks)
   - Request rate
   - Error rate
   - Response time (p50, p95, p99)
   - Memory usage
   - CPU usage
   - Active instances

**Recommended Dashboard Widgets:**

1. **Service Health Overview**
   - Uptime check status for all services
   - Service availability percentage

2. **Request Metrics**
   - Request rate (requests/second)
   - Request count by service
   - Request count by HTTP status code

3. **Performance Metrics**
   - Response time (p50, p95, p99)
   - Response time by service
   - Slowest endpoints

4. **Error Metrics**
   - Error rate (errors/second)
   - Error count by service
   - Error count by type

5. **Resource Usage**
   - Memory usage by service
   - CPU usage by service
   - Active instances by service

### 5. Notification Channels

Create notification channels for alerts:

**Email Channel:**
1. Go to: https://console.cloud.google.com/monitoring/alerting/notifications
2. Click "Add New" → "Email"
3. Enter email address
4. Save

**Slack Channel (Optional):**
1. Create Slack webhook URL
2. Go to notification channels
3. Click "Add New" → "Webhook"
4. Enter webhook URL
5. Save

## Quick Setup Script

Run the comprehensive monitoring setup script:

```bash
cd gcp-migration/monitoring
./setup-comprehensive-monitoring.sh staging
```

Or manually set up via console using the URLs and configurations above.

## Monitoring Best Practices

1. **Set up alerts for critical issues:**
   - Service down (Critical)
   - High error rate (Critical)
   - High response time (Warning)
   - Resource exhaustion (Warning)

2. **Review dashboards regularly:**
   - Daily: Service health and error rates
   - Weekly: Performance trends
   - Monthly: Capacity planning

3. **Configure appropriate notification channels:**
   - Critical alerts: Immediate notification (Email + Slack)
   - Warning alerts: Daily digest (Email)

4. **Set up log retention:**
   - Default: 30 days
   - Important logs: Export to BigQuery for long-term storage

5. **Monitor costs:**
   - Cloud Monitoring has free tier (150MB logs/day, 5GB metrics/month)
   - Monitor usage to avoid unexpected charges

## Access Links

- **Monitoring Dashboard**: https://console.cloud.google.com/monitoring?project=smartwatts-staging
- **Logs Explorer**: https://console.cloud.google.com/logs?project=smartwatts-staging
- **Error Reporting**: https://console.cloud.google.com/errors?project=smartwatts-staging
- **Uptime Checks**: https://console.cloud.google.com/monitoring/uptime?project=smartwatts-staging
- **Alert Policies**: https://console.cloud.google.com/monitoring/alerting?project=smartwatts-staging

## Next Steps

1. ✅ Set up log-based metrics
2. ⏳ Create uptime checks via console
3. ⏳ Create alert policies via console
4. ⏳ Create custom dashboards
5. ⏳ Configure notification channels
6. ⏳ Test alert notifications

