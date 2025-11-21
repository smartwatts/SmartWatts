# SmartWatts ML Training Architecture

## Overview

SmartWatts uses a **centralized training, distributed inference** architecture. Models are trained in the cloud using aggregated data from all edge devices, then distributed to edge gateways for local inference.

## Architecture Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLOUD BACKEND (GCP/AWS)                      │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  1. Data Aggregation                                      │  │
│  │     - Collects data from ALL edge gateways               │  │
│  │     - Stores in PostgreSQL (energy-service)              │  │
│  │     - Aggregates across all locations                    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  2. ML Training Pipeline (ml-training/)                  │  │
│  │     - Train models using aggregated data                 │  │
│  │     - Convert to TensorFlow Lite                         │  │
│  │     - Optimize and quantize                              │  │
│  │     - Validate model performance                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  3. Model Repository                                      │  │
│  │     - Store trained .tflite models                       │  │
│  │     - Version control                                    │  │
│  │     - Model registry                                     │  │
│  │     - URL: https://models.smartwatts.ng/                 │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                           ↓
                    (HTTP/HTTPS Download)
                           ↓
┌─────────────────────────────────────────────────────────────────┐
│              EDGE GATEWAY 1 (Home/Business)                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  4. Model Download (Auto)                                 │  │
│  │     - Downloads models from cloud                        │  │
│  │     - Checks for updates every hour                      │  │
│  │     - Stores locally: /opt/smartwatts/models/            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  5. Local Inference                                       │  │
│  │     - Runs TensorFlow Lite models                        │  │
│  │     - Real-time predictions                              │  │
│  │     - Works offline                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  6. Data Collection                                       │  │
│  │     - Collects energy data locally                       │  │
│  │     - Stores in SQLite (offline-first)                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↓                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  7. Data Sync to Cloud                                    │  │
│  │     - Syncs data every 5 minutes                         │  │
│  │     - Sends to energy-service                            │  │
│  │     - Works when internet available                      │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│              EDGE GATEWAY 2, 3, 4... (Other Locations)          │
│  (Same process: Download models → Inference → Sync data)        │
└─────────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. Cloud-Based Training (ml-training/)

**Location**: Cloud backend (GCP Cloud Run or dedicated training server)

**Process**:
1. **Data Collection**: Energy data from all edge gateways aggregated in PostgreSQL
2. **Training**: Run `train_all_models.py` on aggregated dataset
3. **Conversion**: Convert Keras models to TensorFlow Lite
4. **Optimization**: Quantize models for smaller size
5. **Deployment**: Upload models to model repository

**When to Train**:
- Weekly (automated)
- When new data threshold reached
- When model accuracy degrades
- Manual trigger for new model versions

### 2. Model Distribution

**Model Repository**: `https://models.smartwatts.ng/` (or GCP Cloud Storage)

**Edge Gateway Auto-Download**:
- Checks for model updates every hour (`sync-interval: 3600000`)
- Downloads new models automatically
- Validates model integrity
- Falls back to previous version if download fails

**Configuration** (in `edge-gateway`):
```yaml
edge:
  ml:
    models:
      sync-interval: 3600000  # 1 hour
      auto-download: true
```

### 3. Edge Gateway Inference

**Local Processing**:
- Models stored in `/opt/smartwatts/models/`
- TensorFlow Lite runtime for inference
- Real-time predictions (sub-second)
- Works completely offline

**No Training on Edge**:
- Edge devices only do **inference** (use trained models)
- No computational overhead for training
- Minimal resource usage

## Data Flow

### Edge → Cloud (Data Collection)

```
Edge Gateway (SQLite)
    ↓ (Every 5 minutes)
Data Sync Service
    ↓ (HTTP/MQTT)
Cloud Backend (PostgreSQL)
    ↓ (Aggregated)
Energy Service Database
```

**Data Collected**:
- Energy consumption readings
- Device status
- Anomaly detections
- Optimization recommendations

### Cloud → Edge (Model Distribution)

