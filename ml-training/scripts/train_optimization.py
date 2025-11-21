#!/usr/bin/env python3
"""
Train Optimization Model
Predicts optimization recommendations and potential savings
"""
import argparse
import sys
from pathlib import Path
import logging
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers, models, callbacks
import json
from datetime import datetime

# Add parent directory to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from utils.data_loader import EnergyDataLoader

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def build_optimization_model(input_shape: tuple) -> keras.Model:
    """Build optimization recommendation model"""
    model = models.Sequential([
        layers.Dense(128, activation='relu', input_shape=input_shape),
        layers.Dropout(0.3),
        layers.Dense(64, activation='relu'),
        layers.Dropout(0.3),
        layers.Dense(32, activation='relu'),
        layers.Dropout(0.2),
        layers.Dense(16, activation='relu'),
        layers.Dense(1, activation='sigmoid')  # Optimization score (0-1)
    ])
    
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=0.001),
        loss='mse',
        metrics=['mae']
    )
    
    return model


def train_model(args):
    """Main training function"""
    logger.info("Starting optimization model training...")
    
    # Setup paths
    data_dir = Path(args.data_dir)
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Load and prepare data
    logger.info("Loading and preprocessing data...")
    data_loader = EnergyDataLoader(data_dir, sequence_length=args.sequence_length)
    
    # Find data files
    data_files = list(data_dir.glob("*.csv"))
    if not data_files:
        logger.error(f"No CSV files found in {data_dir}")
        return
    
    # Load and combine data
    import pandas as pd
    dfs = []
    for file in data_files:
        df = data_loader.load_energy_data(str(file))
        dfs.append(df)
    
    combined_df = pd.concat(dfs, ignore_index=True)
    combined_df = combined_df.sort_values('timestamp')
    
    # Prepare data
    X, y = data_loader.prepare_optimization_data(combined_df)
    X_train, X_val, X_test, y_train, y_val, y_test = data_loader.split_data(X, y)
    
    logger.info(f"Training samples: {len(X_train)}")
    logger.info(f"Validation samples: {len(X_val)}")
    logger.info(f"Test samples: {len(X_test)}")
    
    # Build model
    logger.info("Building model...")
    model = build_optimization_model(input_shape=(X_train.shape[1],))
    
    # Callbacks
    checkpoint_cb = callbacks.ModelCheckpoint(
        str(output_dir / "optimization_best.h5"),
        save_best_only=True,
        monitor='val_loss',
        mode='min'
    )
    
    early_stopping_cb = callbacks.EarlyStopping(
        monitor='val_loss',
        patience=20,
        restore_best_weights=True
    )
    
    # Train model
    logger.info("Training model...")
    history = model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=args.epochs,
        batch_size=args.batch_size,
        callbacks=[checkpoint_cb, early_stopping_cb],
        verbose=1
    )
    
    # Evaluate
    logger.info("Evaluating on test set...")
    test_loss, test_mae = model.evaluate(X_test, y_test, verbose=0)
    logger.info(f"Test Loss: {test_loss:.4f}, Test MAE: {test_mae:.4f}")
    
    # Save model
    model_path = output_dir / "optimization_model.h5"
    model.save(str(model_path))
    logger.info(f"Model saved to {model_path}")
    
    # Save metadata
    metadata = {
        "model_name": "optimization",
        "version": "1.0.0",
        "trained_at": datetime.now().isoformat(),
        "epochs": len(history.history['loss']),
        "test_loss": float(test_loss),
        "test_mae": float(test_mae),
        "input_shape": list(X_train.shape[1:])
    }
    
    metadata_path = output_dir / "optimization_metadata.json"
    with open(metadata_path, 'w') as f:
        json.dump(metadata, f, indent=2)
    
    logger.info("Training completed successfully!")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train Optimization Model")
    parser.add_argument("--data-dir", type=str, required=True, help="Directory containing training data")
    parser.add_argument("--output-dir", type=str, default="models", help="Output directory for models")
    parser.add_argument("--epochs", type=int, default=100, help="Number of training epochs")
    parser.add_argument("--batch-size", type=int, default=32, help="Batch size")
    parser.add_argument("--sequence-length", type=int, default=24, help="Input sequence length")
    
    args = parser.parse_args()
    train_model(args)

