package com.smartwatts.billingservice.controller;

import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.dto.BillItemDto;
import com.smartwatts.billingservice.dto.TariffDto;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.service.BillingService;
import com.smartwatts.billingservice.service.BillGenerationService;
import com.smartwatts.billingservice.service.TariffCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    
    private final BillingService billingService;
    private final BillGenerationService billGenerationService;
    private final TariffCalculationService tariffCalculationService; // Used for tariff calculations
    
    /**
     * Check if tariff calculation service is available
     */
    public boolean isTariffServiceAvailable() {
        return tariffCalculationService != null;
    }
    
    // Bill endpoints
    @PostMapping("/bills")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> createBill(@Valid @RequestBody BillDto billDto) {
        log.info("Creating bill for user: {}", billDto.getUserId());
        BillDto createdBill = billingService.createBill(billDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
    }
    
    @GetMapping("/bills/{billId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> getBillById(@PathVariable UUID billId) {
        log.info("Fetching bill with ID: {}", billId);
        BillDto bill = billingService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }
    
    @GetMapping("/bills/number/{billNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> getBillByNumber(@PathVariable String billNumber) {
        log.info("Fetching bill with number: {}", billNumber);
        BillDto bill = billingService.getBillByNumber(billNumber);
        return ResponseEntity.ok(bill);
    }
    
    @GetMapping("/users/{userId}/bills")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<Page<BillDto>> getBillsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching bills for user: {}, page: {}, size: {}", userId, page, size);
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Page<BillDto> bills = billingService.getBillsByUserId(userId, pageable);
        return ResponseEntity.ok(bills);
    }
    
    @GetMapping("/users/{userId}/bills/pending")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<BillDto>> getPendingBills(@PathVariable UUID userId) {
        log.info("Fetching pending bills for user: {}", userId);
        List<BillDto> bills = billingService.getPendingBills(userId);
        return ResponseEntity.ok(bills);
    }
    
    @GetMapping("/users/{userId}/bills/overdue")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<BillDto>> getOverdueBills(@PathVariable UUID userId) {
        log.info("Fetching overdue bills for user: {}", userId);
        List<BillDto> bills = billingService.getOverdueBills(userId);
        return ResponseEntity.ok(bills);
    }
    
    @GetMapping("/users/{userId}/bills/paid")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<BillDto>> getPaidBills(@PathVariable UUID userId) {
        log.info("Fetching paid bills for user: {}", userId);
        List<BillDto> bills = billingService.getPaidBills(userId);
        return ResponseEntity.ok(bills);
    }
    
    @GetMapping("/users/{userId}/bills/disputed")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<BillDto>> getDisputedBills(@PathVariable UUID userId) {
        log.info("Fetching disputed bills for user: {}", userId);
        List<BillDto> bills = billingService.getDisputedBills(userId);
        return ResponseEntity.ok(bills);
    }
    
    @GetMapping("/users/{userId}/outstanding-amount")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BigDecimal> getTotalOutstandingAmount(@PathVariable UUID userId) {
        log.info("Calculating total outstanding amount for user: {}", userId);
        BigDecimal amount = billingService.getTotalOutstandingAmount(userId);
        return ResponseEntity.ok(amount);
    }
    
    @GetMapping("/users/{userId}/paid-amount")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BigDecimal> getTotalPaidAmount(
            @PathVariable UUID userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        log.info("Calculating total paid amount for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal amount = billingService.getTotalPaidAmount(userId, startDate, endDate);
        return ResponseEntity.ok(amount);
    }
    
    @PutMapping("/bills/{billId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> updateBillStatus(
            @PathVariable UUID billId,
            @RequestParam Bill.BillStatus status) {
        log.info("Updating bill status: {} to {}", billId, status);
        BillDto updatedBill = billingService.updateBillStatus(billId, status);
        return ResponseEntity.ok(updatedBill);
    }
    
    @PutMapping("/bills/{billId}/pay")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> markBillAsPaid(
            @PathVariable UUID billId,
            @RequestParam String paymentMethod,
            @RequestParam String paymentReference) {
        log.info("Marking bill as paid: {}", billId);
        BillDto paidBill = billingService.markBillAsPaid(billId, paymentMethod, paymentReference);
        return ResponseEntity.ok(paidBill);
    }
    
    @PutMapping("/bills/{billId}/dispute")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> disputeBill(
            @PathVariable UUID billId,
            @RequestParam String disputeReason) {
        log.info("Disputing bill: {}", billId);
        BillDto disputedBill = billingService.disputeBill(billId, disputeReason);
        return ResponseEntity.ok(disputedBill);
    }
    
    // Bill Item endpoints
    @PostMapping("/bill-items")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillItemDto> createBillItem(@Valid @RequestBody BillItemDto itemDto) {
        log.info("Creating bill item for bill: {}", itemDto.getBillId());
        BillItemDto createdItem = billingService.createBillItem(itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }
    
    @GetMapping("/bill-items/{itemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillItemDto> getBillItemById(@PathVariable UUID itemId) {
        log.info("Fetching bill item with ID: {}", itemId);
        BillItemDto item = billingService.getBillItemById(itemId);
        return ResponseEntity.ok(item);
    }
    
    @GetMapping("/bills/{billId}/items")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<BillItemDto>> getBillItemsByBillId(@PathVariable UUID billId) {
        log.info("Fetching bill items for bill: {}", billId);
        List<BillItemDto> items = billingService.getBillItemsByBillId(billId);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/bills/{billId}/total-amount")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BigDecimal> getBillTotalAmount(@PathVariable UUID billId) {
        log.info("Calculating total amount for bill: {}", billId);
        BigDecimal total = billingService.getBillTotalAmount(billId);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/bills/{billId}/total-tax")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BigDecimal> getBillTotalTax(@PathVariable UUID billId) {
        log.info("Calculating total tax for bill: {}", billId);
        BigDecimal tax = billingService.getBillTotalTax(billId);
        return ResponseEntity.ok(tax);
    }
    
    @GetMapping("/bills/{billId}/total-discount")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BigDecimal> getBillTotalDiscount(@PathVariable UUID billId) {
        log.info("Calculating total discount for bill: {}", billId);
        BigDecimal discount = billingService.getBillTotalDiscount(billId);
        return ResponseEntity.ok(discount);
    }
    
    // Tariff endpoints
    @PostMapping("/tariffs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TARIFF_MANAGER')")
    public ResponseEntity<TariffDto> createTariff(@Valid @RequestBody TariffDto tariffDto) {
        log.info("Creating tariff: {}", tariffDto.getTariffName());
        TariffDto createdTariff = billingService.createTariff(tariffDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTariff);
    }
    
    @GetMapping("/tariffs/{tariffId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<TariffDto> getTariffById(@PathVariable UUID tariffId) {
        log.info("Fetching tariff with ID: {}", tariffId);
        TariffDto tariff = billingService.getTariffById(tariffId);
        return ResponseEntity.ok(tariff);
    }
    
    @GetMapping("/tariffs/code/{tariffCode}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<TariffDto> getTariffByCode(@PathVariable String tariffCode) {
        log.info("Fetching tariff with code: {}", tariffCode);
        TariffDto tariff = billingService.getTariffByCode(tariffCode);
        return ResponseEntity.ok(tariff);
    }
    
    @GetMapping("/tariffs/active")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<TariffDto>> getActiveTariffs() {
        log.info("Fetching active tariffs");
        List<TariffDto> tariffs = billingService.getActiveTariffs();
        return ResponseEntity.ok(tariffs);
    }
    
    @GetMapping("/tariffs/active/type/{tariffType}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<TariffDto>> getActiveTariffsByType(@PathVariable Tariff.TariffType tariffType) {
        log.info("Fetching active tariffs by type: {}", tariffType);
        List<TariffDto> tariffs = billingService.getActiveTariffsByType(tariffType);
        return ResponseEntity.ok(tariffs);
    }
    
    @GetMapping("/tariffs/active/category/{customerCategory}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<TariffDto>> getActiveTariffsByCategory(@PathVariable Tariff.CustomerCategory customerCategory) {
        log.info("Fetching active tariffs by category: {}", customerCategory);
        List<TariffDto> tariffs = billingService.getActiveTariffsByCategory(customerCategory);
        return ResponseEntity.ok(tariffs);
    }
    
    @GetMapping("/tariffs/active/disco/{discoCode}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<List<TariffDto>> getActiveTariffsByDisco(@PathVariable String discoCode) {
        log.info("Fetching active tariffs by disco: {}", discoCode);
        List<TariffDto> tariffs = billingService.getActiveTariffsByDisco(discoCode);
        return ResponseEntity.ok(tariffs);
    }
    
    @PutMapping("/tariffs/{tariffId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TARIFF_MANAGER')")
    public ResponseEntity<TariffDto> approveTariff(
            @PathVariable UUID tariffId,
            @RequestParam UUID approvedBy,
            @RequestParam String approvedByAuthority,
            @RequestParam String approvalReference) {
        log.info("Approving tariff: {}", tariffId);
        TariffDto approvedTariff = billingService.approveTariff(tariffId, approvedBy, approvedByAuthority, approvalReference);
        return ResponseEntity.ok(approvedTariff);
    }
    
    // Bill Generation endpoints
    @PostMapping("/generate/electricity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> generateElectricityBill(
            @RequestParam UUID userId,
            @RequestParam BigDecimal totalConsumptionKwh,
            @RequestParam(defaultValue = "0") BigDecimal peakConsumptionKwh,
            @RequestParam(defaultValue = "0") BigDecimal offPeakConsumptionKwh,
            @RequestParam(defaultValue = "0") BigDecimal nightConsumptionKwh,
            @RequestParam LocalDateTime billingPeriodStart,
            @RequestParam LocalDateTime billingPeriodEnd,
            @RequestParam(required = false) String meterNumber,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerAddress,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) String customerEmail) {
        
        log.info("Generating electricity bill for user: {}", userId);
        BillDto bill = billGenerationService.generateElectricityBill(
                userId, totalConsumptionKwh, peakConsumptionKwh, offPeakConsumptionKwh, nightConsumptionKwh,
                billingPeriodStart, billingPeriodEnd, meterNumber, accountNumber, customerName, customerAddress, customerPhone, customerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
    
    @PostMapping("/generate/solar")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> generateSolarBill(
            @RequestParam UUID userId,
            @RequestParam BigDecimal generationKwh,
            @RequestParam LocalDateTime billingPeriodStart,
            @RequestParam LocalDateTime billingPeriodEnd) {
        
        log.info("Generating solar bill for user: {}", userId);
        BillDto bill = billGenerationService.generateSolarBill(userId, generationKwh, billingPeriodStart, billingPeriodEnd);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
    
    @PostMapping("/generate/generator")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> generateGeneratorBill(
            @RequestParam UUID userId,
            @RequestParam BigDecimal fuelConsumptionLiters,
            @RequestParam BigDecimal fuelPricePerLiter,
            @RequestParam LocalDateTime billingPeriodStart,
            @RequestParam LocalDateTime billingPeriodEnd) {
        
        log.info("Generating generator bill for user: {}", userId);
        BillDto bill = billGenerationService.generateGeneratorBill(userId, fuelConsumptionLiters, fuelPricePerLiter, billingPeriodStart, billingPeriodEnd);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
    
    @PostMapping("/generate/hybrid")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> generateHybridBill(
            @RequestParam UUID userId,
            @RequestParam BigDecimal gridConsumptionKwh,
            @RequestParam BigDecimal solarGenerationKwh,
            @RequestParam BigDecimal netConsumptionKwh,
            @RequestParam LocalDateTime billingPeriodStart,
            @RequestParam LocalDateTime billingPeriodEnd) {
        
        log.info("Generating hybrid bill for user: {}", userId);
        BillDto bill = billGenerationService.generateHybridBill(userId, gridConsumptionKwh, solarGenerationKwh, netConsumptionKwh, billingPeriodStart, billingPeriodEnd);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
    
    @PostMapping("/generate/estimated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<BillDto> generateEstimatedBill(
            @RequestParam UUID userId,
            @RequestParam BigDecimal estimatedConsumptionKwh,
            @RequestParam String estimationReason,
            @RequestParam LocalDateTime billingPeriodStart,
            @RequestParam LocalDateTime billingPeriodEnd) {
        
        log.info("Generating estimated bill for user: {}", userId);
        BillDto bill = billGenerationService.generateEstimatedBill(userId, estimatedConsumptionKwh, estimationReason, billingPeriodStart, billingPeriodEnd);
        return ResponseEntity.status(HttpStatus.CREATED).body(bill);
    }
    
    @PostMapping("/generate/recurring")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BILLING_MANAGER')")
    public ResponseEntity<Void> generateRecurringBills() {
        log.info("Generating recurring bills");
        billGenerationService.generateRecurringBills();
        return ResponseEntity.ok().build();
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Billing Service is running");
    }
} 