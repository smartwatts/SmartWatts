package com.smartwatts.billingservice.controller;

import com.smartwatts.billingservice.dto.TokenDto;
import com.smartwatts.billingservice.model.Token;
import com.smartwatts.billingservice.service.TokenService;
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

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Token Management", description = "APIs for managing prepaid electricity tokens")
public class TokenController {
    
    private final TokenService tokenService;
    
    @PostMapping
    @Operation(summary = "Create a new token", description = "Creates a new prepaid electricity token")
    public ResponseEntity<TokenDto> createToken(@Valid @RequestBody TokenDto tokenDto) {
        log.info("Creating token for user: {}", tokenDto.getUserId());
        TokenDto createdToken = tokenService.createToken(tokenDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdToken);
    }
    
    @GetMapping("/{tokenId}")
    @Operation(summary = "Get token by ID", description = "Retrieves a specific token by its ID")
    public ResponseEntity<TokenDto> getTokenById(
            @Parameter(description = "Token ID") @PathVariable UUID tokenId) {
        log.info("Fetching token with ID: {}", tokenId);
        TokenDto token = tokenService.getTokenById(tokenId);
        return ResponseEntity.ok(token);
    }
    
    @GetMapping("/code/{tokenCode}")
    @Operation(summary = "Get token by code", description = "Retrieves a specific token by its code")
    public ResponseEntity<TokenDto> getTokenByCode(
            @Parameter(description = "Token code") @PathVariable String tokenCode) {
        log.info("Fetching token with code: {}", tokenCode);
        Optional<TokenDto> token = tokenService.getTokenByCode(tokenCode);
        return token.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tokens by user ID", description = "Retrieves all tokens for a specific user")
    public ResponseEntity<Page<TokenDto>> getTokensByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Fetching tokens for user: {}", userId);
        Page<TokenDto> tokens = tokenService.getTokensByUserId(userId, pageable);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get tokens by user ID and status", description = "Retrieves tokens for a user with specific status")
    public ResponseEntity<Page<TokenDto>> getTokensByUserIdAndStatus(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Token status") @PathVariable Token.TokenStatus status,
            Pageable pageable) {
        log.info("Fetching tokens for user: {} with status: {}", userId, status);
        Page<TokenDto> tokens = tokenService.getTokensByUserIdAndStatus(userId, status, pageable);
        return ResponseEntity.ok(tokens);
    }
    
    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get active tokens by user ID", description = "Retrieves all active tokens for a specific user")
    public ResponseEntity<List<TokenDto>> getActiveTokensByUserId(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        log.info("Fetching active tokens for user: {}", userId);
        List<TokenDto> tokens = tokenService.getActiveTokensByUserId(userId);
        return ResponseEntity.ok(tokens);
    }
    
    @PutMapping("/{tokenId}/activate")
    @Operation(summary = "Activate token", description = "Activates a pending token")
    public ResponseEntity<TokenDto> activateToken(
            @Parameter(description = "Token ID") @PathVariable UUID tokenId) {
        log.info("Activating token with ID: {}", tokenId);
        TokenDto activatedToken = tokenService.activateToken(tokenId);
        return ResponseEntity.ok(activatedToken);
    }
    
    @PutMapping("/{tokenId}/consume")
    @Operation(summary = "Consume token units", description = "Consumes units from an active token")
    public ResponseEntity<TokenDto> consumeTokenUnits(
            @Parameter(description = "Token ID") @PathVariable UUID tokenId,
            @Parameter(description = "Units to consume") @RequestParam BigDecimal unitsToConsume) {
        log.info("Consuming {} units from token: {}", unitsToConsume, tokenId);
        TokenDto updatedToken = tokenService.consumeTokenUnits(tokenId, unitsToConsume);
        return ResponseEntity.ok(updatedToken);
    }
    
    @GetMapping("/user/{userId}/total-amount")
    @Operation(summary = "Get total amount paid by user and period", description = "Calculates total amount paid for tokens by a user in a period")
    public ResponseEntity<BigDecimal> getTotalAmountPaidByUserIdAndPeriod(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Calculating total amount paid for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal totalAmount = tokenService.getTotalAmountPaidByUserIdAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/user/{userId}/total-units")
    @Operation(summary = "Get total units purchased by user and period", description = "Calculates total units purchased by a user in a period")
    public ResponseEntity<BigDecimal> getTotalUnitsPurchasedByUserIdAndPeriod(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Start date") @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam LocalDateTime endDate) {
        log.info("Calculating total units purchased for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal totalUnits = tokenService.getTotalUnitsPurchasedByUserIdAndPeriod(userId, startDate, endDate);
        return ResponseEntity.ok(totalUnits);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "Get expired tokens", description = "Retrieves all expired tokens")
    public ResponseEntity<List<TokenDto>> getExpiredTokens() {
        log.info("Fetching expired tokens");
        List<TokenDto> expiredTokens = tokenService.getExpiredTokens();
        return ResponseEntity.ok(expiredTokens);
    }
} 