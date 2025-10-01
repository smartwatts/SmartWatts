# SmartWatts Edge Gateway

Complete edge gateway solution for energy monitoring and AI inference on R501 RK3588 Edge AI Box.

## Features

- **MQTT Broker**: Local MQTT communication for IoT devices
- **Modbus Integration**: RTU/TCP support for inverters, meters, and energy devices
- **Local Storage**: SQLite database with offline-first capabilities
- **Device Discovery**: Automatic detection of MQTT, Modbus, HTTP, and CoAP devices
- **AI Inference**: TensorFlow Lite models for energy forecasting and anomaly detection
- **Cloud Sync**: Offline-first data synchronization with cloud services
- **REST API**: Complete REST API for device management and data access
- **Monitoring**: Prometheus metrics and system monitoring
- **Docker Support**: Containerized deployment with Docker Compose

## Hardware Requirements

- **R501 RK3588 Edge AI Box** (8-core ARM64, 8GB RAM)
- **Storage**: 32GB+ microSD card or eMMC
- **Network**: Ethernet or WiFi connectivity
- **Serial Ports**: RS485/USB for Modbus RTU devices
- **GPIO**: For additional sensor connections

## Quick Start

### Option 1: Direct Installation

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd edge-gateway
   ```

2. **Run installation script**:
   ```bash
   sudo chmod +x deploy/install.sh
   sudo ./deploy/install.sh
   ```

3. **Check service status**:
   ```bash
   sudo systemctl status smartwatts-edge
   ```

### Option 2: Docker Deployment

1. **Install Docker and Docker Compose**:
   ```bash
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker $USER
   sudo apt install docker-compose-plugin
   ```

2. **Deploy with Docker Compose**:
   ```bash
   cd deploy
   docker-compose up -d
   ```

3. **Check container status**:
   ```bash
   docker-compose ps
   ```

## Configuration

### Main Configuration

Edit `/opt/smartwatts/config/edge-config.yml`:

```yaml
# Network Configuration
network:
  host: "0.0.0.0"
  port: 8080
  cloud_api_url: "https://api.smartwatts.com"

# MQTT Configuration
mqtt:
  broker_host: "localhost"
  broker_port: 1883

# Modbus Configuration
modbus:
  enabled: true
  devices:
    - name: "Solar Inverter"
      type: "inverter"
      protocol: "tcp"
      host: "192.168.1.101"
      port: 502
```

### Device Configuration

Add your devices to the Modbus configuration:

```yaml
modbus:
  devices:
    - name: "Solar Inverter"
      type: "inverter"
      address: 1
      protocol: "tcp"
      host: "192.168.1.101"
      port: 502
    - name: "Energy Meter"
      type: "meter"
      address: 2
      protocol: "rtu"
      port: "/dev/ttyUSB0"
      baudrate: 9600
```

## API Usage

### Health Check

```bash
curl http://localhost:8080/api/v1/health
```

### Store Energy Reading

```bash
curl -X POST http://localhost:8080/api/v1/energy/readings \
  -H "Content-Type: application/json" \
  -d '{
    "device_id": "solar_inverter_01",
    "device_type": "inverter",
    "power": 2500.5,
    "voltage": 240.2,
    "current": 10.4
  }'
```

### Get Energy Readings

```bash
curl "http://localhost:8080/api/v1/energy/readings?device_id=solar_inverter_01&limit=100"
```

### MQTT Publishing

```bash
curl -X POST http://localhost:8080/api/v1/mqtt/publish \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "smartwatts/energy/device01/data",
    "message": {"power": 2500, "voltage": 240}
  }'
```

### AI Inference

```bash
curl -X POST http://localhost:8080/api/v1/ai/inference \
  -H "Content-Type: application/json" \
  -d '{
    "model_name": "energy_forecast",
    "prediction_type": "energy_forecast",
    "input_data": {
      "power_history": [1000, 1200, 1500, 1800],
      "voltage": 240,
      "current": 5
    }
  }'
```

## MQTT Topics

### Energy Data
- `smartwatts/energy/{device_id}/data` - Energy readings
- `smartwatts/energy/{device_id}/status` - Device status

### Device Management
- `smartwatts/devices/{device_id}/status` - Device status updates
- `smartwatts/discovery/{device_id}/announce` - Device discovery

### Alerts
- `smartwatts/alerts/{device_id}/{alert_type}` - Alert notifications

### Commands
- `smartwatts/commands/{device_id}/{command}` - Device commands

## Monitoring

### Prometheus Metrics

Access metrics at `http://localhost:9090/metrics`

