-- Weather Data Table Migration
-- Creates table for storing weather data for energy analytics

CREATE TABLE weather_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_name VARCHAR(100) NOT NULL,
    latitude DECIMAL(8,6),
    longitude DECIMAL(9,6),
    weather_date TIMESTAMP NOT NULL,
    temperature_celsius DECIMAL(5,2),
    humidity_percentage DECIMAL(5,2),
    pressure_hpa DECIMAL(6,2),
    wind_speed_ms DECIMAL(5,2),
    wind_direction_degrees DECIMAL(5,2),
    cloud_cover_percentage DECIMAL(5,2),
    solar_radiation_wm2 DECIMAL(6,2),
    weather_condition VARCHAR(50),
    weather_description VARCHAR(255),
    visibility_meters INTEGER,
    uv_index DECIMAL(4,2),
    dew_point_celsius DECIMAL(5,2),
    feels_like_celsius DECIMAL(5,2),
    precipitation_mm DECIMAL(6,2),
    snow_mm DECIMAL(6,2),
    data_source VARCHAR(50),
    forecast_hours INTEGER DEFAULT 0,
    confidence_score DECIMAL(3,2),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Weather Data
CREATE INDEX idx_weather_data_location_name ON weather_data(location_name);
CREATE INDEX idx_weather_data_weather_date ON weather_data(weather_date);
CREATE INDEX idx_weather_data_location_date ON weather_data(location_name, weather_date);
CREATE INDEX idx_weather_data_temperature ON weather_data(temperature_celsius);
CREATE INDEX idx_weather_data_humidity ON weather_data(humidity_percentage);
CREATE INDEX idx_weather_data_solar_radiation ON weather_data(solar_radiation_wm2);

-- Add weather correlation to energy analytics
ALTER TABLE energy_analytics ADD COLUMN weather_correlation DECIMAL(5,2);
ALTER TABLE energy_analytics ADD COLUMN temperature_impact_kwh DECIMAL(10,4);
ALTER TABLE energy_analytics ADD COLUMN humidity_impact_kwh DECIMAL(10,4);
ALTER TABLE energy_analytics ADD COLUMN solar_impact_kwh DECIMAL(10,4);

-- Add weather data to energy predictions
ALTER TABLE energy_predictions ADD COLUMN weather_conditions_forecast TEXT;
ALTER TABLE energy_predictions ADD COLUMN temperature_forecast_celsius DECIMAL(5,2);
ALTER TABLE energy_predictions ADD COLUMN humidity_forecast_percentage DECIMAL(5,2);
ALTER TABLE energy_predictions ADD COLUMN solar_radiation_forecast_wm2 DECIMAL(6,2); 