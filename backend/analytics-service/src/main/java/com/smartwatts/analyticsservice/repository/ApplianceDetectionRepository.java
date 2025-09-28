package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.ApplianceDetection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApplianceDetectionRepository extends JpaRepository<ApplianceDetection, UUID> {
    
    List<ApplianceDetection> findByDeviceId(UUID deviceId);
    
    List<ApplianceDetection> findByDeviceIdAndDetectionTimeBetween(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<ApplianceDetection> findByDeviceIdAndApplianceType(UUID deviceId, ApplianceDetection.ApplianceType applianceType);
    
    List<ApplianceDetection> findByDeviceIdAndStatus(UUID deviceId, ApplianceDetection.DetectionStatus status);
    
    List<ApplianceDetection> findByDeviceIdAndUserConfirmed(UUID deviceId, Boolean userConfirmed);
    
    Page<ApplianceDetection> findByDeviceId(UUID deviceId, Pageable pageable);
    
    Page<ApplianceDetection> findByDeviceIdAndDetectionTimeBetween(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    @Query("SELECT ad FROM ApplianceDetection ad WHERE ad.deviceId = :deviceId AND ad.confidenceScore >= :minConfidence")
    List<ApplianceDetection> findByDeviceIdAndMinConfidence(UUID deviceId, Double minConfidence);
    
    @Query("SELECT ad FROM ApplianceDetection ad WHERE ad.deviceId = :deviceId AND ad.detectionTime >= :startTime AND ad.detectionTime <= :endTime")
    List<ApplianceDetection> findByDeviceIdAndDetectionTimeRange(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT COUNT(ad) FROM ApplianceDetection ad WHERE ad.deviceId = :deviceId AND ad.applianceType = :applianceType")
    long countByDeviceIdAndApplianceType(UUID deviceId, ApplianceDetection.ApplianceType applianceType);
    
    @Query("SELECT COUNT(ad) FROM ApplianceDetection ad WHERE ad.deviceId = :deviceId AND ad.status = :status")
    long countByDeviceIdAndStatus(UUID deviceId, ApplianceDetection.DetectionStatus status);
    
    @Query("SELECT AVG(ad.confidenceScore) FROM ApplianceDetection ad WHERE ad.deviceId = :deviceId")
    Double getAverageConfidenceByDeviceId(UUID deviceId);
    
    @Query("SELECT ad.applianceType, COUNT(ad) FROM ApplianceDetection ad WHERE ad.deviceId = :deviceId GROUP BY ad.applianceType")
    List<Object[]> getDetectionCountsByApplianceType(UUID deviceId);
}
