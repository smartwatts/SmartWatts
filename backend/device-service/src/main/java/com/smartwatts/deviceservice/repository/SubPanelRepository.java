package com.smartwatts.deviceservice.repository;

import com.smartwatts.deviceservice.model.SubPanel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubPanelRepository extends JpaRepository<SubPanel, UUID> {
    
    List<SubPanel> findByDeviceId(UUID deviceId);
    
    List<SubPanel> findByDeviceIdAndIsActive(UUID deviceId, Boolean isActive);
    
    List<SubPanel> findByStatus(SubPanel.SubPanelStatus status);
    
    List<SubPanel> findByDeviceIdAndStatus(UUID deviceId, SubPanel.SubPanelStatus status);
    
    @Query("SELECT sp FROM SubPanel sp WHERE sp.deviceId = :deviceId AND sp.lastReadingTime >= :startTime")
    List<SubPanel> findByDeviceIdAndLastReadingTimeAfter(UUID deviceId, LocalDateTime startTime);
    
    @Query("SELECT sp FROM SubPanel sp WHERE sp.deviceId = :deviceId AND sp.maxCapacity > 0 AND (sp.currentReading / sp.maxCapacity) > 0.9")
    List<SubPanel> findOverloadedSubPanels(UUID deviceId);
    
    @Query("SELECT COUNT(sp) FROM SubPanel sp WHERE sp.deviceId = :deviceId AND sp.isActive = true")
    long countActiveSubPanelsByDeviceId(UUID deviceId);
    
    @Query("SELECT AVG(sp.currentReading) FROM SubPanel sp WHERE sp.deviceId = :deviceId AND sp.isActive = true")
    Double getAverageCurrentReadingByDeviceId(UUID deviceId);
    
    @Query("SELECT SUM(sp.powerReading) FROM SubPanel sp WHERE sp.deviceId = :deviceId AND sp.isActive = true")
    Double getTotalPowerReadingByDeviceId(UUID deviceId);
}
