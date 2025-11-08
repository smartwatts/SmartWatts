package com.smartwatts.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/energy/updates")
    @SendTo("/topic/energy")
    public Map<String, Object> handleEnergyUpdates(Map<String, Object> message) {
        log.info("Received energy update: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "energy_update");
        response.put("data", message);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return response;
    }

    @MessageMapping("/device/status")
    @SendTo("/topic/device")
    public Map<String, Object> handleDeviceStatus(Map<String, Object> message) {
        log.info("Received device status update: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "device_status");
        response.put("data", message);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return response;
    }

    @MessageMapping("/notifications")
    @SendTo("/queue/notifications")
    public Map<String, Object> handleNotifications(Map<String, Object> message) {
        log.info("Received notification: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "notification");
        response.put("data", message);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return response;
    }

    public void sendEnergyUpdate(String userId, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/energy/" + userId, data);
    }

    public void sendDeviceStatus(String userId, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/device/" + userId, data);
    }

    public void sendNotification(String userId, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/queue/notifications/" + userId, data);
    }
}


