package com.smartwatts.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.billingservice.dto.TariffDto;
import com.smartwatts.billingservice.model.Tariff;
import com.smartwatts.billingservice.service.TariffService;
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

@WebMvcTest(TariffController.class)
class TariffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TariffService tariffService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTariff_ShouldReturnCreatedTariff() throws Exception {
        // Given
        TariffDto tariffDto = TariffDto.builder()
                .tariffCode("RES_GRID_2024")
                .tariffName("Residential Grid Tariff 2024")
                .customerCategory(Tariff.CustomerCategory.R1_SMALL_RESIDENTIAL)
                .tariffType(Tariff.TariffType.RESIDENTIAL)
                .baseRate(new BigDecimal("0.25"))
                .serviceCharge(new BigDecimal("0.05"))
                .vatRate(new BigDecimal("0.075"))
                .effectiveDate(LocalDateTime.now())
                .build();

        TariffDto createdTariff = TariffDto.builder()
                .id(UUID.randomUUID())
                .tariffCode("RES_GRID_2024")
                .tariffName("Residential Grid Tariff 2024")
                .customerCategory(Tariff.CustomerCategory.R1_SMALL_RESIDENTIAL)
                .tariffType(Tariff.TariffType.RESIDENTIAL)
                .baseRate(new BigDecimal("0.25"))
                .serviceCharge(new BigDecimal("0.05"))
                .vatRate(new BigDecimal("0.075"))
                .isActive(true)
                .isApproved(false)
                .effectiveDate(LocalDateTime.now())
                .build();

        when(tariffService.createTariff(any(TariffDto.class))).thenReturn(createdTariff);

        // When & Then
        mockMvc.perform(post("/api/v1/tariffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tariffDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tariffCode").value("RES_GRID_2024"))
                .andExpect(jsonPath("$.customerType").value("RESIDENTIAL"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void getTariffById_ShouldReturnTariff() throws Exception {
        // Given
        UUID tariffId = UUID.randomUUID();
        
        TariffDto tariff = TariffDto.builder()
                .id(tariffId)
                .tariffCode("RES_GRID_2024")
                .tariffName("Residential Grid Tariff 2024")
                .customerCategory(Tariff.CustomerCategory.R1_SMALL_RESIDENTIAL)
                .tariffType(Tariff.TariffType.RESIDENTIAL)
                .baseRate(new BigDecimal("0.25"))
                .isActive(true)
                .isApproved(true)
                .build();

        when(tariffService.getTariffById(tariffId)).thenReturn(tariff);

        // When & Then
        mockMvc.perform(get("/api/v1/tariffs/{tariffId}", tariffId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tariffId.toString()))
                .andExpect(jsonPath("$.tariffCode").value("RES_GRID_2024"))
                .andExpect(jsonPath("$.customerType").value("RESIDENTIAL"));
    }

    @Test
    void activateTariff_ShouldReturnActivatedTariff() throws Exception {
        // Given
        UUID tariffId = UUID.randomUUID();
        String approvedBy = "admin@smartwatts.com";
        
        TariffDto activatedTariff = TariffDto.builder()
                .id(tariffId)
                .isActive(true)
                .isApproved(true)
                .approvedBy(UUID.fromString(approvedBy))
                .approvedDate(LocalDateTime.now())
                .build();

        when(tariffService.activateTariff(tariffId, approvedBy)).thenReturn(activatedTariff);

        // When & Then
        mockMvc.perform(put("/api/v1/tariffs/{tariffId}/activate", tariffId)
                        .param("approvedBy", approvedBy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tariffId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void updateTariff_ShouldReturnUpdatedTariff() throws Exception {
        // Given
        UUID tariffId = UUID.randomUUID();
        TariffDto tariffDto = TariffDto.builder()
                .tariffName("Updated Residential Grid Tariff 2024")
                .baseRate(new BigDecimal("0.30"))
                .serviceCharge(new BigDecimal("0.06"))
                .vatRate(new BigDecimal("0.075"))
                .build();

        TariffDto updatedTariff = TariffDto.builder()
                .id(tariffId)
                .tariffName("Updated Residential Grid Tariff 2024")
                .baseRate(new BigDecimal("0.30"))
                .serviceCharge(new BigDecimal("0.06"))
                .vatRate(new BigDecimal("0.075"))
                .isActive(true)
                .isApproved(true)
                .build();

        when(tariffService.updateTariff(tariffId, tariffDto)).thenReturn(updatedTariff);

        // When & Then
        mockMvc.perform(put("/api/v1/tariffs/{tariffId}", tariffId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tariffDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tariffId.toString()))
                .andExpect(jsonPath("$.tariffName").value("Updated Residential Grid Tariff 2024"))
                .andExpect(jsonPath("$.baseRate").value("0.30"));
    }
} 