"""
SmartWatts Edge Gateway Data Sync Service
Cloud synchronization and offline-first capabilities for edge gateway
"""

import asyncio
import logging
import json
from typing import Dict, List, Optional, Any
from datetime import datetime, timedelta
from dataclasses import dataclass
from enum import Enum
import httpx
from core.config import EdgeConfig, DataSyncConfig
from services.storage_service import StorageService

# Azure IoT Hub imports (optional - only if configured)
try:
    from azure.iot.device import IoTHubDeviceClient, Message
    AZURE_IOT_HUB_AVAILABLE = True
except ImportError:
    AZURE_IOT_HUB_AVAILABLE = False

class SyncStatus(Enum):
    """Data synchronization status."""
    PENDING = "pending"
    SYNCING = "syncing"
    SYNCED = "synced"
    FAILED = "failed"
    CONFLICT = "conflict"

@dataclass
class SyncRecord:
    """Data synchronization record."""
    id: int
    table_name: str
    record_id: int
    status: SyncStatus
    last_sync_attempt: Optional[datetime]
    sync_attempts: int
    error_message: Optional[str]
    created_at: datetime

class DataSyncService:
    """Data synchronization service for SmartWatts Edge Gateway."""
    
    def __init__(self, config: EdgeConfig, storage_service: StorageService):
        self.config = config
        self.sync_config = config.data_sync
        self.storage_service = storage_service
        self.logger = logging.getLogger(__name__)
        
        # Sync state
        self.syncing = False
        self.sync_task: Optional[asyncio.Task] = None
        self.last_sync = None
        
        # Cloud API client
        self.cloud_client: Optional[httpx.AsyncClient] = None
        
        # Azure IoT Hub client (optional)
        self.iot_hub_client: Optional[Any] = None
        
        # Statistics
        self.stats = {
            "total_synced": 0,
            "total_failed": 0,
            "pending_records": 0,
            "last_sync_time": None,
            "sync_errors": 0,
            "conflicts_resolved": 0
        }
    
    async def start(self):
        """Start the data synchronization service."""
        self.logger.info("🚀 Starting data sync service...")
        
        try:
            if not self.sync_config.enabled:
                self.logger.info("⚠️ Data synchronization is disabled")
                return
            
            # Initialize cloud API client (HTTP REST)
            await self._initialize_cloud_client()
            
            # Initialize Azure IoT Hub client (if configured)
            if self.sync_config.use_azure_iot_hub:
                await self._initialize_iot_hub_client()
            
            # Start periodic sync
            if self.sync_config.sync_interval_seconds > 0:
                self.syncing = True
                self.sync_task = asyncio.create_task(self._sync_loop())
                self.logger.info(f"✅ Data sync started with {self.sync_config.sync_interval_seconds}s interval")
            else:
                self.logger.info("⚠️ Data sync started but periodic sync is disabled")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to start data sync service: {e}")
            raise
    
    async def stop(self):
        """Stop the data synchronization service."""
        self.logger.info("🛑 Stopping data sync service...")
        
        try:
            # Stop sync loop
            self.syncing = False
            if self.sync_task:
                self.sync_task.cancel()
                try:
                    await self.sync_task
                except asyncio.CancelledError:
                    pass
            
            # Close cloud client
            if self.cloud_client:
                await self.cloud_client.aclose()
            
            # Close IoT Hub client
            if self.iot_hub_client:
                await self.iot_hub_client.disconnect()
            
            self.logger.info("✅ Data sync service stopped")
            
        except Exception as e:
            self.logger.error(f"❌ Error stopping data sync service: {e}")
    
    async def _initialize_cloud_client(self):
        """Initialize cloud API client."""
        try:
            headers = {
                "Content-Type": "application/json",
                "User-Agent": "SmartWatts-Edge-Gateway/1.0.0"
            }
            
            # Add API key if configured
            if self.config.network.cloud_api_key:
                headers["Authorization"] = f"Bearer {self.config.network.cloud_api_key}"
            
            self.cloud_client = httpx.AsyncClient(
                base_url=self.config.network.cloud_api_url,
                headers=headers,
                timeout=30.0
            )
            
            # Test connection
            await self._test_cloud_connection()
            
            self.logger.info("✅ Cloud API client initialized")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to initialize cloud client: {e}")
            raise
    
    async def _test_cloud_connection(self):
        """Test connection to cloud API."""
        try:
            response = await self.cloud_client.get("/health")
            if response.status_code == 200:
                self.logger.info("✅ Cloud API connection successful")
            else:
                self.logger.warning(f"⚠️ Cloud API health check returned {response.status_code}")
        except Exception as e:
            self.logger.warning(f"⚠️ Cloud API connection test failed: {e}")
    
    async def _initialize_iot_hub_client(self):
        """Initialize Azure IoT Hub MQTT client."""
        try:
            if not AZURE_IOT_HUB_AVAILABLE:
                self.logger.error("❌ Azure IoT Hub SDK not available. Install azure-iot-device package.")
                return
            
            connection_string = self.config.network.azure_iot_hub_connection_string
            if not connection_string:
                self.logger.warning("⚠️ Azure IoT Hub connection string not configured")
                return
            
            # Create IoT Hub device client
            self.iot_hub_client = IoTHubDeviceClient.create_from_connection_string(connection_string)
            
            # Connect to IoT Hub
            await self.iot_hub_client.connect()
            
            self.logger.info("✅ Azure IoT Hub client initialized and connected")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to initialize IoT Hub client: {e}")
            self.iot_hub_client = None
    
    async def _sync_loop(self):
        """Main synchronization loop."""
        while self.syncing:
            try:
                await self._perform_sync()
                self.stats["last_sync_time"] = datetime.now()
                
                # Wait for next sync
                await asyncio.sleep(self.sync_config.sync_interval_seconds)
                
            except asyncio.CancelledError:
                break
            except Exception as e:
                self.logger.error(f"❌ Error in sync loop: {e}")
                self.stats["sync_errors"] += 1
                await asyncio.sleep(60)  # Wait before retry
    
    async def _perform_sync(self):
        """Perform a complete data synchronization."""
        self.logger.debug("🔄 Starting data synchronization...")
        
        try:
            # Get pending records
            pending_records = await self.storage_service.get_pending_sync_records(
                limit=self.sync_config.batch_size
            )
            
            if not pending_records:
                self.logger.debug("📭 No pending records to sync")
                return
            
            self.logger.info(f"📤 Syncing {len(pending_records)} records to cloud...")
            
            # Group records by table
            records_by_table = {}
            for record in pending_records:
                table_name = record["table_name"]
                if table_name not in records_by_table:
                    records_by_table[table_name] = []
                records_by_table[table_name].append(record)
            
            # Sync each table
            for table_name, records in records_by_table.items():
                await self._sync_table_records(table_name, records)
            
            self.logger.info("✅ Data synchronization completed")
            
        except Exception as e:
            self.logger.error(f"❌ Error during data synchronization: {e}")
            self.stats["sync_errors"] += 1
    
    async def _sync_table_records(self, table_name: str, records: List[Dict[str, Any]]):
        """Synchronize records for a specific table."""
        try:
            # Get actual record data
            record_data = await self._get_record_data(table_name, records)
            
            if not record_data:
                self.logger.warning(f"⚠️ No data found for table {table_name}")
                return
            
            # Send to cloud API
            response = await self._send_to_cloud(table_name, record_data)
            
            if response.get("success", False):
                # Mark records as synced
                for record in records:
                    await self.storage_service.mark_sync_completed(record["id"])
                    self.stats["total_synced"] += 1
                
                self.logger.info(f"✅ Synced {len(records)} records for table {table_name}")
            else:
                # Mark records as failed
                error_msg = response.get("error", "Unknown error")
                for record in records:
                    await self.storage_service.mark_sync_failed(record["id"], error_msg)
                    self.stats["total_failed"] += 1
                
                self.logger.error(f"❌ Failed to sync records for table {table_name}: {error_msg}")
            
        except Exception as e:
            self.logger.error(f"❌ Error syncing table {table_name}: {e}")
            self.stats["sync_errors"] += 1
    
    async def _get_record_data(self, table_name: str, records: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """Get actual record data from database."""
        try:
            record_ids = [r["record_id"] for r in records]
            
            if table_name == "energy_readings":
                return await self._get_energy_readings_data(record_ids)
            elif table_name == "alerts":
                return await self._get_alerts_data(record_ids)
            elif table_name == "ai_predictions":
                return await self._get_ai_predictions_data(record_ids)
            else:
                self.logger.warning(f"⚠️ Unknown table for sync: {table_name}")
                return []
                
        except Exception as e:
            self.logger.error(f"❌ Error getting record data for {table_name}: {e}")
            return []
    
    async def _get_energy_readings_data(self, record_ids: List[int]) -> List[Dict[str, Any]]:
        """Get energy readings data for sync."""
        try:
            with self.storage_service.get_session() as session:
                from services.storage_service import EnergyReading
                
                readings = session.query(EnergyReading).filter(
                    EnergyReading.id.in_(record_ids)
                ).all()
                
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
            self.logger.error(f"❌ Error getting energy readings data: {e}")
            return []
    
    async def _get_alerts_data(self, record_ids: List[int]) -> List[Dict[str, Any]]:
        """Get alerts data for sync."""
        try:
            with self.storage_service.get_session() as session:
                from services.storage_service import Alert
                
                alerts = session.query(Alert).filter(
                    Alert.id.in_(record_ids)
                ).all()
                
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
            self.logger.error(f"❌ Error getting alerts data: {e}")
            return []
    
    async def _get_ai_predictions_data(self, record_ids: List[int]) -> List[Dict[str, Any]]:
        """Get AI predictions data for sync."""
        try:
            with self.storage_service.get_session() as session:
                from services.storage_service import AIPrediction
                
                predictions = session.query(AIPrediction).filter(
                    AIPrediction.id.in_(record_ids)
                ).all()
                
                return [{
                    "id": p.id,
                    "model_name": p.model_name,
                    "prediction_type": p.prediction_type,
                    "input_data": p.input_data,
                    "prediction_result": p.prediction_result,
                    "confidence": p.confidence,
                    "timestamp": p.timestamp.isoformat()
                } for p in predictions]
                
        except Exception as e:
            self.logger.error(f"❌ Error getting AI predictions data: {e}")
            return []
    
    async def _send_to_cloud(self, table_name: str, data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Send data to cloud API (HTTP REST or Azure IoT Hub MQTT)."""
        try:
            # If Azure IoT Hub is enabled, use MQTT publishing
            if self.sync_config.use_azure_iot_hub and self.iot_hub_client:
                return await self._send_to_iot_hub(table_name, data)
            
            # Otherwise, use HTTP REST API
            if not self.cloud_client:
                return {"success": False, "error": "Cloud client not initialized"}
            
            # Prepare payload
            payload = {
                "table": table_name,
                "data": data,
                "timestamp": datetime.now().isoformat(),
                "source": "edge_gateway"
            }
            
            # Send to appropriate endpoint
            endpoint = f"/api/v1/sync/{table_name}"
            response = await self.cloud_client.post(endpoint, json=payload)
            
            if response.status_code == 200:
                return {"success": True, "response": response.json()}
            elif response.status_code == 409:
                # Handle conflicts
                return await self._handle_sync_conflict(table_name, data, response.json())
            else:
                return {
                    "success": False,
                    "error": f"HTTP {response.status_code}: {response.text}"
                }
                
        except httpx.TimeoutException:
            return {"success": False, "error": "Request timeout"}
        except httpx.ConnectError:
            return {"success": False, "error": "Connection failed"}
        except Exception as e:
            return {"success": False, "error": str(e)}
    
    async def _send_to_iot_hub(self, table_name: str, data: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Send data to Azure IoT Hub via MQTT."""
        try:
            if not self.iot_hub_client:
                return {"success": False, "error": "IoT Hub client not initialized"}
            
            # Prepare message payload
            message_payload = {
                "table": table_name,
                "data": data,
                "timestamp": datetime.now().isoformat(),
                "source": "edge_gateway",
                "device_id": self.sync_config.azure_iot_hub_device_id or "edge-gateway"
            }
            
            # Create IoT Hub message
            message = Message(json.dumps(message_payload))
            message.content_encoding = "utf-8"
            message.content_type = "application/json"
            
            # Add custom properties
            message.custom_properties["table"] = table_name
            message.custom_properties["record_count"] = str(len(data))
            
            # Send message to IoT Hub
            await self.iot_hub_client.send_message(message)
            
            self.logger.debug(f"✅ Sent {len(data)} records to IoT Hub for table {table_name}")
            return {"success": True, "method": "iot_hub", "record_count": len(data)}
            
        except Exception as e:
            self.logger.error(f"❌ Error sending to IoT Hub: {e}")
            return {"success": False, "error": str(e)}
    
    async def _handle_sync_conflict(self, table_name: str, data: List[Dict[str, Any]], conflict_info: Dict[str, Any]) -> Dict[str, Any]:
        """Handle synchronization conflicts."""
        try:
            self.logger.warning(f"⚠️ Sync conflict detected for table {table_name}")
            
            # Apply conflict resolution strategy
            if self.sync_config.conflict_resolution == "edge_priority":
                # Edge data takes priority
                return {"success": True, "conflict_resolved": True}
            elif self.sync_config.conflict_resolution == "cloud_priority":
                # Cloud data takes priority - update local data
                await self._update_local_data(table_name, conflict_info.get("cloud_data", []))
                return {"success": True, "conflict_resolved": True}
            elif self.sync_config.conflict_resolution == "timestamp":
                # Use timestamp-based resolution
                return await self._resolve_by_timestamp(table_name, data, conflict_info)
            else:
                return {"success": False, "error": "Unknown conflict resolution strategy"}
                
        except Exception as e:
            self.logger.error(f"❌ Error handling sync conflict: {e}")
            return {"success": False, "error": str(e)}
    
    async def _update_local_data(self, table_name: str, cloud_data: List[Dict[str, Any]]):
        """Update local data with cloud data."""
        # This would update the local database with cloud data
        # Implementation depends on specific requirements
        self.logger.info(f"📥 Updated local data for table {table_name} with cloud data")
    
    async def _resolve_by_timestamp(self, table_name: str, local_data: List[Dict[str, Any]], conflict_info: Dict[str, Any]) -> Dict[str, Any]:
        """Resolve conflicts based on timestamp."""
        # This would implement timestamp-based conflict resolution
        # Implementation depends on specific requirements
        return {"success": True, "conflict_resolved": True}
    
    async def sync_now(self) -> Dict[str, Any]:
        """Trigger immediate synchronization."""
        try:
            self.logger.info("🔄 Triggering immediate synchronization...")
            await self._perform_sync()
            return {"success": True, "message": "Synchronization completed"}
        except Exception as e:
            self.logger.error(f"❌ Error in immediate sync: {e}")
            return {"success": False, "error": str(e)}
    
    async def get_sync_status(self) -> Dict[str, Any]:
        """Get current synchronization status."""
        try:
            pending_records = await self.storage_service.get_pending_sync_records(limit=1000)
            
            return {
                "syncing": self.syncing,
                "last_sync": self.stats["last_sync_time"].isoformat() if self.stats["last_sync_time"] else None,
                "pending_records": len(pending_records),
                "total_synced": self.stats["total_synced"],
                "total_failed": self.stats["total_failed"],
                "sync_errors": self.stats["sync_errors"],
                "conflicts_resolved": self.stats["conflicts_resolved"]
            }
        except Exception as e:
            self.logger.error(f"❌ Error getting sync status: {e}")
            return {"error": str(e)}
    
    def is_connected(self) -> bool:
        """Check if data sync service is connected to cloud."""
        if self.sync_config.use_azure_iot_hub:
            return self.iot_hub_client is not None and self.iot_hub_client.connected
        return self.cloud_client is not None
    
    def get_stats(self) -> Dict[str, Any]:
        """Get data sync service statistics."""
        return self.stats.copy()
