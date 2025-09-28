package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.SolarString;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SolarStringRepository extends JpaRepository<SolarString, UUID> {
    
    List<SolarString> findByInverterId(UUID inverterId);
    
    List<SolarString> findByInverterIdAndIsActive(UUID inverterId, Boolean isActive);
    
    List<SolarString> findByHasFault(Boolean hasFault);
    
    List<SolarString> findByInverterIdAndHasFault(UUID inverterId, Boolean hasFault);
    
    @Query("SELECT ss FROM SolarString ss WHERE ss.inverterId = :inverterId AND ss.lastReadingTime >= :startTime")
    List<SolarString> findByInverterIdAndLastReadingTimeAfter(UUID inverterId, LocalDateTime startTime);
    
    @Query("SELECT ss FROM SolarString ss WHERE ss.inverterId = :inverterId AND ss.efficiency < :minEfficiency")
    List<SolarString> findUnderperformingStrings(UUID inverterId, Double minEfficiency);
    
    @Query("SELECT COUNT(ss) FROM SolarString ss WHERE ss.inverterId = :inverterId AND ss.isActive = true")
    long countActiveStringsByInverterId(UUID inverterId);
    
    @Query("SELECT COUNT(ss) FROM SolarString ss WHERE ss.inverterId = :inverterId AND ss.hasFault = true")
    long countFaultyStringsByInverterId(UUID inverterId);
    
    @Query("SELECT AVG(ss.efficiency) FROM SolarString ss WHERE ss.inverterId = :inverterId AND ss.isActive = true")
    Double getAverageEfficiencyByInverterId(UUID inverterId);
    
    @Query("SELECT SUM(ss.power) FROM SolarString ss WHERE ss.inverterId = :inverterId AND ss.isActive = true")
    Double getTotalPowerByInverterId(UUID inverterId);
}
