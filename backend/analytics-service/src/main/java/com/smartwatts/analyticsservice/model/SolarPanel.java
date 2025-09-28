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
@Table(name = "solar_panels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolarPanel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "inverter_id", nullable = false)
    private UUID inverterId;

    @Column(name = "string_id")
    private UUID stringId;

    @Column(name = "panel_number", nullable = false)
    private Integer panelNumber;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "rated_power", precision = 10, scale = 2)
    private BigDecimal ratedPower;

    @Column(name = "voltage", precision = 10, scale = 2)
    private BigDecimal voltage;

    @Column(name = "current", precision = 10, scale = 2)
    private BigDecimal current;

    @Column(name = "current_power", precision = 10, scale = 2)
    private BigDecimal currentPower;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "efficiency", precision = 5, scale = 2)
    private BigDecimal efficiency;

    @Column(name = "has_fault")
    private Boolean hasFault;

    @Column(name = "fault_type")
    private String faultType;

    @Column(name = "last_reading_time")
    private LocalDateTime lastReadingTime;

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
}
