package com.smartwatts.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.billingservice.dto.BillDto;
import com.smartwatts.billingservice.model.Bill;
import com.smartwatts.billingservice.service.BillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillController.class)
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillService billService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBill_ShouldReturnCreatedBill() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        BillDto billDto = BillDto.builder()
                .userId(userId)
                .billNumber("BILL-001")
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .billingPeriodStart(LocalDateTime.now())
                .billingPeriodEnd(LocalDateTime.now().plusMonths(1))
                .dueDate(LocalDateTime.now().plusDays(30))
                .totalConsumptionKwh(new BigDecimal("100.5"))
                .totalAmount(new BigDecimal("25.13"))
                .build();

        BillDto createdBill = BillDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .billNumber("BILL-001")
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .status(Bill.BillStatus.PENDING)
                .billingPeriodStart(LocalDateTime.now())
                .billingPeriodEnd(LocalDateTime.now().plusMonths(1))
                .dueDate(LocalDateTime.now().plusDays(30))
                .totalConsumptionKwh(new BigDecimal("100.5"))
                .totalAmount(new BigDecimal("25.13"))
                .finalAmount(new BigDecimal("25.13"))
                .build();

        when(billService.createBill(any(BillDto.class))).thenReturn(createdBill);

        // When & Then
        mockMvc.perform(post("/api/v1/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.billType").value("GRID_ELECTRICITY"))
                .andExpect(jsonPath("$.totalAmount").value("25.13"));
    }

    @Test
    void getBillById_ShouldReturnBill() throws Exception {
        // Given
        UUID billId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        BillDto bill = BillDto.builder()
                .id(billId)
                .userId(userId)
                .billNumber("BILL-001")
                .billType(Bill.BillType.GRID_ELECTRICITY)
                .totalConsumptionKwh(new BigDecimal("100.5"))
                .totalAmount(new BigDecimal("25.13"))
                .status(Bill.BillStatus.PENDING)
                .build();

        when(billService.getBillById(billId)).thenReturn(bill);

        // When & Then
        mockMvc.perform(get("/api/v1/bills/{billId}", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(billId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.billType").value("GRID_ELECTRICITY"));
    }

    @Test
    void updateBillStatus_ShouldReturnUpdatedBill() throws Exception {
        // Given
        UUID billId = UUID.randomUUID();
        Bill.BillStatus newStatus = Bill.BillStatus.PAID;
        
        BillDto updatedBill = BillDto.builder()
                .id(billId)
                .status(newStatus)
                .paidDate(LocalDateTime.now())
                .build();

        when(billService.updateBillStatus(billId, newStatus)).thenReturn(updatedBill);

        // When & Then
        mockMvc.perform(put("/api/v1/bills/{billId}/status", billId)
                        .param("status", newStatus.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(billId.toString()))
                .andExpect(jsonPath("$.status").value("PAID"));
    }
} 