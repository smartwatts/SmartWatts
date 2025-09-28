package com.smartwatts.facilityservice.repository;

import com.smartwatts.facilityservice.model.Asset;
import com.smartwatts.facilityservice.model.AssetStatus;
import com.smartwatts.facilityservice.model.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    Optional<Asset> findByIdAndIsActiveTrue(Long id);
    
    Optional<Asset> findByAssetCodeAndIsActiveTrue(String assetCode);
    
    List<Asset> findByStatusAndIsActiveTrue(AssetStatus status);
    
    List<Asset> findByAssetTypeAndIsActiveTrue(AssetType assetType);
    
    List<Asset> findByDepartmentAndIsActiveTrue(String department);
    
    List<Asset> findByLocationAndIsActiveTrue(String location);
    
    List<Asset> findByBuildingAndIsActiveTrue(String building);
    
    List<Asset> findByAssignedToAndIsActiveTrue(String assignedTo);
    
    List<Asset> findByIsActiveTrue();
    
    @Query("SELECT a FROM Asset a WHERE a.warrantyExpiryDate <= :expiryDate AND a.isActive = true")
    List<Asset> findAssetsWithExpiringWarranty(@Param("expiryDate") LocalDateTime expiryDate);
    
    @Query("SELECT a FROM Asset a WHERE a.installationDate <= :date AND a.isActive = true")
    List<Asset> findAssetsInstalledBefore(@Param("date") LocalDateTime date);
    
    @Query("SELECT a FROM Asset a WHERE a.assetCode LIKE %:searchTerm% OR a.name LIKE %:searchTerm% OR a.description LIKE %:searchTerm%")
    List<Asset> searchAssets(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = :status AND a.isActive = true")
    Long countByStatusAndIsActiveTrue(@Param("status") AssetStatus status);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.assetType = :assetType AND a.isActive = true")
    Long countByAssetTypeAndIsActiveTrue(@Param("assetType") AssetType assetType);
    
    @Query("SELECT a.department, COUNT(a) FROM Asset a WHERE a.isActive = true GROUP BY a.department")
    List<Object[]> countAssetsByDepartment();
    
    @Query("SELECT a.location, COUNT(a) FROM Asset a WHERE a.isActive = true GROUP BY a.location")
    List<Object[]> countAssetsByLocation();
}
