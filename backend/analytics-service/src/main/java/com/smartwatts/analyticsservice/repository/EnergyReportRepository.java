package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.EnergyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergyReportRepository extends JpaRepository<EnergyReport, UUID> {
    
    Page<EnergyReport> findByUserId(UUID userId, Pageable pageable);
    
    Page<EnergyReport> findByUserIdAndReportType(UUID userId, EnergyReport.ReportType reportType, Pageable pageable);
    
    Page<EnergyReport> findByUserIdAndReportPeriod(UUID userId, EnergyReport.ReportPeriod reportPeriod, Pageable pageable);
    
    List<EnergyReport> findByUserIdAndPeriodStartBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT er FROM EnergyReport er WHERE er.userId = :userId AND er.isGenerated = :isGenerated")
    Page<EnergyReport> findByUserIdAndGeneratedStatus(UUID userId, boolean isGenerated, Pageable pageable);
    
    @Query("SELECT er FROM EnergyReport er WHERE er.userId = :userId AND er.efficiencyScore >= :minEfficiency")
    Page<EnergyReport> findByUserIdAndMinEfficiency(UUID userId, BigDecimal minEfficiency, Pageable pageable);
    
    @Query("SELECT er FROM EnergyReport er WHERE er.userId = :userId AND er.savingsPotentialNgn >= :minSavings")
    Page<EnergyReport> findByUserIdAndMinSavings(UUID userId, BigDecimal minSavings, Pageable pageable);
    
    @Query("SELECT er FROM EnergyReport er WHERE er.userId = :userId AND er.periodStart >= :startDate AND er.periodEnd <= :endDate")
    List<EnergyReport> findByUserIdAndPeriodRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT er FROM EnergyReport er WHERE er.userId = :userId AND er.generationDate >= :startDate AND er.generationDate <= :endDate")
    List<EnergyReport> findByUserIdAndGenerationDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(er) FROM EnergyReport er WHERE er.userId = :userId AND er.reportType = :reportType")
    long countByUserIdAndReportType(UUID userId, EnergyReport.ReportType reportType);
    
    @Query("SELECT COUNT(er) FROM EnergyReport er WHERE er.userId = :userId AND er.isGenerated = :isGenerated")
    long countByUserIdAndGeneratedStatus(UUID userId, boolean isGenerated);
    
    @Query("SELECT AVG(er.efficiencyScore) FROM EnergyReport er WHERE er.userId = :userId")
    BigDecimal getAverageEfficiencyScoreByUserId(UUID userId);
    
    @Query("SELECT SUM(er.savingsPotentialNgn) FROM EnergyReport er WHERE er.userId = :userId")
    BigDecimal getTotalSavingsPotentialByUserId(UUID userId);
} 