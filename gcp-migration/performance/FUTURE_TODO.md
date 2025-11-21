# Future To-Do List - Paid Optimizations

## When Budget Allows

### High Priority
1. **Fix billing-service configuration** (Free)
   - Add missing JwtDecoder bean configuration
   - Then downgrade to 512Mi memory

2. **Increase minScale for critical services** (~$50-100/month)
   - API Gateway: minScale 1 → 2
   - User Service: minScale 1 → 2
   - Reduces cold starts significantly

### Medium Priority
3. **Upgrade Cloud SQL instance** (~$25-50/month)
   - Only if database becomes bottleneck
   - Monitor performance first

4. **Increase Cloud Run resources** (~$50-100/month)
   - Only if services show memory/CPU pressure
   - Monitor usage first

### Low Priority
5. **Multi-region deployment** (~$200-400/month)
   - When user base grows globally
   - When latency becomes an issue

6. **Advanced monitoring** (~$50-200/month)
   - When detailed insights needed
   - When debugging complex issues

## Free Optimizations (Can Do Now)

✅ Connection pooling - Script ready
✅ Redis caching - Script ready  
✅ Cold start monitoring - Script ready

Run these when ready:
```bash
./gcp-migration/performance/optimize-connection-pooling.sh
./gcp-migration/performance/implement-caching.sh
./gcp-migration/performance/monitor-cold-starts.sh
```
