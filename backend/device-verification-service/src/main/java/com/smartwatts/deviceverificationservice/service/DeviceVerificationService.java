package com.smartwatts.deviceverificationservice.service;

import com.smartwatts.deviceverificationservice.model.*;
import com.smartwatts.deviceverificationservice.repository.DeviceVerificationRepository;
import com.smartwatts.deviceverificationservice.repository.ActivationTokenRepository;
import com.smartwatts.deviceverificationservice.repository.VerificationAuditLogRepository;
import com.smartwatts.deviceverificationservice.dto.*;
import com.smartwatts.deviceverificationservice.util.JwtTokenUtil;
import com.smartwatts.deviceverificationservice.model.VerificationAuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceVerificationService {

    private final DeviceVerificationRepository deviceVerificationRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final VerificationAuditLogRepository auditLogRepository;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Device Activation Flow
     * Implements the dual token validity system:
     * - Residential: 12 months initially
     * - Commercial: 3 months initially, 12 months on renewal
     */
    @Transactional
    public DeviceActivationResponse activateDevice(DeviceActivationRequest request) {
        log.info("Processing device activation for device: {}", request.getDeviceId());

        try {
            // 1. Validate device metadata
            if (!validateDeviceMetadata(request)) {
                log.warn("Device metadata validation failed for device: {}", request.getDeviceId());
                return DeviceActivationResponse.builder()
                    .success(false)
                    .message("Device metadata validation failed")
                    .build();
            }

            // 2. Determine trust category and customer type
            DeviceTrustCategory trustCategory = determineTrustCategory(request);
            String customerType = determineCustomerType(request);
            
            // 3. Check if device already exists
            Optional<DeviceVerification> existingDevice = deviceVerificationRepository.findByDeviceId(request.getDeviceId());
            
            if (existingDevice.isPresent()) {
                // Device exists - handle renewal
                return handleDeviceRenewal(existingDevice.get(), request, customerType);
            } else {
                // New device - handle initial activation
                return handleNewDeviceActivation(request, trustCategory, customerType);
            }

        } catch (Exception e) {
            log.error("Error during device activation for device: {}", request.getDeviceId(), e);
            return DeviceActivationResponse.builder()
                .success(false)
                .message("Device activation failed: " + e.getMessage())
                .build();
        }
    }

    /**
     * Handle new device activation with initial token validity
     */
    private DeviceActivationResponse handleNewDeviceActivation(
            DeviceActivationRequest request, 
            DeviceTrustCategory trustCategory, 
            String customerType) {
        
        // Calculate initial validity based on customer type
        int initialValidityDays = customerType.equalsIgnoreCase("RESIDENTIAL") ? 365 : 90;
        LocalDateTime activatedAt = LocalDateTime.now();
        LocalDateTime expiresAt = activatedAt.plusDays(initialValidityDays);

        // Create device verification record
        DeviceVerification deviceVerification = DeviceVerification.builder()
            .deviceId(request.getDeviceId())
            .deviceType(request.getDeviceType())
            .hardwareId(request.getHardwareId())
            .firmwareHash(request.getFirmwareHash())
            .firmwareVersion(request.getFirmwareVersion())
            .trustCategory(trustCategory)
            .status(DeviceStatus.ACTIVE)
            .customerType(customerType)
            .customerId(request.getCustomerId())
            .installerId(null) // Will be set by installer tier determination
            .installerTier(InstallerTier.BASIC) // Default tier for now
            .locationLat(request.getLocationLat())
            .locationLng(request.getLocationLng())
            .activatedAt(activatedAt)
            .expiresAt(expiresAt)
            .activationAttempts(0)
            .tamperDetected(false)
            .dockerStartupValid(true)
            .build();

        deviceVerification = deviceVerificationRepository.save(deviceVerification);

        // Generate activation token
        String token = jwtTokenUtil.generateActivationToken(
            deviceVerification.getDeviceId(),
            activatedAt,
            expiresAt,
            customerType,
            initialValidityDays
        );

        // Save activation token
        ActivationToken activationToken = ActivationToken.builder()
            .deviceId(deviceVerification.getDeviceId())
            .tokenHash(jwtTokenUtil.hashToken(token))
            .tokenType("ONLINE")
            .issuedAt(LocalDateTime.now())
            .activatedAt(activatedAt)
            .expiresAt(expiresAt)
            .customerType(customerType)
            .validityDays(initialValidityDays)
            .isActive(true)
            .build();

        activationTokenRepository.save(activationToken);

        // Log activation (temporarily disabled due to IP address type issue)
        // logActivation(deviceVerification, "INITIAL_ACTIVATION", true, null);

        log.info("Device {} activated successfully. Customer type: {}, Validity: {} days", 
            request.getDeviceId(), customerType, initialValidityDays);

        return DeviceActivationResponse.builder()
            .success(true)
            .deviceId(deviceVerification.getDeviceId())
            .activationToken(token)
            .activatedAt(activatedAt)
            .expiresAt(expiresAt)
            .validityDays(initialValidityDays)
            .customerType(customerType)
            .trustCategory(trustCategory)
            .message("Device activated successfully")
            .build();
    }

    /**
     * Handle device renewal with 12-month validity for all customers
     */
    private DeviceActivationResponse handleDeviceRenewal(
            DeviceVerification existingDevice, 
            DeviceActivationRequest request, 
            String customerType) {
        
        // For renewals, all customers get 12 months
        int renewalValidityDays = 365;
        LocalDateTime renewedAt = LocalDateTime.now();
        LocalDateTime newExpiresAt = renewedAt.plusDays(renewalValidityDays);

        // Update existing device
        existingDevice.setStatus(DeviceStatus.ACTIVE);
        existingDevice.setActivatedAt(renewedAt);
        existingDevice.setExpiresAt(newExpiresAt);
        existingDevice.setCustomerType(customerType);
        existingDevice.setCustomerId(request.getCustomerId());
        existingDevice.setInstallerId(null); // Will be set by installer tier determination
        existingDevice.setInstallerTier(InstallerTier.BASIC); // Default tier for now
        existingDevice.setLocationLat(request.getLocationLat());
        existingDevice.setLocationLng(request.getLocationLng());
        existingDevice.setTamperDetected(false);
        existingDevice.setDockerStartupValid(true);
        existingDevice.resetActivationAttempts();

        existingDevice = deviceVerificationRepository.save(existingDevice);

        // Revoke old tokens
        List<ActivationToken> oldTokens = activationTokenRepository.findByDeviceIdAndIsActiveTrue(existingDevice.getDeviceId());
        oldTokens.forEach(token -> token.revoke("Renewed"));

        // Generate new activation token
        String newToken = jwtTokenUtil.generateActivationToken(
            existingDevice.getDeviceId(),
            renewedAt,
            newExpiresAt,
            customerType,
            renewalValidityDays
        );

        // Save new activation token
        ActivationToken newActivationToken = ActivationToken.builder()
            .deviceId(existingDevice.getDeviceId())
            .tokenHash(jwtTokenUtil.hashToken(newToken))
            .tokenType("ONLINE")
            .issuedAt(LocalDateTime.now())
            .activatedAt(renewedAt)
            .expiresAt(newExpiresAt)
            .customerType(customerType)
            .validityDays(renewalValidityDays)
            .isActive(true)
            .build();

        activationTokenRepository.save(newActivationToken);

        // Log renewal (temporarily disabled due to IP address type issue)
        // logActivation(existingDevice, "RENEWAL", true, null);

        log.info("Device {} renewed successfully. Customer type: {}, Validity: {} days", 
            existingDevice.getDeviceId(), customerType, renewalValidityDays);

        return DeviceActivationResponse.builder()
            .success(true)
            .deviceId(existingDevice.getDeviceId())
            .activationToken(newToken)
            .activatedAt(renewedAt)
            .expiresAt(newExpiresAt)
            .validityDays(renewalValidityDays)
            .customerType(customerType)
            .trustCategory(existingDevice.getTrustCategory())
            .message("Device renewed successfully")
            .build();
    }

    /**
     * Validate incoming device data or dashboard access
     */
    public DeviceValidationResponse validateDeviceAccess(String deviceId, String token) {
        log.info("Validating device access for device: {}", deviceId);

        try {
            // 1. Extract and validate token
            if (!jwtTokenUtil.validateToken(token)) {
                log.warn("Invalid token for device: {}", deviceId);
                return DeviceValidationResponse.builder()
                    .valid(false)
                    .message("Invalid activation token")
                    .build();
            }

            // 2. Get device verification
            Optional<DeviceVerification> deviceOpt = deviceVerificationRepository.findByDeviceId(deviceId);
            if (deviceOpt.isEmpty()) {
                log.warn("Device not found: {}", deviceId);
                return DeviceValidationResponse.builder()
                    .valid(false)
                    .message("Device not found")
                    .build();
            }

            DeviceVerification device = deviceOpt.get();

            // 3. Check if device is expired
            if (device.isExpired()) {
                log.warn("Device {} activation expired", deviceId);
                device.setStatus(DeviceStatus.EXPIRED);
                deviceVerificationRepository.save(device);
                
                return DeviceValidationResponse.builder()
                    .valid(false)
                    .message("Device activation expired. Please renew verification to continue service.")
                    .build();
            }

            // 4. Check device status
            if (!DeviceStatus.ACTIVE.equals(device.getStatus())) {
                log.warn("Device {} is not active. Status: {}", deviceId, device.getStatus());
                return DeviceValidationResponse.builder()
                    .valid(false)
                    .message("Device is not active")
                    .build();
            }

            // 5. Check for tampering
            if (Boolean.TRUE.equals(device.getTamperDetected())) {
                log.warn("Device {} tampering detected", deviceId);
                return DeviceValidationResponse.builder()
                    .valid(false)
                    .message("Device tampering detected")
                    .build();
            }

            // Device is valid
            log.info("Device {} access validated successfully", deviceId);
            return DeviceValidationResponse.builder()
                .valid(true)
                .deviceId(deviceId)
                .customerType(device.getCustomerType())
                .trustCategory(device.getTrustCategory())
                .expiresAt(device.getExpiresAt())
                .message("Device access granted")
                .build();

        } catch (Exception e) {
            log.error("Error validating device access for device: {}", deviceId, e);
            return DeviceValidationResponse.builder()
                .valid(false)
                .message("Device validation failed: " + e.getMessage())
                .build();
        }
    }

    // Helper methods
    private boolean validateDeviceMetadata(DeviceActivationRequest request) {
        return request.getDeviceId() != null && !request.getDeviceId().trim().isEmpty() &&
               request.getHardwareId() != null && !request.getHardwareId().trim().isEmpty() &&
               request.getDeviceType() != null && !request.getDeviceType().trim().isEmpty();
    }

    private DeviceTrustCategory determineTrustCategory(DeviceActivationRequest request) {
        // Logic to determine trust category based on installer, hardware, etc.
        if ("OEM_SMARTWATTS".equals(request.getInstallerId())) {
            return DeviceTrustCategory.OEM_LOCKED;
        } else if (request.isOfflineActivation()) {
            return DeviceTrustCategory.OFFLINE_LOCKED;
        } else {
            return DeviceTrustCategory.UNVERIFIED;
        }
    }

    private String determineCustomerType(DeviceActivationRequest request) {
        // Logic to determine customer type based on user account, location, etc.
        // For now, default to RESIDENTIAL - this would integrate with User Service
        return request.getCustomerType() != null ? request.getCustomerType() : "RESIDENTIAL";
    }

    @SuppressWarnings("unused")
    private InstallerTier determineInstallerTier(String installerId) {
        // Logic to determine installer tier
        if ("OEM_SMARTWATTS".equals(installerId)) {
            return InstallerTier.ENTERPRISE;
        } else {
            return InstallerTier.BASIC; // Default tier
        }
    }

    @SuppressWarnings("unused")
    private void logActivation(DeviceVerification device, String action, boolean success, String errorMessage) {
        try {
            VerificationAuditLog auditLog = VerificationAuditLog.builder()
                .deviceId(device.getDeviceId())
                .action(action)
                .actionDetails(String.format("Device %s %s", device.getDeviceId(), action.toLowerCase()))
                .success(success)
                .errorMessage(errorMessage)
                .ipAddress(null) // Set to null to avoid IP address type conversion issues
                .metadata(String.format("{\"customerType\":\"%s\",\"trustCategory\":\"%s\",\"installerId\":\"%s\"}", 
                    device.getCustomerType(), device.getTrustCategory(), device.getInstallerId()))
                .build();
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log activation for device: {}", device.getDeviceId(), e);
        }
    }
}
