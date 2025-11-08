package com.smartwatts.userservice.controller;

import com.smartwatts.userservice.dto.InventoryItemDto;
import com.smartwatts.userservice.dto.InventoryStatsDto;
import com.smartwatts.userservice.model.InventoryItem;
import com.smartwatts.userservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    // Get all inventory items with pagination and sorting
    @GetMapping
    public ResponseEntity<Page<InventoryItemDto>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/v1/inventory - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Page<InventoryItemDto> items = inventoryService.getAllItems(page, size, sortBy, sortDir);
        return ResponseEntity.ok(items);
    }
    
    // Search inventory items
    @GetMapping("/search")
    public ResponseEntity<Page<InventoryItemDto>> searchItems(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/v1/inventory/search - query: {}, page: {}, size: {}", query, page, size);
        
        Page<InventoryItemDto> items;
        if (query != null && !query.trim().isEmpty()) {
            items = inventoryService.searchItems(query, page, size, sortBy, sortDir);
        } else {
            items = inventoryService.getAllItems(page, size, sortBy, sortDir);
        }
        
        return ResponseEntity.ok(items);
    }
    
    // Advanced search with filters
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<InventoryItemDto>> advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) InventoryItem.Category category,
            @RequestParam(required = false) InventoryItem.Status status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/v1/inventory/search/advanced - query: {}, category: {}, status: {}", query, category, status);
        
        Page<InventoryItemDto> items = inventoryService.searchWithFilters(
            query, category, status, minPrice, maxPrice, minStock, maxStock, page, size, sortBy, sortDir);
        
        return ResponseEntity.ok(items);
    }
    
    // Get inventory statistics
    @GetMapping("/stats")
    public ResponseEntity<InventoryStatsDto> getInventoryStats() {
        log.info("GET /api/v1/inventory/stats");
        
        InventoryStatsDto stats = inventoryService.getInventoryStats();
        return ResponseEntity.ok(stats);
    }
    
    // Get item by ID
    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemDto> getItemById(@PathVariable UUID id) {
        log.info("GET /api/v1/inventory/{}", id);
        
        InventoryItemDto item = inventoryService.getItemById(id);
        return ResponseEntity.ok(item);
    }
    
    // Create new inventory item
    @PostMapping
    public ResponseEntity<InventoryItemDto> createItem(@RequestBody InventoryItemDto itemDto) {
        log.info("POST /api/v1/inventory - creating item: {}", itemDto.getName());
        
        InventoryItemDto createdItem = inventoryService.createItem(itemDto);
        return ResponseEntity.ok(createdItem);
    }
    
    // Update inventory item
    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemDto> updateItem(@PathVariable UUID id, @RequestBody InventoryItemDto itemDto) {
        log.info("PUT /api/v1/inventory/{} - updating item", id);
        
        InventoryItemDto updatedItem = inventoryService.updateItem(id, itemDto);
        return ResponseEntity.ok(updatedItem);
    }
    
    // Delete inventory item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        log.info("DELETE /api/v1/inventory/{}", id);
        
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
    
    // Restock item
    @PostMapping("/{id}/restock")
    public ResponseEntity<InventoryItemDto> restockItem(@PathVariable UUID id, @RequestParam Integer quantity) {
        log.info("POST /api/v1/inventory/{}/restock - quantity: {}", id, quantity);
        
        InventoryItemDto restockedItem = inventoryService.restockItem(id, quantity);
        return ResponseEntity.ok(restockedItem);
    }
    
    // Bulk operations
    @PutMapping("/bulk/status")
    public ResponseEntity<Void> bulkUpdateStatus(@RequestParam List<UUID> itemIds, @RequestParam InventoryItem.Status status) {
        log.info("PUT /api/v1/inventory/bulk/status - updating {} items to status: {}", itemIds.size(), status);
        
        inventoryService.bulkUpdateStatus(itemIds, status);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDelete(@RequestParam List<UUID> itemIds) {
        log.info("DELETE /api/v1/inventory/bulk - deleting {} items", itemIds.size());
        
        inventoryService.bulkDelete(itemIds);
        return ResponseEntity.noContent().build();
    }
    
    // Get low stock items
    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryItemDto>> getLowStockItems() {
        log.info("GET /api/v1/inventory/low-stock");
        
        List<InventoryItemDto> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(items);
    }
    
    // Get out of stock items
    @GetMapping("/out-of-stock")
    public ResponseEntity<List<InventoryItemDto>> getOutOfStockItems() {
        log.info("GET /api/v1/inventory/out-of-stock");
        
        List<InventoryItemDto> items = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(items);
    }
}

