package com.smartwatts.analyticsservice.repository;

import com.smartwatts.analyticsservice.model.WeatherData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, UUID> {
    
    List<WeatherData> findByLocationNameOrderByWeatherDateDesc(String locationName);
    
    Optional<WeatherData> findFirstByLocationNameOrderByWeatherDateDesc(String locationName);
    
    Page<WeatherData> findByLocationName(String locationName, Pageable pageable);
    
    List<WeatherData> findByWeatherDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<WeatherData> findByLocationNameAndWeatherDateBetween(String locationName, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT w FROM WeatherData w WHERE w.locationName = :locationName AND w.temperatureCelsius >= :minTemp AND w.temperatureCelsius <= :maxTemp")
    List<WeatherData> findByLocationNameAndTemperatureRange(@Param("locationName") String locationName, 
                                                          @Param("minTemp") BigDecimal minTemp, 
                                                          @Param("maxTemp") BigDecimal maxTemp);
    
    @Query("SELECT w FROM WeatherData w WHERE w.locationName = :locationName AND w.humidityPercentage >= :minHumidity")
    List<WeatherData> findByLocationNameAndMinHumidity(@Param("locationName") String locationName, 
                                                      @Param("minHumidity") BigDecimal minHumidity);
    
    @Query("SELECT w FROM WeatherData w WHERE w.locationName = :locationName AND w.cloudCoverPercentage >= :minCloudCover")
    List<WeatherData> findByLocationNameAndMinCloudCover(@Param("locationName") String locationName, 
                                                        @Param("minCloudCover") BigDecimal minCloudCover);
    
    @Query("SELECT w FROM WeatherData w WHERE w.locationName = :locationName AND w.solarRadiationWm2 >= :minSolarRadiation")
    List<WeatherData> findByLocationNameAndMinSolarRadiation(@Param("locationName") String locationName, 
                                                            @Param("minSolarRadiation") BigDecimal minSolarRadiation);
    
    @Query("SELECT AVG(w.temperatureCelsius) FROM WeatherData w WHERE w.locationName = :locationName AND w.weatherDate >= :startDate")
    BigDecimal getAverageTemperatureByLocationAndDate(@Param("locationName") String locationName, 
                                                    @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT AVG(w.humidityPercentage) FROM WeatherData w WHERE w.locationName = :locationName AND w.weatherDate >= :startDate")
    BigDecimal getAverageHumidityByLocationAndDate(@Param("locationName") String locationName, 
                                                 @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(w) FROM WeatherData w WHERE w.locationName = :locationName AND w.weatherDate >= :startDate")
    long countByLocationNameAndDateAfter(@Param("locationName") String locationName, 
                                       @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT w FROM WeatherData w WHERE w.locationName = :locationName AND w.forecastHours = 0 ORDER BY w.weatherDate DESC LIMIT 1")
    Optional<WeatherData> findLatestCurrentWeather(@Param("locationName") String locationName);
    
    @Query("SELECT w FROM WeatherData w WHERE w.locationName = :locationName AND w.forecastHours > 0 ORDER BY w.weatherDate ASC")
    List<WeatherData> findForecastData(@Param("locationName") String locationName);
} 