package com.smartwatts.facilityservice.dto;

import com.smartwatts.facilityservice.model.FleetType;
import com.smartwatts.facilityservice.model.FleetStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetDto {

    private Long id;

    @NotBlank(message = "Vehicle name is required")
    @Size(max = 255, message = "Vehicle name cannot exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Fleet type is required")
    private FleetType type;

    @NotNull(message = "Fleet status is required")
    private FleetStatus status;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate cannot exceed 20 characters")
    private String licensePlate;

    @Size(max = 17, message = "VIN cannot exceed 17 characters")
    private String vin;

    @Size(max = 50, message = "Make cannot exceed 50 characters")
    private String make;

    @Size(max = 50, message = "Model cannot exceed 50 characters")
    private String model;

    @Min(value = 1900, message = "Model year must be 1900 or later")
    private Integer modelYear;

    @Size(max = 30, message = "Color cannot exceed 30 characters")
    private String color;

    @Size(max = 30, message = "Fuel type cannot exceed 30 characters")
    private String fuelType;

    @DecimalMin(value = "0.0", message = "Fuel capacity cannot be negative")
    private BigDecimal fuelCapacity;

    @DecimalMin(value = "0.0", message = "Current fuel level cannot be negative")
    private BigDecimal currentFuelLevel;

    @Min(value = 0, message = "Mileage cannot be negative")
    private Long mileage;

    @Size(max = 100, message = "Assigned driver cannot exceed 100 characters")
    private String assignedDriver;

    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastMaintenanceDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextMaintenanceDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate insuranceExpiryDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationExpiryDate;

    @DecimalMin(value = "0.0", message = "Purchase cost cannot be negative")
    private BigDecimal purchaseCost;

    @DecimalMin(value = "0.0", message = "Current value cannot be negative")
    private BigDecimal currentValue;

    @Size(max = 100, message = "Insurance company cannot exceed 100 characters")
    private String insuranceCompany;

    @Size(max = 50, message = "Insurance policy number cannot exceed 50 characters")
    private String insurancePolicyNumber;

    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    private String notes;

    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    // Audit fields (read-only)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
}
