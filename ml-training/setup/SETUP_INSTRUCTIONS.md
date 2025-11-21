# Automated Training Setup Instructions

## Prerequisites

1. **GitHub Repository**: Your code must be in a GitHub repository
2. **GCP Service Account**: Create a service account with Cloud Storage access
3. **GitHub Secrets**: Add required secrets to your repository

## Step 1: Create GCP Service Account

```bash
# Create service account
gcloud iam service-accounts create ml-training-sa \
  --display-name="ML Training Service Account"

# Grant Cloud Storage access
gcloud projects add-iam-policy-binding smartwatts-staging \
  --member="serviceAccount:ml-training-sa@smartwatts-staging.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

# Create and download key
gcloud iam service-accounts keys create ml-training-key.json \
  --iam-account=ml-training-sa@smartwatts-staging.iam.gserviceaccount.com
```

## Step 2: Add GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add these secrets:

- `GCP_SA_KEY`: Contents of `ml-training-key.json` (the entire JSON file)
- `DB_HOST`: Database host (e.g., `127.0.0.1` or Cloud SQL IP)
- `DB_PORT`: Database port (e.g., `5433`)
- `DB_USER`: Database user (e.g., `postgres`)
- `DB_PASSWORD`: Database password

## Step 3: Enable GitHub Actions

1. Go to repository Settings → Actions → General
2. Enable "Allow all actions and reusable workflows"
3. Save changes

## Step 4: Test the Workflow

1. Go to Actions tab in GitHub
2. Select "ML Model Training" workflow
3. Click "Run workflow" → "Run workflow"
4. Monitor the execution

## Manual Training

You can also trigger training manually:

```bash
# On your local machine or cloud server
cd ml-training
python scripts/train_all_models.py --data-dir data/raw --epochs 100 --quantize
cd setup
./upload-models.sh
```

## Schedule

The workflow runs automatically:
- **Every Sunday at 2 AM UTC**
- **On push to main** (if ml-training files change)
- **Manually** (via GitHub Actions UI)

## Monitoring

- Check GitHub Actions tab for training status
- View training results in workflow summary
- Download trained models from artifacts
- Check GCP Cloud Storage for uploaded models
