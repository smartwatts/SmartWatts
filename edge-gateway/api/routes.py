"""
SmartWatts Edge Gateway API Routes
REST API endpoints for edge gateway management and data access
"""

import asyncio
import logging
from typing import Dict, List, Optional, Any
from datetime import datetime, timedelta
from fastapi import APIRouter, HTTPException, Query, Depends
from fastapi.responses import JSONResponse
from pydantic import BaseModel

# Import services (these would be injected in a real implementation)
from services.mqtt_service import mqtt_service
from services.modbus_service import modbus_service
from services.storage_service import storage_service
from services.device_discovery import device_discovery
from services.ai_inference import ai_inference
from services.data_sync import data_sync

router = APIRouter()

# Pydantic models for request/response
class EnergyReadingRequest(BaseModel):
    device_id: str
    device_type: str
    power: Optional[float] = None
    voltage: Optional[float] = None
    current: Optional[float] = None
    frequency: Optional[float] = None
    power_factor: Optional[float] = None
    energy: Optional[float] = None
    temperature: Optional[float] = None
    quality: str = "good"

class DeviceStatusRequest(BaseModel):
    device_id: str
    device_type: str
    status: str
    ip_address: Optional[str] = None
    mac_address: Optional[str] = None
    firmware_version: Optional[str] = None
    hardware_version: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None

class AlertRequest(BaseModel):
    device_id: str
    alert_type: str
    severity: str
    message: str
    metadata: Optional[Dict[str, Any]] = None

class AIInferenceRequest(BaseModel):
    model_name: str
    prediction_type: str
    input_data: Dict[str, Any]

# Health and Status Endpoints
@router.get("/health")
async def health_check():
    """Get edge gateway health status."""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "version": "1.0.0",
        "services": {
            "mqtt": mqtt_service.is_connected() if mqtt_service else False,
            "modbus": modbus_service.is_connected() if modbus_service else False,
            "storage": storage_service.is_connected() if storage_service else False,
            "device_discovery": device_discovery.is_running() if device_discovery else False,
            "ai_inference": ai_inference.is_ready() if ai_inference else False,
            "data_sync": data_sync.is_connected() if data_sync else False
        }
    }

@router.get("/status")
async def get_status():
    """Get detailed status of all services."""
    return {
        "timestamp": datetime.now().isoformat(),
        "mqtt": mqtt_service.get_stats() if mqtt_service else {},
        "modbus": modbus_service.get_stats() if modbus_service else {},
        "storage": storage_service.get_stats() if storage_service else {},
        "device_discovery": device_discovery.get_stats() if device_discovery else {},
        "ai_inference": ai_inference.get_stats() if ai_inference else {},
        "data_sync": data_sync.get_stats() if data_sync else {}
    }

