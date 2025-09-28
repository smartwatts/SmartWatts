package com.smartwatts.facilityservice.repository;

import com.smartwatts.facilityservice.model.Space;
import com.smartwatts.facilityservice.model.SpaceStatus;
import com.smartwatts.facilityservice.model.SpaceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    
    Optional<Space> findBySpaceCode(String spaceCode);
    
    Optional<Space> findByIdAndIsActiveTrue(Long id);
    
    Optional<Space> findBySpaceCodeAndIsActiveTrue(String spaceCode);
    
    List<Space> findByStatus(SpaceStatus status);
    
    List<Space> findByStatusAndIsActiveTrue(SpaceStatus status);
    
    List<Space> findByTypeAndIsActiveTrue(SpaceType type);
    
    List<Space> findByBuilding(String building);
    
    List<Space> findByBuildingAndIsActiveTrue(String building);
    
    List<Space> findByFloor(String floor);
    
    List<Space> findByFloorAndIsActiveTrue(String floor);
    
    List<Space> findByDepartment(String department);
    
    List<Space> findByDepartmentAndIsActiveTrue(String department);
    
    List<Space> findByAssignedTo(String assignedTo);
    
    List<Space> findByAssignedToAndIsActiveTrue(String assignedTo);
    
    List<Space> findByIsActiveTrue();
    
    Page<Space> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT s FROM Space s WHERE s.building = :building AND s.floor = :floor AND s.isActive = true")
    List<Space> findByBuildingAndFloorAndIsActiveTrue(@Param("building") String building, @Param("floor") String floor);
    
    @Query("SELECT s FROM Space s WHERE s.capacity >= :minCapacity AND s.isActive = true")
    List<Space> findByCapacityGreaterThanEqualAndIsActiveTrue(@Param("minCapacity") Integer minCapacity);
    
    @Query("SELECT s FROM Space s WHERE s.area >= :minArea AND s.isActive = true")
    List<Space> findByAreaGreaterThanEqualAndIsActiveTrue(@Param("minArea") BigDecimal minArea);
    
    @Query("SELECT s FROM Space s WHERE s.spaceCode LIKE %:searchTerm% OR s.name LIKE %:searchTerm% OR s.description LIKE %:searchTerm% OR s.building LIKE %:searchTerm%")
    Page<Space> searchSpaces(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM Space s WHERE s.status = :status AND s.isActive = true")
    Long countByStatusAndIsActiveTrue(@Param("status") SpaceStatus status);
    
    @Query("SELECT COUNT(s) FROM Space s WHERE s.type = :type AND s.isActive = true")
    Long countByTypeAndIsActiveTrue(@Param("type") SpaceType type);
    
    @Query("SELECT s.building, COUNT(s) FROM Space s WHERE s.isActive = true GROUP BY s.building")
    List<Object[]> countSpacesByBuilding();
    
    @Query("SELECT s.department, COUNT(s) FROM Space s WHERE s.isActive = true GROUP BY s.department")
    List<Object[]> countSpacesByDepartment();
    
    @Query("SELECT s FROM Space s WHERE s.status = 'AVAILABLE' AND s.isActive = true ORDER BY s.name ASC")
    List<Space> findAvailableSpaces();
    
    @Query("SELECT s FROM Space s WHERE s.capacity >= :minCapacity AND s.status = 'AVAILABLE' AND s.isActive = true")
    List<Space> findAvailableSpacesWithCapacity(@Param("minCapacity") Integer minCapacity);
    
    @Query("SELECT s FROM Space s WHERE s.area >= :minArea AND s.status = 'AVAILABLE' AND s.isActive = true")
    List<Space> findAvailableSpacesWithArea(@Param("minArea") BigDecimal minArea);
}
