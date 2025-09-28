package com.smartwatts.edge.controller;

import com.smartwatts.edge.model.DeviceCommand;
import com.smartwatts.edge.model.DeviceReading;
import com.smartwatts.edge.model.DeviceStatus;
import com.smartwatts.edge.protocol.MQTTProtocolHandler;
import com.smartwatts.edge.protocol.ModbusProtocolHandler;
import com.smartwatts.edge.service.EdgeDeviceService;
import com.smartwatts.edge.storage.EdgeStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/edge/iot")
@CrossOrigin(origins = "*")
public class EdgeIoTController {
    
    private static final Logger logger = LoggerFactory.getLogger(EdgeIoTController.class);
    
    private final EdgeDeviceService edgeDeviceService;
    private final EdgeStorageService edgeStorageService;
    private final MQTTProtocolHandler mqttHandler;
    private final ModbusProtocolHandler modbusHandler;
    
    @Autowired
    public EdgeIoTController(EdgeDeviceService edgeDeviceService,
                           EdgeStorageService edgeStorageService,
                           MQTTProtocolHandler mqttHandler,
                           ModbusProtocolHandler modbusHandler) {
        this.edgeDeviceService = edgeDeviceService;
        this.edgeStorageService = edgeStorageService;
        this.mqttHandler = mqttHandler;
        this.modbusHandler = modbusHandler;
    }
    
    /**
     * Get IoT protocol status
     */
    @GetMapping("/protocols/status")
    public ResponseEntity<Map<String, Object>> getProtocolStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // MQTT Status
            Map<String, Object> mqttStatus = new HashMap<>();
            mqttStatus.put("connected", mqttHandler.isConnected());
            mqttStatus.put("broker_url", "tcp://localhost:1883"); // From config
            status.put("mqtt", mqttStatus);
            
            // Modbus Status
            Map<String, Object> modbusStatus = new HashMap<>();
            modbusStatus.put("master_connected", modbusHandler.isMasterConnected());
            modbusStatus.put("slave_running", modbusHandler.isSlaveRunning());
            modbusStatus.put("device_count", modbusHandler.getDeviceConfigs().size());
            status.put("modbus", modbusStatus);
            
            // Overall Status
            status.put("timestamp", java.time.LocalDateTime.now());
            status.put("status", "operational");
            
