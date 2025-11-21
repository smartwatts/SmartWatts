# Performance Optimization Guide

## Overview

This guide documents the performance optimization recommendations and implementations for SmartWatts services on GCP Cloud Run.

## Immediate Actions ✅

### 1. Increase Minimum Instances

**Status**: Script created - `optimize-min-instances.sh`

**Action**: Run the script to increase minScale for critical services:

```bash
./gcp-migration/performance/optimize-min-instances.sh
```

**Changes**:
- API Gateway: minScale 1 → 2
- User Service: minScale 1 → 2
- Other services: minScale 1 (maintained)

**Impact**:
- Reduces cold starts significantly
- Increases costs (2x for critical services)
- Improves response times for first requests

### 2. Monitor Cold Start Frequency

**Status**: Script created - `monitor-cold-starts.sh`

**Action**: Run periodically to monitor cold start rates:

```bash
# Monitor last 24 hours
./gcp-migration/performance/monitor-cold-starts.sh

# Monitor last 7 days
LOOKBACK_HOURS=168 ./gcp-migration/performance/monitor-cold-starts.sh
```

**Metrics Tracked**:
- Cold start count per service
- Cold start rate percentage
- Total requests
- Current minScale settings

**Targets**:
- Cold start rate < 1% (excellent)
- Cold start rate 1-5% (acceptable)
- Cold start rate > 5% (needs optimization)

## Medium-term Optimizations

### 1. Optimize Container Startup Time

**Current Status**: Container startup ~7-10 seconds

**Recommendations**:

#### a. Reduce JAR Size
- Use Spring Boot's layer extraction (already implemented)
- Remove unused dependencies
- Use `spring-boot-thin-launcher` for smaller JARs

#### b. Optimize Application Startup
- Lazy initialization for non-critical beans
- Defer database connection until first request
- Use Spring Boot's `spring.main.lazy-initialization=true` (with caution)

#### c. Optimize Dockerfile
```dockerfile
# Use distroless or alpine for smaller images
FROM gcr.io/distroless/java17-debian11

# Pre-extract layers
RUN java -Djarmode=layertools -jar app.jar extract
```

**Implementation**:
1. Review and optimize Dockerfiles
2. Enable lazy initialization for non-critical services
3. Monitor startup time improvements

### 2. Implement Connection Pooling

**Status**: Script created - `optimize-connection-pooling.sh`

**Action**: Run to update HikariCP settings:

```bash
./gcp-migration/performance/optimize-connection-pooling.sh
```

**Configuration**:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

**Benefits**:
- Reuses database connections
- Reduces connection overhead
- Improves response times

**Next Steps**:
1. Run the optimization script
2. Rebuild and redeploy services
3. Monitor connection pool metrics

### 3. Add Caching for Frequently Accessed Data

**Implementation Plan**:

#### a. Redis Caching (Already Available)
- Services already have Redis configured
- Implement Spring Cache with Redis

#### b. Cache Configuration
Add to `application-cloudrun.yml`:
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "smartwatts:"
```

#### c. Cache Annotations
Add to service methods:
```java
@Cacheable(value = "users", key = "#id")
public UserDto getUserById(UUID id) {
    // ...
}

