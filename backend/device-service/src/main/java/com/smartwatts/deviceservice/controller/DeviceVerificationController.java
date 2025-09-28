package com.smartwatts.deviceservice.controller;

import com.smartwatts.deviceservice.dto.DeviceVerificationRequestDto;
import com.smartwatts.deviceservice.dto.DeviceVerificationReviewDto;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.service.DeviceVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/device-verification")
@RequiredArgsConstructor
@Slf4j
public class DeviceVerificationController {

    private final DeviceVerificationService deviceVerificationService;

    /**
     * Submit device for verification (User endpoint)
     */
    @PostMapping("/submit")
    public ResponseEntity<Device> submitForVerification(@RequestBody DeviceVerificationRequestDto request) {
        log.info("Device verification request received for device: {}", request.getDeviceId());
        
        try {
            Device device = deviceVerificationService.submitForVerification(
                request.getDeviceId(), 
                request.getSamplePayload(), 
                request.getNotes()
            );
            
            return ResponseEntity.ok(device);
        } catch (Exception e) {
            log.error("Error submitting device for verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get devices pending verification (Admin endpoint)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Device>> getDevicesPendingVerification() {
        log.info("Fetching devices pending verification");
        
        try {
            List<Device> devices = deviceVerificationService.getDevicesPendingVerification();
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            log.error("Error fetching devices pending verification: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get devices under review (Admin endpoint)
     */
    @GetMapping("/under-review")
    public ResponseEntity<List<Device>> getDevicesUnderReview() {
        log.info("Fetching devices under review");
        
        try {
            List<Device> devices = deviceVerificationService.getDevicesUnderReview();
            return ResponseEntity.ok(devices);
        } catch (Exception e) {
            log.error("Error fetching devices under review: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Review and approve/reject device verification (Admin endpoint)
     */
    @PostMapping("/review")
    public ResponseEntity<Device> reviewVerification(@RequestBody DeviceVerificationReviewDto review) {
        log.info("Device verification review received for device: {} with status: {}", 
                review.getDeviceId(), review.getVerificationStatus());
        
        try {
            Device.VerificationStatus status = Device.VerificationStatus.valueOf(review.getVerificationStatus());
            
            Device device = deviceVerificationService.reviewVerification(
                review.getDeviceId(),
                status,
                review.getNotes(),
                review.getReviewerId()
            );
            
            return ResponseEntity.ok(device);
        } catch (Exception e) {
            log.error("Error reviewing device verification: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark device as under review (Admin endpoint)
     */
    @PostMapping("/{deviceId}/mark-under-review")
    public ResponseEntity<Device> markUnderReview(@PathVariable UUID deviceId, 
                                                @RequestParam UUID reviewerId) {
        log.info("Marking device {} as under review by reviewer {}", deviceId, reviewerId);
        
        try {
            Device device = deviceVerificationService.markUnderReview(deviceId, reviewerId);
            return ResponseEntity.ok(device);
        } catch (Exception e) {
            log.error("Error marking device as under review: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check if device can send data (Device endpoint)
     */
    @GetMapping("/{deviceId}/can-send-data")
    public ResponseEntity<Boolean> canDeviceSendData(@PathVariable UUID deviceId) {
        log.debug("Checking if device {} can send data", deviceId);
        
        try {
            boolean canSend = deviceVerificationService.canDeviceSendData(deviceId);
            return ResponseEntity.ok(canSend);
        } catch (Exception e) {
            log.error("Error checking device data permission: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Validate device auth secret (Device endpoint)
     */
    @PostMapping("/validate-auth")
    public ResponseEntity<Boolean> validateDeviceAuthSecret(@RequestParam UUID deviceId, 
                                                          @RequestParam String authSecret) {
        log.debug("Validating auth secret for device {}", deviceId);
        
        try {
            boolean isValid = deviceVerificationService.validateDeviceAuthSecret(deviceId, authSecret);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Error validating device auth secret: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get verification statistics (Admin endpoint)
     */
    @GetMapping("/stats")
    public ResponseEntity<VerificationStats> getVerificationStats() {
        log.info("Fetching verification statistics");
        
        try {
            // This would typically aggregate stats from multiple users
            // For now, returning mock data
            VerificationStats stats = new VerificationStats();
            stats.setTotalPending(deviceVerificationService.getDevicesPendingVerification().size());
            stats.setTotalUnderReview(deviceVerificationService.getDevicesUnderReview().size());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching verification statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Inner class for verification statistics
    public static class VerificationStats {
        private int totalPending;
        private int totalUnderReview;
        private int totalApproved;
        private int totalRejected;

        // Getters and setters
        public int getTotalPending() { return totalPending; }
        public void setTotalPending(int totalPending) { this.totalPending = totalPending; }
        
        public int getTotalUnderReview() { return totalUnderReview; }
        public void setTotalUnderReview(int totalUnderReview) { this.totalUnderReview = totalUnderReview; }
        
        public int getTotalApproved() { return totalApproved; }
        public void setTotalApproved(int totalApproved) { this.totalApproved = totalApproved; }
        
        public int getTotalRejected() { return totalRejected; }
        public void setTotalRejected(int totalRejected) { this.totalRejected = totalRejected; }
    }
}
