"""
SmartWatts Edge Test Configuration
"""

import os
from dataclasses import dataclass
from typing import Dict, Any, List
from pathlib import Path


@dataclass
class TestConfig:
    """Test configuration for SmartWatts Edge testing."""
    
    # Edge Device Configuration
    edge_device_ip: str = "192.168.1.100"
    edge_device_port: int = 8080
    edge_device_ssh_port: int = 22
    edge_device_username: str = "orangepi"
    edge_device_password: str = "orangepi"
    
    # Test Environment
    test_mode: bool = True
    offline_mode: bool = True
    headless: bool = True
    parallel_workers: int = 4
    
    # Browser Configuration
    browser_timeout: int = 30000  # 30 seconds
    page_load_timeout: int = 60000  # 60 seconds
    element_timeout: int = 10000  # 10 seconds
    
    # API Configuration
    api_base_url: str = "http://192.168.1.100:8080"
    api_timeout: int = 30
    max_retries: int = 3
    
    # Database Configuration
    test_db_host: str = "localhost"
    test_db_port: int = 5432
    test_db_name: str = "smartwatts_test"
    test_db_user: str = "postgres"
    test_db_password: str = "postgres"
    
    # Redis Configuration
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_db: int = 1
    
    # MQTT Configuration
    mqtt_broker: str = "192.168.1.100"
    mqtt_port: int = 1883
    mqtt_username: str = "smartwatts"
    mqtt_password: str = "smartwatts"
    
    # Modbus Configuration
    modbus_host: str = "192.168.1.100"
    modbus_port: int = 502
    
    # Load Testing Configuration
    load_test_duration: int = 300  # 5 minutes
    load_test_users: int = 100
    load_test_ramp_up: int = 60  # 1 minute
    
    # Reliability Testing Configuration
    soak_test_duration: int = 259200  # 72 hours
    stress_test_duration: int = 3600  # 1 hour
    memory_threshold: float = 80.0  # 80% memory usage
    cpu_threshold: float = 90.0  # 90% CPU usage
    
    # Test Data Configuration
    test_data_dir: Path = Path("/tmp/smartwatts_test_data")
    test_reports_dir: Path = Path("/tmp/smartwatts_test_reports")
    test_screenshots_dir: Path = Path("/tmp/smartwatts_test_screenshots")
    
    # Mock Services Configuration
    mock_cloud_url: str = "http://localhost:9999"
    mock_validation_delay: int = 2  # seconds
    mock_email_service: bool = True
    mock_sms_service: bool = True
    
    # Device Simulation Configuration
    simulated_devices: int = 10
    simulated_data_rate: int = 1000  # events per second
    simulated_power_outages: bool = True
    simulated_network_failures: bool = True
    
    # Test Execution Configuration
    retry_failed_tests: int = 2
    test_parallelism: int = 4
    test_timeout: int = 1800  # 30 minutes
    
    # Reporting Configuration
    generate_html_report: bool = True
    generate_allure_report: bool = True
    generate_junit_report: bool = True
    screenshot_on_failure: bool = True
    video_recording: bool = False
    
    def __post_init__(self):
        """Initialize configuration after dataclass creation."""
        # Create test directories
        self.test_data_dir.mkdir(parents=True, exist_ok=True)
        self.test_reports_dir.mkdir(parents=True, exist_ok=True)
        self.test_screenshots_dir.mkdir(parents=True, exist_ok=True)
        
        # Load environment variables
        self._load_from_env()
    
    def _load_from_env(self):
        """Load configuration from environment variables."""
        env_mappings = {
            "EDGE_DEVICE_IP": "edge_device_ip",
            "EDGE_DEVICE_PORT": "edge_device_port",
            "API_BASE_URL": "api_base_url",
            "TEST_MODE": "test_mode",
            "OFFLINE_MODE": "offline_mode",
            "HEADLESS": "headless",
            "PARALLEL_WORKERS": "parallel_workers",
            "LOAD_TEST_DURATION": "load_test_duration",
            "LOAD_TEST_USERS": "load_test_users",
        }
        
        for env_var, attr_name in env_mappings.items():
            if env_var in os.environ:
                value = os.environ[env_var]
                # Convert to appropriate type
                if attr_name in ["test_mode", "offline_mode", "headless"]:
                    value = value.lower() in ("true", "1", "yes")
                elif attr_name in ["edge_device_port", "edge_device_ssh_port", "api_timeout", "max_retries"]:
                    value = int(value)
                elif attr_name in ["memory_threshold", "cpu_threshold"]:
                    value = float(value)
                
                setattr(self, attr_name, value)
    
    def get_browser_args(self) -> List[str]:
        """Get browser arguments for Playwright."""
        return [
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--disable-web-security",
            "--allow-running-insecure-content",
            "--disable-extensions",
            "--disable-plugins",
            "--disable-images",
            "--disable-javascript",
        ]
    
    def get_test_markers(self) -> List[str]:
        """Get pytest markers for test filtering."""
        markers = []
        if self.offline_mode:
            markers.append("offline")
        if self.headless:
            markers.append("headless")
        return markers
    
    def get_environment_vars(self) -> Dict[str, str]:
        """Get environment variables for test execution."""
        return {
            "TEST_MODE": str(self.test_mode),
            "OFFLINE_MODE": str(self.offline_mode),
            "EDGE_DEVICE_IP": self.edge_device_ip,
            "API_BASE_URL": self.api_base_url,
            "TEST_DATA_DIR": str(self.test_data_dir),
            "TEST_REPORTS_DIR": str(self.test_reports_dir),
        }
