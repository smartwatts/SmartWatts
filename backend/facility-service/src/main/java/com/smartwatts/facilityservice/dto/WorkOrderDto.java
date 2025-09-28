package com.smartwatts.facilityservice.dto;

import com.smartwatts.facilityservice.model.WorkOrderType;
import com.smartwatts.facilityservice.model.WorkOrderPriority;
import com.smartwatts.facilityservice.model.WorkOrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkOrderDto {

    private Long id;

    @NotBlank(message = "Work order title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Work order type is required")
    private WorkOrderType type;

    @NotNull(message = "Priority is required")
    private WorkOrderPriority priority;

    @NotNull(message = "Status is required")
    private WorkOrderStatus status;

    private Long assetId;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @Size(max = 100, message = "Assigned technician cannot exceed 100 characters")
    private String assignedTechnician;

    @Size(max = 100, message = "Requested by cannot exceed 100 characters")
    private String requestedBy;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    @DecimalMin(value = "0.0", message = "Estimated cost cannot be negative")
    private BigDecimal estimatedCost;

    @DecimalMin(value = "0.0", message = "Actual cost cannot be negative")
    private BigDecimal actualCost;

    @Size(max = 1000, message = "Materials used cannot exceed 1000 characters")
    private String materialsUsed;

    @Size(max = 2000, message = "Work performed cannot exceed 2000 characters")
    private String workPerformed;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    private List<String> attachments;

    // Audit fields (read-only)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
}
