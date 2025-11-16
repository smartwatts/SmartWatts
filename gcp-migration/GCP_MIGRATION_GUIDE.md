# SmartWatts GCP Migration Guide

**Version**: 1.0  
**Date**: November 2025  
**Purpose**: Complete step-by-step guide for migrating SmartWatts from Azure to GCP Cloud Run

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Migration Timeline](#migration-timeline)
4. [Phase-by-Phase Instructions](#phase-by-phase-instructions)
5. [Rollback Procedures](#rollback-procedures)
6. [Troubleshooting](#troubleshooting)
7. [Post-Migration Checklist](#post-migration-checklist)

---

## Overview

This guide provides complete instructions for migrating SmartWatts from Azure (VM-based) to GCP Cloud Run (serverless containers) with zero-downtime blue-green deployment.

### Migration Strategy

- **Approach**: Blue-green deployment with gradual traffic migration
- **Database**: Cloud SQL PostgreSQL (managed service)
- **Regions**: Multi-region (europe-west1 primary, europe-west4 replica)
- **Downtime**: Zero (blue-green deployment)

---

## Prerequisites

### Required Tools

- [ ] Azure CLI installed and configured
- [ ] Google Cloud SDK (gcloud) installed and configured
- [ ] Docker installed
- [ ] PostgreSQL client tools (psql, pg_dump, pg_restore)
- [ ] jq for JSON processing
- [ ] Git configured

### Required Access

- [ ] Azure subscription with Contributor role
- [ ] GCP billing account access
- [ ] GCP project creation permissions
- [ ] SSH access to Azure VM
- [ ] GitHub repository access

### Required Information

- [ ] Azure subscription ID
- [ ] GCP billing account ID
- [ ] Azure VM IP address
- [ ] PostgreSQL passwords (Azure and Cloud SQL)
- [ ] Connection strings and secrets

---

## Migration Timeline

**Estimated Total Time**: 6-8 hours

| Phase | Duration | Description |
|-------|----------|-------------|
| Phase 1: Azure Backup | 1-2 hours | Document and backup Azure infrastructure |
| Phase 2: GCP Setup | 1-2 hours | Create projects and infrastructure |
| Phase 3: Docker Optimization | 30 min | Optimize Dockerfiles for Cloud Run |
| Phase 4: Database Migration | 1-2 hours | Migrate databases to Cloud SQL |
| Phase 5: CI/CD Setup | 30 min | Configure deployment pipelines |
| Phase 6: Blue-Green Deployment | 1 hour | Deploy with zero downtime |
| Phase 7: Monitoring | 30 min | Setup monitoring and logging |
| Phase 8: Domain & SSL | 30 min | Configure custom domain |
| Phase 9: Validation | 30 min | Test and validate migration |
| Phase 10: Documentation | 30 min | Complete documentation |

---

## Phase-by-Phase Instructions

### Phase 1: Azure Backup & Documentation

**Objective**: Create complete backup of Azure infrastructure before migration

#### Step 1.1: Export ARM Templates

```bash
cd gcp-migration/azure-backup
./export-arm-templates.sh staging
```

**Verification**: Check `gcp-migration/azure-backup/exports/staging/` for exported templates

#### Step 1.2: Backup Databases

```bash
./backup-databases.sh staging <azure-vm-ip>
```

**Verification**: Check `gcp-migration/azure-backup/database-backups/staging/` for database backups

#### Step 1.3: Backup Configurations

```bash
./backup-configurations.sh staging
```

**Verification**: Check `gcp-migration/azure-backup/configuration-backups/staging/` for configurations

#### Step 1.4: Review Documentation

- Review `azure-architecture-documentation.md`
- Review `azure-restoration-guide.md`

**Phase 1 Complete**: All backups created and verified

---

### Phase 2: GCP Project Setup & Infrastructure

**Objective**: Create GCP projects and configure all infrastructure

#### Step 2.1: Create GCP Projects

```bash
cd gcp-migration/gcp-setup
./create-gcp-projects.sh <billing-account-id>
```

**Verification**: 
- Projects created: `smartwatts-staging` and `smartwatts-production`
- All APIs enabled

#### Step 2.2: Setup Service Accounts

```bash
./setup-service-accounts.sh
```

**Verification**: Service accounts created with proper IAM roles

#### Step 2.3: Setup Cloud SQL

```bash
./setup-cloud-sql.sh
```

**Verification**: 
- Cloud SQL instances created
- All 9 databases created
- Connection names noted

#### Step 2.4: Setup Artifact Registry

```bash
./setup-artifact-registry.sh
```

**Verification**: Docker repositories created for all services

#### Step 2.5: Setup Secrets

```bash
./setup-secrets.sh
```

**Verification**: All secrets migrated to Secret Manager

**Phase 2 Complete**: All GCP infrastructure ready

---

### Phase 3: Docker Optimization

**Objective**: Optimize Dockerfiles for Cloud Run

#### Step 3.1: Generate Cloud Run Dockerfiles

```bash
cd gcp-migration
./generate-cloudrun-dockerfiles.sh
```

**Verification**: All `Dockerfile.cloudrun` files created in service directories

#### Step 3.2: Generate Cloud Run Configs

```bash
./generate-cloudrun-configs.sh
```

**Verification**: All Cloud Run YAML configs created in `cloud-run-configs/`

#### Step 3.3: Update Configurations

- Update Cloud Run configs with actual PROJECT_ID, REGION, INSTANCE_NAME
- Update connection strings
- Verify secret references

**Phase 3 Complete**: All Dockerfiles and configs optimized

---

### Phase 4: Database Migration

**Objective**: Migrate all databases from Azure to Cloud SQL

#### Step 4.1: Start Cloud SQL Proxy

```bash
cloud-sql-proxy PROJECT_ID:REGION:INSTANCE_NAME --port=5432
```

#### Step 4.2: Migrate Databases

```bash
cd gcp-migration/database-migration
./migrate-databases.sh staging <azure-vm-ip> <cloud-sql-instance>
```

**Verification**: All 9 databases migrated successfully

#### Step 4.3: Validate Migration

```bash
./validate-migration.sh staging <azure-vm-ip> <cloud-sql-instance>
```

**Verification**: All databases validated

#### Step 4.4: Update Connection Strings

```bash
./update-connection-strings.sh staging <cloud-sql-instance>
```

**Phase 4 Complete**: All databases migrated and validated

---

### Phase 5: CI/CD Pipeline Setup

**Objective**: Configure automated deployment pipelines

#### Step 5.1: Configure GitHub Secrets

Add the following secrets to GitHub:
- `GCP_SA_KEY_STAGING`: Staging service account key JSON
- `GCP_SA_KEY_PRODUCTION`: Production service account key JSON

#### Step 5.2: Verify Workflows

- Staging workflow: `.github/workflows/gcp-staging-deploy.yml` (enabled)
- Production workflow: `.github/workflows/gcp-production-deploy.yml` (disabled, workflow_dispatch only)

#### Step 5.3: Test Staging Deployment

```bash
git push origin staging
```

**Verification**: Staging deployment succeeds via GitHub Actions

**Phase 5 Complete**: CI/CD pipelines configured and tested

---

### Phase 6: Blue-Green Deployment

**Objective**: Deploy with zero downtime using blue-green strategy

#### Step 6.1: Deploy Green Environment

```bash
cd gcp-migration/deployment
./blue-green-deploy.sh staging api-gateway
```

#### Step 6.2: Verify Green Environment

```bash
./verify-deployment.sh staging
```

#### Step 6.3: Migrate Traffic

Traffic migration happens automatically in blue-green script:
- 10% → 50% → 100%

**Phase 6 Complete**: Blue-green deployment successful

---

### Phase 7: Monitoring & Logging

**Objective**: Setup comprehensive monitoring and logging

#### Step 7.1: Setup Monitoring

```bash
cd gcp-migration/monitoring
./setup-monitoring.sh staging
```

#### Step 7.2: Setup Logging

```bash
./setup-logging.sh staging
```

#### Step 7.3: Setup Error Reporting

```bash
./setup-error-reporting.sh staging
```

**Phase 7 Complete**: Monitoring and logging configured

---

### Phase 8: Custom Domain & SSL

**Objective**: Configure custom domain and SSL certificates

#### Step 8.1: Setup Domain

```bash
cd gcp-migration/domain
./setup-custom-domain.sh <your-domain.com>
```

#### Step 8.2: Manage SSL Certificates

```bash
./manage-ssl-certificates.sh <your-domain.com>
```

**Phase 8 Complete**: Custom domain and SSL configured

---

### Phase 9: Migration Validation

**Objective**: Validate migration and test functionality

#### Step 9.1: Run Migration Tests

```bash
cd gcp-migration/tools
./test-migration.sh staging
```

#### Step 9.2: Compare Performance

```bash
./compare-performance.sh
```

**Phase 9 Complete**: Migration validated

---

### Phase 10: Documentation

**Objective**: Complete all documentation

- [ ] Review all runbooks
- [ ] Update troubleshooting guide
- [ ] Document any custom configurations
- [ ] Create team handoff documentation

**Phase 10 Complete**: All documentation complete

---

## Rollback Procedures

### Emergency Rollback

If migration fails, follow these steps:

1. **Stop GCP Services** (if needed)
   ```bash
   gcloud run services update SERVICE_NAME --no-traffic --region=REGION
   ```

2. **Restore Azure Infrastructure**
   - Follow `azure-restoration-guide.md`
   - Restore databases from backups
   - Restore configurations

3. **Update DNS** to point back to Azure

4. **Verify Azure Services** are running

See `runbooks/rollback-procedures.md` for detailed procedures.

---

## Troubleshooting

### Common Issues

#### Issue: Cloud Run service fails to start

**Symptoms**: Service shows error status

**Solutions**:
1. Check Cloud Run logs: `gcloud run services logs read SERVICE_NAME`
2. Verify environment variables
3. Check Cloud SQL connection
4. Verify service account permissions

#### Issue: Database connection fails

**Symptoms**: Services cannot connect to Cloud SQL

**Solutions**:
1. Verify Cloud SQL Proxy is running
2. Check IAM permissions for service account
3. Verify connection string format
4. Check Cloud SQL instance is running

#### Issue: Secrets not accessible

**Symptoms**: Services cannot access Secret Manager

**Solutions**:
1. Verify service account has `secretmanager.secretAccessor` role
2. Check secret names match
3. Verify secret versions

See `TROUBLESHOOTING.md` for more issues and solutions.

---

## Post-Migration Checklist

- [ ] All services deployed and healthy
- [ ] All databases migrated and validated
- [ ] Monitoring and alerting configured
- [ ] Custom domain and SSL working
- [ ] Performance meets or exceeds Azure
- [ ] Cost analysis completed
- [ ] Team trained on GCP operations
- [ ] Documentation complete
- [ ] Azure resources decommissioned (after validation period)

---

## Production Workflow Enablement

**IMPORTANT**: Production workflows are disabled by default for safety.

### To Enable Production Workflows:

1. **GitHub Actions**:
   - Edit `.github/workflows/gcp-production-deploy.yml`
   - Add push triggers if needed (currently workflow_dispatch only)

2. **Cloud Build**:
   - Go to Cloud Build → Triggers
   - Enable production trigger when ready

3. **Verification**:
   - Test production deployment in staging first
   - Verify all checks pass
   - Then enable production

See `GCP_DEPLOYMENT_GUIDE.md` for detailed enablement procedures.

---

## Support and Resources

- **GCP Documentation**: https://cloud.google.com/docs
- **Cloud Run Documentation**: https://cloud.google.com/run/docs
- **Cloud SQL Documentation**: https://cloud.google.com/sql/docs
- **Troubleshooting Guide**: `TROUBLESHOOTING.md`
- **Runbooks**: `runbooks/` directory

---

**Migration Guide Status**: Complete  
**Last Updated**: November 2025  
**Next Review**: Before migration execution
