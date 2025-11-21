package com.smartwatts.edge.controller;

import com.smartwatts.edge.service.EdgeMLService;
import com.smartwatts.edge.service.EdgeDeviceService;
import com.smartwatts.edge.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/edge")
@CrossOrigin(origins = "*")
public class EdgeGatewayController {
    
    private static final Logger logger = LoggerFactory.getLogger(EdgeGatewayController.class);
    
    @Autowired
    private EdgeMLService edgeMLService;
    
    @Autowired
    private EdgeDeviceService edgeDeviceService;
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "SmartWatts Edge Gateway");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get edge gateway statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGatewayStats() {
        try {
            Map<String, Object> stats = edgeDeviceService.getGatewayStats();
            stats.put("mlModels", edgeMLService.getModelStatus());
            stats.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to get gateway stats", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get gateway stats"));
        }
    }
    
    /**
     * Energy consumption forecasting
     */
    @PostMapping("/ml/forecast")
    public ResponseEntity<EnergyPrediction> forecastEnergyConsumption(
            @RequestBody Map<String, Object> request) {
        try {
            double currentConsumption = Double.parseDouble(request.get("currentConsumption").toString());
            double historicalAverage = Double.parseDouble(request.get("historicalAverage").toString());
            
            @SuppressWarnings("unchecked")
            Map<String, Double> factors = (Map<String, Double>) request.get("factors");
            
            // Add current consumption and historical average to factors for better prediction
            factors.put("currentConsumption", currentConsumption);
            factors.put("historicalAverage", historicalAverage);
            
            EnergyPrediction prediction = edgeMLService.forecastEnergyConsumption(
                "device123", "facility456", 24, factors);
            
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            logger.error("Failed to generate energy forecast", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Anomaly detection
     */
    @PostMapping("/ml/anomaly")
    public ResponseEntity<AnomalyDetection> detectAnomaly(
            @RequestBody Map<String, Object> request) {
        try {
            double currentConsumption = Double.parseDouble(request.get("currentConsumption").toString());
            double baseline = Double.parseDouble(request.get("baseline").toString());
            
            @SuppressWarnings("unchecked")
            Map<String, Double> context = (Map<String, Double>) request.get("context");
            
            AnomalyDetection anomaly = edgeMLService.detectAnomaly(
                currentConsumption, baseline, context);
            
            return ResponseEntity.ok(anomaly);
        } catch (Exception e) {
            logger.error("Failed to detect anomaly", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Generate optimization recommendations
     */
    @PostMapping("/ml/recommendations")
    public ResponseEntity<List<OptimizationRecommendation>> generateRecommendations(
            @RequestBody Map<String, Object> request) {
        try {
            double currentEfficiency = Double.parseDouble(request.get("currentEfficiency").toString());
            
            @SuppressWarnings("unchecked")
            Map<String, Double> metrics = (Map<String, Double>) request.get("metrics");
            
            List<OptimizationRecommendation> recommendations = edgeMLService.generateRecommendations(
                currentEfficiency, metrics);
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Failed to generate recommendations", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get all device statuses
     */
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceStatus>> getAllDevices() {
        try {
            List<DeviceStatus> devices = edgeDeviceService.getAllDeviceStatuses();
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
            return ResponseEntity.ok(readings);
        } catch (Exception e) {
            logger.error("Failed to get device readings for {}", deviceId, e);
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
            command.setTimestamp(LocalDateTime.now());
            
            boolean success = edgeDeviceService.sendDeviceCommand(deviceId, command);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("deviceId", deviceId);
            response.put("command", command.getCommand());
            response.put("timestamp", command.getTimestamp());
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Failed to send command to device {}", deviceId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to send command"));
        }
    }
    
    /**
     * Update device status
     */
    @PutMapping("/devices/{deviceId}/status")
    public ResponseEntity<Map<String, Object>> updateDeviceStatus(
            @PathVariable String deviceId,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            String location = request.get("location");
            
            edgeDeviceService.updateDeviceStatus(deviceId, status, location);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deviceId", deviceId);
            response.put("status", status);
            response.put("location", location);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update device status for {}", deviceId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to update status"));
        }
    }
    
    /**
     * Process device reading (for IoT devices to send data)
     */
    @PostMapping("/devices/{deviceId}/reading")
    public ResponseEntity<Map<String, Object>> processDeviceReading(
            @PathVariable String deviceId,
            @RequestBody DeviceReading reading) {
        try {
            reading.setDeviceId(deviceId);
            reading.setTimestamp(LocalDateTime.now());
            
            edgeDeviceService.processDeviceReading(deviceId, reading);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deviceId", deviceId);
            response.put("timestamp", reading.getTimestamp());
            response.put("energyConsumption", reading.getEnergyConsumption());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to process device reading for {}", deviceId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process reading"));
        }
    }
    
    /**
     * Get consolidated ML model status summary (gateway view)
     */
    @GetMapping("/ml/status/overview")
    public ResponseEntity<Map<String, Object>> getMLModelStatus() {
        try {
            Map<String, Object> status = edgeMLService.getModelStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Failed to get ML model status", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get ML status"));
        }
    }
    
    /**
     * Initialize ML models
     */
    @PostMapping("/ml/initialize")
    public ResponseEntity<Map<String, Object>> initializeMLModels() {
        try {
            edgeMLService.initializeModels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "ML models initialized successfully");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to initialize ML models", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to initialize ML models"));
        }
    }
    
    /**
     * Get edge gateway configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("gatewayId", "edge_gateway_001");
            config.put("location", "Edge Gateway");
            config.put("version", "1.0.0");
            config.put("supportedProtocols", Arrays.asList("mqtt", "modbus", "http", "websocket"));
            config.put("mlCapabilities", Arrays.asList("energy_forecasting", "anomaly_detection", "optimization"));
            config.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Failed to get configuration", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to get configuration"));
        }
    }
}
