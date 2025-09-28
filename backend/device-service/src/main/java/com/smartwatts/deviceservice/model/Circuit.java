package com.smartwatts.deviceservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "circuits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Circuit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sub_panel_id", nullable = false)
    private UUID subPanelId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "max_capacity", precision = 10, scale = 2)
    private BigDecimal maxCapacity;

    @Column(name = "current_reading", precision = 10, scale = 2)
    private BigDecimal currentReading;

    @Column(name = "voltage_reading", precision = 10, scale = 2)
    private BigDecimal voltageReading;

    @Column(name = "power_reading", precision = 10, scale = 2)
    private BigDecimal powerReading;

    @Column(name = "last_reading_time")
    private LocalDateTime lastReadingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CircuitStatus status;

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

    public enum CircuitStatus {
        NORMAL,
        HIGH,
        OVERLOAD,
        FAULT,
        OFFLINE
    }
}
