package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.DeviceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceNotificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DeviceNotificationService deviceNotificationService;

    private DeviceEvent testEvent;
    private UUID testDeviceId;

    @BeforeEach
    void setUp() {
        testDeviceId = UUID.randomUUID();
        
        testEvent = new DeviceEvent();
        testEvent.setId(UUID.randomUUID());
        testEvent.setDeviceId(testDeviceId);
        testEvent.setEventType(DeviceEvent.EventType.ERROR_OCCURRED);
        testEvent.setSeverity(DeviceEvent.Severity.ERROR);
        testEvent.setTitle("Test Error");
        testEvent.setMessage("Test error message");
        testEvent.setEventTimestamp(LocalDateTime.now());
        testEvent.setSource("TEST_SYSTEM");
        testEvent.setErrorCode("TEST_ERROR_001");
        testEvent.setErrorMessage("Test error occurred");
        
        // Set configuration values
        ReflectionTestUtils.setField(deviceNotificationService, "emailNotificationsEnabled", true);
        ReflectionTestUtils.setField(deviceNotificationService, "smsNotificationsEnabled", false);
        ReflectionTestUtils.setField(deviceNotificationService, "pushNotificationsEnabled", true);
        ReflectionTestUtils.setField(deviceNotificationService, "webhookNotificationsEnabled", false);
        ReflectionTestUtils.setField(deviceNotificationService, "emailServiceUrl", "http://localhost:8085");
        ReflectionTestUtils.setField(deviceNotificationService, "pushServiceUrl", "http://localhost:8087");
    }

    @Test
    void sendEventNotification_ErrorEvent_SendsEmailAndPush() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        deviceNotificationService.sendEventNotification(testEvent);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8087/api/v1/push/send"), any(), eq(String.class));
        // SMS should not be called for ERROR events (only CRITICAL)
        verify(restTemplate, never()).postForObject(
                contains("sms"), any(), eq(String.class));
    }

    @Test
    void sendEventNotification_CriticalEvent_SendsAllNotifications() {
        // Given
        testEvent.setSeverity(DeviceEvent.Severity.CRITICAL);
        ReflectionTestUtils.setField(deviceNotificationService, "smsNotificationsEnabled", true);
        ReflectionTestUtils.setField(deviceNotificationService, "smsServiceUrl", "http://localhost:8086");
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        deviceNotificationService.sendEventNotification(testEvent);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8087/api/v1/push/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8086/api/v1/sms/send"), any(), eq(String.class));
    }

    @Test
    void sendEventNotification_InfoEvent_NoNotifications() {
        // Given
        testEvent.setSeverity(DeviceEvent.Severity.INFO);
        testEvent.setEventType(DeviceEvent.EventType.DATA_RECEIVED);

        // When
        deviceNotificationService.sendEventNotification(testEvent);

        // Then
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    void sendEventNotification_ImportantWarningEvent_SendsEmailAndPush() {
        // Given
        testEvent.setSeverity(DeviceEvent.Severity.WARNING);
        testEvent.setEventType(DeviceEvent.EventType.CONNECTION_LOST);
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        deviceNotificationService.sendEventNotification(testEvent);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8087/api/v1/push/send"), any(), eq(String.class));
    }

    @Test
    void sendEventNotification_ServiceFailure_ContinuesProcessing() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then - Should not throw exception
        deviceNotificationService.sendEventNotification(testEvent);

        // Verify that all enabled services were attempted
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8087/api/v1/push/send"), any(), eq(String.class));
    }

    @Test
    void sendTestNotification_Success() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        deviceNotificationService.sendTestNotification(testDeviceId, "Test message");

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }

    @Test
    void sendEventNotification_AllServicesDisabled_NoCalls() {
        // Given
        ReflectionTestUtils.setField(deviceNotificationService, "emailNotificationsEnabled", false);
        ReflectionTestUtils.setField(deviceNotificationService, "pushNotificationsEnabled", false);

        // When
        deviceNotificationService.sendEventNotification(testEvent);

        // Then
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    void sendEventNotification_WebhookEnabled_SendsWebhook() {
        // Given
        ReflectionTestUtils.setField(deviceNotificationService, "webhookNotificationsEnabled", true);
        ReflectionTestUtils.setField(deviceNotificationService, "webhookUrl", "http://localhost:8088/webhook");
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        deviceNotificationService.sendEventNotification(testEvent);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8087/api/v1/push/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8088/webhook"), any(), eq(String.class));
    }
}
