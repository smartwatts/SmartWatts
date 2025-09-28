package com.smartwatts.deviceverificationservice.repository;

import com.smartwatts.deviceverificationservice.model.ActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivationTokenRepository extends JpaRepository<ActivationToken, String> {

    Optional<ActivationToken> findByDeviceId(String deviceId);
    
    List<ActivationToken> findByDeviceIdAndIsActiveTrue(String deviceId);
    
    List<ActivationToken> findByCustomerType(String customerType);
    
    List<ActivationToken> findByIsActiveTrue();
    
    @Query("SELECT t FROM ActivationToken t WHERE t.expiresAt <= :expiryDate AND t.isActive = true")
    List<ActivationToken> findExpiredActiveTokens(@Param("expiryDate") LocalDateTime expiryDate);
    
    @Query("SELECT t FROM ActivationToken t WHERE t.deviceId = :deviceId AND t.isActive = true ORDER BY t.createdAt DESC")
    List<ActivationToken> findActiveTokensByDeviceId(@Param("deviceId") String deviceId);
    
    @Query("SELECT COUNT(t) FROM ActivationToken t WHERE t.deviceId = :deviceId AND t.isActive = true")
    long countActiveTokensByDeviceId(@Param("deviceId") String deviceId);
    
    @Query("SELECT t FROM ActivationToken t WHERE t.tokenHash = :tokenHash AND t.isActive = true")
    Optional<ActivationToken> findByTokenHashAndActive(@Param("tokenHash") String tokenHash);
    
    boolean existsByDeviceIdAndIsActiveTrue(String deviceId);
}
