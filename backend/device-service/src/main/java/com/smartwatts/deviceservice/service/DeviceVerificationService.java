package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceVerificationService {

    private final DeviceRepository deviceRepository;
    private final DeviceEventService eventService;

    /**
     * Submit a device for verification
     */
    @Transactional
    public Device submitForVerification(UUID deviceId, String samplePayload, String notes) {
        log.info("Submitting device {} for verification", deviceId);
        
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        // Check if device is already verified
        if (device.getIsVerified()) {
            throw new RuntimeException("Device is already verified");
        }
        
        // Update verification fields
        device.setVerificationStatus(Device.VerificationStatus.PENDING);
        device.setSamplePayload(samplePayload);
        device.setVerificationNotes(notes);
        device.setVerificationRequestDate(LocalDateTime.now());
        
        Device savedDevice = deviceRepository.save(device);
        
        // Create verification request event
        eventService.logDeviceEvent(deviceId, "VERIFICATION_SUBMITTED", "Device verification request submitted for review");
        
        log.info("Device {} submitted for verification", deviceId);
        return savedDevice;
    }

    /**
     * Review and approve/reject device verification
     */
    @Transactional
    public Device reviewVerification(UUID deviceId, Device.VerificationStatus status, 
                                   String notes, UUID reviewerId) {
        log.info("Reviewing device {} verification with status: {}", deviceId, status);
        
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        // Update verification fields
        device.setVerificationStatus(status);
        device.setVerificationNotes(notes);
        device.setVerificationReviewDate(LocalDateTime.now());
        device.setVerificationReviewer(reviewerId);
        
        if (status == Device.VerificationStatus.APPROVED) {
            device.setIsVerified(true);
            device.setVerificationDate(LocalDateTime.now());
            device.setVerificationBy(reviewerId);
            
            // Generate device auth secret for approved devices
            String authSecret = generateDeviceAuthSecret();
            device.setDeviceAuthSecret(authSecret);
            
            log.info("Device {} verification approved, auth secret generated", deviceId);
        } else if (status == Device.VerificationStatus.REJECTED) {
            device.setIsVerified(false);
            log.info("Device {} verification rejected", deviceId);
        }
        
        Device savedDevice = deviceRepository.save(device);
        
        // Create verification review event
        String eventMessage = status == Device.VerificationStatus.APPROVED ? 
                "Device verification approved" : "Device verification rejected";
        
        eventService.logDeviceVerificationEvent(deviceId, status, eventMessage);
        
        return savedDevice;
    }

    /**
     * Get devices pending verification
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesPendingVerification() {
        return deviceRepository.findByVerificationStatus(Device.VerificationStatus.PENDING);
    }

    /**
     * Get devices under review
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesUnderReview() {
        return deviceRepository.findByVerificationStatus(Device.VerificationStatus.UNDER_REVIEW);
    }

    /**
     * Get verified devices count by user
     */
    @Transactional(readOnly = true)
    public long getVerifiedDevicesCount(UUID userId) {
        return deviceRepository.countVerifiedDevicesByUserId(userId);
    }

    /**
     * Get unverified devices count by user
     */
    @Transactional(readOnly = true)
    public long getUnverifiedDevicesCount(UUID userId) {
        return deviceRepository.countByUserIdAndVerificationStatus(userId, Device.VerificationStatus.PENDING);
    }

    /**
     * Check if device is verified and can send data
     */
    @Transactional(readOnly = true)
    public boolean canDeviceSendData(UUID deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            return device.getIsVerified() && 
                   device.getVerificationStatus() == Device.VerificationStatus.APPROVED;
        }
        return false;
    }

    /**
     * Validate device auth secret
     */
    @Transactional(readOnly = true)
    public boolean validateDeviceAuthSecret(UUID deviceId, String authSecret) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceAuthSecret(authSecret);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            return device.getId().equals(deviceId) && device.getIsVerified();
        }
        return false;
    }

    /**
     * Generate unique device auth secret
     */
    private String generateDeviceAuthSecret() {
        return "SW_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Mark device as under review
     */
    @Transactional
    public Device markUnderReview(UUID deviceId, UUID reviewerId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        device.setVerificationStatus(Device.VerificationStatus.UNDER_REVIEW);
        device.setVerificationReviewer(reviewerId);
        
        return deviceRepository.save(device);
    }

    /**
     * Get device verification status by device ID string
     */
    @Transactional(readOnly = true)
    public String getDeviceVerificationStatus(String deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (deviceOpt.isPresent()) {
            return deviceOpt.get().getVerificationStatus().toString();
        }
        return null;
    }

    /**
     * Generate device auth secret for a device
     */
    @Transactional
    public String generateDeviceAuthSecret(UUID deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found with ID: " + deviceId));
        
        String authSecret = generateDeviceAuthSecret();
        device.setDeviceAuthSecret(authSecret);
        deviceRepository.save(device);
        
        return authSecret;
    }

    /**
     * Get device verification details by device ID string
     */
    @Transactional(readOnly = true)
    public Device getDeviceVerificationDetails(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId).orElse(null);
    }
}
