# ML Training Pipeline Setup Scripts

This directory contains scripts to set up the complete ML training pipeline.

## Quick Setup (Everything at Once)

```bash
cd ml-training/setup
./complete-setup.sh
```

This will:
1. ✅ Install Python dependencies
2. ✅ Create directory structure
3. ✅ Make scripts executable
4. ✅ Set up model repository (GCP Cloud Storage)
5. ✅ Set up automated training (GitHub Actions)
6. ✅ Generate sample data (optional)

## Individual Setup Scripts

### 1. Complete Setup
```bash
./complete-setup.sh
```
One-command setup for everything.

### 2. Model Repository Setup
```bash
./setup-model-repository.sh
```
Creates GCP Cloud Storage bucket for storing trained models.

**Requirements:**
- `gcloud` CLI installed and authenticated
- GCP project with billing enabled

**Environment Variables:**
- `GCP_PROJECT_ID` (default: `smartwatts-staging`)
- `MODEL_BUCKET_NAME` (default: `smartwatts-models`)
- `GCP_REGION` (default: `europe-west1`)

### 3. Extract Training Data
```bash
./extract-training-data.sh
```
Extracts energy consumption data from PostgreSQL database.

**Requirements:**
- `psql` (PostgreSQL client)
- Database access credentials

**Environment Variables:**
- `DB_HOST` (default: `127.0.0.1`)
- `DB_PORT` (default: `5433`)
- `DB_NAME` (default: `smartwatts_energy`)
- `DB_USER` (default: `postgres`)
- `DB_PASSWORD` (prompted if not set)
- `OUTPUT_DIR` (default: `../data/raw`)
- `DAYS_BACK` (default: `365`)

### 4. Upload Models
```bash
./upload-models.sh
```
Uploads trained models to GCP Cloud Storage repository.

**Requirements:**
- `gsutil` (from Google Cloud SDK)
- Trained models in `../models/tflite/`

**Environment Variables:**
- `MODEL_BUCKET_NAME` (default: `smartwatts-models`)
- `MODELS_DIR` (default: `../models/tflite`)
- `MODEL_VERSION` (default: timestamp)

### 5. Automated Training Setup
```bash
./setup-automated-training.sh
```
Creates GitHub Actions workflow for automated weekly training.

**Requirements:**
- GitHub repository
- GCP service account key
- Database credentials

**See:** `SETUP_INSTRUCTIONS.md` for detailed setup steps.

## Usage Examples

### Full Setup (First Time)
```bash
# 1. Run complete setup
cd ml-training/setup
./complete-setup.sh

# 2. Extract data from database
./extract-training-data.sh

# 3. Train models
cd ..
python scripts/train_all_models.py --data-dir data/raw --epochs 100 --quantize

# 4. Upload models
cd setup
./upload-models.sh
```

### Manual Training Workflow
```bash
# 1. Extract data
./extract-training-data.sh

# 2. Train models
cd ..
python scripts/train_all_models.py --data-dir data/raw --epochs 100 --quantize

# 3. Upload models
cd setup
./upload-models.sh
```

### Automated Training (GitHub Actions)
```bash
# 1. Setup automated training
./setup-automated-training.sh

# 2. Add GitHub secrets (see SETUP_INSTRUCTIONS.md)
#    - GCP_SA_KEY
#    - DB_HOST, DB_PORT, DB_USER, DB_PASSWORD

# 3. Trigger workflow manually or wait for schedule
#    (Every Sunday at 2 AM UTC)
```

## Environment Configuration

Create a `.env` file in `ml-training/` directory:

```bash
# GCP Configuration
GCP_PROJECT_ID=smartwatts-staging
MODEL_BUCKET_NAME=smartwatts-models
GCP_REGION=europe-west1

# Database Configuration
DB_HOST=127.0.0.1
DB_PORT=5433
DB_NAME=smartwatts_energy
DB_USER=postgres
DB_PASSWORD=your_password

# Model Configuration
MODEL_VERSION=1.0.0
```

Then source it before running scripts:
```bash
source .env
./complete-setup.sh
```

## Troubleshooting

### "gcloud not found"
Install Google Cloud SDK:
```bash
# macOS
brew install google-cloud-sdk

# Linux
curl https://sdk.cloud.google.com | bash
```

### "psql not found"
Install PostgreSQL client:
```bash
# macOS
brew install postgresql

# Linux
sudo apt-get install postgresql-client
```

### "Permission denied"
Make scripts executable:
```bash
chmod +x setup/*.sh
```

### Database connection failed
- Check database is running
- Verify credentials
- Check network connectivity
- For Cloud SQL, use Cloud SQL Proxy

## Next Steps

After setup:
1. Review `../QUICK_START.md` for training instructions
2. Read `../TRAINING_ARCHITECTURE.md` for architecture details
3. Check `SETUP_INSTRUCTIONS.md` for GitHub Actions setup

