CREATE TABLE devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50),
    location VARCHAR(255),
    last_seen TIMESTAMP
);

CREATE INDEX idx_devices_name ON devices(name);
CREATE INDEX idx_devices_type ON devices(type); 