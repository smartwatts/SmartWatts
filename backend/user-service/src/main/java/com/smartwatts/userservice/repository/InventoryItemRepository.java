package com.smartwatts.userservice.repository;

import com.smartwatts.userservice.model.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    
    // Search functionality
    @Query("SELECT i FROM InventoryItem i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.supplier) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<InventoryItem> searchItems(@Param("query") String query, Pageable pageable);
    
    // Filter by category
    Page<InventoryItem> findByCategory(InventoryItem.Category category, Pageable pageable);
    
    // Filter by status
    Page<InventoryItem> findByStatus(InventoryItem.Status status, Pageable pageable);
    
    // Filter by category and status
    Page<InventoryItem> findByCategoryAndStatus(InventoryItem.Category category, InventoryItem.Status status, Pageable pageable);
    
    // Search with filters
    @Query("SELECT i FROM InventoryItem i WHERE " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.sku) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.supplier) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:minPrice IS NULL OR i.unitPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR i.unitPrice <= :maxPrice) AND " +
           "(:minStock IS NULL OR i.currentStock >= :minStock) AND " +
           "(:maxStock IS NULL OR i.currentStock <= :maxStock)")
    Page<InventoryItem> searchWithFilters(
        @Param("query") String query,
        @Param("category") InventoryItem.Category category,
        @Param("status") InventoryItem.Status status,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minStock") Integer minStock,
        @Param("maxStock") Integer maxStock,
        Pageable pageable
    );
    
    // Find by SKU
    InventoryItem findBySku(String sku);
    
    // Find low stock items
    @Query("SELECT i FROM InventoryItem i WHERE i.currentStock <= i.minStock AND i.status != 'DISCONTINUED'")
    List<InventoryItem> findLowStockItems();
    
    // Find out of stock items
    @Query("SELECT i FROM InventoryItem i WHERE i.currentStock = 0 AND i.status != 'DISCONTINUED'")
    List<InventoryItem> findOutOfStockItems();
    
    // Count by status
    long countByStatus(InventoryItem.Status status);
    
    // Count by category
    long countByCategory(InventoryItem.Category category);
    
    // Get total value
    @Query("SELECT COALESCE(SUM(i.totalValue), 0) FROM InventoryItem i")
    BigDecimal getTotalValue();
    
    // Get total items count
    @Query("SELECT COALESCE(SUM(i.currentStock), 0) FROM InventoryItem i")
    Long getTotalItems();
    
    // Get unique suppliers count
    @Query("SELECT COUNT(DISTINCT i.supplier) FROM InventoryItem i")
    Long getTotalSuppliers();
    
    // Get unique categories count
    @Query("SELECT COUNT(DISTINCT i.category) FROM InventoryItem i")
    Long getTotalCategories();
}

