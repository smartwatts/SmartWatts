"""
Data loading and preprocessing utilities for SmartWatts ML training
"""
import pandas as pd
import numpy as np
from pathlib import Path
from typing import Tuple, List, Dict, Optional
from sklearn.preprocessing import StandardScaler, MinMaxScaler
import logging

logger = logging.getLogger(__name__)


class EnergyDataLoader:
    """Load and preprocess energy consumption data for ML training"""
    
    def __init__(self, data_dir: str, sequence_length: int = 24):
        self.data_dir = Path(data_dir)
        self.sequence_length = sequence_length
        self.scaler = StandardScaler()
        self.feature_scaler = MinMaxScaler()
        
    def load_energy_data(self, file_path: str) -> pd.DataFrame:
        """Load energy consumption data from CSV"""
        df = pd.read_csv(file_path)
        df['timestamp'] = pd.to_datetime(df['timestamp'])
        df = df.sort_values('timestamp')
        return df
    
    def create_sequences(self, data: np.ndarray, target_length: int = 1) -> Tuple[np.ndarray, np.ndarray]:
        """Create sequences for time series prediction"""
        X, y = [], []
        for i in range(len(data) - self.sequence_length - target_length + 1):
            X.append(data[i:i + self.sequence_length])
            y.append(data[i + self.sequence_length:i + self.sequence_length + target_length])
        return np.array(X), np.array(y)
    
    def prepare_forecasting_data(self, df: pd.DataFrame, 
                                 target_col: str = 'consumption',
                                 feature_cols: Optional[List[str]] = None) -> Tuple[np.ndarray, np.ndarray]:
        """Prepare data for energy forecasting"""
        # Extract target variable
        target = df[target_col].values.reshape(-1, 1)
        target_scaled = self.scaler.fit_transform(target)
        
        # Extract features
        if feature_cols:
            features = df[feature_cols].values
            features_scaled = self.feature_scaler.fit_transform(features)
        else:
            # Create time-based features
            df['hour'] = df['timestamp'].dt.hour
            df['day_of_week'] = df['timestamp'].dt.dayofweek
            df['month'] = df['timestamp'].dt.month
            features = df[['hour', 'day_of_week', 'month']].values
            features_scaled = self.feature_scaler.fit_transform(features)
        
        # Combine features with target for input
        combined = np.hstack([target_scaled, features_scaled])
        
        # Create sequences
        X, y = self.create_sequences(combined[:, 0], target_length=24)  # 24-hour forecast
        
        return X, y
    
    def prepare_anomaly_data(self, df: pd.DataFrame, 
                            target_col: str = 'consumption') -> Tuple[np.ndarray, np.ndarray]:
        """Prepare data for anomaly detection"""
        # Extract consumption data
        consumption = df[target_col].values.reshape(-1, 1)
        consumption_scaled = self.scaler.fit_transform(consumption)
        
        # Create sequences
        X, _ = self.create_sequences(consumption_scaled.flatten(), target_length=1)
        
        # For anomaly detection, we use the same data as input and target
        # (autoencoder approach) or create labels based on statistical outliers
        y = self._create_anomaly_labels(consumption)
        
        return X, y
    
    def _create_anomaly_labels(self, data: np.ndarray, threshold: float = 2.0) -> np.ndarray:
        """Create anomaly labels based on statistical outliers"""
        mean = np.mean(data)
        std = np.std(data)
        labels = (np.abs(data - mean) > threshold * std).astype(int)
        return labels
    
    def prepare_optimization_data(self, df: pd.DataFrame) -> Tuple[np.ndarray, np.ndarray]:
        """Prepare data for optimization recommendations"""
        # Extract features: consumption, time, cost, etc.
        features = []
        if 'consumption' in df.columns:
            features.append(df['consumption'].values)
        if 'cost' in df.columns:
            features.append(df['cost'].values)
        
        # Add time features
        df['hour'] = df['timestamp'].dt.hour
        df['day_of_week'] = df['timestamp'].dt.dayofweek
        features.append(df['hour'].values)
        features.append(df['day_of_week'].values)
        
        X = np.column_stack(features)
        X_scaled = self.feature_scaler.fit_transform(X)
        
        # Create optimization targets (savings potential)
        # This is a simplified approach - in production, this would be more sophisticated
        y = self._calculate_optimization_targets(df)
        
        return X_scaled, y
    
    def _calculate_optimization_targets(self, df: pd.DataFrame) -> np.ndarray:
        """Calculate optimization targets (savings potential)"""
        # Simplified: calculate potential savings based on peak usage
        if 'consumption' in df.columns:
            avg_consumption = df['consumption'].mean()
            savings_potential = np.maximum(0, (df['consumption'].values - avg_consumption) / avg_consumption)
            return savings_potential
        return np.zeros(len(df))
    
    def split_data(self, X: np.ndarray, y: np.ndarray, 
                   train_ratio: float = 0.7, 
                   val_ratio: float = 0.15) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
        """Split data into train, validation, and test sets"""
        n = len(X)
        train_end = int(n * train_ratio)
        val_end = int(n * (train_ratio + val_ratio))
        
        X_train, y_train = X[:train_end], y[:train_end]
        X_val, y_val = X[train_end:val_end], y[train_end:val_end]
        X_test, y_test = X[val_end:], y[val_end:]
        
        return X_train, X_val, X_test, y_train, y_val, y_test

