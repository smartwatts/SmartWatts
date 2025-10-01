"""
SmartWatts Edge Gateway Device Discovery Service
Automatic device discovery and registration for MQTT, Modbus, HTTP, and CoAP devices
"""

import asyncio
import logging
import json
import socket
import subprocess
from typing import Dict, List, Optional, Any, Set
from datetime import datetime, timedelta
from dataclasses import dataclass, asdict
from enum import Enum
import httpx
import nmap
from core.config import EdgeConfig, DeviceDiscoveryConfig
from services.mqtt_service import MQTTService
from services.modbus_service import ModbusService

class DiscoveryProtocol(Enum):
    """Supported discovery protocols."""
    MQTT = "mqtt"
    MODBUS = "modbus"
    HTTP = "http"
    COAP = "coap"
    UPNP = "upnp"
    MDNS = "mdns"

@dataclass
class DiscoveredDevice:
    """Discovered device information."""
    device_id: str
    device_type: str
    protocol: DiscoveryProtocol
    ip_address: str
    port: int
    mac_address: Optional[str] = None
    hostname: Optional[str] = None
    manufacturer: Optional[str] = None
    model: Optional[str] = None
    firmware_version: Optional[str] = None
    capabilities: List[str] = None
    metadata: Dict[str, Any] = None
    first_seen: datetime = None
    last_seen: datetime = None
    status: str = "discovered"  # discovered, registered, online, offline, error

