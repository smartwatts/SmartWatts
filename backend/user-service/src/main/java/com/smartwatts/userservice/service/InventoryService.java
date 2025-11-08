package com.smartwatts.userservice.service;

import com.smartwatts.userservice.dto.InventoryItemDto;
import com.smartwatts.userservice.dto.InventoryStatsDto;
import com.smartwatts.userservice.model.InventoryItem;
import com.smartwatts.userservice.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {
    
    private final InventoryItemRepository inventoryItemRepository;
    
    // Get all inventory items with pagination and sorting
    public Page<InventoryItemDto> getAllItems(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching inventory items - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InventoryItem> items = inventoryItemRepository.findAll(pageable);
        return items.map(this::convertToDto);
    }
    
    // Search inventory items
    public Page<InventoryItemDto> searchItems(String query, int page, int size, String sortBy, String sortDir) {
        log.info("Searching inventory items - query: {}, page: {}, size: {}", query, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InventoryItem> items = inventoryItemRepository.searchItems(query, pageable);
        return items.map(this::convertToDto);
    }
    
    // Search with advanced filters
    public Page<InventoryItemDto> searchWithFilters(
            String query,
            InventoryItem.Category category,
            InventoryItem.Status status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minStock,
            Integer maxStock,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        
        log.info("Advanced search - query: {}, category: {}, status: {}, minPrice: {}, maxPrice: {}, minStock: {}, maxStock: {}", 
                query, category, status, minPrice, maxPrice, minStock, maxStock);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InventoryItem> items = inventoryItemRepository.searchWithFilters(
            query, category, status, minPrice, maxPrice, minStock, maxStock, pageable);
        return items.map(this::convertToDto);
    }
    
    // Get inventory statistics
    public InventoryStatsDto getInventoryStats() {
        log.info("Fetching inventory statistics");
        
        Long totalItems = inventoryItemRepository.getTotalItems();
        BigDecimal totalValue = inventoryItemRepository.getTotalValue();
        Long lowStockItems = inventoryItemRepository.countByStatus(InventoryItem.Status.LOW_STOCK);
        Long outOfStockItems = inventoryItemRepository.countByStatus(InventoryItem.Status.OUT_OF_STOCK);
        Long inStockItems = inventoryItemRepository.countByStatus(InventoryItem.Status.IN_STOCK);
        Long discontinuedItems = inventoryItemRepository.countByStatus(InventoryItem.Status.DISCONTINUED);
        Long totalCategories = inventoryItemRepository.getTotalCategories();
        Long totalSuppliers = inventoryItemRepository.getTotalSuppliers();
        
        return new InventoryStatsDto(
            totalItems, totalValue, lowStockItems, outOfStockItems, 
            inStockItems, discontinuedItems, totalCategories, totalSuppliers
        );
    }
    
    // Get item by ID
    public InventoryItemDto getItemById(UUID id) {
        log.info("Fetching inventory item by ID: {}", id);
        InventoryItem item = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));
        return convertToDto(item);
    }
    
    // Create new inventory item
    public InventoryItemDto createItem(InventoryItemDto itemDto) {
        log.info("Creating new inventory item: {}", itemDto.getName());
        
        // Check if SKU already exists
        if (inventoryItemRepository.findBySku(itemDto.getSku()) != null) {
            throw new RuntimeException("SKU already exists: " + itemDto.getSku());
        }
        
        InventoryItem item = convertToEntity(itemDto);
        item.calculateTotalValue();
        item.updateStatus();
        
        InventoryItem savedItem = inventoryItemRepository.save(item);
        log.info("Created inventory item with ID: {}", savedItem.getId());
        
        return convertToDto(savedItem);
    }
    
    // Update inventory item
    public InventoryItemDto updateItem(UUID id, InventoryItemDto itemDto) {
        log.info("Updating inventory item: {}", id);
        
        InventoryItem existingItem = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));
        
        // Check if SKU is being changed and if it already exists
        if (!existingItem.getSku().equals(itemDto.getSku())) {
            if (inventoryItemRepository.findBySku(itemDto.getSku()) != null) {
                throw new RuntimeException("SKU already exists: " + itemDto.getSku());
            }
        }
        
        updateEntityFromDto(existingItem, itemDto);
        existingItem.calculateTotalValue();
        existingItem.updateStatus();
        
        InventoryItem savedItem = inventoryItemRepository.save(existingItem);
        log.info("Updated inventory item: {}", savedItem.getId());
        
        return convertToDto(savedItem);
    }
    
    // Delete inventory item
    public void deleteItem(UUID id) {
        log.info("Deleting inventory item: {}", id);
        
        if (!inventoryItemRepository.existsById(id)) {
            throw new RuntimeException("Inventory item not found with id: " + id);
        }
        
        inventoryItemRepository.deleteById(id);
        log.info("Deleted inventory item: {}", id);
    }
    
    // Restock item
    public InventoryItemDto restockItem(UUID id, Integer quantity) {
        log.info("Restocking item: {} with quantity: {}", id, quantity);
        
        InventoryItem item = inventoryItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory item not found with id: " + id));
        
        if (quantity <= 0) {
            throw new RuntimeException("Restock quantity must be positive");
        }
        
        item.setCurrentStock(item.getCurrentStock() + quantity);
        item.setLastRestocked(LocalDate.now());
        item.calculateTotalValue();
        item.updateStatus();
        
        InventoryItem savedItem = inventoryItemRepository.save(item);
        log.info("Restocked item: {} - new stock: {}", id, savedItem.getCurrentStock());
        
        return convertToDto(savedItem);
    }
    
    // Bulk operations
    public void bulkUpdateStatus(List<UUID> itemIds, InventoryItem.Status status) {
        log.info("Bulk updating status for {} items to: {}", itemIds.size(), status);
        
        List<InventoryItem> items = inventoryItemRepository.findAllById(itemIds);
        items.forEach(item -> {
            item.setStatus(status);
            inventoryItemRepository.save(item);
        });
        
        log.info("Bulk status update completed");
    }
    
    public void bulkDelete(List<UUID> itemIds) {
        log.info("Bulk deleting {} items", itemIds.size());
        inventoryItemRepository.deleteAllById(itemIds);
        log.info("Bulk delete completed");
    }
    
    // Get low stock items
    public List<InventoryItemDto> getLowStockItems() {
        log.info("Fetching low stock items");
        List<InventoryItem> items = inventoryItemRepository.findLowStockItems();
        return items.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Get out of stock items
    public List<InventoryItemDto> getOutOfStockItems() {
        log.info("Fetching out of stock items");
        List<InventoryItem> items = inventoryItemRepository.findOutOfStockItems();
        return items.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // Helper methods
    private InventoryItemDto convertToDto(InventoryItem item) {
        InventoryItemDto dto = new InventoryItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setCategory(item.getCategory());
        dto.setSku(item.getSku());
        dto.setCurrentStock(item.getCurrentStock());
        dto.setMinStock(item.getMinStock());
        dto.setMaxStock(item.getMaxStock());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalValue(item.getTotalValue());
        dto.setSupplier(item.getSupplier());
        dto.setLastRestocked(item.getLastRestocked());
        dto.setNextRestock(item.getNextRestock());
        dto.setStatus(item.getStatus());
        dto.setLocation(item.getLocation());
        dto.setConditionType(item.getConditionType());
        dto.setWarranty(item.getWarranty());
        dto.setDescription(item.getDescription());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
    
    private InventoryItem convertToEntity(InventoryItemDto dto) {
        InventoryItem item = new InventoryItem();
        item.setName(dto.getName());
        item.setCategory(dto.getCategory());
        item.setSku(dto.getSku());
        item.setCurrentStock(dto.getCurrentStock());
        item.setMinStock(dto.getMinStock());
        item.setMaxStock(dto.getMaxStock());
        item.setUnitPrice(dto.getUnitPrice());
        item.setSupplier(dto.getSupplier());
        item.setLastRestocked(dto.getLastRestocked());
        item.setNextRestock(dto.getNextRestock());
        item.setLocation(dto.getLocation());
        item.setConditionType(dto.getConditionType());
        item.setWarranty(dto.getWarranty());
        item.setDescription(dto.getDescription());
        return item;
    }
    
    private void updateEntityFromDto(InventoryItem item, InventoryItemDto dto) {
        item.setName(dto.getName());
        item.setCategory(dto.getCategory());
        item.setSku(dto.getSku());
        item.setCurrentStock(dto.getCurrentStock());
        item.setMinStock(dto.getMinStock());
        item.setMaxStock(dto.getMaxStock());
        item.setUnitPrice(dto.getUnitPrice());
        item.setSupplier(dto.getSupplier());
        item.setLastRestocked(dto.getLastRestocked());
        item.setNextRestock(dto.getNextRestock());
        item.setLocation(dto.getLocation());
        item.setConditionType(dto.getConditionType());
        item.setWarranty(dto.getWarranty());
        item.setDescription(dto.getDescription());
    }
}

