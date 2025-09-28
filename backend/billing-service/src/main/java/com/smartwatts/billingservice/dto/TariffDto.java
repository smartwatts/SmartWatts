package com.smartwatts.billingservice.dto;

import com.smartwatts.billingservice.model.Tariff;
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
public class TariffDto {
    
    private UUID id;
    
    @NotBlank(message = "Tariff name is required")
    private String tariffName;
    
    @NotBlank(message = "Tariff code is required")
    private String tariffCode;
    
    @NotNull(message = "Tariff type is required")
    private Tariff.TariffType tariffType;
    
    @NotNull(message = "Customer category is required")
    private Tariff.CustomerCategory customerCategory;
    
    @NotNull(message = "Effective date is required")
    private LocalDateTime effectiveDate;
    
    private LocalDateTime expiryDate;
    
    private Boolean isActive;
    
    private Boolean isApproved;
    
    private UUID approvedBy;
    
    private LocalDateTime approvedDate;
    
    private String approvedByAuthority;
    
    private String approvalReference;
    
    @DecimalMin(value = "0.0", message = "Base rate must be positive")
    private BigDecimal baseRate;
    
    @DecimalMin(value = "0.0", message = "Peak rate must be positive")
    private BigDecimal peakRate;
    
    @DecimalMin(value = "0.0", message = "Off-peak rate must be positive")
    private BigDecimal offPeakRate;
    
    @DecimalMin(value = "0.0", message = "Night rate must be positive")
    private BigDecimal nightRate;
    
    @DecimalMin(value = "0.0", message = "Service charge must be positive")
    private BigDecimal serviceCharge;
    
    @DecimalMin(value = "0.0", message = "Meter rental must be positive")
    private BigDecimal meterRental;
    
    @DecimalMin(value = "0.0", message = "Demand charge must be positive")
    private BigDecimal demandCharge;
    
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
    
    @DecimalMin(value = "0.0", message = "Fuel adjustment rate must be positive")
    private BigDecimal fuelAdjustmentRate;
    
    @DecimalMin(value = "0.0", message = "Tax rate must be positive")
    private BigDecimal taxRate;
    
    @DecimalMin(value = "0.0", message = "Minimum charge must be positive")
    private BigDecimal minimumCharge;
    
    @DecimalMin(value = "0.0", message = "Maximum charge must be positive")
    private BigDecimal maximumCharge;
    
    private String peakHoursStart;
    
    private String peakHoursEnd;
    
    private String offPeakHoursStart;
    
    private String offPeakHoursEnd;
    
    private String nightHoursStart;
    
    private String nightHoursEnd;
    
    @DecimalMin(value = "0.0", message = "Minimum consumption must be positive")
    private BigDecimal minimumConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Maximum consumption must be positive")
    private BigDecimal maximumConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Tier 1 limit must be positive")
    private BigDecimal tier1Limit;
    
    @DecimalMin(value = "0.0", message = "Tier 1 rate must be positive")
    private BigDecimal tier1Rate;
    
    @DecimalMin(value = "0.0", message = "Tier 2 limit must be positive")
    private BigDecimal tier2Limit;
    
    @DecimalMin(value = "0.0", message = "Tier 2 rate must be positive")
    private BigDecimal tier2Rate;
    
    @DecimalMin(value = "0.0", message = "Tier 3 limit must be positive")
    private BigDecimal tier3Limit;
    
    @DecimalMin(value = "0.0", message = "Tier 3 rate must be positive")
    private BigDecimal tier3Rate;
    
    @DecimalMin(value = "0.0", message = "Tier 4 rate must be positive")
    private BigDecimal tier4Rate;
    
    private String currency;
    
    private String discoCode;
    
    private String discoName;
    
    private String region;
    
    private String state;
    
    private String city;
    
    private String description;
    
    private String notes;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    private BigDecimal vatRate;
    public BigDecimal getVatRate() {
        return vatRate;
    }
} 