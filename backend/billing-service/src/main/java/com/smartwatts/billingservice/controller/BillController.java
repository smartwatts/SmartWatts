package com.smartwatts.billingservice.controller;

import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bill Management", description = "APIs for managing energy bills")
public class BillController {
    
    private final BillService billService;
    
    @GetMapping
    @Operation(summary = "Get all bills", description = "Retrieves all bills (for testing)")
    public ResponseEntity<Page<BillDto>> getAllBills(Pageable pageable) {
        log.info("Fetching all bills");
        Page<BillDto> bills = billService.getAllBills(pageable);
        return ResponseEntity.ok(bills);
    }
    
    @PostMapping
    @Operation(summary = "Create a new bill", description = "Creates a new energy bill with calculated amounts")
    public ResponseEntity<BillDto> createBill(@Valid @RequestBody BillDto billDto) {
        log.info("Creating bill for user: {}", billDto.getUserId());
        BillDto createdBill = billService.createBill(billDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
    }
    
    @GetMapping("/{billId}")
    @Operation(summary = "Get bill by ID", description = "Retrieves a specific bill by its ID")
    public ResponseEntity<BillDto> getBillById(
            @Parameter(description = "Bill ID") @PathVariable UUID billId) {
        log.info("Fetching bill with ID: {}", billId);
        BillDto bill = billService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bills by user ID", description = "Retrieves all bills for a specific user")
    public ResponseEntity<Page<BillDto>> getBillsByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching bills for user: {}", userId);
        Page<BillDto> bills = billService.getBillsByUserId(userId, pageable);
        return ResponseEntity.ok(bills);
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get bills by user ID and status", description = "Retrieves bills for a user with specific status")
    public ResponseEntity<Page<BillDto>> getBillsByUserIdAndStatus(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Bill status") @PathVariable Bill.BillStatus status,
            Pageable pageable) {
        log.info("Fetching bills for user: {} with status: {}", userId, status);
        Page<BillDto> bills = billService.getBillsByUserIdAndStatus(userId, status, pageable);
        return ResponseEntity.ok(bills);
    }
    
    @GetMapping("/user/{userId}/period")
    @Operation(summary = "Get bills by user ID and period", description = "Retrieves bills for a user within a specific period")
    public ResponseEntity<List<BillDto>> getBillsByUserIdAndPeriod(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Fetching bills for user: {} between {} and {}", userId, startDate, endDate);
        List<BillDto> bills = billService.getBillsByUserIdAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(bills);
    }
    
    @PutMapping("/{billId}/status")
    @Operation(summary = "Update bill status", description = "Updates the status of a specific bill")
    public ResponseEntity<BillDto> updateBillStatus(
            @Parameter(description = "Bill ID") @PathVariable UUID billId,
            @Parameter(description = "New status") @RequestParam Bill.BillStatus status) {
        log.info("Updating bill status to: {} for bill: {}", status, billId);
        BillDto updatedBill = billService.updateBillStatus(billId, status);
        return ResponseEntity.ok(updatedBill);
    }
    
    @GetMapping("/user/{userId}/total-amount")
    @Operation(summary = "Get total amount by user and period", description = "Calculates total bill amount for a user in a period")
    public ResponseEntity<BigDecimal> getTotalAmountByUserIdAndPeriod(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Calculating total amount for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal totalAmount = billService.getTotalAmountByUserIdAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/user/{userId}/average-amount")
    @Operation(summary = "Get average bill amount by user and period", description = "Calculates average bill amount for a user in a period")
    public ResponseEntity<BigDecimal> getAverageBillAmount(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Calculating average bill amount for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal averageAmount = billService.getAverageBillAmount(userId, startDate, endDate);
        return ResponseEntity.ok(averageAmount);
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue bills", description = "Retrieves all overdue bills")
    public ResponseEntity<List<BillDto>> getOverdueBills() {
        log.info("Fetching overdue bills");
        List<BillDto> overdueBills = billService.getOverdueBills();
        return ResponseEntity.ok(overdueBills);
    }
} 