CREATE TABLE energy (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    source VARCHAR(50) NOT NULL,
    voltage DOUBLE PRECISION,
    current DOUBLE PRECISION,
    power DOUBLE PRECISION,
    energy DOUBLE PRECISION,
    status VARCHAR(50)
);

CREATE INDEX idx_energy_device_id ON energy(device_id);
CREATE INDEX idx_energy_timestamp ON energy(timestamp); 