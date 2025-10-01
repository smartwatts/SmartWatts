SmartWatts Portable Installation
================================

This is a portable installation of SmartWatts that can run from any computer with Docker installed.

Requirements:
- Docker Desktop (Windows/Mac) or Docker Engine (Linux)
- Java 17 or later
- 4GB RAM minimum
- 2GB free disk space

Quick Start:
1. Make sure Docker is running
2. Run start-smartwatts.sh (Linux/Mac) or start-smartwatts.bat (Windows)
3. Open http://localhost:3000 in your browser

Hardware Integration:
1. Connect your hardware (inverters, meters, etc.)
2. Edit config/edge-gateway.yml with your device settings
3. Restart the edge gateway

Configuration:
- Main config: config/edge-gateway.yml
- MQTT config: config/mqtt/mosquitto.conf
- Data storage: data/
- Logs: logs/

Troubleshooting:
- Check logs: ./view-logs.sh (Linux/Mac) or view-logs.bat (Windows)
- Restart services: ./stop-smartwatts.sh && ./start-smartwatts.sh
- Check Docker: docker ps

Support:
- Documentation: docs/
- GitHub: https://github.com/your-repo/smartwatts
- Email: support@smartwatts.ng

Version: 1.0.0
Last Updated: January 2025




