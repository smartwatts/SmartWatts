package com.smartwatts.facilityservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartwatts.facilityservice.model.WorkOrder;
import com.smartwatts.facilityservice.model.WorkOrderPriority;
import com.smartwatts.facilityservice.model.WorkOrderStatus;
import com.smartwatts.facilityservice.model.WorkOrderType;
import com.smartwatts.facilityservice.service.WorkOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkOrderController.class)
class WorkOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkOrderService workOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    private WorkOrder testWorkOrder;
    private Long testWorkOrderId;

    @BeforeEach
    void setUp() {
        testWorkOrderId = 1L;
        
        testWorkOrder = new WorkOrder();
        testWorkOrder.setId(testWorkOrderId);
        testWorkOrder.setWorkOrderNumber("WO-001");
        testWorkOrder.setTitle("Test Work Order");
        testWorkOrder.setStatus(WorkOrderStatus.DRAFT);
        testWorkOrder.setPriority(WorkOrderPriority.MEDIUM);
        testWorkOrder.setType(WorkOrderType.PREVENTIVE_MAINTENANCE);
        testWorkOrder.setRequestedBy("user@example.com");
        testWorkOrder.setAssignedTechnician("tech@example.com");
        testWorkOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createWorkOrder_Success_ReturnsWorkOrder() throws Exception {
        // Given
        when(workOrderService.createWorkOrder(any(WorkOrder.class))).thenReturn(testWorkOrder);

        // When & Then
        mockMvc.perform(post("/api/v1/work-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testWorkOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Work Order"));

        verify(workOrderService).createWorkOrder(any(WorkOrder.class));
    }

    @Test
    void getWorkOrderById_Success_ReturnsWorkOrder() throws Exception {
        // Given
        when(workOrderService.getWorkOrderById(testWorkOrderId)).thenReturn(Optional.of(testWorkOrder));

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/{id}", testWorkOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testWorkOrderId))
                .andExpect(jsonPath("$.title").value("Test Work Order"));

        verify(workOrderService).getWorkOrderById(testWorkOrderId);
    }

    @Test
    void getWorkOrderById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(workOrderService.getWorkOrderById(testWorkOrderId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/{id}", testWorkOrderId))
                .andExpect(status().isNotFound());

        verify(workOrderService).getWorkOrderById(testWorkOrderId);
    }

    @Test
    void getWorkOrderByNumber_Success_ReturnsWorkOrder() throws Exception {
        // Given
        when(workOrderService.getWorkOrderByNumber("WO-001")).thenReturn(Optional.of(testWorkOrder));

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/number/{workOrderNumber}", "WO-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workOrderNumber").value("WO-001"));

        verify(workOrderService).getWorkOrderByNumber("WO-001");
    }

    @Test
    void getAllWorkOrders_Success_ReturnsList() throws Exception {
        // Given
        List<WorkOrder> workOrders = Arrays.asList(testWorkOrder);
        when(workOrderService.getAllWorkOrders()).thenReturn(workOrders);

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Work Order"));

        verify(workOrderService).getAllWorkOrders();
    }

    @Test
    void getWorkOrdersByStatus_Success_ReturnsList() throws Exception {
        // Given
        List<WorkOrder> workOrders = Arrays.asList(testWorkOrder);
        when(workOrderService.getWorkOrdersByStatus(WorkOrderStatus.DRAFT)).thenReturn(workOrders);

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/status/{status}", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(workOrderService).getWorkOrdersByStatus(WorkOrderStatus.DRAFT);
    }

    @Test
    void getWorkOrdersByPriority_Success_ReturnsList() throws Exception {
        // Given
        List<WorkOrder> workOrders = Arrays.asList(testWorkOrder);
        when(workOrderService.getWorkOrdersByPriority(WorkOrderPriority.MEDIUM)).thenReturn(workOrders);

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/priority/{priority}", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(workOrderService).getWorkOrdersByPriority(WorkOrderPriority.MEDIUM);
    }

    @Test
    void getWorkOrdersByType_Success_ReturnsList() throws Exception {
        // Given
        List<WorkOrder> workOrders = Arrays.asList(testWorkOrder);
        when(workOrderService.getWorkOrdersByType(WorkOrderType.PREVENTIVE_MAINTENANCE)).thenReturn(workOrders);

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/type/{type}", "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(workOrderService).getWorkOrdersByType(WorkOrderType.PREVENTIVE_MAINTENANCE);
    }

    @Test
    void getWorkOrdersByTechnician_Success_ReturnsList() throws Exception {
        // Given
        List<WorkOrder> workOrders = Arrays.asList(testWorkOrder);
        when(workOrderService.getWorkOrdersByTechnician("tech@example.com")).thenReturn(workOrders);

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/technician/{assignedTechnician}", "tech@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(workOrderService).getWorkOrdersByTechnician("tech@example.com");
    }

    @Test
    void getWorkOrdersByRequestor_Success_ReturnsList() throws Exception {
        // Given
        List<WorkOrder> workOrders = Arrays.asList(testWorkOrder);
        when(workOrderService.getWorkOrdersByRequestor("user@example.com")).thenReturn(workOrders);

        // When & Then
        mockMvc.perform(get("/api/v1/work-orders/requestor/{requestedBy}", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(workOrderService).getWorkOrdersByRequestor("user@example.com");
    }

    @Test
    void updateWorkOrder_Success_ReturnsUpdatedWorkOrder() throws Exception {
        // Given
        testWorkOrder.setTitle("Updated Work Order");
        when(workOrderService.updateWorkOrder(eq(testWorkOrderId), any(WorkOrder.class))).thenReturn(testWorkOrder);

        // When & Then
        mockMvc.perform(put("/api/v1/work-orders/{id}", testWorkOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testWorkOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Work Order"));

        verify(workOrderService).updateWorkOrder(eq(testWorkOrderId), any(WorkOrder.class));
    }

    @Test
    void deleteWorkOrder_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(workOrderService).deleteWorkOrder(testWorkOrderId);

        // When & Then
        mockMvc.perform(delete("/api/v1/work-orders/{id}", testWorkOrderId))
                .andExpect(status().isNoContent());

        verify(workOrderService).deleteWorkOrder(testWorkOrderId);
    }
}

