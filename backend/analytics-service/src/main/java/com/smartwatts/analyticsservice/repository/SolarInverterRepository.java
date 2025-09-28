package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.SolarInverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SolarInverterRepository extends JpaRepository<SolarInverter, UUID> {
    
    List<SolarInverter> findByDeviceId(UUID deviceId);
    
    List<SolarInverter> findByDeviceIdAndIsActive(UUID deviceId, Boolean isActive);
    
    List<SolarInverter> findByInverterType(SolarInverter.InverterType inverterType);
    
    List<SolarInverter> findByInverterTypeAndIsActive(SolarInverter.InverterType inverterType, Boolean isActive);
    
    @Query("SELECT si FROM SolarInverter si WHERE si.deviceId = :deviceId AND si.isActive = true")
    List<SolarInverter> findActiveByDeviceId(UUID deviceId);
    
    @Query("SELECT COUNT(si) FROM SolarInverter si WHERE si.deviceId = :deviceId AND si.isActive = true")
    long countActiveByDeviceId(UUID deviceId);
    
    @Query("SELECT COUNT(si) FROM SolarInverter si WHERE si.inverterType = :inverterType AND si.isActive = true")
    long countActiveByInverterType(SolarInverter.InverterType inverterType);
}
