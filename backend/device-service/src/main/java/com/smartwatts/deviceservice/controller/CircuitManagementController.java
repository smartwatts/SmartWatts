package com.smartwatts.deviceservice.controller;

import com.smartwatts.deviceservice.model.Circuit;
import com.smartwatts.deviceservice.model.SubPanel;
import com.smartwatts.deviceservice.service.CircuitManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/circuits")
@RequiredArgsConstructor
@Slf4j
public class CircuitManagementController {

    private final CircuitManagementService circuitManagementService;

    /**
     * Create a new circuit
     */
    @PostMapping
    public ResponseEntity<Circuit> createCircuit(@RequestBody Circuit circuit) {
        log.info("Creating circuit: {}", circuit.getName());
        
        try {
            Circuit createdCircuit = circuitManagementService.createCircuit(circuit);
            return ResponseEntity.ok(createdCircuit);
        } catch (Exception e) {
            log.error("Error creating circuit: {}", circuit.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new sub-panel
     */
    @PostMapping("/sub-panels")
    public ResponseEntity<SubPanel> createSubPanel(@RequestBody SubPanel subPanel) {
        log.info("Creating sub-panel: {}", subPanel.getName());
        
        try {
            SubPanel createdSubPanel = circuitManagementService.createSubPanel(subPanel);
            return ResponseEntity.ok(createdSubPanel);
        } catch (Exception e) {
            log.error("Error creating sub-panel: {}", subPanel.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get circuit hierarchy for dashboard
     */
    @GetMapping("/devices/{deviceId}/hierarchy")
    public ResponseEntity<Map<String, Object>> getCircuitHierarchy(@PathVariable UUID deviceId) {
        log.info("Getting circuit hierarchy for device: {}", deviceId);
        
        try {
            Map<String, Object> hierarchy = circuitManagementService.getCircuitHierarchy(deviceId);
            return ResponseEntity.ok(hierarchy);
        } catch (Exception e) {
            log.error("Error getting circuit hierarchy for device: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get circuit tree view data
     */
    @GetMapping("/devices/{deviceId}/tree")
    public ResponseEntity<List<Map<String, Object>>> getCircuitTreeView(@PathVariable UUID deviceId) {
        log.info("Getting circuit tree view for device: {}", deviceId);
        
        try {
            List<Map<String, Object>> treeView = circuitManagementService.getCircuitTreeView(deviceId);
            return ResponseEntity.ok(treeView);
        } catch (Exception e) {
            log.error("Error getting circuit tree view for device: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get circuit load data for monitoring
     */
    @GetMapping("/{circuitId}/load")
    public ResponseEntity<Map<String, Object>> getCircuitLoadData(@PathVariable UUID circuitId) {
        log.info("Getting circuit load data for circuit: {}", circuitId);
        
        try {
            Map<String, Object> loadData = circuitManagementService.getCircuitLoadData(circuitId);
            return ResponseEntity.ok(loadData);
        } catch (Exception e) {
            log.error("Error getting circuit load data for circuit: {}", circuitId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all circuits status for a device
     */
    @GetMapping("/devices/{deviceId}/status")
    public ResponseEntity<List<Map<String, Object>>> getAllCircuitsStatus(@PathVariable UUID deviceId) {
        log.info("Getting all circuits status for device: {}", deviceId);
        
        try {
            List<Map<String, Object>> circuitsStatus = circuitManagementService.getAllCircuitsStatus(deviceId);
            return ResponseEntity.ok(circuitsStatus);
        } catch (Exception e) {
            log.error("Error getting circuits status for device: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update circuit readings from Modbus/CT sensors
     */
    @PutMapping("/{circuitId}/readings")
    public ResponseEntity<Void> updateCircuitReadings(
            @PathVariable UUID circuitId,
            @RequestBody UpdateCircuitReadingsRequest request) {
        
        log.info("Updating circuit readings for circuit: {}", circuitId);
        
        try {
            circuitManagementService.updateCircuitReadings(
                circuitId, 
                request.getCurrentReading(), 
                request.getVoltageReading()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating circuit readings for circuit: {}", circuitId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request DTO for updating circuit readings
     */
    public static class UpdateCircuitReadingsRequest {
        private BigDecimal currentReading;
        private BigDecimal voltageReading;

        // Getters and setters
        public BigDecimal getCurrentReading() { return currentReading; }
        public void setCurrentReading(BigDecimal currentReading) { this.currentReading = currentReading; }
        
        public BigDecimal getVoltageReading() { return voltageReading; }
        public void setVoltageReading(BigDecimal voltageReading) { this.voltageReading = voltageReading; }
    }
}
