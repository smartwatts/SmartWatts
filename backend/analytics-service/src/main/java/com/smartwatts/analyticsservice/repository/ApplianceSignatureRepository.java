package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.ApplianceSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplianceSignatureRepository extends JpaRepository<ApplianceSignature, UUID> {
    
    List<ApplianceSignature> findByDeviceId(UUID deviceId);
    
    List<ApplianceSignature> findByDeviceIdAndIsActive(UUID deviceId, Boolean isActive);
    
    List<ApplianceSignature> findByApplianceType(ApplianceSignature.ApplianceType applianceType);
    
    List<ApplianceSignature> findByDeviceIdAndApplianceType(UUID deviceId, ApplianceSignature.ApplianceType applianceType);
    
    @Query("SELECT as FROM ApplianceSignature as WHERE as.deviceId = :deviceId AND as.applianceName = :applianceName")
    ApplianceSignature findByDeviceIdAndApplianceName(UUID deviceId, String applianceName);
    
    @Query("SELECT as FROM ApplianceSignature as WHERE as.deviceId = :deviceId AND as.accuracyScore >= :minAccuracy")
    List<ApplianceSignature> findByDeviceIdAndMinAccuracy(UUID deviceId, Double minAccuracy);
    
    @Query("SELECT COUNT(as) FROM ApplianceSignature as WHERE as.deviceId = :deviceId")
    long countByDeviceId(UUID deviceId);
    
    @Query("SELECT AVG(as.accuracyScore) FROM ApplianceSignature as WHERE as.deviceId = :deviceId")
    Double getAverageAccuracyByDeviceId(UUID deviceId);
}
