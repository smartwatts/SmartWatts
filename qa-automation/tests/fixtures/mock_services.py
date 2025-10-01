"""
SmartWatts Edge Mock Services for Offline Testing
"""

import asyncio
import json
import time
import uuid
from datetime import datetime, timedelta
from typing import Dict, Any, List, Optional, Callable
from dataclasses import dataclass
from enum import Enum

import httpx
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import uvicorn


class ValidationStatus(Enum):
    PENDING = "pending"
    VERIFIED = "verified"
    FAILED = "failed"


@dataclass
class MockUser:
    """Mock user for testing."""
    id: str
    email: str
    password_hash: str
    first_name: str
    last_name: str
    phone_number: str
    role: str
    is_active: bool
    email_verified: bool
    phone_verified: bool
    created_at: datetime


@dataclass
class ValidationRequest:
    """Mock validation request."""
    id: str
    user_id: str
    validation_type: str  # email, sms, phone
    code: str
    status: ValidationStatus
    created_at: datetime
    expires_at: datetime


class MockCloudService:
    """Mock cloud service for offline validation simulation."""
    
    def __init__(self):
        self.app = FastAPI(title="SmartWatts Mock Cloud Service")
        self.users: Dict[str, MockUser] = {}
        self.validation_requests: Dict[str, ValidationRequest] = {}
        self.email_queue: List[Dict[str, Any]] = []
        self.sms_queue: List[Dict[str, Any]] = []
        self.server = None
        self._setup_routes()
    
    def _setup_routes(self):
        """Setup FastAPI routes."""
        
        @self.app.post("/api/v1/users/register")
        async def register_user(user_data: Dict[str, Any]):
            """Mock user registration."""
            user_id = str(uuid.uuid4())
            user = MockUser(
                id=user_id,
                email=user_data["email"],
                password_hash=user_data["password"],  # In real app, this would be hashed
                first_name=user_data.get("firstName", ""),
                last_name=user_data.get("lastName", ""),
                phone_number=user_data.get("phoneNumber", ""),
                role=user_data.get("role", "ROLE_USER"),
                is_active=True,
                email_verified=False,
                phone_verified=False,
                created_at=datetime.now()
            )
            
            self.users[user_id] = user
            
            # Create validation requests
            if user_data["email"]:
                await self._create_validation_request(user_id, "email")
            if user_data.get("phoneNumber"):
                await self._create_validation_request(user_id, "sms")
            
            return {
                "userId": user_id,
                "email": user.email,
                "message": "User registered successfully. Please check your email/SMS for verification.",
                "requiresVerification": True
            }
        
        @self.app.post("/api/v1/users/login")
        async def login_user(credentials: Dict[str, Any]):
            """Mock user login."""
            email = credentials.get("usernameOrEmail", "")
            password = credentials.get("password", "")
            
            # Find user by email
            user = None
            for u in self.users.values():
                if u.email == email:
                    user = u
                    break
            
            if not user or user.password_hash != password:
                raise HTTPException(status_code=401, detail="Invalid credentials")
            
            if not user.is_active:
                raise HTTPException(status_code=403, detail="Account is deactivated")
            
            # Generate mock tokens
            access_token = f"mock_access_token_{user_id}_{int(time.time())}"
            refresh_token = f"mock_refresh_token_{user_id}_{int(time.time())}"
            
            return {
                "userId": user.id,
                "username": user.email.split("@")[0],
                "email": user.email,
                "accessToken": access_token,
                "refreshToken": refresh_token,
                "tokenType": "Bearer",
                "expiresAt": (datetime.now() + timedelta(hours=24)).isoformat(),
                "role": user.role,
                "active": user.is_active
            }
        
        @self.app.post("/api/v1/users/verify-email")
        async def verify_email(verification_data: Dict[str, Any]):
            """Mock email verification."""
            code = verification_data.get("code", "")
            email = verification_data.get("email", "")
            
            # Find validation request
            validation = None
            for v in self.validation_requests.values():
                if v.validation_type == "email" and v.code == code:
                    user = self.users.get(v.user_id)
                    if user and user.email == email:
                        validation = v
                        break
            
            if not validation or validation.status != ValidationStatus.PENDING:
                raise HTTPException(status_code=400, detail="Invalid or expired verification code")
            
            if datetime.now() > validation.expires_at:
                raise HTTPException(status_code=400, detail="Verification code has expired")
            
            # Update user verification status
            user = self.users[validation.user_id]
            user.email_verified = True
            validation.status = ValidationStatus.VERIFIED
            
            return {
                "message": "Email verified successfully",
                "verified": True
            }
        
        @self.app.post("/api/v1/users/verify-phone")
        async def verify_phone(verification_data: Dict[str, Any]):
            """Mock phone verification."""
            code = verification_data.get("code", "")
            phone = verification_data.get("phoneNumber", "")
            
            # Find validation request
            validation = None
            for v in self.validation_requests.values():
                if v.validation_type == "sms" and v.code == code:
                    user = self.users.get(v.user_id)
                    if user and user.phone_number == phone:
                        validation = v
                        break
            
            if not validation or validation.status != ValidationStatus.PENDING:
                raise HTTPException(status_code=400, detail="Invalid or expired verification code")
            
            if datetime.now() > validation.expires_at:
                raise HTTPException(status_code=400, detail="Verification code has expired")
            
            # Update user verification status
            user = self.users[validation.user_id]
            user.phone_verified = True
            validation.status = ValidationStatus.VERIFIED
            
            return {
                "message": "Phone verified successfully",
                "verified": True
            }
        
        @self.app.get("/api/v1/users/profile")
        async def get_user_profile(user_id: str = None):
            """Mock get user profile."""
            if not user_id:
                raise HTTPException(status_code=400, detail="User ID required")
            
            user = self.users.get(user_id)
            if not user:
                raise HTTPException(status_code=404, detail="User not found")
            
            return {
                "id": user.id,
                "email": user.email,
                "firstName": user.first_name,
                "lastName": user.last_name,
                "phoneNumber": user.phone_number,
                "role": user.role,
                "isActive": user.is_active,
                "emailVerified": user.email_verified,
                "phoneVerified": user.phone_verified,
                "createdAt": user.created_at.isoformat()
            }
        
        @self.app.get("/api/v1/validation/status/{user_id}")
        async def get_validation_status(user_id: str):
            """Get validation status for user."""
            user = self.users.get(user_id)
            if not user:
                raise HTTPException(status_code=404, detail="User not found")
            
            email_validation = None
            phone_validation = None
            
            for validation in self.validation_requests.values():
                if validation.user_id == user_id:
                    if validation.validation_type == "email":
                        email_validation = validation
                    elif validation.validation_type == "sms":
                        phone_validation = validation
            
            return {
                "userId": user_id,
                "emailVerified": user.email_verified,
                "phoneVerified": user.phone_verified,
                "emailValidation": {
                    "status": email_validation.status.value if email_validation else "not_required",
                    "expiresAt": email_validation.expires_at.isoformat() if email_validation else None
                } if email_validation else None,
                "phoneValidation": {
                    "status": phone_validation.status.value if phone_validation else "not_required",
                    "expiresAt": phone_validation.expires_at.isoformat() if phone_validation else None
                } if phone_validation else None
            }
        
        @self.app.get("/api/v1/email/queue")
        async def get_email_queue():
            """Get email queue for testing."""
            return {"emails": self.email_queue}
        
        @self.app.get("/api/v1/sms/queue")
        async def get_sms_queue():
            """Get SMS queue for testing."""
            return {"sms": self.sms_queue}
    
    async def _create_validation_request(self, user_id: str, validation_type: str):
        """Create validation request."""
        code = str(uuid.uuid4())[:6].upper()
        validation_id = str(uuid.uuid4())
        
        validation = ValidationRequest(
            id=validation_id,
            user_id=user_id,
            validation_type=validation_type,
            code=code,
            status=ValidationStatus.PENDING,
            created_at=datetime.now(),
            expires_at=datetime.now() + timedelta(minutes=15)
        )
        
        self.validation_requests[validation_id] = validation
        
        # Add to email/SMS queue
        user = self.users[user_id]
        if validation_type == "email":
            self.email_queue.append({
                "to": user.email,
                "subject": "SmartWatts Email Verification",
                "body": f"Your verification code is: {code}",
                "code": code,
                "created_at": datetime.now().isoformat()
            })
        elif validation_type == "sms":
            self.sms_queue.append({
                "to": user.phone_number,
                "message": f"Your SmartWatts verification code is: {code}",
                "code": code,
                "created_at": datetime.now().isoformat()
            })
    
    async def start_server(self, host: str = "localhost", port: int = 9999):
        """Start mock server."""
        config = uvicorn.Config(
            self.app,
            host=host,
            port=port,
            log_level="info"
        )
        self.server = uvicorn.Server(config)
        await self.server.serve()
    
    async def stop_server(self):
        """Stop mock server."""
        if self.server:
            self.server.should_exit = True
    
    def get_verification_code(self, email: str, validation_type: str = "email") -> Optional[str]:
        """Get verification code for testing."""
        for validation in self.validation_requests.values():
            if validation.validation_type == validation_type:
                user = self.users.get(validation.user_id)
                if user and user.email == email:
                    return validation.code
        return None
    
    def clear_data(self):
        """Clear all mock data."""
        self.users.clear()
        self.validation_requests.clear()
        self.email_queue.clear()
        self.sms_queue.clear()


