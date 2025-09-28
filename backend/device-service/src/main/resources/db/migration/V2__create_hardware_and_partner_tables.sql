-- Create partners table
CREATE TABLE partners (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id VARCHAR(50) NOT NULL UNIQUE,
    partner_name VARCHAR(255) NOT NULL,
    partner_type VARCHAR(50) NOT NULL,
    contact_person VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    business_license VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    qr_code_url TEXT,
    total_installations INTEGER NOT NULL DEFAULT 0,
    total_commission DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    commission_rate DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    bank_account VARCHAR(50),
    bank_name VARCHAR(255),
    account_name VARCHAR(255),
    tax_id VARCHAR(100),
    ndpr_consent TEXT,
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create hardware_devices table
CREATE TABLE hardware_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mac_address VARCHAR(50) NOT NULL UNIQUE,
    device_type VARCHAR(50) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    model_number VARCHAR(100) NOT NULL,
    firmware_version VARCHAR(50) NOT NULL,
    is_certified BOOLEAN NOT NULL DEFAULT TRUE,
    is_activated BOOLEAN NOT NULL DEFAULT FALSE,
    activation_date TIMESTAMP,
    activation_token VARCHAR(255) NOT NULL,
    is_token_used BOOLEAN NOT NULL DEFAULT FALSE,
    partner_id VARCHAR(50),
    installer_id VARCHAR(50),
    site_location VARCHAR(255),
    organization_id VARCHAR(50),
    device_status VARCHAR(50) NOT NULL DEFAULT 'INACTIVE',
    purchase_price DECIMAL(10,2),
    warranty_expiry VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_partners_partner_id ON partners(partner_id);
CREATE INDEX idx_partners_verification_status ON partners(verification_status);
CREATE INDEX idx_hardware_devices_mac_address ON hardware_devices(mac_address);
CREATE INDEX idx_hardware_devices_device_type ON hardware_devices(device_type);
CREATE INDEX idx_hardware_devices_partner_id ON hardware_devices(partner_id);
CREATE INDEX idx_hardware_devices_organization_id ON hardware_devices(organization_id);

-- Add foreign key constraints
ALTER TABLE hardware_devices 
ADD CONSTRAINT fk_hardware_devices_partner 
FOREIGN KEY (partner_id) REFERENCES partners(partner_id);

-- Create commission tracking table
CREATE TABLE commission_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id VARCHAR(50) NOT NULL,
    hardware_device_id UUID NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- INSTALLATION, REFERRAL, INSURANCE, FINANCE
    amount DECIMAL(10,2) NOT NULL,
    commission_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED
    payment_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (partner_id) REFERENCES partners(partner_id),
    FOREIGN KEY (hardware_device_id) REFERENCES hardware_devices(id)
);

-- Create index for commission tracking
CREATE INDEX idx_commission_transactions_partner_id ON commission_transactions(partner_id);
CREATE INDEX idx_commission_transactions_status ON commission_transactions(status); 