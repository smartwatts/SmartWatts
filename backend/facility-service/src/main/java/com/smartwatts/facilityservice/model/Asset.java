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
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String assetCode;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType assetType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;
    
    @Column(nullable = false)
    private String location;
    
    @Column
    private String building;
    
    @Column
    private String floor;
    
    @Column
    private String room;
    
    @Column
    private String manufacturer;
    
    @Column
    private String model;
    
    @Column
    private String serialNumber;
    
    @Column
    private LocalDateTime installationDate;
    
    @Column
    private LocalDateTime warrantyExpiryDate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal purchaseCost;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal currentValue;
    
    @Column
    private String assignedTo;
    
    @Column
    private String department;
    
    @Column
    private String notes;
    
    @Column
    private String imageUrl;
    
    @Column
    private String qrCode;
    
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
