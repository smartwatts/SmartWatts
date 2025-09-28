package com.smartwatts.deviceservice.repository;

import com.smartwatts.deviceservice.model.DeviceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceConfigurationRepository extends JpaRepository<DeviceConfiguration, UUID> {
    
    List<DeviceConfiguration> findByDeviceId(UUID deviceId);
    
    List<DeviceConfiguration> findByDeviceIdAndIsActive(UUID deviceId, Boolean isActive);
    
    Optional<DeviceConfiguration> findByDeviceIdAndConfigKey(UUID deviceId, String configKey);
    
    boolean existsByDeviceIdAndConfigKey(UUID deviceId, String configKey);
    
    @Query("SELECT dc FROM DeviceConfiguration dc WHERE dc.deviceId = :deviceId AND dc.isRequired = true")
    List<DeviceConfiguration> findRequiredConfigurationsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT dc FROM DeviceConfiguration dc WHERE dc.deviceId = :deviceId AND dc.isEncrypted = true")
    List<DeviceConfiguration> findEncryptedConfigurationsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT COUNT(dc) FROM DeviceConfiguration dc WHERE dc.deviceId = :deviceId AND dc.isActive = true")
    long countActiveConfigurationsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT COUNT(dc) FROM DeviceConfiguration dc WHERE dc.deviceId = :deviceId AND dc.isRequired = true AND dc.configValue IS NULL")
    long countMissingRequiredConfigurationsByDeviceId(@Param("deviceId") UUID deviceId);
    
    @Query("SELECT dc FROM DeviceConfiguration dc WHERE dc.deviceId = :deviceId AND dc.configKey LIKE %:pattern%")
    List<DeviceConfiguration> findConfigurationsByDeviceIdAndKeyPattern(@Param("deviceId") UUID deviceId, @Param("pattern") String pattern);
    
    @Query("SELECT dc FROM DeviceConfiguration dc WHERE dc.deviceId = :deviceId AND dc.dataType = :dataType")
    List<DeviceConfiguration> findConfigurationsByDeviceIdAndDataType(@Param("deviceId") UUID deviceId, @Param("dataType") String dataType);
} 