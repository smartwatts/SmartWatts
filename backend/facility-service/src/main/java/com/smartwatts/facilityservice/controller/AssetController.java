package com.smartwatts.facilityservice.controller;

import com.smartwatts.facilityservice.model.Asset;
import com.smartwatts.facilityservice.model.AssetStatus;
import com.smartwatts.facilityservice.model.AssetType;
import com.smartwatts.facilityservice.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@Tag(name = "Asset Management", description = "APIs for managing facility assets")
public class AssetController {
    
    private final AssetService assetService;
    
    @PostMapping
    @Operation(summary = "Create a new asset", description = "Creates a new facility asset")
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        Asset createdAsset = assetService.createAsset(asset);
        return ResponseEntity.ok(createdAsset);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get asset by ID", description = "Retrieves an asset by its ID")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        Optional<Asset> asset = assetService.getAssetById(id);
        return asset.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{assetCode}")
    @Operation(summary = "Get asset by asset code", description = "Retrieves an asset by its asset code")
    public ResponseEntity<Asset> getAssetByAssetCode(@PathVariable String assetCode) {
        Optional<Asset> asset = assetService.getAssetByAssetCode(assetCode);
        return asset.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get all assets", description = "Retrieves all active assets")
    public ResponseEntity<List<Asset>> getAllAssets() {
        List<Asset> assets = assetService.getAllAssets();
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get assets by status", description = "Retrieves assets by their status")
    public ResponseEntity<List<Asset>> getAssetsByStatus(@PathVariable AssetStatus status) {
        List<Asset> assets = assetService.getAssetsByStatus(status);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/type/{assetType}")
    @Operation(summary = "Get assets by type", description = "Retrieves assets by their type")
    public ResponseEntity<List<Asset>> getAssetsByType(@PathVariable AssetType assetType) {
        List<Asset> assets = assetService.getAssetsByType(assetType);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/department/{department}")
    @Operation(summary = "Get assets by department", description = "Retrieves assets by their assigned department")
    public ResponseEntity<List<Asset>> getAssetsByDepartment(@PathVariable String department) {
        List<Asset> assets = assetService.getAssetsByDepartment(department);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/location/{location}")
    @Operation(summary = "Get assets by location", description = "Retrieves assets by their location")
    public ResponseEntity<List<Asset>> getAssetsByLocation(@PathVariable String location) {
        List<Asset> assets = assetService.getAssetsByLocation(location);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/building/{building}")
    @Operation(summary = "Get assets by building", description = "Retrieves assets by their building")
    public ResponseEntity<List<Asset>> getAssetsByBuilding(@PathVariable String building) {
        List<Asset> assets = assetService.getAssetsByBuilding(building);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/assigned/{assignedTo}")
    @Operation(summary = "Get assets by assigned user", description = "Retrieves assets assigned to a specific user")
    public ResponseEntity<List<Asset>> getAssetsByAssignedTo(@PathVariable String assignedTo) {
        List<Asset> assets = assetService.getAssetsByAssignedTo(assignedTo);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search assets", description = "Searches assets by text in code, name, or description")
    public ResponseEntity<List<Asset>> searchAssets(@RequestParam String searchTerm) {
        List<Asset> assets = assetService.searchAssets(searchTerm);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/warranty-expiring")
    @Operation(summary = "Get assets with expiring warranty", description = "Retrieves assets with warranties expiring before a specified date")
    public ResponseEntity<List<Asset>> getAssetsWithExpiringWarranty(@RequestParam LocalDateTime expiryDate) {
        List<Asset> assets = assetService.getAssetsWithExpiringWarranty(expiryDate);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/installed-before")
    @Operation(summary = "Get assets installed before date", description = "Retrieves assets installed before a specified date")
    public ResponseEntity<List<Asset>> getAssetsInstalledBefore(@RequestParam LocalDateTime date) {
        List<Asset> assets = assetService.getAssetsInstalledBefore(date);
        return ResponseEntity.ok(assets);
    }
    
    @GetMapping("/count/status/{status}")
    @Operation(summary = "Count assets by status", description = "Returns the count of assets with a specific status")
    public ResponseEntity<Long> countAssetsByStatus(@PathVariable AssetStatus status) {
        Long count = assetService.countAssetsByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count/type/{assetType}")
    @Operation(summary = "Count assets by type", description = "Returns the count of assets of a specific type")
    public ResponseEntity<Long> countAssetsByType(@PathVariable AssetType assetType) {
        Long count = assetService.countAssetsByType(assetType);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count/department")
    @Operation(summary = "Count assets by department", description = "Returns the count of assets grouped by department")
    public ResponseEntity<List<Object[]>> getAssetCountByDepartment() {
        List<Object[]> counts = assetService.getAssetCountByDepartment();
        return ResponseEntity.ok(counts);
    }
    
    @GetMapping("/count/location")
    @Operation(summary = "Count assets by location", description = "Returns the count of assets grouped by location")
    public ResponseEntity<List<Object[]>> getAssetCountByLocation() {
        List<Object[]> counts = assetService.getAssetCountByLocation();
        return ResponseEntity.ok(counts);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update asset", description = "Updates an existing asset")
    public ResponseEntity<Asset> updateAsset(@PathVariable Long id, @RequestBody Asset asset) {
        Asset updatedAsset = assetService.updateAsset(id, asset);
        return ResponseEntity.ok(updatedAsset);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update asset status", description = "Updates the status of an asset")
    public ResponseEntity<Asset> updateAssetStatus(@PathVariable Long id, @RequestParam AssetStatus status) {
        Asset updatedAsset = assetService.updateAssetStatus(id, status);
        return ResponseEntity.ok(updatedAsset);
    }
    
    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign asset to user", description = "Assigns an asset to a specific user")
    public ResponseEntity<Asset> assignAssetToUser(@PathVariable Long id, @RequestParam String assignedTo) {
        Asset updatedAsset = assetService.assignAssetToUser(id, assignedTo);
        return ResponseEntity.ok(updatedAsset);
    }
    
    @PatchMapping("/{id}/location")
    @Operation(summary = "Update asset location", description = "Updates the location details of an asset")
    public ResponseEntity<Asset> updateAssetLocation(
            @PathVariable Long id,
            @RequestParam String location,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String room) {
        Asset updatedAsset = assetService.updateAssetLocation(id, location, building, floor, room);
        return ResponseEntity.ok(updatedAsset);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete asset", description = "Deletes an asset (soft delete)")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/check-code/{assetCode}")
    @Operation(summary = "Check if asset code is unique", description = "Checks if an asset code is available for use")
    public ResponseEntity<Boolean> isAssetCodeUnique(@PathVariable String assetCode) {
        boolean isUnique = assetService.isAssetCodeUnique(assetCode);
        return ResponseEntity.ok(isUnique);
    }
    
    @GetMapping("/generate-code")
    @Operation(summary = "Generate asset code", description = "Generates a unique asset code based on type and department")
    public ResponseEntity<String> generateAssetCode(
            @RequestParam AssetType assetType,
            @RequestParam String department) {
        String assetCode = assetService.generateAssetCode(assetType, department);
        return ResponseEntity.ok(assetCode);
    }
}
