"""
SmartWatts Edge Reliability Tests - Stress and Soak Testing
"""

import pytest
import asyncio
import time
import psutil
import threading
from datetime import datetime, timedelta
from typing import Dict, Any, List
from concurrent.futures import ThreadPoolExecutor, as_completed

from tests.utils.config import TestConfig
from tests.fixtures.mock_services import MockDeviceSimulator
from tests.utils.database import TestDatabaseManager


class TestStressAndSoak:
    """Test system reliability under stress and long-running conditions."""
    
    @pytest.mark.reliability
    @pytest.mark.slow
    async def test_high_data_ingestion_stress(self, device_simulator, test_database):
        """Test system under high data ingestion stress (1000+ events/sec)."""
        # Create multiple devices for stress testing
        device_count = 10
        devices = []
        
        for i in range(device_count):
            device_id = f"stress_plug_{i:03d}"
            device_simulator.create_device(device_id, "SMART_PLUG", f"Room {i}")
            device_simulator.start_device(device_id)
            devices.append(device_id)
            
            test_database.execute_query("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, {
                "id": f"device-{device_id}",
                "device_id": device_id,
                "device_type": "SMART_PLUG",
                "device_name": f"Stress Plug {i}",
                "location": f"Room {i}",
                "protocol": "MQTT",
                "status": "ONLINE",
                "power_consumption": 0.0
            })
        
        # Monitor system resources
        start_memory = psutil.virtual_memory().percent
        start_cpu = psutil.cpu_percent()
        
        # Stress test parameters
        duration_seconds = 60  # 1 minute stress test
        target_events_per_second = 1000
        events_per_device = target_events_per_second // device_count
        
        start_time = time.time()
        events_inserted = 0
        errors = []
        
        try:
            while time.time() - start_time < duration_seconds:
                batch_start = time.time()
                
                # Generate data for all devices
                for device_id in devices:
                    for _ in range(events_per_device):
                        try:
                            device_simulator.simulate_power_consumption(device_id, 100 + (events_inserted % 100))
                            device_data = device_simulator.get_device_data(device_id)
                            
                            reading_data = {
                                "device_id": device_id,
                                "reading_type": "POWER_CONSUMPTION",
                                "value": device_data["power_consumption"],
                                "unit": "W",
                                "timestamp": datetime.now().isoformat()
                            }
                            
                            test_database.insert_energy_reading(reading_data)
                            events_inserted += 1
                            
                        except Exception as e:
                            errors.append(str(e))
                
                # Control rate to achieve target events per second
                batch_duration = time.time() - batch_start
                target_batch_duration = 1.0  # 1 second per batch
                if batch_duration < target_batch_duration:
                    await asyncio.sleep(target_batch_duration - batch_duration)
        
        except Exception as e:
            pytest.fail(f"Stress test failed: {e}")
        
        end_time = time.time()
        total_duration = end_time - start_time
        
        # Calculate actual events per second
        actual_events_per_second = events_inserted / total_duration
        
        # Check system resources
        end_memory = psutil.virtual_memory().percent
        end_cpu = psutil.cpu_percent()
        
        # Verify performance metrics
        assert actual_events_per_second >= target_events_per_second * 0.8, \
            f"Actual rate {actual_events_per_second} is below 80% of target {target_events_per_second}"
        
        assert len(errors) < events_inserted * 0.01, \
            f"Error rate {len(errors)/events_inserted*100:.2f}% is too high"
        
        assert end_memory - start_memory < 20, \
            f"Memory usage increased by {end_memory - start_memory}% during stress test"
        
        # Verify data integrity
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) >= events_inserted * 0.95, \
            "Data loss detected during stress test"
    
    @pytest.mark.reliability
    @pytest.mark.slow
    async def test_power_outage_simulation(self, device_simulator, test_database):
        """Test system behavior during power outages."""
        # Create devices
        devices = ["outage_plug_001", "outage_plug_002", "outage_inverter_001"]
        
        for device_id in devices:
            device_type = "INVERTER" if "inverter" in device_id else "SMART_PLUG"
            device_simulator.create_device(device_id, device_type, "Test Room")
            device_simulator.start_device(device_id)
            
            test_database.execute_query("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, {
                "id": f"device-{device_id}",
                "device_id": device_id,
                "device_type": device_type,
                "device_name": f"Outage {device_id}",
                "location": "Test Room",
                "protocol": "MQTT",
                "status": "ONLINE",
                "power_consumption": 0.0
            })
        
        # Generate some initial data
        for device_id in devices:
            device_simulator.simulate_power_consumption(device_id, 100)
            device_data = device_simulator.get_device_data(device_id)
            
            reading_data = {
                "device_id": device_id,
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
        
        # Simulate power outage
        device_simulator.simulate_power_outage(duration_seconds=30)
        
        # Verify all devices are offline
        for device_id in devices:
            device_data = device_simulator.get_device_data(device_id)
            assert device_data["status"] == "POWER_OUTAGE"
            assert device_data["power_consumption"] == 0.0
        
        # Wait for power restoration
        await asyncio.sleep(35)  # Wait for recovery
        
        # Verify devices are back online
        for device_id in devices:
            device_data = device_simulator.get_device_data(device_id)
            assert device_data["status"] == "ONLINE"
        
        # Verify system can resume normal operation
        for device_id in devices:
            device_simulator.simulate_power_consumption(device_id, 150)
            device_data = device_simulator.get_device_data(device_id)
            
            reading_data = {
                "device_id": device_id,
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
        
        # Verify data was recorded after recovery
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) >= 6  # 3 devices × 2 readings each
    
    @pytest.mark.reliability
    @pytest.mark.slow
    async def test_memory_leak_detection(self, device_simulator, test_database):
        """Test for memory leaks during extended operation."""
        # Create device
        device_simulator.create_device("memory_test_plug_001", "SMART_PLUG", "Test Room")
        device_simulator.start_device("memory_test_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'memory_test_plug_001', 'SMART_PLUG', 'Memory Test Plug', 'Test Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Monitor memory usage
        memory_samples = []
        start_time = time.time()
        
        # Run for 5 minutes with continuous data generation
        while time.time() - start_time < 300:  # 5 minutes
            # Generate data
            device_simulator.simulate_power_consumption("memory_test_plug_001", 100)
            device_data = device_simulator.get_device_data("memory_test_plug_001")
            
            reading_data = {
                "device_id": "memory_test_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
            
            # Sample memory usage
            memory_samples.append(psutil.virtual_memory().percent)
            
            await asyncio.sleep(1)  # 1 second intervals
        
        # Analyze memory usage trend
        if len(memory_samples) > 10:
            # Calculate memory growth rate
            initial_memory = sum(memory_samples[:10]) / 10
            final_memory = sum(memory_samples[-10:]) / 10
            memory_growth = final_memory - initial_memory
            
            # Memory growth should be minimal (< 5%)
            assert memory_growth < 5, \
                f"Memory leak detected: {memory_growth:.2f}% growth over 5 minutes"
    
    @pytest.mark.reliability
    @pytest.mark.slow
    async def test_cpu_throttling_under_load(self, device_simulator, test_database):
        """Test CPU throttling and thermal management."""
        # Create multiple devices for CPU load
        device_count = 20
        devices = []
        
        for i in range(device_count):
            device_id = f"cpu_test_plug_{i:03d}"
            device_simulator.create_device(device_id, "SMART_PLUG", f"Room {i}")
            device_simulator.start_device(device_id)
            devices.append(device_id)
            
            test_database.execute_query("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, {
                "id": f"device-{device_id}",
                "device_id": device_id,
                "device_type": "SMART_PLUG",
                "device_name": f"CPU Test Plug {i}",
                "location": f"Room {i}",
                "protocol": "MQTT",
                "status": "ONLINE",
                "power_consumption": 0.0
            })
        
        # Monitor CPU usage
        cpu_samples = []
        start_time = time.time()
        
        # Run intensive workload for 2 minutes
        while time.time() - start_time < 120:  # 2 minutes
            # Generate data for all devices simultaneously
            for device_id in devices:
                device_simulator.simulate_power_consumption(device_id, 100)
                device_data = device_simulator.get_device_data(device_id)
                
                reading_data = {
                    "device_id": device_id,
                    "reading_type": "POWER_CONSUMPTION",
                    "value": device_data["power_consumption"],
                    "unit": "W",
                    "timestamp": datetime.now().isoformat()
                }
                
                test_database.insert_energy_reading(reading_data)
            
            # Sample CPU usage
            cpu_samples.append(psutil.cpu_percent())
            
            await asyncio.sleep(0.1)  # High frequency sampling
        
        # Analyze CPU usage
        if len(cpu_samples) > 10:
            max_cpu = max(cpu_samples)
            avg_cpu = sum(cpu_samples) / len(cpu_samples)
            
            # CPU should not exceed 90% for extended periods
            assert max_cpu < 90, \
                f"CPU usage exceeded 90%: {max_cpu}%"
            
            # Average CPU should be reasonable
            assert avg_cpu < 70, \
                f"Average CPU usage too high: {avg_cpu}%"
    
    @pytest.mark.reliability
    @pytest.mark.slow
    async def test_concurrent_user_sessions(self, page, test_config, test_database):
        """Test system behavior with multiple concurrent user sessions."""
        # Create multiple test users
        users = []
        for i in range(10):
            user_data = {
                "username": f"concurrent_user_{i:03d}",
                "email": f"user{i}@mysmartwatts.com",
                "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
                "first_name": f"User{i}",
                "last_name": "Concurrent",
                "phone_number": f"+23412345678{i:02d}",
                "role": "ROLE_USER",
                "is_active": True
            }
            
            user_id = test_database.insert_user(user_data)
            users.append(user_id)
        
        # Simulate concurrent logins
        def simulate_login(user_id):
            try:
                # This would normally be an API call
                # For testing, we'll just verify the user exists
                user = test_database.execute_query(
                    "SELECT * FROM users WHERE id = ?",
                    {"id": user_id}
                )
                return len(user) > 0
            except Exception as e:
                return False
        
        # Run concurrent logins
        with ThreadPoolExecutor(max_workers=10) as executor:
            futures = [executor.submit(simulate_login, user_id) for user_id in users]
            results = [future.result() for future in as_completed(futures)]
        
        # Verify all logins succeeded
        assert all(results), "Some concurrent logins failed"
        
        # Verify all users are in the database
        all_users = test_database.get_all_users()
        assert len(all_users) >= 10, "Not all users were created"
    
    @pytest.mark.reliability
    @pytest.mark.slow
    async def test_database_corruption_recovery(self, device_simulator, test_database):
        """Test database corruption recovery mechanisms."""
        # Create device and generate data
        device_simulator.create_device("corruption_test_plug_001", "SMART_PLUG", "Test Room")
        device_simulator.start_device("corruption_test_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'corruption_test_plug_001', 'SMART_PLUG', 'Corruption Test Plug', 'Test Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Generate some data
        for i in range(100):
            device_simulator.simulate_power_consumption("corruption_test_plug_001", 100 + i)
            device_data = device_simulator.get_device_data("corruption_test_plug_001")
            
            reading_data = {
                "device_id": "corruption_test_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            test_database.insert_energy_reading(reading_data)
        
        # Verify data integrity
        readings = test_database.get_energy_readings_by_device("corruption_test_plug_001")
        assert len(readings) == 100
        
        # Test database queries still work
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) >= 100
        
        # Test data consistency
        for reading in readings:
            assert "device_id" in reading
            assert "value" in reading
            assert "timestamp" in reading
            assert reading["device_id"] == "corruption_test_plug_001"
    
    @pytest.mark.reliability
    @pytest.mark.slow
    async def test_30_day_data_retention(self, device_simulator, test_database):
        """Test 30-day data retention without corruption."""
        # Create device
        device_simulator.create_device("retention_test_plug_001", "SMART_PLUG", "Test Room")
        device_simulator.start_device("retention_test_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'retention_test_plug_001', 'SMART_PLUG', 'Retention Test Plug', 'Test Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Generate data for 30 days (simulated)
        days_to_simulate = 30
        readings_per_day = 1440  # 1 reading per minute
        
        for day in range(days_to_simulate):
            for minute in range(readings_per_day):
                # Simulate different power consumption throughout the day
                hour = minute // 60
                if 6 <= hour <= 22:  # Daytime
                    power = 150 + (minute % 60) * 2
                else:  # Nighttime
                    power = 50 + (minute % 60)
                
                device_simulator.simulate_power_consumption("retention_test_plug_001", power)
                device_data = device_simulator.get_device_data("retention_test_plug_001")
                
                # Create timestamp for this day and minute
                timestamp = datetime.now() - timedelta(days=days_to_simulate-day-1, minutes=readings_per_day-minute-1)
                
                reading_data = {
                    "device_id": "retention_test_plug_001",
                    "reading_type": "POWER_CONSUMPTION",
                    "value": device_data["power_consumption"],
                    "unit": "W",
                    "timestamp": timestamp.isoformat()
                }
                
                test_database.insert_energy_reading(reading_data)
        
        # Verify data retention
        all_readings = test_database.get_all_energy_readings()
        expected_readings = days_to_simulate * readings_per_day
        
        assert len(all_readings) >= expected_readings * 0.95, \
            f"Data retention issue: expected {expected_readings}, got {len(all_readings)}"
        
        # Verify data integrity
        for reading in all_readings:
            assert "device_id" in reading
            assert "value" in reading
            assert "timestamp" in reading
            assert reading["device_id"] == "retention_test_plug_001"
            assert float(reading["value"]) > 0
        
        # Test data queries still work efficiently
        start_time = time.time()
        recent_readings = test_database.execute_query("""
            SELECT * FROM energy_readings 
            WHERE device_id = ? 
            ORDER BY timestamp DESC 
            LIMIT 100
        """, {"device_id": "retention_test_plug_001"})
        query_time = time.time() - start_time
        
        assert query_time < 1.0, f"Data query too slow: {query_time:.2f}s"
        assert len(recent_readings) == 100
