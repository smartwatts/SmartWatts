-- Add device verification and trust level fields
ALTER TABLE devices 
ADD COLUMN trust_level VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
ADD COLUMN device_auth_secret VARCHAR(255) UNIQUE,
ADD COLUMN verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
ADD COLUMN verification_notes TEXT,
ADD COLUMN sample_payload TEXT,
ADD COLUMN verification_request_date TIMESTAMP,
ADD COLUMN verification_review_date TIMESTAMP,
ADD COLUMN verification_reviewer UUID;

-- Create index for trust level queries
CREATE INDEX idx_devices_trust_level ON devices(trust_level);

-- Create index for verification status queries
CREATE INDEX idx_devices_verification_status ON devices(verification_status);

-- Create index for auth secret lookups
CREATE INDEX idx_devices_auth_secret ON devices(device_auth_secret);

-- Update existing devices to have appropriate trust levels
-- SmartWatts OEM devices should be marked as OEM_LOCKED
UPDATE devices 
SET trust_level = 'OEM_LOCKED', 
    verification_status = 'APPROVED',
    is_verified = true,
    verification_date = CURRENT_TIMESTAMP
WHERE manufacturer IN ('SmartWatts', 'SmartWatts OEM', 'SmartWatts Certified')
   OR device_id LIKE 'SW_%'
   OR device_id LIKE 'OEM_%';

-- Mark all other devices as UNVERIFIED initially
UPDATE devices 
SET trust_level = 'UNVERIFIED',
    verification_status = 'PENDING',
    is_verified = false
WHERE trust_level IS NULL OR trust_level = 'UNVERIFIED';
