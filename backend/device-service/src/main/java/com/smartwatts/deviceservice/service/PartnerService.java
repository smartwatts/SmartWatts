package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.Partner;
import com.smartwatts.deviceservice.repository.PartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PartnerService {
    
    @Autowired
    private PartnerRepository partnerRepository;
    
    @Autowired
    private QRCodeService qrCodeService;
    
    /**
     * Register a new partner
     * @param partner The partner to register
     * @return The registered partner with QR code URL
     */
    public Partner registerPartner(Partner partner) {
        // Generate unique partner ID if not provided
        if (partner.getPartnerId() == null || partner.getPartnerId().isEmpty()) {
            partner.setPartnerId(generateUniquePartnerId(partner.getPartnerType()));
        }
        
        // Generate QR code URL
        String qrCodeUrl = qrCodeService.generatePartnerQRCodeUrl(partner.getPartnerId());
        partner.setQrCodeUrl(qrCodeUrl);
        
        // Set default values
        partner.setVerified(false);
        partner.setVerificationStatus("PENDING");
        partner.setTotalInstallations(0);
        partner.setTotalCommission(BigDecimal.ZERO);
        partner.setCommissionRate(new BigDecimal("5.0")); // Default 5%
        partner.setLastActivity(LocalDateTime.now());
        
        return partnerRepository.save(partner);
    }
    
    /**
     * Verify a partner
     * @param partnerId The partner ID to verify
     * @return The verified partner
     */
    public Partner verifyPartner(String partnerId) {
        Optional<Partner> partnerOpt = partnerRepository.findByPartnerId(partnerId);
        if (partnerOpt.isPresent()) {
            Partner partner = partnerOpt.get();
            partner.setVerified(true);
            partner.setVerificationStatus("APPROVED");
            partner.setLastActivity(LocalDateTime.now());
            return partnerRepository.save(partner);
        }
        throw new RuntimeException("Partner not found: " + partnerId);
    }
    
    /**
     * Get partner by ID
     * @param partnerId The partner ID
     * @return Optional partner
     */
    public Optional<Partner> getPartnerById(String partnerId) {
        return partnerRepository.findByPartnerId(partnerId);
    }
    
    /**
     * Get all verified partners
     * @return List of verified partners
     */
    public List<Partner> getVerifiedPartners() {
        return partnerRepository.findByIsVerifiedTrue();
    }
    
    /**
     * Get partners by type
     * @param partnerType The partner type
     * @return List of partners of the specified type
     */
    public List<Partner> getPartnersByType(String partnerType) {
        return partnerRepository.findByPartnerType(partnerType);
    }
    
    /**
     * Update partner commission
     * @param partnerId The partner ID
     * @param commissionAmount The commission amount to add
     * @return The updated partner
     */
    public Partner updatePartnerCommission(String partnerId, BigDecimal commissionAmount) {
        Optional<Partner> partnerOpt = partnerRepository.findByPartnerId(partnerId);
        if (partnerOpt.isPresent()) {
            Partner partner = partnerOpt.get();
            partner.setTotalCommission(partner.getTotalCommission().add(commissionAmount));
            partner.setLastActivity(LocalDateTime.now());
            return partnerRepository.save(partner);
        }
        throw new RuntimeException("Partner not found: " + partnerId);
    }
    
    /**
     * Increment partner installations
     * @param partnerId The partner ID
     * @return The updated partner
     */
    public Partner incrementPartnerInstallations(String partnerId) {
        Optional<Partner> partnerOpt = partnerRepository.findByPartnerId(partnerId);
        if (partnerOpt.isPresent()) {
            Partner partner = partnerOpt.get();
            partner.setTotalInstallations(partner.getTotalInstallations() + 1);
            partner.setLastActivity(LocalDateTime.now());
            return partnerRepository.save(partner);
        }
        throw new RuntimeException("Partner not found: " + partnerId);
    }
    
    /**
     * Generate unique partner ID
     * @param partnerType The partner type
     * @return Unique partner ID
     */
    private String generateUniquePartnerId(String partnerType) {
        String prefix = getPartnerTypePrefix(partnerType);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uniqueId;
    }
    
    /**
     * Get partner type prefix
     * @param partnerType The partner type
     * @return Prefix for partner ID
     */
    private String getPartnerTypePrefix(String partnerType) {
        switch (partnerType.toUpperCase()) {
            case "INSTALLER":
                return "INST";
            case "FINANCE_PROVIDER":
                return "FIN";
            case "INSURANCE_PROVIDER":
                return "INS";
            case "SOLAR_COMPANY":
                return "SOL";
            default:
                return "PTN";
        }
    }
    
    /**
     * Get partner statistics
     * @return Partner statistics
     */
    public PartnerStatistics getPartnerStatistics() {
        long totalPartners = partnerRepository.count();
        long verifiedPartners = partnerRepository.countByIsVerifiedTrue();
        long pendingPartners = partnerRepository.countByVerificationStatus("PENDING");
        
        return new PartnerStatistics(totalPartners, verifiedPartners, pendingPartners);
    }
    
    /**
     * Partner statistics class
     */
    public static class PartnerStatistics {
        private final long totalPartners;
        private final long verifiedPartners;
        private final long pendingPartners;
        
        public PartnerStatistics(long totalPartners, long verifiedPartners, long pendingPartners) {
            this.totalPartners = totalPartners;
            this.verifiedPartners = verifiedPartners;
            this.pendingPartners = pendingPartners;
        }
        
        public long getTotalPartners() {
            return totalPartners;
        }
        
        public long getVerifiedPartners() {
            return verifiedPartners;
        }
        
        public long getPendingPartners() {
            return pendingPartners;
        }
    }
} 