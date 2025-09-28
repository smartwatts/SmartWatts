-- Appliance Monitoring Service Database Migration
-- Creates tables for appliance monitoring, weather data, and energy correlation

-- Appliances Table
CREATE TABLE appliances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id UUID,
    appliance_name VARCHAR(255) NOT NULL,
    appliance_type VARCHAR(50) NOT NULL CHECK (appliance_type IN (
        'REFRIGERATOR', 'AC_UNIT', 'WASHING_MACHINE', 'DISHWASHER', 'OVEN_STOVE',
        'MICROWAVE', 'TELEVISION', 'COMPUTER', 'LIGHTING', 'WATER_HEATER',
        'HEATING_SYSTEM', 'VENTILATION', 'POOL_EQUIPMENT', 'GARAGE_DOOR',
        'SECURITY_SYSTEM', 'OTHER'
    )),
    manufacturer VARCHAR(255),
    model VARCHAR(255),
    serial_number VARCHAR(255),
    installation_date TIMESTAMP,
    warranty_expiry TIMESTAMP,
    expected_lifespan_years INTEGER,
    rated_power_watts DECIMAL(8,2),
    energy_efficiency_rating VARCHAR(10),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    location VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Appliance Readings Table
CREATE TABLE appliance_readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appliance_id UUID NOT NULL REFERENCES appliances(id),
    timestamp TIMESTAMP NOT NULL,
    real_time_power_watts DECIMAL(8,2),
    voltage_volts DECIMAL(6,2),
    current_amps DECIMAL(6,4),
    power_factor DECIMAL(4,3),
    energy_consumption_kwh DECIMAL(8,4),
    efficiency_percentage DECIMAL(5,2),
    temperature_celsius DECIMAL(5,2),
    operating_status VARCHAR(20),
    anomaly_detected BOOLEAN DEFAULT FALSE,
    anomaly_type VARCHAR(50),
    maintenance_alert BOOLEAN DEFAULT FALSE,
    maintenance_message TEXT,
    data_quality_score DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Weather Data Table
CREATE TABLE weather_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    location_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    temperature_celsius DECIMAL(5,2),
    humidity_percentage DECIMAL(5,2),
    solar_irradiance_wm2 DECIMAL(6,2),
    wind_speed_kmh DECIMAL(5,2),
    wind_direction_degrees DECIMAL(5,2),
    atmospheric_pressure_hpa DECIMAL(7,2),
    precipitation_mm DECIMAL(6,2),
    cloud_cover_percentage DECIMAL(5,2),
    uv_index DECIMAL(4,2),
    weather_condition VARCHAR(50),
    energy_impact_score DECIMAL(5,2),
    seasonal_adjustment_factor DECIMAL(4,3),
    data_source VARCHAR(50),
    data_quality_score DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Energy-Weather Correlation Table
CREATE TABLE energy_weather_correlation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appliance_id UUID REFERENCES appliances(id),
    weather_data_id UUID REFERENCES weather_data(id),
    correlation_timestamp TIMESTAMP NOT NULL,
    energy_consumption_kwh DECIMAL(8,4),
    weather_impact_percentage DECIMAL(5,2),
    correlation_strength DECIMAL(4,3),
    seasonal_factor DECIMAL(4,3),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Appliance Maintenance Schedule Table
CREATE TABLE appliance_maintenance_schedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appliance_id UUID NOT NULL REFERENCES appliances(id),
    maintenance_type VARCHAR(50) NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    last_maintenance_date TIMESTAMP,
    maintenance_interval_days INTEGER,
    maintenance_cost DECIMAL(10,2),
    technician_notes TEXT,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_appliances_user_id ON appliances(user_id);
CREATE INDEX idx_appliances_device_id ON appliances(device_id);
CREATE INDEX idx_appliances_type ON appliances(appliance_type);
CREATE INDEX idx_appliance_readings_appliance_id ON appliance_readings(appliance_id);
CREATE INDEX idx_appliance_readings_timestamp ON appliance_readings(timestamp);
CREATE INDEX idx_weather_data_location_timestamp ON weather_data(location_id, timestamp);
CREATE INDEX idx_energy_weather_correlation_appliance ON energy_weather_correlation(appliance_id);
CREATE INDEX idx_maintenance_schedule_appliance ON appliance_maintenance_schedule(appliance_id);
CREATE INDEX idx_maintenance_schedule_status ON appliance_maintenance_schedule(status);