class MockDeviceSimulator:
    """Mock device simulator for smart plugs and energy sources."""
    
    def __init__(self):
        self.devices: Dict[str, Dict[str, Any]] = {}
        self.data_generators: Dict[str, Callable] = {}
        self.running = False
        self._setup_device_types()
    
    def _setup_device_types(self):
        """Setup mock device types and data generators."""
        self.device_types = {
            "SMART_PLUG": {
                "power_range": (10, 500),  # Watts
                "voltage_range": (220, 240),  # Volts
                "current_range": (0.1, 2.5),  # Amperes
                "power_factor": 0.95
            },
            "INVERTER": {
                "power_range": (0, 5000),  # Watts
                "voltage_range": (220, 240),
                "current_range": (0, 25),
                "power_factor": 0.98
            },
            "SMART_METER": {
                "power_range": (0, 10000),  # Watts
                "voltage_range": (220, 240),
                "current_range": (0, 50),
                "power_factor": 0.92
            },
            "BATTERY": {
                "power_range": (0, 3000),  # Watts
                "voltage_range": (48, 52),  # Volts
                "current_range": (0, 60),  # Amperes
                "power_factor": 0.99
            }
        }
    
    def create_device(self, device_id: str, device_type: str, location: str = "Unknown") -> Dict[str, Any]:
        """Create mock device."""
        device_config = self.device_types.get(device_type, self.device_types["SMART_PLUG"])
        
        device = {
            "device_id": device_id,
            "device_type": device_type,
            "device_name": f"{device_type}_{device_id}",
            "location": location,
            "status": "OFFLINE",
            "power_consumption": 0.0,
            "voltage": 0.0,
            "current": 0.0,
            "power_factor": device_config["power_factor"],
            "created_at": datetime.now(),
            "last_update": datetime.now()
        }
        
        self.devices[device_id] = device
        return device
    
    def start_device(self, device_id: str):
        """Start device simulation."""
        if device_id in self.devices:
            self.devices[device_id]["status"] = "ONLINE"
            self.devices[device_id]["last_update"] = datetime.now()
    
    def stop_device(self, device_id: str):
        """Stop device simulation."""
        if device_id in self.devices:
            self.devices[device_id]["status"] = "OFFLINE"
            self.devices[device_id]["power_consumption"] = 0.0
            self.devices[device_id]["last_update"] = datetime.now()
    
    def simulate_power_consumption(self, device_id: str, base_power: float = None):
        """Simulate realistic power consumption."""
        if device_id not in self.devices:
            return
        
        device = self.devices[device_id]
        device_type = device["device_type"]
        config = self.device_types[device_type]
        
        if device["status"] != "ONLINE":
            device["power_consumption"] = 0.0
            return
        
        # Generate realistic power consumption
        if base_power is None:
            min_power, max_power = config["power_range"]
            base_power = min_power + (max_power - min_power) * 0.3  # 30% of range
        
        # Add some randomness
        import random
        variation = random.uniform(0.8, 1.2)
        power = base_power * variation
        
        # Calculate voltage and current
        voltage = random.uniform(*config["voltage_range"])
        current = power / voltage if voltage > 0 else 0
        
        device["power_consumption"] = round(power, 2)
        device["voltage"] = round(voltage, 2)
        device["current"] = round(current, 2)
        device["last_update"] = datetime.now()
    
    def simulate_solar_generation(self, device_id: str, time_of_day: float = 0.5):
        """Simulate solar generation based on time of day."""
        if device_id not in self.devices:
            return
        
        device = self.devices[device_id]
        if device["device_type"] != "INVERTER" or device["status"] != "ONLINE":
            device["power_consumption"] = 0.0
            return
        
        # Solar generation curve (peak at noon)
        solar_efficiency = max(0, 1 - abs(time_of_day - 0.5) * 2)  # 0.5 = noon
        max_generation = 5000  # 5kW max
        
        generation = max_generation * solar_efficiency * random.uniform(0.9, 1.1)
        
        device["power_consumption"] = -round(generation, 2)  # Negative for generation
        device["last_update"] = datetime.now()
    
    def get_device_data(self, device_id: str) -> Optional[Dict[str, Any]]:
        """Get current device data."""
        return self.devices.get(device_id)
    
    def get_all_devices(self) -> List[Dict[str, Any]]:
        """Get all devices."""
        return list(self.devices.values())
    
    def simulate_power_outage(self, duration_seconds: int = 30):
        """Simulate power outage."""
        for device in self.devices.values():
            if device["status"] == "ONLINE":
                device["status"] = "POWER_OUTAGE"
                device["power_consumption"] = 0.0
        
        # Schedule recovery
        asyncio.create_task(self._recover_from_outage(duration_seconds))
    
    async def _recover_from_outage(self, delay: int):
        """Recover from power outage."""
        await asyncio.sleep(delay)
        for device in self.devices.values():
            if device["status"] == "POWER_OUTAGE":
                device["status"] = "ONLINE"
    
    def start_data_simulation(self, interval_seconds: int = 1):
        """Start continuous data simulation."""
        self.running = True
        asyncio.create_task(self._simulate_data_loop(interval_seconds))
    
    def stop_data_simulation(self):
        """Stop data simulation."""
        self.running = False
    
    async def _simulate_data_loop(self, interval_seconds: int):
        """Data simulation loop."""
        while self.running:
            current_hour = datetime.now().hour
            time_of_day = current_hour / 24.0
            
            for device_id, device in self.devices.items():
                if device["status"] == "ONLINE":
                    if device["device_type"] == "INVERTER":
                        self.simulate_solar_generation(device_id, time_of_day)
                    else:
                        self.simulate_power_consumption(device_id)
            
            await asyncio.sleep(interval_seconds)
    
    def clear_devices(self):
        """Clear all devices."""
        self.devices.clear()


