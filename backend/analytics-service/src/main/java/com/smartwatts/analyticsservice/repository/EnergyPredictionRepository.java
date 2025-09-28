package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.EnergyPrediction;
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
public interface EnergyPredictionRepository extends JpaRepository<EnergyPrediction, UUID> {
    
    Page<EnergyPrediction> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergyPrediction> findByUserIdAndPredictionType(UUID userId, EnergyPrediction.PredictionType predictionType, Pageable pageable);
    
    Page<EnergyPrediction> findByUserIdAndPredictionHorizon(UUID userId, EnergyPrediction.PredictionHorizon predictionHorizon, Pageable pageable);
    
    List<EnergyPrediction> findByUserIdAndTargetDateBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT ep FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.confidenceLevel >= :minConfidence")
    Page<EnergyPrediction> findByUserIdAndMinConfidence(UUID userId, BigDecimal minConfidence, Pageable pageable);
    
    @Query("SELECT ep FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.modelAccuracy >= :minAccuracy")
    Page<EnergyPrediction> findByUserIdAndMinAccuracy(UUID userId, BigDecimal minAccuracy, Pageable pageable);
    
    @Query("SELECT ep FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.isAccurate = :isAccurate")
    Page<EnergyPrediction> findByUserIdAndAccuracy(UUID userId, Boolean isAccurate, Pageable pageable);
    
    @Query("SELECT ep FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.targetDate >= :startDate AND ep.targetDate <= :endDate")
    List<EnergyPrediction> findByUserIdAndTargetDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT ep FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.predictionDate >= :startDate AND ep.predictionDate <= :endDate")
    List<EnergyPrediction> findByUserIdAndPredictionDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(ep) FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.predictionType = :predictionType")
    long countByUserIdAndPredictionType(UUID userId, EnergyPrediction.PredictionType predictionType);
    
    @Query("SELECT COUNT(ep) FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.isAccurate = :isAccurate")
    long countByUserIdAndAccuracy(UUID userId, Boolean isAccurate);
    
    @Query("SELECT AVG(ep.modelAccuracy) FROM EnergyPrediction ep WHERE ep.userId = :userId")
    BigDecimal getAverageModelAccuracyByUserId(UUID userId);
    
    @Query("SELECT AVG(ep.errorPercentage) FROM EnergyPrediction ep WHERE ep.userId = :userId AND ep.isAccurate IS NOT NULL")
    BigDecimal getAverageErrorPercentageByUserId(UUID userId);
} 