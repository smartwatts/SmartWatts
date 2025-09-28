package com.smartwatts.deviceservice.repository;

import com.smartwatts.deviceservice.model.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CircuitRepository extends JpaRepository<Circuit, UUID> {
    
    List<Circuit> findBySubPanelId(UUID subPanelId);
    
    List<Circuit> findBySubPanelIdAndIsActive(UUID subPanelId, Boolean isActive);
    
    List<Circuit> findByStatus(Circuit.CircuitStatus status);
    
    List<Circuit> findBySubPanelIdAndStatus(UUID subPanelId, Circuit.CircuitStatus status);
    
    @Query("SELECT c FROM Circuit c WHERE c.subPanelId = :subPanelId AND c.lastReadingTime >= :startTime")
    List<Circuit> findBySubPanelIdAndLastReadingTimeAfter(UUID subPanelId, LocalDateTime startTime);
    
    @Query("SELECT c FROM Circuit c WHERE c.subPanelId = :subPanelId AND c.maxCapacity > 0 AND (c.currentReading / c.maxCapacity) > 0.9")
    List<Circuit> findOverloadedCircuits(UUID subPanelId);
    
    @Query("SELECT COUNT(c) FROM Circuit c WHERE c.subPanelId = :subPanelId AND c.isActive = true")
    long countActiveCircuitsBySubPanelId(UUID subPanelId);
    
    @Query("SELECT AVG(c.currentReading) FROM Circuit c WHERE c.subPanelId = :subPanelId AND c.isActive = true")
    Double getAverageCurrentReadingBySubPanelId(UUID subPanelId);
    
    @Query("SELECT SUM(c.powerReading) FROM Circuit c WHERE c.subPanelId = :subPanelId AND c.isActive = true")
    Double getTotalPowerReadingBySubPanelId(UUID subPanelId);
}
