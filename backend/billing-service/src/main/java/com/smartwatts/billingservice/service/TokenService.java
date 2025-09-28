package com.smartwatts.billingservice.service;

import com.smartwatts.billingservice.dto.TokenDto;
import com.smartwatts.billingservice.model.Token;
import com.smartwatts.billingservice.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    
    private final TokenRepository tokenRepository;
    
    @Transactional
    public TokenDto createToken(TokenDto tokenDto) {
        log.info("Creating token for user: {}", tokenDto.getUserId());
        
        Token token = new Token();
        BeanUtils.copyProperties(tokenDto, token);
        
        // Set default values
        if (token.getStatus() == null) {
            token.setStatus(Token.TokenStatus.PENDING);
        }
        if (token.getPurchaseDate() == null) {
            token.setPurchaseDate(LocalDateTime.now());
        }
        if (token.getUnitsConsumed() == null) {
            token.setUnitsConsumed(BigDecimal.ZERO);
        }
        if (token.getUnitsRemaining() == null) {
            token.setUnitsRemaining(token.getUnitsPurchased());
        }
        if (token.getExpiryDate() == null) {
            token.setExpiryDate(LocalDateTime.now().plusDays(30)); // 30 days validity
        }
        
        token.setCreatedAt(LocalDateTime.now());
        token.setUpdatedAt(LocalDateTime.now());
        
        Token savedToken = tokenRepository.save(token);
        log.info("Token created with ID: {}", savedToken.getId());
        
        return convertToDto(savedToken);
    }
    
    @Transactional(readOnly = true)
    public TokenDto getTokenById(UUID tokenId) {
        log.info("Fetching token with ID: {}", tokenId);
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found with ID: " + tokenId));
        return convertToDto(token);
    }
    
    @Transactional(readOnly = true)
    public Optional<TokenDto> getTokenByCode(String tokenCode) {
        log.info("Fetching token with code: {}", tokenCode);
        Optional<Token> token = tokenRepository.findByTokenCode(tokenCode);
        return token.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public Page<TokenDto> getTokensByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching tokens for user: {}", userId);
        Page<Token> tokens = tokenRepository.findByUserId(userId, pageable);
        return tokens.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public Page<TokenDto> getTokensByUserIdAndStatus(UUID userId, Token.TokenStatus status, Pageable pageable) {
        log.info("Fetching tokens for user: {} with status: {}", userId, status);
        Page<Token> tokens = tokenRepository.findByUserIdAndStatus(userId, status, pageable);
        return tokens.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<TokenDto> getActiveTokensByUserId(UUID userId) {
        log.info("Fetching active tokens for user: {}", userId);
        List<Token> tokens = tokenRepository.findActiveTokensByUserId(userId, Token.TokenStatus.ACTIVE, LocalDateTime.now());
        return tokens.stream().map(this::convertToDto).toList();
    }
    
    @Transactional
    public TokenDto activateToken(UUID tokenId) {
        log.info("Activating token with ID: {}", tokenId);
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found with ID: " + tokenId));
        
        if (token.getStatus() != Token.TokenStatus.PENDING) {
            throw new RuntimeException("Token is not in pending status");
        }
        
        token.setStatus(Token.TokenStatus.ACTIVE);
        token.setActivationDate(LocalDateTime.now());
        token.setUpdatedAt(LocalDateTime.now());
        
        Token savedToken = tokenRepository.save(token);
        return convertToDto(savedToken);
    }
    
    @Transactional
    public TokenDto consumeTokenUnits(UUID tokenId, BigDecimal unitsToConsume) {
        log.info("Consuming {} units from token: {}", unitsToConsume, tokenId);
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found with ID: " + tokenId));
        
        if (token.getStatus() != Token.TokenStatus.ACTIVE) {
            throw new RuntimeException("Token is not active");
        }
        
        if (token.getUnitsRemaining().compareTo(unitsToConsume) < 0) {
            throw new RuntimeException("Insufficient units remaining");
        }
        
        token.setUnitsConsumed(token.getUnitsConsumed().add(unitsToConsume));
        token.setUnitsRemaining(token.getUnitsRemaining().subtract(unitsToConsume));
        
        if (token.getUnitsRemaining().compareTo(BigDecimal.ZERO) <= 0) {
            token.setStatus(Token.TokenStatus.CONSUMED);
        }
        
        token.setUpdatedAt(LocalDateTime.now());
        
        Token savedToken = tokenRepository.save(token);
        return convertToDto(savedToken);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountPaidByUserIdAndPeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating total amount paid for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal total = tokenRepository.sumAmountPaidByUserIdAndPeriod(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getTotalUnitsPurchasedByUserIdAndPeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating total units purchased for user: {} between {} and {}", userId, startDate, endDate);
        BigDecimal total = tokenRepository.sumUnitsPurchasedByUserIdAndPeriod(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Transactional(readOnly = true)
    public List<TokenDto> getExpiredTokens() {
        log.info("Fetching expired tokens");
        List<Token> expiredTokens = tokenRepository.findExpiredTokens(Token.TokenStatus.ACTIVE, LocalDateTime.now());
        return expiredTokens.stream().map(this::convertToDto).toList();
    }
    
    private TokenDto convertToDto(Token token) {
        TokenDto dto = new TokenDto();
        BeanUtils.copyProperties(token, dto);
        return dto;
    }
} 