class MockServices:
    """Main mock services coordinator."""
    
    def __init__(self, config):
        self.config = config
        self.cloud_service = MockCloudService()
        self.device_simulator = MockDeviceSimulator()
        self.mqtt_data = []
        self.modbus_data = []
    
    def simulate_mqtt_data(self, count: int = 10) -> List[Dict[str, Any]]:
        """Simulate MQTT data for testing."""
        import random
        
        for i in range(count):
            data = {
                "timestamp": datetime.now().isoformat(),
                "device_id": f"device_{i:03d}",
                "topic": f"smartwatts/energy/device_{i:03d}",
                "payload": {
                    "power": round(random.uniform(100, 1000), 2),
                    "voltage": round(random.uniform(220, 240), 2),
                    "current": round(random.uniform(1, 5), 2),
                    "frequency": round(random.uniform(49.5, 50.5), 2),
                    "power_factor": round(random.uniform(0.85, 1.0), 2)
                }
            }
            self.mqtt_data.append(data)
        
        return self.mqtt_data
    
    def simulate_modbus_data(self, count: int = 10) -> List[Dict[str, Any]]:
        """Simulate Modbus data for testing."""
        import random
        
        for i in range(count):
            data = {
                "timestamp": datetime.now().isoformat(),
                "device_id": f"modbus_device_{i:03d}",
                "register_address": 40001 + i,
                "value": random.randint(0, 65535),
                "data_type": "HOLDING_REGISTER",
                "unit": "WATT" if i % 2 == 0 else "VOLT"
            }
            self.modbus_data.append(data)
        
        return self.modbus_data
    
    async def start_all_services(self):
        """Start all mock services."""
        # Start cloud service
        await self.cloud_service.start_server(
            host=self.config.mock_cloud_host,
            port=self.config.mock_cloud_port
        )
        
        # Start device simulation
        self.device_simulator.start_data_simulation()
    
    async def stop_all_services(self):
        """Stop all mock services."""
        await self.cloud_service.stop_server()
        self.device_simulator.stop_data_simulation()
    
    def clear_all_data(self):
        """Clear all mock data."""
        self.cloud_service.clear_data()
        self.device_simulator.clear_devices()
        self.mqtt_data.clear()
        self.modbus_data.clear()
