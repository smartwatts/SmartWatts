package com.smartwatts.userservice.controller;

import com.smartwatts.userservice.dto.AccountDto;
import com.smartwatts.userservice.service.AccountService;
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
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Account Management", description = "APIs for business account management")
public class AccountController {
    
    private final AccountService accountService;
    
    @GetMapping
    @Operation(summary = "Get all accounts", description = "Retrieves all business accounts with pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AccountDto>> getAllAccounts(Pageable pageable) {
        log.info("Fetching all accounts");
        Page<AccountDto> accounts = accountService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account by ID", description = "Retrieves a specific account by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountDto> getAccountById(
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        log.info("Fetching account with ID: {}", accountId);
        AccountDto account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }
    
    @PostMapping
    @Operation(summary = "Create new account", description = "Creates a new business account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody AccountDto accountDto) {
        log.info("Creating new account: {}", accountDto.getName());
        AccountDto createdAccount = accountService.createAccount(accountDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }
    
    @PutMapping("/{accountId}")
    @Operation(summary = "Update account", description = "Updates an existing account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountDto> updateAccount(
            @Parameter(description = "Account ID") @PathVariable UUID accountId,
            @Valid @RequestBody AccountDto accountDto) {
        log.info("Updating account with ID: {}", accountId);
        AccountDto updatedAccount = accountService.updateAccount(accountId, accountDto);
        return ResponseEntity.ok(updatedAccount);
    }
    
    @DeleteMapping("/{accountId}")
    @Operation(summary = "Delete account", description = "Deletes an account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "Account ID") @PathVariable UUID accountId) {
        log.info("Deleting account with ID: {}", accountId);
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get account statistics", description = "Retrieves account statistics for dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountStats> getAccountStats() {
        log.info("Fetching account statistics");
        AccountStats stats = accountService.getAccountStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search accounts", description = "Searches accounts by name, contact person, or email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AccountDto>> searchAccounts(
            @RequestParam String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        log.info("Searching accounts with query: {}", query);
        Page<AccountDto> accounts = accountService.searchAccounts(query, type, status, pageable);
        return ResponseEntity.ok(accounts);
    }
    
    // Inner class for account statistics
    public static class AccountStats {
        private long totalAccounts;
        private long activeAccounts;
        private double totalRevenue;
        private long totalDevices;
        private double averageSavings;
        
        public AccountStats() {}
        
        public AccountStats(long totalAccounts, long activeAccounts, double totalRevenue, long totalDevices, double averageSavings) {
            this.totalAccounts = totalAccounts;
            this.activeAccounts = activeAccounts;
            this.totalRevenue = totalRevenue;
            this.totalDevices = totalDevices;
            this.averageSavings = averageSavings;
        }
        
        // Getters and setters
        public long getTotalAccounts() { return totalAccounts; }
        public void setTotalAccounts(long totalAccounts) { this.totalAccounts = totalAccounts; }
        
        public long getActiveAccounts() { return activeAccounts; }
        public void setActiveAccounts(long activeAccounts) { this.activeAccounts = activeAccounts; }
        
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        
        public long getTotalDevices() { return totalDevices; }
        public void setTotalDevices(long totalDevices) { this.totalDevices = totalDevices; }
        
        public double getAverageSavings() { return averageSavings; }
        public void setAverageSavings(double averageSavings) { this.averageSavings = averageSavings; }
    }
}

