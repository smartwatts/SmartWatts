package com.smartwatts.energyservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "energy")
@Data
public class Energy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String source; // grid, solar, inverter, generator

    private Double voltage;
    private Double current;
    private Double power;
    private Double energy;
    private String status;
} 