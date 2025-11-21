package com.smartwatts.billingservice.repository;

import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.model.Bill.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillRepository extends JpaRepository<Bill, UUID> {
    List<Bill> findByUserIdAndBillingPeriodStartBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.userId = :userId AND b.status = :status AND b.billingPeriodStart >= :start AND b.billingPeriodEnd <= :end")
    BigDecimal sumTotalAmountByUserIdAndStatusAndPeriod(@Param("userId") UUID userId, @Param("status") BillStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(b.totalAmount) FROM Bill b WHERE b.userId = :userId AND b.billingPeriodStart >= :start AND b.billingPeriodEnd <= :end")
    BigDecimal getAverageBillAmount(@Param("userId") UUID userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Bill b WHERE b.status = :status AND b.dueDate < :now")
    List<Bill> findOverdueBills(@Param("status") BillStatus status, @Param("now") LocalDateTime now);

    // Additional required methods
    Optional<Bill> findByBillNumber(String billNumber);
    Page<Bill> findByUserId(UUID userId, Pageable pageable);
    Page<Bill> findByUserIdAndStatus(UUID userId, BillStatus status, Pageable pageable);
    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.status = 'PENDING'")
    List<Bill> findPendingBillsByUserId(@Param("userId") UUID userId);
    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.status = 'OVERDUE'")
    List<Bill> findOverdueBillsByUserId(@Param("userId") UUID userId);
    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.status = 'PAID'")
    List<Bill> findPaidBillsByUserId(@Param("userId") UUID userId);
    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.isDisputed = true")
    List<Bill> findDisputedBillsByUserId(@Param("userId") UUID userId);
    @Query("SELECT SUM(b.balanceDue) FROM Bill b WHERE b.userId = :userId AND b.status <> 'PAID'")
    BigDecimal findTotalOutstandingAmountByUserId(@Param("userId") UUID userId);
    @Query("SELECT SUM(b.amountPaid) FROM Bill b WHERE b.userId = :userId AND b.paidDate >= :startDate AND b.paidDate <= :endDate")
    BigDecimal findTotalPaidAmountByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Method for cost forecast and savings tracking
    List<Bill> findByUserIdOrderByBillingPeriodEndDesc(UUID userId);
} 