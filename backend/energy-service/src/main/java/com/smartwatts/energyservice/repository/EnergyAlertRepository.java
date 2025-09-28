package com.smartwatts.energyservice.repository;

import com.smartwatts.energyservice.model.EnergyAlert;
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
public interface EnergyAlertRepository extends JpaRepository<EnergyAlert, UUID> {
    
    Page<EnergyAlert> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergyAlert> findByUserIdAndAlertType(UUID userId, EnergyAlert.AlertType alertType, Pageable pageable);
    
    Page<EnergyAlert> findByUserIdAndSeverity(UUID userId, EnergyAlert.Severity severity, Pageable pageable);
    
    Page<EnergyAlert> findByUserIdAndIsAcknowledged(UUID userId, Boolean isAcknowledged, Pageable pageable);
    
    Page<EnergyAlert> findByUserIdAndIsResolved(UUID userId, Boolean isResolved, Pageable pageable);
    
    List<EnergyAlert> findByUserIdAndAlertTimestampBetween(UUID userId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT ea FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.alertTimestamp >= :startTime ORDER BY ea.alertTimestamp DESC")
    List<EnergyAlert> findRecentAlertsByUserId(@Param("userId") UUID userId, @Param("startTime") LocalDateTime startTime);
    
    @Query("SELECT ea FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.isAcknowledged = false ORDER BY ea.alertTimestamp DESC")
    List<EnergyAlert> findUnacknowledgedAlertsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ea FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.isResolved = false ORDER BY ea.alertTimestamp DESC")
    List<EnergyAlert> findUnresolvedAlertsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ea FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.severity = :severity AND ea.isResolved = false ORDER BY ea.alertTimestamp DESC")
    List<EnergyAlert> findActiveAlertsByUserIdAndSeverity(@Param("userId") UUID userId, @Param("severity") EnergyAlert.Severity severity);
    
    @Query("SELECT COUNT(ea) FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.isAcknowledged = false")
    long countUnacknowledgedAlertsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(ea) FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.isResolved = false")
    long countUnresolvedAlertsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(ea) FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.severity = :severity AND ea.alertTimestamp BETWEEN :startTime AND :endTime")
    long countAlertsByUserIdAndSeverityAndTimeRange(
            @Param("userId") UUID userId, @Param("severity") EnergyAlert.Severity severity, 
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT ea FROM EnergyAlert ea WHERE ea.userId = :userId AND ea.notificationSent = false ORDER BY ea.alertTimestamp ASC")
    List<EnergyAlert> findPendingNotificationsByUserId(@Param("userId") UUID userId);
} 