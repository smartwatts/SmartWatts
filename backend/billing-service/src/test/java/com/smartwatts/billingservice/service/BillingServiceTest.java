package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.repository.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillingService billingService;

    private Bill testBill;
    private BillDto testBillDto;
    private UUID testBillId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testBillId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testBill = new Bill();
        testBill.setId(testBillId);
        testBill.setUserId(testUserId);
        testBill.setBillNumber("BILL-001");
        testBill.setBillType(Bill.BillType.GRID_ELECTRICITY);
        testBill.setTotalConsumptionKwh(new BigDecimal("100.5"));
        testBill.setTotalAmount(new BigDecimal("25.13"));
        testBill.setStatus(Bill.BillStatus.PENDING);
        testBill.setBillingPeriodStart(LocalDateTime.now().minusMonths(1));
        testBill.setBillingPeriodEnd(LocalDateTime.now());
        testBill.setDueDate(LocalDateTime.now().plusDays(30));
        
        testBillDto = BillDto.builder()
                .id(testBillId)
                .userId(testUserId)
                .billNumber("BILL-001")
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .totalConsumptionKwh(new BigDecimal("100.5"))
                .totalAmount(new BigDecimal("25.13"))
                .status(Bill.BillStatus.PENDING)
                .build();
    }

    @Test
    void createBill_Success_ReturnsBillDto() {
        // Given
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);

        // When
        BillDto result = billingService.createBill(testBillDto);

        // Then
        assertNotNull(result);
        assertEquals(testBillId, result.getId());
        assertEquals("BILL-001", result.getBillNumber());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void getBillById_Success_ReturnsBillDto() {
        // Given
        when(billRepository.findById(testBillId)).thenReturn(Optional.of(testBill));

        // When
        BillDto result = billingService.getBillById(testBillId);

        // Then
        assertNotNull(result);
        assertEquals(testBillId, result.getId());
        verify(billRepository).findById(testBillId);
    }

    @Test
    void getBillById_NotFound_ThrowsException() {
        // Given
        when(billRepository.findById(testBillId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> billingService.getBillById(testBillId));
    }

    @Test
    void getBillByNumber_Success_ReturnsBillDto() {
        // Given
        when(billRepository.findByBillNumber("BILL-001")).thenReturn(Optional.of(testBill));

        // When
        BillDto result = billingService.getBillByNumber("BILL-001");

        // Then
        assertNotNull(result);
        assertEquals("BILL-001", result.getBillNumber());
        verify(billRepository).findByBillNumber("BILL-001");
    }

    @Test
    void getBillsByUserId_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Bill> page = new PageImpl<>(Arrays.asList(testBill));
        when(billRepository.findByUserId(testUserId, pageable)).thenReturn(page);

        // When
        Page<BillDto> result = billingService.getBillsByUserId(testUserId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(billRepository).findByUserId(testUserId, pageable);
    }

    @Test
    void getPendingBills_Success_ReturnsList() {
        // Given
        List<Bill> bills = Arrays.asList(testBill);
        when(billRepository.findPendingBillsByUserId(testUserId))
                .thenReturn(bills);

        // When
        List<BillDto> result = billingService.getPendingBills(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(billRepository).findPendingBillsByUserId(testUserId);
    }

    @Test
    void getOverdueBills_Success_ReturnsList() {
        // Given
        List<Bill> bills = Arrays.asList(testBill);
        when(billRepository.findOverdueBillsByUserId(testUserId))
                .thenReturn(bills);

        // When
        List<BillDto> result = billingService.getOverdueBills(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(billRepository).findOverdueBillsByUserId(testUserId);
    }

    @Test
    void getPaidBills_Success_ReturnsList() {
        // Given
        testBill.setStatus(Bill.BillStatus.PAID);
        List<Bill> bills = Arrays.asList(testBill);
        when(billRepository.findPaidBillsByUserId(testUserId))
                .thenReturn(bills);

        // When
        List<BillDto> result = billingService.getPaidBills(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(billRepository).findPaidBillsByUserId(testUserId);
    }

    @Test
    void payBill_Success_ReturnsBillDto() {
        // Given
        when(billRepository.findById(testBillId)).thenReturn(Optional.of(testBill));
        testBill.setStatus(Bill.BillStatus.PAID);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        testBillDto.setStatus(Bill.BillStatus.PAID);
        when(billingService.markBillAsPaid(testBillId, "CARD", "REF123")).thenReturn(testBillDto);

        // When
        BillDto result = billingService.markBillAsPaid(testBillId, "CARD", "REF123");

        // Then
        assertNotNull(result);
        assertEquals(Bill.BillStatus.PAID, result.getStatus());
        verify(billingService).markBillAsPaid(testBillId, "CARD", "REF123");
    }
}

