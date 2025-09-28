package com.smartwatts.facilityservice.service;

import com.smartwatts.facilityservice.model.WorkOrder;
import com.smartwatts.facilityservice.model.WorkOrderPriority;
import com.smartwatts.facilityservice.model.WorkOrderStatus;
import com.smartwatts.facilityservice.model.WorkOrderType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkOrderService {
    
    WorkOrder createWorkOrder(WorkOrder workOrder);
    
    WorkOrder updateWorkOrder(Long id, WorkOrder workOrder);
    
    void deleteWorkOrder(Long id);
    
    Optional<WorkOrder> getWorkOrderById(Long id);
    
    Optional<WorkOrder> getWorkOrderByNumber(String workOrderNumber);
    
    List<WorkOrder> getAllWorkOrders();
    
    List<WorkOrder> getWorkOrdersByStatus(WorkOrderStatus status);
    
    List<WorkOrder> getWorkOrdersByPriority(WorkOrderPriority priority);
    
    List<WorkOrder> getWorkOrdersByType(WorkOrderType type);
    
    List<WorkOrder> getWorkOrdersByTechnician(String assignedTechnician);
    
    List<WorkOrder> getWorkOrdersByRequestor(String requestedBy);
    
    List<WorkOrder> getWorkOrdersByDepartment(String department);
    
    List<WorkOrder> getWorkOrdersByAsset(Long assetId);
    
    List<WorkOrder> searchWorkOrders(String searchTerm);
    
    List<WorkOrder> getOverdueWorkOrders(LocalDateTime dueDate);
    
    List<WorkOrder> getWorkOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<WorkOrder> getActiveWorkOrdersByTechnician(String technician);
    
    List<WorkOrder> getPendingWorkOrders();
    
    Long countWorkOrdersByStatus(WorkOrderStatus status);
    
    Long countWorkOrdersByPriority(WorkOrderPriority priority);
    
    List<Object[]> getWorkOrderCountByDepartment();
    
    List<Object[]> getWorkOrderCountByTechnician();
    
    WorkOrder updateWorkOrderStatus(Long id, WorkOrderStatus status);
    
    WorkOrder assignWorkOrderToTechnician(Long id, String assignedTechnician);
    
    WorkOrder updateWorkOrderPriority(Long id, WorkOrderPriority priority);
    
    WorkOrder scheduleWorkOrder(Long id, LocalDateTime scheduledDate);
    
    WorkOrder startWorkOrder(Long id);
    
    WorkOrder completeWorkOrder(Long id, String workPerformed, String materialsUsed);
    
    String generateWorkOrderNumber();
    
    List<WorkOrder> getWorkOrdersDueToday();
    
    List<WorkOrder> getWorkOrdersDueThisWeek();
    
    List<WorkOrder> getWorkOrdersDueThisMonth();
}
