# RS485 Configuration Guide for SmartWatts Edge Gateway

## Overview

SmartWatts Edge Gateway now supports complete RS485 serial communication for Modbus RTU-based inverters and energy devices. This guide provides comprehensive instructions for configuring and testing RS485 devices.

## Supported Inverter Types

### Fully Tested Inverters
- **SMA Sunny Boy** - Modbus RTU over RS485
- **Fronius Symo** - Modbus RTU over RS485  
- **Growatt SPH** - Modbus RTU over RS485
- **Solis RHI** - Modbus RTU over RS485
- **Deye SUN** - Modbus RTU over RS485

### Generic Modbus RTU Support
- Any Modbus RTU-compliant inverter
- Custom register mapping support
- Configurable communication parameters

## Hardware Requirements

### Raspberry Pi 5 Setup
```bash
# Install required packages
sudo apt update
sudo apt install -y python3-serial minicom

# Add user to dialout group for serial access
sudo usermod -a -G dialout $USER

# Reboot to apply group changes
sudo reboot
```

### USB-to-RS485 Adapter
- **Recommended**: CH340G or FTDI-based USB-to-RS485 converter
- **Connection**: Connect to inverter's RS485 terminals (A+, B-, GND)
- **Termination**: Use 120Ω termination resistor for long cables

## Configuration

### 1. Serial Port Configuration

Edit `application.yml`:

```yaml
rs485:
  default-port: "/dev/ttyUSB0"        # Serial port device
  default-baud-rate: 9600             # Communication speed
  default-data-bits: 8                # Data bits
  default-parity: "NONE"              # Parity (NONE, ODD, EVEN)
  default-stop-bits: 1                # Stop bits
  read-timeout: 1000                  # Read timeout (ms)
  write-timeout: 1000                 # Write timeout (ms)
  polling-interval: 5000              # Polling interval (ms)
  auto-discovery: true                # Auto-discover devices
  enable-logging: true                # Enable debug logging
  max-retry-attempts: 3               # Max retry attempts
  retry-delay: 1000                   # Retry delay (ms)
```

### 2. Device-Specific Configuration

Add your inverter configuration:

```yaml
rs485:
  devices:
    my-sma-inverter:
      port: "/dev/ttyUSB0"
      baud-rate: 9600
      data-bits: 8
      parity: "NONE"
      stop-bits: 1
      read-timeout: 1000
      write-timeout: 1000
      unit-id: 1                      # Modbus unit ID
      start-address: 40000            # First register address
      register-count: 20              # Number of registers to read
      control-address: 0              # Control register address
      device-type: "SOLAR_INVERTER"
      manufacturer: "SMA"
      model: "Sunny Boy 5.0"
      enabled: true
```

### 3. Common Inverter Configurations

#### SMA Sunny Boy Series
```yaml
sma-sunny-boy:
  port: "/dev/ttyUSB0"
  baud-rate: 9600
  unit-id: 1
  start-address: 40000
  register-count: 20
  manufacturer: "SMA"
  model: "Sunny Boy"
```

#### Fronius Symo Series
```yaml
fronius-symo:
  port: "/dev/ttyUSB1"
  baud-rate: 19200
  unit-id: 1
  start-address: 50000
  register-count: 25
  manufacturer: "Fronius"
  model: "Symo"
```

#### Growatt SPH Series
```yaml
growatt-sph:
  port: "/dev/ttyUSB2"
  baud-rate: 9600
  unit-id: 1
  start-address: 1000
  register-count: 10
  manufacturer: "Growatt"
  model: "SPH"
```

## Testing and Validation

### 1. Test Available Serial Ports

```bash
# List available serial ports
curl http://localhost:8088/api/v1/rs485/ports

# Refresh port list
curl -X POST http://localhost:8088/api/v1/rs485/refresh-ports
```

### 2. Test Device Communication

```bash
# Test specific device
curl -X POST http://localhost:8088/api/v1/rs485/devices/my-inverter/test

# Test all known inverters
curl -X POST http://localhost:8088/api/v1/rs485/inverters/test-all
```

### 3. Get Test Results

