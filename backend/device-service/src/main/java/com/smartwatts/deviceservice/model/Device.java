package com.smartwatts.deviceservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false)
    private Protocol protocol = Protocol.MQTT;

    @Column(name = "connection_string")
    private String connectionString;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "port")
    private Integer port;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "mqtt_topic")
    private String mqttTopic;

    @Column(name = "modbus_address")
    private Integer modbusAddress;

    @Column(name = "modbus_register_start")
    private Integer modbusRegisterStart;

    @Column(name = "modbus_register_count")
    private Integer modbusRegisterCount;

    @Column(name = "location_lat", precision = 10, scale = 8)
    private BigDecimal locationLat;

    @Column(name = "location_lng", precision = 11, scale = 8)
    private BigDecimal locationLng;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeviceStatus status = DeviceStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false)
    private ConnectionStatus connectionStatus = ConnectionStatus.OFFLINE;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "hardware_version")
    private String hardwareVersion;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model")
    private String model;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "warranty_expiry")
    private LocalDateTime warrantyExpiry;

    @Column(name = "is_calibrated")
    private Boolean isCalibrated = false;

    @Column(name = "calibration_date")
    private LocalDateTime calibrationDate;

    @Column(name = "calibration_expiry")
    private LocalDateTime calibrationExpiry;

    @Column(name = "accuracy_percentage", precision = 5, scale = 2)
    private BigDecimal accuracyPercentage;

    @Column(name = "max_voltage", precision = 6, scale = 2)
    private BigDecimal maxVoltage;

    @Column(name = "max_current", precision = 6, scale = 2)
    private BigDecimal maxCurrent;

    @Column(name = "max_power", precision = 8, scale = 2)
    private BigDecimal maxPower;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Column(name = "verification_by")
    private UUID verificationBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "trust_level", nullable = false)
    private TrustLevel trustLevel = TrustLevel.UNVERIFIED;

    @Column(name = "device_auth_secret", unique = true)
    private String deviceAuthSecret;

    @Column(name = "verification_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "sample_payload", columnDefinition = "TEXT")
    private String samplePayload;

    @Column(name = "verification_request_date")
    private LocalDateTime verificationRequestDate;

    @Column(name = "verification_review_date")
    private LocalDateTime verificationReviewDate;

    @Column(name = "verification_reviewer")
    private UUID verificationReviewer;

    @Column(name = "notes")
    private String notes;

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

    public enum DeviceType {
        SMART_METER,
        CURRENT_TRANSFORMER,
        VOLTAGE_TRANSFORMER,
        POWER_QUALITY_MONITOR,
        SOLAR_INVERTER,
        BATTERY_MONITOR,
        GENERATOR_MONITOR,
        ENVIRONMENTAL_SENSOR,
        GATEWAY_DEVICE,
        CUSTOM_DEVICE
    }

    public enum Protocol {
        MQTT,
        MODBUS_TCP,
        MODBUS_RTU,
        HTTP_REST,
        WEBSOCKET,
        CUSTOM
    }

    public enum DeviceStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE,
        FAULTY,
        DECOMMISSIONED
    }

    public enum ConnectionStatus {
        ONLINE,
        OFFLINE,
        CONNECTING,
        ERROR,
        MAINTENANCE
    }

    public enum TrustLevel {
        OEM_LOCKED,    // Pre-approved OEM devices with immediate access
        UNVERIFIED     // Third-party devices requiring manual verification
    }

    public enum VerificationStatus {
        PENDING,       // Verification request submitted, awaiting review
        UNDER_REVIEW,  // Currently being reviewed by SmartWatts team
        APPROVED,      // Verification approved, device can send data
        REJECTED,      // Verification rejected, device blocked
        SUSPENDED      // Verification suspended due to issues
    }
} 