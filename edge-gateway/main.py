#!/usr/bin/env python3
"""
SmartWatts Edge Gateway - Main Application
Complete edge gateway for R501 RK3588 with MQTT, Modbus, AI inference, and local storage
"""

import asyncio
import logging
import signal
import sys
from pathlib import Path
from typing import Optional

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from core.config import EdgeConfig
from core.logger import setup_logging
from services.mqtt_service import MQTTService
from services.modbus_service import ModbusService
from services.storage_service import StorageService
from services.device_discovery import DeviceDiscoveryService
from services.ai_inference import AIInferenceService
from services.data_sync import DataSyncService
from api.routes import router as api_router
from core.monitoring import MetricsCollector

# Global services
mqtt_service: Optional[MQTTService] = None
modbus_service: Optional[ModbusService] = None
storage_service: Optional[StorageService] = None
device_discovery: Optional[DeviceDiscoveryService] = None
ai_inference: Optional[AIInferenceService] = None
data_sync: Optional[DataSyncService] = None
metrics_collector: Optional[MetricsCollector] = None

class SmartWattsEdgeGateway:
    """Main SmartWatts Edge Gateway application."""
    
    def __init__(self, config_path: str = "config/edge-config.yml"):
        self.config = EdgeConfig(config_path)
        self.app = FastAPI(
            title="SmartWatts Edge Gateway",
            description="Complete edge gateway for energy monitoring and AI inference",
            version="1.0.0"
        )
        self.running = False
        
        # Setup logging
        setup_logging(self.config.log_level)
        self.logger = logging.getLogger(__name__)
        
        # Setup FastAPI
        self._setup_fastapi()
        
    def _setup_fastapi(self):
        """Setup FastAPI application with middleware and routes."""
        # CORS middleware
        self.app.add_middleware(
            CORSMiddleware,
            allow_origins=["*"],
            allow_credentials=True,
            allow_methods=["*"],
            allow_headers=["*"],
        )
        
        # Include API routes
        self.app.include_router(api_router, prefix="/api/v1")
        
        # Health check endpoint
        @self.app.get("/health")
        async def health_check():
            return {
                "status": "healthy",
                "gateway": "SmartWatts Edge Gateway",
                "version": "1.0.0",
                "hardware": self.config.hardware.device_type,
                "services": {
                    "mqtt": mqtt_service.is_connected() if mqtt_service else False,
                    "modbus": modbus_service.is_connected() if modbus_service else False,
                    "storage": storage_service.is_connected() if storage_service else False,
                    "ai_inference": ai_inference.is_ready() if ai_inference else False,
                    "device_discovery": device_discovery.is_running() if device_discovery else False,
                    "data_sync": data_sync.is_connected() if data_sync else False
                }
            }
    
    async def initialize_services(self):
        """Initialize all edge gateway services."""
        self.logger.info("🚀 Initializing SmartWatts Edge Gateway services...")
        
        global mqtt_service, modbus_service, storage_service, device_discovery, ai_inference, data_sync, metrics_collector
        
        try:
            # Initialize storage service first
            self.logger.info("💾 Initializing local storage...")
            storage_service = StorageService(self.config)
            await storage_service.initialize()
            
            # Initialize MQTT service
            self.logger.info("📡 Initializing MQTT broker...")
            mqtt_service = MQTTService(self.config)
            await mqtt_service.start()
            
            # Initialize Modbus service
            self.logger.info("🔌 Initializing Modbus service...")
            modbus_service = ModbusService(self.config)
            await modbus_service.start()
            
            # Initialize AI inference service
            self.logger.info("🤖 Initializing AI inference...")
            ai_inference = AIInferenceService(self.config)
            await ai_inference.initialize()
            
            # Initialize device discovery
            self.logger.info("🔍 Initializing device discovery...")
            device_discovery = DeviceDiscoveryService(self.config, mqtt_service, modbus_service)
            await device_discovery.start()
            
            # Initialize data synchronization
            self.logger.info("☁️ Initializing cloud sync...")
            data_sync = DataSyncService(self.config, storage_service)
            await data_sync.start()
            
            # Initialize metrics collection
            self.logger.info("📊 Initializing metrics collection...")
            metrics_collector = MetricsCollector(self.config)
            await metrics_collector.start()
            
            self.logger.info("✅ All services initialized successfully!")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to initialize services: {e}")
            raise
    
    async def start(self):
        """Start the edge gateway."""
        self.logger.info("🚀 Starting SmartWatts Edge Gateway...")
        
        try:
            # Initialize all services
            await self.initialize_services()
            
            # Start the FastAPI server
            config = uvicorn.Config(
                self.app,
                host=self.config.network.host,
                port=self.config.network.port,
                log_level="info"
            )
            server = uvicorn.Server(config)
            
            self.running = True
            self.logger.info(f"✅ SmartWatts Edge Gateway started on {self.config.network.host}:{self.config.network.port}")
            
            # Run the server
            await server.serve()
            
        except Exception as e:
            self.logger.error(f"❌ Failed to start edge gateway: {e}")
            raise
    
    async def stop(self):
        """Stop the edge gateway gracefully."""
        self.logger.info("🛑 Stopping SmartWatts Edge Gateway...")
        
        self.running = False
        
        # Stop all services
        if data_sync:
            await data_sync.stop()
        if device_discovery:
            await device_discovery.stop()
        if ai_inference:
            await ai_inference.stop()
        if modbus_service:
            await modbus_service.stop()
        if mqtt_service:
            await mqtt_service.stop()
        if storage_service:
            await storage_service.close()
        if metrics_collector:
            await metrics_collector.stop()
        
        self.logger.info("✅ SmartWatts Edge Gateway stopped")

# Global gateway instance
gateway: Optional[SmartWattsEdgeGateway] = None

async def main():
    """Main entry point."""
    global gateway
    
    # Create gateway instance
    gateway = SmartWattsEdgeGateway()
    
    # Setup signal handlers for graceful shutdown
    def signal_handler(signum, frame):
        print(f"\n🛑 Received signal {signum}, shutting down gracefully...")
        if gateway:
            asyncio.create_task(gateway.stop())
    
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    try:
        # Start the gateway
        await gateway.start()
    except KeyboardInterrupt:
        print("\n🛑 Keyboard interrupt received, shutting down...")
    except Exception as e:
        print(f"❌ Fatal error: {e}")
        sys.exit(1)
    finally:
        if gateway:
            await gateway.stop()

if __name__ == "__main__":
    asyncio.run(main())
