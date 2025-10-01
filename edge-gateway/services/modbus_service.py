"""
SmartWatts Edge Gateway Modbus Service
Complete Modbus RTU/TCP integration for inverters, meters, and energy devices
"""

import asyncio
import logging
from typing import Dict, List, Optional, Any, Tuple
from datetime import datetime
from dataclasses import dataclass
from enum import Enum
import serial
from pymodbus.client import ModbusTcpClient, ModbusSerialClient
from pymodbus.exceptions import ModbusException, ConnectionException
from pymodbus.payload import BinaryPayloadDecoder, BinaryPayloadBuilder
from pymodbus.constants import Endian
from core.config import EdgeConfig, ModbusConfig

class DeviceType(Enum):
    """Supported Modbus device types."""
    INVERTER = "inverter"
    METER = "meter"
    BATTERY = "battery"
    CONTROLLER = "controller"
    SENSOR = "sensor"

class ModbusProtocol(Enum):
    """Modbus protocol types."""
    TCP = "tcp"
    RTU = "rtu"

@dataclass
class ModbusDevice:
    """Modbus device configuration."""
    name: str
    device_type: DeviceType
    address: int
    protocol: ModbusProtocol
    host: Optional[str] = None
    port: Optional[int] = None
    serial_port: Optional[str] = None
    baudrate: int = 9600
    parity: str = "N"
    stopbits: int = 1
    bytesize: int = 8
    timeout: float = 3.0
    retries: int = 3
    enabled: bool = True

@dataclass
class ModbusRegister:
    """Modbus register definition."""
    address: int
    count: int
    data_type: str  # uint16, int16, uint32, int32, float32, etc.
    scale_factor: float = 1.0
    offset: float = 0.0
    description: str = ""

@dataclass
class ModbusReading:
    """Modbus reading result."""
    device_name: str
    register_address: int
    value: float
    unit: str
    timestamp: datetime
    quality: str = "good"

