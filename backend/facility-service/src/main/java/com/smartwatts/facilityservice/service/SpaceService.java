package com.smartwatts.facilityservice.service;

import com.smartwatts.facilityservice.model.Space;
import com.smartwatts.facilityservice.model.SpaceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SpaceService {

    // CRUD operations
    Space createSpace(Space space);
    Space updateSpace(Long id, Space spaceDetails);
    void deleteSpace(Long id);
    
    // Retrieval operations
    Optional<Space> getSpaceById(Long id);
    Optional<Space> getSpaceByCode(String spaceCode);
    List<Space> getAllSpaces();
    Page<Space> getSpaces(Pageable pageable);
    
    // Filtering operations
    List<Space> getSpacesByStatus(SpaceStatus status);
    List<Space> getSpacesByType(com.smartwatts.facilityservice.model.SpaceType type);
    List<Space> getSpacesByBuilding(String building);
    List<Space> getSpacesByFloor(String floor);
    List<Space> getSpacesByDepartment(String department);
    List<Space> getSpacesByAssignedUser(String userId);
    List<Space> getSpacesByBuildingAndFloor(String building, String floor);
    List<Space> getSpacesByMinimumCapacity(int minCapacity);
    List<Space> getSpacesByMinimumArea(double minArea);
    
    // Search operations
    Page<Space> searchSpaces(String searchTerm, Pageable pageable);
    
    // Counting operations
    long countSpacesByStatus(SpaceStatus status);
    long countSpacesByType(com.smartwatts.facilityservice.model.SpaceType type);
    
    // Business operations
    List<Space> getAvailableSpaces();
    Space updateSpaceStatus(Long id, SpaceStatus status);
    Space assignSpaceToUser(Long id, String userId);
    Space unassignSpace(Long id);
    Space reserveSpace(Long id, String userId);
    
    // Utility operations
    String generateSpaceCode(String building, String floor, String roomNumber);
}
