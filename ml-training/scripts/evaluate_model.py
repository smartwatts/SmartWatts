#!/usr/bin/env python3
"""
Evaluate TensorFlow Lite models
"""
import argparse
import sys
from pathlib import Path
import logging
import numpy as np
import tensorflow as tf
import pandas as pd
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
import json

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def evaluate_tflite_model(model_path: str, test_data_path: str, 
                          model_type: str = "forecast") -> dict:
    """Evaluate a TensorFlow Lite model"""
    logger.info(f"Loading model from {model_path}...")
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    
    logger.info(f"Input shape: {input_details[0]['shape']}")
    logger.info(f"Output shape: {output_details[0]['shape']}")
    
    # Load test data
    logger.info(f"Loading test data from {test_data_path}...")
    df = pd.read_csv(test_data_path)
    
    # Prepare test data (simplified - adjust based on actual data format)
    # This is a placeholder - actual implementation depends on data format
    test_data = df['consumption'].values[:100]  # Sample
    test_data = test_data.reshape(-1, 1)
    
    predictions = []
    actuals = []
    
    for i in range(len(test_data) - input_details[0]['shape'][1]):
        # Prepare input
        input_data = test_data[i:i + input_details[0]['shape'][1]]
        input_data = input_data.astype(input_details[0]['dtype'])
        
        # Run inference
        interpreter.set_tensor(input_details[0]['index'], input_data)
        interpreter.invoke()
        
        # Get output
        output = interpreter.get_tensor(output_details[0]['index'])
        predictions.append(output[0])
        actuals.append(test_data[i + input_details[0]['shape'][1]])
    
    # Calculate metrics
    predictions = np.array(predictions)
    actuals = np.array(actuals)
    
    mae = mean_absolute_error(actuals, predictions)
    mse = mean_squared_error(actuals, predictions)
    rmse = np.sqrt(mse)
    r2 = r2_score(actuals, predictions)
    
    metrics = {
        "mae": float(mae),
        "mse": float(mse),
        "rmse": float(rmse),
        "r2_score": float(r2)
    }
    
    logger.info(f"MAE: {mae:.4f}")
    logger.info(f"RMSE: {rmse:.4f}")
    logger.info(f"R² Score: {r2:.4f}")
    
    return metrics


def main():
    parser = argparse.ArgumentParser(description="Evaluate TensorFlow Lite Model")
    parser.add_argument("--model-path", type=str, required=True, help="Path to .tflite model")
    parser.add_argument("--test-data", type=str, required=True, help="Path to test data CSV")
    parser.add_argument("--model-type", type=str, default="forecast",
                       choices=["forecast", "anomaly", "optimization"],
                       help="Type of model")
    parser.add_argument("--output", type=str, default=None, help="Output JSON file for metrics")
    
    args = parser.parse_args()
    
    metrics = evaluate_tflite_model(args.model_path, args.test_data, args.model_type)
    
    if args.output:
        with open(args.output, 'w') as f:
            json.dump(metrics, f, indent=2)
        logger.info(f"Metrics saved to {args.output}")


if __name__ == "__main__":
    main()

