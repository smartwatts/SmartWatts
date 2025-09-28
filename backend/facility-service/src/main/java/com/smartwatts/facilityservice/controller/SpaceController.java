package com.smartwatts.facilityservice.controller;

import com.smartwatts.facilityservice.model.Space;
import com.smartwatts.facilityservice.model.SpaceStatus;
import com.smartwatts.facilityservice.service.SpaceService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Space Management", description = "APIs for managing facility spaces")
public class SpaceController {

    private final SpaceService spaceService;

    @PostMapping
    @Operation(summary = "Create a new space", description = "Creates a new facility space")
    public ResponseEntity<Space> createSpace(@RequestBody Space space) {
        log.info("Creating new space: {}", space.getName());
        Space createdSpace = spaceService.createSpace(space);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSpace);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a space", description = "Updates an existing facility space")
    public ResponseEntity<Space> updateSpace(
            @Parameter(description = "Space ID") @PathVariable Long id,
            @RequestBody Space spaceDetails) {
        log.info("Updating space with ID: {}", id);
        Space updatedSpace = spaceService.updateSpace(id, spaceDetails);
        return ResponseEntity.ok(updatedSpace);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a space", description = "Soft deletes a facility space")
    public ResponseEntity<Void> deleteSpace(
            @Parameter(description = "Space ID") @PathVariable Long id) {
        log.info("Deleting space with ID: {}", id);
        spaceService.deleteSpace(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get space by ID", description = "Retrieves a space by its ID")
    public ResponseEntity<Space> getSpaceById(
            @Parameter(description = "Space ID") @PathVariable Long id) {
        return spaceService.getSpaceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{spaceCode}")
    @Operation(summary = "Get space by code", description = "Retrieves a space by its code")
    public ResponseEntity<Space> getSpaceByCode(
            @Parameter(description = "Space code") @PathVariable String spaceCode) {
        return spaceService.getSpaceByCode(spaceCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all spaces", description = "Retrieves all spaces with pagination")
    public ResponseEntity<Page<Space>> getAllSpaces(Pageable pageable) {
        Page<Space> spaces = spaceService.getSpaces(pageable);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all spaces without pagination", description = "Retrieves all spaces as a list")
    public ResponseEntity<List<Space>> getAllSpacesList() {
        List<Space> spaces = spaceService.getAllSpaces();
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get spaces by status", description = "Retrieves spaces filtered by status")
    public ResponseEntity<List<Space>> getSpacesByStatus(
            @Parameter(description = "Space status") @PathVariable SpaceStatus status) {
        List<Space> spaces = spaceService.getSpacesByStatus(status);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get spaces by type", description = "Retrieves spaces filtered by type")
    public ResponseEntity<List<Space>> getSpacesByType(
            @Parameter(description = "Space type") @PathVariable String type) {
        List<Space> spaces = spaceService.getSpacesByType(
            com.smartwatts.facilityservice.model.SpaceType.valueOf(type.toUpperCase()));
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/building/{building}")
    @Operation(summary = "Get spaces by building", description = "Retrieves spaces filtered by building")
    public ResponseEntity<List<Space>> getSpacesByBuilding(
            @Parameter(description = "Building name") @PathVariable String building) {
        List<Space> spaces = spaceService.getSpacesByBuilding(building);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/floor/{floor}")
    @Operation(summary = "Get spaces by floor", description = "Retrieves spaces filtered by floor")
    public ResponseEntity<List<Space>> getSpacesByFloor(
            @Parameter(description = "Floor name") @PathVariable String floor) {
        List<Space> spaces = spaceService.getSpacesByFloor(floor);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get spaces by department", description = "Retrieves spaces filtered by department")
    public ResponseEntity<List<Space>> getSpacesByDepartment(
            @Parameter(description = "Department name") @PathVariable String department) {
        List<Space> spaces = spaceService.getSpacesByDepartment(department);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/assigned/{userId}")
    @Operation(summary = "Get spaces by assigned user", description = "Retrieves spaces assigned to a specific user")
    public ResponseEntity<List<Space>> getSpacesByAssignedUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        List<Space> spaces = spaceService.getSpacesByAssignedUser(userId);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/building/{building}/floor/{floor}")
    @Operation(summary = "Get spaces by building and floor", description = "Retrieves spaces filtered by building and floor")
    public ResponseEntity<List<Space>> getSpacesByBuildingAndFloor(
            @Parameter(description = "Building name") @PathVariable String building,
            @Parameter(description = "Floor name") @PathVariable String floor) {
        List<Space> spaces = spaceService.getSpacesByBuildingAndFloor(building, floor);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/capacity/{minCapacity}")
    @Operation(summary = "Get spaces by minimum capacity", description = "Retrieves spaces with capacity greater than or equal to specified value")
    public ResponseEntity<List<Space>> getSpacesByMinimumCapacity(
            @Parameter(description = "Minimum capacity") @PathVariable int minCapacity) {
        List<Space> spaces = spaceService.getSpacesByMinimumCapacity(minCapacity);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/area/{minArea}")
    @Operation(summary = "Get spaces by minimum area", description = "Retrieves spaces with area greater than or equal to specified value")
    public ResponseEntity<List<Space>> getSpacesByMinimumArea(
            @Parameter(description = "Minimum area") @PathVariable double minArea) {
        List<Space> spaces = spaceService.getSpacesByMinimumArea(minArea);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/search")
    @Operation(summary = "Search spaces", description = "Searches spaces by term with pagination")
    public ResponseEntity<Page<Space>> searchSpaces(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            Pageable pageable) {
        Page<Space> spaces = spaceService.searchSpaces(searchTerm, pageable);
        return ResponseEntity.ok(spaces);
    }

    @GetMapping("/count/status/{status}")
    @Operation(summary = "Count spaces by status", description = "Returns the count of spaces with specified status")
    public ResponseEntity<Long> countSpacesByStatus(
            @Parameter(description = "Space status") @PathVariable SpaceStatus status) {
        long count = spaceService.countSpacesByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/type/{type}")
    @Operation(summary = "Count spaces by type", description = "Returns the count of spaces with specified type")
    public ResponseEntity<Long> countSpacesByType(
            @Parameter(description = "Space type") @PathVariable String type) {
        long count = spaceService.countSpacesByType(
            com.smartwatts.facilityservice.model.SpaceType.valueOf(type.toUpperCase()));
        return ResponseEntity.ok(count);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available spaces", description = "Retrieves all available spaces")
    public ResponseEntity<List<Space>> getAvailableSpaces() {
        List<Space> spaces = spaceService.getAvailableSpaces();
        return ResponseEntity.ok(spaces);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update space status", description = "Updates the status of a space")
    public ResponseEntity<Space> updateSpaceStatus(
            @Parameter(description = "Space ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam SpaceStatus status) {
        Space updatedSpace = spaceService.updateSpaceStatus(id, status);
        return ResponseEntity.ok(updatedSpace);
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign space to user", description = "Assigns a space to a specific user")
    public ResponseEntity<Space> assignSpaceToUser(
            @Parameter(description = "Space ID") @PathVariable Long id,
            @Parameter(description = "User ID") @RequestParam String userId) {
        Space updatedSpace = spaceService.assignSpaceToUser(id, userId);
        return ResponseEntity.ok(updatedSpace);
    }

    @PatchMapping("/{id}/unassign")
    @Operation(summary = "Unassign space", description = "Unassigns a space from its current user")
    public ResponseEntity<Space> unassignSpace(
            @Parameter(description = "Space ID") @PathVariable Long id) {
        Space updatedSpace = spaceService.unassignSpace(id);
        return ResponseEntity.ok(updatedSpace);
    }

    @PatchMapping("/{id}/reserve")
    @Operation(summary = "Reserve space", description = "Reserves a space for a specific user")
    public ResponseEntity<Space> reserveSpace(
            @Parameter(description = "Space ID") @PathVariable Long id,
            @Parameter(description = "User ID") @RequestParam String userId) {
        Space updatedSpace = spaceService.reserveSpace(id, userId);
        return ResponseEntity.ok(updatedSpace);
    }

    @GetMapping("/generate-code")
    @Operation(summary = "Generate space code", description = "Generates a space code based on building, floor, and room")
    public ResponseEntity<String> generateSpaceCode(
            @Parameter(description = "Building name") @RequestParam String building,
            @Parameter(description = "Floor name") @RequestParam String floor,
            @Parameter(description = "Room number") @RequestParam String roomNumber) {
        String spaceCode = spaceService.generateSpaceCode(building, floor, roomNumber);
        return ResponseEntity.ok(spaceCode);
    }
}
