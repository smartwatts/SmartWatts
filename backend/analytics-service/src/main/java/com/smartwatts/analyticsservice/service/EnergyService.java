package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.model.EnergyReading;
import com.smartwatts.analyticsservice.repository.EnergyReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnergyService {

    private final EnergyReadingRepository energyReadingRepository;

    /**
     * Get energy readings for a device within a time range
     */
    public List<EnergyReading> getEnergyReadings(UUID deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting energy readings for device: {} between {} and {}", deviceId, startTime, endTime);
        return energyReadingRepository.findByDeviceIdAndReadingTimestampBetween(deviceId, startTime, endTime);
    }

    /**
     * Get energy readings for a user within a time range
     */
    public List<EnergyReading> getEnergyReadingsByUser(UUID userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting energy readings for user: {} between {} and {}", userId, startTime, endTime);
        return energyReadingRepository.findByUserIdAndReadingTimestampBetween(userId, startTime, endTime);
    }

    /**
     * Get latest energy reading for a device
     */
    public EnergyReading getLatestEnergyReading(UUID deviceId) {
        log.info("Getting latest energy reading for device: {}", deviceId);
        List<EnergyReading> readings = energyReadingRepository.findByDeviceIdOrderByReadingTimestampDesc(deviceId);
        return readings.isEmpty() ? null : readings.get(0);
    }

    /**
     * Get latest energy reading for a user
     */
    public EnergyReading getLatestEnergyReadingByUser(UUID userId) {
        log.info("Getting latest energy reading for user: {}", userId);
        List<EnergyReading> readings = energyReadingRepository.findByUserIdOrderByReadingTimestampDesc(userId);
        return readings.isEmpty() ? null : readings.get(0);
    }
}
