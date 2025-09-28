package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.DeviceEvent;
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
public class DeviceNotificationService {
    
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
     * Send notification for a device event
     */
    public void sendEventNotification(DeviceEvent event) {
        log.info("Sending notification for device event ID: {} for device: {}", event.getId(), event.getDeviceId());
        
        try {
            // Determine notification channels based on severity and event type
            boolean shouldSendEmail = shouldSendEmailNotification(event);
            boolean shouldSendSms = shouldSendSmsNotification(event);
            boolean shouldSendPush = shouldSendPushNotification(event);
            boolean shouldSendWebhook = shouldSendWebhookNotification(event);
            
            // Send email notification
            if (emailNotificationsEnabled && shouldSendEmail) {
                sendEmailNotification(event);
            }
            
            // Send SMS notification
            if (smsNotificationsEnabled && shouldSendSms) {
                sendSmsNotification(event);
            }
            
            // Send push notification
            if (pushNotificationsEnabled && shouldSendPush) {
                sendPushNotification(event);
            }
            
            // Send webhook notification
            if (webhookNotificationsEnabled && shouldSendWebhook && !webhookUrl.isEmpty()) {
                sendWebhookNotification(event);
            }
            
            log.info("Successfully sent notification for device event ID: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to send notification for device event ID: {}", event.getId(), e);
            // Don't throw exception to avoid breaking the event creation process
        }
    }
    
    /**
     * Determine if email notification should be sent
     */
    private boolean shouldSendEmailNotification(DeviceEvent event) {
        // Send email for ERROR, CRITICAL, and important WARNING events
        return event.getSeverity() == DeviceEvent.Severity.ERROR ||
               event.getSeverity() == DeviceEvent.Severity.CRITICAL ||
               (event.getSeverity() == DeviceEvent.Severity.WARNING && 
                isImportantWarningEvent(event.getEventType()));
    }
    
    /**
     * Determine if SMS notification should be sent
     */
    private boolean shouldSendSmsNotification(DeviceEvent event) {
        // Send SMS only for CRITICAL events
        return event.getSeverity() == DeviceEvent.Severity.CRITICAL;
    }
    
    /**
     * Determine if push notification should be sent
     */
    private boolean shouldSendPushNotification(DeviceEvent event) {
        // Send push for ERROR, CRITICAL, and WARNING events
        return event.getSeverity() == DeviceEvent.Severity.ERROR ||
               event.getSeverity() == DeviceEvent.Severity.CRITICAL ||
               event.getSeverity() == DeviceEvent.Severity.WARNING;
    }
    
    /**
     * Determine if webhook notification should be sent
     */
    private boolean shouldSendWebhookNotification(DeviceEvent event) {
        // Send webhook for all events if enabled
        return true;
    }
    
    /**
     * Check if this is an important warning event type
     */
    private boolean isImportantWarningEvent(DeviceEvent.EventType eventType) {
        return eventType == DeviceEvent.EventType.CONNECTION_LOST ||
               eventType == DeviceEvent.EventType.AUTHENTICATION_FAILED ||
               eventType == DeviceEvent.EventType.AUTHORIZATION_DENIED ||
               eventType == DeviceEvent.EventType.RATE_LIMIT_EXCEEDED ||
               eventType == DeviceEvent.EventType.TIMEOUT_OCCURRED;
    }
    
