# Advanced ML Models Training

## Overview

This directory contains the ML training framework for SmartWatts energy monitoring models. The framework supports training NILM (Non-Intrusive Load Monitoring) models, energy forecasting models, and anomaly detection models.

## Models

### 1. NILM (Non-Intrusive Load Monitoring)
- **Purpose**: Identify individual appliances from aggregate energy consumption
- **Framework**: TensorFlow Lite
- **Training Data**: Energy consumption signatures
- **Output**: Appliance-level consumption breakdown

### 2. Energy Forecasting
- **Purpose**: Predict future energy consumption
- **Framework**: TensorFlow Lite
- **Training Data**: Historical consumption patterns
- **Output**: 24-hour, 7-day, 30-day forecasts

### 3. Anomaly Detection
- **Purpose**: Detect unusual energy consumption patterns
- **Framework**: TensorFlow Lite
- **Training Data**: Normal consumption patterns
- **Output**: Anomaly scores and alerts

## Training Setup

### Prerequisites
```bash
# Install Python dependencies
pip install tensorflow tensorflow-lite numpy pandas scikit-learn matplotlib

# Install edge gateway dependencies
cd backend/edge-gateway
./gradlew build
```

### Training Data
- **Location**: `ml-training/data/`
- **Format**: CSV files with timestamp, consumption, device_id
- **Preprocessing**: Normalization, feature engineering, windowing

### Training Scripts
- **NILM Training**: `ml-training/train_nilm.py`
- **Forecasting Training**: `ml-training/train_forecasting.py`
- **Anomaly Detection Training**: `ml-training/train_anomaly.py`

## Usage

### Train NILM Model
```bash
python ml-training/train_nilm.py \
  --data-dir ml-training/data \
  --output-dir ml-training/models \
  --epochs 100 \
  --batch-size 32
```

### Train Forecasting Model
```bash
python ml-training/train_forecasting.py \
  --data-dir ml-training/data \
  --output-dir ml-training/models \
  --epochs 100 \
  --batch-size 32 \
  --forecast-horizon 24
```

### Train Anomaly Detection Model
```bash
python ml-training/train_anomaly.py \
  --data-dir ml-training/data \
  --output-dir ml-training/models \
  --epochs 100 \
  --batch-size 32 \
  --threshold 0.95
```

## Model Deployment

### Convert to TensorFlow Lite
```bash
python ml-training/convert_to_tflite.py \
  --model-path ml-training/models/nilm_model.h5 \
  --output-path ml-training/models/nilm_model.tflite
```

### Deploy to Edge Gateway
```bash
# Copy model to edge gateway
cp ml-training/models/*.tflite backend/edge-gateway/src/main/resources/models/

# Rebuild edge gateway
cd backend/edge-gateway
./gradlew build
```

## Model Evaluation

### Metrics
- **NILM**: Accuracy, F1-score, Mean Absolute Error (MAE)
- **Forecasting**: Mean Absolute Percentage Error (MAPE), Root Mean Squared Error (RMSE)
- **Anomaly Detection**: Precision, Recall, F1-score

### Evaluation Scripts
```bash
# Evaluate NILM model
python ml-training/evaluate_nilm.py \
  --model-path ml-training/models/nilm_model.tflite \
  --test-data ml-training/data/test.csv

# Evaluate forecasting model
python ml-training/evaluate_forecasting.py \
  --model-path ml-training/models/forecasting_model.tflite \
  --test-data ml-training/data/test.csv

# Evaluate anomaly detection model
python ml-training/evaluate_anomaly.py \
  --model-path ml-training/models/anomaly_model.tflite \
  --test-data ml-training/data/test.csv
```

## Continuous Training

### Automated Retraining
- **Schedule**: Weekly retraining with new data
- **Trigger**: New data threshold reached
- **Validation**: Model performance validation before deployment

### Model Versioning
- **Version Format**: `model_name_v{version}.tflite`
- **Storage**: Model registry with version history
- **Rollback**: Ability to rollback to previous versions

## References

- [TensorFlow Lite Documentation](https://www.tensorflow.org/lite)
- [NILM Research Papers](https://nilmworkshop.org/)
- [Energy Forecasting Best Practices](https://www.energy.gov/)


