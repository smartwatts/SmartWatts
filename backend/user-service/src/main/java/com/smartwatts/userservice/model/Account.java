package com.smartwatts.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;
    
    @Column(nullable = false)
    private String contactPerson;
    
    @Column(nullable = false)
    private String email;
    
    private String phone;
    private String address;
    private String city;
    private String state;
    private String subscriptionPlan;
    private Double monthlyRevenue;
    private LocalDate lastPayment;
    private LocalDate nextBilling;
    private Integer devices;
    private Double energySavings;
    
    @CreationTimestamp
    private LocalDate createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum AccountType {
        ENTERPRISE, SME, RESIDENTIAL
    }
    
    public enum AccountStatus {
        ACTIVE, SUSPENDED, PENDING, CANCELLED
    }
}

