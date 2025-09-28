-- SmartWatts Azure SQL Database Migration
-- This script creates all necessary databases and schemas for Azure SQL

-- Create databases (Azure SQL uses schemas instead of separate databases)
-- We'll use a single database with different schemas for each service

-- Create schemas for each service
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS energy;
CREATE SCHEMA IF NOT EXISTS devices;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS billing;
CREATE SCHEMA IF NOT EXISTS notifications;
CREATE SCHEMA IF NOT EXISTS edge;
CREATE SCHEMA IF NOT EXISTS feature_flags;
CREATE SCHEMA IF NOT EXISTS device_verification;
CREATE SCHEMA IF NOT EXISTS appliance_monitoring;

-- Create common tables
CREATE TABLE IF NOT EXISTS users.users (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    username NVARCHAR(50) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    first_name NVARCHAR(50),
    last_name NVARCHAR(50),
    phone NVARCHAR(20),
    is_active BIT DEFAULT 1,
    is_verified BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE IF NOT EXISTS users.roles (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255),
    created_at DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE IF NOT EXISTS users.user_roles (
    user_id UNIQUEIDENTIFIER NOT NULL,
    role_id UNIQUEIDENTIFIER NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users.users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES users.roles(id) ON DELETE CASCADE
);

-- Energy Service Tables
CREATE TABLE IF NOT EXISTS energy.energy_sources (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    type NVARCHAR(50) NOT NULL, -- GRID, SOLAR, GENERATOR, INVERTER
    capacity DECIMAL(10,2),
    efficiency DECIMAL(5,2),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE IF NOT EXISTS energy.energy_readings (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    device_id UNIQUEIDENTIFIER NOT NULL,
    source_id UNIQUEIDENTIFIER NOT NULL,
    voltage DECIMAL(10,2),
    current DECIMAL(10,2),
    power DECIMAL(10,2),
    energy DECIMAL(10,2),
    frequency DECIMAL(5,2),
    power_factor DECIMAL(5,2),
    timestamp DATETIME2 NOT NULL,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (source_id) REFERENCES energy.energy_sources(id)
);

-- Device Service Tables
CREATE TABLE IF NOT EXISTS devices.devices (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    type NVARCHAR(50) NOT NULL,
    protocol NVARCHAR(20) NOT NULL,
    ip_address NVARCHAR(45),
    port INT,
    location NVARCHAR(255),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    is_online BIT DEFAULT 0,
    last_seen DATETIME2,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE IF NOT EXISTS devices.device_configurations (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    device_id UNIQUEIDENTIFIER NOT NULL,
    config_key NVARCHAR(100) NOT NULL,
    config_value NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (device_id) REFERENCES devices.devices(id) ON DELETE CASCADE
);

-- Circuit Management Tables
CREATE TABLE IF NOT EXISTS devices.circuits (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    parent_circuit_id UNIQUEIDENTIFIER,
    device_id UNIQUEIDENTIFIER,
    max_capacity DECIMAL(10,2),
    current_load DECIMAL(10,2),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (parent_circuit_id) REFERENCES devices.circuits(id),
    FOREIGN KEY (device_id) REFERENCES devices.devices(id)
);

CREATE TABLE IF NOT EXISTS devices.sub_panels (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    parent_panel_id UNIQUEIDENTIFIER,
    max_capacity DECIMAL(10,2),
    current_load DECIMAL(10,2),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (parent_panel_id) REFERENCES devices.sub_panels(id)
);

-- Analytics Service Tables
CREATE TABLE IF NOT EXISTS analytics.appliance_signatures (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    device_id UNIQUEIDENTIFIER NOT NULL,
    appliance_type NVARCHAR(50) NOT NULL,
    power_signature NVARCHAR(MAX), -- JSON array of power values
    voltage_signature NVARCHAR(MAX), -- JSON array of voltage values
    current_signature NVARCHAR(MAX), -- JSON array of current values
    confidence_score DECIMAL(5,2),
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE IF NOT EXISTS analytics.appliance_detections (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    device_id UNIQUEIDENTIFIER NOT NULL,
    appliance_type NVARCHAR(50) NOT NULL,
    power_consumption DECIMAL(10,2),
    detection_time DATETIME2 NOT NULL,
    confidence_score DECIMAL(5,2),
    created_at DATETIME2 DEFAULT GETUTCDATE()
);

-- Solar Panel Monitoring Tables
CREATE TABLE IF NOT EXISTS analytics.solar_inverters (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    model NVARCHAR(100),
    max_capacity DECIMAL(10,2),
    efficiency DECIMAL(5,2),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE IF NOT EXISTS analytics.solar_panels (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    inverter_id UNIQUEIDENTIFIER NOT NULL,
    string_id UNIQUEIDENTIFIER,
    panel_number INT,
    max_power DECIMAL(10,2),
    current_power DECIMAL(10,2),
    voltage DECIMAL(10,2),
    current DECIMAL(10,2),
    temperature DECIMAL(5,2),
    is_faulty BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (inverter_id) REFERENCES analytics.solar_inverters(id)
);

CREATE TABLE IF NOT EXISTS analytics.solar_strings (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    inverter_id UNIQUEIDENTIFIER NOT NULL,
    string_number INT,
    max_power DECIMAL(10,2),
    current_power DECIMAL(10,2),
    voltage DECIMAL(10,2),
    current DECIMAL(10,2),
    is_faulty BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (inverter_id) REFERENCES analytics.solar_inverters(id)
);

-- Community Benchmarking Tables
CREATE TABLE IF NOT EXISTS analytics.community_benchmarks (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    region NVARCHAR(100) NOT NULL,
    metric_type NVARCHAR(50) NOT NULL,
    average_value DECIMAL(10,2),
    median_value DECIMAL(10,2),
    percentile_25 DECIMAL(10,2),
    percentile_75 DECIMAL(10,2),
    sample_size INT,
    calculated_at DATETIME2 DEFAULT GETUTCDATE()
);

-- Billing Service Tables
CREATE TABLE IF NOT EXISTS billing.tariffs (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL,
    type NVARCHAR(50) NOT NULL, -- RESIDENTIAL, COMMERCIAL, INDUSTRIAL
    rate_per_kwh DECIMAL(10,4),
    fixed_charge DECIMAL(10,2),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE()
);

CREATE TABLE IF NOT EXISTS billing.bills (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    billing_period_start DATETIME2 NOT NULL,
    billing_period_end DATETIME2 NOT NULL,
    total_amount DECIMAL(10,2),
    energy_consumed DECIMAL(10,2),
    tariff_id UNIQUEIDENTIFIER,
    status NVARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (user_id) REFERENCES users.users(id),
    FOREIGN KEY (tariff_id) REFERENCES billing.tariffs(id)
);

-- Notification Service Tables
CREATE TABLE IF NOT EXISTS notifications.notifications (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    user_id UNIQUEIDENTIFIER NOT NULL,
    title NVARCHAR(255) NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    type NVARCHAR(50) NOT NULL,
    priority NVARCHAR(20) DEFAULT 'MEDIUM',
    is_read BIT DEFAULT 0,
    sent_at DATETIME2,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (user_id) REFERENCES users.users(id)
);

-- Feature Flag Service Tables
CREATE TABLE IF NOT EXISTS feature_flags.feature_flags (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255),
    is_enabled BIT DEFAULT 0,
    target_audience NVARCHAR(50) DEFAULT 'ALL',
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    updated_at DATETIME2 DEFAULT GETUTCDATE()
);

-- Device Verification Service Tables
CREATE TABLE IF NOT EXISTS device_verification.device_verifications (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    device_id UNIQUEIDENTIFIER NOT NULL,
    verification_code NVARCHAR(10) NOT NULL,
    status NVARCHAR(20) DEFAULT 'PENDING',
    verified_at DATETIME2,
    expires_at DATETIME2 NOT NULL,
    created_at DATETIME2 DEFAULT GETUTCDATE(),
    FOREIGN KEY (device_id) REFERENCES devices.devices(id)
);

-- Appliance Monitoring Service Tables
CREATE TABLE IF NOT EXISTS appliance_monitoring.appliance_events (
    id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
    device_id UNIQUEIDENTIFIER NOT NULL,
    appliance_id NVARCHAR(100),
    event_type NVARCHAR(50) NOT NULL,
    event_data NVARCHAR(MAX),
    timestamp DATETIME2 NOT NULL,
    created_at DATETIME2 DEFAULT GETUTCDATE()
);

-- Create indexes for performance
CREATE INDEX IX_energy_readings_device_id ON energy.energy_readings(device_id);
CREATE INDEX IX_energy_readings_timestamp ON energy.energy_readings(timestamp);
CREATE INDEX IX_devices_type ON devices.devices(type);
CREATE INDEX IX_devices_is_online ON devices.devices(is_online);
CREATE INDEX IX_appliance_detections_device_id ON analytics.appliance_detections(device_id);
CREATE INDEX IX_appliance_detections_detection_time ON analytics.appliance_detections(detection_time);
CREATE INDEX IX_bills_user_id ON billing.bills(user_id);
CREATE INDEX IX_bills_billing_period ON billing.bills(billing_period_start, billing_period_end);
CREATE INDEX IX_notifications_user_id ON notifications.notifications(user_id);
CREATE INDEX IX_notifications_is_read ON notifications.notifications(is_read);

-- Insert default data
INSERT INTO users.roles (name, description) VALUES 
('ADMIN', 'System Administrator'),
('USER', 'Regular User'),
('PARTNER', 'Business Partner'),
('INSTALLER', 'Device Installer')
ON CONFLICT (name) DO NOTHING;

INSERT INTO billing.tariffs (name, type, rate_per_kwh, fixed_charge) VALUES
('Residential Basic', 'RESIDENTIAL', 0.25, 0.00),
('Commercial Standard', 'COMMERCIAL', 0.35, 50.00),
('Industrial Premium', 'INDUSTRIAL', 0.45, 100.00)
ON CONFLICT (name) DO NOTHING;

INSERT INTO feature_flags.feature_flags (name, description, is_enabled) VALUES
('appliance_recognition', 'AI Appliance Recognition Feature', 1),
('circuit_management', 'Circuit Management Feature', 1),
('solar_monitoring', 'Solar Panel Monitoring Feature', 1),
('community_benchmarking', 'Community Benchmarking Feature', 1),
('partner_services', 'Partner Services Feature', 1)
ON CONFLICT (name) DO NOTHING;

-- Create stored procedures for common operations
CREATE OR ALTER PROCEDURE analytics.GetApplianceUsage
    @DeviceId UNIQUEIDENTIFIER,
    @StartDate DATETIME2,
    @EndDate DATETIME2
AS
BEGIN
    SELECT 
        appliance_type,
        SUM(power_consumption) as total_consumption,
        AVG(power_consumption) as average_consumption,
        COUNT(*) as detection_count
    FROM analytics.appliance_detections
    WHERE device_id = @DeviceId
        AND detection_time BETWEEN @StartDate AND @EndDate
    GROUP BY appliance_type
    ORDER BY total_consumption DESC;
END;

CREATE OR ALTER PROCEDURE analytics.GetCircuitLoadData
    @CircuitId UNIQUEIDENTIFIER
AS
BEGIN
    SELECT 
        c.name as circuit_name,
        c.max_capacity,
        c.current_load,
        (c.current_load / c.max_capacity * 100) as load_percentage,
        c.is_active
    FROM devices.circuits c
    WHERE c.id = @CircuitId;
END;

-- Create views for common queries
CREATE VIEW analytics.EnergyConsumptionSummary AS
SELECT 
    er.device_id,
    es.name as source_name,
    es.type as source_type,
    DATE(er.timestamp) as consumption_date,
    SUM(er.energy) as total_energy,
    AVG(er.power) as average_power,
    MAX(er.power) as peak_power
FROM energy.energy_readings er
JOIN energy.energy_sources es ON er.source_id = es.id
WHERE er.timestamp >= DATEADD(day, -30, GETUTCDATE())
GROUP BY er.device_id, es.name, es.type, DATE(er.timestamp);

CREATE VIEW devices.DeviceStatusSummary AS
SELECT 
    d.id,
    d.name,
    d.type,
    d.is_online,
    d.last_seen,
    CASE 
        WHEN d.last_seen > DATEADD(minute, -5, GETUTCDATE()) THEN 'ONLINE'
        WHEN d.last_seen > DATEADD(hour, -1, GETUTCDATE()) THEN 'RECENTLY_OFFLINE'
        ELSE 'OFFLINE'
    END as status
FROM devices.devices d;

PRINT 'SmartWatts Azure SQL Database migration completed successfully!';
