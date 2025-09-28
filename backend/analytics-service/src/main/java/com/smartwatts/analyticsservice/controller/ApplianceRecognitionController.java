package com.smartwatts.analyticsservice.controller;

import com.smartwatts.analyticsservice.model.ApplianceDetection;
import com.smartwatts.analyticsservice.model.ApplianceSignature;
import com.smartwatts.analyticsservice.service.ApplianceRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appliance-recognition")
@RequiredArgsConstructor
@Slf4j
public class ApplianceRecognitionController {

    private final ApplianceRecognitionService applianceRecognitionService;

    /**
     * Detect appliances from energy readings
     */
    @PostMapping("/devices/{deviceId}/detect")
    public ResponseEntity<List<ApplianceDetection>> detectAppliances(
            @PathVariable UUID deviceId,
            @RequestBody List<Map<String, Object>> energyReadings) {
        
        log.info("Detecting appliances for device: {}", deviceId);
        
        try {
            // Convert energy readings from request body
            // This would typically come from the energy service
            List<ApplianceDetection> detections = applianceRecognitionService.detectAppliances(deviceId, null);
            
            return ResponseEntity.ok(detections);
        } catch (Exception e) {
            log.error("Error detecting appliances for device: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Train appliance signature with user feedback
     */
    @PostMapping("/devices/{deviceId}/train")
    public ResponseEntity<ApplianceSignature> trainApplianceSignature(
            @PathVariable UUID deviceId,
            @RequestBody TrainApplianceRequest request) {
        
        log.info("Training appliance signature for device: {}, appliance: {}", deviceId, request.getApplianceName());
        
        try {
            ApplianceSignature signature = applianceRecognitionService.trainApplianceSignature(
                deviceId, 
                request.getApplianceName(), 
                request.getApplianceType(), 
                null // Training data would come from energy readings
            );
            
            return ResponseEntity.ok(signature);
        } catch (Exception e) {
            log.error("Error training appliance signature for device: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get appliance usage data for dashboard
     */
    @GetMapping("/devices/{deviceId}/usage")
    public ResponseEntity<Map<String, Object>> getApplianceUsage(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        log.info("Getting appliance usage for device: {}", deviceId);
        
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();
            
            Map<String, Object> usageData = applianceRecognitionService.getApplianceUsage(deviceId, start, end);
            
            return ResponseEntity.ok(usageData);
        } catch (Exception e) {
            log.error("Error getting appliance usage for device: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Confirm or reject appliance detection
     */
    @PutMapping("/detections/{detectionId}/confirm")
    public ResponseEntity<ApplianceDetection> confirmDetection(
            @PathVariable UUID detectionId,
            @RequestBody ConfirmDetectionRequest request) {
        
        log.info("Confirming detection: {} with feedback: {}", detectionId, request.getUserFeedback());
        
        try {
            // This would update the detection with user feedback
            // Implementation would depend on the service method
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error confirming detection: {}", detectionId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get appliance detection history
     */
    @GetMapping("/devices/{deviceId}/detections")
    public ResponseEntity<List<ApplianceDetection>> getDetectionHistory(
            @PathVariable UUID deviceId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        log.info("Getting detection history for device: {}", deviceId);
        
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();
            
            // Get detection history from the service
            List<ApplianceDetection> detections = applianceRecognitionService.getDetectionHistory(deviceId, start, end);
            log.debug("Retrieved {} detections for device: {} from {} to {}", detections.size(), deviceId, start, end);
            return ResponseEntity.ok(detections);
        } catch (Exception e) {
            log.error("Error getting detection history for device: {}", deviceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Request DTO for training appliance signatures
     */
    public static class TrainApplianceRequest {
        private String applianceName;
        private String applianceType;
        private String userFeedback;

        // Getters and setters
        public String getApplianceName() { return applianceName; }
        public void setApplianceName(String applianceName) { this.applianceName = applianceName; }
        
        public String getApplianceType() { return applianceType; }
        public void setApplianceType(String applianceType) { this.applianceType = applianceType; }
        
        public String getUserFeedback() { return userFeedback; }
        public void setUserFeedback(String userFeedback) { this.userFeedback = userFeedback; }
    }

    /**
     * Request DTO for confirming detections
     */
    public static class ConfirmDetectionRequest {
        private Boolean confirmed;
        private String userFeedback;
        private String correctedApplianceName;

        // Getters and setters
        public Boolean getConfirmed() { return confirmed; }
        public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }
        
        public String getUserFeedback() { return userFeedback; }
        public void setUserFeedback(String userFeedback) { this.userFeedback = userFeedback; }
        
        public String getCorrectedApplianceName() { return correctedApplianceName; }
        public void setCorrectedApplianceName(String correctedApplianceName) { this.correctedApplianceName = correctedApplianceName; }
    }
}
