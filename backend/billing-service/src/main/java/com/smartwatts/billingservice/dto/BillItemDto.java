package com.smartwatts.billingservice.dto;

import com.smartwatts.billingservice.model.BillItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItemDto {
    
    private UUID id;
    
    @NotNull(message = "Bill ID is required")
    private UUID billId;
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    private String itemDescription;
    
    @NotNull(message = "Item type is required")
    private BillItem.ItemType itemType;
    
    @DecimalMin(value = "0.0", message = "Quantity must be positive")
    private BigDecimal quantity;
    
    @NotBlank(message = "Unit is required")
    private String unit;
    
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", message = "Subtotal must be positive")
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.0", message = "Tax rate must be positive")
    private BigDecimal taxRate;
    
    @DecimalMin(value = "0.0", message = "Tax amount must be positive")
    private BigDecimal taxAmount;
    
    @DecimalMin(value = "0.0", message = "Discount rate must be positive")
    private BigDecimal discountRate;
    
    @DecimalMin(value = "0.0", message = "Discount amount must be positive")
    private BigDecimal discountAmount;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    private BigDecimal startReading;
    
    private BigDecimal endReading;
    
    @DecimalMin(value = "0.0", message = "Consumption must be positive")
    private BigDecimal consumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Rate per kWh must be positive")
    private BigDecimal ratePerKwh;
    
    private Integer peakHours;
    
    private Integer offPeakHours;
    
    private Integer nightHours;
    
    @DecimalMin(value = "0.0", message = "Peak consumption must be positive")
    private BigDecimal peakConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Off-peak consumption must be positive")
    private BigDecimal offPeakConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Night consumption must be positive")
    private BigDecimal nightConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Peak rate must be positive")
    private BigDecimal peakRate;
    
    @DecimalMin(value = "0.0", message = "Off-peak rate must be positive")
    private BigDecimal offPeakRate;
    
    @DecimalMin(value = "0.0", message = "Night rate must be positive")
    private BigDecimal nightRate;
    
    @DecimalMin(value = "0.0", message = "Peak amount must be positive")
    private BigDecimal peakAmount;
    
    @DecimalMin(value = "0.0", message = "Off-peak amount must be positive")
    private BigDecimal offPeakAmount;
    
    @DecimalMin(value = "0.0", message = "Night amount must be positive")
    private BigDecimal nightAmount;
    
    @DecimalMin(value = "0.0", message = "Service charge must be positive")
    private BigDecimal serviceCharge;
    
    @DecimalMin(value = "0.0", message = "Meter rental must be positive")
    private BigDecimal meterRental;
    
    @DecimalMin(value = "0.0", message = "Demand charge must be positive")
    private BigDecimal demandCharge;
    
    @DecimalMin(value = "0.0", message = "Fuel adjustment must be positive")
    private BigDecimal fuelAdjustment;
    
    @DecimalMin(value = "0.0", message = "Capacity charge must be positive")
    private BigDecimal capacityCharge;
    
    @DecimalMin(value = "0.0", message = "Transmission charge must be positive")
    private BigDecimal transmissionCharge;
    
    @DecimalMin(value = "0.0", message = "Distribution charge must be positive")
    private BigDecimal distributionCharge;
    
    @DecimalMin(value = "0.0", message = "Regulatory charge must be positive")
    private BigDecimal regulatoryCharge;
    
    @DecimalMin(value = "0.0", message = "Environmental charge must be positive")
    private BigDecimal environmentalCharge;
    
    @DecimalMin(value = "0.0", message = "Other charges must be positive")
    private BigDecimal otherCharges;
    
    private String notes;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 