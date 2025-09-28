package com.smartwatts.appliancemonitoringservice.repository;

import com.smartwatts.appliancemonitoringservice.model.ApplianceReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApplianceReadingRepository extends JpaRepository<ApplianceReading, UUID> {
    
    List<ApplianceReading> findByApplianceId(UUID applianceId);
    
    List<ApplianceReading> findByApplianceIdAndTimestampBetween(
        UUID applianceId, LocalDateTime startTime, LocalDateTime endTime);
    
    ApplianceReading findTopByApplianceIdOrderByTimestampDesc(UUID applianceId);
    
    @Query("SELECT ar FROM ApplianceReading ar WHERE ar.applianceId = :applianceId AND ar.anomalyDetected = true ORDER BY ar.timestamp DESC")
    List<ApplianceReading> findAnomaliesByApplianceId(@Param("applianceId") UUID applianceId);
    
    @Query("SELECT ar FROM ApplianceReading ar WHERE ar.applianceId = :applianceId AND ar.maintenanceAlert = true ORDER BY ar.timestamp DESC")
    List<ApplianceReading> findMaintenanceAlertsByApplianceId(@Param("applianceId") UUID applianceId);
    
    @Query("SELECT ar FROM ApplianceReading ar WHERE ar.timestamp >= :startTime AND ar.anomalyDetected = true")
    List<ApplianceReading> findRecentAnomalies(@Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT ar FROM ApplianceReading ar WHERE ar.timestamp >= :startTime AND ar.maintenanceAlert = true")
    List<ApplianceReading> findRecentMaintenanceAlerts(@Param("startTime") LocalDateTime startTime);
}
