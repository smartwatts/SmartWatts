package com.smartwatts.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatsDto {
    private Long totalItems;
    private BigDecimal totalValue;
    private Long lowStockItems;
    private Long outOfStockItems;
    private Long inStockItems;
    private Long discontinuedItems;
    private Long totalCategories;
    private Long totalSuppliers;
}

