package com.smartwatts.userservice.service;

import com.smartwatts.userservice.dto.InventoryItemDto;
import com.smartwatts.userservice.dto.InventoryStatsDto;
import com.smartwatts.userservice.model.InventoryItem;
import com.smartwatts.userservice.repository.InventoryItemRepository;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem testItem;
    private InventoryItemDto testItemDto;
    private UUID testItemId;

    @BeforeEach
    void setUp() {
        testItemId = UUID.randomUUID();

        testItem = new InventoryItem();
        testItem.setId(testItemId);
        testItem.setName("Test Item");
        testItem.setCategory(InventoryItem.Category.SMART_METERS);
        testItem.setSku("SKU-001");
        testItem.setCurrentStock(10);
        testItem.setMinStock(5);
        testItem.setMaxStock(100);
        testItem.setUnitPrice(BigDecimal.valueOf(100.00));
        testItem.setTotalValue(BigDecimal.valueOf(1000.00));
        testItem.setStatus(InventoryItem.Status.IN_STOCK);
        testItem.setSupplier("Test Supplier");
        testItem.setLastRestocked(LocalDate.now());

        testItemDto = new InventoryItemDto();
        testItemDto.setId(testItemId);
        testItemDto.setName("Test Item");
        testItemDto.setCategory(InventoryItem.Category.SMART_METERS);
        testItemDto.setSku("SKU-001");
        testItemDto.setCurrentStock(10);
        testItemDto.setMinStock(5);
        testItemDto.setMaxStock(100);
        testItemDto.setUnitPrice(BigDecimal.valueOf(100.00));
        testItemDto.setTotalValue(BigDecimal.valueOf(1000.00));
        testItemDto.setStatus(InventoryItem.Status.IN_STOCK);
        testItemDto.setSupplier("Test Supplier");
        testItemDto.setLastRestocked(LocalDate.now());
    }

    @Test
    void getAllItems_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryItem> itemPage = new PageImpl<>(Arrays.asList(testItem), pageable, 1);
        when(inventoryItemRepository.findAll(any(Pageable.class))).thenReturn(itemPage);

        // When
        Page<InventoryItemDto> result = inventoryService.getAllItems(0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(inventoryItemRepository).findAll(any(Pageable.class));
    }

    @Test
    void getItemById_Success_ReturnsItemDto() {
        // Given
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

        // When
        InventoryItemDto result = inventoryService.getItemById(testItemId);

        // Then
        assertNotNull(result);
        assertEquals(testItemId, result.getId());
        assertEquals("Test Item", result.getName());
        verify(inventoryItemRepository).findById(testItemId);
    }

    @Test
    void getItemById_NotFound_ThrowsException() {
        // Given
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.getItemById(testItemId);
        });
    }

    @Test
    void createItem_Success_CreatesItem() {
        // Given
        when(inventoryItemRepository.findBySku("SKU-001")).thenReturn(null);
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItemDto result = inventoryService.createItem(testItemDto);

        // Then
        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        verify(inventoryItemRepository).findBySku("SKU-001");
        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    void createItem_DuplicateSku_ThrowsException() {
        // Given
        when(inventoryItemRepository.findBySku("SKU-001")).thenReturn(testItem);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.createItem(testItemDto);
        });
        verify(inventoryItemRepository, never()).save(any(InventoryItem.class));
    }

    @Test
    void updateItem_Success_UpdatesItem() {
        // Given
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        // SKU is not being changed, so findBySku won't be called
        testItemDto.setName("Updated Item");
        testItemDto.setSku("SKU-001"); // Same SKU
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItemDto result = inventoryService.updateItem(testItemId, testItemDto);

        // Then
        assertNotNull(result);
        verify(inventoryItemRepository).findById(testItemId);
        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    void updateItem_NotFound_ThrowsException() {
        // Given
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.updateItem(testItemId, testItemDto);
        });
    }

    @Test
    void updateItem_DuplicateSku_ThrowsException() {
        // Given
        InventoryItem otherItem = new InventoryItem();
        otherItem.setId(UUID.randomUUID());
        otherItem.setSku("SKU-002");
        testItemDto.setSku("SKU-002");
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(inventoryItemRepository.findBySku("SKU-002")).thenReturn(otherItem);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.updateItem(testItemId, testItemDto);
        });
    }

    @Test
    void deleteItem_Success_DeletesItem() {
        // Given
        when(inventoryItemRepository.existsById(testItemId)).thenReturn(true);
        doNothing().when(inventoryItemRepository).deleteById(testItemId);

        // When
        inventoryService.deleteItem(testItemId);

        // Then
        verify(inventoryItemRepository).existsById(testItemId);
        verify(inventoryItemRepository).deleteById(testItemId);
    }

    @Test
    void deleteItem_NotFound_ThrowsException() {
        // Given
        when(inventoryItemRepository.existsById(testItemId)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.deleteItem(testItemId);
        });
    }

    @Test
    void restockItem_Success_RestocksItem() {
        // Given
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        InventoryItemDto result = inventoryService.restockItem(testItemId, 10);

        // Then
        assertNotNull(result);
        verify(inventoryItemRepository).findById(testItemId);
        verify(inventoryItemRepository).save(any(InventoryItem.class));
    }

    @Test
    void restockItem_InvalidQuantity_ThrowsException() {
        // Given
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.restockItem(testItemId, -5);
        });
    }

    @Test
    void restockItem_ZeroQuantity_ThrowsException() {
        // Given
        when(inventoryItemRepository.findById(testItemId)).thenReturn(Optional.of(testItem));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            inventoryService.restockItem(testItemId, 0);
        });
    }

    @Test
    void getInventoryStats_Success_ReturnsStats() {
        // Given
        when(inventoryItemRepository.getTotalItems()).thenReturn(100L);
        when(inventoryItemRepository.getTotalValue()).thenReturn(BigDecimal.valueOf(10000.00));
        when(inventoryItemRepository.countByStatus(InventoryItem.Status.LOW_STOCK)).thenReturn(5L);
        when(inventoryItemRepository.countByStatus(InventoryItem.Status.OUT_OF_STOCK)).thenReturn(2L);
        when(inventoryItemRepository.countByStatus(InventoryItem.Status.IN_STOCK)).thenReturn(90L);
        when(inventoryItemRepository.countByStatus(InventoryItem.Status.DISCONTINUED)).thenReturn(3L);
        when(inventoryItemRepository.getTotalCategories()).thenReturn(10L);
        when(inventoryItemRepository.getTotalSuppliers()).thenReturn(5L);

        // When
        InventoryStatsDto result = inventoryService.getInventoryStats();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalItems());
        assertEquals(BigDecimal.valueOf(10000.00), result.getTotalValue());
        assertEquals(5L, result.getLowStockItems());
        assertEquals(2L, result.getOutOfStockItems());
        assertEquals(90L, result.getInStockItems());
        assertEquals(3L, result.getDiscontinuedItems());
        assertEquals(10L, result.getTotalCategories());
        assertEquals(5L, result.getTotalSuppliers());
    }

    @Test
    void searchItems_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryItem> itemPage = new PageImpl<>(Arrays.asList(testItem), pageable, 1);
        when(inventoryItemRepository.searchItems(anyString(), any(Pageable.class))).thenReturn(itemPage);

        // When
        Page<InventoryItemDto> result = inventoryService.searchItems("Test", 0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(inventoryItemRepository).searchItems(anyString(), any(Pageable.class));
    }

    @Test
    void searchWithFilters_Success_ReturnsPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<InventoryItem> itemPage = new PageImpl<>(Arrays.asList(testItem), pageable, 1);
        when(inventoryItemRepository.searchWithFilters(anyString(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(itemPage);

        // When
        Page<InventoryItemDto> result = inventoryService.searchWithFilters(
                "Test", InventoryItem.Category.SMART_METERS, InventoryItem.Status.IN_STOCK,
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(200.0), 5, 100, 0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(inventoryItemRepository).searchWithFilters(anyString(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void getLowStockItems_Success_ReturnsList() {
        // Given
        List<InventoryItem> items = Arrays.asList(testItem);
        when(inventoryItemRepository.findLowStockItems()).thenReturn(items);

        // When
        List<InventoryItemDto> result = inventoryService.getLowStockItems();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryItemRepository).findLowStockItems();
    }

    @Test
    void getOutOfStockItems_Success_ReturnsList() {
        // Given
        List<InventoryItem> items = Arrays.asList(testItem);
        when(inventoryItemRepository.findOutOfStockItems()).thenReturn(items);

        // When
        List<InventoryItemDto> result = inventoryService.getOutOfStockItems();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(inventoryItemRepository).findOutOfStockItems();
    }

    @Test
    void bulkUpdateStatus_Success_UpdatesStatus() {
        // Given
        List<UUID> itemIds = Arrays.asList(testItemId);
        List<InventoryItem> items = Arrays.asList(testItem);
        when(inventoryItemRepository.findAllById(itemIds)).thenReturn(items);
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        // When
        inventoryService.bulkUpdateStatus(itemIds, InventoryItem.Status.DISCONTINUED);

        // Then
        verify(inventoryItemRepository).findAllById(itemIds);
        verify(inventoryItemRepository, atLeastOnce()).save(any(InventoryItem.class));
    }

    @Test
    void bulkDelete_Success_DeletesItems() {
        // Given
        List<UUID> itemIds = Arrays.asList(testItemId);
        doNothing().when(inventoryItemRepository).deleteAllById(itemIds);

        // When
        inventoryService.bulkDelete(itemIds);

        // Then
        verify(inventoryItemRepository).deleteAllById(itemIds);
    }
}

