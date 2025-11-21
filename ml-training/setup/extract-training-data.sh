#!/bin/bash
# Extract training data from PostgreSQL database
# This script exports energy consumption data for ML training

set -e

# Configuration
DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-5433}"
DB_NAME="${DB_NAME:-smartwatts_energy}"
DB_USER="${DB_USER:-postgres}"
OUTPUT_DIR="${OUTPUT_DIR:-../data/raw}"
DAYS_BACK="${DAYS_BACK:-365}"

echo "=========================================="
echo "Extracting Training Data from Database"
echo "=========================================="
echo "Database: $DB_NAME@$DB_HOST:$DB_PORT"
echo "Output: $OUTPUT_DIR"
echo "Days: $DAYS_BACK"
echo ""

# Check if psql is installed
if ! command -v psql &> /dev/null; then
    echo "Error: psql not found. Please install PostgreSQL client."
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Prompt for password if not set
if [ -z "$DB_PASSWORD" ]; then
    echo "Enter database password:"
    read -s DB_PASSWORD
    export PGPASSWORD="$DB_PASSWORD"
fi

# Export energy readings
echo "Exporting energy readings..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
COPY (
    SELECT 
        er.timestamp,
        er.device_id::text as device_id,
        er.consumption_kwh as consumption,
        er.cost_naira as cost,
        er.voltage,
        er.current_amps as current,
        er.power_factor
    FROM energy_readings er
    WHERE er.timestamp >= NOW() - INTERVAL '$DAYS_BACK days'
    ORDER BY er.timestamp ASC
) TO STDOUT WITH CSV HEADER
" > "$OUTPUT_DIR/energy_readings_$(date +%Y%m%d).csv"

if [ $? -eq 0 ]; then
    ROW_COUNT=$(wc -l < "$OUTPUT_DIR/energy_readings_$(date +%Y%m%d).csv" | tr -d ' ')
    echo "✓ Exported $ROW_COUNT rows to energy_readings_$(date +%Y%m%d).csv"
else
    echo "✗ Export failed"
    exit 1
fi

# Export device data (if available)
echo "Exporting device information..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
COPY (
    SELECT 
        d.id::text as device_id,
        d.name,
        d.device_type,
        d.location,
        d.manufacturer,
        d.model
    FROM devices d
) TO STDOUT WITH CSV HEADER
" > "$OUTPUT_DIR/devices_$(date +%Y%m%d).csv" 2>/dev/null || echo "⚠ Device table not found (optional)"

# Create summary
echo ""
echo "=========================================="
echo "Data Extraction Complete"
echo "=========================================="
echo "Files created:"
ls -lh "$OUTPUT_DIR"/*.csv 2>/dev/null | awk '{print "  " $9 " (" $5 ")"}'
echo ""
echo "Next steps:"
echo "1. Review data quality"
echo "2. Run training: python scripts/train_all_models.py --data-dir $OUTPUT_DIR"
echo ""

