package com.smartwatts.appliancemonitoringservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weather_data")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "location_id", nullable = false)
    private String locationId; // City, coordinates, or facility ID

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "temperature_celsius", precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "humidity_percentage", precision = 5, scale = 2)
    private BigDecimal humidityPercentage;

    @Column(name = "solar_irradiance_wm2", precision = 6, scale = 2)
    private BigDecimal solarIrradianceWm2;

    @Column(name = "wind_speed_kmh", precision = 5, scale = 2)
    private BigDecimal windSpeedKmh;

    @Column(name = "wind_direction_degrees", precision = 5, scale = 2)
    private BigDecimal windDirectionDegrees;

    @Column(name = "atmospheric_pressure_hpa", precision = 7, scale = 2)
    private BigDecimal atmosphericPressureHpa;

    @Column(name = "precipitation_mm", precision = 6, scale = 2)
    private BigDecimal precipitationMm;

    @Column(name = "cloud_cover_percentage", precision = 5, scale = 2)
    private BigDecimal cloudCoverPercentage;

    @Column(name = "uv_index", precision = 4, scale = 2)
    private BigDecimal uvIndex;

    @Column(name = "weather_condition")
    private String weatherCondition; // SUNNY, CLOUDY, RAINY, STORMY, etc.

    @Column(name = "energy_impact_score", precision = 5, scale = 2)
    private BigDecimal energyImpactScore; // How much weather affects energy consumption

    @Column(name = "seasonal_adjustment_factor", precision = 4, scale = 3)
    private BigDecimal seasonalAdjustmentFactor;

    @Column(name = "data_source")
    private String dataSource; // OPENWEATHER, ACCUWEATHER, LOCAL_STATION, etc.

    @Column(name = "data_quality_score", precision = 5, scale = 2)
    private BigDecimal dataQualityScore;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
