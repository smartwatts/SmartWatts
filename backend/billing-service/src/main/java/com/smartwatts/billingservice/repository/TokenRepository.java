package com.smartwatts.billingservice.repository;

import com.smartwatts.billingservice.model.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {
    
    Optional<Token> findByTokenCode(String tokenCode);
    
    Page<Token> findByUserId(UUID userId, Pageable pageable);
    
    Page<Token> findByUserIdAndStatus(UUID userId, Token.TokenStatus status, Pageable pageable);
    
    List<Token> findByUserIdAndPurchaseDateBetween(UUID userId, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT t FROM Token t WHERE t.userId = :userId AND t.status = :status AND t.expiryDate > :currentDate")
    List<Token> findActiveTokensByUserId(UUID userId, Token.TokenStatus status, LocalDateTime currentDate);
    
    @Query("SELECT SUM(t.amountPaid) FROM Token t WHERE t.userId = :userId AND t.purchaseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountPaidByUserIdAndPeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(t.unitsPurchased) FROM Token t WHERE t.userId = :userId AND t.purchaseDate BETWEEN :startDate AND :endDate")
    BigDecimal sumUnitsPurchasedByUserIdAndPeriod(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT t FROM Token t WHERE t.status = :status AND t.expiryDate < :currentDate")
    List<Token> findExpiredTokens(Token.TokenStatus status, LocalDateTime currentDate);
    
    @Query("SELECT COUNT(t) FROM Token t WHERE t.userId = :userId AND t.status = :status")
    long countByUserIdAndStatus(UUID userId, Token.TokenStatus status);
    
    @Query("SELECT AVG(t.amountPaid) FROM Token t WHERE t.userId = :userId AND t.purchaseDate BETWEEN :startDate AND :endDate")
    BigDecimal getAverageTokenAmount(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
} 