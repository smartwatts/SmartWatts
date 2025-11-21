#!/bin/bash
# Setup automated ML model training pipeline
# Creates GitHub Actions workflow for weekly training

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
WORKFLOWS_DIR="$PROJECT_ROOT/.github/workflows"

echo "=========================================="
echo "Setting up Automated Training Pipeline"
echo "=========================================="
echo "Project root: $PROJECT_ROOT"
echo ""

# Create .github/workflows directory if it doesn't exist
mkdir -p "$WORKFLOWS_DIR"

# Create training workflow
echo "Creating GitHub Actions workflow..."
cat > "$WORKFLOWS_DIR/ml-training.yml" << 'EOF'
name: ML Model Training

on:
  schedule:
    # Run every Sunday at 2 AM UTC
    - cron: '0 2 * * 0'
  workflow_dispatch:  # Allow manual trigger
  push:
    branches:
      - main
    paths:
      - 'ml-training/**'
      - '.github/workflows/ml-training.yml'

env:
  GCP_PROJECT_ID: smartwatts-staging
  MODEL_BUCKET_NAME: smartwatts-models
  DB_HOST: ${{ secrets.DB_HOST }}
  DB_PORT: ${{ secrets.DB_PORT }}
  DB_NAME: smartwatts_energy
  DB_USER: ${{ secrets.DB_USER }}
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}

jobs:
  train-models:
    runs-on: ubuntu-latest
    timeout-minutes: 120
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.9'
          cache: 'pip'
      
      - name: Install dependencies
        run: |
          cd ml-training
          pip install -r requirements.txt
      
      - name: Authenticate to Google Cloud
        uses: google-github-actions/auth@v1
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}
      
      - name: Set up Cloud SDK
        uses: google-github-actions/setup-gcloud@v1
      
      - name: Extract training data
        run: |
          cd ml-training/setup
          chmod +x extract-training-data.sh
          ./extract-training-data.sh
        env:
          DB_HOST: ${{ secrets.DB_HOST }}
          DB_PORT: ${{ secrets.DB_PORT }}
          DB_NAME: smartwatts_energy
          DB_USER: ${{ secrets.DB_USER }}
          DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
      
      - name: Train all models
        run: |
          cd ml-training
          python scripts/train_all_models.py \
            --data-dir data/raw \
            --epochs 100 \
            --batch-size 32 \
            --sequence-length 24 \
            --quantize
      
      - name: Upload models to repository
        run: |
          cd ml-training/setup
          chmod +x upload-models.sh
          ./upload-models.sh
        env:
          MODEL_BUCKET_NAME: smartwatts-models
          MODELS_DIR: ../models/tflite
      
      - name: Create training report
        run: |
          cd ml-training
          echo "## Training Results" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          echo "Training completed at $(date)" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          if [ -f training_results.json ]; then
            echo "\`\`\`json" >> $GITHUB_STEP_SUMMARY
            cat training_results.json >> $GITHUB_STEP_SUMMARY
            echo "\`\`\`" >> $GITHUB_STEP_SUMMARY
          fi
      
      - name: Upload training artifacts
        uses: actions/upload-artifact@v3
        with:
          name: trained-models
          path: |
            ml-training/models/tflite/*.tflite
            ml-training/training_results.json
          retention-days: 30
EOF

echo "✓ GitHub Actions workflow created"

# Create setup instructions
cat > "$SCRIPT_DIR/SETUP_INSTRUCTIONS.md" << 'EOF'
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
EOF

echo "✓ Setup instructions created"
echo ""
echo "=========================================="
echo "Automated Training Setup Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Review: ml-training/setup/SETUP_INSTRUCTIONS.md"
echo "2. Add GitHub secrets (GCP_SA_KEY, DB credentials)"
echo "3. Test workflow: GitHub → Actions → Run workflow"
echo ""
echo "The workflow will:"
echo "  - Run every Sunday at 2 AM UTC"
echo "  - Extract data from database"
echo "  - Train all models"
echo "  - Upload to GCP Cloud Storage"
echo "  - Edge gateways auto-download new models"
echo ""

