package com.smartwatts.energyservice.repository;

import com.smartwatts.energyservice.model.EnergyReading;
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
public interface EnergyReadingRepository extends JpaRepository<EnergyReading, UUID> {
    
    Page<EnergyReading> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergyReading> findByUserIdAndDeviceId(UUID userId, String deviceId, Pageable pageable);
    
    Page<EnergyReading> findByUserIdAndSourceType(UUID userId, EnergyReading.EnergySource sourceType, Pageable pageable);
    
    List<EnergyReading> findByUserIdAndReadingTimestampBetween(UUID userId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<EnergyReading> findByUserIdAndDeviceIdAndReadingTimestampBetween(
            UUID userId, String deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT er FROM EnergyReading er WHERE er.userId = :userId AND er.readingTimestamp >= :startTime ORDER BY er.readingTimestamp DESC")
    List<EnergyReading> findRecentReadingsByUserId(@Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT er FROM EnergyReading er WHERE er.userId = :userId AND er.deviceId = :deviceId AND er.readingTimestamp >= :startTime ORDER BY er.readingTimestamp DESC")
    List<EnergyReading> findRecentReadingsByUserIdAndDeviceId(
            @Param("userId") UUID userId, @Param("deviceId") String deviceId, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT COUNT(er) FROM EnergyReading er WHERE er.userId = :userId AND er.isProcessed = false")
    long countUnprocessedReadingsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT er FROM EnergyReading er WHERE er.userId = :userId AND er.isProcessed = false ORDER BY er.readingTimestamp ASC")
    List<EnergyReading> findUnprocessedReadingsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT AVG(er.power) FROM EnergyReading er WHERE er.userId = :userId AND er.readingTimestamp BETWEEN :startTime AND :endTime")
    Double getAveragePowerByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT MAX(er.power) FROM EnergyReading er WHERE er.userId = :userId AND er.readingTimestamp BETWEEN :startTime AND :endTime")
    Double getPeakPowerByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT SUM(er.energyConsumed) FROM EnergyReading er WHERE er.userId = :userId AND er.readingTimestamp BETWEEN :startTime AND :endTime")
    Double getTotalEnergyConsumedByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(er) FROM EnergyReading er WHERE er.userId = :userId AND er.readingTimestamp BETWEEN :startTime AND :endTime")
    long countReadingsByUserIdAndTimeRange(
            @Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
} 