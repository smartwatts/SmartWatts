package com.smartwatts.deviceverificationservice.repository;

import com.smartwatts.deviceverificationservice.model.VerificationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationAuditLogRepository extends JpaRepository<VerificationAuditLog, UUID> {

    List<VerificationAuditLog> findByDeviceId(String deviceId);
    
    List<VerificationAuditLog> findByUserId(UUID userId);
    
    List<VerificationAuditLog> findByInstallerId(UUID installerId);
    
    List<VerificationAuditLog> findByAction(String action);
    
    List<VerificationAuditLog> findBySuccess(boolean success);
    
    @Query("SELECT l FROM VerificationAuditLog l WHERE l.createdAt BETWEEN :startDate AND :endDate")
    List<VerificationAuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT l FROM VerificationAuditLog l WHERE l.deviceId = :deviceId ORDER BY l.createdAt DESC")
    List<VerificationAuditLog> findRecentLogsByDeviceId(@Param("deviceId") String deviceId);
    
    @Query("SELECT COUNT(l) FROM VerificationAuditLog l WHERE l.deviceId = :deviceId AND l.success = false")
    long countFailedActionsByDeviceId(@Param("deviceId") String deviceId);
    
    @Query("SELECT l FROM VerificationAuditLog l WHERE l.action = :action AND l.success = false ORDER BY l.createdAt DESC")
    List<VerificationAuditLog> findRecentFailedActions(@Param("action") String action);
}
