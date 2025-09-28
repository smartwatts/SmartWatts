package com.smartwatts.appliancemonitoringservice.repository;

import com.smartwatts.appliancemonitoringservice.model.Appliance;
import com.smartwatts.appliancemonitoringservice.model.ApplianceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, UUID> {
    
    List<Appliance> findByUserId(UUID userId);
    
    List<Appliance> findByUserIdAndIsActive(UUID userId, Boolean isActive);
    
    List<Appliance> findByApplianceType(ApplianceType applianceType);
    
    List<Appliance> findByDeviceId(UUID deviceId);
    
    @Query("SELECT a FROM Appliance a WHERE a.userId = :userId AND a.applianceType = :applianceType")
    List<Appliance> findByUserIdAndApplianceType(@Param("userId") UUID userId, @Param("applianceType") ApplianceType applianceType);
    
    @Query("SELECT a FROM Appliance a WHERE a.userId = :userId AND a.location = :location")
    List<Appliance> findByUserIdAndLocation(@Param("userId") UUID userId, @Param("location") String location);
    
    @Query("SELECT COUNT(a) FROM Appliance a WHERE a.userId = :userId AND a.isActive = true")
    Long countActiveAppliancesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT a FROM Appliance a WHERE a.warrantyExpiry <= :expiryDate AND a.isActive = true")
    List<Appliance> findAppliancesWithExpiringWarranty(@Param("expiryDate") java.time.LocalDateTime expiryDate);
}
