#!/usr/bin/env python3
"""
Convert TensorFlow/Keras models to TensorFlow Lite format
Supports quantization for smaller model sizes
"""
import argparse
import sys
from pathlib import Path
import logging
import tensorflow as tf
import json
from tensorflow import lite as tflite
from tensorflow.lite.python import lite_constants

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def convert_to_tflite(model_path: str, output_path: str, 
                      quantization: str = "none",
                      representative_dataset=None) -> None:
    """
    Convert Keras model to TensorFlow Lite format
    
    Args:
        model_path: Path to Keras model (.h5 file)
        output_path: Path to save TensorFlow Lite model (.tflite file)
        quantization: Quantization type ('none', 'dynamic', 'int8', 'float16')
        representative_dataset: Representative dataset for quantization
    """
    logger.info(f"Loading model from {model_path}...")
    model = tf.keras.models.load_model(model_path)
    
    logger.info("Converting to TensorFlow Lite...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Apply quantization
    if quantization == "dynamic":
        logger.info("Applying dynamic range quantization...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
    elif quantization == "int8":
        logger.info("Applying INT8 quantization...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        if representative_dataset is not None:
            converter.representative_dataset = representative_dataset
        converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
        converter.inference_input_type = tf.int8
        converter.inference_output_type = tf.int8
    elif quantization == "float16":
        logger.info("Applying Float16 quantization...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.float16]
    else:
        logger.info("No quantization applied")
    
    # Convert
    tflite_model = converter.convert()
    
    # Save
    output_path = Path(output_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_bytes(tflite_model)
    
    # Get model size
    model_size_mb = len(tflite_model) / (1024 * 1024)
    logger.info(f"TensorFlow Lite model saved to {output_path}")
    logger.info(f"Model size: {model_size_mb:.2f} MB")
    
    return model_size_mb


def create_representative_dataset(data_path: str, num_samples: int = 100):
    """Create representative dataset for quantization"""
    import numpy as np
    import pandas as pd
    
    # Load sample data
    df = pd.read_csv(data_path)
    # This is a simplified version - in production, use actual model input format
    samples = df['consumption'].values[:num_samples]
    samples = samples.reshape(-1, 1)
    
    def representative_data_gen():
        for sample in samples:
            yield [sample.astype(np.float32)]
    
    return representative_data_gen


def main():
    parser = argparse.ArgumentParser(description="Convert Keras model to TensorFlow Lite")
    parser.add_argument("--model-path", type=str, required=True, help="Path to Keras model (.h5)")
    parser.add_argument("--output-path", type=str, required=True, help="Output path for .tflite model")
    parser.add_argument("--quantization", type=str, default="none",
                       choices=["none", "dynamic", "int8", "float16"],
                       help="Quantization type")
    parser.add_argument("--representative-data", type=str, default=None,
                       help="Path to representative dataset for INT8 quantization")
    
    args = parser.parse_args()
    
    representative_dataset = None
    if args.quantization == "int8" and args.representative_data:
        logger.info("Creating representative dataset...")
        representative_dataset = create_representative_dataset(args.representative_data)
    
    model_size = convert_to_tflite(
        args.model_path,
        args.output_path,
        quantization=args.quantization,
        representative_dataset=representative_dataset
    )
    
    logger.info("Conversion completed successfully!")


if __name__ == "__main__":
    main()

