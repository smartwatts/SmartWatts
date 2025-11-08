package com.smartwatts.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.service.BillingService;
import com.smartwatts.billingservice.service.BillGenerationService;
import com.smartwatts.billingservice.service.TariffCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = BillingController.class, excludeAutoConfiguration = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @MockBean
    private BillGenerationService billGenerationService;

    @MockBean
    private TariffCalculationService tariffCalculationService;

    @Autowired
    private ObjectMapper objectMapper;

    private BillDto testBillDto;
    private UUID testBillId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testBillId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        
        testBillDto = BillDto.builder()
                .id(testBillId)
                .userId(testUserId)
                .billNumber("BILL-001")
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .totalConsumptionKwh(new BigDecimal("100.5"))
                .totalAmount(new BigDecimal("25.13"))
                .status(Bill.BillStatus.PENDING)
                .billingPeriodStart(LocalDateTime.now().minusMonths(1))
                .billingPeriodEnd(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createBill_Success_ReturnsCreated() throws Exception {
        // Given
        when(billingService.createBill(any(BillDto.class))).thenReturn(testBillDto);

        // When & Then
        mockMvc.perform(post("/api/v1/billing/bills")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBillDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.billNumber").value("BILL-001"))
                .andExpect(jsonPath("$.totalAmount").value(25.13));

        verify(billingService).createBill(any(BillDto.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getBillById_Success_ReturnsBill() throws Exception {
        // Given
        when(billingService.getBillById(testBillId)).thenReturn(testBillDto);

        // When & Then
        mockMvc.perform(get("/api/v1/billing/bills/{billId}", testBillId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBillId.toString()))
                .andExpect(jsonPath("$.billNumber").value("BILL-001"));

        verify(billingService).getBillById(testBillId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getBillByNumber_Success_ReturnsBill() throws Exception {
        // Given
        when(billingService.getBillByNumber("BILL-001")).thenReturn(testBillDto);

        // When & Then
        mockMvc.perform(get("/api/v1/billing/bills/number/{billNumber}", "BILL-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billNumber").value("BILL-001"));

        verify(billingService).getBillByNumber("BILL-001");
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getBillsByUserId_Success_ReturnsPage() throws Exception {
        // Given
        Page<BillDto> page = new PageImpl<>(Arrays.asList(testBillDto));
        when(billingService.getBillsByUserId(eq(testUserId), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/billing/users/{userId}/bills", testUserId)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(billingService).getBillsByUserId(eq(testUserId), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getPendingBills_Success_ReturnsList() throws Exception {
        // Given
        List<BillDto> bills = Arrays.asList(testBillDto);
        when(billingService.getPendingBills(testUserId)).thenReturn(bills);

        // When & Then
        mockMvc.perform(get("/api/v1/billing/users/{userId}/bills/pending", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(billingService).getPendingBills(testUserId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getOverdueBills_Success_ReturnsList() throws Exception {
        // Given
        List<BillDto> bills = Arrays.asList(testBillDto);
        when(billingService.getOverdueBills(testUserId)).thenReturn(bills);

        // When & Then
        mockMvc.perform(get("/api/v1/billing/users/{userId}/bills/overdue", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(billingService).getOverdueBills(testUserId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getPaidBills_Success_ReturnsList() throws Exception {
        // Given
        List<BillDto> bills = Arrays.asList(testBillDto);
        when(billingService.getPaidBills(testUserId)).thenReturn(bills);

        // When & Then
        mockMvc.perform(get("/api/v1/billing/users/{userId}/bills/paid", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(billingService).getPaidBills(testUserId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void generateBill_Success_ReturnsBill() throws Exception {
        // Given
        when(billingService.generateBillWithTariff(any(UUID.class), any(BigDecimal.class), anyString())).thenReturn(testBillDto);

        // When & Then
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        mockMvc.perform(post("/api/v1/billing/bills/generate")
                .with(csrf())
                .param("userId", testUserId.toString())
                .param("billType", "GRID_ELECTRICITY")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billNumber").value("BILL-001"));

        verify(billingService).generateBillWithTariff(any(UUID.class), any(BigDecimal.class), anyString());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void payBill_Success_ReturnsBill() throws Exception {
        // Given
        testBillDto.setStatus(Bill.BillStatus.PAID);
        when(billingService.markBillAsPaid(eq(testBillId), anyString(), anyString())).thenReturn(testBillDto);

        // When & Then
        mockMvc.perform(post("/api/v1/billing/bills/{billId}/pay", testBillId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"paymentMethod\":\"CARD\",\"amount\":25.13}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        verify(billingService).markBillAsPaid(eq(testBillId), anyString(), anyString());
    }

    // Note: getTariffs endpoint doesn't exist - BillingController has getActiveTariffs() instead
    // Test removed as it doesn't match any actual endpoint
}