```bash
# Get test results
curl http://localhost:8088/api/v1/rs485/inverters/test-results

# Generate test report
curl http://localhost:8088/api/v1/rs485/inverters/test-report
```

### 4. Monitor Device Status

```bash
# Get RS485 service status
curl http://localhost:8088/api/v1/rs485/status
```

## API Endpoints

### RS485 Management
- `GET /api/v1/rs485/status` - Get service status
- `GET /api/v1/rs485/ports` - Get available ports
- `POST /api/v1/rs485/refresh-ports` - Refresh port list
- `GET /api/v1/rs485/configuration` - Get configuration

### Device Management
- `POST /api/v1/rs485/devices` - Add device configuration
- `DELETE /api/v1/rs485/devices/{deviceId}` - Remove device
- `POST /api/v1/rs485/devices/{deviceId}/test` - Test device
- `POST /api/v1/rs485/devices/{deviceId}/send-command` - Send command

### Testing
- `POST /api/v1/rs485/inverters/test-all` - Test all inverters
- `GET /api/v1/rs485/inverters/test-results` - Get test results
- `GET /api/v1/rs485/inverters/test-report` - Generate report

## Troubleshooting

### Common Issues

#### 1. Permission Denied
```bash
# Add user to dialout group
sudo usermod -a -G dialout $USER
sudo reboot
```

#### 2. Port Not Found
```bash
# Check available ports
ls /dev/ttyUSB*
ls /dev/ttyACM*

# Check dmesg for USB device detection
dmesg | grep tty
```

#### 3. Communication Timeout
- Check RS485 wiring (A+, B-, GND)
- Verify baud rate and parity settings
- Check for proper termination (120Ω resistor)
- Ensure correct Modbus unit ID

#### 4. No Data Received
- Verify inverter Modbus settings
- Check register addresses
- Test with Modbus client (e.g., QModMaster)
- Enable debug logging

### Debug Logging

Enable detailed logging:

```yaml
logging:
  level:
    com.smartwatts.edge.service.RS485SerialService: DEBUG
    com.smartwatts.edge.protocol.ModbusProtocolHandler: DEBUG
```

### Manual Testing

Test with Modbus client:

```bash
# Install QModMaster (GUI)
sudo apt install qmodmaster

# Or use command line tools
sudo apt install libmodbus-dev
```

## Performance Optimization

### 1. Polling Intervals
- **High-frequency devices**: 1-2 seconds
- **Standard monitoring**: 5-10 seconds
- **Low-priority devices**: 30-60 seconds

### 2. Buffer Sizes
- **Small installations**: 1-2 devices per port
- **Large installations**: Use multiple USB-to-RS485 adapters
- **Industrial setups**: Consider RS485 hubs

### 3. Error Handling
- **Retry attempts**: 3-5 attempts
- **Timeout settings**: 1-3 seconds
- **Connection recovery**: Automatic reconnection

## Security Considerations

### 1. Device Authentication
- Use unique Modbus unit IDs
- Implement device verification
- Monitor for unauthorized access

### 2. Data Validation
- Validate all received data
- Check CRC checksums
- Implement data range validation

### 3. Network Security
- Use VPN for remote access
- Implement proper firewall rules
- Monitor communication logs

## Production Deployment

### 1. Hardware Setup
- Use industrial-grade USB-to-RS485 adapters
- Implement proper grounding
- Use shielded cables for long runs
- Install surge protection

### 2. Software Configuration
- Disable auto-discovery in production
- Use fixed device configurations
- Implement proper logging
- Set up monitoring and alerting

### 3. Maintenance
- Regular communication tests
- Monitor device health
- Update configurations as needed
- Backup device settings

## Support

For technical support or questions about RS485 configuration:

1. Check the troubleshooting section above
2. Review the test results and logs
3. Contact the SmartWatts support team
4. Provide detailed error messages and configuration

## Changelog

### Version 1.0.0 (Current)
- Initial RS485 support implementation
- Modbus RTU communication
- Comprehensive testing framework
- REST API for management
- Support for major inverter brands

### Future Enhancements
- Modbus RTU over TCP (gateway mode)
- Advanced error recovery
- Real-time data streaming
- Enhanced security features




