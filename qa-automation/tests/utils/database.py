"""
SmartWatts Edge Test Database Manager
"""

import os
import tempfile
from pathlib import Path
from typing import Generator, Dict, Any, List
from contextlib import contextmanager

from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import StaticPool

from tests.utils.config import TestConfig


class TestDatabaseManager:
    """Manages test database for SmartWatts Edge testing."""
    
    def __init__(self, config: TestConfig = None):
        self.config = config or TestConfig()
        self.engine = None
        self.session_factory = None
        self.temp_db_path = None
    
    def setup(self):
        """Setup test database."""
        # Create temporary SQLite database for testing
        self.temp_db_path = Path(tempfile.mktemp(suffix=".db"))
        
        # Create SQLite engine
        self.engine = create_engine(
            f"sqlite:///{self.temp_db_path}",
            poolclass=StaticPool,
            connect_args={"check_same_thread": False}
        )
        
        self.session_factory = sessionmaker(bind=self.engine)
        
        # Create test tables
        self._create_test_tables()
        
        # Insert test data
        self._insert_test_data()
    
    def cleanup(self):
        """Cleanup test database."""
        if self.engine:
            self.engine.dispose()
        
        if self.temp_db_path and self.temp_db_path.exists():
            self.temp_db_path.unlink()
    
    def _create_test_tables(self):
        """Create test database tables."""
        with self.engine.connect() as conn:
            # Users table
            conn.execute(text("""
                CREATE TABLE users (
                    id VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    first_name VARCHAR(50),
                    last_name VARCHAR(50),
                    phone_number VARCHAR(20),
                    role VARCHAR(50) DEFAULT 'ROLE_USER',
                    is_active BOOLEAN DEFAULT TRUE,
                    email_verified BOOLEAN DEFAULT FALSE,
                    phone_verified BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """))
            
            # Devices table
            conn.execute(text("""
                CREATE TABLE devices (
                    id VARCHAR(36) PRIMARY KEY,
                    device_id VARCHAR(100) UNIQUE NOT NULL,
                    device_type VARCHAR(50) NOT NULL,
                    device_name VARCHAR(100),
                    location VARCHAR(100),
                    protocol VARCHAR(20),
                    status VARCHAR(20) DEFAULT 'OFFLINE',
                    power_consumption DECIMAL(10,2) DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """))
            
            # Energy readings table
            conn.execute(text("""
                CREATE TABLE energy_readings (
                    id VARCHAR(36) PRIMARY KEY,
                    device_id VARCHAR(100),
                    reading_type VARCHAR(50),
                    value DECIMAL(10,2),
                    unit VARCHAR(10),
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (device_id) REFERENCES devices(device_id)
                )
            """))
            
            # User sessions table
            conn.execute(text("""
                CREATE TABLE user_sessions (
                    id VARCHAR(36) PRIMARY KEY,
                    user_id VARCHAR(36),
                    session_token VARCHAR(255) UNIQUE,
                    expires_at TIMESTAMP,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """))
            
            # Device pairings table
            conn.execute(text("""
                CREATE TABLE device_pairings (
                    id VARCHAR(36) PRIMARY KEY,
                    user_id VARCHAR(36),
                    device_id VARCHAR(100),
                    paired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (device_id) REFERENCES devices(device_id)
                )
            """))
            
            conn.commit()
    
    def _insert_test_data(self):
        """Insert test data into database."""
        with self.engine.connect() as conn:
            # Insert test users
            conn.execute(text("""
                INSERT INTO users (id, username, email, password, first_name, last_name, phone_number, role, is_active)
                VALUES 
                    ('admin-001', 'admin', 'admin@mysmartwatts.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', '+2341234567890', 'ROLE_ENTERPRISE_ADMIN', TRUE),
                    ('user-001', 'testuser', 'test@mysmartwatts.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Test', 'User', '+2341234567891', 'ROLE_USER', TRUE)
            """))
            
            # Insert test devices
            conn.execute(text("""
                INSERT INTO devices (id, device_id, device_type, device_name, location, protocol, status, power_consumption)
                VALUES 
                    ('device-001', 'smart_plug_001', 'SMART_PLUG', 'Living Room TV', 'Living Room', 'MQTT', 'ONLINE', 120.5),
                    ('device-002', 'smart_plug_002', 'SMART_PLUG', 'Kitchen Fridge', 'Kitchen', 'MQTT', 'ONLINE', 200.0),
                    ('device-003', 'inverter_001', 'INVERTER', 'Solar Inverter', 'Garage', 'MODBUS', 'ONLINE', 0.0),
                    ('device-004', 'meter_001', 'SMART_METER', 'Main Meter', 'Utility Room', 'MODBUS', 'ONLINE', 0.0)
            """))
            
            # Insert test energy readings
            conn.execute(text("""
                INSERT INTO energy_readings (id, device_id, reading_type, value, unit)
                VALUES 
                    ('reading-001', 'smart_plug_001', 'POWER_CONSUMPTION', 120.5, 'W'),
                    ('reading-002', 'smart_plug_002', 'POWER_CONSUMPTION', 200.0, 'W'),
                    ('reading-003', 'inverter_001', 'SOLAR_GENERATION', 800.2, 'W'),
                    ('reading-004', 'meter_001', 'GRID_CONSUMPTION', 1500.5, 'W')
            """))
            
            conn.commit()
    
    @contextmanager
    def get_session(self) -> Generator[Session, None, None]:
        """Get database session context manager."""
        session = self.session_factory()
        try:
            yield session
            session.commit()
        except Exception:
            session.rollback()
            raise
        finally:
            session.close()
    
    def execute_query(self, query: str, params: Dict[str, Any] = None) -> List[Dict[str, Any]]:
        """Execute query and return results."""
        with self.engine.connect() as conn:
            result = conn.execute(text(query), params or {})
            return [dict(row._mapping) for row in result]
    
    def insert_user(self, user_data: Dict[str, Any]) -> str:
        """Insert user and return user ID."""
        with self.get_session() as session:
            user_id = f"user-{len(self.get_all_users()) + 1:03d}"
            user_data["id"] = user_id
            
            session.execute(text("""
                INSERT INTO users (id, username, email, password, first_name, last_name, phone_number, role, is_active)
                VALUES (:id, :username, :email, :password, :first_name, :last_name, :phone_number, :role, :is_active)
            """), user_data)
            
            return user_id
    
    def get_user_by_email(self, email: str) -> Dict[str, Any]:
        """Get user by email."""
        users = self.execute_query(
            "SELECT * FROM users WHERE email = :email",
            {"email": email}
        )
        return users[0] if users else None
    
    def get_all_users(self) -> List[Dict[str, Any]]:
        """Get all users."""
        return self.execute_query("SELECT * FROM users")
    
    def get_device_by_id(self, device_id: str) -> Dict[str, Any]:
        """Get device by device ID."""
        devices = self.execute_query(
            "SELECT * FROM devices WHERE device_id = :device_id",
            {"device_id": device_id}
        )
        return devices[0] if devices else None
    
    def get_all_devices(self) -> List[Dict[str, Any]]:
        """Get all devices."""
        return self.execute_query("SELECT * FROM devices")
    
    def update_device_status(self, device_id: str, status: str):
        """Update device status."""
        self.execute_query(
            "UPDATE devices SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE device_id = :device_id",
            {"device_id": device_id, "status": status}
        )
    
    def insert_energy_reading(self, reading_data: Dict[str, Any]) -> str:
        """Insert energy reading and return reading ID."""
        with self.get_session() as session:
            reading_id = f"reading-{len(self.get_all_energy_readings()) + 1:03d}"
            reading_data["id"] = reading_id
            
            session.execute(text("""
                INSERT INTO energy_readings (id, device_id, reading_type, value, unit, timestamp)
                VALUES (:id, :device_id, :reading_type, :value, :unit, :timestamp)
            """), reading_data)
            
            return reading_id
    
    def get_all_energy_readings(self) -> List[Dict[str, Any]]:
        """Get all energy readings."""
        return self.execute_query("SELECT * FROM energy_readings ORDER BY timestamp DESC")
    
    def get_energy_readings_by_device(self, device_id: str) -> List[Dict[str, Any]]:
        """Get energy readings by device ID."""
        return self.execute_query(
            "SELECT * FROM energy_readings WHERE device_id = :device_id ORDER BY timestamp DESC",
            {"device_id": device_id}
        )
    
    def create_user_session(self, user_id: str, session_token: str, expires_at: str) -> str:
        """Create user session."""
        with self.get_session() as session:
            session_id = f"session-{len(self.get_all_sessions()) + 1:03d}"
            
            session.execute(text("""
                INSERT INTO user_sessions (id, user_id, session_token, expires_at)
                VALUES (:id, :user_id, :session_token, :expires_at)
            """), {
                "id": session_id,
                "user_id": user_id,
                "session_token": session_token,
                "expires_at": expires_at
            })
            
            return session_id
    
    def get_all_sessions(self) -> List[Dict[str, Any]]:
        """Get all user sessions."""
        return self.execute_query("SELECT * FROM user_sessions")
    
    def cleanup_test_data(self):
        """Cleanup test data."""
        with self.engine.connect() as conn:
            conn.execute(text("DELETE FROM energy_readings"))
            conn.execute(text("DELETE FROM device_pairings"))
            conn.execute(text("DELETE FROM user_sessions"))
            conn.execute(text("DELETE FROM devices"))
            conn.execute(text("DELETE FROM users"))
            conn.commit()
