# GCP Cloud Run Deployment Guide

## Overview

Complete guide for deploying SmartWatts to GCP Cloud Run.

## Architecture

[GCP architecture overview]

## Prerequisites

1. GCP projects created (staging and production)
2. Service accounts configured
3. Cloud SQL instances created
4. Artifact Registry repositories created
5. Secrets migrated to Secret Manager

## Deployment Steps

### 1. Build and Push Docker Images

```bash
./gcp-migration/ci-cd/build-and-push-images.sh staging
```

### 2. Deploy Services

```bash
./gcp-migration/ci-cd/deploy-services.sh staging
```

### 3. Verify Deployment

```bash
./gcp-migration/deployment/verify-deployment.sh staging
```

## Production Workflow Enablement

**IMPORTANT**: Production workflows are created but DISABLED by default.

To enable production workflows:

1. **GitHub Actions**: The workflow file is already created with `workflow_dispatch` only. To enable automatic triggers, edit `.github/workflows/gcp-production-deploy.yml` and add push triggers.

2. **Cloud Build**: The Cloud Build trigger should be created but disabled in GCP Console. To enable:
   - Go to Cloud Build → Triggers
   - Find the production trigger
   - Enable it when ready

3. **Verification**: Test production deployment in staging first, then enable production.

## Scaling

[Scaling strategies and configuration]

## Cost Optimization

[Cost optimization tips and best practices]

## Troubleshooting

[Common issues and solutions]

