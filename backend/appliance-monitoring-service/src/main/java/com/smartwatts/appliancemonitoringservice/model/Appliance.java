package com.smartwatts.appliancemonitoringservice.model;

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
@Table(name = "appliances")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Appliance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "appliance_name", nullable = false)
    private String applianceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "appliance_type", nullable = false)
    private ApplianceType applianceType;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "model")
    private String model;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @Column(name = "warranty_expiry")
    private LocalDateTime warrantyExpiry;

    @Column(name = "expected_lifespan_years")
    private Integer expectedLifespanYears;

    @Column(name = "rated_power_watts")
    private BigDecimal ratedPowerWatts;

    @Column(name = "energy_efficiency_rating")
    private String energyEfficiencyRating;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "location")
    private String location;

    @Column(name = "notes")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
