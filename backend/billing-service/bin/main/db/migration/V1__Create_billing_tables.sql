-- Billing Service Database Migration
-- Creates tables for bills, bill items, and tariffs

-- Bills table
CREATE TABLE bills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    bill_number VARCHAR(50) NOT NULL UNIQUE,
    bill_title VARCHAR(255),
    bill_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    billing_period_start TIMESTAMP NOT NULL,
    billing_period_end TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    issued_date TIMESTAMP NOT NULL,
    paid_date TIMESTAMP,
    total_consumption_kwh DECIMAL(10,4),
    total_amount DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2),
    discount_amount DECIMAL(12,2),
    final_amount DECIMAL(12,2) NOT NULL,
    amount_paid DECIMAL(12,2) DEFAULT 0.00,
    balance_due DECIMAL(12,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    exchange_rate DECIMAL(8,4) DEFAULT 1.0000,
    payment_method VARCHAR(50),
    payment_reference VARCHAR(100),
    disco_reference VARCHAR(100),
    meter_number VARCHAR(50),
    account_number VARCHAR(50),
    customer_name VARCHAR(255),
    customer_address TEXT,
    customer_phone VARCHAR(20),
    customer_email VARCHAR(255),
    billing_address TEXT,
    notes TEXT,
    terms_conditions TEXT,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurring_frequency VARCHAR(50),
    next_billing_date TIMESTAMP,
    is_estimated BOOLEAN DEFAULT FALSE,
    estimation_reason VARCHAR(255),
    is_disputed BOOLEAN DEFAULT FALSE,
    dispute_reason VARCHAR(255),
    dispute_date TIMESTAMP,
    dispute_resolved_date TIMESTAMP,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bill items table
CREATE TABLE bill_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_id UUID NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    item_description TEXT,
    item_type VARCHAR(50) NOT NULL,
    quantity DECIMAL(10,4),
    unit VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10,4) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    tax_rate DECIMAL(5,2),
    tax_amount DECIMAL(12,2),
    discount_rate DECIMAL(5,2),
    discount_amount DECIMAL(12,2),
    total_amount DECIMAL(12,2) NOT NULL,
    start_reading DECIMAL(10,4),
    end_reading DECIMAL(10,4),
    consumption_kwh DECIMAL(10,4),
    rate_per_kwh DECIMAL(8,4),
    peak_hours INTEGER,
    off_peak_hours INTEGER,
    night_hours INTEGER,
    peak_consumption_kwh DECIMAL(10,4),
    off_peak_consumption_kwh DECIMAL(10,4),
    night_consumption_kwh DECIMAL(10,4),
    peak_rate DECIMAL(8,4),
    off_peak_rate DECIMAL(8,4),
    night_rate DECIMAL(8,4),
    peak_amount DECIMAL(12,2),
    off_peak_amount DECIMAL(12,2),
    night_amount DECIMAL(12,2),
    service_charge DECIMAL(12,2),
    meter_rental DECIMAL(12,2),
    demand_charge DECIMAL(12,2),
    fuel_adjustment DECIMAL(12,2),
    capacity_charge DECIMAL(12,2),
    transmission_charge DECIMAL(12,2),
    distribution_charge DECIMAL(12,2),
    regulatory_charge DECIMAL(12,2),
    environmental_charge DECIMAL(12,2),
    other_charges DECIMAL(12,2),
    notes TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Tariffs table
CREATE TABLE tariffs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tariff_name VARCHAR(255) NOT NULL,
    tariff_code VARCHAR(50) NOT NULL UNIQUE,
    tariff_type VARCHAR(50) NOT NULL,
    customer_category VARCHAR(50) NOT NULL,
    effective_date TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by UUID,
    approved_date TIMESTAMP,
    approved_by_authority VARCHAR(255),
    approval_reference VARCHAR(100),
    base_rate DECIMAL(8,4),
    peak_rate DECIMAL(8,4),
    off_peak_rate DECIMAL(8,4),
    night_rate DECIMAL(8,4),
    service_charge DECIMAL(12,2),
    meter_rental DECIMAL(12,2),
    demand_charge DECIMAL(12,2),
    capacity_charge DECIMAL(12,2),
    transmission_charge DECIMAL(12,2),
    distribution_charge DECIMAL(12,2),
    regulatory_charge DECIMAL(12,2),
    environmental_charge DECIMAL(12,2),
    fuel_adjustment_rate DECIMAL(8,4),
    tax_rate DECIMAL(5,2),
    minimum_charge DECIMAL(12,2),
    maximum_charge DECIMAL(12,2),
    peak_hours_start VARCHAR(10),
    peak_hours_end VARCHAR(10),
    off_peak_hours_start VARCHAR(10),
    off_peak_hours_end VARCHAR(10),
    night_hours_start VARCHAR(10),
    night_hours_end VARCHAR(10),
    minimum_consumption_kwh DECIMAL(10,4),
    maximum_consumption_kwh DECIMAL(10,4),
    tier_1_limit DECIMAL(10,4),
    tier_1_rate DECIMAL(8,4),
    tier_2_limit DECIMAL(10,4),
    tier_2_rate DECIMAL(8,4),
    tier_3_limit DECIMAL(10,4),
    tier_3_rate DECIMAL(8,4),
    tier_4_rate DECIMAL(8,4),
    currency VARCHAR(3) NOT NULL DEFAULT 'NGN',
    disco_code VARCHAR(50),
    disco_name VARCHAR(255),
    region VARCHAR(100),
    state VARCHAR(100),
    city VARCHAR(100),
    description TEXT,
    notes TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_bills_user_id ON bills(user_id);
