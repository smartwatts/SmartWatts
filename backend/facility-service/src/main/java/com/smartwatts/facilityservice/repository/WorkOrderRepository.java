package com.smartwatts.facilityservice.repository;

import com.smartwatts.facilityservice.model.WorkOrder;
import com.smartwatts.facilityservice.model.WorkOrderPriority;
import com.smartwatts.facilityservice.model.WorkOrderStatus;
import com.smartwatts.facilityservice.model.WorkOrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    
    Optional<WorkOrder> findByWorkOrderNumber(String workOrderNumber);
    
    Optional<WorkOrder> findByIdAndIsActiveTrue(Long id);
    
    Optional<WorkOrder> findByWorkOrderNumberAndIsActiveTrue(String workOrderNumber);
    
    List<WorkOrder> findByStatus(WorkOrderStatus status);
    
    List<WorkOrder> findByStatusAndIsActiveTrue(WorkOrderStatus status);
    
    List<WorkOrder> findByPriority(WorkOrderPriority priority);
    
    List<WorkOrder> findByPriorityAndIsActiveTrue(WorkOrderPriority priority);
    
    List<WorkOrder> findByTypeAndIsActiveTrue(WorkOrderType type);
    
    List<WorkOrder> findByAssignedTechnician(String assignedTechnician);
    
    List<WorkOrder> findByAssignedTechnicianAndIsActiveTrue(String assignedTechnician);
    
    List<WorkOrder> findByRequestedBy(String requestedBy);
    
    List<WorkOrder> findByRequestedByAndIsActiveTrue(String requestedBy);
    
    List<WorkOrder> findByDepartment(String department);
    
    List<WorkOrder> findByDepartmentAndIsActiveTrue(String department);
    
    List<WorkOrder> findByAssetId(Long assetId);
    
    List<WorkOrder> findByAssetIdAndIsActiveTrue(Long assetId);
    
    List<WorkOrder> findByIsActiveTrue();
    
    Page<WorkOrder> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.dueDate <= :dueDate AND wo.status NOT IN ('COMPLETED', 'CANCELLED') AND wo.isActive = true")
    List<WorkOrder> findOverdueWorkOrders(@Param("dueDate") LocalDateTime dueDate);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.scheduledDate BETWEEN :startDate AND :endDate AND wo.isActive = true")
    List<WorkOrder> findWorkOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.assignedTechnician = :technician AND wo.status IN ('ASSIGNED', 'IN_PROGRESS') AND wo.isActive = true")
    List<WorkOrder> findActiveWorkOrdersByTechnician(@Param("technician") String technician);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.workOrderNumber LIKE %:searchTerm% OR wo.title LIKE %:searchTerm% OR wo.description LIKE %:searchTerm%")
    List<WorkOrder> searchWorkOrders(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.workOrderNumber LIKE %:searchTerm% OR wo.title LIKE %:searchTerm% OR wo.description LIKE %:searchTerm%")
    Page<WorkOrder> searchWorkOrders(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status = :status AND wo.isActive = true")
    Long countByStatusAndIsActiveTrue(@Param("status") WorkOrderStatus status);
    
    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.priority = :priority AND wo.isActive = true")
    Long countByPriorityAndIsActiveTrue(@Param("priority") WorkOrderPriority priority);
    
    @Query("SELECT wo.department, COUNT(wo) FROM WorkOrder wo WHERE wo.isActive = true GROUP BY wo.department")
    List<Object[]> countWorkOrdersByDepartment();
    
    @Query("SELECT wo.assignedTechnician, COUNT(wo) FROM WorkOrder wo WHERE wo.isActive = true GROUP BY wo.assignedTechnician")
    List<Object[]> countWorkOrdersByTechnician();
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.status IN ('SUBMITTED', 'APPROVED') AND wo.isActive = true ORDER BY wo.priority DESC, wo.requestedDate ASC")
    List<WorkOrder> findPendingWorkOrders();
    
    @Query("SELECT COUNT(wo) FROM WorkOrder wo WHERE wo.status IN ('SUBMITTED', 'APPROVED') AND wo.isActive = true")
    Long countPendingWorkOrders();
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.createdAt BETWEEN :startDate AND :endDate AND wo.isActive = true")
    List<WorkOrder> findByCreatedAtBetweenAndIsActiveTrue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.dueDate = :today AND wo.isActive = true")
    List<WorkOrder> findWorkOrdersDueToday(@Param("today") LocalDateTime today);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.dueDate BETWEEN :startDate AND :endDate AND wo.isActive = true")
    List<WorkOrder> findWorkOrdersDueThisWeek(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT wo FROM WorkOrder wo WHERE wo.dueDate BETWEEN :startDate AND :endDate AND wo.isActive = true")
    List<WorkOrder> findWorkOrdersDueThisMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
