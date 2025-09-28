package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.EnergyAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnergyAnalyticsRepository extends JpaRepository<EnergyAnalytics, UUID> {
    
    Page<EnergyAnalytics> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergyAnalytics> findByUserIdAndDeviceId(UUID userId, UUID deviceId, Pageable pageable);
    
    Page<EnergyAnalytics> findByUserIdAndPeriodType(UUID userId, EnergyAnalytics.PeriodType periodType, Pageable pageable);
    
    List<EnergyAnalytics> findByUserIdAndAnalyticsDateBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<EnergyAnalytics> findByUserIdAndDeviceIdAndAnalyticsDateBetween(UUID userId, UUID deviceId, LocalDateTime startDate, LocalDateTime endDate);
    
    Optional<EnergyAnalytics> findByUserIdAndDeviceIdAndAnalyticsDateAndPeriodType(
            UUID userId, UUID deviceId, LocalDateTime analyticsDate, EnergyAnalytics.PeriodType periodType);
    
    @Query("SELECT ea FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.analyticsDate >= :since ORDER BY ea.analyticsDate DESC")
    List<EnergyAnalytics> findRecentAnalyticsByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT ea FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.efficiencyScore >= :minScore ORDER BY ea.efficiencyScore DESC")
    List<EnergyAnalytics> findEfficientAnalyticsByUserId(@Param("userId") UUID userId, @Param("minScore") BigDecimal minScore);
    
    @Query("SELECT ea FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.savingsPotential >= :minSavings ORDER BY ea.savingsPotential DESC")
    List<EnergyAnalytics> findHighSavingsPotentialByUserId(@Param("userId") UUID userId, @Param("minSavings") BigDecimal minSavings);
    
    @Query("SELECT ea FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.anomalyCount > 0 ORDER BY ea.analyticsDate DESC")
    List<EnergyAnalytics> findAnalyticsWithAnomaliesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT AVG(ea.totalConsumptionKwh) FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.analyticsDate BETWEEN :startDate AND :endDate")
    BigDecimal findAverageConsumptionByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(ea.totalCost) FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.analyticsDate BETWEEN :startDate AND :endDate")
    BigDecimal findTotalCostByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(ea.efficiencyScore) FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.analyticsDate BETWEEN :startDate AND :endDate")
    BigDecimal findAverageEfficiencyByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(ea.carbonFootprintKg) FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.analyticsDate BETWEEN :startDate AND :endDate")
    BigDecimal findTotalCarbonFootprintByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(ea) FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.analyticsDate BETWEEN :startDate AND :endDate")
    long countAnalyticsByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ea FROM EnergyAnalytics ea WHERE ea.userId = :userId AND ea.qualityScore >= :minQuality ORDER BY ea.analyticsDate DESC")
    List<EnergyAnalytics> findHighQualityAnalyticsByUserId(@Param("userId") UUID userId, @Param("minQuality") BigDecimal minQuality);
} 