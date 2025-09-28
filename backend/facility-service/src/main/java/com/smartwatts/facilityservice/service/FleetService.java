package com.smartwatts.facilityservice.service;

import com.smartwatts.facilityservice.model.Fleet;
import com.smartwatts.facilityservice.model.FleetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FleetService {

    // CRUD operations
    Fleet createFleet(Fleet fleet);
    Fleet updateFleet(Long id, Fleet fleetDetails);
    void deleteFleet(Long id);
    
    // Retrieval operations
    Optional<Fleet> getFleetById(Long id);
    Optional<Fleet> getFleetByVehicleId(String vehicleId);
    List<Fleet> getAllFleet();
    Page<Fleet> getFleet(Pageable pageable);
    
    // Filtering operations
    List<Fleet> getFleetByStatus(FleetStatus status);
    List<Fleet> getFleetByType(com.smartwatts.facilityservice.model.FleetType type);
    List<Fleet> getFleetByDepartment(String department);
    List<Fleet> getFleetByAssignedDriver(String driverId);
    List<Fleet> getFleetByLocation(String location);
    Optional<Fleet> getFleetByLicensePlate(String licensePlate);
    Optional<Fleet> getFleetByVin(String vin);
    
    // Business-specific operations
    List<Fleet> getFleetDueForMaintenance();
    List<Fleet> getFleetWithExpiringInsurance();
    List<Fleet> getFleetWithExpiringRegistration();
    List<Fleet> getFleetWithLowFuel();
    
    // Search operations
    Page<Fleet> searchFleet(String searchTerm, Pageable pageable);
    
    // Counting operations
    long countFleetByStatus(FleetStatus status);
    long countFleetByType(com.smartwatts.facilityservice.model.FleetType type);
    
    // Business operations
    List<Fleet> getAvailableFleet();
    Fleet updateFleetStatus(Long id, FleetStatus status);
    Fleet assignDriver(Long id, String driverId);
    Fleet unassignDriver(Long id);
    Fleet updateFuelLevel(Long id, Double fuelLevel);
    Fleet updateMileage(Long id, Long mileage);
    Fleet scheduleMaintenance(Long id, LocalDateTime maintenanceDate);
    Fleet completeMaintenance(Long id);
    
    // Utility operations
    String generateVehicleId(com.smartwatts.facilityservice.model.FleetType type, String make, String model);
}
