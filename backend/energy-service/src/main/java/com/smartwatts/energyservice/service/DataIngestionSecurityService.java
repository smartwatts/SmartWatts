package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.dto.EnergyReadingDto;
import com.smartwatts.energyservice.exception.DeviceNotVerifiedException;
import com.smartwatts.energyservice.exception.InvalidDeviceAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;


@Service
@RequiredArgsConstructor
@Slf4j
public class DataIngestionSecurityService {

    private final RestTemplate restTemplate;
    
    @Value("${smartwatts.device-service.url:http://localhost:8083}")
    private String deviceServiceUrl;
    
    @Value("${smartwatts.security.device-verification.enabled:true}")
    private boolean deviceVerificationEnabled;
    
    @Value("${smartwatts.security.auth-secret-validation.enabled:true}")
    private boolean authSecretValidationEnabled;

    /**
     * Validate device data ingestion request
     * This is the main security gate for all device data
     */
    public void validateDeviceDataIngestion(EnergyReadingDto readingDto, String deviceAuthSecret) {
        if (!deviceVerificationEnabled) {
            log.debug("Device verification is disabled, skipping validation");
            return;
        }

        String deviceId = readingDto.getDeviceId();
        log.debug("Validating device data ingestion for device: {}", deviceId);

        try {
            // Step 1: Check if device is verified and can send data
            boolean canSendData = checkDeviceCanSendData(deviceId);
            if (!canSendData) {
                log.warn("Device {} is not verified or cannot send data", deviceId);
                throw new DeviceNotVerifiedException("Device " + deviceId + " is not verified or cannot send data");
            }

            // Step 2: Validate device auth secret if provided
            if (authSecretValidationEnabled && deviceAuthSecret != null && !deviceAuthSecret.isEmpty()) {
                boolean isValidAuth = validateDeviceAuthSecret(deviceId, deviceAuthSecret);
                if (!isValidAuth) {
                    log.warn("Invalid auth secret provided for device: {}", deviceId);
                    throw new InvalidDeviceAuthException("Invalid authentication secret for device " + deviceId);
                }
                log.debug("Device {} auth secret validated successfully", deviceId);
            } else if (authSecretValidationEnabled) {
                log.warn("Device auth secret required but not provided for device: {}", deviceId);
                throw new InvalidDeviceAuthException("Device authentication secret is required for device " + deviceId);
            }

            log.info("Device {} data ingestion validation successful", deviceId);

        } catch (DeviceNotVerifiedException | InvalidDeviceAuthException e) {
            // Re-throw security exceptions
            throw e;
        } catch (Exception e) {
            log.error("Error during device data ingestion validation for device: {}", deviceId, e);
            // For any other errors, we block the data ingestion as a security measure
            throw new DeviceNotVerifiedException("Unable to validate device " + deviceId + " - access denied");
        }
    }

    /**
     * Check if device can send data (is verified and approved)
     */
    private boolean checkDeviceCanSendData(String deviceId) {
        try {
            String url = deviceServiceUrl + "/api/v1/device-verification/" + deviceId + "/can-send-data";
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return result != null && result;
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to device service to validate device: {}", deviceId, e);
            // If we can't reach the device service, we block the data for security
            return false;
        } catch (Exception e) {
            log.error("Error checking if device {} can send data", deviceId, e);
            return false;
        }
    }

    /**
     * Validate device authentication secret
     */
    private boolean validateDeviceAuthSecret(String deviceId, String authSecret) {
        try {
            String url = deviceServiceUrl + "/api/v1/device-verification/validate-auth";
            String requestUrl = url + "?deviceId=" + deviceId + "&authSecret=" + authSecret;
            Boolean result = restTemplate.postForObject(requestUrl, null, Boolean.class);
            return result != null && result;
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to device service to validate auth secret for device: {}", deviceId, e);
            // If we can't reach the device service, we block the data for security
            return false;
        } catch (Exception e) {
            log.error("Error validating auth secret for device: {}", deviceId, e);
            return false;
        }
    }

    /**
     * Get device verification status for monitoring purposes
     */
    public String getDeviceVerificationStatus(String deviceId) {
        try {
            String url = deviceServiceUrl + "/api/v1/devices/" + deviceId;
            log.debug("Checking device verification status at URL: {}", url);
            // This would return device info including verification status
            // For now, we'll just check if it can send data
            boolean canSend = checkDeviceCanSendData(deviceId);
            return canSend ? "VERIFIED" : "UNVERIFIED";
        } catch (Exception e) {
            log.error("Error getting verification status for device: {}", deviceId, e);
            return "UNKNOWN";
        }
    }

    /**
     * Log security event for audit purposes
     */
    public void logSecurityEvent(String deviceId, String eventType, String details, boolean success) {
        if (success) {
            log.info("SECURITY_SUCCESS - Device: {}, Event: {}, Details: {}", deviceId, eventType, details);
        } else {
            log.warn("SECURITY_FAILURE - Device: {}, Event: {}, Details: {}", deviceId, eventType, details);
        }
        // In production, this would also log to a security audit system
    }
}
