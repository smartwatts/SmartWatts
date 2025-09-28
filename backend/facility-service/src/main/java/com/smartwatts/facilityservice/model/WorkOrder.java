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
@Table(name = "work_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String workOrderNumber;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderPriority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;
    
    @Column
    private String location;
    
    @Column
    private String assignedTechnician;
    
    @Column
    private String requestedBy;
    
    @Column
    private String department;
    
    @Column
    private LocalDateTime requestedDate;
    
    @Column
    private LocalDateTime scheduledDate;
    
    @Column
    private LocalDateTime startDate;
    
    @Column
    private LocalDateTime completedDate;
    
    @Column
    private LocalDateTime dueDate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedCost;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal actualCost;
    
    @Column
    private String materialsUsed;
    
    @Column
    private String workPerformed;
    
    @Column
    private String notes;
    
    @Column
    private String attachments;
    
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
