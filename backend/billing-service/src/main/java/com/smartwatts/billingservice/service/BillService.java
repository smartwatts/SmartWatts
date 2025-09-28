package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.repository.BillRepository;
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
public class BillService {
    
    private final BillRepository billRepository;
    private final TariffRepository tariffRepository; // Used for tariff operations
    
    /**
     * Check if tariff repository is available
     */
    public boolean isTariffRepositoryAvailable() {
        return tariffRepository != null;
    }
    
    @Transactional(readOnly = true)
    public Page<BillDto> getAllBills(Pageable pageable) {
        log.info("Fetching all bills");
        Page<Bill> bills = billRepository.findAll(pageable);
        return bills.map(this::convertToDto);
    }
    
    @Transactional
    public BillDto createBill(BillDto billDto) {
        log.info("Creating bill for user: {}", billDto.getUserId());
        
        Bill bill = new Bill();
        BeanUtils.copyProperties(billDto, bill);
        
        // Calculate bill amounts
        calculateBillAmounts(bill);
        
        // Set default values
        if (bill.getStatus() == null) {
            bill.setStatus(Bill.BillStatus.PENDING);
        }
        if (bill.getCreatedAt() == null) {
            bill.setCreatedAt(LocalDateTime.now());
        }
        bill.setUpdatedAt(LocalDateTime.now());
        
        Bill savedBill = billRepository.save(bill);
        log.info("Bill created with ID: {}", savedBill.getId());
        
        return convertToDto(savedBill);
    }
    
    @Transactional(readOnly = true)
    public BillDto getBillById(UUID billId) {
        log.info("Fetching bill with ID: {}", billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        return convertToDto(bill);
    }
    
    @Transactional(readOnly = true)
    public Page<BillDto> getBillsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching bills for user: {}", userId);
        Page<Bill> bills = billRepository.findByUserId(userId, pageable);
        return bills.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public Page<BillDto> getBillsByUserIdAndStatus(UUID userId, Bill.BillStatus status, Pageable pageable) {
        log.info("Fetching bills for user: {} with status: {}", userId, status);
        Page<Bill> bills = billRepository.findByUserIdAndStatus(userId, status, pageable);
        return bills.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<BillDto> getBillsByUserIdAndPeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching bills for user: {} between {} and {}", userId, startDate, endDate);
        List<Bill> bills = billRepository.findByUserIdAndBillingPeriodStartBetween(userId, startDate, endDate);
        return bills.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public BillDto updateBillStatus(UUID billId, Bill.BillStatus status) {
        log.info("Updating bill status to: {} for bill: {}", status, billId);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found with ID: " + billId));
        
        bill.setStatus(status);
        bill.setUpdatedAt(LocalDateTime.now());
        
        if (status == Bill.BillStatus.PAID) {
            bill.setPaidDate(LocalDateTime.now());
        }
        
        Bill savedBill = billRepository.save(bill);
        return convertToDto(savedBill);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByUserIdAndPeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating total amount for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal total = billRepository.sumTotalAmountByUserIdAndStatusAndPeriod(userId, Bill.BillStatus.PAID, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAverageBillAmount(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating average bill amount for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal average = billRepository.getAverageBillAmount(userId, startDate, endDate);
        return average != null ? average : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public List<BillDto> getOverdueBills() {
        log.info("Fetching overdue bills");
        List<Bill> overdueBills = billRepository.findOverdueBills(Bill.BillStatus.PENDING, LocalDateTime.now());
        return overdueBills.stream().map(this::convertToDto).toList();
    }
    
    private void calculateBillAmounts(Bill bill) {
        // Base amount = consumption * rate
        bill.setBaseAmount(bill.getConsumptionKwh().multiply(bill.getRatePerKwh()).setScale(2, RoundingMode.HALF_UP));
        
        // Service charge = base amount * service charge rate (from tariff)
        BigDecimal serviceChargeRate = getServiceChargeRate(bill.getEnergySource());
        bill.setServiceCharge(bill.getBaseAmount().multiply(serviceChargeRate).setScale(2, RoundingMode.HALF_UP));
        
        // VAT = (base amount + service charge) * VAT rate
        BigDecimal vatRate = getVatRate();
        BigDecimal subtotal = bill.getBaseAmount().add(bill.getServiceCharge());
        bill.setVatAmount(subtotal.multiply(vatRate).setScale(2, RoundingMode.HALF_UP));
        
        // Total = base + service charge + VAT
        bill.setTotalAmount(bill.getBaseAmount().add(bill.getServiceCharge()).add(bill.getVatAmount()));
    }
    
    private BigDecimal getServiceChargeRate(Bill.EnergySource energySource) {
        // This would typically be fetched from tariff configuration
        // For now, using a default rate
        return new BigDecimal("0.05"); // 5% service charge
    }
    
    private BigDecimal getVatRate() {
        // This would typically be fetched from tariff configuration
        // For now, using a default rate
        return new BigDecimal("0.075"); // 7.5% VAT
    }
    
    private BillDto convertToDto(Bill bill) {
        BillDto dto = new BillDto();
        BeanUtils.copyProperties(bill, dto);
        return dto;
    }
} 