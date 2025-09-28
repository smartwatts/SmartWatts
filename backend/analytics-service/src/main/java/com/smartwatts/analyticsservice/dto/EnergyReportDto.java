package com.smartwatts.analyticsservice.dto;

import com.smartwatts.analyticsservice.model.EnergyReport;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyReportDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Report type is required")
    private EnergyReport.ReportType reportType;
    
    @NotNull(message = "Report period is required")
    private EnergyReport.ReportPeriod reportPeriod;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Period start is required")
    private LocalDateTime periodStart;
    
    @NotNull(message = "Period end is required")
    private LocalDateTime periodEnd;
    
    @DecimalMin(value = "0.0", message = "Total consumption must be non-negative")
    private BigDecimal totalConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Total cost must be non-negative")
    private BigDecimal totalCostNgn;
    
    @DecimalMin(value = "0.0", message = "Average daily consumption must be non-negative")
    private BigDecimal averageDailyConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Peak consumption must be non-negative")
    private BigDecimal peakConsumptionKwh;
    
    @DecimalMin(value = "0.0", message = "Carbon footprint must be non-negative")
    private BigDecimal carbonFootprintKg;
    
    @DecimalMin(value = "0.0", message = "Efficiency score must be non-negative")
    @DecimalMax(value = "1.0", message = "Efficiency score cannot exceed 1.0")
    private BigDecimal efficiencyScore;
    
    @DecimalMin(value = "0.0", message = "Cost per kWh must be non-negative")
    private BigDecimal costPerKwh;
    
    @DecimalMin(value = "0.0", message = "Savings potential must be non-negative")
    private BigDecimal savingsPotentialNgn;
    
    private String comparisonPreviousPeriod;
    
    private String trendsAnalysis;
    
    private String recommendations;
    
    private String chartsData;
    
    private String insightsSummary;
    
    private String reportUrl;
    
    private boolean isGenerated;
    
    private LocalDateTime generationDate;
    
    private Long fileSizeBytes;
    
    private String format;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 