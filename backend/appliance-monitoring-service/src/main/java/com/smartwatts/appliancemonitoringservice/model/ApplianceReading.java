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
@Table(name = "appliance_readings")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class ApplianceReading {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "appliance_id", nullable = false)
    private UUID applianceId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "real_time_power_watts", precision = 8, scale = 2)
    private BigDecimal realTimePowerWatts;

    @Column(name = "voltage_volts", precision = 6, scale = 2)
    private BigDecimal voltageVolts;

    @Column(name = "current_amps", precision = 6, scale = 4)
    private BigDecimal currentAmps;

    @Column(name = "power_factor", precision = 4, scale = 3)
    private BigDecimal powerFactor;

    @Column(name = "energy_consumption_kwh", precision = 8, scale = 4)
    private BigDecimal energyConsumptionKwh;

    @Column(name = "efficiency_percentage", precision = 5, scale = 2)
    private BigDecimal efficiencyPercentage;

    @Column(name = "temperature_celsius", precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "operating_status")
    private String operatingStatus; // ON, OFF, STANDBY, ERROR

    @Column(name = "anomaly_detected")
    private Boolean anomalyDetected = false;

    @Column(name = "anomaly_type")
    private String anomalyType; // HIGH_CONSUMPTION, LOW_EFFICIENCY, TEMPERATURE_ANOMALY

    @Column(name = "maintenance_alert")
    private Boolean maintenanceAlert = false;

    @Column(name = "maintenance_message")
    private String maintenanceMessage;

    @Column(name = "data_quality_score", precision = 5, scale = 2)
    private BigDecimal dataQualityScore;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
