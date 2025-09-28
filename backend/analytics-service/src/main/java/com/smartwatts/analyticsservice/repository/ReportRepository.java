package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    
    Page<Report> findByUserId(UUID userId, Pageable pageable);
    
    Page<Report> findByUserIdAndReportType(UUID userId, Report.ReportType reportType, Pageable pageable);
    
    Page<Report> findByUserIdAndFormat(UUID userId, Report.Format format, Pageable pageable);
    
    List<Report> findByUserIdAndGeneratedAtBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.isScheduled = true ORDER BY r.nextScheduledAt ASC")
    List<Report> findScheduledReportsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.isArchived = false ORDER BY r.generatedAt DESC")
    List<Report> findActiveReportsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.isArchived = true ORDER BY r.archivedAt DESC")
    List<Report> findArchivedReportsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.downloadCount > 0 ORDER BY r.lastDownloadedAt DESC")
    List<Report> findDownloadedReportsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.isPublic = true ORDER BY r.generatedAt DESC")
    List<Report> findPublicReportsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.generatedAt >= :since ORDER BY r.generatedAt DESC")
    List<Report> findRecentReportsByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.userId = :userId AND r.generatedAt BETWEEN :startDate AND :endDate")
    long countReportsByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.userId = :userId AND r.reportType = :reportType")
    long countReportsByUserIdAndType(@Param("userId") UUID userId, @Param("reportType") Report.ReportType reportType);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.reportName LIKE %:name% ORDER BY r.generatedAt DESC")
    List<Report> findReportsByNamePattern(@Param("userId") UUID userId, @Param("name") String name);
    
    @Query("SELECT r FROM Report r WHERE r.userId = :userId AND r.fileSizeBytes > :minSize ORDER BY r.fileSizeBytes DESC")
    List<Report> findLargeReportsByUserId(@Param("userId") UUID userId, @Param("minSize") Long minSize);
} 