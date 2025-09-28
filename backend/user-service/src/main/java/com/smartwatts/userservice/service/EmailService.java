package com.smartwatts.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final RestTemplate restTemplate;
    
    @Value("${smartwatts.notifications.email.enabled:true}")
    private boolean emailNotificationsEnabled;
    
    @Value("${smartwatts.notifications.email.service.url:http://localhost:8085}")
    private String emailServiceUrl;
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String resetToken, String username) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping password reset email for: {}", email);
            return;
        }
        
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("email", email);
            emailData.put("subject", "SmartWatts Password Reset");
            emailData.put("template", "password-reset");
            emailData.put("data", Map.of(
                "username", username,
                "resetToken", resetToken,
                "resetUrl", generateResetUrl(resetToken)
            ));
            
            // Call email service
            restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            log.info("Password reset email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            // Don't throw exception to avoid breaking the password reset flow
        }
    }
    
    /**
     * Send welcome email for new user registration
     */
    public void sendWelcomeEmail(String email, String username) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping welcome email for: {}", email);
            return;
        }
        
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("email", email);
            emailData.put("subject", "Welcome to SmartWatts");
            emailData.put("template", "welcome");
            emailData.put("data", Map.of(
                "username", username,
                "loginUrl", generateLoginUrl()
            ));
            
            // Call email service
            restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            log.info("Welcome email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            // Don't throw exception to avoid breaking the registration flow
        }
    }
    
    /**
     * Send email verification email
     */
    public void sendEmailVerificationEmail(String email, String verificationToken, String username) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping email verification for: {}", email);
            return;
        }
        
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("email", email);
            emailData.put("subject", "Verify Your SmartWatts Email");
            emailData.put("template", "email-verification");
            emailData.put("data", Map.of(
                "username", username,
                "verificationToken", verificationToken,
                "verificationUrl", generateVerificationUrl(verificationToken)
            ));
            
            // Call email service
            restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            log.info("Email verification email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", email, e);
            // Don't throw exception to avoid breaking the registration flow
        }
    }
    
    /**
     * Send account locked notification email
     */
    public void sendAccountLockedEmail(String email, String username, String reason) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping account locked email for: {}", email);
            return;
        }
        
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("email", email);
            emailData.put("subject", "SmartWatts Account Locked");
            emailData.put("template", "account-locked");
            emailData.put("data", Map.of(
                "username", username,
                "reason", reason,
                "supportUrl", generateSupportUrl()
            ));
            
            // Call email service
            restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            log.info("Account locked email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send account locked email to: {}", email, e);
        }
    }
    
    /**
     * Send test email to verify service connectivity
     */
    public void sendTestEmail(String email, String message) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping test email for: {}", email);
            return;
        }
        
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("email", email);
            emailData.put("subject", "SmartWatts Test Email");
            emailData.put("template", "test-email");
            emailData.put("data", Map.of("message", message));
            
            restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            log.info("Test email sent successfully to: {}", email);
            
        } catch (Exception e) {
            log.error("Failed to send test email to: {}", email, e);
        }
    }
    
    /**
     * Generate password reset URL
     */
    private String generateResetUrl(String resetToken) {
        return "https://smartwatts.com/reset-password?token=" + resetToken;
    }
    
    /**
     * Generate email verification URL
     */
    private String generateVerificationUrl(String verificationToken) {
        return "https://smartwatts.com/verify-email?token=" + verificationToken;
    }
    
    /**
     * Generate login URL
     */
    private String generateLoginUrl() {
        return "https://smartwatts.com/login";
    }
    
    /**
     * Generate support URL
     */
    private String generateSupportUrl() {
        return "https://smartwatts.com/support";
    }
}
