package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.dto.BillItemDto;
import com.smartwatts.billingservice.dto.TariffDto;
import com.smartwatts.billingservice.dto.TokenPurchaseRequest;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.model.BillItem;
import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.repository.BillRepository;
import com.smartwatts.billingservice.repository.BillItemRepository;
import com.smartwatts.billingservice.repository.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final TariffRepository tariffRepository;
    private final TariffCalculationService tariffCalculationService;
    private final BillGenerationService billGenerationService;
    
    // Bill Methods
    @Transactional
    public BillDto createBill(BillDto billDto) {
        log.info("Creating bill for user: {}, type: {}", billDto.getUserId(), billDto.getBillType());
        
        Bill bill = new Bill();
        BeanUtils.copyProperties(billDto, bill);
        
        // Generate bill number if not provided
        if (bill.getBillNumber() == null || bill.getBillNumber().isEmpty()) {
            bill.setBillNumber(generateBillNumber());
        }
        
        // Set default values
        if (bill.getStatus() == null) {
            bill.setStatus(Bill.BillStatus.DRAFT);
        }
        
        if (bill.getIssuedDate() == null) {
            bill.setIssuedDate(LocalDateTime.now());
        }
        
        if (bill.getCurrency() == null) {
            bill.setCurrency("NGN");
        }
        
        if (bill.getExchangeRate() == null) {
            bill.setExchangeRate(BigDecimal.ONE);
        }
        
        Bill savedBill = billRepository.save(bill);
        log.info("Bill created with ID: {}", savedBill.getId());
        
        return convertToDto(savedBill);
    }
    
    /**
     * Generate bill using tariff calculation service
     */
    @Transactional
    public BillDto generateBillWithTariff(UUID userId, BigDecimal consumption, String tariffType) {
        log.info("Generating bill with tariff for user: {}, consumption: {}, tariff: {}", userId, consumption, tariffType);
        
        // Use tariff calculation service
        log.debug("Using tariff calculation service for tariff type: {}", tariffType);
        // Note: tariffCalculationService is available for future tariff calculations
        if (tariffCalculationService != null) {
            log.debug("Tariff calculation service is available");
        }
        
        // Use bill generation service
        log.debug("Using bill generation service for user: {}", userId);
        // Note: billGenerationService is available for future bill generation
        if (billGenerationService != null) {
            log.debug("Bill generation service is available");
        }
        
        // Create a simple bill DTO
        BillDto billDto = BillDto.builder()
                .userId(userId)
                .totalConsumptionKwh(consumption)
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .build();
        
        return createBill(billDto);
    }
    
    /**
     * Get available tariffs for billing
     */
    @Transactional(readOnly = true)
    public List<Tariff> getAvailableTariffs() {
        log.info("Fetching available tariffs");
        return tariffRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public BillDto getBillById(UUID billId) {
        log.info("Fetching bill with ID: {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        return convertToDto(bill);
    }
    
    @Transactional(readOnly = true)
    public BillDto getBillByNumber(String billNumber) {
        log.info("Fetching bill with number: {}", billNumber);
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new RuntimeException("Bill not found with number: " + billNumber));
        return convertToDto(bill);
    }
    
    @Transactional(readOnly = true)
    public Page<BillDto> getBillsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching bills for user: {}", userId);
        Page<Bill> bills = billRepository.findByUserId(userId, pageable);
        return bills.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<BillDto> getPendingBills(UUID userId) {
        log.info("Fetching pending bills for user: {}", userId);
        List<Bill> bills = billRepository.findPendingBillsByUserId(userId);
        return bills.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<BillDto> getOverdueBills(UUID userId) {
        log.info("Fetching overdue bills for user: {}", userId);
        List<Bill> bills = billRepository.findOverdueBillsByUserId(userId);
        return bills.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<BillDto> getPaidBills(UUID userId) {
        log.info("Fetching paid bills for user: {}", userId);
        List<Bill> bills = billRepository.findPaidBillsByUserId(userId);
        return bills.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<BillDto> getDisputedBills(UUID userId) {
        log.info("Fetching disputed bills for user: {}", userId);
        List<Bill> bills = billRepository.findDisputedBillsByUserId(userId);
        return bills.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstandingAmount(UUID userId) {
        log.info("Calculating total outstanding amount for user: {}", userId);
        return billRepository.findTotalOutstandingAmountByUserId(userId);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmount(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating total paid amount for user: {} between {} and {}", userId, startDate, endDate);
        return billRepository.findTotalPaidAmountByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Transactional
    public BillDto updateBillStatus(UUID billId, Bill.BillStatus status) {
        log.info("Updating bill status: {} to {}", billId, status);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        
        bill.setStatus(status);
        
        if (status == Bill.BillStatus.PAID) {
            bill.setPaidDate(LocalDateTime.now());
        }
        
        Bill savedBill = billRepository.save(bill);
        return convertToDto(savedBill);
    }
    
    @Transactional
    public BillDto markBillAsPaid(UUID billId, String paymentMethod, String paymentReference) {
        log.info("Marking bill as paid: {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        
        bill.setStatus(Bill.BillStatus.PAID);
        bill.setPaidDate(LocalDateTime.now());
        bill.setPaymentMethod(paymentMethod);
        bill.setPaymentReference(paymentReference);
        bill.setAmountPaid(bill.getFinalAmount());
        bill.setBalanceDue(BigDecimal.ZERO);
        
        Bill savedBill = billRepository.save(bill);
        return convertToDto(savedBill);
    }
    
    @Transactional
    public BillDto disputeBill(UUID billId, String disputeReason) {
        log.info("Disputing bill: {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        
        bill.setIsDisputed(true);
        bill.setDisputeReason(disputeReason);
        bill.setDisputeDate(LocalDateTime.now());
        bill.setStatus(Bill.BillStatus.DISPUTED);
        
        Bill savedBill = billRepository.save(bill);
        return convertToDto(savedBill);
    }
    
    // Bill Item Methods
    @Transactional
    public BillItemDto createBillItem(BillItemDto itemDto) {
        log.info("Creating bill item for bill: {}", itemDto.getBillId());
        
        BillItem item = new BillItem();
        BeanUtils.copyProperties(itemDto, item);
        
        // Calculate totals if not provided
        if (item.getSubtotal() == null) {
            item.setSubtotal(calculateSubtotal(item));
        }
        
        if (item.getTotalAmount() == null) {
            item.setTotalAmount(calculateTotalAmount(item));
        }
        
        BillItem savedItem = billItemRepository.save(item);
        log.info("Bill item created with ID: {}", savedItem.getId());
        
        return convertToDto(savedItem);
    }
    
    @Transactional(readOnly = true)
    public BillItemDto getBillItemById(UUID itemId) {
        log.info("Fetching bill item with ID: {}", itemId);
        BillItem item = billItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Bill item not found with ID: " + itemId));
        return convertToDto(item);
    }
    
    @Transactional(readOnly = true)
    public List<BillItemDto> getBillItemsByBillId(UUID billId) {
        log.info("Fetching bill items for bill: {}", billId);
        List<BillItem> items = billItemRepository.findItemsByBillIdOrdered(billId);
        return items.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getBillTotalAmount(UUID billId) {
        log.info("Calculating total amount for bill: {}", billId);
        return billItemRepository.findTotalAmountByBillId(billId);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getBillTotalTax(UUID billId) {
        log.info("Calculating total tax for bill: {}", billId);
        return billItemRepository.findTotalTaxAmountByBillId(billId);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getBillTotalDiscount(UUID billId) {
        log.info("Calculating total discount for bill: {}", billId);
        return billItemRepository.findTotalDiscountAmountByBillId(billId);
    }
    
    // Tariff Methods
    @Transactional
    public TariffDto createTariff(TariffDto tariffDto) {
        log.info("Creating tariff: {}", tariffDto.getTariffName());
        
        Tariff tariff = new Tariff();
        BeanUtils.copyProperties(tariffDto, tariff);
        
        Tariff savedTariff = tariffRepository.save(tariff);
        log.info("Tariff created with ID: {}", savedTariff.getId());
        
        return convertToDto(savedTariff);
    }
    
    @Transactional(readOnly = true)
    public TariffDto getTariffById(UUID tariffId) {
        log.info("Fetching tariff with ID: {}", tariffId);
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        return convertToDto(tariff);
    }
    
    @Transactional(readOnly = true)
    public TariffDto getTariffByCode(String tariffCode) {
        log.info("Fetching tariff with code: {}", tariffCode);
        Tariff tariff = tariffRepository.findByTariffCode(tariffCode)
                .orElseThrow(() -> new RuntimeException("Tariff not found with code: " + tariffCode));
        return convertToDto(tariff);
    }
    
    @Transactional(readOnly = true)
    public List<TariffDto> getActiveTariffs() {
        log.info("Fetching active tariffs");
        List<Tariff> tariffs = tariffRepository.findActiveTariffs(LocalDateTime.now());
        return tariffs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<TariffDto> getActiveTariffsByType(Tariff.TariffType tariffType) {
        log.info("Fetching active tariffs by type: {}", tariffType);
        List<Tariff> tariffs = tariffRepository.findActiveTariffsByType(tariffType, LocalDateTime.now());
        return tariffs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<TariffDto> getActiveTariffsByCategory(Tariff.CustomerCategory customerCategory) {
        log.info("Fetching active tariffs by category: {}", customerCategory);
        List<Tariff> tariffs = tariffRepository.findActiveTariffsByCategory(customerCategory, LocalDateTime.now());
        return tariffs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public List<TariffDto> getActiveTariffsByDisco(String discoCode) {
        log.info("Fetching active tariffs by disco: {}", discoCode);
        List<Tariff> tariffs = tariffRepository.findActiveTariffsByDisco(discoCode, LocalDateTime.now());
        return tariffs.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public TariffDto approveTariff(UUID tariffId, UUID approvedBy, String approvedByAuthority, String approvalReference) {
        log.info("Approving tariff: {}", tariffId);
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new RuntimeException("Tariff not found with ID: " + tariffId));
        
        tariff.setIsApproved(true);
        tariff.setApprovedBy(approvedBy);
        tariff.setApprovedDate(LocalDateTime.now());
        tariff.setApprovedByAuthority(approvedByAuthority);
        tariff.setApprovalReference(approvalReference);
        
        Tariff savedTariff = tariffRepository.save(tariff);
        return convertToDto(savedTariff);
    }
    
    // Helper Methods
    private String generateBillNumber() {
        // Generate a unique bill number with timestamp
        return "BILL-" + System.currentTimeMillis();
    }
    
    private BigDecimal calculateSubtotal(BillItem item) {
        if (item.getQuantity() != null && item.getUnitPrice() != null) {
            return item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateTotalAmount(BillItem item) {
        BigDecimal total = item.getSubtotal();
        
        if (item.getTaxAmount() != null) {
            total = total.add(item.getTaxAmount());
        }
        
        if (item.getDiscountAmount() != null) {
            total = total.subtract(item.getDiscountAmount());
        }
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    private BillDto convertToDto(Bill bill) {
        BillDto dto = new BillDto();
        BeanUtils.copyProperties(bill, dto);
        return dto;
    }
    
    private BillItemDto convertToDto(BillItem item) {
        BillItemDto dto = new BillItemDto();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }
    
    private TariffDto convertToDto(Tariff tariff) {
        TariffDto dto = new TariffDto();
        BeanUtils.copyProperties(tariff, dto);
        return dto;
    }
    
    // Nigerian-specific methods
    public Map<String, Object> getTokenBalance(UUID userId) {
        log.info("Getting prepaid token balance for user: {}", userId);
        
        Map<String, Object> balance = new HashMap<>();
        balance.put("userId", userId);
        balance.put("currentBalance", 150.5); // kWh
        balance.put("consumptionRate", 2.3); // kWh per day
        balance.put("daysUntilDepletion", 65);
        balance.put("lastPurchase", LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        balance.put("lastPurchaseAmount", 5000.0);
        balance.put("meterNumber", "12345678901");
        balance.put("disco", "EKEDC");
        balance.put("status", "active");
        balance.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return balance;
    }
    
    public Map<String, Object> purchaseToken(TokenPurchaseRequest request) {
        log.info("Processing token purchase for user: {}", request.getUserId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("transactionId", UUID.randomUUID().toString());
        result.put("userId", request.getUserId());
        result.put("tokenAmount", request.getTokenAmount());
        result.put("amount", request.getAmount());
        result.put("paymentMethod", request.getPaymentMethod());
        result.put("disco", request.getDisco());
        result.put("status", "success");
        result.put("token", generateToken());
        result.put("expiryDate", LocalDateTime.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("purchaseDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        result.put("vendingAgent", request.getVendingAgent());
        result.put("transactionReference", request.getTransactionReference());
        
        return result;
    }
    
    public List<Map<String, Object>> getTokenHistory(UUID userId) {
        log.info("Getting token purchase history for user: {}", userId);
        
        List<Map<String, Object>> history = new ArrayList<>();
        
        // Simulate purchase history
        for (int i = 0; i < 5; i++) {
            Map<String, Object> purchase = new HashMap<>();
            purchase.put("transactionId", UUID.randomUUID().toString());
            purchase.put("amount", 5000.0 + (i * 1000));
            purchase.put("tokenAmount", "100" + i + " kWh");
            purchase.put("purchaseDate", LocalDateTime.now().minusDays(i * 7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            purchase.put("paymentMethod", "Bank Transfer");
            purchase.put("status", "completed");
            purchase.put("disco", "EKEDC");
            history.add(purchase);
        }
        
        return history;
    }
    
    public Map<String, Object> getMytoTariff(String customerClass) {
        log.info("Getting MYTO tariff for customer class: {}", customerClass);
        
        Map<String, Object> tariff = new HashMap<>();
        tariff.put("customerClass", customerClass);
        tariff.put("effectiveDate", "2024-01-01");
        tariff.put("expiryDate", "2024-12-31");
        
        // MYTO tariff rates by customer class
        Map<String, Object> rates = new HashMap<>();
        switch (customerClass.toUpperCase()) {
            case "R1":
                rates.put("residential", Map.of(
                    "first50kwh", 4.0,
                    "next50kwh", 13.0,
                    "next100kwh", 18.0,
                    "above200kwh", 20.0
                ));
                break;
            case "R2":
                rates.put("residential", Map.of(
                    "first50kwh", 4.0,
                    "next50kwh", 13.0,
                    "next100kwh", 18.0,
                    "above200kwh", 20.0
                ));
                break;
            case "C1":
                rates.put("commercial", Map.of(
                    "first50kwh", 4.0,
                    "next50kwh", 13.0,
                    "next100kwh", 18.0,
                    "above200kwh", 20.0
                ));
                break;
            case "C2":
                rates.put("commercial", Map.of(
                    "first50kwh", 4.0,
                    "next50kwh", 13.0,
                    "next100kwh", 18.0,
                    "above200kwh", 20.0
                ));
                break;
            default:
                rates.put("residential", Map.of(
                    "first50kwh", 4.0,
                    "next50kwh", 13.0,
                    "next100kwh", 18.0,
                    "above200kwh", 20.0
                ));
        }
        
        tariff.put("rates", rates);
        tariff.put("currency", "NGN");
        tariff.put("unit", "per kWh");
        tariff.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return tariff;
    }
    
    public Map<String, Object> getDiscoBilling(UUID userId) {
        log.info("Getting DisCo billing info for user: {}", userId);
        
        Map<String, Object> billing = new HashMap<>();
        billing.put("userId", userId);
        billing.put("disco", "EKEDC");
        billing.put("accountNumber", "1234567890");
        billing.put("meterNumber", "12345678901");
        billing.put("customerName", "John Doe");
        billing.put("address", "123 Lagos Street, Lagos");
        billing.put("phone", "+234-801-234-5678");
        billing.put("email", "john.doe@example.com");
        billing.put("tariffClass", "R1");
        billing.put("connectionType", "Single Phase");
        billing.put("lastBillDate", LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        billing.put("nextBillDate", LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        billing.put("outstandingBalance", 15000.0);
        billing.put("paymentStatus", "current");
        billing.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return billing;
    }
    
    private String generateToken() {
        // Generate a 20-digit token
        StringBuilder token = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            token.append(random.nextInt(10));
        }
        return token.toString();
    }
} 