package com.smartwatts.energyservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "energy_sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EnergySource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "source_name", nullable = false)
    private String sourceName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;
    
    @Column(name = "capacity_kw", precision = 10, scale = 3)
    private BigDecimal capacityKw;
    
    @Column(name = "efficiency_percent", precision = 5, scale = 2)
    private BigDecimal efficiencyPercent;
    
    @Column(name = "installation_date")
    private LocalDateTime installationDate;
    
    @Column(name = "last_maintenance_date")
    private LocalDateTime lastMaintenanceDate;
    
    @Column(name = "next_maintenance_date")
    private LocalDateTime nextMaintenanceDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;
    
    @Column(name = "location_lat", precision = 10, scale = 8)
    private BigDecimal locationLat;
    
    @Column(name = "location_lng", precision = 11, scale = 8)
    private BigDecimal locationLng;
    
    @Column(name = "manufacturer")
    private String manufacturer;
    
    @Column(name = "model")
    private String model;
    
    @Column(name = "serial_number")
    private String serialNumber;
    
    @Column(name = "warranty_expiry")
    private LocalDateTime warrantyExpiry;
    
    @Column(name = "notes")
    private String notes;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum SourceType {
        GRID, SOLAR_PANEL, GENERATOR, INVERTER, BATTERY, HYBRID_SYSTEM
    }
    
    public enum Status {
        ACTIVE, INACTIVE, MAINTENANCE, OFFLINE, ERROR
    }
} 