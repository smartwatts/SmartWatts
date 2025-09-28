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


