package com.smartwatts.deviceservice.repository;

import com.smartwatts.deviceservice.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    
    Page<Device> findByUserId(UUID userId, Pageable pageable);
    
    Page<Device> findByUserIdAndDeviceType(UUID userId, Device.DeviceType deviceType, Pageable pageable);
    
    Page<Device> findByUserIdAndStatus(UUID userId, Device.DeviceStatus status, Pageable pageable);
    
    Page<Device> findByUserIdAndConnectionStatus(UUID userId, Device.ConnectionStatus connectionStatus, Pageable pageable);
    
    Optional<Device> findByDeviceId(String deviceId);
    
    Optional<Device> findByUserIdAndDeviceId(UUID userId, String deviceId);
    
    boolean existsByDeviceId(String deviceId);
    
    boolean existsByUserIdAndDeviceId(UUID userId, String deviceId);
    
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.lastSeen >= :since")
    List<Device> findActiveDevicesByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.lastSeen < :since")
    List<Device> findInactiveDevicesByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT d FROM Device d WHERE d.nextMaintenanceDate <= :date")
    List<Device> findDevicesNeedingMaintenance(@Param("date") LocalDateTime date);
    
    @Query("SELECT d FROM Device d WHERE d.calibrationExpiry <= :date")
    List<Device> findDevicesNeedingCalibration(@Param("date") LocalDateTime date);
    
    @Query("SELECT d FROM Device d WHERE d.warrantyExpiry <= :date")
    List<Device> findDevicesWithExpiringWarranty(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId AND d.status = :status")
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") Device.DeviceStatus status);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId AND d.connectionStatus = :connectionStatus")
    long countByUserIdAndConnectionStatus(@Param("userId") UUID userId, @Param("connectionStatus") Device.ConnectionStatus connectionStatus);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId AND d.isVerified = true")
    long countVerifiedDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId AND d.isCalibrated = true")
    long countCalibratedDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.lastSeen IS NULL")
    List<Device> findNeverConnectedDevicesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.lastSeen < :cutoff")
    List<Device> findOfflineDevicesByUserId(@Param("userId") UUID userId, @Param("cutoff") LocalDateTime cutoff);

    // Device verification queries
    @Query("SELECT d FROM Device d WHERE d.verificationStatus = :status")
    List<Device> findByVerificationStatus(@Param("status") Device.VerificationStatus status);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId AND d.verificationStatus = :status")
    long countByUserIdAndVerificationStatus(@Param("userId") UUID userId, @Param("status") Device.VerificationStatus status);
    
    @Query("SELECT d FROM Device d WHERE d.trustLevel = :trustLevel")
    List<Device> findByTrustLevel(@Param("trustLevel") Device.TrustLevel trustLevel);
    
    @Query("SELECT d FROM Device d WHERE d.deviceAuthSecret = :authSecret")
    Optional<Device> findByDeviceAuthSecret(@Param("authSecret") String authSecret);
    
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.trustLevel = :trustLevel")
    List<Device> findByUserIdAndTrustLevel(@Param("userId") UUID userId, @Param("trustLevel") Device.TrustLevel trustLevel);
    
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId AND d.trustLevel = :trustLevel")
    long countByUserIdAndTrustLevel(@Param("userId") UUID userId, @Param("trustLevel") Device.TrustLevel trustLevel);
    
    @Query("SELECT d FROM Device d WHERE d.verificationStatus = :status AND d.trustLevel = :trustLevel")
    List<Device> findByVerificationStatusAndTrustLevel(@Param("status") Device.VerificationStatus status, 
                                                      @Param("trustLevel") Device.TrustLevel trustLevel);
} 