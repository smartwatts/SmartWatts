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
@Table(name = "energy_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EnergyReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "report_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;
    
    @Column(name = "report_period", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportPeriod reportPeriod;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;
    
    @Column(name = "total_consumption_kwh", precision = 12, scale = 3)
    private BigDecimal totalConsumptionKwh;
    
    @Column(name = "total_cost_ngn", precision = 15, scale = 2)
    private BigDecimal totalCostNgn;
    
    @Column(name = "average_daily_consumption_kwh", precision = 10, scale = 3)
    private BigDecimal averageDailyConsumptionKwh;
    
    @Column(name = "peak_consumption_kwh", precision = 10, scale = 3)
    private BigDecimal peakConsumptionKwh;
    
    @Column(name = "carbon_footprint_kg", precision = 10, scale = 2)
    private BigDecimal carbonFootprintKg;
    
    @Column(name = "efficiency_score", precision = 3, scale = 2)
    private BigDecimal efficiencyScore;
    
    @Column(name = "cost_per_kwh", precision = 8, scale = 4)
    private BigDecimal costPerKwh;
    
    @Column(name = "savings_potential_ngn", precision = 12, scale = 2)
    private BigDecimal savingsPotentialNgn;
    
    @Column(name = "comparison_previous_period")
    private String comparisonPreviousPeriod; // JSON object with comparison data
    
    @Column(name = "trends_analysis")
    private String trendsAnalysis; // JSON object with trend data
    
    @Column(name = "recommendations")
    private String recommendations; // JSON array of recommendations
    
    @Column(name = "charts_data")
    private String chartsData; // JSON object with chart configurations and data
    
    @Column(name = "insights_summary")
    private String insightsSummary; // JSON object with key insights
    
    @Column(name = "report_url")
    private String reportUrl;
    
    @Column(name = "is_generated", nullable = false)
    private boolean isGenerated;
    
    @Column(name = "generation_date")
    private LocalDateTime generationDate;
    
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;
    
    @Column(name = "format")
    private String format; // PDF, CSV, JSON, etc.
    
    @Column(name = "metadata")
    private String metadata; // JSON object for additional data
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum ReportType {
        CONSUMPTION_SUMMARY, COST_ANALYSIS, EFFICIENCY_REPORT, COMPARATIVE_ANALYSIS,
        TREND_ANALYSIS, PREDICTION_REPORT, MAINTENANCE_REPORT, CUSTOM_REPORT
    }
    
    public enum ReportPeriod {
        DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    }
} 