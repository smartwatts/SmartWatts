# SmartWatts Edge QA Automation Framework

## Overview

This comprehensive automated testing framework is designed specifically for **Local (Edge-Only) Testing** of the SmartWatts Edge Device, focusing on new user onboarding, account creation, and local validation without cloud services or smart meters.

## 🎯 Testing Scope

### Functional Testing
- ✅ **Local Dashboard Validation** - Verify dashboard loads and functions correctly
- ✅ **New User Account Creation** - Test local admin account setup
- ✅ **Local Credential Storage** - Validate secure local storage
- ✅ **Password Reset (Local Admin Recovery)** - Test offline password recovery
- ✅ **Role-Based Access Control** - Verify user permissions
- ✅ **Device Discovery/Pairing** - Simulate smart plug discovery
- ✅ **Core Features** - Real-time monitoring, mocked NILM, cost estimation
- ✅ **Offline Report Generation** - Test report generation without internet

### Integration Testing
- ✅ **Multi-Source Data Ingestion** - Grid, inverter, solar, generator simulation
- ✅ **Data Normalization** - Verify data processing and standardization
- ✅ **Interoperability Testing** - Mock drivers and analytics integration
- ✅ **Dashboard Updates** - Confirm real-time updates within 1 second
- ✅ **Session Persistence** - Test across reboots and power failures
- ✅ **Multi-Device Edge Sync** - Validate device synchronization

### Reliability Testing
- ✅ **Stress Tests** - 1000+ events/second processing
- ✅ **Power Outage Simulation** - Test restart and account persistence
- ✅ **Failover Testing** - System recovery validation
- ✅ **72-Hour Soak Test** - Memory leak and CPU throttling detection
- ✅ **30-Day Local Data Storage** - Long-term data retention testing

### User Onboarding & Validation Simulation
- ✅ **Mock Cloud Validation** - Email/SMS simulation using local fake service
- ✅ **Offline Account Setup** - Complete local-first account creation
- ✅ **Validation Event Queuing** - Queue pending validation events
- ✅ **Cloud Migration** - Automatic migration of local-first accounts to cloud auth

## 🏗️ Framework Architecture

### Core Components

#### 1. **Edge Device Manager** (`tests/fixtures/edge_device.py`)
- Hardware simulation for Orange Pi 5 Plus
- Device connection and disconnection
- Command execution and system status monitoring
- Firmware update simulation
- Log retrieval and device restart

#### 2. **Mock Services** (`tests/fixtures/mock_services.py`)
- **MockCloudService**: Complete cloud API simulation
- **MockDeviceSimulator**: Smart plug and energy source simulation
- **MockServices**: Main coordinator for all mock services
- MQTT and Modbus data generation
- User registration and validation simulation

#### 3. **Database Manager** (`tests/utils/database.py`)
- Test database setup and teardown
- Data seeding and cleanup
- Connection management
- Schema validation

#### 4. **Configuration Management** (`tests/utils/config.py`)
- Environment-specific settings
- Edge device configuration
- Mock service endpoints
- Test data parameters

### Test Categories

#### **Functional Tests** (`tests/functional/`)
- **User Onboarding** (`test_user_onboarding.py`)
  - First-time setup wizard
  - Account creation and validation
  - Local credential storage
  - Offline login functionality
  - Password reset in offline mode
  - Role-based access control
  - Dashboard functionality
  - Mobile/tablet responsive design
  - Offline validation simulation
  - User session persistence

- **Device Discovery** (`test_device_discovery.py`)
  - Device discovery page loading
  - Smart plug discovery simulation
  - Device pairing process
  - Device status monitoring
  - Device configuration
  - Device removal
  - Multiple protocol support
  - Discovery timeout handling
  - Offline device handling

#### **Integration Tests** (`tests/integration/`)
- **Data Ingestion** (`test_data_ingestion.py`)
  - MQTT data ingestion
  - Modbus data ingestion
  - Multi-source data normalization
  - Real-time dashboard updates
  - High-frequency data processing
  - Network failure handling
  - Data validation and cleaning
  - Data aggregation and summarization

#### **Load Tests** (`tests/load/`)
- **Load Scenarios** (`test_load_scenarios.py`)
  - Basic load (50 users)
  - Stress load (200 users)
  - Spike load (sudden traffic)
  - 72-hour soak test
  - Concurrent API requests

#### **Reliability Tests** (`tests/reliability/`)
- **Stress and Soak** (`test_stress_and_soak.py`)
  - High data ingestion stress
  - Power outage simulation
  - Memory leak detection
  - CPU throttling under load
  - Concurrent user sessions
  - Database corruption recovery
  - 30-day data retention

## 🚀 Quick Start

### Prerequisites
- Python 3.8+
- pip
- Virtual environment

### Installation

1. **Clone and Setup**
```bash
cd qa-automation
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

2. **Run Demo**
```bash
python run_tests.py
```

3. **Run All Tests**
```bash
pytest tests/ -v
```

4. **Run Specific Test Category**
```bash
# Functional tests
pytest tests/functional/ -v

# Integration tests
pytest tests/integration/ -v

