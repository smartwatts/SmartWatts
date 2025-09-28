package com.smartwatts.featureflagservice.repository;

import com.smartwatts.featureflagservice.model.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    Optional<FeatureFlag> findByFeatureKey(String featureKey);
    
    List<FeatureFlag> findByIsGloballyEnabled(Boolean isGloballyEnabled);
    
    List<FeatureFlag> findByIsPaidFeature(Boolean isPaidFeature);
    
    List<FeatureFlag> findByFeatureCategory(String featureCategory);
    
    @Query("SELECT f FROM FeatureFlag f WHERE f.isGloballyEnabled = true")
    List<FeatureFlag> findAllGloballyEnabled();
    
    @Query("SELECT f FROM FeatureFlag f WHERE f.featureKey IN :featureKeys")
    List<FeatureFlag> findByFeatureKeys(@Param("featureKeys") List<String> featureKeys);
}
