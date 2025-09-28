package com.smartwatts.facilityservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "fleet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fleet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String vehicleId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FleetType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FleetStatus status;
    
    @Column(nullable = false)
    private String licensePlate;
    
    @Column
    private String vin;
    
    @Column
    private String make;
    
    @Column
    private String model;
    
    @Column
    private Integer modelYear;
    
    @Column
    private String color;
    
    @Column
    private String fuelType;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal fuelCapacity;
    
    @Column
    private String fuelUnit;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal currentFuelLevel;
    
    @Column
    private Integer mileage;
    
    @Column
    private String mileageUnit;
    
    @Column
    private String assignedDriver;
    
    @Column
    private String department;
    
    @Column
    private String location;
    
    @Column
    private LocalDateTime lastMaintenanceDate;
    
    @Column
    private LocalDateTime nextMaintenanceDate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal purchaseCost;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal currentValue;
    
    @Column
    private String insuranceProvider;
    
    @Column
    private LocalDateTime insuranceExpiryDate;
    
    @Column
    private String registrationNumber;
    
    @Column
    private LocalDateTime registrationExpiryDate;
    
    @Column
    private String notes;
    
    @Column
    private String imageUrl;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private String createdBy;
    
    @Column
    private String updatedBy;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
