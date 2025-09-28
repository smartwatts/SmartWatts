package com.smartwatts.facilityservice.service;

import com.smartwatts.facilityservice.model.Asset;
import com.smartwatts.facilityservice.model.AssetStatus;
import com.smartwatts.facilityservice.model.AssetType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssetService {
    
    Asset createAsset(Asset asset);
    
    Asset updateAsset(Long id, Asset asset);
    
    void deleteAsset(Long id);
    
    Optional<Asset> getAssetById(Long id);
    
    Optional<Asset> getAssetByAssetCode(String assetCode);
    
    List<Asset> getAllAssets();
    
    List<Asset> getAssetsByStatus(AssetStatus status);
    
    List<Asset> getAssetsByType(AssetType assetType);
    
    List<Asset> getAssetsByDepartment(String department);
    
    List<Asset> getAssetsByLocation(String location);
    
    List<Asset> getAssetsByBuilding(String building);
    
    List<Asset> getAssetsByAssignedTo(String assignedTo);
    
    List<Asset> searchAssets(String searchTerm);
    
    List<Asset> getAssetsWithExpiringWarranty(LocalDateTime expiryDate);
    
    List<Asset> getAssetsInstalledBefore(LocalDateTime date);
    
    Long countAssetsByStatus(AssetStatus status);
    
    Long countAssetsByType(AssetType assetType);
    
    List<Object[]> getAssetCountByDepartment();
    
    List<Object[]> getAssetCountByLocation();
    
    Asset updateAssetStatus(Long id, AssetStatus status);
    
    Asset assignAssetToUser(Long id, String assignedTo);
    
    Asset updateAssetLocation(Long id, String location, String building, String floor, String room);
    
    boolean isAssetCodeUnique(String assetCode);
    
    String generateAssetCode(AssetType assetType, String department);
}