            logger.info("Retrieved IoT protocol status");
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Failed to get protocol status", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all IoT devices
     */
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceStatus>> getAllDevices() {
        try {
            List<DeviceStatus> devices = edgeDeviceService.getAllDeviceStatuses();
            logger.info("Retrieved {} IoT devices", devices.size());
            return ResponseEntity.ok(devices);
            
        } catch (Exception e) {
            logger.error("Failed to get devices", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get specific device status
     */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceStatus> getDeviceStatus(@PathVariable String deviceId) {
        try {
            DeviceStatus status = edgeDeviceService.getDeviceStatus(deviceId);
            if (status != null) {
                return ResponseEntity.ok(status);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Failed to get device status for {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get device readings
     */
    @GetMapping("/devices/{deviceId}/readings")
    public ResponseEntity<List<DeviceReading>> getDeviceReadings(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<DeviceReading> readings = edgeDeviceService.getDeviceReadings(deviceId, limit);
            logger.info("Retrieved {} readings for device {}", readings.size(), deviceId);
            return ResponseEntity.ok(readings);
            
        } catch (Exception e) {
            logger.error("Failed to get readings for device {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Send command to device
     */
    @PostMapping("/devices/{deviceId}/command")
    public ResponseEntity<Map<String, Object>> sendDeviceCommand(
            @PathVariable String deviceId,
            @RequestBody DeviceCommand command) {
        try {
            command.setDeviceId(deviceId);
            command.setTimestamp(java.time.LocalDateTime.now());
            
            boolean success = edgeDeviceService.sendDeviceCommand(deviceId, command);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("device_id", deviceId);
            response.put("command", command.getCommand());
            response.put("timestamp", command.getTimestamp());
            
            if (success) {
                logger.info("Command sent successfully to device {}: {}", deviceId, command.getCommand());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Failed to send command to device {}: {}", deviceId, command.getCommand());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("Failed to send command to device {}", deviceId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update device status
     */
    @PutMapping("/devices/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> updateDeviceStatus(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            String location = statusUpdate.get("location");
            
            edgeDeviceService.updateDeviceStatus(deviceId, status, location);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("device_id", deviceId);
            response.put("status", status);
            response.put("location", location);
            response.put("updated_at", java.time.LocalDateTime.now());
            
            logger.info("Updated device {} status to: {}", deviceId, status);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to update device status for {}", deviceId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Register Modbus device
     */
    @PostMapping("/modbus/devices")
    public ResponseEntity<Map<String, Object>> registerModbusDevice(
            @RequestBody ModbusProtocolHandler.ModbusDeviceConfig config) {
        try {
            String deviceId = "modbus_" + System.currentTimeMillis();
            modbusHandler.registerDevice(deviceId, config);
            
            // Create device status
            DeviceStatus status = new DeviceStatus();
            status.setDeviceId(deviceId);
            status.setProtocol("modbus");
            status.setStatus("online");
            status.setLastSeen(java.time.LocalDateTime.now());
            status.setLocation(config.getHost() + ":" + config.getPort());
            status.setCapabilities(List.of("energy_monitoring", "modbus_tcp"));
            
            edgeDeviceService.updateDeviceStatus(deviceId, status.getStatus(), status.getLocation());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("device_id", deviceId);
            response.put("config", config);
            response.put("message", "Modbus device registered successfully");
            
            logger.info("Registered Modbus device: {} at {}:{}", deviceId, config.getHost(), config.getPort());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to register Modbus device", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get Modbus device configurations
     */
    @GetMapping("/modbus/devices")
    public ResponseEntity<Map<String, ModbusProtocolHandler.ModbusDeviceConfig>> getModbusDevices() {
        try {
            Map<String, ModbusProtocolHandler.ModbusDeviceConfig> devices = modbusHandler.getDeviceConfigs();
            return ResponseEntity.ok(devices);
            
        } catch (Exception e) {
            logger.error("Failed to get Modbus devices", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get edge storage statistics
     */
    @GetMapping("/storage/stats")
    public ResponseEntity<Map<String, Object>> getStorageStats() {
        try {
            Map<String, Object> stats = edgeStorageService.getStorageStats();
            logger.info("Retrieved edge storage statistics");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Failed to get storage statistics", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Test MQTT connection
     */
    @PostMapping("/mqtt/test")
    public ResponseEntity<Map<String, Object>> testMQTTConnection() {
        try {
            boolean connected = mqttHandler.isConnected();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", connected);
            response.put("mqtt_connected", connected);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            if (connected) {
                response.put("message", "MQTT connection is active");
            } else {
                response.put("message", "MQTT connection is not active");
            }
            
            logger.info("MQTT connection test: {}", connected ? "SUCCESS" : "FAILED");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to test MQTT connection", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Test Modbus connection
     */
    @PostMapping("/modbus/test")
    public ResponseEntity<Map<String, Object>> testModbusConnection() {
        try {
            boolean masterConnected = modbusHandler.isMasterConnected();
            boolean slaveRunning = modbusHandler.isSlaveRunning();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", masterConnected || slaveRunning);
            response.put("master_connected", masterConnected);
            response.put("slave_running", slaveRunning);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            if (masterConnected || slaveRunning) {
                response.put("message", "Modbus connection is active");
            } else {
                response.put("message", "Modbus connection is not active");
            }
            
            logger.info("Modbus connection test: MASTER={}, SLAVE={}", masterConnected, slaveRunning);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to test Modbus connection", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
