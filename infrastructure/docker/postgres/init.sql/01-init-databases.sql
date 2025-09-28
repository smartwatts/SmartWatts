-- SmartWatts PostgreSQL Initialization Script
-- This script creates the necessary databases for all microservices

-- Create facility360 database
CREATE DATABASE smartwatts_facility360;

-- Grant permissions to postgres user
GRANT ALL PRIVILEGES ON DATABASE smartwatts_facility360 TO postgres;

-- Connect to facility360 database and create extensions
\c smartwatts_facility360;

-- Create UUID extension for generating unique identifiers
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Disconnect from facility360 database
\c smartwatts;

-- Create UUID extension for main database
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create updated_at trigger function for main database
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';
