package com.smartwatts.billingservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.billingservice.dto.TokenDto;
import com.smartwatts.billingservice.model.Token;
import com.smartwatts.billingservice.service.TokenService;
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

@WebMvcTest(TokenController.class)
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createToken_ShouldReturnCreatedToken() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        TokenDto tokenDto = TokenDto.builder()
                .userId(userId)
                .tokenCode("TOKEN123456")
                .meterNumber("METER001")
                .amountPaid(new BigDecimal("1000.00"))
                .unitsPurchased(new BigDecimal("400.0"))
                .ratePerUnit(new BigDecimal("2.50"))
                .build();

        TokenDto createdToken = TokenDto.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenCode("TOKEN123456")
                .meterNumber("METER001")
                .amountPaid(new BigDecimal("1000.00"))
                .unitsPurchased(new BigDecimal("400.0"))
                .unitsConsumed(BigDecimal.ZERO)
                .unitsRemaining(new BigDecimal("400.0"))
                .ratePerUnit(new BigDecimal("2.50"))
                .status(Token.TokenStatus.PENDING)
                .purchaseDate(LocalDateTime.now())
                .build();

        when(tokenService.createToken(any(TokenDto.class))).thenReturn(createdToken);

        // When & Then
        mockMvc.perform(post("/api/v1/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.tokenCode").value("TOKEN123456"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getTokenById_ShouldReturnToken() throws Exception {
        // Given
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        TokenDto token = TokenDto.builder()
                .id(tokenId)
                .userId(userId)
                .tokenCode("TOKEN123456")
                .meterNumber("METER001")
                .amountPaid(new BigDecimal("1000.00"))
                .unitsPurchased(new BigDecimal("400.0"))
                .status(Token.TokenStatus.ACTIVE)
                .build();

        when(tokenService.getTokenById(tokenId)).thenReturn(token);

        // When & Then
        mockMvc.perform(get("/api/v1/tokens/{tokenId}", tokenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tokenId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.tokenCode").value("TOKEN123456"));
    }

    @Test
    void activateToken_ShouldReturnActivatedToken() throws Exception {
        // Given
        UUID tokenId = UUID.randomUUID();
        
        TokenDto activatedToken = TokenDto.builder()
                .id(tokenId)
                .status(Token.TokenStatus.ACTIVE)
                .activationDate(LocalDateTime.now())
                .build();

        when(tokenService.activateToken(tokenId)).thenReturn(activatedToken);

        // When & Then
        mockMvc.perform(put("/api/v1/tokens/{tokenId}/activate", tokenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tokenId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void consumeTokenUnits_ShouldReturnUpdatedToken() throws Exception {
        // Given
        UUID tokenId = UUID.randomUUID();
        BigDecimal unitsToConsume = new BigDecimal("50.0");
        
        TokenDto updatedToken = TokenDto.builder()
                .id(tokenId)
                .unitsConsumed(new BigDecimal("50.0"))
                .unitsRemaining(new BigDecimal("350.0"))
                .status(Token.TokenStatus.ACTIVE)
                .build();

        when(tokenService.consumeTokenUnits(tokenId, unitsToConsume)).thenReturn(updatedToken);

        // When & Then
        mockMvc.perform(put("/api/v1/tokens/{tokenId}/consume", tokenId)
                        .param("unitsToConsume", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tokenId.toString()))
                .andExpect(jsonPath("$.unitsConsumed").value("50.0"))
                .andExpect(jsonPath("$.unitsRemaining").value("350.0"));
    }
} 