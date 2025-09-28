package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.DeviceEvent;
import com.smartwatts.deviceservice.repository.DeviceEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    
    private final DeviceEventRepository deviceEventRepository;
    private final DeviceNotificationService deviceNotificationService;
    
    @Transactional
    public DeviceEvent createDeviceEvent(UUID deviceId, DeviceEvent.EventType eventType, 
                                       DeviceEvent.Severity severity, String title, String message) {
        return createDeviceEvent(deviceId, eventType, severity, title, message, null, null, null);
    }
    
    @Transactional
    public DeviceEvent createDeviceEvent(UUID deviceId, DeviceEvent.EventType eventType, 
                                       DeviceEvent.Severity severity, String title, String message,
                                       String source, String errorCode, String errorMessage) {
        log.info("Creating device event: {} for device: {}", eventType, deviceId);
        
        DeviceEvent event = new DeviceEvent();
        event.setDeviceId(deviceId);
        event.setEventType(eventType);
        event.setSeverity(severity);
        event.setTitle(title);
        event.setMessage(message);
        event.setEventTimestamp(LocalDateTime.now());
        event.setSource(source);
        event.setErrorCode(errorCode);
        event.setErrorMessage(errorMessage);
        event.setIsAcknowledged(false);
        event.setIsResolved(false);
        event.setNotificationSent(false);
        
        DeviceEvent savedEvent = deviceEventRepository.save(event);
        log.info("Device event created with ID: {}", savedEvent.getId());
        
        // Send notification based on severity and event type
        try {
            deviceNotificationService.sendEventNotification(savedEvent);
            
            // Mark notification as sent
            savedEvent.setNotificationSent(true);
            savedEvent.setNotificationSentAt(LocalDateTime.now());
            deviceEventRepository.save(savedEvent);
            
        } catch (Exception e) {
            log.error("Failed to send notification for event ID: {}", savedEvent.getId(), e);
            // Don't mark as sent if there was an error
        }
        
        return savedEvent;
    }
    
    @Transactional
    public DeviceEvent acknowledgeEvent(UUID eventId, UUID acknowledgedBy) {
        log.info("Acknowledging event with ID: {}", eventId);
        DeviceEvent event = deviceEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Device event not found with ID: " + eventId));
        
        event.setIsAcknowledged(true);
        event.setAcknowledgedAt(LocalDateTime.now());
        event.setAcknowledgedBy(acknowledgedBy);
        
        DeviceEvent savedEvent = deviceEventRepository.save(event);
        return savedEvent;
    }
    
    @Transactional
    public DeviceEvent resolveEvent(UUID eventId, UUID resolvedBy, String resolutionNotes) {
        log.info("Resolving event with ID: {}", eventId);
        DeviceEvent event = deviceEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Device event not found with ID: " + eventId));
        
        event.setIsResolved(true);
        event.setResolvedAt(LocalDateTime.now());
        event.setResolvedBy(resolvedBy);
        event.setResolutionNotes(resolutionNotes);
        
        DeviceEvent savedEvent = deviceEventRepository.save(event);
        return savedEvent;
    }
    
    @Transactional
    public void processPendingNotifications() {
        log.info("Processing pending notifications");
        
        try {
            // Find all device events that haven't had notifications sent yet
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            var pendingEvents = deviceEventRepository.findAll()
                    .stream()
                    .filter(event -> !event.getNotificationSent() && 
                            event.getEventTimestamp().isAfter(oneHourAgo))
                    .toList();
            
            log.info("Found {} pending notifications to process", pendingEvents.size());
            
            for (DeviceEvent event : pendingEvents) {
                try {
                    deviceNotificationService.sendEventNotification(event);
                    
                    // Mark notification as sent
                    event.setNotificationSent(true);
                    event.setNotificationSentAt(LocalDateTime.now());
                    deviceEventRepository.save(event);
                    
                    log.debug("Processed notification for device event ID: {}", event.getId());
                    
                } catch (Exception e) {
                    log.error("Failed to process notification for device event ID: {}", event.getId(), e);
                }
            }
            
            log.info("Successfully processed {} pending notifications", pendingEvents.size());
            
        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
        }
    }
    

    
    @Transactional
    public void createConnectionEvent(UUID deviceId, boolean isConnected, String details) {
        DeviceEvent.EventType eventType = isConnected ? 
                DeviceEvent.EventType.CONNECTION_ESTABLISHED : 
                DeviceEvent.EventType.CONNECTION_LOST;
        
        DeviceEvent.Severity severity = isConnected ? 
                DeviceEvent.Severity.INFO : 
                DeviceEvent.Severity.WARNING;
        
        String title = isConnected ? "Connection Established" : "Connection Lost";
        String message = isConnected ? 
                "Device connection has been established" : 
                "Device connection has been lost";
        
        if (details != null && !details.isEmpty()) {
            message += " - " + details;
        }
        
        createDeviceEvent(deviceId, eventType, severity, title, message);
    }
    
    @Transactional
    public void createDataEvent(UUID deviceId, boolean isReceived, String details) {
        DeviceEvent.EventType eventType = isReceived ? 
                DeviceEvent.EventType.DATA_RECEIVED : 
                DeviceEvent.EventType.DATA_SENT;
        
        String title = isReceived ? "Data Received" : "Data Sent";
        String message = isReceived ? 
                "Data has been received from device" : 
                "Data has been sent to device";
        
        if (details != null && !details.isEmpty()) {
            message += " - " + details;
        }
        
        createDeviceEvent(deviceId, eventType, DeviceEvent.Severity.INFO, title, message);
    }
    
    @Transactional
    public void createErrorEvent(UUID deviceId, String errorCode, String errorMessage, String stackTrace) {
        createDeviceEvent(deviceId, DeviceEvent.EventType.ERROR_OCCURRED, 
                DeviceEvent.Severity.ERROR, "Error Occurred", 
                "An error has occurred: " + errorMessage, 
                "SYSTEM", errorCode, errorMessage);
    }
    
    @Transactional
    public void createMaintenanceEvent(UUID deviceId, boolean isScheduled, String details) {
        DeviceEvent.EventType eventType = isScheduled ? 
                DeviceEvent.EventType.MAINTENANCE_SCHEDULED : 
                DeviceEvent.EventType.MAINTENANCE_COMPLETED;
        
        String title = isScheduled ? "Maintenance Scheduled" : "Maintenance Completed";
        String message = isScheduled ? 
                "Maintenance has been scheduled for device" : 
                "Maintenance has been completed for device";
        
        if (details != null && !details.isEmpty()) {
            message += " - " + details;
        }
        
        createDeviceEvent(deviceId, eventType, DeviceEvent.Severity.INFO, title, message);
    }
} 