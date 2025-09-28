package com.smartwatts.energyservice.repository;

import com.smartwatts.energyservice.model.EnergyConsumption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumption, UUID> {
    
    Page<EnergyConsumption> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergyConsumption> findByUserIdAndDeviceId(UUID userId, String deviceId, Pageable pageable);
    
    Page<EnergyConsumption> findByUserIdAndSourceType(UUID userId, EnergyConsumption.EnergySource sourceType, Pageable pageable);
    
    Page<EnergyConsumption> findByUserIdAndPeriodType(UUID userId, EnergyConsumption.PeriodType periodType, Pageable pageable);
    
    List<EnergyConsumption> findByUserIdAndPeriodStartBetween(UUID userId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<EnergyConsumption> findByUserIdAndDeviceIdAndPeriodStartBetween(
            UUID userId, String deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT ec FROM EnergyConsumption ec WHERE ec.userId = :userId AND ec.periodType = :periodType AND ec.periodStart >= :startTime ORDER BY ec.periodStart DESC")
    List<EnergyConsumption> findRecentConsumptionByUserIdAndPeriodType(
            @Param("userId") UUID userId, @Param("periodType") EnergyConsumption.PeriodType periodType, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT SUM(ec.totalEnergy) FROM EnergyConsumption ec WHERE ec.userId = :userId AND ec.periodStart BETWEEN :startTime AND :endTime")
    Double getTotalEnergyConsumedByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT SUM(ec.totalCost) FROM EnergyConsumption ec WHERE ec.userId = :userId AND ec.periodStart BETWEEN :startTime AND :endTime")
    Double getTotalCostByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT MAX(ec.peakPower) FROM EnergyConsumption ec WHERE ec.userId = :userId AND ec.periodStart BETWEEN :startTime AND :endTime")
    Double getPeakPowerByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT AVG(ec.averagePower) FROM EnergyConsumption ec WHERE ec.userId = :userId AND ec.periodStart BETWEEN :startTime AND :endTime")
    Double getAveragePowerByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT ec FROM EnergyConsumption ec WHERE ec.userId = :userId AND ec.isBilled = false ORDER BY ec.periodStart ASC")
    List<EnergyConsumption> findUnbilledConsumptionByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(ec) FROM EnergyConsumption ec WHERE ec.userId = :userId AND ec.periodStart BETWEEN :startTime AND :endTime")
    long countConsumptionByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
} 