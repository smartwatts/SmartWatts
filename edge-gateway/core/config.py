"""
SmartWatts Edge Gateway Configuration
Comprehensive configuration management for R501 RK3588 deployment
"""

import os
from pathlib import Path
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings
import yaml

class HardwareConfig(BaseModel):
    """Hardware-specific configuration for R501 RK3588."""
    device_type: str = "R501_RK3588"
    cpu_cores: int = 8
    memory_gb: int = 8
    storage_path: str = "/opt/smartwatts/data"
    max_concurrent_devices: int = 50
    ai_model_path: str = "/opt/smartwatts/models"
    enable_gpu_acceleration: bool = True

class NetworkConfig(BaseModel):
    """Network configuration for edge gateway."""
    host: str = "0.0.0.0"
    port: int = 8080
    mqtt_broker_host: str = "localhost"
    mqtt_broker_port: int = 1883
    mqtt_username: Optional[str] = None
    mqtt_password: Optional[str] = None
    cloud_api_url: str = "https://api.smartwatts.com"
    cloud_api_key: Optional[str] = None
    azure_iot_hub_connection_string: Optional[str] = None  # Azure IoT Hub connection string
    enable_ssl: bool = False
    ssl_cert_path: Optional[str] = None
    ssl_key_path: Optional[str] = None

class MQTTConfig(BaseModel):
    """MQTT broker and client configuration."""
    broker_host: str = "localhost"
    broker_port: int = 1883
    username: Optional[str] = None
    password: Optional[str] = None
    client_id: str = "smartwatts_edge_gateway"
    keepalive: int = 60
    qos: int = 1
    retain: bool = True
    topics: Dict[str, str] = Field(default_factory=lambda: {
        "energy_data": "smartwatts/energy/+/data",
        "device_status": "smartwatts/devices/+/status",
        "device_discovery": "smartwatts/discovery/+/announce",
        "alerts": "smartwatts/alerts/+/+",
        "commands": "smartwatts/commands/+/+"
    })

class ModbusConfig(BaseModel):
    """Modbus RTU/TCP configuration."""
    enabled: bool = True
    tcp_host: str = "192.168.1.100"
    tcp_port: int = 502
    rtu_port: str = "/dev/ttyUSB0"
    rtu_baudrate: int = 9600
    rtu_parity: str = "N"
    rtu_stopbits: int = 1
    rtu_bytesize: int = 8
    timeout: float = 3.0
    retries: int = 3
    devices: List[Dict[str, Any]] = Field(default_factory=lambda: [
        {
            "name": "Solar Inverter",
            "type": "inverter",
            "address": 1,
            "protocol": "tcp",
            "host": "192.168.1.101",
            "port": 502
        },
        {
            "name": "Energy Meter",
            "type": "meter",
            "address": 2,
            "protocol": "rtu",
            "port": "/dev/ttyUSB0",
            "baudrate": 9600
        }
    ])

class StorageConfig(BaseModel):
    """Local storage configuration."""
    database_url: str = "sqlite:///opt/smartwatts/data/edge.db"
    backup_enabled: bool = True
    backup_interval_hours: int = 24
    backup_retention_days: int = 30
    max_data_age_days: int = 90
    compression_enabled: bool = True

class AIConfig(BaseModel):
    """AI/ML inference configuration."""
    enabled: bool = True
    model_path: str = "/opt/smartwatts/models"
    models: Dict[str, str] = Field(default_factory=lambda: {
        "energy_forecast": "energy_forecast.tflite",
        "anomaly_detection": "anomaly_detection.tflite",
        "load_prediction": "load_prediction.tflite",
        "efficiency_optimization": "efficiency_optimization.tflite"
    })
    inference_interval_seconds: int = 60
    batch_size: int = 32
    confidence_threshold: float = 0.8
    enable_gpu: bool = True

class DeviceDiscoveryConfig(BaseModel):
    """Device discovery configuration."""
    enabled: bool = True
    scan_interval_seconds: int = 30
    discovery_timeout_seconds: int = 10
    protocols: List[str] = ["mqtt", "modbus", "http", "coap"]
    auto_register: bool = True
    device_types: List[str] = [
        "smart_plug", "inverter", "battery", "meter", 
        "sensor", "controller", "gateway"
    ]

