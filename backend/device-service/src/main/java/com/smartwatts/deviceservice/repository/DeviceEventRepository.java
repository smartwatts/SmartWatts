package com.smartwatts.deviceservice.repository;

import com.smartwatts.deviceservice.model.DeviceEvent;
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
public interface DeviceEventRepository extends JpaRepository<DeviceEvent, UUID> {
    
    Page<DeviceEvent> findByDeviceId(UUID deviceId, Pageable pageable);
    
    Page<DeviceEvent> findByDeviceIdAndEventType(UUID deviceId, DeviceEvent.EventType eventType, Pageable pageable);
    
    Page<DeviceEvent> findByDeviceIdAndSeverity(UUID deviceId, DeviceEvent.Severity severity, Pageable pageable);
    
    Page<DeviceEvent> findByDeviceIdAndIsAcknowledged(UUID deviceId, Boolean isAcknowledged, Pageable pageable);
    
    Page<DeviceEvent> findByDeviceIdAndIsResolved(UUID deviceId, Boolean isResolved, Pageable pageable);
    
    List<DeviceEvent> findByDeviceIdAndEventTimestampBetween(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT de FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.eventTimestamp >= :since ORDER BY de.eventTimestamp DESC")
    List<DeviceEvent> findRecentEventsByDeviceId(@Param("deviceId") UUID deviceId, @Param("since") LocalDateTime since);
    
    @Query("SELECT de FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.isAcknowledged = false ORDER BY de.eventTimestamp DESC")
    List<DeviceEvent> findUnacknowledgedEventsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT de FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.isResolved = false ORDER BY de.eventTimestamp DESC")
    List<DeviceEvent> findUnresolvedEventsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT de FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.severity = :severity AND de.isResolved = false ORDER BY de.eventTimestamp DESC")
    List<DeviceEvent> findActiveEventsByDeviceIdAndSeverity(@Param("deviceId") UUID deviceId, @Param("severity") DeviceEvent.Severity severity);
    
    @Query("SELECT COUNT(de) FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.isAcknowledged = false")
    long countUnacknowledgedEventsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT COUNT(de) FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.isResolved = false")
    long countUnresolvedEventsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT COUNT(de) FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.severity = :severity AND de.eventTimestamp BETWEEN :startTime AND :endTime")
    long countEventsByDeviceIdAndSeverityAndTimeRange(
            @Param("deviceId") UUID deviceId, 
            @Param("severity") DeviceEvent.Severity severity,
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT de FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.notificationSent = false ORDER BY de.eventTimestamp ASC")
    List<DeviceEvent> findPendingNotificationsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT de FROM DeviceEvent de WHERE de.deviceId = :deviceId AND de.eventType = :eventType ORDER BY de.eventTimestamp DESC LIMIT 1")
    DeviceEvent findLatestEventByDeviceIdAndEventType(@Param("deviceId") UUID deviceId, @Param("eventType") DeviceEvent.EventType eventType);
} 