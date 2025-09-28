package com.smartwatts.deviceservice.repository;

import com.smartwatts.deviceservice.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, UUID> {
    
    /**
     * Find partner by partner ID
     * @param partnerId The partner ID
     * @return Optional partner
     */
    Optional<Partner> findByPartnerId(String partnerId);
    
    /**
     * Find all verified partners
     * @return List of verified partners
     */
    List<Partner> findByIsVerifiedTrue();
    
    /**
     * Find partners by type
     * @param partnerType The partner type
     * @return List of partners of the specified type
     */
    List<Partner> findByPartnerType(String partnerType);
    
    /**
     * Find partners by verification status
     * @param verificationStatus The verification status
     * @return List of partners with the specified status
     */
    List<Partner> findByVerificationStatus(String verificationStatus);
    
    /**
     * Count verified partners
     * @return Number of verified partners
     */
    long countByIsVerifiedTrue();
    
    /**
     * Count partners by verification status
     * @param verificationStatus The verification status
     * @return Number of partners with the specified status
     */
    long countByVerificationStatus(String verificationStatus);
    
    /**
     * Find partners by city
     * @param city The city
     * @return List of partners in the specified city
     */
    List<Partner> findByCity(String city);
    
    /**
     * Find partners by state
     * @param state The state
     * @return List of partners in the specified state
     */
    List<Partner> findByState(String state);
    
    /**
     * Find partners with commission rate greater than specified value
     * @param commissionRate The minimum commission rate
     * @return List of partners with commission rate >= specified value
     */
    List<Partner> findByCommissionRateGreaterThanEqual(BigDecimal commissionRate);
    
    /**
     * Find partners with total installations greater than specified value
     * @param installations The minimum number of installations
     * @return List of partners with installations >= specified value
     */
    List<Partner> findByTotalInstallationsGreaterThanEqual(int installations);
    
    /**
     * Find partners by email
     * @param email The email address
     * @return Optional partner
     */
    Optional<Partner> findByEmail(String email);
    
    /**
     * Find partners by phone
     * @param phone The phone number
     * @return Optional partner
     */
    Optional<Partner> findByPhone(String phone);
    
    /**
     * Check if partner exists by partner ID
     * @param partnerId The partner ID
     * @return true if exists, false otherwise
     */
    boolean existsByPartnerId(String partnerId);
    
    /**
     * Check if partner exists by email
     * @param email The email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if partner exists by phone
     * @param phone The phone number
     * @return true if exists, false otherwise
     */
    boolean existsByPhone(String phone);
} 