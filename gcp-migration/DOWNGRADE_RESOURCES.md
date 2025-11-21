# How to Downgrade Cloud Run Resources

After services are stable, you can reduce resource allocation to save costs.

## Current Settings (High Resources for Stability)
- Memory: 2GB per service
- CPU: 2 cores per service  
- Timeout: 600 seconds
- Startup delay: 180 seconds

## Recommended Production Settings (Cost Optimized)
- Memory: 512MB - 1GB per service
- CPU: 1 core per service
- Timeout: 300 seconds
- Startup delay: 60-120 seconds

## Steps to Downgrade

1. Edit the YAML files in `gcp-migration/cloud-run-configs/`:
   ```yaml
   resources:
     limits:
       cpu: "1"        # Change from "2"
       memory: 1Gi      # Change from 2Gi
     requests:
       cpu: "1"        # Change from "2"  
       memory: "512Mi"  # Change from 1Gi or 2Gi
   ```

2. Update timeout and startup delays:
   ```yaml
   timeoutSeconds: 300  # Change from 600
   startupProbe:
     initialDelaySeconds: 60  # Change from 180
   ```

3. Redeploy:
   ```bash
   gcloud run services replace gcp-migration/cloud-run-configs/<service-name>.yaml \
     --region=europe-west1 \
     --project=smartwatts-staging
   ```

## Cost Impact

- Current (2GB RAM, 2 CPU): ~$0.00002400 per request (100ms)
- Optimized (512MB RAM, 1 CPU): ~$0.00000600 per request (100ms)
- **Savings: ~75% reduction in compute costs**

## Monitoring

After downgrading, monitor:
- Response times
- Error rates
- Memory usage
- CPU utilization

If services show performance issues, gradually increase resources.

