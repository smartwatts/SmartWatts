-- Create energy_sources table
CREATE TABLE energy_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    source_name VARCHAR(200) NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    capacity_kw DECIMAL(10,3),
    efficiency_percent DECIMAL(5,2),
    installation_date TIMESTAMP,
    last_maintenance_date TIMESTAMP,
    next_maintenance_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    location_lat DECIMAL(10,8),
    location_lng DECIMAL(11,8),
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),
    warranty_expiry TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create energy_readings table
CREATE TABLE energy_readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    meter_number VARCHAR(50),
    reading_timestamp TIMESTAMP NOT NULL,
    voltage DECIMAL(10,2),
    current DECIMAL(10,2),
    power DECIMAL(10,2),
    energy_consumed DECIMAL(10,4),
    frequency DECIMAL(5,2),
    power_factor DECIMAL(3,2),
    source_type VARCHAR(20) NOT NULL DEFAULT 'GRID',
    reading_type VARCHAR(20) NOT NULL DEFAULT 'REAL_TIME',
    quality_score DECIMAL(3,2),
    is_processed BOOLEAN DEFAULT false,
    processing_timestamp TIMESTAMP,
    raw_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create energy_consumption table
CREATE TABLE energy_consumption (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    meter_number VARCHAR(50),
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    period_type VARCHAR(20) NOT NULL,
    source_type VARCHAR(20) NOT NULL DEFAULT 'GRID',
    total_energy DECIMAL(12,4) NOT NULL,
    peak_power DECIMAL(10,2),
    average_power DECIMAL(10,2),
    minimum_power DECIMAL(10,2),
    total_cost DECIMAL(10,2),
    tariff_rate DECIMAL(8,4),
    reading_count INTEGER,
    quality_score DECIMAL(3,2),
    is_billed BOOLEAN DEFAULT false,
    billing_reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create energy_alerts table
CREATE TABLE energy_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id VARCHAR(100),
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    threshold_value DECIMAL(10,2),
    actual_value DECIMAL(10,2),
    alert_timestamp TIMESTAMP NOT NULL,
    is_acknowledged BOOLEAN DEFAULT false,
    acknowledged_at TIMESTAMP,
    acknowledged_by UUID,
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMP,
    resolved_by UUID,
    resolution_notes TEXT,
    notification_sent BOOLEAN DEFAULT false,
    notification_sent_at TIMESTAMP,
    notification_channels VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_energy_sources_user_id ON energy_sources(user_id);
CREATE INDEX idx_energy_sources_source_type ON energy_sources(source_type);
CREATE INDEX idx_energy_sources_status ON energy_sources(status);
CREATE INDEX idx_energy_sources_next_maintenance ON energy_sources(next_maintenance_date);

CREATE INDEX idx_energy_readings_user_id ON energy_readings(user_id);
CREATE INDEX idx_energy_readings_device_id ON energy_readings(device_id);
CREATE INDEX idx_energy_readings_timestamp ON energy_readings(reading_timestamp);
CREATE INDEX idx_energy_readings_source_type ON energy_readings(source_type);
CREATE INDEX idx_energy_readings_is_processed ON energy_readings(is_processed);
CREATE INDEX idx_energy_readings_user_device_time ON energy_readings(user_id, device_id, reading_timestamp);

CREATE INDEX idx_energy_consumption_user_id ON energy_consumption(user_id);
CREATE INDEX idx_energy_consumption_device_id ON energy_consumption(device_id);
CREATE INDEX idx_energy_consumption_period_start ON energy_consumption(period_start);
CREATE INDEX idx_energy_consumption_period_type ON energy_consumption(period_type);
CREATE INDEX idx_energy_consumption_source_type ON energy_consumption(source_type);
CREATE INDEX idx_energy_consumption_is_billed ON energy_consumption(is_billed);
CREATE INDEX idx_energy_consumption_user_period ON energy_consumption(user_id, period_start, period_end);

CREATE INDEX idx_energy_alerts_user_id ON energy_alerts(user_id);
CREATE INDEX idx_energy_alerts_device_id ON energy_alerts(device_id);
CREATE INDEX idx_energy_alerts_alert_type ON energy_alerts(alert_type);
CREATE INDEX idx_energy_alerts_severity ON energy_alerts(severity);
CREATE INDEX idx_energy_alerts_timestamp ON energy_alerts(alert_timestamp);
CREATE INDEX idx_energy_alerts_is_acknowledged ON energy_alerts(is_acknowledged);
CREATE INDEX idx_energy_alerts_is_resolved ON energy_alerts(is_resolved);
CREATE INDEX idx_energy_alerts_notification_sent ON energy_alerts(notification_sent);
CREATE INDEX idx_energy_alerts_user_active ON energy_alerts(user_id, is_resolved, alert_timestamp);

-- Add foreign key constraints (if user service is in same database)
-- ALTER TABLE energy_readings ADD CONSTRAINT fk_energy_readings_user_id FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE energy_consumption ADD CONSTRAINT fk_energy_consumption_user_id FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE energy_alerts ADD CONSTRAINT fk_energy_alerts_user_id FOREIGN KEY (user_id) REFERENCES users(id); 