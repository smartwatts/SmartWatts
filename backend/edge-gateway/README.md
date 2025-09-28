# SmartWatts Edge Gateway

The Edge Gateway service provides local intelligence and device management capabilities for the SmartWatts energy monitoring platform.

## Features

- **Local ML Inference**: Energy forecasting, anomaly detection, and optimization recommendations
- **IoT Device Management**: Device discovery, monitoring, and command execution
- **Offline-First Architecture**: Core functionality works without internet connectivity
- **Real-time Processing**: MQTT-based communication with IoT devices
- **Edge Analytics**: Local data processing and insights generation

## Architecture

The Edge Gateway follows a hybrid approach:
- **Spring Boot 3.x** for the core service framework
- **Eureka Client** for service discovery integration
- **REST APIs** for external communication
- **Scheduled Tasks** for periodic operations
- **Local Storage** for offline data persistence

## Services

### EdgeMLService
- Energy consumption forecasting
- Anomaly detection in power usage
- Optimization recommendations
- Model synchronization with cloud

### EdgeDeviceService
- IoT device discovery and registration
- Device health monitoring
- Command execution and control
- Protocol handling (MQTT, Modbus, etc.)

## Configuration

- **Port**: 8088
- **Service Name**: edge-gateway
- **Discovery**: Eureka client enabled
- **Health Checks**: Actuator endpoints exposed

## Building

```bash
./gradlew clean build
```

## Running

```bash
./gradlew bootRun
```

## Docker

```bash
docker build -t smartwatts-edge-gateway .
docker run -p 8088:8088 smartwatts-edge-gateway
```

## API Endpoints

- `GET /api/edge/health` - Health check
- `GET /api/edge/stats` - Gateway statistics
- `POST /api/edge/ml/forecast` - Energy forecasting
- `GET /api/edge/devices` - Device status
- `POST /api/edge/devices/command` - Send device command

## Integration

The Edge Gateway integrates with:
- **Service Discovery**: Eureka server for service registration
- **IoT Devices**: MQTT, Modbus, and REST protocols
- **Cloud Services**: For model updates and data synchronization
- **Local Dashboard**: Web interface for local management
