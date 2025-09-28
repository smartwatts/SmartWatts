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
@Table(name = "solar_inverters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolarInverter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "device_id", nullable = false)
    private UUID deviceId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "inverter_type", nullable = false)
    private InverterType inverterType;

    @Column(name = "api_base_url", nullable = false)
    private String apiBaseUrl;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "station_id")
    private String stationId;

    @Column(name = "device_id_external")
    private String deviceIdExternal;

    @Column(name = "max_capacity", precision = 10, scale = 2)
    private BigDecimal maxCapacity;

    @Column(name = "string_count")
    private Integer stringCount;

    @Column(name = "panel_count")
    private Integer panelCount;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

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

    public enum InverterType {
        DEYE,
        SOLIS,
        GROWATT,
        SOLAREDGE,
        ENPHASE,
        FRONIUS,
        SMA,
        OTHER
    }
}
