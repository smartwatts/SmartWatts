package com.smartwatts.analyticsservice.controller;

import com.smartwatts.analyticsservice.service.CommunityBenchmarkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
@Slf4j
public class CommunityBenchmarkingController {

    private final CommunityBenchmarkingService communityBenchmarkingService;

    /**
     * Get user's efficiency ranking in their region
     */
    @GetMapping("/benchmark/{region}/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRanking(
            @PathVariable String region,
            @PathVariable UUID userId) {
        
        log.info("Getting user ranking for user: {} in region: {}", userId, region);
        
        try {
            Map<String, Object> ranking = communityBenchmarkingService.calculateUserRanking(userId, region);
            return ResponseEntity.ok(ranking);
        } catch (Exception e) {
            log.error("Error getting user ranking for user: {} in region: {}", userId, region, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get community leaderboard for a region
     */
    @GetMapping("/leaderboard/{region}")
    public ResponseEntity<Map<String, Object>> getCommunityLeaderboard(
            @PathVariable String region,
            @RequestParam(defaultValue = "ENERGY_EFFICIENCY") String metricType,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting community leaderboard for region: {}, metric: {}, limit: {}", region, metricType, limit);
        
        try {
            Map<String, Object> leaderboard = communityBenchmarkingService.getCommunityLeaderboard(region, metricType, limit);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            log.error("Error getting community leaderboard for region: {}", region, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get solar utilization comparison
     */
    @GetMapping("/solar-comparison/{region}/user/{userId}")
    public ResponseEntity<Map<String, Object>> getSolarUtilizationComparison(
            @PathVariable String region,
            @PathVariable UUID userId) {
        
        log.info("Getting solar utilization comparison for user: {} in region: {}", userId, region);
        
        try {
            Map<String, Object> comparison = communityBenchmarkingService.getSolarUtilizationComparison(userId, region);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            log.error("Error getting solar utilization comparison for user: {} in region: {}", userId, region, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get energy savings comparison
     */
    @GetMapping("/savings-comparison/{region}/user/{userId}")
    public ResponseEntity<Map<String, Object>> getEnergySavingsComparison(
            @PathVariable String region,
            @PathVariable UUID userId) {
        
        log.info("Getting energy savings comparison for user: {} in region: {}", userId, region);
        
        try {
            Map<String, Object> comparison = communityBenchmarkingService.getEnergySavingsComparison(userId, region);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            log.error("Error getting energy savings comparison for user: {} in region: {}", userId, region, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update community benchmarks with anonymized data
     */
    @PostMapping("/benchmark/{region}/{metricType}")
    public ResponseEntity<Void> updateCommunityBenchmarks(
            @PathVariable String region,
            @PathVariable String metricType,
            @RequestBody Map<String, Object> anonymizedData) {
        
        log.info("Updating community benchmarks for region: {}, metric: {}", region, metricType);
        
        try {
            communityBenchmarkingService.updateCommunityBenchmarks(region, metricType, anonymizedData);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating community benchmarks for region: {}, metric: {}", region, metricType, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
