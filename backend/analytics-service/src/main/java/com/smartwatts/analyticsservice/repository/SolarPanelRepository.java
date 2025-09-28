package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.SolarPanel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SolarPanelRepository extends JpaRepository<SolarPanel, UUID> {
    
    List<SolarPanel> findByInverterId(UUID inverterId);
    
    List<SolarPanel> findByInverterIdAndIsActive(UUID inverterId, Boolean isActive);
    
    List<SolarPanel> findByStringId(UUID stringId);
    
    List<SolarPanel> findByStringIdAndIsActive(UUID stringId, Boolean isActive);
    
    List<SolarPanel> findByHasFault(Boolean hasFault);
    
    List<SolarPanel> findByInverterIdAndHasFault(UUID inverterId, Boolean hasFault);
    
    @Query("SELECT sp FROM SolarPanel sp WHERE sp.inverterId = :inverterId AND sp.lastReadingTime >= :startTime")
    List<SolarPanel> findByInverterIdAndLastReadingTimeAfter(UUID inverterId, LocalDateTime startTime);
    
    @Query("SELECT sp FROM SolarPanel sp WHERE sp.inverterId = :inverterId AND sp.efficiency < :minEfficiency")
    List<SolarPanel> findUnderperformingPanels(UUID inverterId, Double minEfficiency);
    
    @Query("SELECT sp FROM SolarPanel sp WHERE sp.inverterId = :inverterId AND sp.temperature > :maxTemperature")
    List<SolarPanel> findOverheatedPanels(UUID inverterId, Double maxTemperature);
    
    @Query("SELECT COUNT(sp) FROM SolarPanel sp WHERE sp.inverterId = :inverterId AND sp.isActive = true")
    long countActivePanelsByInverterId(UUID inverterId);
    
    @Query("SELECT COUNT(sp) FROM SolarPanel sp WHERE sp.inverterId = :inverterId AND sp.hasFault = true")
    long countFaultyPanelsByInverterId(UUID inverterId);
    
    @Query("SELECT AVG(sp.efficiency) FROM SolarPanel sp WHERE sp.inverterId = :inverterId AND sp.isActive = true")
    Double getAverageEfficiencyByInverterId(UUID inverterId);
    
    @Query("SELECT SUM(sp.currentPower) FROM SolarPanel sp WHERE sp.inverterId = :inverterId AND sp.isActive = true")
    Double getTotalPowerByInverterId(UUID inverterId);
}
