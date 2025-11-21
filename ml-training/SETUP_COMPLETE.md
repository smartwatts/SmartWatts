# ML Training Pipeline - Complete Setup ✅

## What's Been Set Up

Everything is now automated! Here's what's ready:

### ✅ Training Pipeline
- **Training Scripts**: 4 model types (Energy Forecast, Anomaly Detection, Load Prediction, Optimization)
- **TensorFlow Lite Conversion**: Automatic conversion with quantization
- **Model Evaluation**: Performance metrics and validation

### ✅ Model Repository
- **GCP Cloud Storage**: Script to create bucket and configure access
- **Public URLs**: Models accessible via HTTPS
- **Version Control**: Track model versions

### ✅ Data Extraction
- **Database Export**: Extract training data from PostgreSQL
- **CSV Format**: Ready for training pipeline
- **Configurable**: Days back, output directory, etc.

### ✅ Model Upload
- **Automated Upload**: Upload trained models to repository
- **Versioning**: Save versioned copies
- **Public Access**: Set permissions automatically

### ✅ Automated Training
- **GitHub Actions**: Weekly automated training
- **Manual Trigger**: Run training on-demand
- **Complete Workflow**: Extract → Train → Upload

### ✅ Edge Gateway Integration
- **Auto-Download**: Edge gateways download models automatically
- **Hourly Updates**: Check for new models every hour
- **Fallback Support**: Use local models if download fails

## Quick Start

### Option 1: Complete Automated Setup (Recommended)

```bash
cd ml-training/setup
./complete-setup.sh
```

This will:
1. ✅ Install dependencies
2. ✅ Create directories
3. ✅ Set up model repository
4. ✅ Set up automated training
5. ✅ Generate sample data (optional)

### Option 2: Manual Setup

```bash
# 1. Install dependencies
cd ml-training
pip install -r requirements.txt

# 2. Set up model repository
cd setup
./setup-model-repository.sh

# 3. Extract training data
./extract-training-data.sh

# 4. Train models
cd ..
python scripts/train_all_models.py --data-dir data/raw --epochs 100 --quantize

# 5. Upload models
cd setup
./upload-models.sh

# 6. Update edge gateway config
./update-edge-gateway-config.sh
```

## Automated Workflow

Once set up, the system works automatically:

```
┌─────────────────────────────────────────┐
│  Every Sunday at 2 AM UTC               │
│  (GitHub Actions)                       │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  1. Extract data from database          │
│     (All edge gateway data aggregated)  │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  2. Train all models                    │
│     (Energy, Anomaly, Load, Optimization)│
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  3. Convert to TensorFlow Lite          │
│     (Optimize and quantize)             │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  4. Upload to GCP Cloud Storage         │
│     (Public repository)                 │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  5. Edge Gateways Auto-Download         │
│     (Every hour, all locations)         │
└─────────────────────────────────────────┘
              ↓
┌─────────────────────────────────────────┐
│  6. Models Ready for Inference          │
│     (Real-time predictions)             │
└─────────────────────────────────────────┘
```

## Configuration

### Environment Variables

Create `ml-training/.env`:

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
```

### GitHub Secrets (for automated training)

Add to GitHub repository → Settings → Secrets:

- `GCP_SA_KEY`: GCP service account JSON key
- `DB_HOST`: Database host
- `DB_PORT`: Database port
- `DB_USER`: Database user
- `DB_PASSWORD`: Database password

## What You Need to Do

### 1. Run Complete Setup (One Time)

```bash
cd ml-training/setup
./complete-setup.sh
```

### 2. Add GitHub Secrets (For Automated Training)

1. Go to GitHub repository → Settings → Secrets
2. Add the secrets listed above
3. See `setup/SETUP_INSTRUCTIONS.md` for details

### 3. That's It!

The system will:
- ✅ Train models automatically (weekly)
- ✅ Upload to repository
- ✅ Edge gateways auto-download
- ✅ Everything works automatically

## Manual Training (If Needed)

If you want to train manually:

```bash
# Extract data
cd ml-training/setup
./extract-training-data.sh

# Train models
cd ..
python scripts/train_all_models.py --data-dir data/raw --epochs 100 --quantize

# Upload models
cd setup
./upload-models.sh
```

## Files Created

### Setup Scripts
- `setup/complete-setup.sh` - One-command setup
- `setup/setup-model-repository.sh` - GCP bucket setup
- `setup/extract-training-data.sh` - Database export
- `setup/upload-models.sh` - Model upload
- `setup/setup-automated-training.sh` - GitHub Actions setup
- `setup/update-edge-gateway-config.sh` - Edge gateway config

### Training Scripts
- `scripts/train_energy_forecast.py` - Energy forecasting
- `scripts/train_anomaly_detection.py` - Anomaly detection
- `scripts/train_load_prediction.py` - Load prediction
- `scripts/train_optimization.py` - Optimization
- `scripts/train_all_models.py` - Complete pipeline
- `scripts/convert_to_tflite.py` - TFLite conversion
- `scripts/quantize_models.py` - Model quantization
- `scripts/evaluate_model.py` - Model evaluation

### Documentation
- `README.md` - Full documentation
- `QUICK_START.md` - Quick start guide
- `TRAINING_ARCHITECTURE.md` - Architecture details
- `setup/README.md` - Setup instructions
- `setup/SETUP_INSTRUCTIONS.md` - GitHub Actions setup

## Next Steps

1. **Run Setup**: `cd ml-training/setup && ./complete-setup.sh`
2. **Add Secrets**: Configure GitHub secrets for automated training
3. **Test Training**: Run manual training to verify everything works
4. **Monitor**: Check GitHub Actions for automated training status

## Support

- **Quick Start**: See `QUICK_START.md`
- **Architecture**: See `TRAINING_ARCHITECTURE.md`
- **Setup Help**: See `setup/README.md`
- **Troubleshooting**: See `setup/README.md` troubleshooting section

---

**Everything is ready! Just run the setup script and you're done!** 🚀
