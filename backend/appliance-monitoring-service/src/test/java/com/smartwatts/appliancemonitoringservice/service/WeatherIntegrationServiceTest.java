package com.smartwatts.appliancemonitoringservice.service;

import com.smartwatts.appliancemonitoringservice.model.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherIntegrationServiceTest {

    @InjectMocks
    private WeatherIntegrationService weatherIntegrationService;

    private RestTemplate mockRestTemplate;
    private String testLocationId;
    private double testLatitude;
    private double testLongitude;

    @BeforeEach
    void setUp() throws Exception {
        testLocationId = "test-location-1";
        testLatitude = 6.5244;
        testLongitude = 3.3792;

        // Set up test properties using reflection
        ReflectionTestUtils.setField(weatherIntegrationService, "openWeatherBaseUrl", "https://api.openweathermap.org/data/2.5");
        ReflectionTestUtils.setField(weatherIntegrationService, "openWeatherApiKey", "test-api-key");

        // Replace RestTemplate with mock using reflection
        mockRestTemplate = mock(RestTemplate.class);
        Field restTemplateField = WeatherIntegrationService.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(weatherIntegrationService, mockRestTemplate);
    }

    @Test
    void fetchCurrentWeather_Success_ReturnsWeatherData() {
        // Given
        Map<String, Object> response = createMockWeatherResponse();
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        // When
        WeatherData result = weatherIntegrationService.fetchCurrentWeather(testLocationId, testLatitude, testLongitude);

        // Then
        assertNotNull(result);
        assertEquals(testLocationId, result.getLocationId());
        assertNotNull(result.getTimestamp());
        assertEquals("OPENWEATHER", result.getDataSource());
        verify(mockRestTemplate).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void fetchCurrentWeather_InvalidResponse_ReturnsDefaultData() {
        // Given
        Map<String, Object> invalidResponse = new HashMap<>();
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(invalidResponse);

        // When
        WeatherData result = weatherIntegrationService.fetchCurrentWeather(testLocationId, testLatitude, testLongitude);

        // Then
        assertNotNull(result);
        assertEquals(testLocationId, result.getLocationId());
        assertEquals("DEFAULT", result.getDataSource());
    }

    @Test
    void fetchCurrentWeather_ApiException_ReturnsDefaultData() {
        // Given
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RestClientException("API Error"));

        // When
        WeatherData result = weatherIntegrationService.fetchCurrentWeather(testLocationId, testLatitude, testLongitude);

        // Then
        assertNotNull(result);
        assertEquals(testLocationId, result.getLocationId());
        assertEquals("DEFAULT", result.getDataSource());
    }

    @Test
    void fetchCurrentWeather_WithValidData_ParsesCorrectly() {
        // Given
        Map<String, Object> response = createMockWeatherResponse();
        when(mockRestTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

        // When
        WeatherData result = weatherIntegrationService.fetchCurrentWeather(testLocationId, testLatitude, testLongitude);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTemperatureCelsius());
        assertNotNull(result.getHumidityPercentage());
        assertNotNull(result.getAtmosphericPressureHpa());
        assertNotNull(result.getWindSpeedKmh());
        assertNotNull(result.getWindDirectionDegrees());
        assertNotNull(result.getCloudCoverPercentage());
        assertNotNull(result.getWeatherCondition());
    }

    private Map<String, Object> createMockWeatherResponse() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> main = new HashMap<>();
        main.put("temp", 25.5);
        main.put("humidity", 65.0);
        main.put("pressure", 1013.25);
        response.put("main", main);
        
        Map<String, Object> wind = new HashMap<>();
        wind.put("speed", 5.5); // m/s
        wind.put("deg", 180.0);
        response.put("wind", wind);
        
        Map<String, Object> clouds = new HashMap<>();
        clouds.put("all", 30.0);
        response.put("clouds", clouds);
        
        Map<String, Object> weather = new HashMap<>();
        weather.put("main", "Clear");
        response.put("weather", java.util.Arrays.asList(weather));
        
        return response;
    }
}

