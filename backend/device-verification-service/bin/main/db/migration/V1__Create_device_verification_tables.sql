-- Device Verification & Activation Service Database Schema
-- V1: Initial schema creation

-- Device Trust Categories
CREATE TYPE device_trust_category AS ENUM ('OEM_LOCKED', 'OFFLINE_LOCKED', 'UNVERIFIED', 'EXPIRED');

-- Device Status
CREATE TYPE device_status AS ENUM ('ACTIVE', 'INACTIVE', 'EXPIRED', 'SUSPENDED', 'TAMPERED');

-- Installer Tiers
CREATE TYPE installer_tier AS ENUM ('BASIC', 'CERTIFIED', 'PREMIUM', 'ENTERPRISE');

-- Device Verification Table
CREATE TABLE device_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(255) NOT NULL UNIQUE,
    device_type VARCHAR(100) NOT NULL,
    hardware_id VARCHAR(255) NOT NULL,
    firmware_hash VARCHAR(255),
    firmware_version VARCHAR(100),
    trust_category device_trust_category NOT NULL DEFAULT 'UNVERIFIED',
    status device_status NOT NULL DEFAULT 'INACTIVE',
    customer_type VARCHAR(50) NOT NULL, -- 'RESIDENTIAL' or 'COMMERCIAL'
    customer_id UUID,
    installer_id UUID,
    installer_tier installer_tier DEFAULT 'BASIC',
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    activated_at TIMESTAMP,
    expires_at TIMESTAMP,
    activation_token TEXT,
    activation_attempts INTEGER DEFAULT 0,
    last_activation_attempt TIMESTAMP,
    tamper_detected BOOLEAN DEFAULT FALSE,
    tamper_details TEXT,
    docker_startup_valid BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Activation Tokens Table
CREATE TABLE activation_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(255) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    token_type VARCHAR(50) NOT NULL, -- 'ONLINE', 'OFFLINE'
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activated_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    customer_type VARCHAR(50) NOT NULL,
    validity_days INTEGER NOT NULL, -- 365 for residential, 90 for commercial
    is_active BOOLEAN DEFAULT TRUE,
    revoked_at TIMESTAMP,
    revoked_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit Log Table
CREATE TABLE verification_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    action_details TEXT,
    user_id UUID,
    installer_id UUID,
    ip_address VARCHAR(255),
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Installer Management Table
CREATE TABLE installers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    installer_code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    tier VARCHAR(20) DEFAULT 'BASIC',
    is_active BOOLEAN DEFAULT TRUE,
    auto_approval_limit INTEGER DEFAULT 10,
    location_lat DECIMAL(10, 8),
    location_lng DECIMAL(11, 8),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Device Renewal Requests Table
CREATE TABLE renewal_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(255) NOT NULL,
    customer_id UUID NOT NULL,
    request_type VARCHAR(50) NOT NULL, -- 'RENEWAL', 'UPGRADE', 'TRANSFER'
    current_plan VARCHAR(100),
    requested_plan VARCHAR(100),
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING', -- 'PENDING', 'APPROVED', 'REJECTED'
    approved_by UUID,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_device_verifications_device_id ON device_verifications(device_id);
CREATE INDEX idx_device_verifications_trust_category ON device_verifications(trust_category);
CREATE INDEX idx_device_verifications_status ON device_verifications(status);
CREATE INDEX idx_device_verifications_customer_type ON device_verifications(customer_type);
CREATE INDEX idx_device_verifications_expires_at ON device_verifications(expires_at);
CREATE INDEX idx_activation_tokens_device_id ON activation_tokens(device_id);
CREATE INDEX idx_activation_tokens_expires_at ON activation_tokens(expires_at);
CREATE INDEX idx_audit_logs_device_id ON verification_audit_logs(device_id);
CREATE INDEX idx_audit_logs_created_at ON verification_audit_logs(created_at);
CREATE INDEX idx_installers_tier ON installers(tier);
CREATE INDEX idx_renewal_requests_device_id ON renewal_requests(device_id);
CREATE INDEX idx_renewal_requests_status ON renewal_requests(status);

-- Foreign key constraints (will be added when user service integration is complete)
-- For now, we'll use the tables without foreign key constraints

-- Insert default installer tiers
INSERT INTO installers (installer_code, name, email, tier, auto_approval_limit) VALUES
('OEM_SMARTWATTS', 'SmartWatts OEM', 'oem@smartwatts.ng', 'ENTERPRISE', 1000),
('CERTIFIED_BASIC', 'Basic Certified Installer', 'basic@installer.ng', 'BASIC', 10),
('CERTIFIED_PREMIUM', 'Premium Certified Installer', 'premium@installer.ng', 'PREMIUM', 100);

-- Insert sample device verification for testing
INSERT INTO device_verifications (
    device_id, 
    device_type, 
    hardware_id, 
    trust_category, 
    status, 
    customer_type, 
    activated_at, 
    expires_at
) VALUES (
    'TEST-DEVICE-001',
    'SMART_METER',
    'HW-001-ABC-123',
    'OEM_LOCKED',
    'ACTIVE',
    'RESIDENTIAL',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '1 year'
);
