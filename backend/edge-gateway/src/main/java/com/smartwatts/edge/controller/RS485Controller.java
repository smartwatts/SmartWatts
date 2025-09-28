package com.smartwatts.edge.controller;

import com.smartwatts.edge.service.RS485SerialService;
import com.smartwatts.edge.service.RS485InverterTestService;
import com.smartwatts.edge.config.RS485Configuration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

/**
 * RS485 Communication Controller
 * Provides REST API for RS485 device management and testing
 */
@RestController
@RequestMapping("/api/v1/rs485")
@Tag(name = "RS485 Communication", description = "RS485 serial communication management and testing")
public class RS485Controller {

    @Autowired
    private RS485SerialService rs485Service;

    @Autowired
    private RS485InverterTestService inverterTestService;

    @Autowired
    private RS485Configuration rs485Config;

    @GetMapping("/status")
    @Operation(summary = "Get RS485 service status", description = "Get current status of RS485 serial communication service")
    public ResponseEntity<Map<String, Object>> getStatus() {
        try {
            Map<String, Object> status = rs485Service.getDeviceStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get RS485 status: " + e.getMessage()));
        }
    }

    @GetMapping("/ports")
    @Operation(summary = "Get available serial ports", description = "Get list of available serial ports for RS485 communication")
    public ResponseEntity<List<String>> getAvailablePorts() {
        try {
            List<String> ports = rs485Config.getAvailablePorts();
            return ResponseEntity.ok(ports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/refresh-ports")
    @Operation(summary = "Refresh available ports", description = "Refresh the list of available serial ports")
    public ResponseEntity<Map<String, Object>> refreshPorts() {
        try {
            rs485Config.refreshAvailablePorts();
            List<String> ports = rs485Config.getAvailablePorts();
            return ResponseEntity.ok(Map.of(
                "message", "Ports refreshed successfully",
                "available_ports", ports,
                "count", ports.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to refresh ports: " + e.getMessage()));
        }
    }

    @PostMapping("/devices/{deviceId}/test")
    @Operation(summary = "Test device communication", description = "Test communication with a specific RS485 device")
    public ResponseEntity<Map<String, Object>> testDevice(@PathVariable String deviceId) {
        try {
            boolean success = rs485Service.testDeviceCommunication(deviceId);
            return ResponseEntity.ok(Map.of(
                "device_id", deviceId,
                "communication_test", success ? "PASSED" : "FAILED",
                "success", success
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to test device: " + e.getMessage()));
        }
    }

    @PostMapping("/inverters/test-all")
    @Operation(summary = "Test all inverters", description = "Run comprehensive tests on all known inverter types")
    public ResponseEntity<Map<String, Object>> testAllInverters() {
        try {
            Map<String, RS485InverterTestService.InverterTestResult> results = inverterTestService.testAllInverters();
            
            int totalTests = results.size();
            long successfulTests = results.values().stream().filter(RS485InverterTestService.InverterTestResult::isSuccess).count();
            
            return ResponseEntity.ok(Map.of(
                "message", "Inverter testing completed",
                "total_tests", totalTests,
                "successful_tests", successfulTests,
                "failed_tests", totalTests - successfulTests,
                "success_rate", String.format("%.1f%%", (double) successfulTests / totalTests * 100),
                "results", results
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to test inverters: " + e.getMessage()));
        }
    }

    @GetMapping("/inverters/test-results")
    @Operation(summary = "Get test results", description = "Get results from the latest inverter tests")
    public ResponseEntity<Map<String, RS485InverterTestService.InverterTestResult>> getTestResults() {
        try {
            Map<String, RS485InverterTestService.InverterTestResult> results = inverterTestService.getAllTestResults();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/inverters/test-report")
    @Operation(summary = "Generate test report", description = "Generate a comprehensive test report for all inverter tests")
    public ResponseEntity<Map<String, String>> getTestReport() {
        try {
            String report = inverterTestService.generateTestReport();
            return ResponseEntity.ok(Map.of("report", report));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate test report: " + e.getMessage()));
        }
    }

    @PostMapping("/devices")
    @Operation(summary = "Add device configuration", description = "Add a new RS485 device configuration")
    public ResponseEntity<Map<String, Object>> addDevice(@RequestBody RS485Configuration.RS485DeviceConfig deviceConfig) {
        try {
            String deviceId = deviceConfig.getManufacturer() + "_" + deviceConfig.getModel() + "_" + System.currentTimeMillis();
            rs485Service.addDevice(deviceId, deviceConfig);
            
            return ResponseEntity.ok(Map.of(
                "message", "Device added successfully",
                "device_id", deviceId,
                "device_config", deviceConfig
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to add device: " + e.getMessage()));
        }
    }

    @DeleteMapping("/devices/{deviceId}")
    @Operation(summary = "Remove device", description = "Remove a device configuration")
    public ResponseEntity<Map<String, Object>> removeDevice(@PathVariable String deviceId) {
        try {
            rs485Service.removeDevice(deviceId);
            return ResponseEntity.ok(Map.of(
                "message", "Device removed successfully",
                "device_id", deviceId
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to remove device: " + e.getMessage()));
        }
    }

    @PostMapping("/devices/{deviceId}/send-command")
    @Operation(summary = "Send command to device", description = "Send a Modbus RTU command to a specific device")
    public ResponseEntity<Map<String, Object>> sendCommand(
            @PathVariable String deviceId,
            @RequestParam int functionCode,
            @RequestParam int startAddress,
            @RequestParam int quantity) {
        try {
            boolean success = rs485Service.sendModbusRTUCommand(deviceId, functionCode, startAddress, quantity);
            return ResponseEntity.ok(Map.of(
                "device_id", deviceId,
                "function_code", functionCode,
                "start_address", startAddress,
                "quantity", quantity,
                "success", success
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to send command: " + e.getMessage()));
        }
    }

    @GetMapping("/configuration")
    @Operation(summary = "Get RS485 configuration", description = "Get current RS485 configuration settings")
    public ResponseEntity<RS485Configuration> getConfiguration() {
        try {
            return ResponseEntity.ok(rs485Config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/configuration/refresh")
    @Operation(summary = "Refresh configuration", description = "Refresh RS485 configuration and reinitialize service")
    public ResponseEntity<Map<String, Object>> refreshConfiguration() {
        try {
            // This would typically involve restarting the service or reloading configuration
            rs485Config.refreshAvailablePorts();
            return ResponseEntity.ok(Map.of(
                "message", "Configuration refreshed successfully",
                "available_ports", rs485Config.getAvailablePorts()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to refresh configuration: " + e.getMessage()));
        }
    }
}


