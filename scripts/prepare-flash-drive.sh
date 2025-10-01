#!/bin/bash

# SmartWatts Flash Drive Preparation Script
# This script prepares a flash drive for portable SmartWatts installation

set -e

echo "💾 SmartWatts Flash Drive Preparation"
echo "===================================="

# Check if we're in the SmartWatts directory
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Please run this script from the SmartWatts root directory"
    exit 1
fi

# Get flash drive path
echo "Enter the path to your flash drive (e.g., /media/usb or /Volumes/USB):"
read -p "Flash drive path: " FLASH_PATH

if [ ! -d "$FLASH_PATH" ]; then
    echo "❌ Flash drive path does not exist: $FLASH_PATH"
    exit 1
fi

# Create SmartWatts directory on flash drive
SMARTWATTS_DIR="$FLASH_PATH/SmartWatts-Portable"
echo "Creating SmartWatts directory on flash drive..."
mkdir -p "$SMARTWATTS_DIR"

# Copy all SmartWatts files
echo "Copying SmartWatts files to flash drive..."
cp -r . "$SMARTWATTS_DIR/"

# Remove unnecessary files
echo "Cleaning up flash drive..."
cd "$SMARTWATTS_DIR"
rm -rf .git
rm -rf node_modules
rm -rf backend/*/build
rm -rf backend/*/bin
rm -rf frontend/.next
rm -rf frontend/node_modules
rm -rf data
rm -f *.log

# Create data directory structure
mkdir -p data/{postgres,redis,edge-gateway,backups}

# Make scripts executable
chmod +x scripts/*.sh

# Create flash drive README
cat > README.md << 'EOF'
# SmartWatts Portable Installation

## Quick Start

### For Windows:
1. Double-click `scripts\portable-install.bat`
2. Wait for installation to complete
3. Open http://localhost:3000 in your browser

### For Linux/Mac:
1. Open terminal in this directory
2. Run: `chmod +x scripts/portable-install.sh && ./scripts/portable-install.sh`
3. Open http://localhost:3000 in your browser

### For Raspberry Pi:
1. Open terminal in this directory
2. Run: `chmod +x scripts/portable-install-raspberry-pi.sh && ./scripts/portable-install-raspberry-pi.sh`
3. Open http://[PI_IP_ADDRESS]:3000 in your browser

## Requirements

- Docker and Docker Compose installed
- At least 10GB free disk space
- Internet connection (for initial setup)

## What's Included

- Complete SmartWatts platform
- All backend services (13 microservices)
- Frontend dashboard
- Edge gateway for hardware integration
- Database and Redis
- MQTT broker
- Installation scripts for all platforms

## Management Scripts

- **Start**: `scripts/start-smartwatts.sh` (or `.bat` on Windows)
- **Stop**: `scripts/stop-smartwatts.sh` (or `.bat` on Windows)
- **Health Check**: `scripts/health-check.sh` (or `.bat` on Windows)
- **Backup**: `scripts/backup-data.sh`

## Access Points

- **Dashboard**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Edge Gateway**: http://localhost:8088

## Hardware Integration

See `docs/Hardware_Integration_Guide.md` for connecting real hardware.

## Support

Check `docs/` folder for detailed guides and troubleshooting.

---

**SmartWatts Platform v1.0.0**  
**Portable Installation Package**
EOF

# Calculate size
SIZE=$(du -sh "$SMARTWATTS_DIR" | cut -f1)

echo ""
echo "✅ Flash drive preparation complete!"
echo "=================================="
echo "SmartWatts portable installation created at:"
echo "  $SMARTWATTS_DIR"
echo ""
echo "Total size: $SIZE"
echo ""
echo "The flash drive is now ready for portable installation!"
echo "Users can simply:"
echo "  1. Insert the flash drive"
echo "  2. Navigate to the SmartWatts-Portable folder"
echo "  3. Run the appropriate installation script"
echo ""
echo "For detailed instructions, see:"
echo "  docs/SmartWatts_Portable_Installation_Guide.md"




