package com.smartwatts.edge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;


@Service
@RequiredArgsConstructor
@Slf4j
public class EdgeSecurityService {

    private final RestTemplate restTemplate;
    
    @Value("${smartwatts.device-service.url:http://localhost:8083}")
    private String deviceServiceUrl;
    
    @Value("${smartwatts.security.device-verification.enabled:true}")
    private boolean deviceVerificationEnabled;
    
    @Value("${smartwatts.security.auth-secret-validation.enabled:true}")
    private boolean authSecretValidationEnabled;

    /**
     * Validate device can send data (is verified and approved)
     */
    public boolean canDeviceSendData(String deviceId) {
        if (!deviceVerificationEnabled) {
            log.debug("Device verification is disabled, allowing data from device: {}", deviceId);
            return true;
        }

        try {
            String url = deviceServiceUrl + "/api/v1/device-verification/" + deviceId + "/can-send-data";
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            boolean canSend = result != null && result;
            
            if (canSend) {
                log.debug("Device {} is verified and can send data", deviceId);
            } else {
                log.warn("Device {} is not verified or cannot send data", deviceId);
            }
            
            return canSend;
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
    public boolean validateDeviceAuthSecret(String deviceId, String authSecret) {
        if (!authSecretValidationEnabled) {
            log.debug("Auth secret validation is disabled, skipping validation for device: {}", deviceId);
            return true;
        }

        if (authSecret == null || authSecret.isEmpty()) {
            log.warn("Device {} provided no auth secret", deviceId);
            return false;
        }

        try {
            String url = deviceServiceUrl + "/api/v1/device-verification/validate-auth";
            String requestUrl = url + "?deviceId=" + deviceId + "&authSecret=" + authSecret;
            Boolean result = restTemplate.postForObject(requestUrl, null, Boolean.class);
            boolean isValid = result != null && result;
            
            if (isValid) {
                log.debug("Device {} auth secret validated successfully", deviceId);
            } else {
                log.warn("Device {} provided invalid auth secret", deviceId);
            }
            
            return isValid;
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
     * Extract device auth secret from MQTT payload or headers
     * This method parses the MQTT message to find authentication information
     */
    public String extractDeviceAuthSecret(String payload) {
        try {
            // Simple JSON parsing to extract auth_secret field
            // In production, use proper JSON parsing library
            if (payload.contains("\"auth_secret\"")) {
                int startIndex = payload.indexOf("\"auth_secret\"") + 14;
                int endIndex = payload.indexOf("\"", startIndex);
                if (endIndex > startIndex) {
                    return payload.substring(startIndex, endIndex);
                }
            }
            
            // Alternative: check for auth_secret in different formats
            if (payload.contains("auth_secret:")) {
                int startIndex = payload.indexOf("auth_secret:") + 12;
                int endIndex = payload.indexOf(",", startIndex);
                if (endIndex > startIndex) {
                    return payload.substring(startIndex, endIndex).trim();
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error extracting auth secret from payload", e);
            return null;
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

    /**
     * Get device verification status for monitoring
     */
    public String getDeviceVerificationStatus(String deviceId) {
        try {
            boolean canSend = canDeviceSendData(deviceId);
            return canSend ? "VERIFIED" : "UNVERIFIED";
        } catch (Exception e) {
            log.error("Error getting verification status for device: {}", deviceId, e);
            return "UNKNOWN";
        }
    }
}
