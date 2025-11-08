#!/bin/bash
set -e

echo "=========================================="
echo "SmartWatts Staging Test Data Seeding"
echo "=========================================="

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Database configuration
DB_HOST="${STAGING_POSTGRES_HOST:-localhost}"
DB_PORT="${STAGING_POSTGRES_PORT:-5433}"
DB_USER="${STAGING_POSTGRES_USER:-smartwatts_staging}"
DB_PASSWORD="${STAGING_POSTGRES_PASSWORD:-staging_password_123}"

echo -e "${GREEN}Seeding staging database with test data...${NC}"

# Wait for database to be ready
echo "Waiting for database to be ready..."
until PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "smartwatts_users_staging" -c '\q' 2>/dev/null; do
    echo "Database is unavailable - sleeping"
    sleep 1
done

echo "Database is ready!"

# Seed test users
echo "Seeding test users..."
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "smartwatts_users_staging" <<EOF
INSERT INTO users (id, username, email, password, first_name, last_name, role, is_active, created_at, updated_at)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 'testuser', 'test@example.com', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test', 'User', 'ROLE_USER', true, NOW(), NOW()),
    ('550e8400-e29b-41d4-a716-446655440001', 'admin', 'admin@smartwatts.ng', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', 'ROLE_ADMIN', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
EOF

# Seed test devices
echo "Seeding test devices..."
PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "smartwatts_devices_staging" <<EOF
INSERT INTO devices (id, name, device_id, device_type, protocol, status, user_id, created_at, updated_at)
VALUES 
    ('660e8400-e29b-41d4-a716-446655440000', 'Test Smart Meter', 'SW_001', 'SMART_METER', 'MQTT', 'ONLINE', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW()),
    ('660e8400-e29b-41d4-a716-446655440001', 'Test Solar Inverter', 'SW_002', 'SOLAR_INVERTER', 'MODBUS_TCP', 'ONLINE', '550e8400-e29b-41d4-a716-446655440000', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
EOF

echo -e "${GREEN}Test data seeding completed!${NC}"

