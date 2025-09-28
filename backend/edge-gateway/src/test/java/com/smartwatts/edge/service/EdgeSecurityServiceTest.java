package com.smartwatts.edge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdgeSecurityServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EdgeSecurityService edgeSecurityService;

    private static final String DEVICE_ID = "TEST_DEVICE_001";
    private static final String AUTH_SECRET = "valid-auth-secret-123";
    private static final String DEVICE_SERVICE_URL = "http://localhost:8083";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(edgeSecurityService, "deviceServiceUrl", DEVICE_SERVICE_URL);
        ReflectionTestUtils.setField(edgeSecurityService, "deviceVerificationEnabled", true);
        ReflectionTestUtils.setField(edgeSecurityService, "authSecretValidationEnabled", true);
    }

    @Test
    void canDeviceSendData_DeviceVerificationEnabled_Success() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenReturn(true);

        // When
        boolean result = edgeSecurityService.canDeviceSendData(DEVICE_ID);

        // Then
        assertTrue(result);
        verify(restTemplate).getForObject(
            eq(DEVICE_SERVICE_URL + "/api/v1/device-verification/" + DEVICE_ID + "/can-send-data"),
            eq(Boolean.class)
        );
    }

    @Test
    void canDeviceSendData_DeviceVerificationEnabled_Failure() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenReturn(false);

        // When
        boolean result = edgeSecurityService.canDeviceSendData(DEVICE_ID);

        // Then
        assertFalse(result);
        verify(restTemplate).getForObject(anyString(), eq(Boolean.class));
    }

    @Test
    void canDeviceSendData_DeviceVerificationEnabled_ServiceUnavailable() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenThrow(new ResourceAccessException("Service unavailable"));

        // When
        boolean result = edgeSecurityService.canDeviceSendData(DEVICE_ID);

        // Then
        assertFalse(result);
        verify(restTemplate).getForObject(anyString(), eq(Boolean.class));
    }

    @Test
    void canDeviceSendData_DeviceVerificationDisabled() {
        // Given
        ReflectionTestUtils.setField(edgeSecurityService, "deviceVerificationEnabled", false);

        // When
        boolean result = edgeSecurityService.canDeviceSendData(DEVICE_ID);

        // Then
        assertTrue(result);
        verify(restTemplate, never()).getForObject(anyString(), any());
    }

    @Test
    void validateDeviceAuthSecret_AuthSecretValidationEnabled_Success() {
        // Given
        when(restTemplate.postForObject(anyString(), isNull(), eq(Boolean.class)))
            .thenReturn(true);

        // When
        boolean result = edgeSecurityService.validateDeviceAuthSecret(DEVICE_ID, AUTH_SECRET);

        // Then
        assertTrue(result);
        verify(restTemplate).postForObject(
            eq(DEVICE_SERVICE_URL + "/api/v1/device-verification/validate-auth?deviceId=" + DEVICE_ID + "&authSecret=" + AUTH_SECRET),
            isNull(),
            eq(Boolean.class)
        );
    }

    @Test
    void validateDeviceAuthSecret_AuthSecretValidationEnabled_Failure() {
        // Given
        when(restTemplate.postForObject(anyString(), isNull(), eq(Boolean.class)))
            .thenReturn(false);

        // When
        boolean result = edgeSecurityService.validateDeviceAuthSecret(DEVICE_ID, AUTH_SECRET);

        // Then
        assertFalse(result);
        verify(restTemplate).postForObject(anyString(), isNull(), eq(Boolean.class));
    }

    @Test
    void validateDeviceAuthSecret_AuthSecretValidationEnabled_ServiceUnavailable() {
        // Given
        when(restTemplate.postForObject(anyString(), isNull(), eq(Boolean.class)))
            .thenThrow(new ResourceAccessException("Service unavailable"));

        // When
        boolean result = edgeSecurityService.validateDeviceAuthSecret(DEVICE_ID, AUTH_SECRET);

        // Then
        assertFalse(result);
        verify(restTemplate).postForObject(anyString(), isNull(), eq(Boolean.class));
    }

    @Test
    void validateDeviceAuthSecret_AuthSecretValidationDisabled() {
        // Given
        ReflectionTestUtils.setField(edgeSecurityService, "authSecretValidationEnabled", false);

        // When
        boolean result = edgeSecurityService.validateDeviceAuthSecret(DEVICE_ID, AUTH_SECRET);

        // Then
        assertTrue(result);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void validateDeviceAuthSecret_NullAuthSecret() {
        // When
        boolean result = edgeSecurityService.validateDeviceAuthSecret(DEVICE_ID, null);

        // Then
        assertFalse(result);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void validateDeviceAuthSecret_EmptyAuthSecret() {
        // When
        boolean result = edgeSecurityService.validateDeviceAuthSecret(DEVICE_ID, "");

        // Then
        assertFalse(result);
        verify(restTemplate, never()).postForObject(anyString(), any(), any());
    }

    @Test
    void extractDeviceAuthSecret_JsonFormat() {
        // Given
        String payload = "{\"voltage\": 220, \"current\": 5.5, \"auth_secret\": \"secret123\"}";

        // When
        String result = edgeSecurityService.extractDeviceAuthSecret(payload);

        // Then
        assertEquals("secret123", result);
    }

    @Test
    void extractDeviceAuthSecret_AlternativeFormat() {
        // Given
        String payload = "voltage: 220, current: 5.5, auth_secret: secret456";

        // When
        String result = edgeSecurityService.extractDeviceAuthSecret(payload);

        // Then
        assertEquals("secret456", result);
    }

    @Test
    void extractDeviceAuthSecret_NoAuthSecret() {
        // Given
        String payload = "{\"voltage\": 220, \"current\": 5.5}";

        // When
        String result = edgeSecurityService.extractDeviceAuthSecret(payload);

        // Then
        assertNull(result);
    }

    @Test
    void extractDeviceAuthSecret_MalformedPayload() {
        // Given
        String payload = "invalid json {";

        // When
        String result = edgeSecurityService.extractDeviceAuthSecret(payload);

        // Then
        assertNull(result);
    }

    @Test
    void logSecurityEvent_Success() {
        // When
        assertDoesNotThrow(() -> 
            edgeSecurityService.logSecurityEvent(DEVICE_ID, "TEST_EVENT", "Test details", true)
        );
    }

    @Test
    void logSecurityEvent_Failure() {
        // When
        assertDoesNotThrow(() -> 
            edgeSecurityService.logSecurityEvent(DEVICE_ID, "TEST_EVENT", "Test details", false)
        );
    }

    @Test
    void getDeviceVerificationStatus_Success() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenReturn(true);

        // When
        String result = edgeSecurityService.getDeviceVerificationStatus(DEVICE_ID);

        // Then
        assertEquals("VERIFIED", result);
        verify(restTemplate).getForObject(anyString(), eq(Boolean.class));
    }

    @Test
    void getDeviceVerificationStatus_Unverified() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenReturn(false);

        // When
        String result = edgeSecurityService.getDeviceVerificationStatus(DEVICE_ID);

        // Then
        assertEquals("UNVERIFIED", result);
        verify(restTemplate).getForObject(anyString(), eq(Boolean.class));
    }

    @Test
    void getDeviceVerificationStatus_Error() {
        // Given
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
            .thenThrow(new RuntimeException("Test error"));

        // When
        String result = edgeSecurityService.getDeviceVerificationStatus(DEVICE_ID);

        // Then
        assertEquals("UNKNOWN", result);
        verify(restTemplate).getForObject(anyString(), eq(Boolean.class));
    }
}
