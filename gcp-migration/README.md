# SmartWatts Azure to GCP Cloud Run Migration

Complete migration package for migrating SmartWatts from Azure to GCP Cloud Run.

## Overview

This migration package provides all necessary scripts, configurations, and documentation to migrate SmartWatts from Azure (VM-based) to GCP Cloud Run (serverless containers) with zero-downtime blue-green deployment.

## Quick Start

### 1. Replace Placeholders

**IMPORTANT**: Before deployment, replace all placeholders in configuration files:

```bash
# For staging environment
./gcp-migration/replace-placeholders.sh staging

# Verify replacements
./gcp-migration/verify-placeholders.sh

# For production environment (when ready)
./gcp-migration/replace-placeholders.sh production
```

See `PLACEHOLDER_REPLACEMENT_GUIDE.md` for detailed instructions.

### 2. Execute Migration Phases

Follow the phases sequentially (each must be 100% complete):

1. **Phase 1**: Azure Backup
   ```bash
   ./gcp-migration/azure-backup/export-arm-templates.sh staging
   ./gcp-migration/azure-backup/backup-databases.sh staging <vm-ip>
   ./gcp-migration/azure-backup/backup-configurations.sh staging
   ```

2. **Phase 2**: GCP Setup
   ```bash
   ./gcp-migration/gcp-setup/create-gcp-projects.sh <billing-account-id>
   ./gcp-migration/gcp-setup/setup-service-accounts.sh
   ./gcp-migration/gcp-setup/setup-cloud-sql.sh
   ./gcp-migration/gcp-setup/setup-artifact-registry.sh
   ./gcp-migration/gcp-setup/setup-secrets.sh
   ```

3. **Phase 3**: Docker Optimization (Already complete)
   - All Dockerfiles optimized
   - Cloud Run configs generated

4. **Phase 4**: Database Migration
   ```bash
   ./gcp-migration/database-migration/migrate-databases.sh staging <vm-ip> <cloud-sql-instance>
   ./gcp-migration/database-migration/validate-migration.sh staging <vm-ip> <cloud-sql-instance>
   ```

5. **Phase 5+**: Continue with remaining phases as documented

## Directory Structure

```
gcp-migration/
├── azure-backup/              # Phase 1: Azure backup and documentation
├── gcp-setup/                 # Phase 2: GCP project and infrastructure setup
├── cloud-run-configs/          # Phase 3: Cloud Run service configurations (13 files)
├── database-migration/        # Phase 4: Database migration scripts
├── ci-cd/                     # Phase 5: CI/CD pipeline configurations
├── deployment/                # Phase 6: Blue-green deployment scripts
├── monitoring/                # Phase 7: Monitoring and logging setup
├── domain/                    # Phase 8: Custom domain and SSL
├── tools/                     # Phase 9: Migration tools and testing
├── runbooks/                 # Phase 10: Operational runbooks
├── replace-placeholders.sh    # ⚠️ Run this first to replace placeholders
├── verify-placeholders.sh     # Verify all placeholders are replaced
└── Documentation files
```

## Key Features

- ✅ Zero-downtime blue-green deployment
- ✅ Multi-region setup (europe-west1 primary, europe-west4 replica)
- ✅ Production workflows **DISABLED** by default (safety)
- ✅ Complete Azure backup and restoration procedures
- ✅ Comprehensive monitoring and logging
- ✅ Automated CI/CD pipelines
- ✅ Database migration with validation

## Production Workflow Safety

**All production workflows are created but DISABLED**:
- Production GitHub Actions: `workflow_dispatch` only (manual trigger)
- Production Cloud Build: Created but disabled in GCP Console
- See `GCP_DEPLOYMENT_GUIDE.md` for enablement procedures

## Important Notes

### Placeholder Replacement

**CRITICAL**: All Cloud Run configuration files contain placeholders that MUST be replaced before deployment:

- `PROJECT_ID` → Your GCP project ID
- `REGION` → `europe-west1`
- `REPOSITORY` → Service name
- `INSTANCE_NAME` → Cloud SQL instance name
- `DATABASE_NAME` → Database name per service

**Run the replacement script**:
```bash
./gcp-migration/replace-placeholders.sh staging
./gcp-migration/verify-placeholders.sh
```

### Sequential Phase Execution

**IMPORTANT**: Each phase must be 100% complete before proceeding to the next phase. No parallel work across phases.

## Documentation

- [Migration Guide](GCP_MIGRATION_GUIDE.md) - Complete step-by-step migration
- [Deployment Guide](GCP_DEPLOYMENT_GUIDE.md) - GCP deployment procedures
- [Placeholder Replacement Guide](PLACEHOLDER_REPLACEMENT_GUIDE.md) - How to replace placeholders
- [Troubleshooting](TROUBLESHOOTING.md) - Common issues and solutions
- [Runbooks](runbooks/) - Operational procedures
- [Implementation Summary](IMPLEMENTATION_SUMMARY.md) - Complete implementation status

## Support

For issues or questions, refer to:
- Troubleshooting guide: `TROUBLESHOOTING.md`
- Runbooks: `runbooks/` directory
- Migration guide: `GCP_MIGRATION_GUIDE.md`

---

**Migration Package Status**: ✅ Complete and Ready  
**Last Updated**: November 2025
