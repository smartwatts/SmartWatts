package com.smartwatts.facilityservice.repository;

import com.smartwatts.facilityservice.model.Fleet;
import com.smartwatts.facilityservice.model.FleetStatus;
import com.smartwatts.facilityservice.model.FleetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FleetRepository extends JpaRepository<Fleet, Long> {
    
    Optional<Fleet> findByVehicleId(String vehicleId);
    
    Optional<Fleet> findByIdAndIsActiveTrue(Long id);
    
    Optional<Fleet> findByVehicleIdAndIsActiveTrue(String vehicleId);
    
    List<Fleet> findByStatus(FleetStatus status);
    
    List<Fleet> findByStatusAndIsActiveTrue(FleetStatus status);
    
    List<Fleet> findByTypeAndIsActiveTrue(FleetType type);
    
    List<Fleet> findByDepartment(String department);
    
    List<Fleet> findByDepartmentAndIsActiveTrue(String department);
    
    List<Fleet> findByAssignedDriver(String assignedDriver);
    
    List<Fleet> findByAssignedDriverAndIsActiveTrue(String assignedDriver);
    
    List<Fleet> findByLocation(String location);
    
    List<Fleet> findByLocationAndIsActiveTrue(String location);
    
    Optional<Fleet> findByLicensePlate(String licensePlate);
    
    Optional<Fleet> findByLicensePlateAndIsActiveTrue(String licensePlate);
    
    Optional<Fleet> findByVin(String vin);
    
    Optional<Fleet> findByVinAndIsActiveTrue(String vin);
    
    List<Fleet> findByIsActiveTrue();
    
    Page<Fleet> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT f FROM Fleet f WHERE f.nextMaintenanceDate <= :maintenanceDate AND f.isActive = true")
    List<Fleet> findVehiclesDueForMaintenance(@Param("maintenanceDate") LocalDate maintenanceDate);
    
    @Query("SELECT f FROM Fleet f WHERE f.insuranceExpiryDate <= :expiryDate AND f.isActive = true")
    List<Fleet> findVehiclesWithExpiringInsurance(@Param("expiryDate") LocalDate expiryDate);
    
    @Query("SELECT f FROM Fleet f WHERE f.registrationExpiryDate <= :expiryDate AND f.isActive = true")
    List<Fleet> findVehiclesWithExpiringRegistration(@Param("expiryDate") LocalDate expiryDate);
    
    @Query("SELECT f FROM Fleet f WHERE f.currentFuelLevel <= :fuelThreshold AND f.isActive = true")
    List<Fleet> findVehiclesWithLowFuel(@Param("fuelThreshold") BigDecimal fuelThreshold);
    
    @Query("SELECT f FROM Fleet f WHERE f.licensePlate LIKE %:searchTerm% OR f.name LIKE %:searchTerm% OR f.make LIKE %:searchTerm% OR f.model LIKE %:searchTerm%")
    Page<Fleet> searchFleet(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.status = :status AND f.isActive = true")
    Long countByStatusAndIsActiveTrue(@Param("status") FleetStatus status);
    
    @Query("SELECT COUNT(f) FROM Fleet f WHERE f.type = :type AND f.isActive = true")
    Long countByTypeAndIsActiveTrue(@Param("type") FleetType type);
    
    @Query("SELECT f.department, COUNT(f) FROM Fleet f WHERE f.isActive = true GROUP BY f.department")
    List<Object[]> countFleetByDepartment();
    
    @Query("SELECT f.assignedDriver, COUNT(f) FROM Fleet f WHERE f.isActive = true GROUP BY f.assignedDriver")
    List<Object[]> countFleetByDriver();
    
    @Query("SELECT f FROM Fleet f WHERE f.mileage >= :minMileage AND f.isActive = true ORDER BY f.mileage DESC")
    List<Fleet> findHighMileageVehicles(@Param("minMileage") Long minMileage);
    
    @Query("SELECT f FROM Fleet f WHERE f.currentFuelLevel <= f.fuelCapacity * 0.1 AND f.isActive = true")
    List<Fleet> findVehiclesNeedingFuel();
}
