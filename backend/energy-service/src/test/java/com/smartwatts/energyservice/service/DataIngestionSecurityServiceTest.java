package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.dto.EnergyReadingDto;
import com.smartwatts.energyservice.exception.DeviceNotVerifiedException;
import com.smartwatts.energyservice.exception.InvalidDeviceAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataIngestionSecurityServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DataIngestionSecurityService securityService;

    private EnergyReadingDto testReading;
    private String deviceId;
    private String deviceAuthSecret;

    @BeforeEach
    void setUp() {
        deviceId = UUID.randomUUID().toString();
        deviceAuthSecret = "test-auth-secret-123";
        
        testReading = EnergyReadingDto.builder()
                .deviceId(deviceId)
                .readingTimestamp(LocalDateTime.now())
                .voltage(new BigDecimal("220.5"))
                .current(new BigDecimal("5.2"))
                .power(new BigDecimal("1146.6"))
                .energyConsumed(new BigDecimal("2.3"))
                .frequency(new BigDecimal("50.0"))
                .powerFactor(new BigDecimal("0.95"))
                .build();
    }

    @Test
    void validateDeviceDataIngestion_Success() {
        // Mock successful verification
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("true", HttpStatus.OK));

        // Should not throw exception
        assertDoesNotThrow(() -> 
            securityService.validateDeviceDataIngestion(testReading, deviceAuthSecret)
        );

        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/can-send-data"), 
            eq(String.class)
        );
    }

    @Test
    void validateDeviceDataIngestion_DeviceNotVerified() {
        // Mock device cannot send data
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("false", HttpStatus.OK));

        // Should throw DeviceNotVerifiedException
        DeviceNotVerifiedException exception = assertThrows(
            DeviceNotVerifiedException.class,
            () -> securityService.validateDeviceDataIngestion(testReading, deviceAuthSecret)
        );

        assertTrue(exception.getMessage().contains(deviceId));
        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/can-send-data"), 
            eq(String.class)
        );
    }

    @Test
    void validateDeviceDataIngestion_ServiceUnavailable() {
        // Mock service unavailable
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        // Should throw DeviceNotVerifiedException (fail secure)
        DeviceNotVerifiedException exception = assertThrows(
            DeviceNotVerifiedException.class,
            () -> securityService.validateDeviceDataIngestion(testReading, deviceAuthSecret)
        );

        assertTrue(exception.getMessage().contains(deviceId));
        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/can-send-data"), 
            eq(String.class)
        );
    }

    @Test
    void validateDeviceDataIngestion_WithAuthSecret() {
        // Mock successful verification
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("true", HttpStatus.OK));

        // Mock successful auth validation
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("true", HttpStatus.OK));

        // Should not throw exception
        assertDoesNotThrow(() -> 
            securityService.validateDeviceDataIngestion(testReading, deviceAuthSecret)
        );

        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/can-send-data"), 
            eq(String.class)
        );
        verify(restTemplate, times(1)).postForEntity(
            contains("/devices/verification/validate-auth"), 
            any(), 
            eq(String.class)
        );
    }

    @Test
    void validateDeviceDataIngestion_InvalidAuthSecret() {
        // Mock successful verification
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("true", HttpStatus.OK));

        // Mock invalid auth secret
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("false", HttpStatus.OK));

        // Should throw InvalidDeviceAuthException
        InvalidDeviceAuthException exception = assertThrows(
            InvalidDeviceAuthException.class,
            () -> securityService.validateDeviceDataIngestion(testReading, deviceAuthSecret)
        );

        assertTrue(exception.getMessage().contains(deviceId));
        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/can-send-data"), 
            eq(String.class)
        );
        verify(restTemplate, times(1)).postForEntity(
            contains("/devices/verification/validate-auth"), 
            any(), 
            eq(String.class)
        );
    }

    @Test
    void validateDeviceDataIngestion_WithoutAuthSecret() {
        // Mock successful verification
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("true", HttpStatus.OK));

        // Should not throw exception (auth secret is optional)
        assertDoesNotThrow(() -> 
            securityService.validateDeviceDataIngestion(testReading, null)
        );

        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/can-send-data"), 
            eq(String.class)
        );
        // Should not call auth validation
        verify(restTemplate, never()).postForEntity(
            contains("/devices/verification/validate-auth"), 
            any(), 
            eq(String.class)
        );
    }











    @Test
    void getDeviceVerificationStatus_Success() {
        String expectedStatus = "APPROVED";
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(expectedStatus, HttpStatus.OK));

        String result = securityService.getDeviceVerificationStatus(deviceId);

        assertEquals(expectedStatus, result);
        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/status"), 
            eq(String.class)
        );
    }

    @Test
    void getDeviceVerificationStatus_ServiceUnavailable() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        // Should return null (fail gracefully)
        String result = securityService.getDeviceVerificationStatus(deviceId);

        assertNull(result);
        verify(restTemplate, times(1)).getForEntity(
            contains("/devices/verification/" + deviceId + "/status"), 
            eq(String.class)
        );
    }

    @Test
    void logSecurityEvent_Success() {
        // Should not throw exception
        assertDoesNotThrow(() -> 
            securityService.logSecurityEvent(deviceId, "TEST_EVENT", "Test security event", true)
        );

        // In a real implementation, this would log to a security audit system
        // For now, we just verify the method doesn't throw
    }

    @Test
    void logSecurityEvent_WithNullValues() {
        // Should handle null values gracefully
        assertDoesNotThrow(() -> 
            securityService.logSecurityEvent(null, null, null, false)
        );
    }
}
