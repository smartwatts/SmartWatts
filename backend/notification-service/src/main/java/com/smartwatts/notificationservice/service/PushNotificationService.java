package com.smartwatts.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PushNotificationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${smartwatts.notifications.push.enabled:true}")
    private boolean pushNotificationsEnabled;
    
    @Value("${smartwatts.notifications.push.fcm.server-key:}")
    private String fcmServerKey;
    
    @Value("${smartwatts.notifications.push.fcm.url:https://fcm.googleapis.com/fcm/send}")
    private String fcmUrl;
    
    public PushNotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Send push notification to user
     */
    public void sendPushNotification(UUID userId, String title, String body, Map<String, Object> data) {
        if (!pushNotificationsEnabled) {
            log.info("Push notifications disabled, skipping push notification for user: {}", userId);
            return;
        }
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", body);
            
            Map<String, Object> message = new HashMap<>();
            message.put("to", "/topics/user-" + userId);
            message.put("notification", notification);
            if (data != null && !data.isEmpty()) {
                message.put("data", data);
            }
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "key=" + fcmServerKey);
            headers.put("Content-Type", "application/json");
            
            // Call FCM API
            restTemplate.postForObject(fcmUrl, message, String.class);
            log.info("Push notification sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send push notification to user: {}", userId, e);
            // Don't throw exception to avoid breaking the flow
        }
    }
    
    /**
     * Send push notification to topic
     */
    public void sendPushNotificationToTopic(String topic, String title, String body, Map<String, Object> data) {
        if (!pushNotificationsEnabled) {
            log.info("Push notifications disabled, skipping push notification to topic: {}", topic);
            return;
        }
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", body);
            
            Map<String, Object> message = new HashMap<>();
            message.put("to", "/topics/" + topic);
            message.put("notification", notification);
            if (data != null && !data.isEmpty()) {
                message.put("data", data);
            }
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "key=" + fcmServerKey);
            headers.put("Content-Type", "application/json");
            
            // Call FCM API
            restTemplate.postForObject(fcmUrl, message, String.class);
            log.info("Push notification sent successfully to topic: {}", topic);
            
        } catch (Exception e) {
            log.error("Failed to send push notification to topic: {}", topic, e);
        }
    }
}


