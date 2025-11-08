package com.smartwatts.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Column(unique = true, nullable = false)
    private String sku;
    
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;
    
    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 0;
    
    @Column(name = "max_stock", nullable = false)
    private Integer maxStock = 0;
    
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private String supplier;
    
    @Column(name = "last_restocked")
    private LocalDate lastRestocked;
    
    @Column(name = "next_restock")
    private LocalDate nextRestock;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    private String location;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false)
    private ConditionType conditionType;
    
    private String warranty;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum Category {
        SMART_METERS, SOLAR_INVERTERS, MONITORING_DEVICES, SENSORS, ACCESSORIES
    }
    
    public enum Status {
        IN_STOCK, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED
    }
    
    public enum ConditionType {
        NEW, REFURBISHED, USED
    }
    
    // Helper methods
    public void calculateTotalValue() {
        this.totalValue = unitPrice.multiply(BigDecimal.valueOf(currentStock));
    }
    
    public void updateStatus() {
        if (currentStock == 0) {
            this.status = Status.OUT_OF_STOCK;
        } else if (currentStock <= minStock) {
            this.status = Status.LOW_STOCK;
        } else {
            this.status = Status.IN_STOCK;
        }
    }
}

