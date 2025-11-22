# SmartWatts Monitoring Setup Status

**Date**: November 20, 2025  
**Environment**: Staging  
**Project**: smartwatts-staging

## ✅ Completed

### 1. Log-Based Metrics
The following log-based metrics have been created:

- ✅ **staging_error_count**: Count of error logs (severity >= ERROR)
- ✅ **staging_http_5xx_count**: Count of HTTP 5xx errors
- ✅ **staging_http_4xx_count**: Count of HTTP 4xx errors (created via script)

### 2. Cloud Logging
- ✅ Automatically enabled for all Cloud Run services
- ✅ All service logs are being collected
- ✅ Log retention: 30 days (default)

### 3. Error Reporting
- ✅ Automatically enabled for Cloud Run services
- ✅ Errors are automatically grouped and tracked

## ⏳ To Be Completed (Via GCP Console)

### 1. Uptime Checks
Create uptime checks for all services via the GCP Console:

**Access**: https://console.cloud.google.com/monitoring/uptime?project=smartwatts-staging

**Services to Monitor:**
1. api-gateway: https://api-gateway-3daykcsw5a-ew.a.run.app/actuator/health
2. user-service: https://user-service-3daykcsw5a-ew.a.run.app/actuator/health
3. energy-service: https://energy-service-3daykcsw5a-ew.a.run.app/actuator/health
4. device-service: https://device-service-3daykcsw5a-ew.a.run.app/actuator/health
5. analytics-service: https://analytics-service-3daykcsw5a-ew.a.run.app/actuator/health
6. billing-service: https://billing-service-3daykcsw5a-ew.a.run.app/actuator/health
7. notification-service: https://notification-service-3daykcsw5a-ew.a.run.app/actuator/health
8. edge-gateway: https://edge-gateway-3daykcsw5a-ew.a.run.app/actuator/health
9. facility-service: https://facility-service-3daykcsw5a-ew.a.run.app/actuator/health
10. feature-flag-service: https://feature-flag-service-3daykcsw5a-ew.a.run.app/actuator/health
11. device-verification-service: https://device-verification-service-3daykcsw5a-ew.a.run.app/actuator/health
12. appliance-monitoring-service: https://appliance-monitoring-service-3daykcsw5a-ew.a.run.app/actuator/health
13. service-discovery: https://service-discovery-3daykcsw5a-ew.a.run.app/actuator/health

**Configuration:**
- Check interval: 60 seconds
- Timeout: 10 seconds
- Resource type: URL

### 2. Notification Channels
Create notification channels for alerts:

**Access**: https://console.cloud.google.com/monitoring/alerting/notifications?project=smartwatts-staging

**Channels to Create:**
1. **Critical Alerts** (Email)
   - Type: Email
   - Email: admin@mysmartwatts.com (or your email)
   - For: Service down, high error rate

2. **Warning Alerts** (Email)
   - Type: Email
   - Email: ops@mysmartwatts.com (or your email)
   - For: High response time, resource usage warnings

3. **Slack Alerts** (Optional)
   - Type: Webhook
   - Webhook URL: Your Slack webhook URL
   - For: Critical alerts only

### 3. Alert Policies
Create alert policies via the GCP Console:

**Access**: https://console.cloud.google.com/monitoring/alerting?project=smartwatts-staging

**Policies to Create:**

#### A. Service Down Alert (Critical)
- **Condition**: Uptime check fails
- **Duration**: 2 minutes
- **Notification**: Critical Alerts channel
- **Services**: All 13 services

#### B. High Error Rate Alert (Critical)
- **Metric**: `logging.googleapis.com/user/staging_error_count`
- **Condition**: Rate > 5 errors/minute
- **Duration**: 5 minutes
- **Notification**: Critical Alerts channel

#### C. High HTTP 5xx Rate Alert (Critical)
- **Metric**: `logging.googleapis.com/user/staging_http_5xx_count`
- **Condition**: Rate > 10 errors/minute
- **Duration**: 5 minutes
- **Notification**: Critical Alerts channel

#### D. High Response Time Alert (Warning)
- **Metric**: `run.googleapis.com/request_latencies`
- **Condition**: 95th percentile > 2 seconds
- **Duration**: 5 minutes
- **Notification**: Warning Alerts channel

#### E. High Memory Usage Alert (Warning)
- **Metric**: `run.googleapis.com/container/memory/utilizations`
- **Condition**: Memory > 80%
- **Duration**: 5 minutes
- **Notification**: Warning Alerts channel

#### F. High CPU Usage Alert (Warning)
- **Metric**: `run.googleapis.com/container/cpu/utilizations`
- **Condition**: CPU > 80%
- **Duration**: 5 minutes
- **Notification**: Warning Alerts channel

### 4. Custom Dashboards
Create custom dashboards for monitoring:

**Access**: https://console.cloud.google.com/monitoring/dashboards?project=smartwatts-staging

**Recommended Dashboards:**

#### Dashboard 1: Service Health Overview
- Uptime check status for all services
- Service availability percentage
- Service health timeline

#### Dashboard 2: Request Metrics
- Request rate (requests/second) by service
- Request count by HTTP status code
- Top endpoints by request count

#### Dashboard 3: Performance Metrics
- Response time (p50, p95, p99) by service
- Response time distribution
- Slowest endpoints

#### Dashboard 4: Error Metrics
- Error rate (errors/second) by service
- Error count by type
- Error trends over time

#### Dashboard 5: Resource Usage
- Memory usage by service
- CPU usage by service
- Active instances by service
- Container startup time

## Quick Links

- **Monitoring Dashboard**: https://console.cloud.google.com/monitoring?project=smartwatts-staging
- **Logs Explorer**: https://console.cloud.google.com/logs?project=smartwatts-staging
- **Error Reporting**: https://console.cloud.google.com/errors?project=smartwatts-staging
- **Uptime Checks**: https://console.cloud.google.com/monitoring/uptime?project=smartwatts-staging
- **Alert Policies**: https://console.cloud.google.com/monitoring/alerting?project=smartwatts-staging
- **Dashboards**: https://console.cloud.google.com/monitoring/dashboards?project=smartwatts-staging

## Next Steps

1. ✅ Log-based metrics created
2. ⏳ Create uptime checks (via console - ~15 minutes)
3. ⏳ Create notification channels (via console - ~5 minutes)
4. ⏳ Create alert policies (via console - ~30 minutes)
5. ⏳ Create custom dashboards (via console - ~45 minutes)

**Estimated Total Time**: ~1.5 hours

## Documentation

See `MONITORING_SETUP_GUIDE.md` for detailed instructions on setting up each component.

