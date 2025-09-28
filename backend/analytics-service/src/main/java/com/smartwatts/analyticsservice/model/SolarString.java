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
@Table(name = "solar_strings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolarString {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "inverter_id", nullable = false)
    private UUID inverterId;

    @Column(name = "string_number", nullable = false)
    private Integer stringNumber;

    @Column(name = "panel_count")
    private Integer panelCount;

    @Column(name = "rated_power", precision = 10, scale = 2)
    private BigDecimal ratedPower;

    @Column(name = "voltage", precision = 10, scale = 2)
    private BigDecimal voltage;

    @Column(name = "current", precision = 10, scale = 2)
    private BigDecimal current;

    @Column(name = "power", precision = 10, scale = 2)
    private BigDecimal power;

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
