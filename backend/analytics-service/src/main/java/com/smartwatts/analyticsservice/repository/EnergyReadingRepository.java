package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.EnergyReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergyReadingRepository extends JpaRepository<EnergyReading, UUID> {
    
    List<EnergyReading> findByDeviceId(UUID deviceId);
    
    List<EnergyReading> findByUserId(UUID userId);
    
    List<EnergyReading> findByDeviceIdAndReadingTimestampBetween(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<EnergyReading> findByUserIdAndReadingTimestampBetween(UUID userId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT er FROM EnergyReading er WHERE er.deviceId = :deviceId ORDER BY er.readingTimestamp DESC")
    List<EnergyReading> findByDeviceIdOrderByReadingTimestampDesc(UUID deviceId);
    
    @Query("SELECT er FROM EnergyReading er WHERE er.userId = :userId ORDER BY er.readingTimestamp DESC")
    List<EnergyReading> findByUserIdOrderByReadingTimestampDesc(UUID userId);
    
    @Query("SELECT AVG(er.powerConsumption) FROM EnergyReading er WHERE er.deviceId = :deviceId AND er.readingTimestamp >= :startTime")
    Double getAveragePowerConsumptionByDeviceIdAndTimeRange(UUID deviceId, LocalDateTime startTime);
    
    @Query("SELECT SUM(er.energyConsumed) FROM EnergyReading er WHERE er.deviceId = :deviceId AND er.readingTimestamp >= :startTime")
    Double getTotalEnergyConsumptionByDeviceIdAndTimeRange(UUID deviceId, LocalDateTime startTime);
}
