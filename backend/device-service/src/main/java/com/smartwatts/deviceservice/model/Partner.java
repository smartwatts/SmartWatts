package com.smartwatts.deviceservice.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "partners")
@EntityListeners(AuditingEntityListener.class)
public class Partner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String partnerId; // Unique partner identifier like SOLARTECH001
    
    @Column(nullable = false)
    private String partnerName;
    
    @Column(nullable = false)
    private String partnerType; // INSTALLER, FINANCE_PROVIDER, INSURANCE_PROVIDER, SOLAR_COMPANY
    
    @Column(nullable = false)
    private String contactPerson;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false)
    private String address;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String state;
    
    @Column(nullable = false)
    private String country;
    
    @Column(nullable = false)
    private String businessLicense;
    
    @Column(nullable = false)
    private boolean isVerified;
    
    @Column(nullable = false)
    private String verificationStatus; // PENDING, APPROVED, REJECTED, SUSPENDED
    
    @Column(nullable = false)
    private String qrCodeUrl;
    
    @Column(nullable = false)
    private int totalInstallations;
    
    @Column(nullable = false)
    private BigDecimal totalCommission;
    
    @Column(nullable = false)
    private BigDecimal commissionRate; // Percentage commission rate
    
    @Column(nullable = false)
    private String bankAccount;
    
    @Column(nullable = false)
    private String bankName;
    
    @Column(nullable = false)
    private String accountName;
    
    @Column(nullable = false)
    private String taxId;
    
    @Column(nullable = false)
    private String ndprConsent; // NDPR compliance consent
    
    @Column(nullable = false)
    private LocalDateTime lastActivity;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Partner() {}
    
    public Partner(String partnerId, String partnerName, String partnerType, 
                   String contactPerson, String email, String phone, String address,
                   String city, String state, String country, String businessLicense) {
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.partnerType = partnerType;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.businessLicense = businessLicense;
        this.isVerified = false;
        this.verificationStatus = "PENDING";
        this.totalInstallations = 0;
        this.totalCommission = BigDecimal.ZERO;
        this.commissionRate = new BigDecimal("5.0"); // Default 5% commission
        this.lastActivity = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getPartnerId() {
        return partnerId;
    }
    
    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }
    
    public String getPartnerName() {
        return partnerName;
    }
    
    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }
    
    public String getPartnerType() {
        return partnerType;
    }
    
    public void setPartnerType(String partnerType) {
        this.partnerType = partnerType;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getBusinessLicense() {
        return businessLicense;
    }
    
    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public String getVerificationStatus() {
        return verificationStatus;
    }
    
    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
    
    public String getQrCodeUrl() {
        return qrCodeUrl;
    }
    
    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
    
    public int getTotalInstallations() {
        return totalInstallations;
    }
    
    public void setTotalInstallations(int totalInstallations) {
        this.totalInstallations = totalInstallations;
    }
    
    public BigDecimal getTotalCommission() {
        return totalCommission;
    }
    
    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }
    
    public BigDecimal getCommissionRate() {
        return commissionRate;
    }
    
    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
    
    public String getBankAccount() {
        return bankAccount;
    }
    
    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public String getTaxId() {
        return taxId;
    }
    
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }
    
    public String getNdprConsent() {
        return ndprConsent;
    }
    
    public void setNdprConsent(String ndprConsent) {
        this.ndprConsent = ndprConsent;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
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