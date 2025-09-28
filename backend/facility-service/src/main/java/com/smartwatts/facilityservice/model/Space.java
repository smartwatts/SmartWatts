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
@Table(name = "spaces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Space {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String spaceCode;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceStatus status;
    
    @Column(nullable = false)
    private String building;
    
    @Column(nullable = false)
    private String floor;
    
    @Column
    private String wing;
    
    @Column
    private String roomNumber;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal area;
    
    @Column
    private String areaUnit;
    
    @Column
    private Integer capacity;
    
    @Column
    private String department;
    
    @Column
    private String assignedTo;
    
    @Column
    private String contactPerson;
    
    @Column
    private String phone;
    
    @Column
    private String email;
    
    @Column
    private String accessLevel;
    
    @Column
    private String specialRequirements;
    
    @Column
    private String notes;
    
    @Column
    private String floorPlanUrl;
    
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
