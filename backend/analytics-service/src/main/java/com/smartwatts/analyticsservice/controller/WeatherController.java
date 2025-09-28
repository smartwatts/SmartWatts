package com.smartwatts.analyticsservice.controller;

import com.smartwatts.analyticsservice.dto.WeatherDataDto;
import com.smartwatts.analyticsservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {
    
    private final WeatherService weatherService;
    
    @GetMapping("/current/{location}")
    public ResponseEntity<WeatherDataDto> getCurrentWeather(@PathVariable String location) {
        log.info("Getting current weather for location: {}", location);
        WeatherDataDto weather = weatherService.getCurrentWeather(location);
        if (weather != null) {
            return ResponseEntity.ok(weather);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/history/{location}")
    public ResponseEntity<List<WeatherDataDto>> getWeatherHistory(@PathVariable String location) {
        log.info("Getting weather history for location: {}", location);
        List<WeatherDataDto> weatherHistory = weatherService.getWeatherDataByLocation(location);
        return ResponseEntity.ok(weatherHistory);
    }
    
    @GetMapping("/impact/{location}")
    public ResponseEntity<Map<String, Object>> getWeatherImpactOnEnergy(@PathVariable String location) {
        log.info("Getting weather impact on energy for location: {}", location);
        Map<String, Object> impact = weatherService.getWeatherImpactOnEnergy(location);
        return ResponseEntity.ok(impact);
    }
    
    @PostMapping("/update")
    public ResponseEntity<String> triggerWeatherUpdate() {
        log.info("Triggering manual weather data update");
        weatherService.updateWeatherData();
        return ResponseEntity.ok("Weather data update triggered successfully");
    }
} 