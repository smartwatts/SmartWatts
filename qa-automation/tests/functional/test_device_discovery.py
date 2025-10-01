"""
SmartWatts Edge Functional Tests - Device Discovery and Pairing
"""

import pytest
import asyncio
from playwright.async_api import expect
from tests.utils.config import TestConfig
from tests.fixtures.mock_services import MockDeviceSimulator


class TestDeviceDiscovery:
    """Test device discovery and pairing functionality."""
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_device_discovery_page_loads(self, page, test_config, mock_user_data, test_database):
        """Test that device discovery page loads correctly."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Verify device discovery elements
        await expect(page.locator("text=Device Management")).to_be_visible()
        await expect(page.locator("text=Discover Devices")).to_be_visible()
        await expect(page.locator("button:has-text('Scan for Devices')")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_simulated_smart_plug_discovery(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test discovery of simulated smart plugs."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Create simulated devices
        device_simulator.create_device("smart_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.create_device("smart_plug_002", "SMART_PLUG", "Kitchen")
        device_simulator.create_device("inverter_001", "INVERTER", "Garage")
        device_simulator.start_device("smart_plug_001")
        device_simulator.start_device("smart_plug_002")
        device_simulator.start_device("inverter_001")
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Start device discovery
        await page.click("button:has-text('Scan for Devices')")
        
        # Wait for devices to be discovered
        await expect(page.locator("text=smart_plug_001")).to_be_visible(timeout=10000)
        await expect(page.locator("text=smart_plug_002")).to_be_visible()
        await expect(page.locator("text=inverter_001")).to_be_visible()
        
        # Verify device information
        await expect(page.locator("text=Living Room")).to_be_visible()
        await expect(page.locator("text=Kitchen")).to_be_visible()
        await expect(page.locator("text=Garage")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_device_pairing_process(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test device pairing process."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Create simulated device
        device_simulator.create_device("smart_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.start_device("smart_plug_001")
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Start device discovery
        await page.click("button:has-text('Scan for Devices')")
        
        # Wait for device to be discovered
        await expect(page.locator("text=smart_plug_001")).to_be_visible(timeout=10000)
        
        # Click on device to pair
        await page.click("button:has-text('Pair Device')")
        
        # Verify pairing dialog
        await expect(page.locator("text=Pair Device")).to_be_visible()
        await expect(page.locator("text=smart_plug_001")).to_be_visible()
        
        # Enter device name
        await page.fill("input[name='deviceName']", "Living Room TV")
        
        # Confirm pairing
        await page.click("button:has-text('Confirm Pairing')")
        
        # Verify device is paired
        await expect(page.locator("text=Device paired successfully")).to_be_visible()
        await expect(page.locator("text=Living Room TV")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_device_status_monitoring(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test device status monitoring."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Create and pair device
        device_simulator.create_device("smart_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.start_device("smart_plug_001")
        
        # Add device to database
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'smart_plug_001', 'SMART_PLUG', 'Living Room TV', 'Living Room', 'MQTT', 'ONLINE', 120.5)
        """)
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Verify device status
        await expect(page.locator("text=Living Room TV")).to_be_visible()
        await expect(page.locator("text=ONLINE")).to_be_visible()
        await expect(page.locator("text=120.5 W")).to_be_visible()
        
        # Simulate device going offline
        device_simulator.stop_device("smart_plug_001")
        
        # Refresh page to see status change
        await page.reload()
        
        # Verify device is now offline
        await expect(page.locator("text=OFFLINE")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_device_configuration(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test device configuration options."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Create and pair device
        device_simulator.create_device("smart_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.start_device("smart_plug_001")
        
        # Add device to database
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'smart_plug_001', 'SMART_PLUG', 'Living Room TV', 'Living Room', 'MQTT', 'ONLINE', 120.5)
        """)
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Click on device settings
        await page.click("button[aria-label='Device Settings']")
        
        # Verify configuration options
        await expect(page.locator("text=Device Configuration")).to_be_visible()
        await expect(page.locator("input[name='deviceName']")).to_be_visible()
        await expect(page.locator("input[name='location']")).to_be_visible()
        await expect(page.locator("select[name='protocol']")).to_be_visible()
        
        # Update device name
        await page.fill("input[name='deviceName']", "Updated TV Name")
        await page.fill("input[name='location']", "Updated Living Room")
        
        # Save configuration
        await page.click("button:has-text('Save Configuration')")
        
        # Verify configuration saved
        await expect(page.locator("text=Configuration saved successfully")).to_be_visible()
        await expect(page.locator("text=Updated TV Name")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_device_removal(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test device removal process."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Create and pair device
        device_simulator.create_device("smart_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.start_device("smart_plug_001")
        
        # Add device to database
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'smart_plug_001', 'SMART_PLUG', 'Living Room TV', 'Living Room', 'MQTT', 'ONLINE', 120.5)
        """)
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Click on device remove button
        await page.click("button[aria-label='Remove Device']")
        
        # Confirm removal
        await expect(page.locator("text=Remove Device")).to_be_visible()
        await expect(page.locator("text=Are you sure you want to remove this device?")).to_be_visible()
        
        await page.click("button:has-text('Confirm Removal')")
        
        # Verify device is removed
        await expect(page.locator("text=Device removed successfully")).to_be_visible()
        await expect(page.locator("text=Living Room TV")).not_to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_multiple_protocol_support(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test support for multiple device protocols."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Create devices with different protocols
        device_simulator.create_device("mqtt_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.create_device("modbus_inverter_001", "INVERTER", "Garage")
        device_simulator.create_device("wifi_meter_001", "SMART_METER", "Utility Room")
        
        device_simulator.start_device("mqtt_plug_001")
        device_simulator.start_device("modbus_inverter_001")
        device_simulator.start_device("wifi_meter_001")
        
        # Add devices to database with different protocols
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES 
                ('device-001', 'mqtt_plug_001', 'SMART_PLUG', 'MQTT Plug', 'Living Room', 'MQTT', 'ONLINE', 120.5),
                ('device-002', 'modbus_inverter_001', 'INVERTER', 'Modbus Inverter', 'Garage', 'MODBUS', 'ONLINE', 0.0),
                ('device-003', 'wifi_meter_001', 'SMART_METER', 'WiFi Meter', 'Utility Room', 'WIFI', 'ONLINE', 0.0)
        """)
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Verify all devices are visible
        await expect(page.locator("text=MQTT Plug")).to_be_visible()
        await expect(page.locator("text=Modbus Inverter")).to_be_visible()
        await expect(page.locator("text=WiFi Meter")).to_be_visible()
        
        # Verify protocol indicators
        await expect(page.locator("text=MQTT")).to_be_visible()
        await expect(page.locator("text=MODBUS")).to_be_visible()
        await expect(page.locator("text=WIFI")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_device_discovery_timeout(self, page, test_config, mock_user_data, test_database):
        """Test device discovery timeout handling."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Start device discovery
        await page.click("button:has-text('Scan for Devices')")
        
        # Wait for discovery to complete (no devices found)
        await expect(page.locator("text=No devices found")).to_be_visible(timeout=15000)
        await expect(page.locator("text=Discovery completed")).to_be_visible()
    
    @pytest.mark.ui
    @pytest.mark.offline
    async def test_device_discovery_with_offline_devices(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test device discovery when some devices are offline."""
        # Create user and login
        user_id = test_database.insert_user({
            "username": mock_user_data["email"].split("@")[0],
            "email": mock_user_data["email"],
            "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "first_name": mock_user_data["firstName"],
            "last_name": mock_user_data["lastName"],
            "phone_number": mock_user_data["phoneNumber"],
            "role": mock_user_data["role"],
            "is_active": True
        })
        
        # Create devices - one online, one offline
        device_simulator.create_device("online_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.create_device("offline_plug_001", "SMART_PLUG", "Kitchen")
        
        device_simulator.start_device("online_plug_001")
        # offline_plug_001 remains offline
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to devices page
        await page.goto(f"{test_config.api_base_url}/devices")
        
        # Start device discovery
        await page.click("button:has-text('Scan for Devices')")
        
        # Wait for online device to be discovered
        await expect(page.locator("text=online_plug_001")).to_be_visible(timeout=10000)
        
        # Verify offline device is not shown
        await expect(page.locator("text=offline_plug_001")).not_to_be_visible()
        
        # Verify discovery status
        await expect(page.locator("text=1 device found")).to_be_visible()
