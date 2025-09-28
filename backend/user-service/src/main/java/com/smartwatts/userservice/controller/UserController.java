package com.smartwatts.userservice.controller;

import com.smartwatts.userservice.dto.AuthRequest;
import com.smartwatts.userservice.dto.AuthResponse;
import com.smartwatts.userservice.dto.UserDto;
import com.smartwatts.userservice.model.Role;

import com.smartwatts.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user management and authentication")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        log.info("Registering new user: {}", userDto.getUsername());
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT tokens")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("User login attempt: {}", authRequest.getUsernameOrEmail());
        AuthResponse authResponse = userService.authenticate(authRequest);
        return ResponseEntity.ok(authResponse);
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user")
    public ResponseEntity<UserDto> getCurrentUserProfile() {
        log.info("Fetching current user profile");
        UserDto user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching user with ID: {}", userId);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieves a specific user by their username")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        log.info("Fetching user with username: {}", username);
        UserDto user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves a specific user by their email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserByEmail(
            @Parameter(description = "Email") @PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        log.info("Fetching all users");
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieves all users with a specific role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getUsersByRole(
            @Parameter(description = "User role") @PathVariable Role.RoleName role,
            Pageable pageable) {
        log.info("Fetching users with role: {}", role);
        Page<UserDto> users = userService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Updates an existing user")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.username")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Valid @RequestBody UserDto userDto) {
        log.info("Updating user with ID: {}", userId);
        UserDto updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role", description = "Updates the role of a specific user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "New role") @RequestParam Role.RoleName role) {
        log.info("Updating user role to: {} for user: {}", role, userId);
        UserDto updatedUser = userService.updateUserRole(userId, role);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{userId}/activate")
    @Operation(summary = "Activate user", description = "Activates a deactivated user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> activateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Activating user with ID: {}", userId);
        UserDto activatedUser = userService.activateUser(userId);
        return ResponseEntity.ok(activatedUser);
    }
    
    @PutMapping("/{userId}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates an active user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> deactivateUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Deactivating user with ID: {}", userId);
        UserDto deactivatedUser = userService.deactivateUser(userId);
        return ResponseEntity.ok(deactivatedUser);
    }
    
    @GetMapping("/count/role/{role}")
    @Operation(summary = "Get user count by role", description = "Returns the count of users with a specific role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getUserCountByRole(
            @Parameter(description = "User role") @PathVariable Role.RoleName role) {
        log.info("Counting users with role: {}", role);
        long count = userService.getUserCountByRole(role);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/count/active")
    @Operation(summary = "Get active user count", description = "Returns the count of active users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveUserCount() {
        log.info("Counting active users");
        long count = userService.getActiveUserCount();
        return ResponseEntity.ok(count);
    }
    
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Refreshes the access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        log.info("Refreshing access token");
        AuthResponse authResponse = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Initiates password reset process")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        log.info("Password reset requested for email: {}", email);
        userService.forgotPassword(email);
        return ResponseEntity.ok("Password reset email sent");
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Resets password using reset token")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        log.info("Password reset with token");
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successful");
    }
} 