    /**
     * Send email notification
     */
    private void sendEmailNotification(DeviceEvent event) {
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("deviceId", event.getDeviceId());
            emailData.put("subject", "SmartWatts Device Event: " + event.getTitle());
            emailData.put("template", "device-event");
            emailData.put("data", Map.of(
                "eventType", event.getEventType().toString(),
                "severity", event.getSeverity().toString(),
                "title", event.getTitle(),
                "message", event.getMessage(),
                "deviceId", event.getDeviceId(),
                "timestamp", event.getEventTimestamp().toString(),
                "source", event.getSource() != null ? event.getSource() : "SYSTEM",
                "errorCode", event.getErrorCode() != null ? event.getErrorCode() : "",
                "errorMessage", event.getErrorMessage() != null ? event.getErrorMessage() : ""
            ));
            
            // Call email service
            restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            log.debug("Email notification sent for device event ID: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to send email notification for device event ID: {}", event.getId(), e);
        }
    }
    
    /**
     * Send SMS notification
     */
    private void sendSmsNotification(DeviceEvent event) {
        try {
            Map<String, Object> smsData = new HashMap<>();
            smsData.put("deviceId", event.getDeviceId());
            smsData.put("message", String.format("SmartWatts Critical Alert: %s - %s", 
                event.getTitle(), event.getMessage()));
            
            // Call SMS service
            restTemplate.postForObject(smsServiceUrl + "/api/v1/sms/send", smsData, String.class);
            log.debug("SMS notification sent for device event ID: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to send SMS notification for device event ID: {}", event.getId(), e);
        }
    }
    
    /**
     * Send push notification
     */
    private void sendPushNotification(DeviceEvent event) {
        try {
            Map<String, Object> pushData = new HashMap<>();
            pushData.put("deviceId", event.getDeviceId());
            pushData.put("title", "SmartWatts Device Event");
            pushData.put("body", event.getTitle() + ": " + event.getMessage());
            pushData.put("data", Map.of(
                "eventId", event.getId().toString(),
                "eventType", event.getEventType().toString(),
                "severity", event.getSeverity().toString(),
                "deviceId", event.getDeviceId()
            ));
            
            // Call push notification service
            restTemplate.postForObject(pushServiceUrl + "/api/v1/push/send", pushData, String.class);
            log.debug("Push notification sent for device event ID: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to send push notification for device event ID: {}", event.getId(), e);
        }
    }
    
    /**
     * Send webhook notification
     */
    private void sendWebhookNotification(DeviceEvent event) {
        try {
            Map<String, Object> webhookData = new HashMap<>();
            webhookData.put("event", "device_event");
            webhookData.put("timestamp", LocalDateTime.now().toString());
            Map<String, Object> deviceEventData = new HashMap<>();
            deviceEventData.put("id", event.getId().toString());
            deviceEventData.put("deviceId", event.getDeviceId().toString());
            deviceEventData.put("eventType", event.getEventType().toString());
            deviceEventData.put("severity", event.getSeverity().toString());
            deviceEventData.put("title", event.getTitle());
            deviceEventData.put("message", event.getMessage());
            deviceEventData.put("source", event.getSource() != null ? event.getSource() : "SYSTEM");
            deviceEventData.put("errorCode", event.getErrorCode() != null ? event.getErrorCode() : "");
            deviceEventData.put("errorMessage", event.getErrorMessage() != null ? event.getErrorMessage() : "");
            deviceEventData.put("eventTimestamp", event.getEventTimestamp().toString());
            deviceEventData.put("isAcknowledged", event.getIsAcknowledged());
            deviceEventData.put("isResolved", event.getIsResolved());
            
            webhookData.put("deviceEvent", deviceEventData);
            
            // Send webhook
            restTemplate.postForObject(webhookUrl, webhookData, String.class);
            log.debug("Webhook notification sent for device event ID: {}", event.getId());
            
        } catch (Exception e) {
            log.error("Failed to send webhook notification for device event ID: {}", event.getId(), e);
        }
    }
    
    /**
     * Send test notification to verify service connectivity
     */
    public void sendTestNotification(UUID deviceId, String message) {
        log.info("Sending test notification for device: {}", deviceId);
        
        try {
            if (emailNotificationsEnabled) {
                Map<String, Object> emailData = new HashMap<>();
                emailData.put("deviceId", deviceId);
                emailData.put("subject", "SmartWatts Device Test Notification");
                emailData.put("template", "test-notification");
                emailData.put("data", Map.of("message", message));
                
                restTemplate.postForObject(emailServiceUrl + "/api/v1/emails/send", emailData, String.class);
            }
            
            log.info("Test notification sent successfully for device: {}", deviceId);
            
        } catch (Exception e) {
            log.error("Failed to send test notification for device: {}", deviceId, e);
        }
    }
}
