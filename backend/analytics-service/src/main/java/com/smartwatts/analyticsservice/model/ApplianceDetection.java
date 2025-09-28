package com.smartwatts.analyticsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appliance_detections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplianceDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "appliance_name", nullable = false)
    private String applianceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "appliance_type", nullable = false)
    private ApplianceType applianceType;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "detection_time", nullable = false)
    private LocalDateTime detectionTime;

    @Column(name = "power_consumption", precision = 10, scale = 2)
    private BigDecimal powerConsumption;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DetectionStatus status;

    @Column(name = "user_confirmed")
    private Boolean userConfirmed;

    @Column(name = "user_feedback")
    private String userFeedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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

    public enum ApplianceType {
        REFRIGERATOR,
        AIR_CONDITIONER,
        WASHING_MACHINE,
        DRYER,
        DISHWASHER,
        WATER_HEATER,
        ELECTRIC_STOVE,
        MICROWAVE,
        TELEVISION,
        COMPUTER,
        LIGHTING,
        EV_CHARGER,
        SOLAR_INVERTER,
        GENERATOR,
        PUMP,
        FAN,
        UNKNOWN
    }

    public enum DetectionStatus {
        DETECTED,
        UNKNOWN,
        CONFIRMED,
        REJECTED
    }
}
