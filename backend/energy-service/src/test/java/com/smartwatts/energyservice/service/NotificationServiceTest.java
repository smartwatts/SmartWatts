package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.model.EnergyAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private EnergyAlert testAlert;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testAlert = new EnergyAlert();
        testAlert.setId(UUID.randomUUID());
        testAlert.setUserId(testUserId);
        testAlert.setDeviceId("TEST_DEVICE_001");
        testAlert.setAlertType(EnergyAlert.AlertType.HIGH_CONSUMPTION);
        testAlert.setSeverity(EnergyAlert.Severity.HIGH);
        testAlert.setTitle("High Power Consumption");
        testAlert.setMessage("Power consumption exceeds threshold");
        testAlert.setThresholdValue(new BigDecimal("5000"));
        testAlert.setActualValue(new BigDecimal("6000"));
        testAlert.setAlertTimestamp(LocalDateTime.now());
        
        // Set configuration values
        ReflectionTestUtils.setField(notificationService, "emailNotificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "smsNotificationsEnabled", false);
        ReflectionTestUtils.setField(notificationService, "pushNotificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "webhookNotificationsEnabled", false);
        ReflectionTestUtils.setField(notificationService, "emailServiceUrl", "http://localhost:8085");
        ReflectionTestUtils.setField(notificationService, "pushServiceUrl", "http://localhost:8087");
    }

    @Test
    void sendAlertNotification_Success() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        notificationService.sendAlertNotification(testAlert);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8087/api/v1/push/send"), any(), eq(String.class));
        // SMS should not be called for non-critical alerts
        verify(restTemplate, never()).postForObject(
                contains("sms"), any(), eq(String.class));
    }

    @Test
    void sendAlertNotification_CriticalAlert_SendsSms() {
        // Given
        testAlert.setSeverity(EnergyAlert.Severity.CRITICAL);
        ReflectionTestUtils.setField(notificationService, "smsNotificationsEnabled", true);
        ReflectionTestUtils.setField(notificationService, "smsServiceUrl", "http://localhost:8086");
        
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        notificationService.sendAlertNotification(testAlert);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8087/api/v1/push/send"), any(), eq(String.class));
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8086/api/v1/sms/send"), any(), eq(String.class));
    }

    @Test
    void sendAlertNotification_ServiceFailure_ContinuesProcessing() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then - Should not throw exception
        notificationService.sendAlertNotification(testAlert);

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
        notificationService.sendTestNotification(testUserId, "Test message");

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }

    @Test
    void sendAlertNotification_AllServicesDisabled_NoCalls() {
        // Given
        ReflectionTestUtils.setField(notificationService, "emailNotificationsEnabled", false);
        ReflectionTestUtils.setField(notificationService, "pushNotificationsEnabled", false);

        // When
        notificationService.sendAlertNotification(testAlert);

        // Then
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(String.class));
    }
}
