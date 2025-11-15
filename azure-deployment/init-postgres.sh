#!/bin/bash
set -e

# This script creates multiple databases in PostgreSQL
# It's used by the docker-compose setup to initialize the database

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create databases for each microservice
    CREATE DATABASE smartwatts_users;
    CREATE DATABASE smartwatts_energy;
    CREATE DATABASE smartwatts_devices;
    CREATE DATABASE smartwatts_analytics;
    CREATE DATABASE smartwatts_billing;
    CREATE DATABASE smartwatts_facility360;
    CREATE DATABASE smartwatts_feature_flags;
    CREATE DATABASE smartwatts_device_verification;
    CREATE DATABASE smartwatts_appliance_monitoring;
    
    -- Grant all privileges to the postgres user on all databases
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_users TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_energy TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_devices TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_analytics TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_billing TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_facility360 TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_feature_flags TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_device_verification TO postgres;
    GRANT ALL PRIVILEGES ON DATABASE smartwatts_appliance_monitoring TO postgres;
EOSQL

echo "✅ All databases created successfully"

