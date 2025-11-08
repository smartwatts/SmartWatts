package com.smartwatts.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.userservice.dto.InventoryItemDto;
import com.smartwatts.userservice.dto.InventoryStatsDto;
import com.smartwatts.userservice.model.InventoryItem;
import com.smartwatts.userservice.service.InventoryService;
import com.smartwatts.userservice.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.smartwatts.userservice.config.TestSecurityConfig;
import com.smartwatts.userservice.TestApplication;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = InventoryController.class, 
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class, 
        HibernateJpaAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        SecurityAutoConfiguration.class
    }
)
@ContextConfiguration(classes = {TestApplication.class, TestSecurityConfig.class})
@Import(TestSecurityConfig.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private InventoryItemDto testItemDto;
    private UUID testItemId;

    @BeforeEach
    void setUp() {
        testItemId = UUID.randomUUID();
        
        testItemDto = new InventoryItemDto();
        testItemDto.setId(testItemId);
        testItemDto.setName("Test Item");
        testItemDto.setCategory(InventoryItem.Category.SMART_METERS);
        testItemDto.setStatus(InventoryItem.Status.IN_STOCK);
        testItemDto.setUnitPrice(new BigDecimal("100.00"));
        testItemDto.setCurrentStock(10);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllItems_Success_ReturnsPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryItemDto> page = new PageImpl<>(Arrays.asList(testItemDto), pageable, 1);
        when(inventoryService.getAllItems(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Item"));

        verify(inventoryService).getAllItems(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getItemById_Success_ReturnsItemDto() throws Exception {
        // Given
        when(inventoryService.getItemById(testItemId)).thenReturn(testItemDto);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/{id}", testItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testItemId.toString()))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(inventoryService).getItemById(testItemId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createItem_Success_ReturnsItemDto() throws Exception {
        // Given
        when(inventoryService.createItem(any(InventoryItemDto.class))).thenReturn(testItemDto);

        // When & Then
        mockMvc.perform(post("/api/v1/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(inventoryService).createItem(any(InventoryItemDto.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateItem_Success_ReturnsUpdatedItem() throws Exception {
        // Given
        testItemDto.setName("Updated Item");
        when(inventoryService.updateItem(eq(testItemId), any(InventoryItemDto.class))).thenReturn(testItemDto);

        // When & Then
        mockMvc.perform(put("/api/v1/inventory/{id}", testItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"));

        verify(inventoryService).updateItem(eq(testItemId), any(InventoryItemDto.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void deleteItem_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(inventoryService).deleteItem(testItemId);

        // When & Then
        mockMvc.perform(delete("/api/v1/inventory/{id}", testItemId))
                .andExpect(status().isNoContent());

        verify(inventoryService).deleteItem(testItemId);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void searchItems_Success_ReturnsPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryItemDto> page = new PageImpl<>(Arrays.asList(testItemDto), pageable, 1);
        when(inventoryService.searchItems(anyString(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/search")
                .param("query", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(inventoryService).searchItems(anyString(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void advancedSearch_Success_ReturnsPage() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryItemDto> page = new PageImpl<>(Arrays.asList(testItemDto), pageable, 1);
        when(inventoryService.searchWithFilters(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/search/advanced")
                .param("query", "Test")
                .param("category", InventoryItem.Category.SMART_METERS.name())
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(inventoryService).searchWithFilters(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getInventoryStats_Success_ReturnsStats() throws Exception {
        // Given
        InventoryStatsDto stats = new InventoryStatsDto();
        stats.setTotalItems(100L);
        when(inventoryService.getInventoryStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(100));

        verify(inventoryService).getInventoryStats();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void restockItem_Success_ReturnsUpdatedItem() throws Exception {
        // Given
        testItemDto.setCurrentStock(20);
        when(inventoryService.restockItem(eq(testItemId), anyInt())).thenReturn(testItemDto);

        // When & Then
        mockMvc.perform(post("/api/v1/inventory/{id}/restock", testItemId)
                .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStock").value(20));

        verify(inventoryService).restockItem(eq(testItemId), anyInt());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getLowStockItems_Success_ReturnsList() throws Exception {
        // Given
        List<InventoryItemDto> items = Arrays.asList(testItemDto);
        when(inventoryService.getLowStockItems()).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(inventoryService).getLowStockItems();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getOutOfStockItems_Success_ReturnsList() throws Exception {
        // Given
        List<InventoryItemDto> items = Arrays.asList(testItemDto);
        when(inventoryService.getOutOfStockItems()).thenReturn(items);

        // When & Then
        mockMvc.perform(get("/api/v1/inventory/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(inventoryService).getOutOfStockItems();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void bulkUpdateStatus_Success_ReturnsOk() throws Exception {
        // Given
        doNothing().when(inventoryService).bulkUpdateStatus(anyList(), any(InventoryItem.Status.class));

        // When & Then
        mockMvc.perform(put("/api/v1/inventory/bulk/status")
                .param("itemIds", testItemId.toString())
                .param("status", InventoryItem.Status.IN_STOCK.name()))
                .andExpect(status().isOk());

        verify(inventoryService).bulkUpdateStatus(anyList(), any(InventoryItem.Status.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void bulkDelete_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(inventoryService).bulkDelete(anyList());

        // When & Then
        mockMvc.perform(delete("/api/v1/inventory/bulk")
                .param("itemIds", testItemId.toString()))
                .andExpect(status().isNoContent());

        verify(inventoryService).bulkDelete(anyList());
    }
}