CREATE INDEX idx_bills_bill_number ON bills(bill_number);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_bills_bill_type ON bills(bill_type);
CREATE INDEX idx_bills_billing_period ON bills(billing_period_start, billing_period_end);
CREATE INDEX idx_bills_due_date ON bills(due_date);
CREATE INDEX idx_bills_issued_date ON bills(issued_date);
CREATE INDEX idx_bills_is_disputed ON bills(is_disputed);
CREATE INDEX idx_bills_is_recurring ON bills(is_recurring);
CREATE INDEX idx_bills_is_estimated ON bills(is_estimated);

CREATE INDEX idx_bill_items_bill_id ON bill_items(bill_id);
CREATE INDEX idx_bill_items_item_type ON bill_items(item_type);
CREATE INDEX idx_bill_items_created_at ON bill_items(created_at);

CREATE INDEX idx_tariffs_tariff_code ON tariffs(tariff_code);
CREATE INDEX idx_tariffs_tariff_type ON tariffs(tariff_type);
CREATE INDEX idx_tariffs_customer_category ON tariffs(customer_category);
CREATE INDEX idx_tariffs_is_active ON tariffs(is_active);
CREATE INDEX idx_tariffs_is_approved ON tariffs(is_approved);
CREATE INDEX idx_tariffs_effective_date ON tariffs(effective_date);
CREATE INDEX idx_tariffs_expiry_date ON tariffs(expiry_date);
CREATE INDEX idx_tariffs_disco_code ON tariffs(disco_code);
CREATE INDEX idx_tariffs_region ON tariffs(region);
CREATE INDEX idx_tariffs_state ON tariffs(state);
CREATE INDEX idx_tariffs_city ON tariffs(city);

-- Foreign key constraints (commented out as they reference external services)
-- ALTER TABLE bills ADD CONSTRAINT fk_bills_user_id FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE bill_items ADD CONSTRAINT fk_bill_items_bill_id FOREIGN KEY (bill_id) REFERENCES bills(id);
-- ALTER TABLE tariffs ADD CONSTRAINT fk_tariffs_approved_by FOREIGN KEY (approved_by) REFERENCES users(id);

-- Triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_bills_updated_at BEFORE UPDATE ON bills
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bill_items_updated_at BEFORE UPDATE ON bill_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tariffs_updated_at BEFORE UPDATE ON tariffs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default tariff for testing
INSERT INTO tariffs (
    tariff_name,
    tariff_code,
    tariff_type,
    customer_category,
    effective_date,
    is_active,
    is_approved,
    base_rate,
    peak_rate,
    off_peak_rate,
    night_rate,
    service_charge,
    meter_rental,
    tax_rate,
    currency,
    disco_code,
    disco_name,
    region,
    state,
    city,
    description
) VALUES (
    'Default Residential Tariff',
    'R1_DEFAULT',
    'RESIDENTIAL',
    'R1_SMALL_RESIDENTIAL',
    CURRENT_TIMESTAMP,
    TRUE,
    TRUE,
    25.00,
    35.00,
    20.00,
    15.00,
    500.00,
    200.00,
    7.50,
    'NGN',
    'IKEDC',
    'Ikeja Electric Distribution Company',
    'South West',
    'Lagos',
    'Lagos',
    'Default residential tariff for small consumers'
);

-- Insert sample bill for testing
INSERT INTO bills (
    user_id,
    bill_number,
    bill_title,
    bill_type,
    status,
    billing_period_start,
    billing_period_end,
    due_date,
    issued_date,
    total_consumption_kwh,
    total_amount,
    tax_amount,
    final_amount,
    balance_due,
    currency,
    customer_name,
    customer_email
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000', -- Sample UUID
    'BILL-20241201001',
    'Electricity Bill - December 2024',
    'GRID_ELECTRICITY',
    'PENDING',
    '2024-12-01 00:00:00',
    '2024-12-31 23:59:59',
    '2025-01-15 23:59:59',
    CURRENT_TIMESTAMP,
    150.50,
    5000.00,
    375.00,
    5375.00,
    5375.00,
    'NGN',
    'John Doe',
    'john.doe@example.com'
);

-- Insert sample bill items for testing
INSERT INTO bill_items (
    bill_id,
    item_name,
    item_description,
    item_type,
    quantity,
    unit,
    unit_price,
    subtotal,
    tax_amount,
    total_amount,
    consumption_kwh,
    rate_per_kwh,
    service_charge,
    notes
) VALUES (
    (SELECT id FROM bills WHERE bill_number = 'BILL-20241201001'),
    'Electricity Consumption',
    'Total electricity consumption for billing period',
    'ELECTRICITY_CONSUMPTION',
    150.50,
    'kWh',
    25.00,
    3762.50,
    282.19,
    4044.69,
    150.50,
    25.00,
    500.00,
    'Standard electricity consumption'
),
(
    (SELECT id FROM bills WHERE bill_number = 'BILL-20241201001'),
    'Service Charge',
    'Monthly service charge',
    'SERVICE_CHARGE',
    1,
    'Monthly',
    500.00,
    500.00,
    37.50,
    537.50,
    NULL,
    NULL,
    500.00,
    'Monthly service charge'
),
(
    (SELECT id FROM bills WHERE bill_number = 'BILL-20241201001'),
    'Value Added Tax (VAT)',
    'VAT on electricity consumption',
    'TAX',
    1,
    'Tax',
    375.00,
    375.00,
    NULL,
    375.00,
    NULL,
    NULL,
    NULL,
    '7.5% VAT on total consumption'
); 