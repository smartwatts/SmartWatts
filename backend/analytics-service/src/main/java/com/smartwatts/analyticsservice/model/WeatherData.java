package com.smartwatts.analyticsservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weather_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class WeatherData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "location_name", nullable = false)
    private String locationName;
    
    @Column(name = "latitude", precision = 8, scale = 6)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;
    
    @Column(name = "weather_date", nullable = false)
    private LocalDateTime weatherDate;
    
    @Column(name = "temperature_celsius", precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;
    
    @Column(name = "humidity_percentage", precision = 5, scale = 2)
    private BigDecimal humidityPercentage;
    
    @Column(name = "pressure_hpa", precision = 6, scale = 2)
    private BigDecimal pressureHpa;
    
    @Column(name = "wind_speed_ms", precision = 5, scale = 2)
    private BigDecimal windSpeedMs;
    
    @Column(name = "wind_direction_degrees", precision = 5, scale = 2)
    private BigDecimal windDirectionDegrees;
    
    @Column(name = "cloud_cover_percentage", precision = 5, scale = 2)
    private BigDecimal cloudCoverPercentage;
    
    @Column(name = "solar_radiation_wm2", precision = 6, scale = 2)
    private BigDecimal solarRadiationWm2;
    
    @Column(name = "weather_condition")
    private String weatherCondition; // e.g., "clear", "cloudy", "rainy"
    
    @Column(name = "weather_description")
    private String weatherDescription;
    
    @Column(name = "visibility_meters")
    private Integer visibilityMeters;
    
    @Column(name = "uv_index", precision = 4, scale = 2)
    private BigDecimal uvIndex;
    
    @Column(name = "dew_point_celsius", precision = 5, scale = 2)
    private BigDecimal dewPointCelsius;
    
    @Column(name = "feels_like_celsius", precision = 5, scale = 2)
    private BigDecimal feelsLikeCelsius;
    
    @Column(name = "precipitation_mm", precision = 6, scale = 2)
    private BigDecimal precipitationMm;
    
    @Column(name = "snow_mm", precision = 6, scale = 2)
    private BigDecimal snowMm;
    
    @Column(name = "data_source")
    private String dataSource; // e.g., "openweathermap", "local_station"
    
    @Column(name = "forecast_hours")
    private Integer forecastHours; // 0 for current, 1-168 for forecast
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional weather data
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 