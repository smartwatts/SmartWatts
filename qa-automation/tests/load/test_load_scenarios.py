"""
SmartWatts Edge Load Tests - Performance and Scalability
"""

import pytest
import asyncio
import time
import statistics
from datetime import datetime
from typing import Dict, Any, List
from concurrent.futures import ThreadPoolExecutor, as_completed

from tests.utils.config import TestConfig
from tests.fixtures.mock_services import MockDeviceSimulator
from tests.utils.database import TestDatabaseManager


class TestLoadScenarios:
    """Test system performance under various load conditions."""
    
    @pytest.mark.load
    @pytest.mark.slow
    async def test_basic_load_50_users(self, device_simulator, test_database):
        """Test basic load with 50 concurrent users."""
        # Create test users
        users = []
        for i in range(50):
            user_data = {
                "username": f"load_user_{i:03d}",
                "email": f"loaduser{i}@mysmartwatts.com",
                "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
                "first_name": f"LoadUser{i}",
                "last_name": "Test",
                "phone_number": f"+23412345678{i:02d}",
                "role": "ROLE_USER",
                "is_active": True
            }
            
            user_id = test_database.insert_user(user_data)
            users.append(user_id)
        
        # Create devices for each user
        devices_per_user = 3
        all_devices = []
        
        for user_id in users:
            for j in range(devices_per_user):
                device_id = f"load_device_{user_id}_{j:03d}"
                device_simulator.create_device(device_id, "SMART_PLUG", f"User {user_id} Room {j}")
                device_simulator.start_device(device_id)
                all_devices.append(device_id)
                
                test_database.execute_query("""
                    INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, {
                    "id": f"device-{device_id}",
                    "device_id": device_id,
                    "device_type": "SMART_PLUG",
                    "device_name": f"Load Device {j}",
                    "location": f"User {user_id} Room {j}",
                    "protocol": "MQTT",
                    "status": "ONLINE",
                    "power_consumption": 0.0
                })
        
        # Simulate concurrent data generation
        start_time = time.time()
        readings_generated = 0
        
        def generate_user_data(user_id):
            user_devices = [d for d in all_devices if user_id in d]
            user_readings = 0
            
            for device_id in user_devices:
                device_simulator.simulate_power_consumption(device_id, 100 + hash(device_id) % 100)
                device_data = device_simulator.get_device_data(device_id)
                
                reading_data = {
                    "device_id": device_id,
                    "reading_type": "POWER_CONSUMPTION",
                    "value": device_data["power_consumption"],
                    "unit": "W",
                    "timestamp": datetime.now().isoformat()
                }
                
                test_database.insert_energy_reading(reading_data)
                user_readings += 1
            
            return user_readings
        
        # Run concurrent data generation
        with ThreadPoolExecutor(max_workers=50) as executor:
            futures = [executor.submit(generate_user_data, user_id) for user_id in users]
            results = [future.result() for future in as_completed(futures)]
        
        readings_generated = sum(results)
        end_time = time.time()
        duration = end_time - start_time
        
        # Verify performance metrics
        readings_per_second = readings_generated / duration
        assert readings_per_second >= 100, f"Readings per second {readings_per_second} is below 100"
        
        # Verify data integrity
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) >= readings_generated * 0.95, "Data loss detected"
        
        # Verify response time
        assert duration < 30, f"Load test took too long: {duration:.2f}s"
    
    @pytest.mark.load
    @pytest.mark.slow
    async def test_stress_load_200_users(self, device_simulator, test_database):
        """Test stress load with 200 concurrent users."""
        # Create test users
        users = []
        for i in range(200):
            user_data = {
                "username": f"stress_user_{i:03d}",
                "email": f"stressuser{i}@mysmartwatts.com",
                "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
                "first_name": f"StressUser{i}",
                "last_name": "Test",
                "phone_number": f"+23412345678{i:02d}",
                "role": "ROLE_USER",
                "is_active": True
            }
            
            user_id = test_database.insert_user(user_data)
            users.append(user_id)
        
        # Create devices for each user
        devices_per_user = 2
        all_devices = []
        
        for user_id in users:
            for j in range(devices_per_user):
                device_id = f"stress_device_{user_id}_{j:03d}"
                device_simulator.create_device(device_id, "SMART_PLUG", f"User {user_id} Room {j}")
                device_simulator.start_device(device_id)
                all_devices.append(device_id)
                
                test_database.execute_query("""
                    INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, {
                    "id": f"device-{device_id}",
                    "device_id": device_id,
                    "device_type": "SMART_PLUG",
                    "device_name": f"Stress Device {j}",
                    "location": f"User {user_id} Room {j}",
                    "protocol": "MQTT",
                    "status": "ONLINE",
                    "power_consumption": 0.0
                })
        
        # Simulate high-frequency data generation
        start_time = time.time()
        readings_generated = 0
        errors = []
        
        def generate_stress_data(user_id):
            user_devices = [d for d in all_devices if user_id in d]
            user_readings = 0
            user_errors = 0
            
            for _ in range(10):  # 10 iterations per user
                for device_id in user_devices:
                    try:
                        device_simulator.simulate_power_consumption(device_id, 100 + hash(device_id) % 100)
                        device_data = device_simulator.get_device_data(device_id)
                        
                        reading_data = {
                            "device_id": device_id,
                            "reading_type": "POWER_CONSUMPTION",
                            "value": device_data["power_consumption"],
                            "unit": "W",
                            "timestamp": datetime.now().isoformat()
                        }
                        
                        test_database.insert_energy_reading(reading_data)
                        user_readings += 1
                        
                    except Exception as e:
                        user_errors += 1
                        errors.append(str(e))
            
            return user_readings, user_errors
        
        # Run stress test
        with ThreadPoolExecutor(max_workers=200) as executor:
            futures = [executor.submit(generate_stress_data, user_id) for user_id in users]
            results = [future.result() for future in as_completed(futures)]
        
        readings_generated = sum(r[0] for r in results)
        total_errors = sum(r[1] for r in results)
        end_time = time.time()
        duration = end_time - start_time
        
        # Verify performance metrics
        readings_per_second = readings_generated / duration
        error_rate = total_errors / (readings_generated + total_errors) if (readings_generated + total_errors) > 0 else 0
        
        assert readings_per_second >= 500, f"Readings per second {readings_per_second} is below 500"
        assert error_rate < 0.05, f"Error rate {error_rate:.2%} is too high"
        
        # Verify system stability
        assert duration < 60, f"Stress test took too long: {duration:.2f}s"
    
    @pytest.mark.load
    @pytest.mark.slow
    async def test_spike_load_sudden_traffic(self, device_simulator, test_database):
        """Test system behavior during sudden traffic spikes."""
        # Create base users
        base_users = 50
        users = []
        
        for i in range(base_users):
            user_data = {
                "username": f"spike_user_{i:03d}",
                "email": f"spikeuser{i}@mysmartwatts.com",
                "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
                "first_name": f"SpikeUser{i}",
                "last_name": "Test",
                "phone_number": f"+23412345678{i:02d}",
                "role": "ROLE_USER",
                "is_active": True
            }
            
            user_id = test_database.insert_user(user_data)
            users.append(user_id)
        
        # Create devices
        all_devices = []
        for user_id in users:
            device_id = f"spike_device_{user_id}"
            device_simulator.create_device(device_id, "SMART_PLUG", f"User {user_id}")
            device_simulator.start_device(device_id)
            all_devices.append(device_id)
            
            test_database.execute_query("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, {
                "id": f"device-{device_id}",
                "device_id": device_id,
                "device_type": "SMART_PLUG",
                "device_name": f"Spike Device",
                "location": f"User {user_id}",
                "protocol": "MQTT",
                "status": "ONLINE",
                "power_consumption": 0.0
            })
        
        # Phase 1: Normal load (30 seconds)
        print("Phase 1: Normal load")
        normal_start = time.time()
        normal_readings = 0
        
        while time.time() - normal_start < 30:
            for device_id in all_devices:
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
                normal_readings += 1
            
            await asyncio.sleep(1)  # 1 second intervals
        
        # Phase 2: Traffic spike (10 seconds)
        print("Phase 2: Traffic spike")
        spike_start = time.time()
        spike_readings = 0
        
        # Add 200 more users during spike
        spike_users = []
        for i in range(200):
            user_data = {
                "username": f"spike_user_{base_users + i:03d}",
                "email": f"spikeuser{base_users + i}@mysmartwatts.com",
                "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
                "first_name": f"SpikeUser{base_users + i}",
                "last_name": "Test",
                "phone_number": f"+23412345678{base_users + i:02d}",
                "role": "ROLE_USER",
                "is_active": True
            }
            
            user_id = test_database.insert_user(user_data)
            spike_users.append(user_id)
            
            # Create device for spike user
            device_id = f"spike_device_{user_id}"
            device_simulator.create_device(device_id, "SMART_PLUG", f"User {user_id}")
            device_simulator.start_device(device_id)
            all_devices.append(device_id)
            
            test_database.execute_query("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, {
                "id": f"device-{device_id}",
                "device_id": device_id,
                "device_type": "SMART_PLUG",
                "device_name": f"Spike Device",
                "location": f"User {user_id}",
                "protocol": "MQTT",
                "status": "ONLINE",
                "power_consumption": 0.0
            })
        
        # Generate high-frequency data during spike
        while time.time() - spike_start < 10:
            for device_id in all_devices:
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
                spike_readings += 1
            
            await asyncio.sleep(0.1)  # High frequency during spike
        
        # Phase 3: Return to normal (30 seconds)
        print("Phase 3: Return to normal")
        normal2_start = time.time()
        normal2_readings = 0
        
        while time.time() - normal2_start < 30:
            for device_id in all_devices:
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
                normal2_readings += 1
            
            await asyncio.sleep(1)
        
        # Analyze results
        total_duration = time.time() - normal_start
        total_readings = normal_readings + spike_readings + normal2_readings
        
        # Calculate rates for each phase
        normal_rate = normal_readings / 30
        spike_rate = spike_readings / 10
        normal2_rate = normal2_readings / 30
        
        print(f"Normal rate: {normal_rate:.2f} readings/sec")
        print(f"Spike rate: {spike_rate:.2f} readings/sec")
        print(f"Normal2 rate: {normal2_rate:.2f} readings/sec")
        
        # Verify system handled the spike
        assert spike_rate > normal_rate * 2, "Spike rate should be significantly higher than normal"
        assert normal2_rate >= normal_rate * 0.8, "System should recover to near-normal performance"
        
        # Verify data integrity
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) >= total_readings * 0.95, "Data loss detected during spike test"
    
    @pytest.mark.load
    @pytest.mark.slow
    async def test_soak_test_72_hours(self, device_simulator, test_database):
        """Test system stability over 72 hours (simulated)."""
        # Create test setup
        device_simulator.create_device("soak_test_plug_001", "SMART_PLUG", "Test Room")
        device_simulator.start_device("soak_test_plug_001")
        
        test_database.execute_query("""
            INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
            VALUES ('device-001', 'soak_test_plug_001', 'SMART_PLUG', 'Soak Test Plug', 'Test Room', 'MQTT', 'ONLINE', 0.0)
        """)
        
        # Simulate 72 hours of operation (compressed to 72 minutes for testing)
        hours_to_simulate = 72
        minutes_per_hour = 1  # Compress time for testing
        total_minutes = hours_to_simulate * minutes_per_hour
        
        start_time = time.time()
        readings_generated = 0
        errors = []
        memory_samples = []
        cpu_samples = []
        
        for minute in range(total_minutes):
            # Generate data every minute
            device_simulator.simulate_power_consumption("soak_test_plug_001", 100 + (minute % 100))
            device_data = device_simulator.get_device_data("soak_test_plug_001")
            
            reading_data = {
                "device_id": "soak_test_plug_001",
                "reading_type": "POWER_CONSUMPTION",
                "value": device_data["power_consumption"],
                "unit": "W",
                "timestamp": datetime.now().isoformat()
            }
            
            try:
                test_database.insert_energy_reading(reading_data)
                readings_generated += 1
            except Exception as e:
                errors.append(str(e))
            
            # Sample system resources every 10 minutes
            if minute % 10 == 0:
                memory_samples.append(psutil.virtual_memory().percent)
                cpu_samples.append(psutil.cpu_percent())
            
            # Simulate time passage
            await asyncio.sleep(1)  # 1 second = 1 minute in simulation
        
        end_time = time.time()
        actual_duration = end_time - start_time
        
        # Analyze results
        error_rate = len(errors) / readings_generated if readings_generated > 0 else 0
        
        print(f"Soak test completed:")
        print(f"  Duration: {actual_duration:.2f} seconds")
        print(f"  Readings generated: {readings_generated}")
        print(f"  Error rate: {error_rate:.4f}")
        print(f"  Memory samples: {len(memory_samples)}")
        print(f"  CPU samples: {len(cpu_samples)}")
        
        # Verify system stability
        assert error_rate < 0.01, f"Error rate {error_rate:.4f} is too high for soak test"
        assert readings_generated >= total_minutes * 0.95, "Data generation rate too low"
        
        # Verify memory stability
        if len(memory_samples) > 5:
            memory_variance = statistics.variance(memory_samples)
            assert memory_variance < 100, f"Memory usage too variable: {memory_variance}"
        
        # Verify CPU stability
        if len(cpu_samples) > 5:
            max_cpu = max(cpu_samples)
            assert max_cpu < 90, f"CPU usage too high: {max_cpu}%"
        
        # Verify data integrity
        all_readings = test_database.get_all_energy_readings()
        assert len(all_readings) >= readings_generated * 0.95, "Data loss detected during soak test"
    
    @pytest.mark.load
    @pytest.mark.slow
    async def test_concurrent_api_requests(self, test_config, test_database):
        """Test concurrent API request handling."""
        # Create test users
        users = []
        for i in range(100):
            user_data = {
                "username": f"api_user_{i:03d}",
                "email": f"apiuser{i}@mysmartwatts.com",
                "password": "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
                "first_name": f"ApiUser{i}",
                "last_name": "Test",
                "phone_number": f"+23412345678{i:02d}",
                "role": "ROLE_USER",
                "is_active": True
            }
            
            user_id = test_database.insert_user(user_data)
            users.append(user_id)
        
        # Simulate concurrent API requests
        def simulate_api_request(user_id):
            try:
                # Simulate API call (database query)
                user = test_database.execute_query(
                    "SELECT * FROM users WHERE id = ?",
                    {"id": user_id}
                )
                
                # Simulate response time
                time.sleep(0.01)  # 10ms response time
                
                return len(user) > 0
            except Exception as e:
                return False
        
        # Run concurrent requests
        start_time = time.time()
        
        with ThreadPoolExecutor(max_workers=100) as executor:
            futures = [executor.submit(simulate_api_request, user_id) for user_id in users]
            results = [future.result() for future in as_completed(futures)]
        
        end_time = time.time()
        duration = end_time - start_time
        
        # Analyze results
        successful_requests = sum(results)
        success_rate = successful_requests / len(users)
        requests_per_second = len(users) / duration
        
        print(f"Concurrent API test results:")
        print(f"  Total requests: {len(users)}")
        print(f"  Successful requests: {successful_requests}")
        print(f"  Success rate: {success_rate:.2%}")
        print(f"  Requests per second: {requests_per_second:.2f}")
        print(f"  Duration: {duration:.2f} seconds")
        
        # Verify performance
        assert success_rate >= 0.95, f"Success rate {success_rate:.2%} is too low"
        assert requests_per_second >= 50, f"Requests per second {requests_per_second:.2f} is too low"
        assert duration < 10, f"Test duration {duration:.2f}s is too long"
