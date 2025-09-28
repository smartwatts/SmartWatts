package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.UsagePattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UsagePatternRepository extends JpaRepository<UsagePattern, UUID> {
    
    Page<UsagePattern> findByUserId(UUID userId, Pageable pageable);
    
    Page<UsagePattern> findByUserIdAndDeviceId(UUID userId, UUID deviceId, Pageable pageable);
    
    Page<UsagePattern> findByUserIdAndPatternType(UUID userId, UsagePattern.PatternType patternType, Pageable pageable);
    
    List<UsagePattern> findByUserIdAndPatternDateBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<UsagePattern> findByUserIdAndDeviceIdAndPatternDateBetween(UUID userId, UUID deviceId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.isAnomaly = true ORDER BY up.patternDate DESC")
    List<UsagePattern> findAnomalousPatternsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.efficiencyRating >= :minRating ORDER BY up.efficiencyRating DESC")
    List<UsagePattern> findEfficientPatternsByUserId(@Param("userId") UUID userId, @Param("minRating") BigDecimal minRating);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.optimizationPotential >= :minPotential ORDER BY up.optimizationPotential DESC")
    List<UsagePattern> findHighOptimizationPotentialByUserId(@Param("userId") UUID userId, @Param("minPotential") BigDecimal minPotential);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.confidenceScore >= :minConfidence ORDER BY up.confidenceScore DESC")
    List<UsagePattern> findHighConfidencePatternsByUserId(@Param("userId") UUID userId, @Param("minConfidence") BigDecimal minConfidence);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.frequencyPercentage >= :minFrequency ORDER BY up.frequencyPercentage DESC")
    List<UsagePattern> findFrequentPatternsByUserId(@Param("userId") UUID userId, @Param("minFrequency") BigDecimal minFrequency);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.category = :category ORDER BY up.patternDate DESC")
    List<UsagePattern> findPatternsByCategory(@Param("userId") UUID userId, @Param("category") String category);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.subcategory = :subcategory ORDER BY up.patternDate DESC")
    List<UsagePattern> findPatternsBySubcategory(@Param("userId") UUID userId, @Param("subcategory") String subcategory);
    
    @Query("SELECT AVG(up.totalConsumptionKwh) FROM UsagePattern up WHERE up.userId = :userId AND up.patternDate BETWEEN :startDate AND :endDate")
    BigDecimal findAverageConsumptionByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(up.efficiencyRating) FROM UsagePattern up WHERE up.userId = :userId AND up.patternDate BETWEEN :startDate AND :endDate")
    BigDecimal findAverageEfficiencyByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(up) FROM UsagePattern up WHERE up.userId = :userId AND up.isAnomaly = true AND up.patternDate BETWEEN :startDate AND :endDate")
    long countAnomalousPatternsByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT up FROM UsagePattern up WHERE up.userId = :userId AND up.patternDate >= :since ORDER BY up.patternDate DESC")
    List<UsagePattern> findRecentPatternsByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
} 