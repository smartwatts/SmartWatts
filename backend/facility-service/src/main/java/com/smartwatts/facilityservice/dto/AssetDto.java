package com.smartwatts.facilityservice.dto;

import com.smartwatts.facilityservice.model.AssetType;
import com.smartwatts.facilityservice.model.AssetStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDto {

    private Long id;

    @NotBlank(message = "Asset name is required")
    @Size(max = 255, message = "Asset name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Asset type is required")
    private AssetType assetType;

    @NotNull(message = "Asset status is required")
    private AssetStatus status;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @Size(max = 100, message = "Building cannot exceed 100 characters")
    private String building;

    @Size(max = 50, message = "Floor cannot exceed 50 characters")
    private String floor;

    @Size(max = 50, message = "Room cannot exceed 50 characters")
    private String room;

    @Size(max = 100, message = "Manufacturer cannot exceed 100 characters")
    private String manufacturer;

    @Size(max = 100, message = "Model cannot exceed 100 characters")
    private String model;

    @Size(max = 100, message = "Serial number cannot exceed 100 characters")
    private String serialNumber;

    @PastOrPresent(message = "Installation date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate installationDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate warrantyExpiryDate;

    @DecimalMin(value = "0.0", message = "Purchase cost cannot be negative")
    private BigDecimal purchaseCost;

    @DecimalMin(value = "0.0", message = "Current value cannot be negative")
    private BigDecimal currentValue;

    @Size(max = 100, message = "Assigned user ID cannot exceed 100 characters")
    private String assignedTo;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    @Size(max = 100, message = "QR code cannot exceed 100 characters")
    private String qrCode;

    // Audit fields (read-only)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
}
