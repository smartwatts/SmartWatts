package com.smartwatts.facilityservice.service.impl;

import com.smartwatts.facilityservice.model.Fleet;
import com.smartwatts.facilityservice.model.FleetStatus;
import com.smartwatts.facilityservice.model.FleetType;
import com.smartwatts.facilityservice.repository.FleetRepository;
import com.smartwatts.facilityservice.service.FleetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FleetServiceImpl implements FleetService {

    private final FleetRepository fleetRepository;

    @Autowired
    public FleetServiceImpl(FleetRepository fleetRepository) {
        this.fleetRepository = fleetRepository;
    }

    @Override
    public Fleet createFleet(Fleet fleet) {
        fleet.setCreatedAt(LocalDateTime.now());
        fleet.setUpdatedAt(LocalDateTime.now());
        fleet.setIsActive(true);
        
        // Generate unique vehicle ID if not provided
        if (fleet.getVehicleId() == null || fleet.getVehicleId().isEmpty()) {
            fleet.setVehicleId(generateVehicleId(fleet.getType(), fleet.getMake(), fleet.getModel()));
        }
        
        return fleetRepository.save(fleet);
    }

    @Override
    public Fleet updateFleet(Long id, Fleet fleetDetails) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            
            if (fleetDetails.getMake() != null) fleet.setMake(fleetDetails.getMake());
            if (fleetDetails.getModel() != null) fleet.setModel(fleetDetails.getModel());
            if (fleetDetails.getModelYear() != null) fleet.setModelYear(fleetDetails.getModelYear());
            if (fleetDetails.getLicensePlate() != null) fleet.setLicensePlate(fleetDetails.getLicensePlate());
            if (fleetDetails.getVin() != null) fleet.setVin(fleetDetails.getVin());
            if (fleetDetails.getType() != null) fleet.setType(fleetDetails.getType());
            if (fleetDetails.getStatus() != null) fleet.setStatus(fleetDetails.getStatus());
            if (fleetDetails.getDepartment() != null) fleet.setDepartment(fleetDetails.getDepartment());
            if (fleetDetails.getAssignedDriver() != null) fleet.setAssignedDriver(fleetDetails.getAssignedDriver());
            if (fleetDetails.getLocation() != null) fleet.setLocation(fleetDetails.getLocation());
            if (fleetDetails.getMileage() != null) fleet.setMileage(fleetDetails.getMileage());
            if (fleetDetails.getCurrentFuelLevel() != null) fleet.setCurrentFuelLevel(fleetDetails.getCurrentFuelLevel());
            if (fleetDetails.getInsuranceProvider() != null) fleet.setInsuranceProvider(fleetDetails.getInsuranceProvider());
            if (fleetDetails.getInsuranceExpiryDate() != null) fleet.setInsuranceExpiryDate(fleetDetails.getInsuranceExpiryDate());
            if (fleetDetails.getRegistrationNumber() != null) fleet.setRegistrationNumber(fleetDetails.getRegistrationNumber());
            if (fleetDetails.getRegistrationExpiryDate() != null) fleet.setRegistrationExpiryDate(fleetDetails.getRegistrationExpiryDate());
            if (fleetDetails.getLastMaintenanceDate() != null) fleet.setLastMaintenanceDate(fleetDetails.getLastMaintenanceDate());
            if (fleetDetails.getNextMaintenanceDate() != null) fleet.setNextMaintenanceDate(fleetDetails.getNextMaintenanceDate());
            if (fleetDetails.getPurchaseCost() != null) fleet.setPurchaseCost(fleetDetails.getPurchaseCost());
            if (fleetDetails.getCurrentValue() != null) fleet.setCurrentValue(fleetDetails.getCurrentValue());
            if (fleetDetails.getNotes() != null) fleet.setNotes(fleetDetails.getNotes());
            
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public void deleteFleet(Long id) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setIsActive(false);
            fleet.setUpdatedAt(LocalDateTime.now());
            fleetRepository.save(fleet);
        }
    }

    @Override
    public Optional<Fleet> getFleetById(Long id) {
        return fleetRepository.findByIdAndIsActiveTrue(id);
    }

    @Override
    public Optional<Fleet> getFleetByVehicleId(String vehicleId) {
        return fleetRepository.findByVehicleIdAndIsActiveTrue(vehicleId);
    }

    @Override
    public List<Fleet> getAllFleet() {
        return fleetRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Fleet> getFleet(Pageable pageable) {
        return fleetRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public List<Fleet> getFleetByStatus(FleetStatus status) {
        return fleetRepository.findByStatusAndIsActiveTrue(status);
    }

    @Override
    public List<Fleet> getFleetByType(FleetType type) {
        return fleetRepository.findByTypeAndIsActiveTrue(type);
    }

    @Override
    public List<Fleet> getFleetByDepartment(String department) {
        return fleetRepository.findByDepartmentAndIsActiveTrue(department);
    }

    @Override
    public List<Fleet> getFleetByAssignedDriver(String driverId) {
        return fleetRepository.findByAssignedDriverAndIsActiveTrue(driverId);
    }

    @Override
    public List<Fleet> getFleetByLocation(String location) {
        return fleetRepository.findByLocationAndIsActiveTrue(location);
    }

    @Override
    public Optional<Fleet> getFleetByLicensePlate(String licensePlate) {
        return fleetRepository.findByLicensePlateAndIsActiveTrue(licensePlate);
    }

    @Override
    public Optional<Fleet> getFleetByVin(String vin) {
        return fleetRepository.findByVinAndIsActiveTrue(vin);
    }

    @Override
    public List<Fleet> getFleetDueForMaintenance() {
        return fleetRepository.findVehiclesDueForMaintenance(LocalDate.now());
    }

    @Override
    public List<Fleet> getFleetWithExpiringInsurance() {
        LocalDate expiryDate = LocalDate.now().plusDays(30);
        return fleetRepository.findVehiclesWithExpiringInsurance(expiryDate);
    }

    @Override
    public List<Fleet> getFleetWithExpiringRegistration() {
        LocalDate expiryDate = LocalDate.now().plusDays(30);
        return fleetRepository.findVehiclesWithExpiringRegistration(expiryDate);
    }

    @Override
    public List<Fleet> getFleetWithLowFuel() {
        // Use 20% of fuel capacity as threshold
        BigDecimal fuelThreshold = new BigDecimal("0.2");
        return fleetRepository.findVehiclesWithLowFuel(fuelThreshold);
    }

    @Override
    public Page<Fleet> searchFleet(String searchTerm, Pageable pageable) {
        return fleetRepository.searchFleet(searchTerm, pageable);
    }

    @Override
    public long countFleetByStatus(FleetStatus status) {
        return fleetRepository.countByStatusAndIsActiveTrue(status);
    }

    @Override
    public long countFleetByType(FleetType type) {
        return fleetRepository.countByTypeAndIsActiveTrue(type);
    }

    @Override
    public List<Fleet> getAvailableFleet() {
        return fleetRepository.findByStatusAndIsActiveTrue(FleetStatus.AVAILABLE);
    }

    @Override
    public Fleet updateFleetStatus(Long id, FleetStatus status) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setStatus(status);
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public Fleet assignDriver(Long id, String driverId) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setAssignedDriver(driverId);
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public Fleet unassignDriver(Long id) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setAssignedDriver(null);
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public Fleet updateFuelLevel(Long id, Double fuelLevel) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setCurrentFuelLevel(BigDecimal.valueOf(fuelLevel));
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public Fleet updateMileage(Long id, Long mileage) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setMileage(mileage.intValue());
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public Fleet scheduleMaintenance(Long id, LocalDateTime maintenanceDate) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setNextMaintenanceDate(maintenanceDate);
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public Fleet completeMaintenance(Long id) {
        Optional<Fleet> fleetOpt = fleetRepository.findByIdAndIsActiveTrue(id);
        if (fleetOpt.isPresent()) {
            Fleet fleet = fleetOpt.get();
            fleet.setLastMaintenanceDate(LocalDateTime.now());
            fleet.setUpdatedAt(LocalDateTime.now());
            return fleetRepository.save(fleet);
        }
        throw new RuntimeException("Fleet not found");
    }

    @Override
    public String generateVehicleId(FleetType type, String make, String model) {
        String prefix = type.name().substring(0, 3).toUpperCase();
        String makeCode = make != null ? make.substring(0, Math.min(3, make.length())).toUpperCase() : "UNK";
        String modelCode = model != null ? model.substring(0, Math.min(3, model.length())).toUpperCase() : "UNK";
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return String.format("%s-%s-%s-%s", prefix, makeCode, modelCode, uniqueId);
    }
}
