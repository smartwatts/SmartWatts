package com.smartwatts.analyticsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "community_benchmarks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityBenchmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "metric_type", nullable = false)
    private String metricType;

    @Column(name = "average_value", precision = 10, scale = 2)
    private BigDecimal averageValue;

    @Column(name = "median_value", precision = 10, scale = 2)
    private BigDecimal medianValue;

    @Column(name = "percentile_25", precision = 10, scale = 2)
    private BigDecimal percentile25;

    @Column(name = "percentile_75", precision = 10, scale = 2)
    private BigDecimal percentile75;

    @Column(name = "percentile_90", precision = 10, scale = 2)
    private BigDecimal percentile90;

    @Column(name = "sample_size")
    private Integer sampleSize;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
