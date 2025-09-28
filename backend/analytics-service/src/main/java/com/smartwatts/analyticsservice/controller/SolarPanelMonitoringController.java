package com.smartwatts.analyticsservice.controller;

import com.smartwatts.analyticsservice.service.SolarPanelMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solar")
@RequiredArgsConstructor
@Slf4j
public class SolarPanelMonitoringController {

    private final SolarPanelMonitoringService solarPanelMonitoringService;

    /**
     * Update solar panel data from inverter API
     */
    @PostMapping("/inverters/{inverterId}/update")
    public ResponseEntity<Void> updateSolarPanelData(@PathVariable UUID inverterId) {
        log.info("Updating solar panel data for inverter: {}", inverterId);
        
        try {
            solarPanelMonitoringService.updateSolarPanelData(inverterId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating solar panel data for inverter: {}", inverterId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get solar array heatmap data for dashboard
     */
    @GetMapping("/inverters/{inverterId}/heatmap")
    public ResponseEntity<Map<String, Object>> getSolarArrayHeatmap(@PathVariable UUID inverterId) {
        log.info("Getting solar array heatmap for inverter: {}", inverterId);
        
        try {
            Map<String, Object> heatmapData = solarPanelMonitoringService.getSolarArrayHeatmap(inverterId);
            return ResponseEntity.ok(heatmapData);
        } catch (Exception e) {
            log.error("Error getting solar array heatmap for inverter: {}", inverterId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get real-time vs historical solar production comparison
     */
    @GetMapping("/inverters/{inverterId}/comparison")
    public ResponseEntity<Map<String, Object>> getSolarProductionComparison(
            @PathVariable UUID inverterId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        log.info("Getting solar production comparison for inverter: {}", inverterId);
        
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();
            
            Map<String, Object> comparisonData = solarPanelMonitoringService.getSolarProductionComparison(inverterId, start, end);
            return ResponseEntity.ok(comparisonData);
        } catch (Exception e) {
            log.error("Error getting solar production comparison for inverter: {}", inverterId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Detect solar panel faults
     */
    @GetMapping("/inverters/{inverterId}/faults")
    public ResponseEntity<List<Map<String, Object>>> detectSolarFaults(@PathVariable UUID inverterId) {
        log.info("Detecting solar faults for inverter: {}", inverterId);
        
        try {
            List<Map<String, Object>> faults = solarPanelMonitoringService.detectSolarFaults(inverterId);
            return ResponseEntity.ok(faults);
        } catch (Exception e) {
            log.error("Error detecting solar faults for inverter: {}", inverterId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get solar panel performance analytics
     */
    @GetMapping("/inverters/{inverterId}/analytics")
    public ResponseEntity<Map<String, Object>> getSolarPerformanceAnalytics(
            @PathVariable UUID inverterId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        log.info("Getting solar performance analytics for inverter: {}", inverterId);
        
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();
            
            Map<String, Object> analytics = solarPanelMonitoringService.getSolarPerformanceAnalytics(inverterId, start, end);
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting solar performance analytics for inverter: {}", inverterId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
