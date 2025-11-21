"""
Generate sample training data for ML models
Useful for testing the training pipeline
"""
import pandas as pd
import numpy as np
from pathlib import Path
from datetime import datetime, timedelta
import argparse


def generate_energy_data(output_path: str, days: int = 365, 
                        devices: int = 5, noise_level: float = 0.1):
    """Generate synthetic energy consumption data"""
    start_date = datetime.now() - timedelta(days=days)
    timestamps = pd.date_range(start=start_date, periods=days * 24, freq='H')
    
    all_data = []
    
    for device_id in range(1, devices + 1):
        # Generate base consumption pattern (daily cycle)
        hours = np.arange(len(timestamps)) % 24
        base_consumption = 50 + 30 * np.sin(2 * np.pi * hours / 24 - np.pi/2)
        
        # Add weekly pattern
        day_of_week = np.array([ts.weekday() for ts in timestamps])
        weekly_factor = 1.0 + 0.2 * (day_of_week < 5)  # Higher on weekdays
        
        # Add seasonal pattern
        months = np.array([ts.month for ts in timestamps])
        seasonal_factor = 1.0 + 0.3 * np.sin(2 * np.pi * (months - 1) / 12)
        
        # Add noise
        noise = np.random.normal(0, noise_level * 50, len(timestamps))
        
        # Calculate consumption
        consumption = base_consumption * weekly_factor * seasonal_factor + noise
        consumption = np.maximum(consumption, 0)  # No negative consumption
        
        # Calculate cost (simplified)
        cost = consumption * 0.20  # 20 Naira per kWh
        
        device_data = pd.DataFrame({
            'timestamp': timestamps,
            'device_id': f'device_{device_id}',
            'consumption': consumption,
            'cost': cost,
            'voltage': 220 + np.random.normal(0, 5, len(timestamps)),
            'current': consumption / 220 + np.random.normal(0, 0.1, len(timestamps)),
            'power_factor': 0.9 + np.random.normal(0, 0.05, len(timestamps))
        })
        
        all_data.append(device_data)
    
    combined_df = pd.concat(all_data, ignore_index=True)
    combined_df = combined_df.sort_values(['device_id', 'timestamp'])
    
    output_path = Path(output_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    combined_df.to_csv(output_path, index=False)
    
    print(f"Generated {len(combined_df)} records for {devices} devices")
    print(f"Data saved to {output_path}")
    
    return combined_df


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate sample energy data")
    parser.add_argument("--output", type=str, default="data/raw/sample_energy_data.csv",
                       help="Output CSV file path")
    parser.add_argument("--days", type=int, default=365, help="Number of days of data")
    parser.add_argument("--devices", type=int, default=5, help="Number of devices")
    parser.add_argument("--noise", type=float, default=0.1, help="Noise level (0-1)")
    
    args = parser.parse_args()
    generate_energy_data(args.output, args.days, args.devices, args.noise)

