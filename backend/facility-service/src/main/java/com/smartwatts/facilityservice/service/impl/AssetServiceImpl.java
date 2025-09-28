package com.smartwatts.facilityservice.service.impl;

import com.smartwatts.facilityservice.model.Asset;
import com.smartwatts.facilityservice.model.AssetStatus;
import com.smartwatts.facilityservice.model.AssetType;
import com.smartwatts.facilityservice.repository.AssetRepository;
import com.smartwatts.facilityservice.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class AssetServiceImpl implements AssetService {
    
    private final AssetRepository assetRepository;
    private final AtomicInteger assetCounter = new AtomicInteger(1);
    
    @Autowired
    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }
    
    @Override
    public Asset createAsset(Asset asset) {
        if (asset.getAssetCode() == null || asset.getAssetCode().trim().isEmpty()) {
            asset.setAssetCode(generateAssetCode(asset.getAssetType(), asset.getDepartment()));
        }
        
        if (asset.getStatus() == null) {
            asset.setStatus(AssetStatus.OPERATIONAL);
        }
        
        if (asset.getCreatedBy() == null) {
            asset.setCreatedBy("system");
        }
        
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setIsActive(true);
        
        return assetRepository.save(asset);
    }
    
    @Override
    public Asset updateAsset(Long id, Asset asset) {
        Optional<Asset> existingAssetOpt = assetRepository.findByIdAndIsActiveTrue(id);
        if (existingAssetOpt.isEmpty()) {
            throw new RuntimeException("Asset not found");
        }
        
        Asset existingAsset = existingAssetOpt.get();
        
        // Update fields
        if (asset.getName() != null) {
            existingAsset.setName(asset.getName());
        }
        if (asset.getDescription() != null) {
            existingAsset.setDescription(asset.getDescription());
        }
        if (asset.getAssetType() != null) {
            existingAsset.setAssetType(asset.getAssetType());
        }
        if (asset.getStatus() != null) {
            existingAsset.setStatus(asset.getStatus());
        }
        if (asset.getLocation() != null) {
            existingAsset.setLocation(asset.getLocation());
        }
        if (asset.getBuilding() != null) {
            existingAsset.setBuilding(asset.getBuilding());
        }
        if (asset.getFloor() != null) {
            existingAsset.setFloor(asset.getFloor());
        }
        if (asset.getRoom() != null) {
            existingAsset.setRoom(asset.getRoom());
        }
        if (asset.getManufacturer() != null) {
            existingAsset.setManufacturer(asset.getManufacturer());
        }
        if (asset.getModel() != null) {
            existingAsset.setModel(asset.getModel());
        }
        if (asset.getSerialNumber() != null) {
            existingAsset.setSerialNumber(asset.getSerialNumber());
        }
        if (asset.getInstallationDate() != null) {
            existingAsset.setInstallationDate(asset.getInstallationDate());
        }
        if (asset.getWarrantyExpiryDate() != null) {
            existingAsset.setWarrantyExpiryDate(asset.getWarrantyExpiryDate());
        }
        if (asset.getPurchaseCost() != null) {
            existingAsset.setPurchaseCost(asset.getPurchaseCost());
        }
        if (asset.getCurrentValue() != null) {
            existingAsset.setCurrentValue(asset.getCurrentValue());
        }
        if (asset.getAssignedTo() != null) {
            existingAsset.setAssignedTo(asset.getAssignedTo());
        }
        if (asset.getDepartment() != null) {
            existingAsset.setDepartment(asset.getDepartment());
        }
        if (asset.getNotes() != null) {
            existingAsset.setNotes(asset.getNotes());
        }
        if (asset.getImageUrl() != null) {
            existingAsset.setImageUrl(asset.getImageUrl());
        }
        if (asset.getQrCode() != null) {
            existingAsset.setQrCode(asset.getQrCode());
        }
        if (asset.getUpdatedBy() != null) {
            existingAsset.setUpdatedBy(asset.getUpdatedBy());
        }
        
        existingAsset.setUpdatedAt(LocalDateTime.now());
        
        return assetRepository.save(existingAsset);
    }
    
    @Override
    public void deleteAsset(Long id) {
        Optional<Asset> assetOpt = assetRepository.findByIdAndIsActiveTrue(id);
        if (assetOpt.isEmpty()) {
            throw new RuntimeException("Asset not found");
        }
        
        Asset asset = assetOpt.get();
        asset.setIsActive(false);
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setUpdatedBy("system");
        
        assetRepository.save(asset);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findByIdAndIsActiveTrue(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Asset> getAssetByAssetCode(String assetCode) {
        return assetRepository.findByAssetCodeAndIsActiveTrue(assetCode);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAllAssets() {
        return assetRepository.findByIsActiveTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsByStatus(AssetStatus status) {
        return assetRepository.findByStatusAndIsActiveTrue(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsByType(AssetType assetType) {
        return assetRepository.findByAssetTypeAndIsActiveTrue(assetType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsByDepartment(String department) {
        return assetRepository.findByDepartmentAndIsActiveTrue(department);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsByLocation(String location) {
        return assetRepository.findByLocationAndIsActiveTrue(location);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsByBuilding(String building) {
        return assetRepository.findByBuildingAndIsActiveTrue(building);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsByAssignedTo(String assignedTo) {
        return assetRepository.findByAssignedToAndIsActiveTrue(assignedTo);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> searchAssets(String searchTerm) {
        return assetRepository.searchAssets(searchTerm);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsWithExpiringWarranty(LocalDateTime expiryDate) {
        return assetRepository.findAssetsWithExpiringWarranty(expiryDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Asset> getAssetsInstalledBefore(LocalDateTime date) {
        return assetRepository.findAssetsInstalledBefore(date);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countAssetsByStatus(AssetStatus status) {
        return assetRepository.countByStatusAndIsActiveTrue(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long countAssetsByType(AssetType assetType) {
        return assetRepository.countByAssetTypeAndIsActiveTrue(assetType);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAssetCountByDepartment() {
        return assetRepository.countAssetsByDepartment();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getAssetCountByLocation() {
        return assetRepository.countAssetsByLocation();
    }
    
    @Override
    public Asset updateAssetStatus(Long id, AssetStatus status) {
        Optional<Asset> assetOpt = assetRepository.findByIdAndIsActiveTrue(id);
        if (assetOpt.isEmpty()) {
            throw new RuntimeException("Asset not found");
        }
        
        Asset asset = assetOpt.get();
        asset.setStatus(status);
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setUpdatedBy("system");
        
        return assetRepository.save(asset);
    }
    
    @Override
    public Asset assignAssetToUser(Long id, String assignedTo) {
        Optional<Asset> assetOpt = assetRepository.findByIdAndIsActiveTrue(id);
        if (assetOpt.isEmpty()) {
            throw new RuntimeException("Asset not found");
        }
        
        Asset asset = assetOpt.get();
        asset.setAssignedTo(assignedTo);
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setUpdatedBy("system");
        
        return assetRepository.save(asset);
    }
    
    @Override
    public Asset updateAssetLocation(Long id, String location, String building, String floor, String room) {
        Optional<Asset> assetOpt = assetRepository.findByIdAndIsActiveTrue(id);
        if (assetOpt.isEmpty()) {
            throw new RuntimeException("Asset not found");
        }
        
        Asset asset = assetOpt.get();
        asset.setLocation(location);
        asset.setBuilding(building);
        asset.setFloor(floor);
        asset.setRoom(room);
        asset.setUpdatedAt(LocalDateTime.now());
        asset.setUpdatedBy("system");
        
        return assetRepository.save(asset);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isAssetCodeUnique(String assetCode) {
        return assetRepository.findByAssetCodeAndIsActiveTrue(assetCode).isEmpty();
    }
    
    @Override
    public String generateAssetCode(AssetType assetType, String department) {
        String prefix = assetType.name().substring(0, 3).toUpperCase();
        String deptPrefix = department != null ? department.substring(0, Math.min(3, department.length())).toUpperCase() : "GEN";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%04d", assetCounter.getAndIncrement());
        
        return String.format("%s-%s-%s-%s", prefix, deptPrefix, timestamp, sequence);
    }
}
