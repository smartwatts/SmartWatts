"""
SmartWatts Edge Gateway MQTT Service
Complete MQTT broker and client implementation for device communication
"""

import asyncio
import json
import logging
from typing import Dict, List, Callable, Optional, Any
from datetime import datetime
import paho.mqtt.client as mqtt
from asyncio_mqtt import Client as AsyncMQTTClient
from core.config import EdgeConfig, MQTTConfig

class MQTTService:
    """MQTT broker and client service for SmartWatts Edge Gateway."""
    
    def __init__(self, config: EdgeConfig):
        self.config = config
        self.mqtt_config = config.mqtt
        self.logger = logging.getLogger(__name__)
        
        # MQTT client
        self.client: Optional[AsyncMQTTClient] = None
        self.connected = False
        self.subscribed_topics: List[str] = []
        
        # Message handlers
        self.message_handlers: Dict[str, Callable] = {}
        
        # Statistics
        self.stats = {
            "messages_received": 0,
            "messages_sent": 0,
            "connection_errors": 0,
            "last_message_time": None
        }
    
    async def start(self):
        """Start the MQTT service."""
        self.logger.info("🚀 Starting MQTT service...")
        
        try:
            # Create MQTT client
            self.client = AsyncMQTTClient(
                client_id=self.mqtt_config.client_id,
                hostname=self.mqtt_config.broker_host,
                port=self.mqtt_config.broker_port,
                username=self.mqtt_config.username,
                password=self.mqtt_config.password,
                keepalive=self.mqtt_config.keepalive
            )
            
            # Set callbacks
            self.client.on_connect = self._on_connect
            self.client.on_disconnect = self._on_disconnect
            self.client.on_message = self._on_message
            self.client.on_log = self._on_log
            
            # Connect to broker
            await self.client.connect()
            
            # Start the client loop
            await self.client.loop_start()
            
            # Subscribe to default topics
            await self._subscribe_to_default_topics()
            
            self.logger.info("✅ MQTT service started successfully")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to start MQTT service: {e}")
            self.stats["connection_errors"] += 1
            raise
    
    async def stop(self):
        """Stop the MQTT service."""
        self.logger.info("🛑 Stopping MQTT service...")
        
        if self.client:
            try:
                # Unsubscribe from all topics
                for topic in self.subscribed_topics:
                    await self.client.unsubscribe(topic)
                
                # Disconnect
                await self.client.disconnect()
                self.connected = False
                
                self.logger.info("✅ MQTT service stopped")
                
            except Exception as e:
                self.logger.error(f"❌ Error stopping MQTT service: {e}")
    
    def _on_connect(self, client, userdata, flags, rc):
        """Callback for MQTT connection."""
        if rc == 0:
            self.connected = True
            self.logger.info(f"✅ Connected to MQTT broker at {self.mqtt_config.broker_host}:{self.mqtt_config.broker_port}")
        else:
            self.connected = False
            self.logger.error(f"❌ Failed to connect to MQTT broker. Return code: {rc}")
            self.stats["connection_errors"] += 1
    
    def _on_disconnect(self, client, userdata, rc):
        """Callback for MQTT disconnection."""
        self.connected = False
        if rc != 0:
            self.logger.warning(f"⚠️ Unexpected MQTT disconnection. Return code: {rc}")
        else:
            self.logger.info("🔌 MQTT client disconnected")
    
    def _on_message(self, client, userdata, message):
        """Callback for received MQTT messages."""
        try:
            topic = message.topic
            payload = message.payload.decode('utf-8')
            
            # Update statistics
            self.stats["messages_received"] += 1
            self.stats["last_message_time"] = datetime.now()
            
            # Parse JSON payload
            try:
                data = json.loads(payload)
            except json.JSONDecodeError:
                data = {"raw": payload}
            
            # Log message
            self.logger.debug(f"📨 Received MQTT message on topic '{topic}': {data}")
            
            # Call registered handlers
            for pattern, handler in self.message_handlers.items():
                if self._topic_matches(topic, pattern):
                    try:
                        asyncio.create_task(self._call_handler(handler, topic, data))
                    except Exception as e:
                        self.logger.error(f"❌ Error in message handler for topic '{topic}': {e}")
            
        except Exception as e:
            self.logger.error(f"❌ Error processing MQTT message: {e}")
    
    def _on_log(self, client, userdata, level, buf):
        """Callback for MQTT logs."""
        if level == mqtt.MQTT_LOG_ERR:
            self.logger.error(f"MQTT Error: {buf}")
        elif level == mqtt.MQTT_LOG_WARNING:
            self.logger.warning(f"MQTT Warning: {buf}")
        elif level == mqtt.MQTT_LOG_INFO:
            self.logger.info(f"MQTT Info: {buf}")
        else:
            self.logger.debug(f"MQTT Debug: {buf}")
    
    async def _subscribe_to_default_topics(self):
        """Subscribe to default MQTT topics."""
        for topic_name, topic_pattern in self.mqtt_config.topics.items():
            await self.subscribe(topic_pattern)
            self.logger.info(f"📡 Subscribed to topic: {topic_pattern}")
    
    def _topic_matches(self, topic: str, pattern: str) -> bool:
        """Check if topic matches pattern (supports MQTT wildcards)."""
        if pattern == topic:
            return True
        
        # Simple wildcard matching for + and #
        pattern_parts = pattern.split('/')
        topic_parts = topic.split('/')
        
        if len(pattern_parts) != len(topic_parts) and not pattern.endswith('#'):
            return False
        
        for i, (pattern_part, topic_part) in enumerate(zip(pattern_parts, topic_parts)):
            if pattern_part == '+':
                continue
            elif pattern_part == '#':
                return True
            elif pattern_part != topic_part:
                return False
        
        return True
    
    async def _call_handler(self, handler: Callable, topic: str, data: Any):
        """Call a message handler asynchronously."""
        if asyncio.iscoroutinefunction(handler):
            await handler(topic, data)
        else:
            handler(topic, data)
    
    async def subscribe(self, topic: str, qos: int = None) -> bool:
        """Subscribe to an MQTT topic."""
        if not self.client or not self.connected:
            self.logger.error("❌ MQTT client not connected")
            return False
        
        try:
            qos = qos or self.mqtt_config.qos
            await self.client.subscribe(topic, qos)
            self.subscribed_topics.append(topic)
            self.logger.info(f"📡 Subscribed to topic: {topic} (QoS: {qos})")
            return True
        except Exception as e:
            self.logger.error(f"❌ Failed to subscribe to topic '{topic}': {e}")
            return False
    
    async def unsubscribe(self, topic: str) -> bool:
        """Unsubscribe from an MQTT topic."""
        if not self.client or not self.connected:
            self.logger.error("❌ MQTT client not connected")
            return False
        
        try:
            await self.client.unsubscribe(topic)
            if topic in self.subscribed_topics:
                self.subscribed_topics.remove(topic)
            self.logger.info(f"📡 Unsubscribed from topic: {topic}")
            return True
        except Exception as e:
            self.logger.error(f"❌ Failed to unsubscribe from topic '{topic}': {e}")
            return False
    
    async def publish(self, topic: str, payload: Any, qos: int = None, retain: bool = None) -> bool:
        """Publish a message to an MQTT topic."""
        if not self.client or not self.connected:
            self.logger.error("❌ MQTT client not connected")
            return False
        
        try:
            # Convert payload to JSON if it's a dict
            if isinstance(payload, dict):
                payload = json.dumps(payload)
            elif not isinstance(payload, (str, bytes)):
                payload = str(payload)
            
            qos = qos or self.mqtt_config.qos
            retain = retain if retain is not None else self.mqtt_config.retain
            
            await self.client.publish(topic, payload, qos=qos, retain=retain)
            
            # Update statistics
            self.stats["messages_sent"] += 1
            
            self.logger.debug(f"📤 Published message to topic '{topic}': {payload}")
            return True
        except Exception as e:
            self.logger.error(f"❌ Failed to publish message to topic '{topic}': {e}")
            return False
    
    def register_handler(self, topic_pattern: str, handler: Callable):
        """Register a message handler for a topic pattern."""
        self.message_handlers[topic_pattern] = handler
        self.logger.info(f"📝 Registered handler for topic pattern: {topic_pattern}")
    
    def unregister_handler(self, topic_pattern: str):
        """Unregister a message handler."""
        if topic_pattern in self.message_handlers:
            del self.message_handlers[topic_pattern]
            self.logger.info(f"📝 Unregistered handler for topic pattern: {topic_pattern}")
    
    def is_connected(self) -> bool:
        """Check if MQTT client is connected."""
        return self.connected and self.client is not None
    
    def get_stats(self) -> Dict[str, Any]:
        """Get MQTT service statistics."""
        return {
            **self.stats,
            "connected": self.connected,
            "subscribed_topics": len(self.subscribed_topics),
            "registered_handlers": len(self.message_handlers)
        }
    
    async def publish_energy_data(self, device_id: str, data: Dict[str, Any]):
        """Publish energy data for a specific device."""
        topic = f"smartwatts/energy/{device_id}/data"
        payload = {
            "device_id": device_id,
            "timestamp": datetime.now().isoformat(),
            "data": data
        }
        return await self.publish(topic, payload)
    
    async def publish_device_status(self, device_id: str, status: str, metadata: Dict[str, Any] = None):
        """Publish device status update."""
        topic = f"smartwatts/devices/{device_id}/status"
        payload = {
            "device_id": device_id,
            "status": status,
            "timestamp": datetime.now().isoformat(),
            "metadata": metadata or {}
        }
        return await self.publish(topic, payload)
    
    async def publish_alert(self, device_id: str, alert_type: str, message: str, severity: str = "info"):
        """Publish an alert message."""
        topic = f"smartwatts/alerts/{device_id}/{alert_type}"
        payload = {
            "device_id": device_id,
            "alert_type": alert_type,
            "message": message,
            "severity": severity,
            "timestamp": datetime.now().isoformat()
        }
        return await self.publish(topic, payload)
