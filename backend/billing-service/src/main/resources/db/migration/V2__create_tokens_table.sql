CREATE TABLE tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_id UUID,
    token_code VARCHAR(50) NOT NULL UNIQUE,
    meter_number VARCHAR(50) NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    units_purchased DECIMAL(10,3) NOT NULL,
    units_consumed DECIMAL(10,3) NOT NULL DEFAULT 0,
    units_remaining DECIMAL(10,3) NOT NULL,
    rate_per_unit DECIMAL(10,4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    purchase_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activation_date TIMESTAMP,
    expiry_date TIMESTAMP,
    payment_method VARCHAR(50),
    transaction_reference VARCHAR(100),
    disco_reference VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_tokens_user_id ON tokens(user_id);
CREATE INDEX idx_tokens_device_id ON tokens(device_id);
CREATE INDEX idx_tokens_token_code ON tokens(token_code);
CREATE INDEX idx_tokens_meter_number ON tokens(meter_number);
CREATE INDEX idx_tokens_status ON tokens(status);
CREATE INDEX idx_tokens_purchase_date ON tokens(purchase_date);
CREATE INDEX idx_tokens_expiry_date ON tokens(expiry_date);
CREATE INDEX idx_tokens_user_status ON tokens(user_id, status);
CREATE INDEX idx_tokens_user_period ON tokens(user_id, purchase_date); 