Key metrics:
- `smartwatts_energy_readings_total` - Total energy readings
- `smartwatts_devices_total` - Device count by status
- `smartwatts_mqtt_messages_total` - MQTT message count
- `smartwatts_ai_inferences_total` - AI inference count

### Grafana Dashboard

Access Grafana at `http://localhost:3000` (admin/admin)

### System Monitoring

```bash
# Check service status
sudo systemctl status smartwatts-edge

# View logs
sudo journalctl -u smartwatts-edge -f

# Run monitoring script
/opt/smartwatts/monitor.sh
```

## Device Integration

### Modbus RTU Devices

1. Connect device to USB/RS485 adapter
2. Configure in `edge-config.yml`:
   ```yaml
   modbus:
     devices:
       - name: "Energy Meter"
         type: "meter"
         protocol: "rtu"
         port: "/dev/ttyUSB0"
         baudrate: 9600
   ```

### Modbus TCP Devices

1. Ensure device is on network
2. Configure in `edge-config.yml`:
   ```yaml
   modbus:
     devices:
       - name: "Solar Inverter"
         type: "inverter"
         protocol: "tcp"
         host: "192.168.1.101"
         port: 502
   ```

### MQTT Devices

1. Configure device to publish to MQTT broker
2. Device will be automatically discovered
3. Data will be stored and processed

## AI Models

### Supported Models

- **Energy Forecast**: 24-hour energy consumption prediction
- **Anomaly Detection**: Detect unusual energy patterns
- **Load Prediction**: Predict load consumption patterns
- **Efficiency Optimization**: Optimize energy efficiency

### Model Management

```bash
# List loaded models
curl http://localhost:8080/api/v1/ai/models

# Run inference
curl -X POST http://localhost:8080/api/v1/ai/inference \
  -H "Content-Type: application/json" \
  -d '{
    "model_name": "anomaly_detection",
    "prediction_type": "anomaly_detection",
    "input_data": {
      "power": 2500,
      "voltage": 240,
      "current": 10.4
    }
  }'
```

## Data Synchronization

### Cloud Sync

Configure cloud API in `edge-config.yml`:

```yaml
data_sync:
  enabled: true
  sync_interval_seconds: 300
  batch_size: 1000
  offline_mode: true
  conflict_resolution: "edge_priority"
```

### Manual Sync

```bash
# Trigger immediate sync
curl -X POST http://localhost:8080/api/v1/sync/trigger

# Check sync status
curl http://localhost:8080/api/v1/sync/status
```

## Maintenance

### Backup

```bash
# Manual backup
/opt/smartwatts/backup.sh

# Automatic backups (daily at 2 AM)
crontab -l -u smartwatts
```

### Update

```bash
# Update edge gateway
/opt/smartwatts/update.sh
```

### Cleanup

```bash
# Clean old data (30+ days)
curl -X POST http://localhost:8080/api/v1/maintenance/cleanup?days=30
```

## Troubleshooting

### Service Issues

```bash
# Check service status
sudo systemctl status smartwatts-edge

# View logs
sudo journalctl -u smartwatts-edge -f

# Restart service
sudo systemctl restart smartwatts-edge
```

### MQTT Issues

```bash
# Check MQTT broker
sudo systemctl status mosquitto

# Test MQTT connection
mosquitto_pub -h localhost -t test -m "Hello World"
mosquitto_sub -h localhost -t test
```

### Modbus Issues

```bash
# Check serial ports
ls -la /dev/ttyUSB*

# Test Modbus connection
python3 -c "
from pymodbus.client import ModbusSerialClient
client = ModbusSerialClient(method='rtu', port='/dev/ttyUSB0', baudrate=9600)
print('Connected:', client.connect())
"
```

### Database Issues

```bash
# Check database
sqlite3 /opt/smartwatts/data/edge.db ".tables"

# Repair database
sqlite3 /opt/smartwatts/data/edge.db ".recover" | sqlite3 /opt/smartwatts/data/edge_recovered.db
```

## Development

### Local Development

```bash
# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run in development mode
python main.py
```

### Testing

```bash
# Run tests
pytest tests/

# Run specific test
pytest tests/test_mqtt_service.py -v
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the logs for error messages

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request
