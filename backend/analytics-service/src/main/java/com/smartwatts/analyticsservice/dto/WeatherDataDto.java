package com.smartwatts.analyticsservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherDataDto {
    
    private UUID id;
    
    @NotBlank(message = "Location name is required")
    private String locationName;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;
    
    @NotNull(message = "Weather date is required")
    private LocalDateTime weatherDate;
    
    @DecimalMin(value = "-100.0", message = "Temperature must be reasonable")
    @DecimalMax(value = "100.0", message = "Temperature must be reasonable")
    private BigDecimal temperatureCelsius;
    
    @DecimalMin(value = "0.0", message = "Humidity must be non-negative")
    @DecimalMax(value = "100.0", message = "Humidity cannot exceed 100%")
    private BigDecimal humidityPercentage;
    
    @DecimalMin(value = "0.0", message = "Pressure must be non-negative")
    private BigDecimal pressureHpa;
    
    @DecimalMin(value = "0.0", message = "Wind speed must be non-negative")
    private BigDecimal windSpeedMs;
    
    @DecimalMin(value = "0.0", message = "Wind direction must be non-negative")
    @DecimalMax(value = "360.0", message = "Wind direction cannot exceed 360 degrees")
    private BigDecimal windDirectionDegrees;
    
    @DecimalMin(value = "0.0", message = "Cloud cover must be non-negative")
    @DecimalMax(value = "100.0", message = "Cloud cover cannot exceed 100%")
    private BigDecimal cloudCoverPercentage;
    
    @DecimalMin(value = "0.0", message = "Solar radiation must be non-negative")
    private BigDecimal solarRadiationWm2;
    
    private String weatherCondition;
    private String weatherDescription;
    private Integer visibilityMeters;
    
    @DecimalMin(value = "0.0", message = "UV index must be non-negative")
    private BigDecimal uvIndex;
    
    private BigDecimal dewPointCelsius;
    private BigDecimal feelsLikeCelsius;
    private BigDecimal precipitationMm;
    private BigDecimal snowMm;
    private String dataSource;
    private Integer forecastHours;
    
    @DecimalMin(value = "0.0", message = "Confidence score must be non-negative")
    @DecimalMax(value = "1.0", message = "Confidence score cannot exceed 1.0")
    private BigDecimal confidenceScore;
    
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 