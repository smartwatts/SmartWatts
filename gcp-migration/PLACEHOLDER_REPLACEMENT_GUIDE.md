# Placeholder Replacement Guide

## Overview

All configuration files contain placeholders that must be replaced with actual values before deployment. This guide explains how to use the automated replacement script.

## Placeholders to Replace

| Placeholder | Description | Example Value |
|------------|-------------|---------------|
| `PROJECT_ID` | GCP Project ID | `smartwatts-staging` or `smartwatts-production` |
| `REGION` | GCP Region | `europe-west1` |
| `REPOSITORY` | Artifact Registry repository name | Service name (e.g., `api-gateway`) |
| `INSTANCE_NAME` | Cloud SQL instance name | `smartwatts-staging-db` or `smartwatts-production-db` |
| `DATABASE_NAME` | PostgreSQL database name | `smartwatts_users`, `smartwatts_energy`, etc. |

## Automated Replacement

### For Staging Environment

```bash
cd gcp-migration
./replace-placeholders.sh staging
```

This will replace all placeholders in:
- All 13 Cloud Run configuration files
- Cloud Build configuration files
- Scripts with hardcoded examples

### For Production Environment

```bash
./replace-placeholders.sh production
```

**Important**: Run this separately for production when ready to deploy.

## Verification

After running the replacement script, verify all placeholders are replaced:

```bash
./verify-placeholders.sh
```

This will check all configuration files and report any remaining placeholders.

## Manual Replacement (If Needed)

If you need to manually replace placeholders, use these values:

### Staging Environment
- `PROJECT_ID` → `smartwatts-staging`
- `REGION` → `europe-west1`
- `INSTANCE_NAME` → `smartwatts-staging-db`
- `REPOSITORY` → Service name (e.g., `api-gateway`, `user-service`)
- `DATABASE_NAME` → See database mapping below

### Production Environment
- `PROJECT_ID` → `smartwatts-production`
- `REGION` → `europe-west1`
- `INSTANCE_NAME` → `smartwatts-production-db`
- `REPOSITORY` → Service name
- `DATABASE_NAME` → See database mapping below

## Database Name Mapping

| Service | Database Name |
|---------|---------------|
| user-service | smartwatts_users |
| energy-service | smartwatts_energy |
| device-service | smartwatts_devices |
| analytics-service | smartwatts_analytics |
| billing-service | smartwatts_billing |
| facility-service | smartwatts_facility360 |
| feature-flag-service | smartwatts_feature_flags |
| device-verification-service | smartwatts_device_verification |
| appliance-monitoring-service | smartwatts_appliance_monitoring |

## Files That Need Replacement

### Cloud Run Configurations (13 files)
- `gcp-migration/cloud-run-configs/api-gateway.yaml`
- `gcp-migration/cloud-run-configs/user-service.yaml`
- `gcp-migration/cloud-run-configs/energy-service.yaml`
- `gcp-migration/cloud-run-configs/device-service.yaml`
- `gcp-migration/cloud-run-configs/analytics-service.yaml`
- `gcp-migration/cloud-run-configs/billing-service.yaml`
- `gcp-migration/cloud-run-configs/service-discovery.yaml`
- `gcp-migration/cloud-run-configs/edge-gateway.yaml`
- `gcp-migration/cloud-run-configs/facility-service.yaml`
- `gcp-migration/cloud-run-configs/feature-flag-service.yaml`
- `gcp-migration/cloud-run-configs/device-verification-service.yaml`
- `gcp-migration/cloud-run-configs/appliance-monitoring-service.yaml`
- `gcp-migration/cloud-run-configs/notification-service.yaml`

### Cloud Build Configurations
- `gcp-migration/ci-cd/cloudbuild-staging.yaml` (uses `${PROJECT_ID}` variable - correct)
- `gcp-migration/ci-cd/cloudbuild-production.yaml` (uses `${PROJECT_ID}` variable - correct)

**Note**: Cloud Build files use `${PROJECT_ID}` which is automatically set by Cloud Build, so no replacement needed.

## Backup Files

The replacement script creates `.bak` backup files. After verifying replacements are correct, you can remove them:

```bash
find gcp-migration -name "*.bak" -delete
```

## Troubleshooting

### Issue: Some placeholders not replaced

**Solution**: 
1. Check if the file exists
2. Verify the placeholder spelling (case-sensitive)
3. Run verification script: `./verify-placeholders.sh`
4. Manually check and replace if needed

### Issue: Wrong environment values

**Solution**: 
1. Restore from `.bak` backup files
2. Run replacement script with correct environment: `./replace-placeholders.sh <environment>`

### Issue: Database name not replaced

**Solution**: 
1. Check if service is in the database mapping
2. Services without databases (api-gateway, service-discovery, edge-gateway) don't need DATABASE_NAME

---

**Last Updated**: November 2025

