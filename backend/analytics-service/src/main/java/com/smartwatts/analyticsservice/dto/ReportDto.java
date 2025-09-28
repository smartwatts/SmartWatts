package com.smartwatts.analyticsservice.dto;

import com.smartwatts.analyticsservice.model.Report;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDto {
    
    private UUID id;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Report name is required")
    private String reportName;
    
    private String reportTitle;
    
    @NotNull(message = "Report type is required")
    private Report.ReportType reportType;
    
    private Report.Format format;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    private LocalDateTime generatedAt;
    
    private UUID generatedBy;
    
    private String filePath;
    
    private Long fileSizeBytes;
    
    private Integer downloadCount;
    
    private LocalDateTime lastDownloadedAt;
    
    private Boolean isScheduled;
    
    private String scheduleFrequency;
    
    private LocalDateTime nextScheduledAt;
    
    private String parameters;
    
    private String summary;
    
    private String keyFindings;
    
    private String recommendations;
    
    private Boolean isPublic;
    
    private Boolean isArchived;
    
    private LocalDateTime archivedAt;
    
    private UUID archivedBy;
    
    private String metadata;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
} 