# Free Tier Status

## Current Configuration

### ✅ Cloud Run Services
- **12 services**: 512Mi memory, 1 CPU, minScale=1
- **1 service**: 1Gi memory, 1 CPU, minScale=1 (billing-service)
- **All services**: Can scale to zero when not in use

### ✅ Cloud SQL
- **Instance**: db-f1-micro
- **Storage**: 20GB
- **Region**: europe-west1

## Important: GCP Free Tier Reality

### ⚠️ **Not Everything is Free**

GCP's "Always Free" tier has **very limited** free allowances:

#### Cloud Run Free Tier Limits:
- **2 million requests per month** (free)
- **360,000 GiB-seconds of memory** (free)
- **180,000 vCPU-seconds** (free)
- **1 GB egress per month** (free)

**After free tier limits, you pay for:**
- Requests: $0.40 per million requests
- Memory: $0.0000025 per GiB-second
- CPU: $0.00002400 per vCPU-second
- Egress: $0.12 per GB

#### Cloud SQL Free Tier:
- **db-f1-micro is NOT free** - it's the cheapest paid tier
- **Cost**: ~$7-10/month for db-f1-micro
- **No free Cloud SQL tier** (except for SQL Server which has a free trial)

#### Artifact Registry:
- **5 GB storage free** per month
- **After that**: $0.10 per GB per month

## Current Cost Estimate

### What You're Actually Paying:

1. **Cloud Run**: 
   - Free for first 2M requests/month
   - Free for first 360K GiB-seconds/month
   - **If you exceed**: ~$0-50/month depending on usage

2. **Cloud SQL (db-f1-micro)**:
   - **~$7-10/month** (NOT free)
   - This is the minimum cost for Cloud SQL

3. **Artifact Registry**:
   - Free for first 5GB
   - **Likely $0-5/month** depending on image sizes

4. **Network Egress**:
   - Free for first 1GB/month
   - **After that**: ~$0-10/month

### Estimated Monthly Cost: **~$10-70/month**

**Minimum**: ~$10/month (Cloud SQL + minimal Cloud Run usage)  
**Typical**: ~$30-50/month (with normal usage)  
**Maximum**: ~$70/month (with higher usage)

## How to Minimize Costs Further

### Option 1: Use Cloud SQL Free Alternative
- **Cloud SQL is NOT free** - db-f1-micro costs ~$7-10/month
- Consider using a free PostgreSQL alternative:
  - **Supabase** (free tier available)
  - **Neon** (free tier available)
  - **Railway** (free tier available)
  - **Self-hosted** (if you have infrastructure)

### Option 2: Reduce Cloud Run Usage
- Keep minScale=1 (already done ✅)
- Use 512Mi memory (already done ✅)
- Monitor usage to stay within free tier limits

### Option 3: Use GCP Free Credits
- New GCP accounts get $300 free credits
- Valid for 90 days
- Can cover initial costs

## Summary

### ✅ **Optimized for Free Tier**:
- Cloud Run services are configured for minimal cost
- Resources are at minimum levels
- Services can scale to zero

### ⚠️ **Not Completely Free**:
- **Cloud SQL costs ~$7-10/month** (minimum)
- Cloud Run is free up to limits, then you pay
- Artifact Registry has free tier limits

### 💰 **Realistic Monthly Cost**: **~$10-70/month**

**Minimum possible**: ~$10/month (just Cloud SQL)  
**With normal usage**: ~$30-50/month  
**With high usage**: ~$70/month

## Recommendations

1. **Monitor usage** in GCP Console to see actual costs
2. **Set up billing alerts** to avoid surprises
3. **Consider free database alternatives** if you want to eliminate Cloud SQL costs
4. **Use GCP free credits** if available ($300 for new accounts)

## Free Tier Monitoring

Check your actual usage:
```bash
# View billing account
gcloud billing accounts list

# Check current month costs (if billing export is set up)
# Or check in GCP Console: https://console.cloud.google.com/billing
```

## Conclusion

**Your services are optimized for minimal cost**, but **not completely free**. The main cost is Cloud SQL (~$7-10/month minimum). Everything else can be free if you stay within free tier limits.

