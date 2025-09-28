package com.smartwatts.facilityservice.controller;

import com.smartwatts.facilityservice.model.Fleet;
import com.smartwatts.facilityservice.model.FleetStatus;
import com.smartwatts.facilityservice.service.FleetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fleet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fleet Management", description = "APIs for managing fleet vehicles")
public class FleetController {

    private final FleetService fleetService;

    @PostMapping
    @Operation(summary = "Create a new fleet vehicle", description = "Creates a new fleet vehicle")
    public ResponseEntity<Fleet> createFleet(@RequestBody Fleet fleet) {
        log.info("Creating new fleet vehicle: {}", fleet.getName());
        Fleet createdFleet = fleetService.createFleet(fleet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFleet);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a fleet vehicle", description = "Updates an existing fleet vehicle")
    public ResponseEntity<Fleet> updateFleet(
            @Parameter(description = "Fleet ID") @PathVariable Long id,
            @RequestBody Fleet fleetDetails) {
        log.info("Updating fleet vehicle with ID: {}", id);
        Fleet updatedFleet = fleetService.updateFleet(id, fleetDetails);
        return ResponseEntity.ok(updatedFleet);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a fleet vehicle", description = "Soft deletes a fleet vehicle")
    public ResponseEntity<Void> deleteFleet(
            @Parameter(description = "Fleet ID") @PathVariable Long id) {
        log.info("Deleting fleet vehicle with ID: {}", id);
        fleetService.deleteFleet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fleet vehicle by ID", description = "Retrieves a fleet vehicle by its ID")
    public ResponseEntity<Fleet> getFleetById(
            @Parameter(description = "Fleet ID") @PathVariable Long id) {
        return fleetService.getFleetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get fleet vehicle by vehicle ID", description = "Retrieves a fleet vehicle by its vehicle ID")
    public ResponseEntity<Fleet> getFleetByVehicleId(
            @Parameter(description = "Vehicle ID") @PathVariable String vehicleId) {
        return fleetService.getFleetByVehicleId(vehicleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all fleet vehicles", description = "Retrieves all fleet vehicles with pagination")
    public ResponseEntity<Page<Fleet>> getAllFleet(Pageable pageable) {
        Page<Fleet> fleet = fleetService.getFleet(pageable);
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all fleet vehicles without pagination", description = "Retrieves all fleet vehicles as a list")
    public ResponseEntity<List<Fleet>> getAllFleetList() {
        List<Fleet> fleet = fleetService.getAllFleet();
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get fleet vehicles by status", description = "Retrieves fleet vehicles filtered by status")
    public ResponseEntity<List<Fleet>> getFleetByStatus(
            @Parameter(description = "Fleet status") @PathVariable FleetStatus status) {
        List<Fleet> fleet = fleetService.getFleetByStatus(status);
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get fleet vehicles by type", description = "Retrieves fleet vehicles filtered by type")
    public ResponseEntity<List<Fleet>> getFleetByType(
            @Parameter(description = "Fleet type") @PathVariable String type) {
        List<Fleet> fleet = fleetService.getFleetByType(
            com.smartwatts.facilityservice.model.FleetType.valueOf(type.toUpperCase()));
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get fleet vehicles by department", description = "Retrieves fleet vehicles filtered by department")
    public ResponseEntity<List<Fleet>> getFleetByDepartment(
            @Parameter(description = "Department name") @PathVariable String department) {
        List<Fleet> fleet = fleetService.getFleetByDepartment(department);
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get fleet vehicles by assigned driver", description = "Retrieves fleet vehicles assigned to a specific driver")
    public ResponseEntity<List<Fleet>> getFleetByAssignedDriver(
            @Parameter(description = "Driver ID") @PathVariable String driverId) {
        List<Fleet> fleet = fleetService.getFleetByAssignedDriver(driverId);
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "Get fleet vehicles by location", description = "Retrieves fleet vehicles filtered by location")
    public ResponseEntity<List<Fleet>> getFleetByLocation(
            @Parameter(description = "Location") @PathVariable String location) {
        List<Fleet> fleet = fleetService.getFleetByLocation(location);
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/license/{licensePlate}")
    @Operation(summary = "Get fleet vehicle by license plate", description = "Retrieves a fleet vehicle by its license plate")
    public ResponseEntity<Fleet> getFleetByLicensePlate(
            @Parameter(description = "License plate") @PathVariable String licensePlate) {
        return fleetService.getFleetByLicensePlate(licensePlate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vin/{vin}")
    @Operation(summary = "Get fleet vehicle by VIN", description = "Retrieves a fleet vehicle by its VIN")
    public ResponseEntity<Fleet> getFleetByVin(
            @Parameter(description = "VIN") @PathVariable String vin) {
        return fleetService.getFleetByVin(vin)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/maintenance-due")
    @Operation(summary = "Get fleet vehicles due for maintenance", description = "Retrieves fleet vehicles that are due for maintenance")
    public ResponseEntity<List<Fleet>> getFleetDueForMaintenance() {
        List<Fleet> fleet = fleetService.getFleetDueForMaintenance();
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/insurance-expiring")
    @Operation(summary = "Get fleet vehicles with expiring insurance", description = "Retrieves fleet vehicles with insurance expiring soon")
    public ResponseEntity<List<Fleet>> getFleetWithExpiringInsurance() {
        List<Fleet> fleet = fleetService.getFleetWithExpiringInsurance();
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/registration-expiring")
    @Operation(summary = "Get fleet vehicles with expiring registration", description = "Retrieves fleet vehicles with registration expiring soon")
    public ResponseEntity<List<Fleet>> getFleetWithExpiringRegistration() {
        List<Fleet> fleet = fleetService.getFleetWithExpiringRegistration();
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/low-fuel")
    @Operation(summary = "Get fleet vehicles with low fuel", description = "Retrieves fleet vehicles with low fuel levels")
    public ResponseEntity<List<Fleet>> getFleetWithLowFuel() {
        List<Fleet> fleet = fleetService.getFleetWithLowFuel();
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/search")
    @Operation(summary = "Search fleet vehicles", description = "Searches fleet vehicles by term with pagination")
    public ResponseEntity<Page<Fleet>> searchFleet(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            Pageable pageable) {
        Page<Fleet> fleet = fleetService.searchFleet(searchTerm, pageable);
        return ResponseEntity.ok(fleet);
    }

    @GetMapping("/count/status/{status}")
    @Operation(summary = "Count fleet vehicles by status", description = "Returns the count of fleet vehicles with specified status")
    public ResponseEntity<Long> countFleetByStatus(
            @Parameter(description = "Fleet status") @PathVariable FleetStatus status) {
        long count = fleetService.countFleetByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/type/{type}")
    @Operation(summary = "Count fleet vehicles by type", description = "Returns the count of fleet vehicles with specified type")
    public ResponseEntity<Long> countFleetByType(
            @Parameter(description = "Fleet type") @PathVariable String type) {
        long count = fleetService.countFleetByType(
            com.smartwatts.facilityservice.model.FleetType.valueOf(type.toUpperCase()));
        return ResponseEntity.ok(count);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available fleet vehicles", description = "Retrieves all available fleet vehicles")
    public ResponseEntity<List<Fleet>> getAvailableFleet() {
        List<Fleet> fleet = fleetService.getAvailableFleet();
        return ResponseEntity.ok(fleet);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update fleet vehicle status", description = "Updates the status of a fleet vehicle")
    public ResponseEntity<Fleet> updateFleetStatus(
            @Parameter(description = "Fleet ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam FleetStatus status) {
        Fleet updatedFleet = fleetService.updateFleetStatus(id, status);
        return ResponseEntity.ok(updatedFleet);
    }

    @PatchMapping("/{id}/assign-driver")
    @Operation(summary = "Assign driver to fleet vehicle", description = "Assigns a driver to a fleet vehicle")
    public ResponseEntity<Fleet> assignDriver(
            @Parameter(description = "Fleet ID") @PathVariable Long id,
            @Parameter(description = "Driver ID") @RequestParam String driverId) {
        Fleet updatedFleet = fleetService.assignDriver(id, driverId);
        return ResponseEntity.ok(updatedFleet);
    }

    @PatchMapping("/{id}/unassign-driver")
    @Operation(summary = "Unassign driver from fleet vehicle", description = "Unassigns a driver from a fleet vehicle")
    public ResponseEntity<Fleet> unassignDriver(
            @Parameter(description = "Fleet ID") @PathVariable Long id) {
        Fleet updatedFleet = fleetService.unassignDriver(id);
        return ResponseEntity.ok(updatedFleet);
    }

    @PatchMapping("/{id}/fuel-level")
    @Operation(summary = "Update fuel level", description = "Updates the fuel level of a fleet vehicle")
    public ResponseEntity<Fleet> updateFuelLevel(
            @Parameter(description = "Fleet ID") @PathVariable Long id,
            @Parameter(description = "Fuel level") @RequestParam Double fuelLevel) {
        Fleet updatedFleet = fleetService.updateFuelLevel(id, fuelLevel);
        return ResponseEntity.ok(updatedFleet);
    }

    @PatchMapping("/{id}/mileage")
    @Operation(summary = "Update mileage", description = "Updates the mileage of a fleet vehicle")
    public ResponseEntity<Fleet> updateMileage(
            @Parameter(description = "Fleet ID") @PathVariable Long id,
            @Parameter(description = "Mileage") @RequestParam Long mileage) {
        Fleet updatedFleet = fleetService.updateMileage(id, mileage);
        return ResponseEntity.ok(updatedFleet);
    }

    @PatchMapping("/{id}/schedule-maintenance")
    @Operation(summary = "Schedule maintenance", description = "Schedules maintenance for a fleet vehicle")
    public ResponseEntity<Fleet> scheduleMaintenance(
            @Parameter(description = "Fleet ID") @PathVariable Long id,
            @Parameter(description = "Maintenance date") @RequestParam LocalDateTime maintenanceDate) {
        Fleet updatedFleet = fleetService.scheduleMaintenance(id, maintenanceDate);
        return ResponseEntity.ok(updatedFleet);
    }

    @PatchMapping("/{id}/complete-maintenance")
    @Operation(summary = "Complete maintenance", description = "Marks maintenance as completed for a fleet vehicle")
    public ResponseEntity<Fleet> completeMaintenance(
            @Parameter(description = "Fleet ID") @PathVariable Long id) {
        Fleet updatedFleet = fleetService.completeMaintenance(id);
        return ResponseEntity.ok(updatedFleet);
    }

    @GetMapping("/generate-vehicle-id")
    @Operation(summary = "Generate vehicle ID", description = "Generates a vehicle ID based on type, make, and model")
    public ResponseEntity<String> generateVehicleId(
            @Parameter(description = "Fleet type") @RequestParam String type,
            @Parameter(description = "Make") @RequestParam String make,
            @Parameter(description = "Model") @RequestParam String model) {
        String vehicleId = fleetService.generateVehicleId(
            com.smartwatts.facilityservice.model.FleetType.valueOf(type.toUpperCase()), make, model);
        return ResponseEntity.ok(vehicleId);
    }
}
