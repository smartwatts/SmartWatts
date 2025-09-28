package com.smartwatts.analyticsservice.dto;

import com.smartwatts.analyticsservice.model.EnergyInsight;
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
public class EnergyInsightDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Insight type is required")
    private EnergyInsight.InsightType insightType;
    
    @NotNull(message = "Insight category is required")
    private EnergyInsight.InsightCategory insightCategory;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Severity is required")
    private EnergyInsight.Severity severity;
    
    @DecimalMin(value = "0.0", message = "Confidence score must be non-negative")
    @DecimalMax(value = "1.0", message = "Confidence score cannot exceed 1.0")
    private BigDecimal confidenceScore;
    
    @DecimalMin(value = "0.0", message = "Energy savings must be non-negative")
    private BigDecimal energySavingsKwh;
    
    @DecimalMin(value = "0.0", message = "Cost savings must be non-negative")
    private BigDecimal costSavingsNgn;
    
    @DecimalMin(value = "0.0", message = "Carbon reduction must be non-negative")
    private BigDecimal carbonReductionKg;
    
    private String recommendation;
    
    private String actionItems;
    
    private String dataSources;
    
    private LocalDateTime timePeriodStart;
    
    private LocalDateTime timePeriodEnd;
    
    private boolean isActionable;
    
    private boolean isImplemented;
    
    private LocalDateTime implementationDate;
    
    private String tags;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 