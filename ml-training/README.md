# SmartWatts ML Model Training Pipeline

Complete machine learning training pipeline for SmartWatts energy monitoring models. This pipeline trains models using TensorFlow/Keras, converts them to TensorFlow Lite format, and optimizes them for edge deployment.

## Overview

The training pipeline supports 4 main model types:
1. **Energy Forecasting** - Predicts future energy consumption (LSTM-based)
2. **Anomaly Detection** - Detects unusual energy patterns (Autoencoder-based)
3. **Load Prediction** - Forecasts load patterns (GRU-based)
4. **Optimization** - Provides optimization recommendations (DNN-based)

## Quick Start

### 1. Install Dependencies

```bash
cd ml-training
pip install -r requirements.txt
```

### 2. Prepare Training Data

Place your training data CSV files in `data/raw/` directory. The CSV should have columns:
- `timestamp` - DateTime of the reading
- `consumption` - Energy consumption in kWh
- `device_id` - Device identifier (optional)
- `cost` - Cost in Naira (optional)

**Generate Sample Data (for testing):**
```bash
python utils/generate_sample_data.py --output data/raw/sample_energy_data.csv --days 365 --devices 5
```

### 3. Train All Models

```bash
# Train all models, convert to TFLite, and quantize
python scripts/train_all_models.py \
  --data-dir data/raw \
  --epochs 100 \
  --batch-size 32 \
  --sequence-length 24 \
  --quantize
```

### 4. Train Individual Models

```bash
# Energy Forecasting
python scripts/train_energy_forecast.py \
  --data-dir data/raw \
  --output-dir models \
  --epochs 100 \
  --batch-size 32 \
  --sequence-length 24 \
  --forecast-horizon 24

# Anomaly Detection
python scripts/train_anomaly_detection.py \
  --data-dir data/raw \
  --output-dir models \
  --epochs 100 \
  --batch-size 32 \
  --sequence-length 24

# Load Prediction
python scripts/train_load_prediction.py \
  --data-dir data/raw \
  --output-dir models \
  --epochs 100 \
  --batch-size 32 \
  --sequence-length 24 \
  --prediction-horizon 24

# Optimization
python scripts/train_optimization.py \
  --data-dir data/raw \
  --output-dir models \
  --epochs 100 \
  --batch-size 32 \
  --sequence-length 24
```

### 5. Convert to TensorFlow Lite

```bash
# Convert Keras model to TFLite
python scripts/convert_to_tflite.py \
  --model-path models/energy_forecast_model.h5 \
  --output-path models/tflite/energy_forecast.tflite \
  --quantization dynamic

# Quantization options:
# - none: No quantization (largest, most accurate)
# - dynamic: Dynamic range quantization (good balance)
# - int8: INT8 quantization (smallest, may have accuracy loss)
# - float16: Float16 quantization (good for GPU acceleration)
```

### 6. Quantize Models (Optional)

```bash
# Quantize existing TFLite model
python scripts/quantize_models.py \
  --input-path models/tflite/energy_forecast.tflite \
  --output-path models/quantized/energy_forecast.tflite \
  --quantization-type int8
```

### 7. Evaluate Models

```bash
python scripts/evaluate_model.py \
  --model-path models/tflite/energy_forecast.tflite \
  --test-data data/raw/test_data.csv \
  --model-type forecast \
  --output evaluation_results.json
```

## Directory Structure

```
ml-training/
├── data/
│   ├── raw/              # Raw training data (CSV files)
│   └── processed/        # Processed/preprocessed data
├── models/
│   ├── tflite/          # TensorFlow Lite models
│   └── quantized/       # Quantized models
├── scripts/
│   ├── train_energy_forecast.py
│   ├── train_anomaly_detection.py
│   ├── train_load_prediction.py
│   ├── train_optimization.py
│   ├── train_all_models.py      # Complete pipeline
│   ├── convert_to_tflite.py
│   ├── quantize_models.py
│   └── evaluate_model.py
├── utils/
│   ├── data_loader.py           # Data loading utilities
│   └── generate_sample_data.py  # Sample data generator
├── requirements.txt
└── README.md
```

## Model Details

### 1. Energy Forecasting Model
- **Architecture**: LSTM (128 → 64 → 32 units)
- **Input**: 24 hours of historical consumption
- **Output**: 24-hour forecast
- **Loss**: MSE (Mean Squared Error)
- **Metrics**: MAE, MAPE

### 2. Anomaly Detection Model
- **Architecture**: Autoencoder (64 → 32 → 16 → 32 → 64)
- **Input**: 24 hours of consumption data
- **Output**: Reconstructed input (anomaly score = reconstruction error)
- **Loss**: MSE
- **Threshold**: 95th percentile of training reconstruction errors

### 3. Load Prediction Model
- **Architecture**: GRU (128 → 64 → 32 units)
- **Input**: 24 hours of historical load
- **Output**: 24-hour load prediction
- **Loss**: MSE
- **Metrics**: MAE, MAPE

### 4. Optimization Model
- **Architecture**: DNN (128 → 64 → 32 → 16 → 1)
- **Input**: Consumption, cost, time features
- **Output**: Optimization score (0-1)
- **Loss**: MSE
- **Metrics**: MAE

## Quantization

Quantization reduces model size and speeds up inference:

- **Dynamic Range Quantization**: ~4x smaller, minimal accuracy loss
- **INT8 Quantization**: ~4x smaller, may have accuracy loss
- **Float16 Quantization**: ~2x smaller, good for GPU

**Recommended**: Start with dynamic range quantization, use INT8 if model size is critical.

## Deployment

After training, deploy models to edge gateway:

```bash
# Copy TFLite models to edge gateway
cp models/tflite/*.tflite backend/edge-gateway/src/main/resources/models/

# Or copy quantized models
cp models/quantized/*.tflite backend/edge-gateway/src/main/resources/models/

# Rebuild edge gateway
cd backend/edge-gateway
./gradlew build
```

## Model Performance Targets

- **Energy Forecasting**: MAPE < 15%, MAE < 10 kWh
- **Anomaly Detection**: Precision > 0.85, Recall > 0.80
- **Load Prediction**: MAPE < 12%, MAE < 8 kW
- **Optimization**: R² Score > 0.70

## Continuous Training

Set up automated retraining:

```bash
# Weekly retraining (add to cron)
0 2 * * 0 cd /path/to/ml-training && python scripts/train_all_models.py --data-dir data/raw --epochs 50 --quantize
```

## Troubleshooting

### Out of Memory
- Reduce batch size: `--batch-size 16`
- Reduce sequence length: `--sequence-length 12`
- Use data generators for large datasets

### Poor Model Performance
- Increase training data (more days/records)
- Increase model complexity (more layers/units)
- Tune hyperparameters (learning rate, dropout)
- Check data quality and preprocessing

### Conversion Issues
- Ensure TensorFlow version >= 2.13.0
- Check model architecture compatibility
- Verify input/output shapes match

## References

- [TensorFlow Lite Documentation](https://www.tensorflow.org/lite)
- [Model Optimization Guide](https://www.tensorflow.org/model_optimization)
- [NILM Research](https://nilmworkshop.org/)
