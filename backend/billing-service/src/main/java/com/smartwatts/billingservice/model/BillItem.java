package com.smartwatts.billingservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bill_items")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bill_id", nullable = false)
    private UUID billId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_description")
    private String itemDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "quantity", precision = 10, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "unit_price", precision = 10, scale = 4, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "start_reading")
    private BigDecimal startReading;

    @Column(name = "end_reading")
    private BigDecimal endReading;

    @Column(name = "consumption_kwh", precision = 10, scale = 4)
    private BigDecimal consumptionKwh;

    @Column(name = "rate_per_kwh", precision = 8, scale = 4)
    private BigDecimal ratePerKwh;

    @Column(name = "peak_hours")
    private Integer peakHours;

    @Column(name = "off_peak_hours")
    private Integer offPeakHours;

    @Column(name = "night_hours")
    private Integer nightHours;

    @Column(name = "peak_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal peakConsumptionKwh;

    @Column(name = "off_peak_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal offPeakConsumptionKwh;

    @Column(name = "night_consumption_kwh", precision = 10, scale = 4)
    private BigDecimal nightConsumptionKwh;

    @Column(name = "peak_rate", precision = 8, scale = 4)
    private BigDecimal peakRate;

    @Column(name = "off_peak_rate", precision = 8, scale = 4)
    private BigDecimal offPeakRate;

    @Column(name = "night_rate", precision = 8, scale = 4)
    private BigDecimal nightRate;

    @Column(name = "peak_amount", precision = 12, scale = 2)
    private BigDecimal peakAmount;

    @Column(name = "off_peak_amount", precision = 12, scale = 2)
    private BigDecimal offPeakAmount;

    @Column(name = "night_amount", precision = 12, scale = 2)
    private BigDecimal nightAmount;

    @Column(name = "service_charge", precision = 12, scale = 2)
    private BigDecimal serviceCharge;

    @Column(name = "meter_rental", precision = 12, scale = 2)
    private BigDecimal meterRental;

    @Column(name = "demand_charge", precision = 12, scale = 2)
    private BigDecimal demandCharge;

    @Column(name = "fuel_adjustment", precision = 12, scale = 2)
    private BigDecimal fuelAdjustment;

    @Column(name = "capacity_charge", precision = 12, scale = 2)
    private BigDecimal capacityCharge;

    @Column(name = "transmission_charge", precision = 12, scale = 2)
    private BigDecimal transmissionCharge;

    @Column(name = "distribution_charge", precision = 12, scale = 2)
    private BigDecimal distributionCharge;

    @Column(name = "regulatory_charge", precision = 12, scale = 2)
    private BigDecimal regulatoryCharge;

    @Column(name = "environmental_charge", precision = 12, scale = 2)
    private BigDecimal environmentalCharge;

    @Column(name = "other_charges", precision = 12, scale = 2)
    private BigDecimal otherCharges;

    @Column(name = "notes")
    private String notes;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional item data

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ItemType {
        ELECTRICITY_CONSUMPTION,
        PEAK_CONSUMPTION,
        OFF_PEAK_CONSUMPTION,
        NIGHT_CONSUMPTION,
        SERVICE_CHARGE,
        METER_RENTAL,
        DEMAND_CHARGE,
        FUEL_ADJUSTMENT,
        CAPACITY_CHARGE,
        TRANSMISSION_CHARGE,
        DISTRIBUTION_CHARGE,
        REGULATORY_CHARGE,
        ENVIRONMENTAL_CHARGE,
        TAX,
        DISCOUNT,
        OTHER_CHARGE
    }
} 