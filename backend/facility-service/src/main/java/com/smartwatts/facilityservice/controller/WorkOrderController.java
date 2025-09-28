package com.smartwatts.facilityservice.controller;

import com.smartwatts.facilityservice.model.WorkOrder;
import com.smartwatts.facilityservice.model.WorkOrderPriority;
import com.smartwatts.facilityservice.model.WorkOrderStatus;
import com.smartwatts.facilityservice.model.WorkOrderType;
import com.smartwatts.facilityservice.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work Order Management", description = "APIs for managing facility work orders")
public class WorkOrderController {
    
    private final WorkOrderService workOrderService;
    
    @PostMapping
    @Operation(summary = "Create a new work order", description = "Creates a new facility work order")
    public ResponseEntity<WorkOrder> createWorkOrder(@RequestBody WorkOrder workOrder) {
        WorkOrder createdWorkOrder = workOrderService.createWorkOrder(workOrder);
        return ResponseEntity.ok(createdWorkOrder);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get work order by ID", description = "Retrieves a work order by its ID")
    public ResponseEntity<WorkOrder> getWorkOrderById(@PathVariable Long id) {
        Optional<WorkOrder> workOrder = workOrderService.getWorkOrderById(id);
        return workOrder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{workOrderNumber}")
    @Operation(summary = "Get work order by number", description = "Retrieves a work order by its work order number")
    public ResponseEntity<WorkOrder> getWorkOrderByNumber(@PathVariable String workOrderNumber) {
        Optional<WorkOrder> workOrder = workOrderService.getWorkOrderByNumber(workOrderNumber);
        return workOrder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Get all work orders", description = "Retrieves all active work orders")
    public ResponseEntity<List<WorkOrder>> getAllWorkOrders() {
        List<WorkOrder> workOrders = workOrderService.getAllWorkOrders();
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get work orders by status", description = "Retrieves work orders by their status")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByStatus(@PathVariable WorkOrderStatus status) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByStatus(status);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get work orders by priority", description = "Retrieves work orders by their priority")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByPriority(@PathVariable WorkOrderPriority priority) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByPriority(priority);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get work orders by type", description = "Retrieves work orders by their type")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByType(@PathVariable WorkOrderType type) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByType(type);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/technician/{assignedTechnician}")
    @Operation(summary = "Get work orders by technician", description = "Retrieves work orders assigned to a specific technician")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByTechnician(@PathVariable String assignedTechnician) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByTechnician(assignedTechnician);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/requestor/{requestedBy}")
    @Operation(summary = "Get work orders by requestor", description = "Retrieves work orders requested by a specific user")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByRequestor(@PathVariable String requestedBy) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByRequestor(requestedBy);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/department/{department}")
    @Operation(summary = "Get work orders by department", description = "Retrieves work orders by their department")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByDepartment(@PathVariable String department) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByDepartment(department);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/asset/{assetId}")
    @Operation(summary = "Get work orders by asset", description = "Retrieves work orders associated with a specific asset")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByAsset(@PathVariable Long assetId) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByAsset(assetId);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search work orders", description = "Searches work orders by text in number, title, or description")
    public ResponseEntity<List<WorkOrder>> searchWorkOrders(@RequestParam String searchTerm) {
        List<WorkOrder> workOrders = workOrderService.searchWorkOrders(searchTerm);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue work orders", description = "Retrieves work orders that are overdue")
    public ResponseEntity<List<WorkOrder>> getOverdueWorkOrders(@RequestParam LocalDateTime dueDate) {
        List<WorkOrder> workOrders = workOrderService.getOverdueWorkOrders(dueDate);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get work orders by date range", description = "Retrieves work orders within a specified date range")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersByDateRange(startDate, endDate);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/technician/{technician}/active")
    @Operation(summary = "Get active work orders by technician", description = "Retrieves active work orders assigned to a specific technician")
    public ResponseEntity<List<WorkOrder>> getActiveWorkOrdersByTechnician(@PathVariable String technician) {
        List<WorkOrder> workOrders = workOrderService.getActiveWorkOrdersByTechnician(technician);
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get pending work orders", description = "Retrieves work orders that are pending approval or assignment")
    public ResponseEntity<List<WorkOrder>> getPendingWorkOrders() {
        List<WorkOrder> workOrders = workOrderService.getPendingWorkOrders();
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/count/status/{status}")
    @Operation(summary = "Count work orders by status", description = "Returns the count of work orders with a specific status")
    public ResponseEntity<Long> countWorkOrdersByStatus(@PathVariable WorkOrderStatus status) {
        Long count = workOrderService.countWorkOrdersByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count/priority/{priority}")
    @Operation(summary = "Count work orders by priority", description = "Returns the count of work orders with a specific priority")
    public ResponseEntity<Long> countWorkOrdersByPriority(@PathVariable WorkOrderPriority priority) {
        Long count = workOrderService.countWorkOrdersByPriority(priority);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count/department")
    @Operation(summary = "Count work orders by department", description = "Returns the count of work orders grouped by department")
    public ResponseEntity<List<Object[]>> getWorkOrderCountByDepartment() {
        List<Object[]> counts = workOrderService.getWorkOrderCountByDepartment();
        return ResponseEntity.ok(counts);
    }
    
    @GetMapping("/count/technician")
    @Operation(summary = "Count work orders by technician", description = "Returns the count of work orders grouped by assigned technician")
    public ResponseEntity<List<Object[]>> getWorkOrderCountByTechnician() {
        List<Object[]> counts = workOrderService.getWorkOrderCountByTechnician();
        return ResponseEntity.ok(counts);
    }
    
    @GetMapping("/due/today")
    @Operation(summary = "Get work orders due today", description = "Retrieves work orders that are due today")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersDueToday() {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersDueToday();
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/due/this-week")
    @Operation(summary = "Get work orders due this week", description = "Retrieves work orders that are due this week")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersDueThisWeek() {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersDueThisWeek();
        return ResponseEntity.ok(workOrders);
    }
    
    @GetMapping("/due/this-month")
    @Operation(summary = "Get work orders due this month", description = "Retrieves work orders that are due this month")
    public ResponseEntity<List<WorkOrder>> getWorkOrdersDueThisMonth() {
        List<WorkOrder> workOrders = workOrderService.getWorkOrdersDueThisMonth();
        return ResponseEntity.ok(workOrders);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update work order", description = "Updates an existing work order")
    public ResponseEntity<WorkOrder> updateWorkOrder(@PathVariable Long id, @RequestBody WorkOrder workOrder) {
        WorkOrder updatedWorkOrder = workOrderService.updateWorkOrder(id, workOrder);
        return ResponseEntity.ok(updatedWorkOrder);
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update work order status", description = "Updates the status of a work order")
    public ResponseEntity<WorkOrder> updateWorkOrderStatus(@PathVariable Long id, @RequestParam WorkOrderStatus status) {
        WorkOrder updatedWorkOrder = workOrderService.updateWorkOrderStatus(id, status);
        return ResponseEntity.ok(updatedWorkOrder);
    }
    
    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign work order to technician", description = "Assigns a work order to a specific technician")
    public ResponseEntity<WorkOrder> assignWorkOrderToTechnician(@PathVariable Long id, @RequestParam String assignedTechnician) {
        WorkOrder updatedWorkOrder = workOrderService.assignWorkOrderToTechnician(id, assignedTechnician);
        return ResponseEntity.ok(updatedWorkOrder);
    }
    
    @PatchMapping("/{id}/priority")
    @Operation(summary = "Update work order priority", description = "Updates the priority of a work order")
    public ResponseEntity<WorkOrder> updateWorkOrderPriority(@PathVariable Long id, @RequestParam WorkOrderPriority priority) {
        WorkOrder updatedWorkOrder = workOrderService.updateWorkOrderPriority(id, priority);
        return ResponseEntity.ok(updatedWorkOrder);
    }
    
    @PatchMapping("/{id}/schedule")
    @Operation(summary = "Schedule work order", description = "Schedules a work order for a specific date and time")
    public ResponseEntity<WorkOrder> scheduleWorkOrder(@PathVariable Long id, @RequestParam LocalDateTime scheduledDate) {
        WorkOrder updatedWorkOrder = workOrderService.scheduleWorkOrder(id, scheduledDate);
        return ResponseEntity.ok(updatedWorkOrder);
    }
    
    @PatchMapping("/{id}/start")
    @Operation(summary = "Start work order", description = "Marks a work order as started")
    public ResponseEntity<WorkOrder> startWorkOrder(@PathVariable Long id) {
        WorkOrder updatedWorkOrder = workOrderService.startWorkOrder(id);
        return ResponseEntity.ok(updatedWorkOrder);
    }
    
    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete work order", description = "Marks a work order as completed with work performed and materials used")
    public ResponseEntity<WorkOrder> completeWorkOrder(
            @PathVariable Long id,
            @RequestParam String workPerformed,
            @RequestParam String materialsUsed) {
        WorkOrder updatedWorkOrder = workOrderService.completeWorkOrder(id, workPerformed, materialsUsed);
        return ResponseEntity.ok(updatedWorkOrder);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete work order", description = "Deletes a work order (soft delete)")
    public ResponseEntity<Void> deleteWorkOrder(@PathVariable Long id) {
        workOrderService.deleteWorkOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/generate-number")
    @Operation(summary = "Generate work order number", description = "Generates a unique work order number")
    public ResponseEntity<String> generateWorkOrderNumber() {
        String workOrderNumber = workOrderService.generateWorkOrderNumber();
        return ResponseEntity.ok(workOrderNumber);
    }
}
