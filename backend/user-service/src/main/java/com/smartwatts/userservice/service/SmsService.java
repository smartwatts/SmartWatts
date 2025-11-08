package com.smartwatts.userservice.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    
    @Value("${smartwatts.notifications.sms.enabled:true}")
    private boolean smsNotificationsEnabled;
    
    @Value("${smartwatts.notifications.sms.twilio.account-sid:}")
    private String accountSid;
    
    @Value("${smartwatts.notifications.sms.twilio.auth-token:}")
    private String authToken;
    
    @Value("${smartwatts.notifications.sms.twilio.from-number:}")
    private String fromNumber;
    
    private boolean initialized = false;
    
    private void initializeTwilio() {
        if (!initialized && smsNotificationsEnabled && accountSid != null && !accountSid.isEmpty()) {
            Twilio.init(accountSid, authToken);
            initialized = true;
            log.info("Twilio SMS service initialized");
        }
    }
    
    /**
     * Send SMS verification code
     */
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        if (!smsNotificationsEnabled) {
            log.info("SMS notifications disabled, skipping verification code for: {}", phoneNumber);
            return;
        }
        
        try {
            initializeTwilio();
            
            if (!initialized) {
                log.warn("Twilio not initialized, cannot send SMS");
                return;
            }
            
            String message = String.format("Your SmartWatts verification code is: %s. Valid for 10 minutes.", verificationCode);
            
            Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromNumber),
                message
            ).create();
            
            log.info("SMS verification code sent successfully to: {}", phoneNumber);
            
        } catch (Exception e) {
            log.error("Failed to send SMS verification code to: {}", phoneNumber, e);
            // Don't throw exception to avoid breaking the verification flow
        }
    }
    
    /**
     * Send password reset SMS
     */
    public void sendPasswordResetSms(String phoneNumber, String resetToken) {
        if (!smsNotificationsEnabled) {
            log.info("SMS notifications disabled, skipping password reset SMS for: {}", phoneNumber);
            return;
        }
        
        try {
            initializeTwilio();
            
            if (!initialized) {
                log.warn("Twilio not initialized, cannot send SMS");
                return;
            }
            
            String resetUrl = "https://smartwatts.com/reset-password?token=" + resetToken;
            String message = String.format("Your SmartWatts password reset link: %s. Valid for 1 hour.", resetUrl);
            
            Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromNumber),
                message
            ).create();
            
            log.info("Password reset SMS sent successfully to: {}", phoneNumber);
            
        } catch (Exception e) {
            log.error("Failed to send password reset SMS to: {}", phoneNumber, e);
        }
    }
    
    /**
     * Send test SMS
     */
    public void sendTestSms(String phoneNumber, String message) {
        if (!smsNotificationsEnabled) {
            log.info("SMS notifications disabled, skipping test SMS for: {}", phoneNumber);
            return;
        }
        
        try {
            initializeTwilio();
            
            if (!initialized) {
                log.warn("Twilio not initialized, cannot send SMS");
                return;
            }
            
            Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromNumber),
                "SmartWatts Test: " + message
            ).create();
            
            log.info("Test SMS sent successfully to: {}", phoneNumber);
            
        } catch (Exception e) {
            log.error("Failed to send test SMS to: {}", phoneNumber, e);
        }
    }
}


