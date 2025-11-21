#!/usr/bin/env python3
"""
Complete ML Training Pipeline
Trains all models, converts to TensorFlow Lite, and optimizes them
"""
import argparse
import sys
import subprocess
from pathlib import Path
import logging
import json
from datetime import datetime

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)


def run_command(cmd: list, description: str) -> bool:
    """Run a command and return success status"""
    logger.info(f"Running: {description}")
    logger.debug(f"Command: {' '.join(cmd)}")
    
    try:
        result = subprocess.run(cmd, check=True, capture_output=True, text=True)
        logger.info(f"✓ {description} completed successfully")
        return True
    except subprocess.CalledProcessError as e:
        logger.error(f"✗ {description} failed")
        logger.error(f"Error: {e.stderr}")
        return False


def train_all_models(args):
    """Train all ML models"""
    logger.info("=" * 60)
    logger.info("SmartWatts ML Model Training Pipeline")
    logger.info("=" * 60)
    
    scripts_dir = Path(__file__).parent
    base_dir = scripts_dir.parent
    models_dir = base_dir / "models"
    tflite_dir = base_dir / "models" / "tflite"
    quantized_dir = base_dir / "models" / "quantized"
    
    # Create output directories
    models_dir.mkdir(parents=True, exist_ok=True)
    tflite_dir.mkdir(parents=True, exist_ok=True)
    quantized_dir.mkdir(parents=True, exist_ok=True)
    
    results = {
        "training_started": datetime.now().isoformat(),
        "models": {}
    }
    
    # 1. Train Energy Forecasting Model
    logger.info("\n" + "=" * 60)
    logger.info("Step 1: Training Energy Forecasting Model")
    logger.info("=" * 60)
    
    success = run_command([
        sys.executable,
        str(scripts_dir / "train_energy_forecast.py"),
        "--data-dir", str(args.data_dir),
        "--output-dir", str(models_dir),
        "--epochs", str(args.epochs),
        "--batch-size", str(args.batch_size),
        "--sequence-length", str(args.sequence_length),
        "--forecast-horizon", "24"
    ], "Energy Forecasting Model Training")
    
    if success:
        results["models"]["energy_forecast"] = {"trained": True}
        
        # Convert to TensorFlow Lite
        logger.info("\nConverting Energy Forecast model to TensorFlow Lite...")
        convert_success = run_command([
            sys.executable,
            str(scripts_dir / "convert_to_tflite.py"),
            "--model-path", str(models_dir / "energy_forecast_model.h5"),
            "--output-path", str(tflite_dir / "energy_forecast.tflite"),
            "--quantization", "dynamic"
        ], "Energy Forecast TFLite Conversion")
        
        if convert_success:
            results["models"]["energy_forecast"]["converted"] = True
            
            # Quantize
            if args.quantize:
                logger.info("\nQuantizing Energy Forecast model...")
                quantize_success = run_command([
                    sys.executable,
                    str(scripts_dir / "quantize_models.py"),
                    "--input-path", str(tflite_dir / "energy_forecast.tflite"),
                    "--output-path", str(quantized_dir / "energy_forecast.tflite"),
                    "--quantization-type", "int8"
                ], "Energy Forecast Quantization")
                results["models"]["energy_forecast"]["quantized"] = quantize_success
    
    # 2. Train Anomaly Detection Model
    logger.info("\n" + "=" * 60)
    logger.info("Step 2: Training Anomaly Detection Model")
    logger.info("=" * 60)
    
    success = run_command([
        sys.executable,
        str(scripts_dir / "train_anomaly_detection.py"),
        "--data-dir", str(args.data_dir),
        "--output-dir", str(models_dir),
        "--epochs", str(args.epochs),
        "--batch-size", str(args.batch_size),
        "--sequence-length", str(args.sequence_length)
    ], "Anomaly Detection Model Training")
    
    if success:
        results["models"]["anomaly_detection"] = {"trained": True}
        
        # Convert to TensorFlow Lite
        logger.info("\nConverting Anomaly Detection model to TensorFlow Lite...")
        convert_success = run_command([
            sys.executable,
            str(scripts_dir / "convert_to_tflite.py"),
            "--model-path", str(models_dir / "anomaly_detection_model.h5"),
            "--output-path", str(tflite_dir / "anomaly_detection.tflite"),
            "--quantization", "dynamic"
        ], "Anomaly Detection TFLite Conversion")
        
        if convert_success:
            results["models"]["anomaly_detection"]["converted"] = True
            
            # Quantize
            if args.quantize:
                logger.info("\nQuantizing Anomaly Detection model...")
                quantize_success = run_command([
                    sys.executable,
                    str(scripts_dir / "quantize_models.py"),
                    "--input-path", str(tflite_dir / "anomaly_detection.tflite"),
                    "--output-path", str(quantized_dir / "anomaly_detection.tflite"),
                    "--quantization-type", "int8"
                ], "Anomaly Detection Quantization")
                results["models"]["anomaly_detection"]["quantized"] = quantize_success
    
    # 3. Train Load Prediction Model
    logger.info("\n" + "=" * 60)
    logger.info("Step 3: Training Load Prediction Model")
    logger.info("=" * 60)
    
    success = run_command([
        sys.executable,
        str(scripts_dir / "train_load_prediction.py"),
        "--data-dir", str(args.data_dir),
        "--output-dir", str(models_dir),
        "--epochs", str(args.epochs),
        "--batch-size", str(args.batch_size),
        "--sequence-length", str(args.sequence_length),
        "--prediction-horizon", "24"
    ], "Load Prediction Model Training")
    
    if success:
        results["models"]["load_prediction"] = {"trained": True}
        
        # Convert to TensorFlow Lite
        logger.info("\nConverting Load Prediction model to TensorFlow Lite...")
        convert_success = run_command([
            sys.executable,
            str(scripts_dir / "convert_to_tflite.py"),
            "--model-path", str(models_dir / "load_prediction_model.h5"),
            "--output-path", str(tflite_dir / "load_prediction.tflite"),
            "--quantization", "dynamic"
        ], "Load Prediction TFLite Conversion")
        
        if convert_success:
            results["models"]["load_prediction"]["converted"] = True
    
    # 4. Train Optimization Model
    logger.info("\n" + "=" * 60)
    logger.info("Step 4: Training Optimization Model")
    logger.info("=" * 60)
    
    success = run_command([
        sys.executable,
        str(scripts_dir / "train_optimization.py"),
        "--data-dir", str(args.data_dir),
        "--output-dir", str(models_dir),
        "--epochs", str(args.epochs),
        "--batch-size", str(args.batch_size),
        "--sequence-length", str(args.sequence_length)
    ], "Optimization Model Training")
    
    if success:
        results["models"]["optimization"] = {"trained": True}
        
        # Convert to TensorFlow Lite
        logger.info("\nConverting Optimization model to TensorFlow Lite...")
        convert_success = run_command([
            sys.executable,
            str(scripts_dir / "convert_to_tflite.py"),
            "--model-path", str(models_dir / "optimization_model.h5"),
            "--output-path", str(tflite_dir / "optimization.tflite"),
            "--quantization", "dynamic"
        ], "Optimization TFLite Conversion")
        
        if convert_success:
            results["models"]["optimization"]["converted"] = True
    
    # Save results
    results["training_completed"] = datetime.now().isoformat()
    results_path = base_dir / "training_results.json"
    with open(results_path, 'w') as f:
        json.dump(results, f, indent=2)
    
    logger.info("\n" + "=" * 60)
    logger.info("Training Pipeline Summary")
    logger.info("=" * 60)
    
    for model_name, status in results["models"].items():
        logger.info(f"{model_name}:")
        logger.info(f"  Trained: {'✓' if status.get('trained') else '✗'}")
        logger.info(f"  Converted: {'✓' if status.get('converted') else '✗'}")
        if args.quantize:
            logger.info(f"  Quantized: {'✓' if status.get('quantized') else '✗'}")
    
    logger.info(f"\nResults saved to {results_path}")
    logger.info("\nTensorFlow Lite models are ready in: models/tflite/")
    if args.quantize:
        logger.info("Quantized models are ready in: models/quantized/")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train All ML Models")
    parser.add_argument("--data-dir", type=str, required=True, help="Directory containing training data")
    parser.add_argument("--epochs", type=int, default=100, help="Number of training epochs")
    parser.add_argument("--batch-size", type=int, default=32, help="Batch size")
    parser.add_argument("--sequence-length", type=int, default=24, help="Input sequence length")
    parser.add_argument("--quantize", action="store_true", help="Quantize models after conversion")
    
    args = parser.parse_args()
    train_all_models(args)

