package com.smartwatts.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmailService emailService;

    private String testEmail;
    private String testUsername;
    private String testToken;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testUsername = "testuser";
        testToken = "test-reset-token-123";
        
        // Set configuration values
        ReflectionTestUtils.setField(emailService, "emailNotificationsEnabled", true);
        ReflectionTestUtils.setField(emailService, "emailServiceUrl", "http://localhost:8085");
    }

    @Test
    void sendPasswordResetEmail_Success() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        emailService.sendPasswordResetEmail(testEmail, testToken, testUsername);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }

    @Test
    void sendPasswordResetEmail_ServiceFailure_DoesNotThrow() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then - Should not throw exception
        emailService.sendPasswordResetEmail(testEmail, testToken, testUsername);

        // Verify that the service was attempted
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }

    @Test
    void sendPasswordResetEmail_NotificationsDisabled_NoCall() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailNotificationsEnabled", false);

        // When
        emailService.sendPasswordResetEmail(testEmail, testToken, testUsername);

        // Then
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(String.class));
    }

    @Test
    void sendWelcomeEmail_Success() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        emailService.sendWelcomeEmail(testEmail, testUsername);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }

    @Test
    void sendEmailVerificationEmail_Success() {
        // Given
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        emailService.sendEmailVerificationEmail(testEmail, testToken, testUsername);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }

    @Test
    void sendAccountLockedEmail_Success() {
        // Given
        String reason = "Multiple failed login attempts";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        emailService.sendAccountLockedEmail(testEmail, testUsername, reason);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }

    @Test
    void sendTestEmail_Success() {
        // Given
        String message = "Test message";
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("success");

        // When
        emailService.sendTestEmail(testEmail, message);

        // Then
        verify(restTemplate, times(1)).postForObject(
                eq("http://localhost:8085/api/v1/emails/send"), any(), eq(String.class));
    }
}
