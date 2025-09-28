-- Facility360 Database Migration V1
-- Create core facility management tables

-- Create assets table
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    asset_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    asset_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    building VARCHAR(100),
    floor VARCHAR(50),
    room VARCHAR(50),
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),
    installation_date TIMESTAMP,
    warranty_expiry_date TIMESTAMP,
    purchase_cost DECIMAL(10,2),
    current_value DECIMAL(10,2),
    assigned_to VARCHAR(100),
    department VARCHAR(100),
    notes TEXT,
    image_url VARCHAR(500),
    qr_code VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create work_orders table
CREATE TABLE work_orders (
    id BIGSERIAL PRIMARY KEY,
    work_order_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    asset_id BIGINT REFERENCES assets(id),
    location VARCHAR(255),
    assigned_technician VARCHAR(100),
    requested_by VARCHAR(100),
    department VARCHAR(100),
    requested_date TIMESTAMP,
    scheduled_date TIMESTAMP,
    start_date TIMESTAMP,
    completed_date TIMESTAMP,
    due_date TIMESTAMP,
    estimated_cost DECIMAL(10,2),
    actual_cost DECIMAL(10,2),
    materials_used TEXT,
    work_performed TEXT,
    notes TEXT,
    attachments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create spaces table
CREATE TABLE spaces (
    id BIGSERIAL PRIMARY KEY,
    space_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    building VARCHAR(100) NOT NULL,
    floor VARCHAR(50) NOT NULL,
    wing VARCHAR(50),
    room_number VARCHAR(50),
    area DECIMAL(8,2),
    area_unit VARCHAR(20),
    capacity INTEGER,
    department VARCHAR(100),
    assigned_to VARCHAR(100),
    contact_person VARCHAR(100),
    phone VARCHAR(50),
    email VARCHAR(100),
    access_level VARCHAR(50),
    special_requirements TEXT,
    notes TEXT,
    floor_plan_url VARCHAR(500),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create fleet table
CREATE TABLE fleet (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    license_plate VARCHAR(50) NOT NULL,
    vin VARCHAR(100),
    make VARCHAR(100),
    model VARCHAR(100),
    model_year INTEGER,
    color VARCHAR(50),
    fuel_type VARCHAR(50),
    fuel_capacity DECIMAL(8,2),
    fuel_unit VARCHAR(20),
    current_fuel_level DECIMAL(8,2),
    mileage INTEGER,
    mileage_unit VARCHAR(20),
    assigned_driver VARCHAR(100),
    department VARCHAR(100),
    location VARCHAR(255),
    last_maintenance_date TIMESTAMP,
    next_maintenance_date TIMESTAMP,
    purchase_cost DECIMAL(10,2),
    current_value DECIMAL(10,2),
    insurance_provider VARCHAR(100),
    insurance_expiry_date TIMESTAMP,
    registration_number VARCHAR(100),
    registration_expiry_date TIMESTAMP,
    notes TEXT,
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes for better performance
CREATE INDEX idx_assets_asset_code ON assets(asset_code);
CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_assets_asset_type ON assets(asset_type);
CREATE INDEX idx_assets_location ON assets(location);
CREATE INDEX idx_assets_department ON assets(department);

CREATE INDEX idx_work_orders_work_order_number ON work_orders(work_order_number);
CREATE INDEX idx_work_orders_status ON work_orders(status);
CREATE INDEX idx_work_orders_priority ON work_orders(priority);
CREATE INDEX idx_work_orders_asset_id ON work_orders(asset_id);
CREATE INDEX idx_work_orders_assigned_technician ON work_orders(assigned_technician);

CREATE INDEX idx_spaces_space_code ON spaces(space_code);
CREATE INDEX idx_spaces_type ON spaces(type);
CREATE INDEX idx_spaces_status ON spaces(status);
CREATE INDEX idx_spaces_building ON spaces(building);
CREATE INDEX idx_spaces_department ON spaces(department);

CREATE INDEX idx_fleet_vehicle_id ON fleet(vehicle_id);
CREATE INDEX idx_fleet_status ON fleet(status);
CREATE INDEX idx_fleet_type ON fleet(type);
CREATE INDEX idx_fleet_department ON fleet(department);
CREATE INDEX idx_fleet_assigned_driver ON fleet(assigned_driver);

-- Create audit trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_assets_updated_at BEFORE UPDATE ON assets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_work_orders_updated_at BEFORE UPDATE ON work_orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_spaces_updated_at BEFORE UPDATE ON spaces FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_fleet_updated_at BEFORE UPDATE ON fleet FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
