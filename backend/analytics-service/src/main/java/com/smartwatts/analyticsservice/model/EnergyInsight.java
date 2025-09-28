package com.smartwatts.analyticsservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "energy_insights")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EnergyInsight {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "insight_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InsightType insightType;
    
    @Column(name = "insight_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private InsightCategory insightCategory;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "energy_savings_kwh")
    private BigDecimal energySavingsKwh;
    
    @Column(name = "cost_savings_ngn")
    private BigDecimal costSavingsNgn;
    
    @Column(name = "carbon_reduction_kg")
    private BigDecimal carbonReductionKg;
    
    @Column(name = "recommendation")
    private String recommendation;
    
    @Column(name = "action_items")
    private String actionItems; // JSON array of action items
    
    @Column(name = "data_sources")
    private String dataSources; // JSON array of data sources used
    
    @Column(name = "time_period_start")
    private LocalDateTime timePeriodStart;
    
    @Column(name = "time_period_end")
    private LocalDateTime timePeriodEnd;
    
    @Column(name = "is_actionable", nullable = false)
    private boolean isActionable;
    
    @Column(name = "is_implemented", nullable = false)
    private boolean isImplemented;
    
    @Column(name = "implementation_date")
    private LocalDateTime implementationDate;
    
    @Column(name = "tags")
    private String tags; // JSON array of tags
    
    @Column(name = "metadata")
    private String metadata; // JSON object for additional data
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum InsightType {
        CONSUMPTION_PATTERN, PEAK_USAGE, EFFICIENCY_OPPORTUNITY, COST_OPTIMIZATION,
        EQUIPMENT_PERFORMANCE, MAINTENANCE_ALERT, BEHAVIORAL_CHANGE, COMPARATIVE_ANALYSIS
    }
    
    public enum InsightCategory {
        CONSUMPTION, EFFICIENCY, COST, MAINTENANCE, BEHAVIOR, COMPARISON, PREDICTION
    }
    
    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
} 