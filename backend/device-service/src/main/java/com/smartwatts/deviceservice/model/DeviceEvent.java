package com.smartwatts.deviceservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_events")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class DeviceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity = Severity.INFO;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "source")
    private String source;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @Column(name = "is_acknowledged")
    private Boolean isAcknowledged = false;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "is_resolved")
    private Boolean isResolved = false;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "notification_sent")
    private Boolean notificationSent = false;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum EventType {
        DEVICE_ONLINE,
        DEVICE_OFFLINE,
        CONNECTION_ESTABLISHED,
        CONNECTION_LOST,
        DATA_RECEIVED,
        DATA_SENT,
        CONFIGURATION_CHANGED,
        FIRMWARE_UPDATE_STARTED,
        FIRMWARE_UPDATE_COMPLETED,
        FIRMWARE_UPDATE_FAILED,
        MAINTENANCE_SCHEDULED,
        MAINTENANCE_COMPLETED,
        CALIBRATION_REQUIRED,
        CALIBRATION_COMPLETED,
        ERROR_OCCURRED,
        WARNING_GENERATED,
        ALERT_TRIGGERED,
        DEVICE_DISCOVERED,
        DEVICE_REGISTERED,
        DEVICE_DEREGISTERED,
        AUTHENTICATION_SUCCESS,
        AUTHENTICATION_FAILED,
        AUTHORIZATION_DENIED,
        RATE_LIMIT_EXCEEDED,
        TIMEOUT_OCCURRED,
        RETRY_ATTEMPTED,
        CUSTOM_EVENT
    }

    public enum Severity {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
} 