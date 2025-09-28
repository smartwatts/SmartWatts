package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.EnergyInsight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergyInsightRepository extends JpaRepository<EnergyInsight, UUID> {
    
    Page<EnergyInsight> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergyInsight> findByUserIdAndInsightType(UUID userId, EnergyInsight.InsightType insightType, Pageable pageable);
    
    Page<EnergyInsight> findByUserIdAndInsightCategory(UUID userId, EnergyInsight.InsightCategory insightCategory, Pageable pageable);
    
    Page<EnergyInsight> findByUserIdAndSeverity(UUID userId, EnergyInsight.Severity severity, Pageable pageable);
    
    List<EnergyInsight> findByUserIdAndIsActionable(UUID userId, boolean isActionable);
    
    List<EnergyInsight> findByUserIdAndIsImplemented(UUID userId, boolean isImplemented);
    
    @Query("SELECT ei FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.confidenceScore >= :minConfidence")
    Page<EnergyInsight> findByUserIdAndMinConfidence(UUID userId, BigDecimal minConfidence, Pageable pageable);
    
    @Query("SELECT ei FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.energySavingsKwh >= :minSavings")
    Page<EnergyInsight> findByUserIdAndMinEnergySavings(UUID userId, BigDecimal minSavings, Pageable pageable);
    
    @Query("SELECT ei FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.costSavingsNgn >= :minCostSavings")
    Page<EnergyInsight> findByUserIdAndMinCostSavings(UUID userId, BigDecimal minCostSavings, Pageable pageable);
    
    @Query("SELECT ei FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.timePeriodStart >= :startDate AND ei.timePeriodEnd <= :endDate")
    List<EnergyInsight> findByUserIdAndTimePeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(ei) FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.severity = :severity")
    long countByUserIdAndSeverity(UUID userId, EnergyInsight.Severity severity);
    
    @Query("SELECT COUNT(ei) FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.isActionable = :isActionable")
    long countByUserIdAndActionable(UUID userId, boolean isActionable);
    
    @Query("SELECT COUNT(ei) FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.isImplemented = :isImplemented")
    long countByUserIdAndImplemented(UUID userId, boolean isImplemented);
    
    @Query("SELECT SUM(ei.energySavingsKwh) FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.isImplemented = true")
    BigDecimal getTotalEnergySavingsByUserId(UUID userId);
    
    @Query("SELECT SUM(ei.costSavingsNgn) FROM EnergyInsight ei WHERE ei.userId = :userId AND ei.isImplemented = true")
    BigDecimal getTotalCostSavingsByUserId(UUID userId);
} 