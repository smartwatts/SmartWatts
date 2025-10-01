"""
SmartWatts Edge QA Automation - Pytest Configuration
"""

import pytest
import asyncio
import os
import tempfile
import shutil
from pathlib import Path
from typing import Generator, Dict, Any
from unittest.mock import Mock, patch

import docker
import psutil
from playwright.async_api import async_playwright, Browser, BrowserContext, Page
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from tests.fixtures.mock_services import MockCloudService, MockDeviceSimulator
from tests.fixtures.edge_device import EdgeDeviceManager
from tests.utils.database import TestDatabaseManager
from tests.utils.config import TestConfig


@pytest.fixture(scope="session")
def event_loop():
    """Create an instance of the default event loop for the test session."""
    loop = asyncio.get_event_loop_policy().new_event_loop()
    yield loop
    loop.close()


@pytest.fixture(scope="session")
def test_config() -> TestConfig:
    """Load test configuration."""
    return TestConfig()


@pytest.fixture(scope="session")
def temp_dir() -> Generator[Path, None, None]:
    """Create temporary directory for test artifacts."""
    temp_path = Path(tempfile.mkdtemp(prefix="smartwatts_test_"))
    yield temp_path
    shutil.rmtree(temp_path, ignore_errors=True)


@pytest.fixture(scope="session")
def docker_client():
    """Docker client for container management."""
    return docker.from_env()


@pytest.fixture(scope="session")
async def browser() -> Generator[Browser, None, None]:
    """Playwright browser instance."""
    async with async_playwright() as p:
        browser = await p.chromium.launch(
            headless=os.getenv("HEADLESS", "true").lower() == "true",
            args=["--no-sandbox", "--disable-dev-shm-usage"]
        )
        yield browser
        await browser.close()


@pytest.fixture
async def browser_context(browser: Browser) -> Generator[BrowserContext, None, None]:
    """Browser context for isolated testing."""
    context = await browser.new_context(
        viewport={"width": 1920, "height": 1080},
        user_agent="SmartWatts-QA-Test/1.0"
    )
    yield context
    await context.close()


@pytest.fixture
async def page(browser_context: BrowserContext) -> Generator[Page, None, None]:
    """Browser page for UI testing."""
    page = await browser_context.new_page()
    yield page
    await page.close()


@pytest.fixture(scope="session")
def edge_device_manager(test_config: TestConfig) -> EdgeDeviceManager:
    """Edge device manager for hardware simulation."""
    return EdgeDeviceManager(test_config)


@pytest.fixture
def mock_cloud_service() -> MockCloudService:
    """Mock cloud service for offline validation simulation."""
    return MockCloudService()


@pytest.fixture
def device_simulator() -> MockDeviceSimulator:
    """Device simulator for smart plugs and energy sources."""
    return MockDeviceSimulator()


@pytest.fixture(scope="session")
def test_database() -> Generator[TestDatabaseManager, None, None]:
    """Test database manager."""
    db_manager = TestDatabaseManager()
    db_manager.setup()
    yield db_manager
    db_manager.cleanup()


@pytest.fixture
def mock_user_data():
    """Mock user data for testing."""
    return {
        "email": "test@mysmartwatts.com",
        "password": "TestPassword123!",
        "firstName": "Test",
        "lastName": "User",
        "phoneNumber": "+2341234567890",
        "role": "ROLE_USER"
    }


@pytest.fixture
def mock_admin_data():
    """Mock admin data for testing."""
    return {
        "email": "admin@mysmartwatts.com",
        "password": "AdminPassword123!",
        "firstName": "Admin",
        "lastName": "User",
        "phoneNumber": "+2341234567890",
        "role": "ROLE_ENTERPRISE_ADMIN"
    }


@pytest.fixture
def mock_energy_data():
    """Mock energy data for testing."""
    return {
        "grid_consumption": 1500.5,
        "solar_generation": 800.2,
        "battery_level": 75.0,
        "cost_per_kwh": 45.0,
        "timestamp": "2024-01-15T10:30:00Z"
    }


@pytest.fixture
def mock_device_data():
    """Mock device data for testing."""
    return {
        "device_id": "smart_plug_001",
        "device_type": "SMART_PLUG",
        "power_consumption": 120.5,
        "status": "ONLINE",
        "location": "Living Room",
        "protocol": "MQTT"
    }


@pytest.fixture(autouse=True)
def setup_test_environment(test_config: TestConfig, temp_dir: Path):
    """Setup test environment before each test."""
    # Set test environment variables
    os.environ["TEST_MODE"] = "true"
    os.environ["EDGE_MODE"] = "true"
    os.environ["OFFLINE_MODE"] = "true"
    os.environ["TEST_DATA_DIR"] = str(temp_dir)
    
    # Mock internet connectivity
    with patch('requests.get') as mock_get:
        mock_get.side_effect = Exception("No internet connection")
        yield


@pytest.fixture(autouse=True)
def cleanup_test_environment():
    """Cleanup after each test."""
    yield
    # Cleanup any test artifacts
    pass


# Pytest configuration
def pytest_configure(config):
    """Configure pytest with custom settings."""
    config.addinivalue_line(
        "markers", "slow: marks tests as slow (deselect with '-m \"not slow\"')"
    )
    config.addinivalue_line(
        "markers", "integration: marks tests as integration tests"
    )
    config.addinivalue_line(
        "markers", "ui: marks tests as UI tests"
    )
    config.addinivalue_line(
        "markers", "api: marks tests as API tests"
    )
    config.addinivalue_line(
        "markers", "load: marks tests as load tests"
    )
    config.addinivalue_line(
        "markers", "reliability: marks tests as reliability tests"
    )


def pytest_collection_modifyitems(config, items):
    """Modify test collection to add markers based on test names."""
    for item in items:
        if "test_load_" in item.name:
            item.add_marker(pytest.mark.load)
        if "test_reliability_" in item.name:
            item.add_marker(pytest.mark.reliability)
        if "test_integration_" in item.name:
            item.add_marker(pytest.mark.integration)
        if "test_ui_" in item.name:
            item.add_marker(pytest.mark.ui)
        if "test_api_" in item.name:
            item.add_marker(pytest.mark.api)
