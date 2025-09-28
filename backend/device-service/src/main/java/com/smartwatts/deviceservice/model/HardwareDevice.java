package com.smartwatts.deviceservice.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "hardware_devices")
@EntityListeners(AuditingEntityListener.class)
public class HardwareDevice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String macAddress;
    
    @Column(nullable = false)
    private String deviceType; // GATEWAY, PZEM_016, SMART_PLUG, RELAY, CT_CLAMP, VOLTAGE_SENSING_RELAY
    
    @Column(nullable = false)
    private String serialNumber;
    
    @Column(nullable = false)
    private String modelNumber;
    
    @Column(nullable = false)
    private String firmwareVersion;
    
    @Column(nullable = false)
    private boolean isCertified;
    
    @Column(nullable = false)
    private boolean isActivated;
    
    @Column(nullable = false)
    private LocalDateTime activationDate;
    
    @Column(nullable = false)
    private String activationToken;
    
    @Column(nullable = false)
    private boolean isTokenUsed;
    
    @Column(nullable = false)
    private String partnerId; // Installer/Partner who activated this device
    
    @Column(nullable = false)
    private String installerId;
    
    @Column(nullable = false)
    private String siteLocation;
    
    @Column(nullable = false)
    private String organizationId;
    
    @Column(nullable = false)
    private String deviceStatus; // ACTIVE, INACTIVE, MAINTENANCE, REPLACED
    
    @Column(nullable = false)
    private BigDecimal purchasePrice;
    
    @Column(nullable = false)
    private String warrantyExpiry;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public HardwareDevice() {}
    
    public HardwareDevice(String macAddress, String deviceType, String serialNumber, 
                         String modelNumber, String firmwareVersion, String activationToken) {
        this.macAddress = macAddress;
        this.deviceType = deviceType;
        this.serialNumber = serialNumber;
        this.modelNumber = modelNumber;
        this.firmwareVersion = firmwareVersion;
        this.activationToken = activationToken;
        this.isCertified = true; // SmartWatts devices are certified by default
        this.isActivated = false;
        this.isTokenUsed = false;
        this.deviceStatus = "INACTIVE";
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getMacAddress() {
        return macAddress;
    }
    
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    public String getDeviceType() {
        return deviceType;
    }
    
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public String getModelNumber() {
        return modelNumber;
    }
    
    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }
    
    public String getFirmwareVersion() {
        return firmwareVersion;
    }
    
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    
    public boolean isCertified() {
        return isCertified;
    }
    
    public void setCertified(boolean certified) {
        isCertified = certified;
    }
    
    public boolean isActivated() {
        return isActivated;
    }
    
    public void setActivated(boolean activated) {
        isActivated = activated;
    }
    
    public LocalDateTime getActivationDate() {
        return activationDate;
    }
    
    public void setActivationDate(LocalDateTime activationDate) {
        this.activationDate = activationDate;
    }
    
    public String getActivationToken() {
        return activationToken;
    }
    
    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }
    
    public boolean isTokenUsed() {
        return isTokenUsed;
    }
    
    public void setTokenUsed(boolean tokenUsed) {
        isTokenUsed = tokenUsed;
    }
    
    public String getPartnerId() {
        return partnerId;
    }
    
    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }
    
    public String getInstallerId() {
        return installerId;
    }
    
    public void setInstallerId(String installerId) {
        this.installerId = installerId;
    }
    
    public String getSiteLocation() {
        return siteLocation;
    }
    
    public void setSiteLocation(String siteLocation) {
        this.siteLocation = siteLocation;
    }
    
    public String getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    
    public String getDeviceStatus() {
        return deviceStatus;
    }
    
    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
    
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public String getWarrantyExpiry() {
        return warrantyExpiry;
    }
    
    public void setWarrantyExpiry(String warrantyExpiry) {
        this.warrantyExpiry = warrantyExpiry;
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
} 