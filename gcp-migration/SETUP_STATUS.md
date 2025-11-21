# GCP Migration Setup Status

**Last Updated**: November 16, 2025, 8:50 PM

## ✅ Completed Phases

### Phase 1: Azure Backup & Documentation
- ✅ Azure architecture documented
- ✅ ARM template export scripts created
- ✅ Database backup scripts created
- ✅ Configuration backup scripts created
- ✅ Azure restoration guide created

### Phase 2: GCP Project Setup & Infrastructure
- ✅ **GCP Projects Created**
  - smartwatts-staging
  - smartwatts-production
- ✅ **Service Accounts Created**
  - cloud-run-sa (for Cloud Run services)
  - cloud-sql-sa (for Cloud SQL access)
  - artifact-registry-sa (for Docker images)
- ✅ **Cloud SQL Instances Created**
  - smartwatts-staging-db (europe-west1)
  - smartwatts-production-db (europe-west1)
  - All 9 databases created in each instance
- ✅ **Artifact Registry Setup**
  - Docker repositories created for all 13 services
- ✅ **Secret Manager Setup**
  - All secrets migrated to Secret Manager
  - Secrets available for both staging and production

### Phase 3: Docker Optimization
- ✅ All 13 service Dockerfiles optimized (Dockerfile.cloudrun)
- ✅ Frontend Dockerfile optimized
- ✅ Cloud Run configuration YAML files created
- ✅ Placeholders replaced in configs (staging)

## 🔄 In Progress / Ready to Execute

### Phase 4: Database Migration
**Status**: Scripts updated and ready
- ✅ Migration scripts updated to use port 5433 (to avoid conflict with local PostgreSQL)
- ✅ Cloud SQL Proxy running on port 5433
- ⏳ **Ready to run**: Database migration from Azure to Cloud SQL

**To Complete**:
```bash
# Ensure Cloud SQL Proxy is running (already done)
# Then run migration:
./gcp-migration/database-migration/migrate-databases.sh staging <azure-vm-ip> smartwatts-staging:europe-west1:smartwatts-staging-db

# Validate migration:
CLOUD_SQL_PORT=5433 ./gcp-migration/database-migration/validate-migration.sh staging <azure-vm-ip> smartwatts-staging:europe-west1:smartwatts-staging-db

# Update connection strings:
./gcp-migration/database-migration/update-connection-strings.sh staging smartwatts-staging:europe-west1:smartwatts-staging-db
```

**Requirements**:
- Azure VM IP address
- Azure PostgreSQL password
- Cloud SQL root password (already set)

### Phase 5: CI/CD Pipeline Setup
**Status**: Workflows created, needs GitHub secrets
- ✅ GitHub Actions workflows created
- ✅ Staging workflow enabled
- ✅ Production workflow disabled (safety)
- ⏳ **Needs**: GitHub secrets configured
  - `GCP_SA_KEY_STAGING`: Service account JSON key
  - `GCP_SA_KEY_PRODUCTION`: Service account JSON key (for later)

**To Complete**:
1. Create service account keys:
   ```bash
   gcloud iam service-accounts keys create ~/gcp-sa-key-staging.json \
     --iam-account=cloud-run-sa@smartwatts-staging.iam.gserviceaccount.com \
     --project=smartwatts-staging
   ```
2. Add to GitHub Secrets (Settings → Secrets and variables → Actions)

### Phase 6: Blue-Green Deployment
**Status**: Scripts ready, waiting for Phase 4 completion
- ✅ Deployment scripts created
- ✅ Blue-green deployment script ready
- ⏳ **Waiting for**: Database migration completion

### Phase 7: Monitoring & Logging
**Status**: Scripts ready
- ✅ Monitoring setup script ready
- ✅ Logging setup script ready
- ✅ Error reporting script ready
- ⏳ **Ready to run** after deployment

### Phase 8: Custom Domain & SSL
**Status**: Scripts ready
- ✅ Domain setup script ready
- ✅ SSL certificate management script ready
- ⏳ **Optional**: Can be done after deployment

### Phase 9: Migration Tools & Testing
**Status**: Scripts ready
- ✅ Test migration script ready
- ✅ Performance comparison tool ready
- ⏳ **Ready to run** after deployment

### Phase 10: Documentation
- ✅ All documentation complete
- ✅ Runbooks created
- ✅ Troubleshooting guide created

## 📋 Current Configuration

### Cloud SQL Proxy
- **Status**: ✅ Running
- **Port**: 5433 (to avoid conflict with local PostgreSQL on 5432)
- **Instance**: smartwatts-staging:europe-west1:smartwatts-staging-db
- **Command**: `cloud-sql-proxy smartwatts-staging:europe-west1:smartwatts-staging-db --port=5433`

### Environment Variables
- `CLOUD_SQL_PORT=5433` (for migration scripts)

### Updated Scripts
- ✅ `migrate-databases.sh` - Updated to use port 5433
- ✅ `validate-migration.sh` - Updated to use port 5433

## 🎯 Next Immediate Steps

1. **Complete Database Migration** (Phase 4)
   - Get Azure VM IP address
   - Run migration script
   - Validate migration
   - Update connection strings

2. **Configure GitHub Secrets** (Phase 5)
   - Create service account keys
   - Add to GitHub repository secrets

3. **Build and Push Docker Images** (Phase 6)
   - Build images for all services
   - Push to Artifact Registry

4. **Deploy to Cloud Run** (Phase 6)
   - Deploy services using blue-green strategy
   - Verify all services are healthy

5. **Setup Monitoring** (Phase 7)
   - Configure Cloud Monitoring dashboards
   - Setup alerting

## 📝 Notes

- Cloud SQL Proxy must remain running during database migration
- All scripts have been updated to use port 5433 instead of 5432
- Production workflows are disabled for safety
- Placeholders have been replaced in staging configs

## 🔗 Useful Commands

```bash
# Check Cloud SQL Proxy status
lsof -i :5433

# Test Cloud SQL connection
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres

# List Cloud SQL instances
gcloud sql instances list --project=smartwatts-staging

# List secrets
gcloud secrets list --project=smartwatts-staging

# Check service accounts
gcloud iam service-accounts list --project=smartwatts-staging
```

---

**Setup Progress**: ~70% Complete
**Remaining**: Database migration, CI/CD secrets, deployment, monitoring