```
Cloud Model Repository
    ↓ (HTTP/HTTPS)
Edge Gateway Model Download
    ↓ (Auto-check every hour)
Local Model Storage
    ↓ (Load on startup)
TensorFlow Lite Runtime
```

**Models Distributed**:
- `energy_forecast.tflite`
- `anomaly_detection.tflite`
- `load_prediction.tflite`
- `optimization.tflite`

## Training Workflow

### Step 1: Collect Data (Automatic)

Edge gateways automatically sync data to cloud:
- Every 5 minutes when online
- Queued when offline, synced when back online
- All data aggregated in cloud database

### Step 2: Train Models (Cloud)

Run training pipeline on cloud server:

```bash
# On cloud server (GCP Cloud Run or dedicated VM)
cd ml-training

# Train all models using aggregated data
python scripts/train_all_models.py \
  --data-dir /path/to/aggregated/data \
  --epochs 100 \
  --quantize
```

**Data Source**: PostgreSQL database (energy-service)

### Step 3: Deploy Models (Cloud)

Upload trained models to repository:

```bash
# Upload to model repository (GCP Cloud Storage or CDN)
gsutil cp models/tflite/*.tflite gs://smartwatts-models/
# or
aws s3 cp models/tflite/*.tflite s3://smartwatts-models/
```

### Step 4: Edge Gateways Auto-Update (Automatic)

Edge gateways automatically:
1. Check for new models (every hour)
2. Download updated models
3. Validate and load new models
4. Continue inference with updated models

## Benefits of This Architecture

### ✅ Centralized Training
- **Single source of truth**: One training pipeline
- **Better models**: Trained on data from ALL locations
- **Easier management**: Update models once, deploy everywhere
- **Resource efficient**: Training uses cloud resources, not edge devices

### ✅ Distributed Inference
- **Low latency**: Real-time predictions on edge
- **Offline capable**: Works without internet
- **Privacy**: Sensitive data stays local
- **Scalable**: Each edge gateway independent

### ✅ Automatic Updates
- **No manual intervention**: Edge gateways auto-update
- **Version control**: Track model versions
- **Rollback capability**: Revert to previous models if needed
- **A/B testing**: Deploy different models to different gateways

## Implementation Details

### Model Repository Setup

**Option 1: GCP Cloud Storage**
```bash
# Create bucket
gsutil mb gs://smartwatts-models

# Upload models
gsutil cp models/tflite/*.tflite gs://smartwatts-models/

# Make publicly accessible (or use signed URLs)
gsutil iam ch allUsers:objectViewer gs://smartwatts-models
```

**Option 2: AWS S3**
```bash
# Create bucket
aws s3 mb s3://smartwatts-models

# Upload models
aws s3 cp models/tflite/*.tflite s3://smartwatts-models/

# Make publicly accessible
aws s3api put-bucket-policy --bucket smartwatts-models --policy file://policy.json
```

**Option 3: CDN (Recommended for Production)**
- Use CloudFlare, CloudFront, or similar
- Better performance and reliability
- Automatic caching and distribution

### Automated Training Pipeline

**Cloud Build / GitHub Actions**:
```yaml
# Weekly training job
name: Train ML Models
on:
  schedule:
    - cron: '0 2 * * 0'  # Every Sunday at 2 AM
  workflow_dispatch:  # Manual trigger

jobs:
  train:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Python
        uses: actions/setup-python@v2
        with:
          python-version: '3.9'
      - name: Install dependencies
        run: pip install -r ml-training/requirements.txt
      - name: Train models
        run: |
          python ml-training/scripts/train_all_models.py \
            --data-dir /path/to/data \
            --epochs 100 \
            --quantize
      - name: Upload models
        run: |
          gsutil cp ml-training/models/tflite/*.tflite gs://smartwatts-models/
```

## Summary

**Training**: ✅ **Cloud-based** (centralized, one location)
**Inference**: ✅ **Edge-based** (distributed, each gateway)
**Data Flow**: ✅ **Edge → Cloud** (for training)
**Model Flow**: ✅ **Cloud → Edge** (for inference)

**You train once in the cloud, deploy everywhere automatically!**

