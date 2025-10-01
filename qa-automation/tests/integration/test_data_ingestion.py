"""
SmartWatts Edge Integration Tests - Data Ingestion
"""

import pytest
import asyncio
import json
import time
from datetime import datetime, timedelta
from typing import Dict, Any, List

from tests.utils.config import TestConfig
from tests.fixtures.mock_services import MockDeviceSimulator
from tests.utils.database import TestDatabaseManager


class TestDataIngestion:
    """Test data ingestion from multiple sources."""
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_mqtt_data_ingestion(self, device_simulator, test_database):
        """Test MQTT data ingestion from smart plugs."""
        # Create MQTT device
        device_simulator.create_device("mqtt_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.start_device("mqtt_plug_001")
        
        # Add device to database
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'mqtt_plug_001', 'SMART_PLUG', 'MQTT Plug', 'Living Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Simulate MQTT data ingestion
        for i in range(10):
            device_simulator.simulate_power_consumption("mqtt_plug_001", 100 + i * 10)
            device_data = device_simulator.get_device_data("mqtt_plug_001")
            
            # Simulate MQTT message processing
            reading_data = {
                "device_id": "mqtt_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
            await asyncio.sleep(0.1)  # Simulate real-time data
        
        # Verify data was ingested
        readings = test_database.get_energy_readings_by_device("mqtt_plug_001")
        assert len(readings) == 10
        
        # Verify data values are increasing (simulated load increase)
        values = [float(r["value"]) for r in readings]
        assert all(values[i] <= values[i+1] for i in range(len(values)-1))
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_modbus_data_ingestion(self, device_simulator, test_database):
        """Test Modbus data ingestion from inverter."""
        # Create Modbus device
        device_simulator.create_device("modbus_inverter_001", "INVERTER", "Garage")
        device_simulator.start_device("modbus_inverter_001")
        
        # Add device to database
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-002', 'modbus_inverter_001', 'INVERTER', 'Modbus Inverter', 'Garage', 'MODBUS', 'ONLINE', 0.0)
        """)
        
        # Simulate Modbus data ingestion
        for i in range(5):
            # Simulate solar generation (negative power consumption)
            device_simulator.simulate_solar_generation("modbus_inverter_001", i / 5.0)
            device_data = device_simulator.get_device_data("modbus_inverter_001")
            
            # Simulate Modbus register reading
            reading_data = {
                "device_id": "modbus_inverter_001",
                "reading_type": "SOLAR_GENERATION",
                "value": abs(device_data["power_consumption"]),  # Convert negative to positive
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
            await asyncio.sleep(0.2)  # Simulate slower Modbus polling
        
        # Verify data was ingested
        readings = test_database.get_energy_readings_by_device("modbus_inverter_001")
        assert len(readings) == 5
        
        # Verify solar generation values
        values = [float(r["value"]) for r in readings]
        assert all(v >= 0 for v in values)  # Solar generation should be positive
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_multi_source_data_normalization(self, device_simulator, test_database):
        """Test data normalization across different protocols."""
        # Create devices with different protocols
        devices = [
            ("mqtt_plug_001", "SMART_PLUG", "MQTT"),
            ("modbus_inverter_001", "INVERTER", "MODBUS"),
            ("wifi_meter_001", "SMART_METER", "WIFI")
        ]
        
        for device_id, device_type, protocol in devices:
            device_simulator.create_device(device_id, device_type, "Test Location")
            device_simulator.start_device(device_id)
            
            test_database.execute_query("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, {
                "id": f"device-{device_id}",
                "device_id": device_id,
                "device_type": device_type,
                "device_name": f"{protocol} Device",
                "location": "Test Location",
                "protocol": protocol,
                "status": "ONLINE",
                "power_consumption": 0.0
            })
        
        # Simulate data from all sources
        for i in range(3):
            for device_id, device_type, protocol in devices:
                if device_type == "INVERTER":
                    device_simulator.simulate_solar_generation(device_id, i / 3.0)
                else:
                    device_simulator.simulate_power_consumption(device_id, 100 + i * 50)
                
                device_data = device_simulator.get_device_data(device_id)
                
                # Normalize data format
                reading_data = {
                    "device_id": device_id,
                    "reading_type": "SOLAR_GENERATION" if device_type == "INVERTER" else "POWER_CONSUMPTION",
                    "value": abs(device_data["power_consumption"]),
                    "unit": "W",
                    "timestamp": datetime.now().isoformat(),
                    "protocol": protocol
                }
                
                test_database.insert_energy_reading(reading_data)
            
            await asyncio.sleep(0.1)
        
        # Verify all data was normalized and stored
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) == 9  # 3 devices × 3 readings
        
        # Verify data format consistency
        for reading in all_readings:
            assert "device_id" in reading
            assert "reading_type" in reading
            assert "value" in reading
            assert "unit" in reading
            assert "timestamp" in reading
            assert reading["unit"] == "W"
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_real_time_dashboard_updates(self, page, test_config, device_simulator, mock_user_data, test_database):
        """Test dashboard updates within 1 second of data injection."""
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
        
        # Create device
        device_simulator.create_device("real_time_plug_001", "SMART_PLUG", "Living Room")
        device_simulator.start_device("real_time_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'real_time_plug_001', 'SMART_PLUG', 'Real-time Plug', 'Living Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Login
        await page.goto(f"{test_config.api_base_url}/login")
        await page.fill("input[name='email']", mock_user_data["email"])
        await page.fill("input[name='password']", "password")
        await page.click("button[type='submit']")
        
        # Navigate to dashboard
        await page.goto(f"{test_config.api_base_url}/dashboard")
        
        # Get initial power value
        initial_power_element = page.locator("text=0 W")
        await expect(initial_power_element).to_be_visible()
        
        # Simulate power change
        start_time = time.time()
        device_simulator.simulate_power_consumption("real_time_plug_001", 150.0)
        
        # Insert reading into database
        reading_data = {
            "device_id": "real_time_plug_001",
            "reading_type": "POWER_CONSUMPTION",
            "value": 150.0,
            "unit": "W",
            "timestamp": datetime.now().isoformat()
        }
        test_database.insert_energy_reading(reading_data)
        
        # Wait for dashboard update (should be within 1 second)
        await expect(page.locator("text=150 W")).to_be_visible(timeout=1000)
        
        update_time = time.time()
        assert update_time - start_time < 1.0, "Dashboard update took longer than 1 second"
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_data_ingestion_with_high_frequency(self, device_simulator, test_database):
        """Test data ingestion with high frequency (1000+ events/sec)."""
        # Create device
        device_simulator.create_device("high_freq_plug_001", "SMART_PLUG", "Test Room")
        device_simulator.start_device("high_freq_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'high_freq_plug_001', 'SMART_PLUG', 'High Freq Plug', 'Test Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Simulate high-frequency data ingestion
        start_time = time.time()
        readings_inserted = 0
        
        for i in range(1000):  # 1000 readings
            device_simulator.simulate_power_consumption("high_freq_plug_001", 100 + i % 100)
            device_data = device_simulator.get_device_data("high_freq_plug_001")
            
            reading_data = {
                "device_id": "high_freq_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
            readings_inserted += 1
            
            # Small delay to simulate real-time processing
            await asyncio.sleep(0.001)  # 1ms delay
        
        end_time = time.time()
        duration = end_time - start_time
        
        # Verify all readings were inserted
        readings = test_database.get_energy_readings_by_device("high_freq_plug_001")
        assert len(readings) == 1000
        
        # Verify ingestion rate
        ingestion_rate = readings_inserted / duration
        assert ingestion_rate >= 1000, f"Ingestion rate {ingestion_rate} is below 1000 events/sec"
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_data_ingestion_with_network_failure(self, device_simulator, test_database):
        """Test data ingestion resilience during network failures."""
        # Create device
        device_simulator.create_device("network_test_plug_001", "SMART_PLUG", "Test Room")
        device_simulator.start_device("network_test_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'network_test_plug_001', 'SMART_PLUG', 'Network Test Plug', 'Test Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Simulate normal operation
        for i in range(5):
            device_simulator.simulate_power_consumption("network_test_plug_001", 100 + i * 10)
            device_data = device_simulator.get_device_data("network_test_plug_001")
            
            reading_data = {
                "device_id": "network_test_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
            await asyncio.sleep(0.1)
        
        # Simulate network failure
        device_simulator.stop_device("network_test_plug_001")
        
        # Try to insert data during network failure
        failed_readings = []
        for i in range(3):
            try:
                reading_data = {
                    "device_id": "network_test_plug_001",
                    "reading_type": "POWER_CONSUMPTION",
                    "value": 0.0,  # Device is offline
                    "unit": "W",
                    "timestamp": datetime.now().isoformat()
                }
                
                test_database.insert_energy_reading(reading_data)
            except Exception as e:
                failed_readings.append(str(e))
        
        # Verify system handles network failure gracefully
        readings = test_database.get_energy_readings_by_device("network_test_plug_001")
        assert len(readings) >= 5  # At least the initial readings should be there
        
        # Simulate network recovery
        device_simulator.start_device("network_test_plug_001")
        
        # Verify data ingestion resumes
        device_simulator.simulate_power_consumption("network_test_plug_001", 200.0)
        device_data = device_simulator.get_device_data("network_test_plug_001")
        
        reading_data = {
            "device_id": "network_test_plug_001",
            "reading_type": "POWER_CONSUMPTION",
            "value": device_data["power_consumption"],
            "unit": "W",
            "timestamp": datetime.now().isoformat()
        }
        
        test_database.insert_energy_reading(reading_data)
        
        # Verify new reading was inserted
        final_readings = test_database.get_energy_readings_by_device("network_test_plug_001")
        assert len(final_readings) >= 6  # Should have at least one more reading
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_data_validation_and_cleaning(self, device_simulator, test_database):
        """Test data validation and cleaning during ingestion."""
        # Create device
        device_simulator.create_device("validation_plug_001", "SMART_PLUG", "Test Room")
        device_simulator.start_device("validation_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'validation_plug_001', 'SMART_PLUG', 'Validation Plug', 'Test Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Test valid data
        valid_reading = {
            "device_id": "validation_plug_001",
            "reading_type": "POWER_CONSUMPTION",
            "value": 150.5,
            "unit": "W",
            "timestamp": datetime.now().isoformat()
        }
        
        test_database.insert_energy_reading(valid_reading)
        
        # Test invalid data (should be handled gracefully)
        invalid_readings = [
            {
                "device_id": "validation_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": -100.0,  # Negative power (invalid)
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            },
            {
                "device_id": "validation_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": 10000.0,  # Unrealistically high power
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            },
            {
                "device_id": "validation_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": "invalid",  # Invalid data type
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
        ]
        
        for invalid_reading in invalid_readings:
            try:
                test_database.insert_energy_reading(invalid_reading)
            except Exception:
                # Invalid data should be rejected
                pass
        
        # Verify only valid data was stored
        readings = test_database.get_energy_readings_by_device("validation_plug_001")
        assert len(readings) == 1
        assert readings[0]["value"] == 150.5
    
    @pytest.mark.integration
    @pytest.mark.offline
    async def test_data_aggregation_and_summarization(self, device_simulator, test_database):
        """Test data aggregation and summarization."""
        # Create multiple devices
        devices = ["agg_plug_001", "agg_plug_002", "agg_plug_003"]
        
        for device_id in devices:
            device_simulator.create_device(device_id, "SMART_PLUG", "Test Room")
            device_simulator.start_device(device_id)
            
            test_database.execute_query("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, {
                "id": f"device-{device_id}",
                "device_id": device_id,
                "device_type": "SMART_PLUG",
                "device_name": f"Aggregation {device_id}",
                "location": "Test Room",
                "protocol": "MQTT",
                "status": "ONLINE",
                "power_consumption": 0.0
            })
        
        # Generate data for each device
        total_power = 0
        for device_id in devices:
            power = 100 + hash(device_id) % 100  # Consistent but different power
            device_simulator.simulate_power_consumption(device_id, power)
            device_data = device_simulator.get_device_data(device_id)
            
            reading_data = {
                "device_id": device_id,
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
            total_power += device_data["power_consumption"]
        
        # Test aggregation queries
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) == 3
        
        # Calculate total power consumption
        calculated_total = sum(float(r["value"]) for r in all_readings)
        assert abs(calculated_total - total_power) < 0.01  # Allow for floating point precision
        
        # Test average power consumption
        average_power = calculated_total / len(all_readings)
        assert average_power > 0
        
        # Test device count
        device_count = len(set(r["device_id"] for r in all_readings))
        assert device_count == 3
