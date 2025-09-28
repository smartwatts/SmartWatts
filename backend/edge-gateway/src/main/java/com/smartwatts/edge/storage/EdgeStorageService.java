package com.smartwatts.edge.storage;

import com.smartwatts.edge.model.AnomalyDetection;
import com.smartwatts.edge.model.OptimizationRecommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EdgeStorageService {

    private static final String DB_URL = "jdbc:sqlite:edge_data.db";

    public void saveAnomalyDetection(AnomalyDetection anomaly) {
        String sql = "INSERT INTO anomaly_detections (device_id, consumption, baseline, anomaly_score, is_anomaly, severity, context, detected_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, anomaly.getDeviceId());
            pstmt.setDouble(2, anomaly.getActualValue());
            pstmt.setDouble(3, anomaly.getExpectedValue());
            pstmt.setDouble(4, anomaly.getConfidence());
            pstmt.setBoolean(5, anomaly.getConfidence() > 0.7);
            pstmt.setString(6, anomaly.getSeverity().toString());
            pstmt.setString(7, anomaly.getContext() != null ? anomaly.getContext().toString() : "{}");
            pstmt.setTimestamp(8, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
            log.info("Saved anomaly detection for device: {}", anomaly.getDeviceId());
            
        } catch (SQLException e) {
            log.error("Error saving anomaly detection: {}", e.getMessage());
        }
    }

    public void saveOptimizationRecommendations(List<OptimizationRecommendation> recommendations) {
        String sql = "INSERT INTO optimization_recommendations (device_id, type, priority, estimated_savings, implementation_time, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (OptimizationRecommendation rec : recommendations) {
                pstmt.setString(1, rec.getDeviceId());
                pstmt.setString(2, rec.getOptimizationType().toString());
                pstmt.setString(3, rec.getPriority().toString());
                pstmt.setDouble(4, rec.getExpectedCostSavings());
                pstmt.setDouble(5, rec.getEstimatedImplementationTime());
                pstmt.setTimestamp(6, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                
                pstmt.executeUpdate();
            }
            
            log.info("Saved {} optimization recommendations", recommendations.size());
            
        } catch (SQLException e) {
            log.error("Error saving optimization recommendations: {}", e.getMessage());
        }
    }

    public List<AnomalyDetection> getAnomalyDetections(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        // Implementation for retrieving anomaly detections
        log.info("Retrieving anomaly detections for device: {} between {} and {}", deviceId, startTime, endTime);
        return List.of(); // Placeholder
    }

    public List<OptimizationRecommendation> getOptimizationRecommendations(String deviceId) {
        // Implementation for retrieving optimization recommendations
        log.info("Retrieving optimization recommendations for device: {}", deviceId);
        return List.of(); // Placeholder
    }
    
    /**
     * Get device readings for analysis
     */
    public List<Map<String, Object>> getDeviceReadings(String deviceId, int limit) {
        log.info("Retrieving {} readings for device: {}", limit, deviceId);
        return List.of(); // Placeholder
    }
    
    /**
     * Get storage statistics
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_anomalies", 0);
        stats.put("total_recommendations", 0);
        stats.put("storage_size_mb", 0.0);
        stats.put("last_updated", LocalDateTime.now());
        return stats;
    }
}