class DataSyncConfig(BaseModel):
    """Cloud synchronization configuration."""
    enabled: bool = True
    sync_interval_seconds: int = 300
    batch_size: int = 1000
    retry_attempts: int = 3
    retry_delay_seconds: int = 60
    offline_mode: bool = True
    conflict_resolution: str = "edge_priority"  # edge_priority, cloud_priority, timestamp
    use_azure_iot_hub: bool = False  # Enable Azure IoT Hub MQTT publishing
    azure_iot_hub_device_id: Optional[str] = None  # Device ID for Azure IoT Hub

class SecurityConfig(BaseModel):
    """Security configuration."""
    enable_authentication: bool = True
    jwt_secret: str = "your-secret-key-change-in-production"
    jwt_expiry_hours: int = 24
    enable_encryption: bool = True
    encryption_key: str = "your-encryption-key-change-in-production"
    allowed_ips: List[str] = ["127.0.0.1", "192.168.1.0/24"]
    rate_limit_requests_per_minute: int = 100

class LoggingConfig(BaseModel):
    """Logging configuration."""
    level: str = "INFO"
    format: str = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"
    file_path: str = "/opt/smartwatts/logs/edge_gateway.log"
    max_file_size_mb: int = 100
    backup_count: int = 5
    enable_console: bool = True
    enable_file: bool = True

class EdgeConfig(BaseSettings):
    """Main SmartWatts Edge Gateway configuration."""
    
    # Core settings
    app_name: str = "SmartWatts Edge Gateway"
    version: str = "1.0.0"
    debug: bool = False
    
    # Configuration sections
    hardware: HardwareConfig = Field(default_factory=HardwareConfig)
    network: NetworkConfig = Field(default_factory=NetworkConfig)
    mqtt: MQTTConfig = Field(default_factory=MQTTConfig)
    modbus: ModbusConfig = Field(default_factory=ModbusConfig)
    storage: StorageConfig = Field(default_factory=StorageConfig)
    ai: AIConfig = Field(default_factory=AIConfig)
    device_discovery: DeviceDiscoveryConfig = Field(default_factory=DeviceDiscoveryConfig)
    data_sync: DataSyncConfig = Field(default_factory=DataSyncConfig)
    security: SecurityConfig = Field(default_factory=SecurityConfig)
    logging: LoggingConfig = Field(default_factory=LoggingConfig)
    
    class Config:
        env_file = ".env"
        env_nested_delimiter = "__"
        case_sensitive = False
    
    def __init__(self, config_path: str = None, **kwargs):
        super().__init__(**kwargs)
        
        if config_path and Path(config_path).exists():
            self._load_from_yaml(config_path)
    
    def _load_from_yaml(self, config_path: str):
        """Load configuration from YAML file."""
        try:
            with open(config_path, 'r') as f:
                config_data = yaml.safe_load(f)
            
            # Update configuration with YAML data
            for section, data in config_data.items():
                if hasattr(self, section) and isinstance(data, dict):
                    section_config = getattr(self, section)
                    for key, value in data.items():
                        if hasattr(section_config, key):
                            setattr(section_config, key, value)
        except Exception as e:
            print(f"Warning: Could not load config from {config_path}: {e}")
    
    def save_to_yaml(self, config_path: str):
        """Save current configuration to YAML file."""
        config_data = {}
        for section_name in [
            'hardware', 'network', 'mqtt', 'modbus', 'storage', 
            'ai', 'device_discovery', 'data_sync', 'security', 'logging'
        ]:
            section = getattr(self, section_name)
            config_data[section_name] = section.dict()
        
        with open(config_path, 'w') as f:
            yaml.dump(config_data, f, default_flow_style=False, indent=2)
    
    @property
    def log_level(self) -> str:
        """Get logging level."""
        return self.logging.level
    
    @property
    def is_production(self) -> bool:
        """Check if running in production mode."""
        return not self.debug
    
    def get_database_url(self) -> str:
        """Get database URL with proper path resolution."""
        if self.storage.database_url.startswith("sqlite:///"):
            db_path = self.storage.database_url.replace("sqlite:///", "")
            # Ensure directory exists
            Path(db_path).parent.mkdir(parents=True, exist_ok=True)
        return self.storage.database_url

# Global configuration instance
config: Optional[EdgeConfig] = None

def get_config() -> EdgeConfig:
    """Get global configuration instance."""
    global config
    if config is None:
        config = EdgeConfig()
    return config
