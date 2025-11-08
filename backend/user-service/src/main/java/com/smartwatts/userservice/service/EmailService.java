package com.smartwatts.userservice.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {
    
    @Value("${smartwatts.notifications.email.enabled:true}")
    private boolean emailNotificationsEnabled;
    
    @Value("${smartwatts.notifications.email.sendgrid.api-key:}")
    private String sendGridApiKey;
    
    @Value("${smartwatts.notifications.email.sendgrid.from-email:info@mysmartwatts.com}")
    private String fromEmail;
    
    @Value("${smartwatts.notifications.email.sendgrid.from-name:SmartWatts}")
    private String fromName;
    
    @Value("${smartwatts.notifications.email.service.url:}")
    private String emailServiceUrl;
    
    private SendGrid sendGrid;
    
    private void initializeSendGrid() {
        if (sendGrid == null && sendGridApiKey != null && !sendGridApiKey.isEmpty()) {
            sendGrid = new SendGrid(sendGridApiKey);
            log.info("SendGrid email service initialized");
        }
    }
    
    private void sendEmailViaSendGrid(String toEmail, String subject, String htmlContent, String textContent) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping email to: {}", toEmail);
            return;
        }
        
        try {
            initializeSendGrid();
            
            if (sendGrid == null) {
                log.warn("SendGrid not initialized, cannot send email");
                return;
            }
            
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, to, content);
            
            if (textContent != null) {
                Content text = new Content("text/plain", textContent);
                mail.addContent(text);
            }
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            sendGrid.api(request);
            log.info("Email sent successfully to: {}", toEmail);
            
        } catch (IOException e) {
            log.error("Failed to send email to: {}", toEmail, e);
            // Don't throw exception to avoid breaking the flow
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String email, String resetToken, String username) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping password reset email for: {}", email);
            return;
        }
        
        String resetUrl = generateResetUrl(resetToken);
        String subject = "SmartWatts Password Reset";
        String htmlContent = String.format(
            "<html><body>" +
            "<h2>Password Reset Request</h2>" +
            "<p>Hello %s,</p>" +
            "<p>You requested to reset your password. Click the link below to reset it:</p>" +
            "<p><a href=\"%s\">Reset Password</a></p>" +
            "<p>Or copy this link: %s</p>" +
            "<p>This link will expire in 1 hour.</p>" +
            "<p>If you didn't request this, please ignore this email.</p>" +
            "</body></html>",
            username, resetUrl, resetUrl
        );
        String textContent = String.format(
            "Hello %s,\n\nYou requested to reset your password. Visit: %s\n\nThis link will expire in 1 hour.",
            username, resetUrl
        );
        
        sendEmailViaSendGrid(email, subject, htmlContent, textContent);
    }
    
    /**
     * Send welcome email for new user registration
     */
    public void sendWelcomeEmail(String email, String username) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping welcome email for: {}", email);
            return;
        }
        
        String loginUrl = generateLoginUrl();
        String subject = "Welcome to SmartWatts";
        String htmlContent = String.format(
            "<html><body>" +
            "<h2>Welcome to SmartWatts!</h2>" +
            "<p>Hello %s,</p>" +
            "<p>Thank you for joining SmartWatts. Your account has been created successfully.</p>" +
            "<p><a href=\"%s\">Login to your account</a></p>" +
            "<p>Start monitoring your energy consumption and save money today!</p>" +
            "</body></html>",
            username, loginUrl
        );
        String textContent = String.format(
            "Hello %s,\n\nWelcome to SmartWatts! Your account has been created. Login at: %s",
            username, loginUrl
        );
        
        sendEmailViaSendGrid(email, subject, htmlContent, textContent);
    }
    
    /**
     * Send email verification email
     */
    public void sendEmailVerificationEmail(String email, String verificationToken, String username) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping email verification for: {}", email);
            return;
        }
        
        String verificationUrl = generateVerificationUrl(verificationToken);
        String subject = "Verify Your SmartWatts Email";
        String htmlContent = String.format(
            "<html><body>" +
            "<h2>Verify Your Email Address</h2>" +
            "<p>Hello %s,</p>" +
            "<p>Please verify your email address by clicking the link below:</p>" +
            "<p><a href=\"%s\">Verify Email</a></p>" +
            "<p>Or copy this link: %s</p>" +
            "<p>This link will expire in 24 hours.</p>" +
            "<p>If you didn't create an account, please ignore this email.</p>" +
            "</body></html>",
            username, verificationUrl, verificationUrl
        );
        String textContent = String.format(
            "Hello %s,\n\nPlease verify your email address: %s\n\nThis link will expire in 24 hours.",
            username, verificationUrl
        );
        
        sendEmailViaSendGrid(email, subject, htmlContent, textContent);
    }
    
    /**
     * Send account locked notification email
     */
    public void sendAccountLockedEmail(String email, String username, String reason) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping account locked email for: {}", email);
            return;
        }
        
        String supportUrl = generateSupportUrl();
        String subject = "SmartWatts Account Locked";
        String htmlContent = String.format(
            "<html><body>" +
            "<h2>Account Locked</h2>" +
            "<p>Hello %s,</p>" +
            "<p>Your SmartWatts account has been locked.</p>" +
            "<p>Reason: %s</p>" +
            "<p>Please contact support: <a href=\"%s\">%s</a></p>" +
            "</body></html>",
            username, reason, supportUrl, supportUrl
        );
        String textContent = String.format(
            "Hello %s,\n\nYour account has been locked. Reason: %s\n\nContact support: %s",
            username, reason, supportUrl
        );
        
        sendEmailViaSendGrid(email, subject, htmlContent, textContent);
    }
    
    /**
     * Send test email to verify service connectivity
     */
    public void sendTestEmail(String email, String message) {
        if (!emailNotificationsEnabled) {
            log.info("Email notifications disabled, skipping test email for: {}", email);
            return;
        }
        
        String subject = "SmartWatts Test Email";
        String htmlContent = String.format(
            "<html><body>" +
            "<h2>Test Email</h2>" +
            "<p>%s</p>" +
            "<p>If you received this email, your email service is working correctly.</p>" +
            "</body></html>",
            message
        );
        String textContent = message + "\n\nIf you received this email, your email service is working correctly.";
        
        sendEmailViaSendGrid(email, subject, htmlContent, textContent);
    }
    
    /**
     * Generate password reset URL
     */
    private String generateResetUrl(String resetToken) {
        return "https://mysmartwatts.com/reset-password?token=" + resetToken;
    }
    
    /**
     * Generate email verification URL
     */
    private String generateVerificationUrl(String verificationToken) {
        return "https://mysmartwatts.com/verify-email?token=" + verificationToken;
    }
    
    /**
     * Generate login URL
     */
    private String generateLoginUrl() {
        return "https://mysmartwatts.com/login";
    }
    
    /**
     * Generate support URL
     */
    private String generateSupportUrl() {
        return "https://mysmartwatts.com/support";
    }
}
