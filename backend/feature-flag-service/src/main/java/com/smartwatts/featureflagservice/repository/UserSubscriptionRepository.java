package com.smartwatts.featureflagservice.repository;

import com.smartwatts.featureflagservice.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

    Optional<UserSubscription> findByUserIdAndStatus(UUID userId, UserSubscription.SubscriptionStatus status);
    
    List<UserSubscription> findByUserId(UUID userId);
    
    @Query("SELECT us FROM UserSubscription us WHERE us.userId = :userId AND us.status = 'ACTIVE' AND us.endDate > :currentDate")
    Optional<UserSubscription> findActiveSubscriptionByUserId(@Param("userId") UUID userId, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'ACTIVE' AND us.endDate > :currentDate")
    List<UserSubscription> findAllActiveSubscriptions(@Param("currentDate") LocalDateTime currentDate);
}