class ModbusService:
    """Modbus RTU/TCP service for SmartWatts Edge Gateway."""
    
    def __init__(self, config: EdgeConfig):
        self.config = config
        self.modbus_config = config.modbus
        self.logger = logging.getLogger(__name__)
        
        # Device configurations
        self.devices: Dict[str, ModbusDevice] = {}
        self.clients: Dict[str, Any] = {}  # Modbus clients
        
        # Register definitions for different device types
        self.register_definitions: Dict[DeviceType, List[ModbusRegister]] = {}
        
        # Statistics
        self.stats = {
            "devices_connected": 0,
            "total_readings": 0,
            "connection_errors": 0,
            "read_errors": 0,
            "last_reading_time": None
        }
        
        # Initialize register definitions
        self._initialize_register_definitions()
        
        # Initialize devices from config
        self._initialize_devices()
    
    def _initialize_register_definitions(self):
        """Initialize register definitions for different device types."""
        
        # Solar Inverter registers (common SunSpec standard)
        self.register_definitions[DeviceType.INVERTER] = [
            ModbusRegister(40001, 1, "uint16", 1.0, 0.0, "AC Power"),
            ModbusRegister(40002, 1, "uint16", 1.0, 0.0, "AC Voltage"),
            ModbusRegister(40003, 1, "uint16", 1.0, 0.0, "AC Current"),
            ModbusRegister(40004, 1, "uint16", 0.1, 0.0, "Frequency"),
            ModbusRegister(40005, 2, "uint32", 1.0, 0.0, "Total Energy"),
            ModbusRegister(40007, 1, "uint16", 0.1, 0.0, "Power Factor"),
            ModbusRegister(40008, 1, "int16", 1.0, 0.0, "Temperature"),
            ModbusRegister(40009, 1, "uint16", 1.0, 0.0, "Status"),
        ]
        
        # Energy Meter registers
        self.register_definitions[DeviceType.METER] = [
            ModbusRegister(40001, 1, "uint16", 1.0, 0.0, "Active Power"),
            ModbusRegister(40002, 1, "uint16", 1.0, 0.0, "Voltage L1"),
            ModbusRegister(40003, 1, "uint16", 1.0, 0.0, "Voltage L2"),
            ModbusRegister(40004, 1, "uint16", 1.0, 0.0, "Voltage L3"),
            ModbusRegister(40005, 1, "uint16", 1.0, 0.0, "Current L1"),
            ModbusRegister(40006, 1, "uint16", 1.0, 0.0, "Current L2"),
            ModbusRegister(40007, 1, "uint16", 1.0, 0.0, "Current L3"),
            ModbusRegister(40008, 2, "uint32", 1.0, 0.0, "Total Energy"),
            ModbusRegister(40010, 1, "uint16", 0.1, 0.0, "Frequency"),
        ]
        
        # Battery registers
        self.register_definitions[DeviceType.BATTERY] = [
            ModbusRegister(40001, 1, "uint16", 0.1, 0.0, "Voltage"),
            ModbusRegister(40002, 1, "int16", 0.1, 0.0, "Current"),
            ModbusRegister(40003, 1, "uint16", 0.1, 0.0, "State of Charge"),
            ModbusRegister(40004, 1, "uint16", 0.1, 0.0, "Temperature"),
            ModbusRegister(40005, 1, "uint16", 1.0, 0.0, "Status"),
            ModbusRegister(40006, 2, "uint32", 1.0, 0.0, "Total Energy"),
        ]
    
    def _initialize_devices(self):
        """Initialize devices from configuration."""
        for device_config in self.modbus_config.devices:
            try:
                device = ModbusDevice(
                    name=device_config["name"],
                    device_type=DeviceType(device_config["type"]),
                    address=device_config["address"],
                    protocol=ModbusProtocol(device_config["protocol"]),
                    host=device_config.get("host"),
                    port=device_config.get("port"),
                    serial_port=device_config.get("port"),
                    baudrate=device_config.get("baudrate", 9600),
                    timeout=self.modbus_config.timeout,
                    retries=self.modbus_config.retries,
                    enabled=device_config.get("enabled", True)
                )
                self.devices[device.name] = device
                self.logger.info(f"📋 Initialized Modbus device: {device.name} ({device.device_type.value})")
            except Exception as e:
                self.logger.error(f"❌ Failed to initialize device {device_config.get('name', 'unknown')}: {e}")
    
    async def start(self):
        """Start the Modbus service."""
        self.logger.info("🚀 Starting Modbus service...")
        
        try:
            # Connect to all enabled devices
            for device_name, device in self.devices.items():
                if device.enabled:
                    await self._connect_device(device)
            
            self.logger.info(f"✅ Modbus service started with {self.stats['devices_connected']} connected devices")
            
        except Exception as e:
            self.logger.error(f"❌ Failed to start Modbus service: {e}")
            raise
    
    async def stop(self):
        """Stop the Modbus service."""
        self.logger.info("🛑 Stopping Modbus service...")
        
        # Close all connections
        for client in self.clients.values():
            try:
                if hasattr(client, 'close'):
                    client.close()
            except Exception as e:
                self.logger.error(f"❌ Error closing Modbus client: {e}")
        
        self.clients.clear()
        self.stats["devices_connected"] = 0
        
        self.logger.info("✅ Modbus service stopped")
    
    async def _connect_device(self, device: ModbusDevice) -> bool:
        """Connect to a Modbus device."""
        try:
            if device.protocol == ModbusProtocol.TCP:
                client = ModbusTcpClient(
                    host=device.host,
                    port=device.port,
                    timeout=device.timeout,
                    retries=device.retries
                )
            elif device.protocol == ModbusProtocol.RTU:
                client = ModbusSerialClient(
                    method='rtu',
                    port=device.serial_port,
                    baudrate=device.baudrate,
                    parity=device.parity,
                    stopbits=device.stopbits,
                    bytesize=device.bytesize,
                    timeout=device.timeout,
                    retries=device.retries
                )
            else:
                self.logger.error(f"❌ Unsupported protocol for device {device.name}: {device.protocol}")
                return False
            
            # Test connection
            if client.connect():
                self.clients[device.name] = client
                self.stats["devices_connected"] += 1
                self.logger.info(f"✅ Connected to Modbus device: {device.name} ({device.protocol.value})")
                return True
            else:
                self.logger.error(f"❌ Failed to connect to Modbus device: {device.name}")
                self.stats["connection_errors"] += 1
                return False
                
        except Exception as e:
            self.logger.error(f"❌ Error connecting to device {device.name}: {e}")
            self.stats["connection_errors"] += 1
            return False
    
    async def read_device(self, device_name: str) -> List[ModbusReading]:
        """Read all registers from a specific device."""
        if device_name not in self.devices:
            self.logger.error(f"❌ Device not found: {device_name}")
            return []
        
        device = self.devices[device_name]
        if device_name not in self.clients:
            self.logger.error(f"❌ Device not connected: {device_name}")
            return []
        
        client = self.clients[device_name]
        readings = []
        
        try:
            # Get register definitions for this device type
            registers = self.register_definitions.get(device.device_type, [])
            
            for register in registers:
                try:
                    # Read holding registers
                    result = client.read_holding_registers(
                        address=register.address,
                        count=register.count,
                        unit=device.address
                    )
                    
                    if result.isError():
                        self.logger.warning(f"⚠️ Error reading register {register.address} from {device_name}: {result}")
                        continue
                    
                    # Decode the value based on data type
                    value = self._decode_register_value(result.registers, register)
                    
                    # Apply scale factor and offset
                    scaled_value = (value * register.scale_factor) + register.offset
                    
                    # Create reading
                    reading = ModbusReading(
                        device_name=device_name,
                        register_address=register.address,
                        value=scaled_value,
                        unit=self._get_unit_for_register(register),
                        timestamp=datetime.now(),
                        quality="good"
                    )
                    
                    readings.append(reading)
                    self.stats["total_readings"] += 1
                    
                except ModbusException as e:
                    self.logger.error(f"❌ Modbus error reading register {register.address} from {device_name}: {e}")
                    self.stats["read_errors"] += 1
                    continue
            
            self.stats["last_reading_time"] = datetime.now()
            
        except Exception as e:
            self.logger.error(f"❌ Error reading device {device_name}: {e}")
            self.stats["read_errors"] += 1
        
        return readings
    
    async def read_all_devices(self) -> Dict[str, List[ModbusReading]]:
        """Read all connected devices."""
        all_readings = {}
        
        for device_name in self.devices.keys():
            if device_name in self.clients:
                readings = await self.read_device(device_name)
                all_readings[device_name] = readings
        
        return all_readings
    
    def _decode_register_value(self, registers: List[int], register: ModbusRegister) -> float:
        """Decode register value based on data type."""
        if register.data_type == "uint16":
            return float(registers[0])
        elif register.data_type == "int16":
            return float(registers[0] if registers[0] < 32768 else registers[0] - 65536)
        elif register.data_type == "uint32":
            if len(registers) >= 2:
                return float((registers[0] << 16) | registers[1])
            else:
                return float(registers[0])
        elif register.data_type == "int32":
            if len(registers) >= 2:
                value = (registers[0] << 16) | registers[1]
                return float(value if value < 2147483648 else value - 4294967296)
            else:
                return float(registers[0])
        elif register.data_type == "float32":
            if len(registers) >= 2:
                decoder = BinaryPayloadDecoder.fromRegisters(registers, Endian.Big)
                return float(decoder.decode_32bit_float())
            else:
                return float(registers[0])
        else:
            return float(registers[0])
    
    def _get_unit_for_register(self, register: ModbusRegister) -> str:
        """Get unit for a register based on its description."""
        description = register.description.lower()
        
        if "power" in description:
            return "W"
        elif "voltage" in description:
            return "V"
        elif "current" in description:
            return "A"
        elif "frequency" in description:
            return "Hz"
        elif "energy" in description:
            return "kWh"
        elif "temperature" in description:
            return "°C"
        elif "charge" in description:
            return "%"
        else:
            return ""
    
    def is_connected(self) -> bool:
        """Check if any Modbus devices are connected."""
        return len(self.clients) > 0
    
    def get_connected_devices(self) -> List[str]:
        """Get list of connected device names."""
        return list(self.clients.keys())
    
    def get_device_info(self, device_name: str) -> Optional[Dict[str, Any]]:
        """Get information about a specific device."""
        if device_name not in self.devices:
            return None
        
        device = self.devices[device_name]
        return {
            "name": device.name,
            "type": device.device_type.value,
            "address": device.address,
            "protocol": device.protocol.value,
            "connected": device_name in self.clients,
            "enabled": device.enabled
        }
    
    def get_stats(self) -> Dict[str, Any]:
        """Get Modbus service statistics."""
        return {
            **self.stats,
            "total_devices": len(self.devices),
            "connected_devices": len(self.clients),
            "enabled_devices": sum(1 for d in self.devices.values() if d.enabled)
        }
    
    async def write_register(self, device_name: str, address: int, value: int) -> bool:
        """Write a value to a Modbus register."""
        if device_name not in self.clients:
            self.logger.error(f"❌ Device not connected: {device_name}")
            return False
        
        try:
            device = self.devices[device_name]
            client = self.clients[device_name]
            
            result = client.write_register(
                address=address,
                value=value,
                unit=device.address
            )
            
            if result.isError():
                self.logger.error(f"❌ Error writing to register {address} on {device_name}: {result}")
                return False
            
            self.logger.info(f"✅ Wrote value {value} to register {address} on {device_name}")
            return True
            
        except Exception as e:
            self.logger.error(f"❌ Error writing to device {device_name}: {e}")
            return False
