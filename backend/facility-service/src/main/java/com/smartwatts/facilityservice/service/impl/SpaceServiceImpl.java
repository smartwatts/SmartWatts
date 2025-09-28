package com.smartwatts.facilityservice.service.impl;

import com.smartwatts.facilityservice.model.Space;
import com.smartwatts.facilityservice.model.SpaceStatus;
import com.smartwatts.facilityservice.model.SpaceType;
import com.smartwatts.facilityservice.repository.SpaceRepository;
import com.smartwatts.facilityservice.service.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SpaceServiceImpl implements SpaceService {

    private final SpaceRepository spaceRepository;

    @Autowired
    public SpaceServiceImpl(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    @Override
    public Space createSpace(Space space) {
        space.setCreatedAt(LocalDateTime.now());
        space.setUpdatedAt(LocalDateTime.now());
        space.setIsActive(true);
        
        // Generate unique space code if not provided
        if (space.getSpaceCode() == null || space.getSpaceCode().isEmpty()) {
            space.setSpaceCode(generateSpaceCode(space.getBuilding(), space.getFloor(), space.getRoomNumber()));
        }
        
        return spaceRepository.save(space);
    }

    @Override
    public Space updateSpace(Long id, Space spaceDetails) {
        Optional<Space> spaceOpt = spaceRepository.findByIdAndIsActiveTrue(id);
        if (spaceOpt.isPresent()) {
            Space space = spaceOpt.get();
            
            if (spaceDetails.getName() != null) space.setName(spaceDetails.getName());
            if (spaceDetails.getDescription() != null) space.setDescription(spaceDetails.getDescription());
            if (spaceDetails.getType() != null) space.setType(spaceDetails.getType());
            if (spaceDetails.getStatus() != null) space.setStatus(spaceDetails.getStatus());
            if (spaceDetails.getBuilding() != null) space.setBuilding(spaceDetails.getBuilding());
            if (spaceDetails.getFloor() != null) space.setFloor(spaceDetails.getFloor());
            if (spaceDetails.getRoomNumber() != null) space.setRoomNumber(spaceDetails.getRoomNumber());
            if (spaceDetails.getDepartment() != null) space.setDepartment(spaceDetails.getDepartment());
            if (spaceDetails.getAssignedTo() != null) space.setAssignedTo(spaceDetails.getAssignedTo());
            if (spaceDetails.getCapacity() != null) space.setCapacity(spaceDetails.getCapacity());
            if (spaceDetails.getArea() != null) space.setArea(spaceDetails.getArea());
            if (spaceDetails.getPhone() != null) space.setPhone(spaceDetails.getPhone());
            if (spaceDetails.getEmail() != null) space.setEmail(spaceDetails.getEmail());
            if (spaceDetails.getNotes() != null) space.setNotes(spaceDetails.getNotes());
            
            space.setUpdatedAt(LocalDateTime.now());
            return spaceRepository.save(space);
        }
        throw new RuntimeException("Space not found");
    }

    @Override
    public void deleteSpace(Long id) {
        Optional<Space> spaceOpt = spaceRepository.findByIdAndIsActiveTrue(id);
        if (spaceOpt.isPresent()) {
            Space space = spaceOpt.get();
            space.setIsActive(false);
            space.setUpdatedAt(LocalDateTime.now());
            spaceRepository.save(space);
        }
    }

    @Override
    public Optional<Space> getSpaceById(Long id) {
        return spaceRepository.findByIdAndIsActiveTrue(id);
    }

    @Override
    public Optional<Space> getSpaceByCode(String spaceCode) {
        return spaceRepository.findBySpaceCodeAndIsActiveTrue(spaceCode);
    }

    @Override
    public List<Space> getAllSpaces() {
        return spaceRepository.findByIsActiveTrue();
    }

    @Override
    public Page<Space> getSpaces(Pageable pageable) {
        return spaceRepository.findByIsActiveTrue(pageable);
    }

    @Override
    public List<Space> getSpacesByStatus(SpaceStatus status) {
        return spaceRepository.findByStatusAndIsActiveTrue(status);
    }

    @Override
    public List<Space> getSpacesByType(SpaceType type) {
        return spaceRepository.findByTypeAndIsActiveTrue(type);
    }

    @Override
    public List<Space> getSpacesByBuilding(String building) {
        return spaceRepository.findByBuildingAndIsActiveTrue(building);
    }

    @Override
    public List<Space> getSpacesByFloor(String floor) {
        return spaceRepository.findByFloorAndIsActiveTrue(floor);
    }

    @Override
    public List<Space> getSpacesByDepartment(String department) {
        return spaceRepository.findByDepartmentAndIsActiveTrue(department);
    }

    @Override
    public List<Space> getSpacesByAssignedUser(String userId) {
        return spaceRepository.findByAssignedToAndIsActiveTrue(userId);
    }

    @Override
    public List<Space> getSpacesByBuildingAndFloor(String building, String floor) {
        return spaceRepository.findByBuildingAndFloorAndIsActiveTrue(building, floor);
    }

    @Override
    public List<Space> getSpacesByMinimumCapacity(int minCapacity) {
        return spaceRepository.findByCapacityGreaterThanEqualAndIsActiveTrue(minCapacity);
    }

    @Override
    public List<Space> getSpacesByMinimumArea(double minArea) {
        return spaceRepository.findByAreaGreaterThanEqualAndIsActiveTrue(BigDecimal.valueOf(minArea));
    }

    @Override
    public Page<Space> searchSpaces(String searchTerm, Pageable pageable) {
        return spaceRepository.searchSpaces(searchTerm, pageable);
    }

    @Override
    public long countSpacesByStatus(SpaceStatus status) {
        return spaceRepository.countByStatusAndIsActiveTrue(status);
    }

    @Override
    public long countSpacesByType(SpaceType type) {
        return spaceRepository.countByTypeAndIsActiveTrue(type);
    }

    @Override
    public List<Space> getAvailableSpaces() {
        return spaceRepository.findByStatusAndIsActiveTrue(SpaceStatus.AVAILABLE);
    }

    @Override
    public Space updateSpaceStatus(Long id, SpaceStatus status) {
        Optional<Space> spaceOpt = spaceRepository.findByIdAndIsActiveTrue(id);
        if (spaceOpt.isPresent()) {
            Space space = spaceOpt.get();
            space.setStatus(status);
            space.setUpdatedAt(LocalDateTime.now());
            return spaceRepository.save(space);
        }
        throw new RuntimeException("Space not found");
    }

    @Override
    public Space assignSpaceToUser(Long id, String userId) {
        Optional<Space> spaceOpt = spaceRepository.findByIdAndIsActiveTrue(id);
        if (spaceOpt.isPresent()) {
            Space space = spaceOpt.get();
            space.setAssignedTo(userId);
            space.setUpdatedAt(LocalDateTime.now());
            return spaceRepository.save(space);
        }
        throw new RuntimeException("Space not found");
    }

    @Override
    public Space unassignSpace(Long id) {
        Optional<Space> spaceOpt = spaceRepository.findByIdAndIsActiveTrue(id);
        if (spaceOpt.isPresent()) {
            Space space = spaceOpt.get();
            space.setAssignedTo(null);
            space.setUpdatedAt(LocalDateTime.now());
            return spaceRepository.save(space);
        }
        throw new RuntimeException("Space not found");
    }

    @Override
    public Space reserveSpace(Long id, String userId) {
        Optional<Space> spaceOpt = spaceRepository.findByIdAndIsActiveTrue(id);
        if (spaceOpt.isPresent()) {
            Space space = spaceOpt.get();
            space.setAssignedTo(userId);
            space.setStatus(SpaceStatus.RESERVED);
            space.setUpdatedAt(LocalDateTime.now());
            return spaceRepository.save(space);
        }
        throw new RuntimeException("Space not found");
    }

    @Override
    public String generateSpaceCode(String building, String floor, String roomNumber) {
        String buildingCode = building != null ? building.substring(0, Math.min(3, building.length())).toUpperCase() : "BLD";
        String floorCode = floor != null ? floor.substring(0, Math.min(2, floor.length())).toUpperCase() : "00";
        String roomCode = roomNumber != null ? roomNumber.substring(0, Math.min(3, roomNumber.length())).toUpperCase() : "000";
        String uniqueId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        return String.format("%s-%s-%s-%s", buildingCode, floorCode, roomCode, uniqueId);
    }
}
