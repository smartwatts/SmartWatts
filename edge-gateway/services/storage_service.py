"""
SmartWatts Edge Gateway Storage Service
Local data storage with SQLite database for offline-first operation
"""

import asyncio
import logging
import json
import gzip
from typing import Dict, List, Optional, Any, Union
from datetime import datetime, timedelta
from pathlib import Path
import sqlite3
import aiosqlite
from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, Text, Boolean, JSON
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy.dialects.sqlite import insert
from core.config import EdgeConfig, StorageConfig

# SQLAlchemy base
Base = declarative_base()

class EnergyReading(Base):
    """Energy reading data model."""
    __tablename__ = "energy_readings"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    device_id = Column(String(100), nullable=False, index=True)
    device_type = Column(String(50), nullable=False)
    timestamp = Column(DateTime, nullable=False, index=True)
    power = Column(Float, nullable=True)
    voltage = Column(Float, nullable=True)
    current = Column(Float, nullable=True)
    frequency = Column(Float, nullable=True)
    power_factor = Column(Float, nullable=True)
    energy = Column(Float, nullable=True)
    temperature = Column(Float, nullable=True)
    raw_data = Column(JSON, nullable=True)
    quality = Column(String(20), default="good")
    created_at = Column(DateTime, default=datetime.utcnow)

