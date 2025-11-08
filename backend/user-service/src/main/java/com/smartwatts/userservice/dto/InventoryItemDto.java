package com.smartwatts.userservice.dto;

import com.smartwatts.userservice.model.InventoryItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDto {
    private UUID id;
    private String name;
    private InventoryItem.Category category;
    private String sku;
    private Integer currentStock;
    private Integer minStock;
    private Integer maxStock;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private String supplier;
    private LocalDate lastRestocked;
    private LocalDate nextRestock;
    private InventoryItem.Status status;
    private String location;
    private InventoryItem.ConditionType conditionType;
    private String warranty;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