@CacheEvict(value = "users", key = "#user.id")
public UserDto updateUser(UserDto user) {
    // ...
}
```

**Services to Cache**:
1. User Service: User profiles, user lists
2. Device Service: Device configurations
3. Feature Flag Service: Feature flags
4. Analytics Service: Aggregated analytics

**Implementation Steps**:
1. Add Spring Cache dependencies (if not present)
2. Configure Redis cache in `application-cloudrun.yml`
3. Add `@Cacheable` annotations to read methods
4. Add `@CacheEvict` annotations to write methods
5. Test cache hit rates

## Long-term Optimizations

### 1. Fine-tune Auto-scaling Parameters

**Current Settings**:
- minScale: 1-2
- maxScale: 10
- containerConcurrency: 80

**Optimization Strategy**:

#### a. Adjust containerConcurrency
- **Current**: 80 requests per container
- **Recommendation**: 
  - CPU-intensive services: 40-60
  - I/O-intensive services: 100-200
  - Mixed workloads: 80 (current)

#### b. Adjust maxScale
- **Current**: 10
- **Recommendation**: 
  - Critical services: 20-50
  - Moderate services: 10-20
  - Low-traffic services: 5-10

#### c. Implement Custom Metrics
- Scale based on custom metrics (queue depth, processing time)
- Use Cloud Monitoring custom metrics

**Implementation**:
1. Analyze traffic patterns
2. Adjust containerConcurrency per service
3. Increase maxScale for high-traffic services
4. Monitor and iterate

### 2. Multi-Region Deployment

**Current**: Single region (europe-west1)

**Benefits**:
- Reduced latency for global users
- Improved availability
- Better disaster recovery

**Implementation Plan**:
1. **Phase 1**: Deploy to 2 regions (europe-west1, us-central1)
2. **Phase 2**: Add Global Load Balancer
3. **Phase 3**: Implement region-aware routing
4. **Phase 4**: Add 3rd region (asia-southeast1)

**Considerations**:
- Database replication (Cloud SQL read replicas)
- Redis replication (Memorystore with replication)
- Cross-region latency
- Cost implications

### 3. Detailed Performance Monitoring

**Current**: Basic Cloud Run metrics

**Enhancements Needed**:

#### a. Application Performance Monitoring (APM)
- **Option 1**: Google Cloud Trace
- **Option 2**: New Relic / Datadog
- **Option 3**: OpenTelemetry

#### b. Custom Metrics
- Response time percentiles (p50, p95, p99)
- Error rates by endpoint
- Database query performance
- Cache hit rates
- Cold start frequency

#### c. Dashboards
- Real-time performance dashboard
- Service health dashboard
- Cost optimization dashboard

#### d. Alerting
- Response time > 1s (p95)
- Error rate > 1%
- Cold start rate > 5%
- Database connection pool exhaustion

**Implementation**:
1. Set up Cloud Trace
2. Add custom metrics to services
3. Create Grafana dashboards
4. Configure alerting policies
5. Set up SLOs/SLIs

## Performance Targets

| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Avg Response Time | ~700ms | <200ms | ⚠️ Needs improvement |
| P95 Response Time | ~1s | <500ms | ⚠️ Needs improvement |
| P99 Response Time | ~1.6s | <1s | ⚠️ Needs improvement |
| Cold Start Rate | ~2-5% | <1% | ⚠️ Needs improvement |
| Success Rate | 100% | >99.9% | ✅ Exceeds |
| Requests/sec | ~28 | >100 | ⚠️ Needs improvement |

## Cost Optimization

### Current Costs (Estimated)
- Cloud Run: ~$50-100/month (with minScale=1)
- Cloud SQL: ~$30-50/month
- Memorystore (Redis): ~$30-50/month
- **Total**: ~$110-200/month

### After Optimizations
- Cloud Run: ~$100-200/month (with minScale=2 for critical services)
- Cloud SQL: ~$30-50/month
- Memorystore: ~$30-50/month
- **Total**: ~$160-300/month

### Cost Optimization Tips
1. Use committed use discounts for predictable workloads
2. Right-size Cloud SQL instances
3. Use Cloud CDN for static content
4. Monitor and adjust minScale based on actual traffic
5. Use Cloud Scheduler to scale down during off-hours

## Monitoring and Maintenance

### Daily
- Check cold start rates
- Monitor error rates
- Review response times

### Weekly
- Analyze performance trends
- Review cost reports
- Optimize slow endpoints

### Monthly
- Performance review meeting
- Cost optimization review
- Capacity planning

## Scripts Reference

1. **optimize-min-instances.sh**: Increase minimum instances
2. **monitor-cold-starts.sh**: Monitor cold start frequency
3. **optimize-connection-pooling.sh**: Update HikariCP settings

## Next Steps

1. ✅ Run `optimize-min-instances.sh` (Immediate)
2. ✅ Run `monitor-cold-starts.sh` (Immediate)
3. ⏳ Run `optimize-connection-pooling.sh` (Medium-term)
4. ⏳ Implement caching (Medium-term)
5. ⏳ Fine-tune auto-scaling (Long-term)
6. ⏳ Plan multi-region deployment (Long-term)
7. ⏳ Set up detailed monitoring (Long-term)

## References

- [Cloud Run Performance Best Practices](https://cloud.google.com/run/docs/tips)
- [Spring Boot Performance](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.application-availability)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)

