package com.smartwatts.facilityservice.dto;

import com.smartwatts.facilityservice.model.SpaceType;
import com.smartwatts.facilityservice.model.SpaceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceDto {

    private Long id;

    @NotBlank(message = "Space name is required")
    @Size(max = 255, message = "Space name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Space type is required")
    private SpaceType type;

    @NotNull(message = "Space status is required")
    private SpaceStatus status;

    @Size(max = 100, message = "Building cannot exceed 100 characters")
    private String building;

    @Size(max = 50, message = "Floor cannot exceed 50 characters")
    private String floor;

    @Size(max = 50, message = "Wing cannot exceed 50 characters")
    private String wing;

    @Size(max = 50, message = "Room number cannot exceed 50 characters")
    private String roomNumber;

    @DecimalMin(value = "0.0", message = "Area cannot be negative")
    private BigDecimal area;

    @Min(value = 0, message = "Capacity cannot be negative")
    private Integer capacity;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Size(max = 100, message = "Assigned user ID cannot exceed 100 characters")
    private String assignedTo;

    @Size(max = 100, message = "Contact person cannot exceed 100 characters")
    private String contactPerson;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 50, message = "Access level cannot exceed 50 characters")
    private String accessLevel;

    @Size(max = 500, message = "Special requirements cannot exceed 500 characters")
    private String specialRequirements;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    @Size(max = 500, message = "Floor plan URL cannot exceed 500 characters")
    private String floorPlanUrl;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    // Audit fields (read-only)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
}
