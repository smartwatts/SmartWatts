package com.smartwatts.notificationservice.controller;

import com.smartwatts.notificationservice.dto.PushNotificationRequest;
import com.smartwatts.notificationservice.service.PushNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Push Notifications", description = "APIs for push notification management")
public class PushNotificationController {
    
    private final PushNotificationService pushNotificationService;
    
    @PostMapping("/send")
    @Operation(summary = "Send push notification", description = "Sends a push notification to a user or topic")
    public ResponseEntity<String> sendPushNotification(@Valid @RequestBody PushNotificationRequest request) {
        log.info("Sending push notification to user: {} or topic: {}", request.getUserId(), request.getTopic());
        
        if (request.getUserId() != null) {
            pushNotificationService.sendPushNotification(
                request.getUserId(),
                request.getTitle(),
                request.getBody(),
                request.getData()
            );
        } else if (request.getTopic() != null) {
            pushNotificationService.sendPushNotificationToTopic(
                request.getTopic(),
                request.getTitle(),
                request.getBody(),
                request.getData()
            );
        } else {
            return ResponseEntity.badRequest().body("Either userId or topic must be provided");
        }
        
        return ResponseEntity.ok("Push notification sent successfully");
    }
}


