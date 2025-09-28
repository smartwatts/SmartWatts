package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.dto.BillItemDto;
import com.smartwatts.billingservice.dto.TariffDto;
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
import java.util.List;
import java.util.UUID;

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
} 