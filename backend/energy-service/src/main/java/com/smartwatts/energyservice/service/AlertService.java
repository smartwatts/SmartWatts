package com.smartwatts.energyservice.service;

import com.smartwatts.energyservice.model.EnergyAlert;
import com.smartwatts.energyservice.model.EnergyReading;
import com.smartwatts.energyservice.repository.EnergyAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {
    
    private final EnergyAlertRepository energyAlertRepository;
    private final NotificationService notificationService;
    
    // Alert thresholds (these could be configurable per user)
    private static final BigDecimal HIGH_CONSUMPTION_THRESHOLD = new BigDecimal("5000"); // 5kW
    private static final BigDecimal VOLTAGE_FLUCTUATION_THRESHOLD = new BigDecimal("20"); // 20V
    private static final BigDecimal NORMAL_VOLTAGE = new BigDecimal("240"); // 240V
    private static final BigDecimal LOW_POWER_FACTOR_THRESHOLD = new BigDecimal("0.8"); // 0.8
    
    @Transactional
    public void checkForAlerts(EnergyReading reading) {
        log.info("Checking for alerts on reading ID: {}", reading.getId());
        
        // Check for high consumption
        if (reading.getPower() != null && reading.getPower().compareTo(HIGH_CONSUMPTION_THRESHOLD) > 0) {
            createAlert(reading, EnergyAlert.AlertType.HIGH_CONSUMPTION, EnergyAlert.Severity.HIGH,
                    "High Power Consumption", 
                    String.format("Power consumption of %.2f kW exceeds the threshold of %.2f kW", 
                            reading.getPower(), HIGH_CONSUMPTION_THRESHOLD),
                    HIGH_CONSUMPTION_THRESHOLD, reading.getPower());
        }
        
        // Check for voltage fluctuations
        if (reading.getVoltage() != null) {
            BigDecimal voltageDifference = reading.getVoltage().subtract(NORMAL_VOLTAGE).abs();
            if (voltageDifference.compareTo(VOLTAGE_FLUCTUATION_THRESHOLD) > 0) {
                createAlert(reading, EnergyAlert.AlertType.VOLTAGE_FLUCTUATION, EnergyAlert.Severity.MEDIUM,
                        "Voltage Fluctuation",
                        String.format("Voltage of %.2f V is outside the normal range (%.2f ± %.2f V)",
                                reading.getVoltage(), NORMAL_VOLTAGE, VOLTAGE_FLUCTUATION_THRESHOLD),
                        VOLTAGE_FLUCTUATION_THRESHOLD, voltageDifference);
            }
        }
        
        // Check for low power factor
        if (reading.getPowerFactor() != null && reading.getPowerFactor().compareTo(LOW_POWER_FACTOR_THRESHOLD) < 0) {
            createAlert(reading, EnergyAlert.AlertType.ENERGY_EFFICIENCY, EnergyAlert.Severity.MEDIUM,
                    "Low Power Factor",
                    String.format("Power factor of %.2f is below the recommended threshold of %.2f",
                            reading.getPowerFactor(), LOW_POWER_FACTOR_THRESHOLD),
                    LOW_POWER_FACTOR_THRESHOLD, reading.getPowerFactor());
        }
        
        // Check for power outage (zero power for extended period)
        if (reading.getPower() != null && reading.getPower().compareTo(BigDecimal.ZERO) == 0) {
            createAlert(reading, EnergyAlert.AlertType.POWER_OUTAGE, EnergyAlert.Severity.CRITICAL,
                    "Power Outage Detected",
                    "No power consumption detected - possible power outage",
                    BigDecimal.ZERO, reading.getPower());
        }
    }
    
    private void createAlert(EnergyReading reading, EnergyAlert.AlertType alertType, EnergyAlert.Severity severity,
                           String title, String message, BigDecimal thresholdValue, BigDecimal actualValue) {
        
        // Check if similar alert already exists and is unresolved
        if (hasActiveAlert(reading.getUserId(), alertType)) {
            log.debug("Active alert of type {} already exists for user {}", alertType, reading.getUserId());
            return;
        }
        
        EnergyAlert alert = new EnergyAlert();
        alert.setUserId(reading.getUserId());
        alert.setDeviceId(reading.getDeviceId());
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setThresholdValue(thresholdValue);
        alert.setActualValue(actualValue);
        alert.setAlertTimestamp(LocalDateTime.now());
        alert.setIsAcknowledged(false);
        alert.setIsResolved(false);
        alert.setNotificationSent(false);
        
        EnergyAlert savedAlert = energyAlertRepository.save(alert);
        log.info("Created alert with ID: {} for user: {}, type: {}", 
                savedAlert.getId(), reading.getUserId(), alertType);
        
        // Send notification (email, SMS, push notification)
        try {
            notificationService.sendAlertNotification(savedAlert);
            
            // Mark notification as sent
            savedAlert.setNotificationSent(true);
            savedAlert.setNotificationSentAt(LocalDateTime.now());
            energyAlertRepository.save(savedAlert);
            
        } catch (Exception e) {
            log.error("Failed to send notification for alert ID: {}", savedAlert.getId(), e);
            // Don't mark as sent if there was an error
        }
    }
    
    private boolean hasActiveAlert(UUID userId, EnergyAlert.AlertType alertType) {
        // Check for unresolved alerts of the same type in the last hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return energyAlertRepository.findByUserIdAndAlertType(userId, alertType, null)
                .stream()
                .anyMatch(alert -> !alert.getIsResolved() && 
                        alert.getAlertTimestamp().isAfter(oneHourAgo));
    }
    

    
    @Transactional
    public void processPendingNotifications() {
        log.info("Processing pending notifications");
        
        try {
            // Find all alerts that haven't had notifications sent yet
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            var pendingAlerts = energyAlertRepository.findAll()
                    .stream()
                    .filter(alert -> !alert.getNotificationSent() && 
                            alert.getAlertTimestamp().isAfter(oneHourAgo))
                    .toList();
            
            log.info("Found {} pending notifications to process", pendingAlerts.size());
            
            for (EnergyAlert alert : pendingAlerts) {
                try {
                    notificationService.sendAlertNotification(alert);
                    
                    // Mark notification as sent
                    alert.setNotificationSent(true);
                    alert.setNotificationSentAt(LocalDateTime.now());
                    energyAlertRepository.save(alert);
                    
                    log.debug("Processed notification for alert ID: {}", alert.getId());
                    
                } catch (Exception e) {
                    log.error("Failed to process notification for alert ID: {}", alert.getId(), e);
                }
            }
            
            log.info("Successfully processed {} pending notifications", pendingAlerts.size());
            
        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
        }
    }
} 