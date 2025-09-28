package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.model.EnergyAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${smartwatts.notifications.email.enabled:true}")
    private boolean emailNotificationsEnabled;
    
    @Value("${smartwatts.notifications.sms.enabled:false}")
    private boolean smsNotificationsEnabled;
    
    @Value("${smartwatts.notifications.push.enabled:true}")
    private boolean pushNotificationsEnabled;
    
    @Value("${smartwatts.notifications.webhook.enabled:false}")
    private boolean webhookNotificationsEnabled;
    
    @Value("${smartwatts.notifications.webhook.url:}")
    private String webhookUrl;
    
    @Value("${smartwatts.notifications.email.service.url:http://localhost:8085}")
    private String emailServiceUrl;
    
    @Value("${smartwatts.notifications.sms.service.url:http://localhost:8086}")
    private String smsServiceUrl;
    
    @Value("${smartwatts.notifications.push.service.url:http://localhost:8087}")
    private String pushServiceUrl;
    
    /**
     * Send notification for an energy alert
     */
    public void sendAlertNotification(EnergyAlert alert) {
        log.info("Sending notification for alert ID: {} to user: {}", alert.getId(), alert.getUserId());
        
        try {
            // Send email notification
            if (emailNotificationsEnabled) {
                sendEmailNotification(alert);
            }
            
            // Send SMS notification for critical alerts
            if (smsNotificationsEnabled && alert.getSeverity() == EnergyAlert.Severity.CRITICAL) {
                sendSmsNotification(alert);
            }
            
            // Send push notification
            if (pushNotificationsEnabled) {
                sendPushNotification(alert);
            }
            
            // Send webhook notification
            if (webhookNotificationsEnabled && !webhookUrl.isEmpty()) {
                sendWebhookNotification(alert);
            }
            
            log.info("Successfully sent notification for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Failed to send notification for alert ID: {}", alert.getId(), e);
            // Don't throw exception to avoid breaking the alert creation process
        }
    }
    
    /**
     * Send email notification
     */
    private void sendEmailNotification(EnergyAlert alert) {
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("userId", alert.getUserId());
            emailData.put("subject", "SmartWatts Alert: " + alert.getTitle());
            emailData.put("template", "energy-alert");
            emailData.put("data", Map.of(
                "alertType", alert.getAlertType().toString(),
                "severity", alert.getSeverity().toString(),
                "title", alert.getTitle(),
                "message", alert.getMessage(),
                "deviceId", alert.getDeviceId(),
                "timestamp", alert.getAlertTimestamp().toString(),
                "thresholdValue", alert.getThresholdValue(),
                "actualValue", alert.getActualValue()
            ));
            
            // Call email service
            restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            log.debug("Email notification sent for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Failed to send email notification for alert ID: {}", alert.getId(), e);
        }
    }
    
    /**
     * Send SMS notification
     */
    private void sendSmsNotification(EnergyAlert alert) {
        try {
            Map<String, Object> smsData = new HashMap<>();
            smsData.put("userId", alert.getUserId());
            smsData.put("message", String.format("SmartWatts Alert: %s - %s", 
                alert.getTitle(), alert.getMessage()));
            
            // Call SMS service
            restTemplate.postForObject(smsServiceUrl + "/api/v1/sms/send", smsData, String.class);
            log.debug("SMS notification sent for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Failed to send SMS notification for alert ID: {}", alert.getId(), e);
        }
    }
    
    /**
     * Send push notification
     */
    private void sendPushNotification(EnergyAlert alert) {
        try {
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("userId", alert.getUserId());
            pushData.put("title", "SmartWatts Alert");
            pushData.put("body", alert.getTitle() + ": " + alert.getMessage());
            pushData.put("data", Map.of(
                "alertId", alert.getId().toString(),
                "alertType", alert.getAlertType().toString(),
                "severity", alert.getSeverity().toString(),
                "deviceId", alert.getDeviceId()
            ));
            
            // Call push notification service
            restTemplate.postForObject(pushServiceUrl + "/api/v1/push/send", pushData, String.class);
            log.debug("Push notification sent for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Failed to send push notification for alert ID: {}", alert.getId(), e);
        }
    }
    
    /**
     * Send webhook notification
     */
    private void sendWebhookNotification(EnergyAlert alert) {
        try {
            Map<String, Object> webhookData = new HashMap<>();
            webhookData.put("event", "energy_alert");
            webhookData.put("timestamp", LocalDateTime.now().toString());
            webhookData.put("alert", Map.of(
                "id", alert.getId().toString(),
                "userId", alert.getUserId().toString(),
                "deviceId", alert.getDeviceId(),
                "alertType", alert.getAlertType().toString(),
                "severity", alert.getSeverity().toString(),
                "title", alert.getTitle(),
                "message", alert.getMessage(),
                "thresholdValue", alert.getThresholdValue(),
                "actualValue", alert.getActualValue(),
                "alertTimestamp", alert.getAlertTimestamp().toString()
            ));
            
            // Send webhook
            restTemplate.postForObject(webhookUrl, webhookData, String.class);
            log.debug("Webhook notification sent for alert ID: {}", alert.getId());
            
        } catch (Exception e) {
            log.error("Failed to send webhook notification for alert ID: {}", alert.getId(), e);
        }
    }
    
    /**
     * Send test notification to verify service connectivity
     */
    public void sendTestNotification(UUID userId, String message) {
        log.info("Sending test notification to user: {}", userId);
        
        try {
            if (emailNotificationsEnabled) {
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("userId", userId);
                emailData.put("subject", "SmartWatts Test Notification");
                emailData.put("template", "test-notification");
                emailData.put("data", Map.of("message", message));
                
                restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            }
            
            log.info("Test notification sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send test notification to user: {}", userId, e);
        }
    }
}
