package com.smartwatts.facilityservice.service.impl;

import com.smartwatts.facilityservice.model.WorkOrder;
import com.smartwatts.facilityservice.model.WorkOrderPriority;
import com.smartwatts.facilityservice.model.WorkOrderStatus;
import com.smartwatts.facilityservice.model.WorkOrderType;
import com.smartwatts.facilityservice.repository.WorkOrderRepository;
import com.smartwatts.facilityservice.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class WorkOrderServiceImpl implements WorkOrderService {
    
    private final WorkOrderRepository workOrderRepository;
    private static final AtomicInteger workOrderCounter = new AtomicInteger(1);
    
    @Autowired
    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }
    
    @Override
    public WorkOrder createWorkOrder(WorkOrder workOrder) {
        if (workOrder.getRequestedDate() == null) {
            workOrder.setRequestedDate(LocalDateTime.now());
        }
        workOrder.setStatus(WorkOrderStatus.SUBMITTED);
        workOrder.setWorkOrderNumber(generateWorkOrderNumber());
        workOrder.setCreatedAt(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());
        workOrder.setIsActive(true);
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public WorkOrder updateWorkOrder(Long id, WorkOrder workOrderDetails) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        
        if (workOrderDetails.getTitle() != null) {
            workOrder.setTitle(workOrderDetails.getTitle());
        }
        if (workOrderDetails.getDescription() != null) {
            workOrder.setDescription(workOrderDetails.getDescription());
        }
        if (workOrderDetails.getType() != null) {
            workOrder.setType(workOrderDetails.getType());
        }
        if (workOrderDetails.getPriority() != null) {
            workOrder.setPriority(workOrderDetails.getPriority());
        }
        if (workOrderDetails.getStatus() != null) {
            workOrder.setStatus(workOrderDetails.getStatus());
        }
        if (workOrderDetails.getAsset() != null) {
            workOrder.setAsset(workOrderDetails.getAsset());
        }
        if (workOrderDetails.getLocation() != null) {
            workOrder.setLocation(workOrderDetails.getLocation());
        }
        if (workOrderDetails.getAssignedTechnician() != null) {
            workOrder.setAssignedTechnician(workOrderDetails.getAssignedTechnician());
        }
        if (workOrderDetails.getRequestedBy() != null) {
            workOrder.setRequestedBy(workOrderDetails.getRequestedBy());
        }
        if (workOrderDetails.getDepartment() != null) {
            workOrder.setDepartment(workOrderDetails.getDepartment());
        }
        if (workOrderDetails.getRequestedDate() != null) {
            workOrder.setRequestedDate(workOrderDetails.getRequestedDate());
        }
        if (workOrderDetails.getScheduledDate() != null) {
            workOrder.setScheduledDate(workOrderDetails.getScheduledDate());
        }
        if (workOrderDetails.getStartDate() != null) {
            workOrder.setStartDate(workOrderDetails.getStartDate());
        }
        if (workOrderDetails.getCompletedDate() != null) {
            workOrder.setCompletedDate(workOrderDetails.getCompletedDate());
        }
        if (workOrderDetails.getDueDate() != null) {
            workOrder.setDueDate(workOrderDetails.getDueDate());
        }
        if (workOrderDetails.getEstimatedCost() != null) {
            workOrder.setEstimatedCost(workOrderDetails.getEstimatedCost());
        }
        if (workOrderDetails.getActualCost() != null) {
            workOrder.setActualCost(workOrderDetails.getActualCost());
        }
        if (workOrderDetails.getMaterialsUsed() != null) {
            workOrder.setMaterialsUsed(workOrderDetails.getMaterialsUsed());
        }
        if (workOrderDetails.getWorkPerformed() != null) {
            workOrder.setWorkPerformed(workOrderDetails.getWorkPerformed());
        }
        if (workOrderDetails.getNotes() != null) {
            workOrder.setNotes(workOrderDetails.getNotes());
        }
        if (workOrderDetails.getAttachments() != null) {
            workOrder.setAttachments(workOrderDetails.getAttachments());
        }
        
        workOrder.setUpdatedAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public void deleteWorkOrder(Long id) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        workOrder.setIsActive(false);
        workOrder.setUpdatedAt(LocalDateTime.now());
        workOrderRepository.save(workOrder);
    }
    
    @Override
    public Optional<WorkOrder> getWorkOrderById(Long id) {
        return workOrderRepository.findByIdAndIsActiveTrue(id);
    }
    
    @Override
    public Optional<WorkOrder> getWorkOrderByNumber(String workOrderNumber) {
        return workOrderRepository.findByWorkOrderNumberAndIsActiveTrue(workOrderNumber);
    }
    
    @Override
    public List<WorkOrder> getAllWorkOrders() {
        return workOrderRepository.findByIsActiveTrue();
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByStatus(WorkOrderStatus status) {
        return workOrderRepository.findByStatusAndIsActiveTrue(status);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByPriority(WorkOrderPriority priority) {
        return workOrderRepository.findByPriorityAndIsActiveTrue(priority);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByType(WorkOrderType type) {
        return workOrderRepository.findByTypeAndIsActiveTrue(type);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByTechnician(String assignedTechnician) {
        return workOrderRepository.findByAssignedTechnicianAndIsActiveTrue(assignedTechnician);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByRequestor(String requestedBy) {
        return workOrderRepository.findByRequestedByAndIsActiveTrue(requestedBy);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByDepartment(String department) {
        return workOrderRepository.findByDepartmentAndIsActiveTrue(department);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByAsset(Long assetId) {
        return workOrderRepository.findByAssetIdAndIsActiveTrue(assetId);
    }
    
    @Override
    public List<WorkOrder> searchWorkOrders(String searchTerm) {
        return workOrderRepository.searchWorkOrders(searchTerm);
    }
    
    @Override
    public List<WorkOrder> getOverdueWorkOrders(LocalDateTime dueDate) {
        return workOrderRepository.findOverdueWorkOrders(dueDate);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return workOrderRepository.findWorkOrdersByDateRange(startDate, endDate);
    }
    
    @Override
    public List<WorkOrder> getActiveWorkOrdersByTechnician(String technician) {
        return workOrderRepository.findActiveWorkOrdersByTechnician(technician);
    }
    
    @Override
    public List<WorkOrder> getPendingWorkOrders() {
        return workOrderRepository.findPendingWorkOrders();
    }
    
    @Override
    public Long countWorkOrdersByStatus(WorkOrderStatus status) {
        return workOrderRepository.countByStatusAndIsActiveTrue(status);
    }
    
    @Override
    public Long countWorkOrdersByPriority(WorkOrderPriority priority) {
        return workOrderRepository.countByPriorityAndIsActiveTrue(priority);
    }
    
    @Override
    public List<Object[]> getWorkOrderCountByDepartment() {
        return workOrderRepository.countWorkOrdersByDepartment();
    }
    
    @Override
    public List<Object[]> getWorkOrderCountByTechnician() {
        return workOrderRepository.countWorkOrdersByTechnician();
    }
    
    @Override
    public WorkOrder updateWorkOrderStatus(Long id, WorkOrderStatus status) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        workOrder.setStatus(status);
        workOrder.setUpdatedAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public WorkOrder assignWorkOrderToTechnician(Long id, String assignedTechnician) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        workOrder.setAssignedTechnician(assignedTechnician);
        workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrder.setUpdatedAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public WorkOrder updateWorkOrderPriority(Long id, WorkOrderPriority priority) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        workOrder.setPriority(priority);
        workOrder.setUpdatedAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public WorkOrder scheduleWorkOrder(Long id, LocalDateTime scheduledDate) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        workOrder.setScheduledDate(scheduledDate);
        workOrder.setUpdatedAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public WorkOrder startWorkOrder(Long id) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrder.setStartDate(LocalDateTime.now());
        workOrder.setUpdatedAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public WorkOrder completeWorkOrder(Long id, String workPerformed, String materialsUsed) {
        WorkOrder workOrder = workOrderRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("WorkOrder not found with id: " + id));
        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        workOrder.setCompletedDate(LocalDateTime.now());
        workOrder.setWorkPerformed(workPerformed);
        workOrder.setMaterialsUsed(materialsUsed);
        workOrder.setUpdatedAt(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }
    
    @Override
    public String generateWorkOrderNumber() {
        return "WO-" + String.format("%06d", workOrderCounter.getAndIncrement());
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersDueToday() {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        return workOrderRepository.findWorkOrdersDueToday(today);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersDueThisWeek() {
        LocalDateTime startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(7);
        return workOrderRepository.findWorkOrdersDueThisWeek(startDate, endDate);
    }
    
    @Override
    public List<WorkOrder> getWorkOrdersDueThisMonth() {
        LocalDateTime startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(30);
        return workOrderRepository.findWorkOrdersDueThisMonth(startDate, endDate);
    }
}
