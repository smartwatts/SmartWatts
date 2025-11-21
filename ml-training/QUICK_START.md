# ML Training Pipeline - Quick Start Guide

## Prerequisites

1. **Python 3.8+** installed
2. **pip** package manager

## Installation (5 minutes)

```bash
# Navigate to ml-training directory
cd ml-training

# Install dependencies
pip install -r requirements.txt
```

**Note**: TensorFlow installation may take a few minutes. If you encounter issues, use:
```bash
pip install tensorflow --upgrade
```

## Quick Test (Generate Sample Data & Train)

### Step 1: Generate Sample Data
```bash
python utils/generate_sample_data.py \
  --output data/raw/sample_energy_data.csv \
  --days 30 \
  --devices 3
```

### Step 2: Train a Single Model (Quick Test)
```bash
# Train energy forecasting model (takes ~5-10 minutes)
python scripts/train_energy_forecast.py \
  --data-dir data/raw \
  --output-dir models \
  --epochs 10 \
  --batch-size 32 \
  --sequence-length 24 \
  --forecast-horizon 24
```

### Step 3: Convert to TensorFlow Lite
```bash
python scripts/convert_to_tflite.py \
  --model-path models/energy_forecast_model.h5 \
  --output-path models/tflite/energy_forecast.tflite \
  --quantization dynamic
```

### Step 4: Deploy to Edge Gateway
```bash
# Copy model to edge gateway
cp models/tflite/energy_forecast.tflite \
   ../backend/edge-gateway/src/main/resources/models/

# Rebuild edge gateway (optional)
cd ../backend/edge-gateway
./gradlew build
```

## Full Training Pipeline

Train all 4 models with one command:

```bash
python scripts/train_all_models.py \
  --data-dir data/raw \
  --epochs 100 \
  --batch-size 32 \
  --sequence-length 24 \
  --quantize
```

This will:
1. ✅ Train all 4 models (Energy Forecast, Anomaly Detection, Load Prediction, Optimization)
2. ✅ Convert each to TensorFlow Lite format
3. ✅ Quantize models for smaller size
4. ✅ Save results to `training_results.json`

**Time**: ~30-60 minutes depending on data size and hardware

## Using Your Own Data

1. **Prepare CSV files** with columns:
   - `timestamp` (required)
   - `consumption` (required)
   - `device_id` (optional)
   - `cost` (optional)

2. **Place files** in `data/raw/` directory

3. **Run training**:
```bash
python scripts/train_all_models.py \
  --data-dir data/raw \
  --epochs 100 \
  --quantize
```

## Model Outputs

After training, you'll find:

- **Keras Models**: `models/*.h5` (full precision)
- **TFLite Models**: `models/tflite/*.tflite` (optimized for edge)
- **Quantized Models**: `models/quantized/*.tflite` (smallest size)
- **Metadata**: `models/*_metadata.json` (model info)
- **Training History**: `models/*_history.json` (training metrics)

## Next Steps

1. **Evaluate Models**: Use `scripts/evaluate_model.py` to test accuracy
2. **Deploy Models**: Copy `.tflite` files to edge gateway
3. **Monitor Performance**: Check model accuracy in production
4. **Retrain**: Set up weekly retraining with new data

## Troubleshooting

### "ModuleNotFoundError: No module named 'tensorflow'"
```bash
pip install tensorflow tensorflow-lite
```

### "Out of Memory" errors
- Reduce batch size: `--batch-size 16`
- Reduce sequence length: `--sequence-length 12`
- Use smaller dataset for testing

### "No CSV files found"
- Check that CSV files are in `data/raw/` directory
- Verify CSV has `timestamp` and `consumption` columns

## Need Help?

See the full [README.md](README.md) for detailed documentation.

