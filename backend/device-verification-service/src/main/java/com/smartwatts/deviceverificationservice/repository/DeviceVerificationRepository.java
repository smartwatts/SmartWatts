package com.smartwatts.deviceverificationservice.repository;

import com.smartwatts.deviceverificationservice.model.DeviceVerification;
import com.smartwatts.deviceverificationservice.model.DeviceStatus;
import com.smartwatts.deviceverificationservice.model.DeviceTrustCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceVerificationRepository extends JpaRepository<DeviceVerification, UUID> {

    Optional<DeviceVerification> findByDeviceId(String deviceId);
    
    Optional<DeviceVerification> findByHardwareId(String hardwareId);
    
    List<DeviceVerification> findByCustomerId(UUID customerId);
    
    List<DeviceVerification> findByCustomerType(String customerType);
    
    List<DeviceVerification> findByStatus(DeviceStatus status);
    
    List<DeviceVerification> findByTrustCategory(DeviceTrustCategory trustCategory);
    
    List<DeviceVerification> findByInstallerId(UUID installerId);
    
    @Query("SELECT d FROM DeviceVerification d WHERE d.expiresAt <= :expiryDate")
    List<DeviceVerification> findExpiredDevices(@Param("expiryDate") LocalDateTime expiryDate);
    
    @Query("SELECT d FROM DeviceVerification d WHERE d.expiresAt BETWEEN :startDate AND :endDate")
    List<DeviceVerification> findDevicesExpiringBetween(@Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT d FROM DeviceVerification d WHERE d.tamperDetected = true")
    List<DeviceVerification> findTamperedDevices();
    
    @Query("SELECT d FROM DeviceVerification d WHERE d.dockerStartupValid = false")
    List<DeviceVerification> findDevicesWithInvalidDockerStartup();
    
    @Query("SELECT COUNT(d) FROM DeviceVerification d WHERE d.customerType = :customerType AND d.status = 'ACTIVE'")
    long countActiveDevicesByCustomerType(@Param("customerType") String customerType);
    
    @Query("SELECT COUNT(d) FROM DeviceVerification d WHERE d.trustCategory = :trustCategory")
    long countDevicesByTrustCategory(@Param("trustCategory") DeviceTrustCategory trustCategory);
    
    @Query("SELECT d FROM DeviceVerification d WHERE d.activationAttempts >= :maxAttempts")
    List<DeviceVerification> findDevicesWithExceededActivationAttempts(@Param("maxAttempts") int maxAttempts);
    
    boolean existsByDeviceId(String deviceId);
    
    boolean existsByHardwareId(String hardwareId);
}
