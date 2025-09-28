# Azure IoT Hub Integration for SmartWatts

## Overview
This document outlines the integration of Azure IoT Hub with SmartWatts for device communication, replacing the local MQTT broker.

## Architecture Changes

### Before (Local MQTT)
```
IoT Devices → MQTT Broker (Mosquitto) → Edge Gateway → Microservices
```

### After (Azure IoT Hub)
```
IoT Devices → Azure IoT Hub → Edge Gateway → Microservices
```

## Configuration

### 1. Azure IoT Hub Setup
- **Tier**: Free (F1)
- **Messages**: 8,000/day
- **Devices**: 2 free
- **Partitions**: 2

### 2. Device Registration
```bash
# Register a device in Azure IoT Hub
az iot hub device-identity create \
  --hub-name smartwatts-iot-hub \
  --device-id smartwatts-gateway \
  --edge-enabled false
```

### 3. Connection Strings
```bash
# Get device connection string
az iot hub device-identity connection-string show \
  --hub-name smartwatts-iot-hub \
  --device-id smartwatts-gateway
```

## Code Changes

### 1. Edge Gateway Service
Replace MQTT client with Azure IoT Hub client:

```java
// Add to build.gradle
implementation 'com.microsoft.azure.sdk.iot:iot-device-client:1.34.0'
implementation 'com.microsoft.azure.sdk.iot:iot-service-client:1.34.0'

// Replace MQTT handler with Azure IoT Hub handler
@Service
public class AzureIoTHubHandler {
    
    @Value("${azure.iot.hub.connection-string}")
    private String connectionString;
    
    private DeviceClient deviceClient;
    
    @PostConstruct
    public void initialize() {
        try {
            deviceClient = new DeviceClient(connectionString, IotHubClientProtocol.MQTT);
            deviceClient.open();
        } catch (Exception e) {
            log.error("Failed to initialize Azure IoT Hub client", e);
        }
    }
    
    public void sendTelemetry(String deviceId, Map<String, Object> data) {
        try {
            String messageJson = objectMapper.writeValueAsString(data);
            Message message = new Message(messageJson);
            message.setProperty("deviceId", deviceId);
            message.setProperty("timestamp", Instant.now().toString());
            
            deviceClient.sendEventAsync(message, new EventCallback(), null);
        } catch (Exception e) {
            log.error("Failed to send telemetry", e);
        }
    }
}
```

### 2. Device Service
Update device registration to use Azure IoT Hub:

```java
@Service
public class AzureDeviceRegistrationService {
    
    @Value("${azure.iot.hub.connection-string}")
    private String connectionString;
    
    public void registerDevice(String deviceId) {
        try {
            RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
            
            Device device = Device.createFromId(deviceId, null, null);
            Device registeredDevice = registryManager.addDevice(device);
            
            log.info("Device registered: {}", registeredDevice.getDeviceId());
        } catch (Exception e) {
            log.error("Failed to register device", e);
        }
    }
}
```

### 3. Configuration Updates
Update `application-azure.yml`:

```yaml
azure:
  iot:
    hub:
      connection-string: ${AZURE_IOT_HUB_CONNECTION_STRING}
      device-id: ${AZURE_DEVICE_ID:smartwatts-gateway}
    
spring:
  cloud:
    azure:
      iot:
        hub:
          connection-string: ${AZURE_IOT_HUB_CONNECTION_STRING}
```

## Message Format

### Telemetry Messages
```json
{
  "deviceId": "smartwatts-gateway",
  "timestamp": "2025-01-06T10:30:00Z",
  "data": {
    "voltage": 220.5,
    "current": 15.2,
    "power": 3348.6,
    "energy": 125.4,
    "frequency": 50.0,
    "powerFactor": 0.95
  }
}
```

### Device Twin Updates
```json
{
  "deviceId": "smartwatts-gateway",
  "properties": {
    "reported": {
      "status": "online",
      "lastSeen": "2025-01-06T10:30:00Z",
      "firmwareVersion": "1.0.0"
    }
  }
}
```

## Monitoring and Analytics

### 1. Azure IoT Hub Metrics
- Message count
- Device count
- Error count
- Throttle count

### 2. Custom Metrics
- Device online/offline status
- Message processing time
- Error rates
- Data quality metrics

## Security

### 1. Device Authentication
- X.509 certificates
- SAS tokens
- Device connection strings

### 2. Message Security
- TLS 1.2 encryption
- Message signing
- Device identity verification

## Cost Optimization

### 1. Message Batching
```java
// Batch messages to reduce cost
List<Message> messageBatch = new ArrayList<>();
// ... add messages to batch
deviceClient.sendEventAsync(messageBatch, new EventCallback(), null);
```

### 2. Message Compression
```java
// Compress large messages
message.setProperty("content-encoding", "gzip");
```

### 3. Message Filtering
```java
// Only send important messages
if (isImportantMessage(data)) {
    sendTelemetry(deviceId, data);
}
```

## Troubleshooting

### Common Issues
1. **Connection Timeout**: Check network connectivity
2. **Authentication Failed**: Verify connection string
3. **Message Throttling**: Implement backoff strategy
4. **Device Not Found**: Register device first

### Debug Commands
```bash
# Check device status
az iot hub device-identity show \
  --hub-name smartwatts-iot-hub \
  --device-id smartwatts-gateway

# Monitor messages
az iot hub monitor-events \
  --hub-name smartwatts-iot-hub \
  --device-id smartwatts-gateway

# Check connection string
az iot hub device-identity connection-string show \
  --hub-name smartwatts-iot-hub \
  --device-id smartwatts-gateway
```

## Migration Plan

### Phase 1: Setup
1. Create Azure IoT Hub
2. Register devices
3. Update configuration

### Phase 2: Code Changes
1. Replace MQTT client with Azure IoT Hub client
2. Update message handling
3. Implement device management

### Phase 3: Testing
1. Test device connectivity
2. Verify message flow
3. Test error handling

### Phase 4: Deployment
1. Deploy to Azure VM
2. Monitor performance
3. Optimize costs

## Benefits

### 1. Scalability
- Handle more devices
- Better message routing
- Global availability

### 2. Reliability
- 99.9% uptime SLA
- Message durability
- Automatic failover

### 3. Security
- Enterprise-grade security
- Device authentication
- Message encryption

### 4. Monitoring
- Built-in metrics
- Custom dashboards
- Alerting

## Cost Analysis

### Free Tier Usage
- **Messages**: 8,000/day (6 sites × 1 msg/65s = ~8,000/day) ✅
- **Devices**: 2 free (1 gateway + 1 backup) ✅
- **Storage**: 0.5 GB free ✅

### Potential Overages
- **Messages**: $0.10/1M messages if over 8,000/day
- **Devices**: $0.50/device/month if over 2 devices
- **Storage**: $0.10/GB/month if over 0.5 GB

**Total Estimated Cost**: $0/month (within free tier limits)

## Next Steps

1. **Implement Azure IoT Hub integration**
2. **Update device communication code**
3. **Test with real devices**
4. **Monitor and optimize**
5. **Scale as needed**

SmartWatts will have enterprise-grade IoT connectivity with zero additional cost! 🚀