class DeviceStatus(Base):
    """Device status data model."""
    __tablename__ = "device_status"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    device_id = Column(String(100), nullable=False, index=True)
    device_type = Column(String(50), nullable=False)
    status = Column(String(20), nullable=False)  # online, offline, error
    last_seen = Column(DateTime, nullable=False)
    ip_address = Column(String(45), nullable=True)
    mac_address = Column(String(17), nullable=True)
    firmware_version = Column(String(50), nullable=True)
    hardware_version = Column(String(50), nullable=True)
    metadata = Column(JSON, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

class Alert(Base):
    """Alert data model."""
    __tablename__ = "alerts"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    device_id = Column(String(100), nullable=False, index=True)
    alert_type = Column(String(50), nullable=False)
    severity = Column(String(20), nullable=False)  # critical, warning, info
    message = Column(Text, nullable=False)
    timestamp = Column(DateTime, nullable=False, index=True)
    acknowledged = Column(Boolean, default=False)
    resolved = Column(Boolean, default=False)
    metadata = Column(JSON, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

class AIPrediction(Base):
    """AI prediction data model."""
    __tablename__ = "ai_predictions"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    model_name = Column(String(100), nullable=False)
    prediction_type = Column(String(50), nullable=False)
    input_data = Column(JSON, nullable=False)
    prediction_result = Column(JSON, nullable=False)
    confidence = Column(Float, nullable=False)
    timestamp = Column(DateTime, nullable=False, index=True)
    created_at = Column(DateTime, default=datetime.utcnow)

class DataSync(Base):
    """Data synchronization tracking model."""
    __tablename__ = "data_sync"
    
    id = Column(Integer, primary_key=True, autoincrement=True)
    table_name = Column(String(100), nullable=False)
    record_id = Column(Integer, nullable=False)
    sync_status = Column(String(20), nullable=False)  # pending, synced, failed
    last_sync_attempt = Column(DateTime, nullable=True)
    sync_attempts = Column(Integer, default=0)
    error_message = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

class StorageService:
    """Local storage service for SmartWatts Edge Gateway."""
    
    def __init__(self, config: EdgeConfig):
        self.config = config
        self.storage_config = config.storage
        self.logger = logging.getLogger(__name__)
        
        # Database components
        self.engine = None
        self.SessionLocal = None
        self.db_path = None
        
        # Statistics
        self.stats = {
            "total_readings": 0,
            "total_devices": 0,
            "total_alerts": 0,
            "total_predictions": 0,
            "database_size_mb": 0,
            "last_backup": None
        }
    
    async def initialize(self):
        """Initialize the storage service."""
        self.logger.info("🚀 Initializing storage service...")
        
        try:
            # Get database URL
            db_url = self.config.get_database_url()
            self.db_path = db_url.replace("sqlite:///", "")
            
            # Ensure directory exists
            Path(self.db_path).parent.mkdir(parents=True, exist_ok=True)
            
            # Create SQLAlchemy engine
            self.engine = create_engine(
                db_url,
                echo=False,
                pool_pre_ping=True,
                connect_args={"check_same_thread": False}  # For SQLite
            )
            
            # Create session factory
            self.SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=self.engine)
            
            # Create all tables
            Base.metadata.create_all(bind=self.engine)
            
            # Initialize statistics
            await self._update_statistics()
            
            self.logger.info(f"✅ Storage service initialized with database: {self.db_path}")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to initialize storage service: {e}")
            raise
    
    async def close(self):
        """Close the storage service."""
        self.logger.info("🛑 Closing storage service...")
        
        if self.engine:
            self.engine.dispose()
        
        self.logger.info("✅ Storage service closed")
    
    def get_session(self):
        """Get a database session."""
        return self.SessionLocal()
    
    async def store_energy_reading(self, device_id: str, device_type: str, data: Dict[str, Any]) -> bool:
        """Store an energy reading."""
        try:
            with self.get_session() as session:
                reading = EnergyReading(
                    device_id=device_id,
                    device_type=device_type,
                    timestamp=data.get("timestamp", datetime.utcnow()),
                    power=data.get("power"),
                    voltage=data.get("voltage"),
                    current=data.get("current"),
                    frequency=data.get("frequency"),
                    power_factor=data.get("power_factor"),
                    energy=data.get("energy"),
                    temperature=data.get("temperature"),
                    raw_data=data.get("raw_data"),
                    quality=data.get("quality", "good")
                )
                
                session.add(reading)
                session.commit()
                
                # Mark for sync
                await self._mark_for_sync("energy_readings", reading.id)
                
                self.stats["total_readings"] += 1
                return True
                
        except Exception as e:
            self.logger.error(f"❌ Error storing energy reading: {e}")
            return False
    
    async def store_device_status(self, device_id: str, device_type: str, status: str, metadata: Dict[str, Any] = None) -> bool:
        """Store device status."""
        try:
            with self.get_session() as session:
                # Check if device exists
                existing = session.query(DeviceStatus).filter(
                    DeviceStatus.device_id == device_id
                ).first()
                
                if existing:
                    # Update existing
                    existing.status = status
                    existing.last_seen = datetime.utcnow()
                    existing.metadata = metadata or existing.metadata
                else:
                    # Create new
                    device_status = DeviceStatus(
                        device_id=device_id,
                        device_type=device_type,
                        status=status,
                        last_seen=datetime.utcnow(),
                        ip_address=metadata.get("ip_address") if metadata else None,
                        mac_address=metadata.get("mac_address") if metadata else None,
                        firmware_version=metadata.get("firmware_version") if metadata else None,
                        hardware_version=metadata.get("hardware_version") if metadata else None,
                        metadata=metadata
                    )
                    session.add(device_status)
                    self.stats["total_devices"] += 1
                
                session.commit()
                return True
                
        except Exception as e:
            self.logger.error(f"❌ Error storing device status: {e}")
            return False
    
    async def store_alert(self, device_id: str, alert_type: str, severity: str, message: str, metadata: Dict[str, Any] = None) -> bool:
        """Store an alert."""
        try:
            with self.get_session() as session:
                alert = Alert(
                    device_id=device_id,
                    alert_type=alert_type,
                    severity=severity,
                    message=message,
                    timestamp=datetime.utcnow(),
                    metadata=metadata
                )
                
                session.add(alert)
                session.commit()
                
                # Mark for sync
                await self._mark_for_sync("alerts", alert.id)
                
                self.stats["total_alerts"] += 1
                return True
                
        except Exception as e:
            self.logger.error(f"❌ Error storing alert: {e}")
            return False
    
    async def store_ai_prediction(self, model_name: str, prediction_type: str, input_data: Dict[str, Any], 
                                prediction_result: Dict[str, Any], confidence: float) -> bool:
        """Store AI prediction result."""
        try:
            with self.get_session() as session:
                prediction = AIPrediction(
                    model_name=model_name,
                    prediction_type=prediction_type,
                    input_data=input_data,
                    prediction_result=prediction_result,
                    confidence=confidence,
                    timestamp=datetime.utcnow()
                )
                
                session.add(prediction)
                session.commit()
                
                # Mark for sync
                await self._mark_for_sync("ai_predictions", prediction.id)
                
                self.stats["total_predictions"] += 1
                return True
                
        except Exception as e:
            self.logger.error(f"❌ Error storing AI prediction: {e}")
            return False
    
    async def get_energy_readings(self, device_id: str = None, start_time: datetime = None, 
                                end_time: datetime = None, limit: int = 1000) -> List[Dict[str, Any]]:
        """Get energy readings with optional filters."""
        try:
            with self.get_session() as session:
                query = session.query(EnergyReading)
                
                if device_id:
                    query = query.filter(EnergyReading.device_id == device_id)
                if start_time:
                    query = query.filter(EnergyReading.timestamp >= start_time)
                if end_time:
                    query = query.filter(EnergyReading.timestamp <= end_time)
                
                query = query.order_by(EnergyReading.timestamp.desc()).limit(limit)
                
                readings = query.all()
                
                return [{
                    "id": r.id,
                    "device_id": r.device_id,
                    "device_type": r.device_type,
                    "timestamp": r.timestamp.isoformat(),
                    "power": r.power,
                    "voltage": r.voltage,
                    "current": r.current,
                    "frequency": r.frequency,
                    "power_factor": r.power_factor,
                    "energy": r.energy,
                    "temperature": r.temperature,
                    "quality": r.quality,
                    "raw_data": r.raw_data
                } for r in readings]
                
        except Exception as e:
            self.logger.error(f"❌ Error getting energy readings: {e}")
            return []
    
    async def get_device_statuses(self) -> List[Dict[str, Any]]:
        """Get all device statuses."""
        try:
            with self.get_session() as session:
                devices = session.query(DeviceStatus).all()
                
                return [{
                    "device_id": d.device_id,
                    "device_type": d.device_type,
                    "status": d.status,
                    "last_seen": d.last_seen.isoformat(),
                    "ip_address": d.ip_address,
                    "mac_address": d.mac_address,
                    "firmware_version": d.firmware_version,
                    "hardware_version": d.hardware_version,
                    "metadata": d.metadata
                } for d in devices]
                
        except Exception as e:
            self.logger.error(f"❌ Error getting device statuses: {e}")
            return []
    
    async def get_alerts(self, device_id: str = None, severity: str = None, 
                        acknowledged: bool = None, limit: int = 100) -> List[Dict[str, Any]]:
        """Get alerts with optional filters."""
        try:
            with self.get_session() as session:
                query = session.query(Alert)
                
                if device_id:
                    query = query.filter(Alert.device_id == device_id)
                if severity:
                    query = query.filter(Alert.severity == severity)
                if acknowledged is not None:
                    query = query.filter(Alert.acknowledged == acknowledged)
                
                query = query.order_by(Alert.timestamp.desc()).limit(limit)
                
                alerts = query.all()
                
                return [{
                    "id": a.id,
                    "device_id": a.device_id,
                    "alert_type": a.alert_type,
                    "severity": a.severity,
                    "message": a.message,
                    "timestamp": a.timestamp.isoformat(),
                    "acknowledged": a.acknowledged,
                    "resolved": a.resolved,
                    "metadata": a.metadata
                } for a in alerts]
                
        except Exception as e:
            self.logger.error(f"❌ Error getting alerts: {e}")
            return []
    
    async def get_pending_sync_records(self, table_name: str = None, limit: int = 1000) -> List[Dict[str, Any]]:
        """Get records pending synchronization."""
        try:
            with self.get_session() as session:
                query = session.query(DataSync).filter(DataSync.sync_status == "pending")
                
                if table_name:
                    query = query.filter(DataSync.table_name == table_name)
                
                query = query.limit(limit)
                
                records = query.all()
                
                return [{
                    "id": r.id,
                    "table_name": r.table_name,
                    "record_id": r.record_id,
                    "sync_status": r.sync_status,
                    "last_sync_attempt": r.last_sync_attempt.isoformat() if r.last_sync_attempt else None,
                    "sync_attempts": r.sync_attempts,
                    "error_message": r.error_message
                } for r in records]
                
        except Exception as e:
            self.logger.error(f"❌ Error getting pending sync records: {e}")
            return []
    
    async def mark_sync_completed(self, sync_id: int) -> bool:
        """Mark a sync record as completed."""
        try:
            with self.get_session() as session:
                sync_record = session.query(DataSync).filter(DataSync.id == sync_id).first()
                if sync_record:
                    sync_record.sync_status = "synced"
                    sync_record.last_sync_attempt = datetime.utcnow()
                    session.commit()
                    return True
            return False
        except Exception as e:
            self.logger.error(f"❌ Error marking sync completed: {e}")
            return False
    
    async def mark_sync_failed(self, sync_id: int, error_message: str) -> bool:
        """Mark a sync record as failed."""
        try:
            with self.get_session() as session:
                sync_record = session.query(DataSync).filter(DataSync.id == sync_id).first()
                if sync_record:
                    sync_record.sync_status = "failed"
                    sync_record.last_sync_attempt = datetime.utcnow()
                    sync_record.sync_attempts += 1
                    sync_record.error_message = error_message
                    session.commit()
                    return True
            return False
        except Exception as e:
            self.logger.error(f"❌ Error marking sync failed: {e}")
            return False
    
    async def _mark_for_sync(self, table_name: str, record_id: int):
        """Mark a record for synchronization."""
        try:
            with self.get_session() as session:
                sync_record = DataSync(
                    table_name=table_name,
                    record_id=record_id,
                    sync_status="pending"
                )
                session.add(sync_record)
                session.commit()
        except Exception as e:
            self.logger.error(f"❌ Error marking record for sync: {e}")
    
    async def cleanup_old_data(self, days: int = None):
        """Clean up old data based on retention policy."""
        if days is None:
            days = self.storage_config.max_data_age_days
        
        cutoff_date = datetime.utcnow() - timedelta(days=days)
        
        try:
            with self.get_session() as session:
                # Clean up old energy readings
                deleted_readings = session.query(EnergyReading).filter(
                    EnergyReading.timestamp < cutoff_date
                ).delete()
                
                # Clean up old alerts (keep critical ones longer)
                deleted_alerts = session.query(Alert).filter(
                    Alert.timestamp < cutoff_date,
                    Alert.severity != "critical"
                ).delete()
                
                # Clean up old AI predictions
                deleted_predictions = session.query(AIPrediction).filter(
                    AIPrediction.timestamp < cutoff_date
                ).delete()
                
                session.commit()
                
                self.logger.info(f"🧹 Cleaned up old data: {deleted_readings} readings, {deleted_alerts} alerts, {deleted_predictions} predictions")
                
        except Exception as e:
            self.logger.error(f"❌ Error cleaning up old data: {e}")
    
    async def create_backup(self) -> Optional[str]:
        """Create a database backup."""
        if not self.storage_config.backup_enabled:
            return None
        
        try:
            backup_path = f"{self.db_path}.backup.{datetime.now().strftime('%Y%m%d_%H%M%S')}"
            
            # Simple file copy for SQLite
            import shutil
            shutil.copy2(self.db_path, backup_path)
            
            # Compress if enabled
            if self.storage_config.compression_enabled:
                compressed_path = f"{backup_path}.gz"
                with open(backup_path, 'rb') as f_in:
                    with gzip.open(compressed_path, 'wb') as f_out:
                        shutil.copyfileobj(f_in, f_out)
                Path(backup_path).unlink()  # Remove uncompressed file
                backup_path = compressed_path
            
            self.stats["last_backup"] = datetime.utcnow()
            self.logger.info(f"💾 Database backup created: {backup_path}")
            return backup_path
            
        except Exception as e:
            self.logger.error(f"❌ Error creating backup: {e}")
            return None
    
    async def _update_statistics(self):
        """Update storage statistics."""
        try:
            with self.get_session() as session:
                self.stats["total_readings"] = session.query(EnergyReading).count()
                self.stats["total_devices"] = session.query(DeviceStatus).count()
                self.stats["total_alerts"] = session.query(Alert).count()
                self.stats["total_predictions"] = session.query(AIPrediction).count()
                
                # Database size
                if self.db_path and Path(self.db_path).exists():
                    self.stats["database_size_mb"] = Path(self.db_path).stat().st_size / (1024 * 1024)
                
        except Exception as e:
            self.logger.error(f"❌ Error updating statistics: {e}")
    
    def is_connected(self) -> bool:
        """Check if storage service is connected."""
        return self.engine is not None
    
    def get_stats(self) -> Dict[str, Any]:
        """Get storage service statistics."""
        return self.stats.copy()
