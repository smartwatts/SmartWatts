package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.CommunityBenchmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBenchmarkRepository extends JpaRepository<CommunityBenchmark, UUID> {
    
    Optional<CommunityBenchmark> findByRegionAndMetricType(String region, String metricType);
    
    List<CommunityBenchmark> findByRegion(String region);
    
    List<CommunityBenchmark> findByMetricType(String metricType);
    
    List<CommunityBenchmark> findByRegionAndIsActive(String region, Boolean isActive);
    
    List<CommunityBenchmark> findByMetricTypeAndIsActive(String metricType, Boolean isActive);
    
    @Query("SELECT cb FROM CommunityBenchmark cb WHERE cb.region = :region AND cb.metricType = :metricType AND cb.isActive = true")
    Optional<CommunityBenchmark> findActiveByRegionAndMetricType(String region, String metricType);
    
    @Query("SELECT DISTINCT cb.region FROM CommunityBenchmark cb WHERE cb.isActive = true")
    List<String> findDistinctActiveRegions();
    
    @Query("SELECT DISTINCT cb.metricType FROM CommunityBenchmark cb WHERE cb.isActive = true")
    List<String> findDistinctActiveMetricTypes();
    
    @Query("SELECT COUNT(cb) FROM CommunityBenchmark cb WHERE cb.region = :region AND cb.isActive = true")
    long countActiveByRegion(String region);
    
    @Query("SELECT AVG(cb.averageValue) FROM CommunityBenchmark cb WHERE cb.region = :region AND cb.metricType = :metricType AND cb.isActive = true")
    Double getAverageValueByRegionAndMetricType(String region, String metricType);
}
