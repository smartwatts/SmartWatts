package com.smartwatts.edge.model;

import java.time.LocalDateTime;

/**
 * Model representing a facility asset for edge processing
 */
public class FacilityAsset {
    
    private String assetId;
    private String name;
    private String description;
    private String assetType;
    private String status;
    private String location;
    private String building;
    private String floor;
    private String room;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private LocalDateTime installationDate;
    private LocalDateTime lastMaintenanceDate;
    private Integer maintenanceIntervalMonths;
    private Double purchaseCost;
    private Double currentValue;
    private String assignedTo;
    private String department;
    private String notes;
    private String imageUrl;
    private String qrCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public FacilityAsset() {}
    
    // Getters and Setters
    public String getAssetId() {
        return assetId;
    }
    
    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAssetType() {
        return assetType;
    }
    
    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getBuilding() {
        return building;
    }
    
    public void setBuilding(String building) {
        this.building = building;
    }
    
    public String getFloor() {
        return floor;
    }
    
    public void setFloor(String floor) {
        this.floor = floor;
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        this.room = room;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public LocalDateTime getInstallationDate() {
        return installationDate;
    }
    
    public void setInstallationDate(LocalDateTime installationDate) {
        this.installationDate = installationDate;
    }
    
    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }
    
    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }
    
    public Integer getMaintenanceIntervalMonths() {
        return maintenanceIntervalMonths;
    }
    
    public void setMaintenanceIntervalMonths(Integer maintenanceIntervalMonths) {
        this.maintenanceIntervalMonths = maintenanceIntervalMonths;
    }
    
    public Double getPurchaseCost() {
        return purchaseCost;
    }
    
    public void setPurchaseCost(Double purchaseCost) {
        this.purchaseCost = purchaseCost;
    }
    
    public Double getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getQrCode() {
        return qrCode;
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "FacilityAsset{" +
                "assetId='" + assetId + '\'' +
                ", name='" + name + '\'' +
                ", assetType='" + assetType + '\'' +
                ", status='" + status + '\'' +
                ", location='" + location + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
