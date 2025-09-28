package com.smartwatts.edge.controller;

import com.smartwatts.edge.model.FacilityAsset;
import com.smartwatts.edge.model.FacilityAlert;
import com.smartwatts.edge.model.FacilityEvent;
import com.smartwatts.edge.service.FacilityIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Controller for facility integration endpoints in the edge gateway
 */
@RestController
@RequestMapping("/api/edge/facility")
@CrossOrigin(origins = "*")
public class FacilityIntegrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(FacilityIntegrationController.class);
    
    private final FacilityIntegrationService facilityIntegrationService;
    
    @Autowired
    public FacilityIntegrationController(FacilityIntegrationService facilityIntegrationService) {
        this.facilityIntegrationService = facilityIntegrationService;
    }
    
    /**
     * Get all cached facility assets
     */
    @GetMapping("/assets")
    public ResponseEntity<Collection<FacilityAsset>> getAllAssets() {
        try {
            logger.info("Retrieving all facility assets from edge cache");
            Collection<FacilityAsset> assets = facilityIntegrationService.getAllAssets();
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            logger.error("Failed to retrieve facility assets", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get a specific facility asset by ID
     */
    @GetMapping("/assets/{assetId}")
    public ResponseEntity<FacilityAsset> getAsset(@PathVariable String assetId) {
        try {
            logger.info("Retrieving facility asset: {}", assetId);
            FacilityAsset asset = facilityIntegrationService.getAsset(assetId);
            
            if (asset != null) {
                return ResponseEntity.ok(asset);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve facility asset: {}", assetId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all active facility alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<Collection<FacilityAlert>> getActiveAlerts() {
        try {
            logger.info("Retrieving active facility alerts");
            Collection<FacilityAlert> alerts = facilityIntegrationService.getActiveAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Failed to retrieve facility alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get event history for a specific asset
     */
    @GetMapping("/assets/{assetId}/events")
    public ResponseEntity<List<FacilityEvent>> getAssetEventHistory(@PathVariable String assetId) {
        try {
            logger.info("Retrieving event history for asset: {}", assetId);
            List<FacilityEvent> events = facilityIntegrationService.getAssetEventHistory(assetId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            logger.error("Failed to retrieve event history for asset: {}", assetId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get facility integration statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIntegrationStats() {
        try {
            logger.info("Retrieving facility integration statistics");
            Map<String, Object> stats = facilityIntegrationService.getIntegrationStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to retrieve facility integration statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Manually trigger facility data synchronization
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, String>> triggerSync() {
        try {
            logger.info("Manually triggering facility data synchronization");
            facilityIntegrationService.syncFacilityData();
            
            Map<String, String> response = Map.of(
                "status", "success",
                "message", "Facility data synchronization triggered successfully"
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to trigger facility data synchronization", e);
            
            Map<String, String> response = Map.of(
                "status", "error",
                "message", "Failed to trigger synchronization: " + e.getMessage()
            );
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Health check endpoint for facility integration
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        try {
            Map<String, String> health = Map.of(
                "status", "healthy",
                "service", "facility-integration",
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Health check failed", e);
            
            Map<String, String> health = Map.of(
                "status", "unhealthy",
                "service", "facility-integration",
                "error", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            
            return ResponseEntity.status(503).body(health);
        }
    }
}
