package com.smartwatts.analyticsservice.service;

import com.smartwatts.analyticsservice.dto.WeatherDataDto;
import com.smartwatts.analyticsservice.model.WeatherData;
import com.smartwatts.analyticsservice.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {
    
    private final WeatherDataRepository weatherDataRepository;
    private final RestTemplate restTemplate;
    
    @Value("${analytics.weather.api-key}")
    private String apiKey;
    
    @Value("${analytics.weather.base-url}")
    private String baseUrl;
    
    @Value("${analytics.weather.enabled:false}")
    private boolean weatherEnabled;
    
    @Transactional
    public WeatherDataDto createWeatherData(WeatherDataDto weatherDataDto) {
        log.info("Creating weather data for location: {}", weatherDataDto.getLocationName());
        
        WeatherData weatherData = new WeatherData();
        BeanUtils.copyProperties(weatherDataDto, weatherData);
        
        WeatherData savedWeatherData = weatherDataRepository.save(weatherData);
        log.info("Weather data created with ID: {}", savedWeatherData.getId());
        
        return convertToDto(savedWeatherData);
    }
    
    @Transactional(readOnly = true)
    public List<WeatherDataDto> getWeatherDataByLocation(String locationName) {
        log.info("Fetching weather data for location: {}", locationName);
        List<WeatherData> weatherDataList = weatherDataRepository.findByLocationNameOrderByWeatherDateDesc(locationName);
        return weatherDataList.stream().map(this::convertToDto).toList();
    }
    
    @Transactional(readOnly = true)
    public WeatherDataDto getCurrentWeather(String locationName) {
        log.info("Fetching current weather for location: {}", locationName);
        return weatherDataRepository.findFirstByLocationNameOrderByWeatherDateDesc(locationName)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    // @Scheduled(fixedRateString = "${analytics.weather.update-interval:1800000}") // 30 minutes default - temporarily disabled
    public void updateWeatherData() {
        if (!weatherEnabled) {
            log.debug("Weather service is disabled");
            return;
        }
        
        log.info("Updating weather data for all configured locations");
        
        try {
            // Check if we have a real API key
            if ("demo-key".equals(apiKey) || apiKey == null || apiKey.isEmpty()) {
                log.warn("No weather API key configured - weather data will not be available");
                return;
            } else {
                // Update weather for Lagos
                updateWeatherForLocation("Lagos", new BigDecimal("6.5244"), new BigDecimal("3.3792"));
                
                // Update weather for Abuja
                updateWeatherForLocation("Abuja", new BigDecimal("9.0820"), new BigDecimal("7.3986"));
                
                // Update weather for Kano
                updateWeatherForLocation("Kano", new BigDecimal("11.9914"), new BigDecimal("8.5313"));
            }
            
            log.info("Weather data update completed successfully");
        } catch (Exception e) {
            log.error("Error updating weather data: {}", e.getMessage(), e);
        }
    }
    
    // Mock weather data methods removed - production ready
    
    private void updateWeatherForLocation(String locationName, BigDecimal lat, BigDecimal lon) {
        try {
            String url = String.format("%s/weather?lat=%s&lon=%s&appid=%s&units=metric", 
                    baseUrl, lat, lon, apiKey);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                WeatherDataDto weatherData = parseWeatherResponse(response, locationName, lat, lon);
                createWeatherData(weatherData);
                log.debug("Updated weather data for {}: {}°C", locationName, weatherData.getTemperatureCelsius());
            }
        } catch (Exception e) {
            log.error("Error updating weather for {}: {}", locationName, e.getMessage());
        }
    }
    
    private WeatherDataDto parseWeatherResponse(Map<String, Object> response, String locationName, 
                                             BigDecimal lat, BigDecimal lon) {
        @SuppressWarnings("unchecked")
        Map<String, Object> main = (Map<String, Object>) response.get("main");
        @SuppressWarnings("unchecked")
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        @SuppressWarnings("unchecked")
        Map<String, Object> clouds = (Map<String, Object>) response.get("clouds");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
        
        WeatherDataDto weatherData = WeatherDataDto.builder()
                .locationName(locationName)
                .latitude(lat)
                .longitude(lon)
                .weatherDate(LocalDateTime.now())
                .temperatureCelsius(new BigDecimal(main.get("temp").toString()))
                .humidityPercentage(new BigDecimal(main.get("humidity").toString()))
                .pressureHpa(new BigDecimal(main.get("pressure").toString()))
                .windSpeedMs(new BigDecimal(wind.get("speed").toString()))
                .windDirectionDegrees(new BigDecimal(wind.get("deg").toString()))
                .cloudCoverPercentage(new BigDecimal(clouds.get("all").toString()))
                .weatherCondition(weather.get(0).get("main").toString())
                .weatherDescription(weather.get(0).get("description").toString())
                .visibilityMeters((Integer) response.get("visibility"))
                .feelsLikeCelsius(new BigDecimal(main.get("feels_like").toString()))
                .dataSource("openweathermap")
                .forecastHours(0) // Current weather
                .confidenceScore(new BigDecimal("0.95"))
                .build();
        
        return weatherData;
    }
    
    public Map<String, Object> getWeatherImpactOnEnergy(String locationName) {
        log.info("Calculating weather impact on energy for location: {}", locationName);
        
        WeatherDataDto currentWeather = getCurrentWeather(locationName);
        if (currentWeather == null) {
            return Map.of("error", "No weather data available");
        }
        
        // Calculate weather impact on energy consumption
        BigDecimal temperature = currentWeather.getTemperatureCelsius();
        BigDecimal humidity = currentWeather.getHumidityPercentage();
        BigDecimal solarRadiation = currentWeather.getSolarRadiationWm2();
        
        // Simple weather impact calculation
        BigDecimal coolingImpact = calculateCoolingImpact(temperature, humidity);
        BigDecimal heatingImpact = calculateHeatingImpact(temperature);
        BigDecimal solarImpact = calculateSolarImpact(solarRadiation, currentWeather.getCloudCoverPercentage());
        
        return Map.of(
            "location", locationName,
            "temperature", temperature,
            "humidity", humidity,
            "cooling_impact_kwh", coolingImpact,
            "heating_impact_kwh", heatingImpact,
            "solar_impact_kwh", solarImpact,
            "total_weather_impact_kwh", coolingImpact.add(heatingImpact).add(solarImpact),
            "recommendations", generateWeatherRecommendations(currentWeather)
        );
    }
    
    private BigDecimal calculateCoolingImpact(BigDecimal temperature, BigDecimal humidity) {
        // Higher temperature and humidity increase cooling needs
        if (temperature.compareTo(new BigDecimal("25")) > 0) {
            BigDecimal tempFactor = temperature.subtract(new BigDecimal("25"));
            BigDecimal humidityFactor = humidity.divide(new BigDecimal("100"));
            return tempFactor.multiply(humidityFactor).multiply(new BigDecimal("2.5"));
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateHeatingImpact(BigDecimal temperature) {
        // Lower temperature increases heating needs
        if (temperature.compareTo(new BigDecimal("18")) < 0) {
            BigDecimal tempFactor = new BigDecimal("18").subtract(temperature);
            return tempFactor.multiply(new BigDecimal("1.8"));
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateSolarImpact(BigDecimal solarRadiation, BigDecimal cloudCover) {
        // Solar radiation affects solar panel efficiency
        if (solarRadiation != null && solarRadiation.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal cloudFactor = new BigDecimal("1").subtract(cloudCover.divide(new BigDecimal("100")));
            return solarRadiation.multiply(cloudFactor).multiply(new BigDecimal("0.001"));
        }
        return BigDecimal.ZERO;
    }
    
    private List<String> generateWeatherRecommendations(WeatherDataDto weather) {
        List<String> recommendations = new java.util.ArrayList<>();
        
        BigDecimal temp = weather.getTemperatureCelsius();
        BigDecimal humidity = weather.getHumidityPercentage();
        
        if (temp.compareTo(new BigDecimal("30")) > 0) {
            recommendations.add("High temperature detected. Consider reducing AC usage during peak hours.");
        }
        
        if (humidity.compareTo(new BigDecimal("80")) > 0) {
            recommendations.add("High humidity detected. Dehumidifier usage may increase energy consumption.");
        }
        
        if (weather.getCloudCoverPercentage().compareTo(new BigDecimal("70")) > 0) {
            recommendations.add("Cloudy conditions detected. Solar generation may be reduced.");
        }
        
        if (weather.getWindSpeedMs().compareTo(new BigDecimal("10")) > 0) {
            recommendations.add("High wind detected. Consider securing outdoor equipment.");
        }
        
        return recommendations;
    }
    
    private WeatherDataDto convertToDto(WeatherData weatherData) {
        WeatherDataDto dto = new WeatherDataDto();
        BeanUtils.copyProperties(weatherData, dto);
        return dto;
    }
} 