package com.smartwatts.billingservice.repository;

import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.model.Tariff.CustomerType;
import com.smartwatts.billingservice.model.Tariff.EnergySource;
import com.smartwatts.billingservice.model.Tariff.TariffStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, UUID> {
    
    Optional<Tariff> findByTariffCode(String tariffCode);
    
    Page<Tariff> findByTariffType(Tariff.TariffType tariffType, Pageable pageable);
    
    Page<Tariff> findByCustomerCategory(Tariff.CustomerCategory customerCategory, Pageable pageable);
    
    Page<Tariff> findByIsActive(Boolean isActive, Pageable pageable);
    
    Page<Tariff> findByIsApproved(Boolean isApproved, Pageable pageable);
    
    List<Tariff> findByEffectiveDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffs(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.tariffType = :tariffType AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByType(@Param("tariffType") Tariff.TariffType tariffType, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.customerCategory = :customerCategory AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByCategory(@Param("customerCategory") Tariff.CustomerCategory customerCategory, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.discoCode = :discoCode AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByDisco(@Param("discoCode") String discoCode, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.region = :region AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByRegion(@Param("region") String region, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.state = :state AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByState(@Param("state") String state, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.city = :city AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByCity(@Param("city") String city, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.baseRate >= :minRate AND t.baseRate <= :maxRate AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByRateRange(@Param("minRate") BigDecimal minRate, @Param("maxRate") BigDecimal maxRate, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.tariffName LIKE :name AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate)")
    List<Tariff> findActiveTariffsByNamePattern(@Param("name") String name, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT COUNT(t) FROM Tariff t WHERE t.isActive = true AND t.isApproved = true")
    long countActiveTariffs();
    
    @Query("SELECT COUNT(t) FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.tariffType = :tariffType")
    long countActiveTariffsByType(@Param("tariffType") Tariff.TariffType tariffType);
    
    @Query("SELECT COUNT(t) FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.customerCategory = :customerCategory")
    long countActiveTariffsByCategory(@Param("customerCategory") Tariff.CustomerCategory customerCategory);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate) ORDER BY t.baseRate ASC")
    List<Tariff> findActiveTariffsOrderedByRate(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Tariff t WHERE t.isActive = true AND t.isApproved = true AND t.effectiveDate <= :currentDate AND (t.expiryDate IS NULL OR t.expiryDate >= :currentDate) ORDER BY t.effectiveDate DESC")
    List<Tariff> findActiveTariffsOrderedByDate(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT DISTINCT t.discoCode FROM Tariff t WHERE t.isActive = true AND t.isApproved = true")
    List<String> findDistinctDiscoCodes();
    
    @Query("SELECT DISTINCT t.region FROM Tariff t WHERE t.isActive = true AND t.isApproved = true")
    List<String> findDistinctRegions();
    
    @Query("SELECT DISTINCT t.state FROM Tariff t WHERE t.isActive = true AND t.isApproved = true")
    List<String> findDistinctStates();
    
    @Query("SELECT DISTINCT t.city FROM Tariff t WHERE t.isActive = true AND t.isApproved = true")
    List<String> findDistinctCities();

    Page<Tariff> findByStatus(TariffStatus status, Pageable pageable);

    @Query("SELECT t FROM Tariff t WHERE t.status = :status AND t.effectiveDate <= :now AND (t.expiryDate IS NULL OR t.expiryDate > :now)")
    List<Tariff> findActiveTariffs(@Param("status") TariffStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Tariff t WHERE t.customerCategory = :customerType AND t.status = :status AND t.effectiveDate <= :now AND (t.expiryDate IS NULL OR t.expiryDate > :now) AND t.energySource = :energySource")
    List<Tariff> findActiveTariffsByTypeAndSource(@Param("customerType") CustomerType customerType, @Param("energySource") EnergySource energySource, @Param("status") TariffStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Tariff t WHERE t.status = :status AND t.expiryDate < :now")
    List<Tariff> findExpiredTariffs(@Param("status") TariffStatus status, @Param("now") LocalDateTime now);

    long countByStatus(TariffStatus status);
} 