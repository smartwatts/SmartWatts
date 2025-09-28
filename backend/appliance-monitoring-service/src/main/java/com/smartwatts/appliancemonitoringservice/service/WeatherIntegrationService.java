package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.WeatherData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherIntegrationService {
    
    @Value("${weather.api.openweather.base-url}")
    private String openWeatherBaseUrl;
    
    @Value("${weather.api.openweather.api-key}")
    private String openWeatherApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Fetch current weather data for a location
     */
    public WeatherData fetchCurrentWeather(String locationId, double latitude, double longitude) {
        try {
            log.info("Fetching weather data for location: {} ({}, {})", locationId, latitude, longitude);
            
            String url = String.format("%s/weather?lat=%.6f&lon=%.6f&appid=%s&units=metric",
                openWeatherBaseUrl, latitude, longitude, openWeatherApiKey);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("main")) {
                return parseOpenWeatherResponse(locationId, response);
            } else {
                log.warn("Invalid response from OpenWeather API for location: {}", locationId);
                return createDefaultWeatherData(locationId);
            }
            
        } catch (Exception e) {
            log.error("Error fetching weather data for location: {}", locationId, e);
            return createDefaultWeatherData(locationId);
        }
    }
    
    /**
     * Parse OpenWeather API response into WeatherData object
     */
    private WeatherData parseOpenWeatherResponse(String locationId, Map<String, Object> response) {
        WeatherData weatherData = new WeatherData();
        weatherData.setLocationId(locationId);
        weatherData.setTimestamp(LocalDateTime.now());
        weatherData.setDataSource("OPENWEATHER");
        
        try {
            // Parse main weather data
            @SuppressWarnings("unchecked")
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            if (main != null) {
                if (main.containsKey("temp")) {
                    weatherData.setTemperatureCelsius(BigDecimal.valueOf(((Number) main.get("temp")).doubleValue()));
                }
                if (main.containsKey("humidity")) {
                    weatherData.setHumidityPercentage(BigDecimal.valueOf(((Number) main.get("humidity")).doubleValue()));
                }
                if (main.containsKey("pressure")) {
                    weatherData.setAtmosphericPressureHpa(BigDecimal.valueOf(((Number) main.get("pressure")).doubleValue()));
                }
            }
            
            // Parse wind data
            @SuppressWarnings("unchecked")
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            if (wind != null) {
                if (wind.containsKey("speed")) {
                    // Convert m/s to km/h
                    double speedMs = ((Number) wind.get("speed")).doubleValue();
                    weatherData.setWindSpeedKmh(BigDecimal.valueOf(speedMs * 3.6));
                }
                if (wind.containsKey("deg")) {
                    weatherData.setWindDirectionDegrees(BigDecimal.valueOf(((Number) wind.get("deg")).doubleValue()));
                }
            }
            
            // Parse cloud data
            @SuppressWarnings("unchecked")
            Map<String, Object> clouds = (Map<String, Object>) response.get("clouds");
            if (clouds != null && clouds.containsKey("all")) {
                weatherData.setCloudCoverPercentage(BigDecimal.valueOf(((Number) clouds.get("all")).doubleValue()));
            }
            
            // Parse weather condition
            if (response.containsKey("weather") && response.get("weather") instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> weatherList = (java.util.List<Map<String, Object>>) response.get("weather");
                if (!weatherList.isEmpty()) {
                    Map<String, Object> weather = weatherList.get(0);
                    if (weather.containsKey("main")) {
                        weatherData.setWeatherCondition(weather.get("main").toString());
                    }
                }
            }
            
            // Calculate solar irradiance based on time and cloud cover
            weatherData.setSolarIrradianceWm2(calculateSolarIrradiance(weatherData));
            
            // Calculate energy impact score
            weatherData.setEnergyImpactScore(calculateEnergyImpactScore(weatherData));
            
            // Calculate seasonal adjustment factor
            weatherData.setSeasonalAdjustmentFactor(calculateSeasonalAdjustmentFactor(weatherData));
            
            // Set data quality score
            weatherData.setDataQualityScore(BigDecimal.valueOf(95.0)); // High quality for API data
            
        } catch (Exception e) {
            log.error("Error parsing weather response for location: {}", locationId, e);
        }
        
        return weatherData;
    }
    
    /**
     * Calculate solar irradiance based on time and weather conditions
     */
    private BigDecimal calculateSolarIrradiance(WeatherData weatherData) {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // Base solar irradiance by hour (peak at noon)
        double baseIrradiance = 0;
        if (hour >= 6 && hour <= 18) {
            // Daytime hours
            if (hour == 12) {
                baseIrradiance = 1000; // Peak at noon
            } else {
                baseIrradiance = 800; // Average daytime
            }
        }
        
        // Adjust for cloud cover
        if (weatherData.getCloudCoverPercentage() != null) {
            double cloudFactor = 1.0 - (weatherData.getCloudCoverPercentage().doubleValue() / 100.0) * 0.7;
            baseIrradiance *= cloudFactor;
        }
        
        // Adjust for weather condition
        if (weatherData.getWeatherCondition() != null) {
            switch (weatherData.getWeatherCondition().toUpperCase()) {
                case "CLEAR":
                    baseIrradiance *= 1.0;
                    break;
                case "CLOUDS":
                    baseIrradiance *= 0.6;
                    break;
                case "RAIN":
                    baseIrradiance *= 0.3;
                    break;
                case "SNOW":
                    baseIrradiance *= 0.2;
                    break;
                default:
                    baseIrradiance *= 0.7;
                    break;
            }
        }
        
        return BigDecimal.valueOf(Math.max(0, baseIrradiance));
    }
    
    /**
     * Calculate energy impact score based on weather conditions
     */
    private BigDecimal calculateEnergyImpactScore(WeatherData weatherData) {
        double score = 50.0; // Base neutral impact
        
        // Temperature impact
        if (weatherData.getTemperatureCelsius() != null) {
            double temp = weatherData.getTemperatureCelsius().doubleValue();
            if (temp > 30) {
                score += 30; // High AC usage
            } else if (temp > 25) {
                score += 20; // Moderate AC usage
            } else if (temp < 10) {
                score += 25; // Heating usage
            } else if (temp < 5) {
                score += 35; // High heating usage
            }
        }
        
        // Humidity impact
        if (weatherData.getHumidityPercentage() != null) {
            double humidity = weatherData.getHumidityPercentage().doubleValue();
            if (humidity > 80) {
                score += 15; // Dehumidifier usage
            } else if (humidity < 30) {
                score += 10; // Humidifier usage
            }
        }
        
        // Wind impact
        if (weatherData.getWindSpeedKmh() != null) {
            double windSpeed = weatherData.getWindSpeedKmh().doubleValue();
            if (windSpeed > 50) {
                score += 20; // High wind may affect outdoor equipment
            }
        }
        
        return BigDecimal.valueOf(Math.min(100, Math.max(0, score)));
    }
    
    /**
     * Calculate seasonal adjustment factor
     */
    private BigDecimal calculateSeasonalAdjustmentFactor(WeatherData weatherData) {
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        
        // Seasonal factors (1.0 = neutral, >1.0 = higher energy usage, <1.0 = lower energy usage)
        double seasonalFactor = 1.0;
        
        if (month >= 12 || month <= 2) {
            seasonalFactor = 1.3; // Winter - higher heating
        } else if (month >= 6 && month <= 8) {
            seasonalFactor = 1.4; // Summer - higher AC
        } else if (month >= 3 && month <= 5) {
            seasonalFactor = 0.9; // Spring - moderate usage
        } else if (month >= 9 && month <= 11) {
            seasonalFactor = 0.8; // Fall - moderate usage
        }
        
        return BigDecimal.valueOf(seasonalFactor);
    }
    
    /**
     * Create default weather data when API fails
     */
    private WeatherData createDefaultWeatherData(String locationId) {
        WeatherData weatherData = new WeatherData();
        weatherData.setLocationId(locationId);
        weatherData.setTimestamp(LocalDateTime.now());
        weatherData.setDataSource("DEFAULT");
        weatherData.setDataQualityScore(BigDecimal.valueOf(10.0)); // Low quality for default data
        weatherData.setEnergyImpactScore(BigDecimal.valueOf(50.0)); // Neutral impact
        weatherData.setSeasonalAdjustmentFactor(BigDecimal.valueOf(1.0)); // No seasonal adjustment
        
        return weatherData;
    }
}
