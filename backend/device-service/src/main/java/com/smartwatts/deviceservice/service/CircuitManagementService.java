package com.smartwatts.deviceservice.service;

import com.smartwatts.deviceservice.model.Circuit;
import com.smartwatts.deviceservice.model.SubPanel;
import com.smartwatts.deviceservice.model.Device;
import com.smartwatts.deviceservice.repository.CircuitRepository;
import com.smartwatts.deviceservice.repository.SubPanelRepository;
import com.smartwatts.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CircuitManagementService {

    private final CircuitRepository circuitRepository;
    private final SubPanelRepository subPanelRepository;
    private final DeviceRepository deviceRepository;

    /**
     * Create a new circuit
     */
    @Transactional
    public Circuit createCircuit(Circuit circuit) {
        log.info("Creating circuit: {} for subpanel: {}", circuit.getName(), circuit.getSubPanelId());
        
        circuit.setCreatedAt(LocalDateTime.now());
        circuit.setUpdatedAt(LocalDateTime.now());
        circuit.setIsActive(true);
        
        Circuit savedCircuit = circuitRepository.save(circuit);
        log.info("Created circuit with ID: {}", savedCircuit.getId());
        
        return savedCircuit;
    }

    /**
     * Create a new sub-panel
     */
    @Transactional
    public SubPanel createSubPanel(SubPanel subPanel) {
        log.info("Creating sub-panel: {} for device: {}", subPanel.getName(), subPanel.getDeviceId());
        
        subPanel.setCreatedAt(LocalDateTime.now());
        subPanel.setUpdatedAt(LocalDateTime.now());
        subPanel.setIsActive(true);
        
        SubPanel savedSubPanel = subPanelRepository.save(subPanel);
        log.info("Created sub-panel with ID: {}", savedSubPanel.getId());
        
        return savedSubPanel;
    }

    /**
     * Get hierarchical circuit structure for dashboard
     */
    public Map<String, Object> getCircuitHierarchy(UUID deviceId) {
        log.info("Getting circuit hierarchy for device: {}", deviceId);
        
        // Get main device
        Device device = deviceRepository.findById(deviceId)
            .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));
        
        // Get all sub-panels for this device
        List<SubPanel> subPanels = subPanelRepository.findByDeviceIdAndIsActive(deviceId, true);
        
        // Build hierarchy
        Map<String, Object> hierarchy = new HashMap<>();
        hierarchy.put("device", buildDeviceNode(device));
        hierarchy.put("subPanels", subPanels.stream()
            .map(this::buildSubPanelNode)
            .collect(Collectors.toList()));
        
        return hierarchy;
    }

    /**
     * Get circuit tree view data
     */
    public List<Map<String, Object>> getCircuitTreeView(UUID deviceId) {
        log.info("Getting circuit tree view for device: {}", deviceId);
        
        List<Map<String, Object>> treeNodes = new ArrayList<>();
        
        // Main device node
        Map<String, Object> deviceNode = new HashMap<>();
        deviceNode.put("id", "device-" + deviceId);
        deviceNode.put("name", "Main Panel");
        deviceNode.put("type", "DEVICE");
        deviceNode.put("currentLoad", getDeviceCurrentLoad(deviceId));
        deviceNode.put("children", new ArrayList<>());
        treeNodes.add(deviceNode);
        
        // Get sub-panels
        List<SubPanel> subPanels = subPanelRepository.findByDeviceIdAndIsActive(deviceId, true);
        for (SubPanel subPanel : subPanels) {
            Map<String, Object> subPanelNode = buildSubPanelTreeNode(subPanel);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) deviceNode.get("children");
            children.add(subPanelNode);
        }
        
        return treeNodes;
    }

    /**
     * Get circuit load data for monitoring
     */
    public Map<String, Object> getCircuitLoadData(UUID circuitId) {
        log.info("Getting circuit load data for circuit: {}", circuitId);
        
        Circuit circuit = circuitRepository.findById(circuitId)
            .orElseThrow(() -> new RuntimeException("Circuit not found: " + circuitId));
        
        Map<String, Object> loadData = new HashMap<>();
        loadData.put("circuitId", circuitId);
        loadData.put("circuitName", circuit.getName());
        loadData.put("currentLoad", getCircuitCurrentLoad(circuitId));
        loadData.put("maxCapacity", circuit.getMaxCapacity());
        loadData.put("loadPercentage", calculateLoadPercentage(circuitId, circuit.getMaxCapacity()));
        loadData.put("status", getCircuitStatus(circuitId));
        loadData.put("lastReading", LocalDateTime.now());
        
        return loadData;
    }

    /**
     * Get all circuits with their current status
     */
    public List<Map<String, Object>> getAllCircuitsStatus(UUID deviceId) {
        log.info("Getting all circuits status for device: {}", deviceId);
        
        List<SubPanel> subPanels = subPanelRepository.findByDeviceIdAndIsActive(deviceId, true);
        List<Map<String, Object>> circuitsStatus = new ArrayList<>();
        
        for (SubPanel subPanel : subPanels) {
            List<Circuit> circuits = circuitRepository.findBySubPanelIdAndIsActive(subPanel.getId(), true);
            
            for (Circuit circuit : circuits) {
                Map<String, Object> status = new HashMap<>();
                status.put("circuitId", circuit.getId());
                status.put("circuitName", circuit.getName());
                status.put("subPanelName", subPanel.getName());
                status.put("currentLoad", getCircuitCurrentLoad(circuit.getId()));
                status.put("maxCapacity", circuit.getMaxCapacity());
                status.put("loadPercentage", calculateLoadPercentage(circuit.getId(), circuit.getMaxCapacity()));
                status.put("status", getCircuitStatus(circuit.getId()));
                status.put("location", circuit.getLocation());
                
                circuitsStatus.add(status);
            }
        }
        
        return circuitsStatus;
    }

    /**
     * Update circuit readings from Modbus/CT sensors
     */
    @Transactional
    public void updateCircuitReadings(UUID circuitId, BigDecimal currentReading, BigDecimal voltageReading) {
        log.info("Updating circuit readings for circuit: {} - Current: {}A, Voltage: {}V", 
            circuitId, currentReading, voltageReading);
        
        Circuit circuit = circuitRepository.findById(circuitId)
            .orElseThrow(() -> new RuntimeException("Circuit not found: " + circuitId));
        
        // Update circuit readings
        circuit.setCurrentReading(currentReading);
        circuit.setVoltageReading(voltageReading);
        circuit.setPowerReading(currentReading.multiply(voltageReading));
        circuit.setLastReadingTime(LocalDateTime.now());
        circuit.setUpdatedAt(LocalDateTime.now());
        
        circuitRepository.save(circuit);
        
        // Check for overload conditions
        checkCircuitOverload(circuit);
    }

    /**
     * Build device node for hierarchy
     */
    private Map<String, Object> buildDeviceNode(Device device) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", device.getId());
        node.put("name", device.getName());
        node.put("type", "DEVICE");
        node.put("currentLoad", getDeviceCurrentLoad(device.getId()));
        node.put("maxCapacity", device.getMaxPower());
        return node;
    }

    /**
     * Build sub-panel node for hierarchy
     */
    private Map<String, Object> buildSubPanelNode(SubPanel subPanel) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", subPanel.getId());
        node.put("name", subPanel.getName());
        node.put("type", "SUB_PANEL");
        node.put("currentLoad", getSubPanelCurrentLoad(subPanel.getId()));
        node.put("maxCapacity", subPanel.getMaxCapacity());
        node.put("location", subPanel.getLocation());
        
        // Get circuits for this sub-panel
        List<Circuit> circuits = circuitRepository.findBySubPanelIdAndIsActive(subPanel.getId(), true);
        List<Map<String, Object>> circuitNodes = circuits.stream()
            .map(this::buildCircuitNode)
            .collect(Collectors.toList());
        node.put("circuits", circuitNodes);
        
        return node;
    }

    /**
     * Build circuit node for hierarchy
     */
    private Map<String, Object> buildCircuitNode(Circuit circuit) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", circuit.getId());
        node.put("name", circuit.getName());
        node.put("type", "CIRCUIT");
        node.put("currentLoad", getCircuitCurrentLoad(circuit.getId()));
        node.put("maxCapacity", circuit.getMaxCapacity());
        node.put("location", circuit.getLocation());
        node.put("status", getCircuitStatus(circuit.getId()));
        return node;
    }

    /**
     * Build sub-panel tree node
     */
    private Map<String, Object> buildSubPanelTreeNode(SubPanel subPanel) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", "subpanel-" + subPanel.getId());
        node.put("name", subPanel.getName());
        node.put("type", "SUB_PANEL");
        node.put("currentLoad", getSubPanelCurrentLoad(subPanel.getId()));
        node.put("children", new ArrayList<>());
        
        // Get circuits for this sub-panel
        List<Circuit> circuits = circuitRepository.findBySubPanelIdAndIsActive(subPanel.getId(), true);
        for (Circuit circuit : circuits) {
            Map<String, Object> circuitNode = new HashMap<>();
            circuitNode.put("id", "circuit-" + circuit.getId());
            circuitNode.put("name", circuit.getName());
            circuitNode.put("type", "CIRCUIT");
            circuitNode.put("currentLoad", getCircuitCurrentLoad(circuit.getId()));
            circuitNode.put("maxCapacity", circuit.getMaxCapacity());
            circuitNode.put("status", getCircuitStatus(circuit.getId()));
            circuitNode.put("children", new ArrayList<>());
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            children.add(circuitNode);
        }
        
        return node;
    }

    /**
     * Get device current load (sum of all sub-panels)
     */
    private BigDecimal getDeviceCurrentLoad(UUID deviceId) {
        List<SubPanel> subPanels = subPanelRepository.findByDeviceIdAndIsActive(deviceId, true);
        return subPanels.stream()
            .map(subPanel -> getSubPanelCurrentLoad(subPanel.getId()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get sub-panel current load (sum of all circuits)
     */
    private BigDecimal getSubPanelCurrentLoad(UUID subPanelId) {
        List<Circuit> circuits = circuitRepository.findBySubPanelIdAndIsActive(subPanelId, true);
        return circuits.stream()
            .map(circuit -> getCircuitCurrentLoad(circuit.getId()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get circuit current load
     */
    private BigDecimal getCircuitCurrentLoad(UUID circuitId) {
        Circuit circuit = circuitRepository.findById(circuitId).orElse(null);
        return circuit != null ? circuit.getCurrentReading() : BigDecimal.ZERO;
    }

    /**
     * Calculate load percentage
     */
    private BigDecimal calculateLoadPercentage(UUID circuitId, BigDecimal maxCapacity) {
        BigDecimal currentLoad = getCircuitCurrentLoad(circuitId);
        if (maxCapacity.compareTo(BigDecimal.ZERO) > 0) {
            return currentLoad.divide(maxCapacity, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Get circuit status based on load
     */
    private String getCircuitStatus(UUID circuitId) {
        Circuit circuit = circuitRepository.findById(circuitId).orElse(null);
        if (circuit == null) return "UNKNOWN";
        
        BigDecimal loadPercentage = calculateLoadPercentage(circuitId, circuit.getMaxCapacity());
        
        if (loadPercentage.compareTo(new BigDecimal("90")) > 0) {
            return "OVERLOAD";
        } else if (loadPercentage.compareTo(new BigDecimal("75")) > 0) {
            return "HIGH";
        } else if (loadPercentage.compareTo(new BigDecimal("50")) > 0) {
            return "NORMAL";
        } else {
            return "LOW";
        }
    }

    /**
     * Check for circuit overload conditions
     */
    private void checkCircuitOverload(Circuit circuit) {
        BigDecimal loadPercentage = calculateLoadPercentage(circuit.getId(), circuit.getMaxCapacity());
        
        if (loadPercentage.compareTo(new BigDecimal("90")) > 0) {
            log.warn("Circuit overload detected: {} at {}% capacity", 
                circuit.getName(), loadPercentage);
            // Here you would trigger alerts/notifications
        }
    }
}
