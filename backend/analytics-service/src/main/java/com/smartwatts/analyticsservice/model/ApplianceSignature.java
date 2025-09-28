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
@Table(name = "appliance_signatures")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplianceSignature {

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

    @Column(name = "typical_power_consumption", precision = 10, scale = 2)
    private BigDecimal typicalPowerConsumption;

    @Column(name = "typical_usage_pattern", length = 50)
    private String typicalUsagePattern;

    @Column(name = "frequency_characteristics", length = 50)
    private String frequencyCharacteristics;

    @Column(name = "training_data_size")
    private Integer trainingDataSize;

    @Column(name = "accuracy_score", precision = 5, scale = 2)
    private BigDecimal accuracyScore;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive;

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
}