# Energy Data Endpoints
@router.post("/energy/readings")
async def store_energy_reading(reading: EnergyReadingRequest):
    """Store an energy reading."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        data = reading.dict()
        data["timestamp"] = datetime.now()
        
        success = await storage_service.store_energy_reading(
            reading.device_id,
            reading.device_type,
            data
        )
        
        if success:
            return {"status": "success", "message": "Energy reading stored"}
        else:
            raise HTTPException(status_code=500, detail="Failed to store energy reading")
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/energy/readings")
async def get_energy_readings(
    device_id: Optional[str] = Query(None),
    start_time: Optional[datetime] = Query(None),
    end_time: Optional[datetime] = Query(None),
    limit: int = Query(1000, le=10000)
):
    """Get energy readings with optional filters."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        readings = await storage_service.get_energy_readings(
            device_id=device_id,
            start_time=start_time,
            end_time=end_time,
            limit=limit
        )
        
        return {
            "readings": readings,
            "count": len(readings),
            "filters": {
                "device_id": device_id,
                "start_time": start_time.isoformat() if start_time else None,
                "end_time": end_time.isoformat() if end_time else None,
                "limit": limit
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Device Management Endpoints
@router.post("/devices/status")
async def update_device_status(status: DeviceStatusRequest):
    """Update device status."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        success = await storage_service.store_device_status(
            status.device_id,
            status.device_type,
            status.status,
            status.metadata
        )
        
        if success:
            return {"status": "success", "message": "Device status updated"}
        else:
            raise HTTPException(status_code=500, detail="Failed to update device status")
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/devices")
async def get_devices():
    """Get all devices."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        devices = await storage_service.get_device_statuses()
        
        return {
            "devices": devices,
            "count": len(devices)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/devices/discovered")
async def get_discovered_devices():
    """Get discovered devices."""
    try:
        if not device_discovery:
            raise HTTPException(status_code=503, detail="Device discovery service not available")
        
        devices = device_discovery.get_discovered_devices()
        
        return {
            "devices": devices,
            "count": len(devices)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Alert Management Endpoints
@router.post("/alerts")
async def create_alert(alert: AlertRequest):
    """Create an alert."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        success = await storage_service.store_alert(
            alert.device_id,
            alert.alert_type,
            alert.severity,
            alert.message,
            alert.metadata
        )
        
        if success:
            return {"status": "success", "message": "Alert created"}
        else:
            raise HTTPException(status_code=500, detail="Failed to create alert")
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/alerts")
async def get_alerts(
    device_id: Optional[str] = Query(None),
    severity: Optional[str] = Query(None),
    acknowledged: Optional[bool] = Query(None),
    limit: int = Query(100, le=1000)
):
    """Get alerts with optional filters."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        alerts = await storage_service.get_alerts(
            device_id=device_id,
            severity=severity,
            acknowledged=acknowledged,
            limit=limit
        )
        
        return {
            "alerts": alerts,
            "count": len(alerts),
            "filters": {
                "device_id": device_id,
                "severity": severity,
                "acknowledged": acknowledged,
                "limit": limit
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# MQTT Management Endpoints
@router.post("/mqtt/publish")
async def publish_mqtt_message(
    topic: str,
    message: Dict[str, Any],
    qos: int = 1,
    retain: bool = False
):
    """Publish a message to MQTT topic."""
    try:
        if not mqtt_service:
            raise HTTPException(status_code=503, detail="MQTT service not available")
        
        success = await mqtt_service.publish(topic, message, qos, retain)
        
        if success:
            return {"status": "success", "message": "Message published"}
        else:
            raise HTTPException(status_code=500, detail="Failed to publish message")
            
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/mqtt/stats")
async def get_mqtt_stats():
    """Get MQTT service statistics."""
    try:
        if not mqtt_service:
            raise HTTPException(status_code=503, detail="MQTT service not available")
        
        return mqtt_service.get_stats()
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Modbus Management Endpoints
@router.get("/modbus/devices")
async def get_modbus_devices():
    """Get Modbus devices."""
    try:
        if not modbus_service:
            raise HTTPException(status_code=503, detail="Modbus service not available")
        
        devices = modbus_service.get_connected_devices()
        device_info = []
        
        for device_name in devices:
            info = modbus_service.get_device_info(device_name)
            if info:
                device_info.append(info)
        
        return {
            "devices": device_info,
            "count": len(device_info)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/modbus/read/{device_name}")
async def read_modbus_device(device_name: str):
    """Read data from a Modbus device."""
    try:
        if not modbus_service:
            raise HTTPException(status_code=503, detail="Modbus service not available")
        
        readings = await modbus_service.read_device(device_name)
        
        return {
            "device_name": device_name,
            "readings": [
                {
                    "register_address": r.register_address,
                    "value": r.value,
                    "unit": r.unit,
                    "timestamp": r.timestamp.isoformat(),
                    "quality": r.quality
                } for r in readings
            ],
            "count": len(readings)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# AI Inference Endpoints
@router.post("/ai/inference")
async def run_ai_inference(request: AIInferenceRequest):
    """Run AI inference."""
    try:
        if not ai_inference:
            raise HTTPException(status_code=503, detail="AI inference service not available")
        
        if request.model_name == "energy_forecast":
            result = await ai_inference.predict_energy_forecast(request.input_data)
        elif request.model_name == "anomaly_detection":
            result = await ai_inference.detect_anomaly(request.input_data)
        elif request.model_name == "load_prediction":
            result = await ai_inference.predict_load(request.input_data)
        elif request.model_name == "efficiency_optimization":
            result = await ai_inference.optimize_efficiency(request.input_data)
        else:
            raise HTTPException(status_code=400, detail="Unknown model name")
        
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/ai/models")
async def get_ai_models():
    """Get available AI models."""
    try:
        if not ai_inference:
            raise HTTPException(status_code=503, detail="AI inference service not available")
        
        models = []
        for model_name in ai_inference.get_loaded_models():
            model_info = ai_inference.get_model_info(model_name)
            if model_info:
                models.append(model_info)
        
        return {
            "models": models,
            "count": len(models)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Data Sync Endpoints
@router.post("/sync/trigger")
async def trigger_sync():
    """Trigger immediate data synchronization."""
    try:
        if not data_sync:
            raise HTTPException(status_code=503, detail="Data sync service not available")
        
        result = await data_sync.sync_now()
        return result
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/sync/status")
async def get_sync_status():
    """Get data synchronization status."""
    try:
        if not data_sync:
            raise HTTPException(status_code=503, detail="Data sync service not available")
        
        status = await data_sync.get_sync_status()
        return status
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Configuration Endpoints
@router.get("/config")
async def get_configuration():
    """Get current configuration."""
    try:
        # This would return the current configuration
        # Implementation depends on how config is exposed
        return {"message": "Configuration endpoint not implemented"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Utility Endpoints
@router.get("/metrics")
async def get_metrics():
    """Get Prometheus-style metrics."""
    try:
        # This would return metrics in Prometheus format
        # Implementation depends on metrics collection
        return {"message": "Metrics endpoint not implemented"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/maintenance/cleanup")
async def cleanup_old_data(days: int = 30):
    """Clean up old data."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        await storage_service.cleanup_old_data(days)
        
        return {"status": "success", "message": f"Cleaned up data older than {days} days"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/maintenance/backup")
async def create_backup():
    """Create a database backup."""
    try:
        if not storage_service:
            raise HTTPException(status_code=503, detail="Storage service not available")
        
        backup_path = await storage_service.create_backup()
        
        if backup_path:
            return {"status": "success", "backup_path": backup_path}
        else:
            return {"status": "warning", "message": "Backup creation skipped or failed"}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
