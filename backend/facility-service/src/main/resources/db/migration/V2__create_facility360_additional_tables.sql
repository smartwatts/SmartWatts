-- Facility360 Database Migration V2
-- Create additional facility management tables

-- Create maintenance_schedules table
CREATE TABLE maintenance_schedules (
    id BIGSERIAL PRIMARY KEY,
    schedule_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    asset_id BIGINT REFERENCES assets(id),
    frequency_type VARCHAR(50) NOT NULL, -- DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    frequency_value INTEGER NOT NULL,
    last_maintenance_date TIMESTAMP,
    next_maintenance_date TIMESTAMP NOT NULL,
    estimated_duration_hours INTEGER,
    estimated_cost DECIMAL(10,2),
    assigned_technician VARCHAR(100),
    department VARCHAR(100),
    maintenance_type VARCHAR(50) NOT NULL, -- PREVENTIVE, INSPECTION, CALIBRATION
    instructions TEXT,
    required_tools TEXT,
    required_materials TEXT,
    safety_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create space_bookings table
CREATE TABLE space_bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    space_id BIGINT REFERENCES spaces(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    booked_by VARCHAR(100) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    attendees_count INTEGER,
    purpose VARCHAR(255),
    status VARCHAR(50) NOT NULL, -- CONFIRMED, PENDING, CANCELLED, COMPLETED
    recurring_pattern VARCHAR(50), -- NONE, DAILY, WEEKLY, MONTHLY
    recurring_end_date TIMESTAMP,
    special_requirements TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create fleet_trips table
CREATE TABLE fleet_trips (
    id BIGSERIAL PRIMARY KEY,
    trip_code VARCHAR(50) NOT NULL UNIQUE,
    vehicle_id BIGINT REFERENCES fleet(id),
    driver_name VARCHAR(100) NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    destination VARCHAR(255),
    start_location VARCHAR(255),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    distance_km DECIMAL(8,2),
    fuel_consumed DECIMAL(8,2),
    fuel_cost DECIMAL(10,2),
    toll_fees DECIMAL(10,2),
    parking_fees DECIMAL(10,2),
    other_expenses DECIMAL(10,2),
    total_cost DECIMAL(10,2),
    status VARCHAR(50) NOT NULL, -- PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create facility_incidents table
CREATE TABLE facility_incidents (
    id BIGSERIAL PRIMARY KEY,
    incident_code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    incident_type VARCHAR(50) NOT NULL, -- SECURITY, SAFETY, MAINTENANCE, ACCIDENT, OTHER
    severity VARCHAR(50) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    location VARCHAR(255),
    reported_by VARCHAR(100) NOT NULL,
    reported_date TIMESTAMP NOT NULL,
    incident_date TIMESTAMP,
    status VARCHAR(50) NOT NULL, -- REPORTED, INVESTIGATING, RESOLVED, CLOSED
    assigned_to VARCHAR(100),
    resolution TEXT,
    resolution_date TIMESTAMP,
    cost_impact DECIMAL(10,2),
    insurance_claim BOOLEAN DEFAULT FALSE,
    claim_number VARCHAR(100),
    attachments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create compliance_checklists table
CREATE TABLE compliance_checklists (
    id BIGSERIAL PRIMARY KEY,
    checklist_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL, -- SAFETY, SECURITY, ENVIRONMENTAL, REGULATORY
    frequency VARCHAR(50) NOT NULL, -- DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    last_completed_date TIMESTAMP,
    next_due_date TIMESTAMP NOT NULL,
    assigned_to VARCHAR(100),
    department VARCHAR(100),
    status VARCHAR(50) NOT NULL, -- PENDING, IN_PROGRESS, COMPLETED, OVERDUE
    total_items INTEGER NOT NULL,
    completed_items INTEGER DEFAULT 0,
    compliance_percentage DECIMAL(5,2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create checklist_items table
CREATE TABLE checklist_items (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT REFERENCES compliance_checklists(id),
    item_text TEXT NOT NULL,
    item_order INTEGER NOT NULL,
    is_required BOOLEAN DEFAULT TRUE,
    item_type VARCHAR(50) DEFAULT 'CHECKBOX', -- CHECKBOX, TEXT, NUMBER, DATE
    expected_value TEXT,
    instructions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create checklist_responses table
CREATE TABLE checklist_responses (
    id BIGSERIAL PRIMARY KEY,
    checklist_id BIGINT REFERENCES compliance_checklists(id),
    item_id BIGINT REFERENCES checklist_items(id),
    response_value TEXT,
    response_date TIMESTAMP NOT NULL,
    responded_by VARCHAR(100) NOT NULL,
    notes TEXT,
    attachments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes for better performance
CREATE INDEX idx_maintenance_schedules_schedule_code ON maintenance_schedules(schedule_code);
CREATE INDEX idx_maintenance_schedules_asset_id ON maintenance_schedules(asset_id);
CREATE INDEX idx_maintenance_schedules_next_maintenance_date ON maintenance_schedules(next_maintenance_date);

CREATE INDEX idx_space_bookings_booking_code ON space_bookings(booking_code);
CREATE INDEX idx_space_bookings_space_id ON space_bookings(space_id);
CREATE INDEX idx_space_bookings_start_time ON space_bookings(start_time);
CREATE INDEX idx_space_bookings_booked_by ON space_bookings(booked_by);

CREATE INDEX idx_fleet_trips_trip_code ON fleet_trips(trip_code);
CREATE INDEX idx_fleet_trips_vehicle_id ON fleet_trips(vehicle_id);
CREATE INDEX idx_fleet_trips_driver_name ON fleet_trips(driver_name);
CREATE INDEX idx_fleet_trips_start_time ON fleet_trips(start_time);

CREATE INDEX idx_facility_incidents_incident_code ON facility_incidents(incident_code);
CREATE INDEX idx_facility_incidents_incident_type ON facility_incidents(incident_type);
CREATE INDEX idx_facility_incidents_severity ON facility_incidents(severity);
CREATE INDEX idx_facility_incidents_status ON facility_incidents(status);

CREATE INDEX idx_compliance_checklists_checklist_code ON compliance_checklists(checklist_code);
CREATE INDEX idx_compliance_checklists_category ON compliance_checklists(category);
CREATE INDEX idx_compliance_checklists_next_due_date ON compliance_checklists(next_due_date);
CREATE INDEX idx_compliance_checklists_status ON compliance_checklists(status);

CREATE INDEX idx_checklist_items_checklist_id ON checklist_items(checklist_id);
CREATE INDEX idx_checklist_responses_checklist_id ON checklist_responses(checklist_id);
CREATE INDEX idx_checklist_responses_item_id ON checklist_responses(item_id);

-- Create triggers for updated_at
CREATE TRIGGER update_maintenance_schedules_updated_at BEFORE UPDATE ON maintenance_schedules FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_space_bookings_updated_at BEFORE UPDATE ON space_bookings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_fleet_trips_updated_at BEFORE UPDATE ON fleet_trips FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_facility_incidents_updated_at BEFORE UPDATE ON facility_incidents FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_compliance_checklists_updated_at BEFORE UPDATE ON compliance_checklists FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_checklist_items_updated_at BEFORE UPDATE ON checklist_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_checklist_responses_updated_at BEFORE UPDATE ON checklist_responses FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
