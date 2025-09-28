package com.smartwatts.facilityservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.facilityservice.model.Asset;
import com.smartwatts.facilityservice.model.AssetType;
import com.smartwatts.facilityservice.model.AssetStatus;
import com.smartwatts.facilityservice.service.AssetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.smartwatts.facilityservice.config.SecurityConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AssetController.class)
@Import(SecurityConfig.class)
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssetService assetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAssetById() throws Exception {
        // Create test asset
        Asset asset = Asset.builder()
                .id(1L)
                .name("Test Asset")
                .description("Test Description")
                .assetType(AssetType.ELECTRICAL_EQUIPMENT)
                .status(AssetStatus.OPERATIONAL)
                .location("Test Location")
                .building("Test Building")
                .floor("1")
                .room("101")
                .manufacturer("Test Manufacturer")
                .model("Test Model")
                .serialNumber("SN123456")
                .installationDate(LocalDateTime.now())
                .purchaseCost(new BigDecimal("1000.00"))
                .currentValue(new BigDecimal("800.00"))
                .isActive(true)
                .build();

        when(assetService.getAssetById(1L)).thenReturn(Optional.of(asset));

        mockMvc.perform(get("/api/v1/assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Asset"))
                .andExpect(jsonPath("$.assetType").value("ELECTRICAL_EQUIPMENT"))
                .andExpect(jsonPath("$.status").value("OPERATIONAL"));
    }

    @Test
    void testCreateAsset() throws Exception {
        Asset asset = Asset.builder()
                .name("New Asset")
                .description("New Description")
                .assetType(AssetType.CLEANING_EQUIPMENT)
                .status(AssetStatus.OPERATIONAL)
                .location("New Location")
                .isActive(true)
                .build();

        Asset savedAsset = Asset.builder()
                .id(1L)
                .name("New Asset")
                .description("New Description")
                .assetType(AssetType.CLEANING_EQUIPMENT)
                .status(AssetStatus.OPERATIONAL)
                .location("New Location")
                .isActive(true)
                .build();

        when(assetService.createAsset(any(Asset.class))).thenReturn(savedAsset);

        mockMvc.perform(post("/api/v1/assets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Asset"));
    }

    @Test
    void testGetAssetByIdNotFound() throws Exception {
        when(assetService.getAssetById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/assets/999"))
                .andExpect(status().isNotFound());
    }
}