class DeviceDiscoveryService:
    """Device discovery service for SmartWatts Edge Gateway."""
    
    def __init__(self, config: EdgeConfig, mqtt_service: MQTTService, modbus_service: ModbusService):
        self.config = config
        self.discovery_config = config.device_discovery
        self.mqtt_service = mqtt_service
        self.modbus_service = modbus_service
        self.logger = logging.getLogger(__name__)
        
        # Discovery state
        self.discovered_devices: Dict[str, DiscoveredDevice] = {}
        self.scanning = False
        self.scan_task: Optional[asyncio.Task] = None
        
        # Network scanner
        self.nm = nmap.PortScanner()
        
        # Statistics
        self.stats = {
            "total_discovered": 0,
            "total_registered": 0,
            "scan_cycles": 0,
            "last_scan": None,
            "discovery_errors": 0
        }
    
    async def start(self):
        """Start the device discovery service."""
        self.logger.info("🚀 Starting device discovery service...")
        
        try:
            if not self.discovery_config.enabled:
                self.logger.info("⚠️ Device discovery is disabled")
                return
            
            # Register MQTT handlers
            self.mqtt_service.register_handler(
                "smartwatts/discovery/+/announce",
                self._handle_mqtt_discovery
            )
            
            # Start periodic scanning
            if self.discovery_config.scan_interval_seconds > 0:
                self.scanning = True
                self.scan_task = asyncio.create_task(self._scan_loop())
                self.logger.info(f"✅ Device discovery started with {self.discovery_config.scan_interval_seconds}s interval")
            else:
                self.logger.info("⚠️ Device discovery started but scanning is disabled")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to start device discovery: {e}")
            raise
    
    async def stop(self):
        """Stop the device discovery service."""
        self.logger.info("🛑 Stopping device discovery service...")
        
        try:
            # Stop scanning
            self.scanning = False
            if self.scan_task:
                self.scan_task.cancel()
                try:
                    await self.scan_task
                except asyncio.CancelledError:
                    pass
            
            # Unregister MQTT handlers
            self.mqtt_service.unregister_handler("smartwatts/discovery/+/announce")
            
            self.logger.info("✅ Device discovery service stopped")
            
        except Exception as e:
            self.logger.error(f"❌ Error stopping device discovery: {e}")
    
    async def _scan_loop(self):
        """Main discovery scanning loop."""
        while self.scanning:
            try:
                await self._perform_scan()
                self.stats["scan_cycles"] += 1
                self.stats["last_scan"] = datetime.now()
                
                # Wait for next scan
                await asyncio.sleep(self.discovery_config.scan_interval_seconds)
                
            except asyncio.CancelledError:
                break
            except Exception as e:
                self.logger.error(f"❌ Error in discovery scan loop: {e}")
                self.stats["discovery_errors"] += 1
                await asyncio.sleep(10)  # Wait before retry
    
    async def _perform_scan(self):
        """Perform a complete device discovery scan."""
        self.logger.debug("🔍 Starting device discovery scan...")
        
        # Get local network range
        network_range = await self._get_network_range()
        if not network_range:
            self.logger.warning("⚠️ Could not determine network range for scanning")
            return
        
        # Scan for each enabled protocol
        tasks = []
        
        if DiscoveryProtocol.MQTT in [DiscoveryProtocol(p) for p in self.discovery_config.protocols]:
            tasks.append(self._scan_mqtt_devices(network_range))
        
        if DiscoveryProtocol.MODBUS in [DiscoveryProtocol(p) for p in self.discovery_config.protocols]:
            tasks.append(self._scan_modbus_devices(network_range))
        
        if DiscoveryProtocol.HTTP in [DiscoveryProtocol(p) for p in self.discovery_config.protocols]:
            tasks.append(self._scan_http_devices(network_range))
        
        if DiscoveryProtocol.COAP in [DiscoveryProtocol(p) for p in self.discovery_config.protocols]:
            tasks.append(self._scan_coap_devices(network_range))
        
        if DiscoveryProtocol.UPNP in [DiscoveryProtocol(p) for p in self.discovery_config.protocols]:
            tasks.append(self._scan_upnp_devices())
        
        if DiscoveryProtocol.MDNS in [DiscoveryProtocol(p) for p in self.discovery_config.protocols]:
            tasks.append(self._scan_mdns_devices())
        
        # Run all scans concurrently
        if tasks:
            await asyncio.gather(*tasks, return_exceptions=True)
        
        # Update device statuses
        await self._update_device_statuses()
        
        self.logger.debug(f"🔍 Discovery scan completed. Found {len(self.discovered_devices)} devices")
    
    async def _get_network_range(self) -> Optional[str]:
        """Get the local network range for scanning."""
        try:
            # Get local IP address
            s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            s.connect(("8.8.8.8", 80))
            local_ip = s.getsockname()[0]
            s.close()
            
            # Extract network prefix (assuming /24)
            network_parts = local_ip.split('.')
            network_range = f"{network_parts[0]}.{network_parts[1]}.{network_parts[2]}.0/24"
            
            return network_range
            
        except Exception as e:
            self.logger.error(f"❌ Error getting network range: {e}")
            return None
    
    async def _scan_mqtt_devices(self, network_range: str):
        """Scan for MQTT devices."""
        try:
            # Scan for MQTT ports (1883, 8883)
            self.nm.scan(network_range, '1883,8883', arguments='-sS -T4')
            
            for host in self.nm.all_hosts():
                if self.nm[host].state() == 'up':
                    for port in self.nm[host]['tcp']:
                        if self.nm[host]['tcp'][port]['state'] == 'open':
                            device_id = f"mqtt_{host}_{port}"
                            
                            device = DiscoveredDevice(
                                device_id=device_id,
                                device_type="mqtt_broker",
                                protocol=DiscoveryProtocol.MQTT,
                                ip_address=host,
                                port=port,
                                first_seen=datetime.now(),
                                last_seen=datetime.now(),
                                capabilities=["mqtt_broker"],
                                metadata={"scan_method": "port_scan"}
                            )
                            
                            await self._register_discovered_device(device)
                            
        except Exception as e:
            self.logger.error(f"❌ Error scanning MQTT devices: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _scan_modbus_devices(self, network_range: str):
        """Scan for Modbus devices."""
        try:
            # Scan for Modbus TCP port (502)
            self.nm.scan(network_range, '502', arguments='-sS -T4')
            
            for host in self.nm.all_hosts():
                if self.nm[host].state() == 'up':
                    if 'tcp' in self.nm[host] and 502 in self.nm[host]['tcp']:
                        if self.nm[host]['tcp'][502]['state'] == 'open':
                            # Try to identify device type
                            device_type = await self._identify_modbus_device(host, 502)
                            
                            device_id = f"modbus_{host}_502"
                            
                            device = DiscoveredDevice(
                                device_id=device_id,
                                device_type=device_type,
                                protocol=DiscoveryProtocol.MODBUS,
                                ip_address=host,
                                port=502,
                                first_seen=datetime.now(),
                                last_seen=datetime.now(),
                                capabilities=["modbus_tcp"],
                                metadata={"scan_method": "port_scan"}
                            )
                            
                            await self._register_discovered_device(device)
                            
        except Exception as e:
            self.logger.error(f"❌ Error scanning Modbus devices: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _identify_modbus_device(self, host: str, port: int) -> str:
        """Identify the type of Modbus device."""
        try:
            # Try to read common registers to identify device type
            # This is a simplified identification - in practice, you'd read specific registers
            return "unknown_modbus_device"
        except Exception:
            return "unknown_modbus_device"
    
    async def _scan_http_devices(self, network_range: str):
        """Scan for HTTP devices (smart plugs, sensors, etc.)."""
        try:
            # Scan for common HTTP ports
            self.nm.scan(network_range, '80,443,8080,8443', arguments='-sS -T4')
            
            for host in self.nm.all_hosts():
                if self.nm[host].state() == 'up':
                    for port in self.nm[host]['tcp']:
                        if self.nm[host]['tcp'][port]['state'] == 'open':
                            # Try to identify device via HTTP
                            device_info = await self._identify_http_device(host, port)
                            
                            if device_info:
                                device_id = f"http_{host}_{port}"
                                
                                device = DiscoveredDevice(
                                    device_id=device_id,
                                    device_type=device_info.get("type", "unknown_http_device"),
                                    protocol=DiscoveryProtocol.HTTP,
                                    ip_address=host,
                                    port=port,
                                    manufacturer=device_info.get("manufacturer"),
                                    model=device_info.get("model"),
                                    firmware_version=device_info.get("firmware_version"),
                                    first_seen=datetime.now(),
                                    last_seen=datetime.now(),
                                    capabilities=device_info.get("capabilities", []),
                                    metadata={"scan_method": "http_scan", **device_info}
                                )
                                
                                await self._register_discovered_device(device)
                            
        except Exception as e:
            self.logger.error(f"❌ Error scanning HTTP devices: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _identify_http_device(self, host: str, port: int) -> Optional[Dict[str, Any]]:
        """Identify HTTP device by making requests."""
        try:
            url = f"http://{host}:{port}"
            
            async with httpx.AsyncClient(timeout=5.0) as client:
                # Try common endpoints
                endpoints = ["/", "/api", "/status", "/info", "/device"]
                
                for endpoint in endpoints:
                    try:
                        response = await client.get(f"{url}{endpoint}")
                        if response.status_code == 200:
                            # Try to parse response for device info
                            try:
                                data = response.json()
                                return self._parse_device_info(data)
                            except:
                                # Try to parse HTML for device info
                                return self._parse_html_device_info(response.text)
                    except:
                        continue
            
            return None
            
        except Exception as e:
            self.logger.debug(f"Could not identify HTTP device at {host}:{port}: {e}")
            return None
    
    def _parse_device_info(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Parse device information from JSON response."""
        return {
            "type": data.get("device_type", data.get("type", "unknown")),
            "manufacturer": data.get("manufacturer", data.get("brand")),
            "model": data.get("model", data.get("device_model")),
            "firmware_version": data.get("firmware_version", data.get("version")),
            "capabilities": data.get("capabilities", [])
        }
    
    def _parse_html_device_info(self, html: str) -> Dict[str, Any]:
        """Parse device information from HTML response."""
        # Simple HTML parsing - in practice, you'd use BeautifulSoup
        info = {"type": "unknown_http_device"}
        
        # Look for common patterns
        if "smart" in html.lower() and "plug" in html.lower():
            info["type"] = "smart_plug"
        elif "sensor" in html.lower():
            info["type"] = "sensor"
        elif "inverter" in html.lower():
            info["type"] = "inverter"
        
        return info
    
    async def _scan_coap_devices(self, network_range: str):
        """Scan for CoAP devices."""
        try:
            # CoAP typically uses port 5683
            self.nm.scan(network_range, '5683', arguments='-sU -T4')
            
            for host in self.nm.all_hosts():
                if self.nm[host].state() == 'up':
                    if 'udp' in self.nm[host] and 5683 in self.nm[host]['udp']:
                        if self.nm[host]['udp'][5683]['state'] == 'open':
                            device_id = f"coap_{host}_5683"
                            
                            device = DiscoveredDevice(
                                device_id=device_id,
                                device_type="coap_device",
                                protocol=DiscoveryProtocol.COAP,
                                ip_address=host,
                                port=5683,
                                first_seen=datetime.now(),
                                last_seen=datetime.now(),
                                capabilities=["coap"],
                                metadata={"scan_method": "port_scan"}
                            )
                            
                            await self._register_discovered_device(device)
                            
        except Exception as e:
            self.logger.error(f"❌ Error scanning CoAP devices: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _scan_upnp_devices(self):
        """Scan for UPnP devices."""
        try:
            # Use upnpc or similar tool to discover UPnP devices
            # This is a simplified implementation
            pass
        except Exception as e:
            self.logger.error(f"❌ Error scanning UPnP devices: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _scan_mdns_devices(self):
        """Scan for mDNS devices."""
        try:
            # Use avahi-browse or similar tool to discover mDNS devices
            # This is a simplified implementation
            pass
        except Exception as e:
            self.logger.error(f"❌ Error scanning mDNS devices: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _handle_mqtt_discovery(self, topic: str, data: Dict[str, Any]):
        """Handle MQTT discovery announcements."""
        try:
            device_id = data.get("device_id")
            if not device_id:
                return
            
            device = DiscoveredDevice(
                device_id=device_id,
                device_type=data.get("device_type", "unknown"),
                protocol=DiscoveryProtocol.MQTT,
                ip_address=data.get("ip_address", "unknown"),
                port=data.get("port", 1883),
                mac_address=data.get("mac_address"),
                hostname=data.get("hostname"),
                manufacturer=data.get("manufacturer"),
                model=data.get("model"),
                firmware_version=data.get("firmware_version"),
                first_seen=datetime.now(),
                last_seen=datetime.now(),
                capabilities=data.get("capabilities", []),
                metadata={"discovery_method": "mqtt_announce", **data}
            )
            
            await self._register_discovered_device(device)
            
        except Exception as e:
            self.logger.error(f"❌ Error handling MQTT discovery: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _register_discovered_device(self, device: DiscoveredDevice):
        """Register a discovered device."""
        try:
            # Check if device already exists
            if device.device_id in self.discovered_devices:
                # Update existing device
                existing = self.discovered_devices[device.device_id]
                existing.last_seen = device.last_seen
                existing.status = "online"
                
                # Update metadata if new info is available
                if device.metadata:
                    if existing.metadata:
                        existing.metadata.update(device.metadata)
                    else:
                        existing.metadata = device.metadata
            else:
                # Add new device
                self.discovered_devices[device.device_id] = device
                self.stats["total_discovered"] += 1
                
                self.logger.info(f"🔍 Discovered new device: {device.device_id} ({device.device_type}) at {device.ip_address}:{device.port}")
            
            # Auto-register if enabled
            if self.discovery_config.auto_register:
                await self._auto_register_device(device)
            
        except Exception as e:
            self.logger.error(f"❌ Error registering discovered device: {e}")
            self.stats["discovery_errors"] += 1
    
    async def _auto_register_device(self, device: DiscoveredDevice):
        """Automatically register a device with the appropriate service."""
        try:
            if device.protocol == DiscoveryProtocol.MQTT:
                # Register with MQTT service
                await self.mqtt_service.publish_device_status(
                    device.device_id,
                    "discovered",
                    {
                        "device_type": device.device_type,
                        "ip_address": device.ip_address,
                        "port": device.port,
                        "capabilities": device.capabilities
                    }
                )
            
            elif device.protocol == DiscoveryProtocol.MODBUS:
                # Register with Modbus service
                # This would involve adding the device to the Modbus service configuration
                pass
            
            device.status = "registered"
            self.stats["total_registered"] += 1
            
            self.logger.info(f"✅ Auto-registered device: {device.device_id}")
            
        except Exception as e:
            self.logger.error(f"❌ Error auto-registering device {device.device_id}: {e}")
    
    async def _update_device_statuses(self):
        """Update device statuses based on last seen times."""
        now = datetime.now()
        timeout_threshold = now - timedelta(seconds=self.discovery_config.discovery_timeout_seconds)
        
        for device in self.discovered_devices.values():
            if device.last_seen < timeout_threshold:
                if device.status == "online":
                    device.status = "offline"
                    self.logger.debug(f"📱 Device {device.device_id} marked as offline")
    
    def get_discovered_devices(self) -> List[Dict[str, Any]]:
        """Get all discovered devices."""
        return [asdict(device) for device in self.discovered_devices.values()]
    
    def get_device_by_id(self, device_id: str) -> Optional[Dict[str, Any]]:
        """Get a specific device by ID."""
        device = self.discovered_devices.get(device_id)
        return asdict(device) if device else None
    
    def is_running(self) -> bool:
        """Check if discovery service is running."""
        return self.scanning
    
    def get_stats(self) -> Dict[str, Any]:
        """Get discovery service statistics."""
        return {
            **self.stats,
            "discovered_devices": len(self.discovered_devices),
            "online_devices": sum(1 for d in self.discovered_devices.values() if d.status == "online"),
            "offline_devices": sum(1 for d in self.discovered_devices.values() if d.status == "offline"),
            "registered_devices": sum(1 for d in self.discovered_devices.values() if d.status == "registered")
        }