# Load tests
pytest tests/load/ -v

# Reliability tests
pytest tests/reliability/ -v
```

## 📊 Test Execution

### Test Markers
- `@pytest.mark.offline` - Tests that can run offline
- `@pytest.mark.functional` - Functional tests
- `@pytest.mark.integration` - Integration tests
- `@pytest.mark.load` - Load tests
- `@pytest.mark.reliability` - Reliability tests

### Running Specific Tests
```bash
# Run only offline tests
pytest -m offline -v

# Run functional tests with offline capability
pytest -m "functional and offline" -v

# Run load tests
pytest -m load -v
```

### Test Reports
- **HTML Report**: `reports/pytest-report.html`
- **JUnit XML**: `reports/junit.xml`
- **Allure Results**: `reports/allure-results/`
- **Coverage Report**: `reports/htmlcov/`

## 🔧 Configuration

### Environment Variables
```bash
# Edge Device Configuration
EDGE_DEVICE_IP=192.168.1.100
EDGE_DEVICE_PORT=8080
EDGE_DEVICE_TYPE=ORANGE_PI_5_PLUS

# Mock Services
MOCK_CLOUD_HOST=localhost
MOCK_CLOUD_PORT=9999

# Database
TEST_DB_HOST=localhost
TEST_DB_PORT=5432
TEST_DB_NAME=smartwatts_test
TEST_DB_USER=test_user
TEST_DB_PASSWORD=test_password
```

### Test Configuration
Edit `tests/utils/config.py` to customize:
- Edge device settings
- Mock service endpoints
- Database connections
- Test data parameters
- Timeout values

## 📈 Test Data Generation

### MQTT Data Simulation
```python
from tests.fixtures.mock_services import MockServices

mock_services = MockServices(config)
mqtt_data = mock_services.simulate_mqtt_data(count=100)
```

### Modbus Data Simulation
```python
modbus_data = mock_services.simulate_modbus_data(count=50)
```

### Device Simulation
```python
# Create smart plug
device = mock_services.device_simulator.create_device(
    device_id="smart_plug_001",
    device_type="SMART_PLUG",
    location="Living Room"
)

# Start simulation
mock_services.device_simulator.start_device("smart_plug_001")
```

## 🎭 Mock Services

### Cloud Service Simulation
- User registration and login
- Email/SMS verification
- Profile management
- Validation status tracking
- Offline queue management

### Device Simulator
- Smart plug simulation
- Inverter simulation
- Solar generation simulation
- Power outage simulation
- Real-time data generation

## 📋 Test Case Examples

### Gherkin Format
```gherkin
Feature: User Onboarding
  Scenario: First-time setup wizard loads
    Given the edge device is running
    When I navigate to the setup wizard
    Then I should see the welcome screen
    And I should be able to create a new account
    And I should be able to configure device settings
```

### Python Test Example
```python
@pytest.mark.functional
@pytest.mark.offline
async def test_first_time_setup_wizard_loads(browser, page):
    """Test that the first-time setup wizard loads correctly."""
    # Navigate to setup wizard
    await page.goto("http://localhost:3000/setup")
    
    # Verify welcome screen
    welcome_text = await page.text_content("h1")
    assert "Welcome to SmartWatts" in welcome_text
    
    # Verify account creation form
    email_input = await page.query_selector("input[type='email']")
    assert email_input is not None
```

## 🔍 Debugging and Troubleshooting

### Common Issues

1. **Async Test Failures**
   - Ensure `pytest-asyncio` is installed
   - Check `pytest.ini` configuration
   - Verify async/await syntax

2. **Device Connection Issues**
   - Check edge device IP and port
   - Verify network connectivity
   - Review device logs

3. **Mock Service Failures**
   - Ensure ports are available
   - Check service startup logs
   - Verify configuration settings

### Debug Mode
```bash
# Run with debug output
pytest tests/ -v -s --tb=long

# Run specific test with debug
pytest tests/functional/test_user_onboarding.py::TestUserOnboarding::test_first_time_setup_wizard_loads -v -s
```

## 📚 API Reference

### Edge Device Manager
```python
class EdgeDeviceManager:
    def connect(self) -> bool
    def disconnect(self) -> bool
    def get_device_info(self) -> Dict[str, Any]
    def execute_command(self, command: str) -> Dict[str, Any]
    def get_system_status(self) -> Dict[str, Any]
    def restart_device(self) -> bool
    def update_firmware(self, firmware_path: str) -> bool
    def get_logs(self, lines: int = 100) -> str
```

### Mock Services
```python
class MockServices:
    def simulate_mqtt_data(self, count: int = 10) -> List[Dict[str, Any]]
    def simulate_modbus_data(self, count: int = 10) -> List[Dict[str, Any]]
    async def start_all_services(self)
    async def stop_all_services(self)
    def clear_all_data(self)
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is part of the SmartWatts Edge QA Automation Framework.

## 🆘 Support

For issues and questions:
1. Check the troubleshooting section
2. Review test logs
3. Create an issue with detailed information

---

**Ready for comprehensive edge device testing!** 🚀