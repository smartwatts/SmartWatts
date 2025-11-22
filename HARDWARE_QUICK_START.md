# SmartWatts Hardware Integration - Quick Start Guide

## 🚀 Quick Start (5 Minutes)

### Step 1: Connect Your Hardware

#### For Solar Inverters (RS485):
```bash
# 1. Connect RS485 to USB adapter to your computer
# 2. Connect to inverter's RS485 port (usually labeled "RS485" or "Modbus")
# 3. Check available ports
ls /dev/ttyUSB* /dev/ttyACM*

# 4. Set permissions
sudo chmod 666 /dev/ttyUSB0
```

#### For Network Devices (Modbus TCP):
```bash
# 1. Connect device to your network
# 2. Find device IP address
nmap -p 502 192.168.1.0/24

# 3. Test connection
telnet 192.168.1.100 502
```

### Step 2: Deploy SmartWatts

```bash
# 1. Clone and navigate to project
git clone https://github.com/your-repo/smartwatts.git
cd smartwatts

# 2. Run the deployment script
./scripts/deploy-hardware.sh

# 3. Or use Docker Compose
docker-compose -f docker-compose.hardware.yml up -d
```

### Step 3: Configure Your Hardware

Edit `config/edge-gateway.yml`:

```yaml
rs485:
  devices:
    your-inverter:
      port: "/dev/ttyUSB0"  # Your actual port
      baud-rate: 9600       # Match your device
      unit-id: 1            # Device unit ID
      start-address: 40000  # First register address
      register-count: 20    # Number of registers
      device-type: "SOLAR_INVERTER"
      manufacturer: "YourBrand"
      model: "YourModel"
      enabled: true
```

### Step 4: Test Integration

```bash
# 1. Check service status
docker-compose -f docker-compose.hardware.yml ps

# 2. Check edge gateway logs
docker logs smartwatts-edge-gateway -f

# 3. Test device discovery
curl http://localhost:8088/api/v1/devices/discovered

# 4. Access dashboard
open http://localhost:3000
```

## 🔧 Hardware-Specific Examples

### SMA Sunny Boy Inverter
```yaml
rs485:
  devices:
    sma-sunny-boy:
      port: "/dev/ttyUSB0"
      baud-rate: 9600
      unit-id: 1
      start-address: 40000
      register-count: 20
      device-type: "SOLAR_INVERTER"
      manufacturer: "SMA"
      model: "Sunny Boy 3.0"
      enabled: true
```

### Fronius Symo Inverter
```yaml
rs485:
  devices:
    fronius-symo:
      port: "/dev/ttyUSB1"
      baud-rate: 19200
      unit-id: 1
      start-address: 50000
      register-count: 25
      device-type: "SOLAR_INVERTER"
      manufacturer: "Fronius"
      model: "Symo 10.0-3-M"
      enabled: true
```

### Smart Meter
```yaml
rs485:
  devices:
    smart-meter:
      port: "/dev/ttyUSB2"
      baud-rate: 9600
      unit-id: 1
      start-address: 1000
      register-count: 10
      device-type: "SMART_METER"
      manufacturer: "Landis+Gyr"
      model: "E650"
      enabled: true
```

### Network Device (Modbus TCP)
```yaml
modbus:
  tcp:
    devices:
      network-inverter:
        host: "192.168.1.100"
        port: 502
        unit-id: 1
        start-address: 40000
        register-count: 20
        device-type: "SOLAR_INVERTER"
        manufacturer: "Huawei"
        model: "SUN2000"
        enabled: true
```

## 📱 MQTT Device Integration

### Temperature Sensor
```yaml
mqtt-devices:
  temperature-sensor:
    topic: "sensors/temp/001/data"
    device-type: "TEMPERATURE_SENSOR"
    manufacturer: "DHT22"
    model: "Temperature Sensor"
    enabled: true
```

### Power Meter
```yaml
mqtt-devices:
  power-meter:
    topic: "meters/power/001/data"
    device-type: "POWER_METER"
    manufacturer: "Schneider Electric"
    model: "PM8000"
    enabled: true
```

## 🐛 Troubleshooting

### Serial Port Issues
```bash
# Check available ports
ls /dev/ttyUSB* /dev/ttyACM*

# Set permissions
sudo chmod 666 /dev/ttyUSB0
sudo usermod -aG dialout $USER

# Test communication
minicom -D /dev/ttyUSB0 -b 9600
```

### Network Issues
```bash
# Test network connectivity
ping 192.168.1.100
telnet 192.168.1.100 502

# Check firewall
sudo ufw status
sudo ufw allow 1883  # MQTT
sudo ufw allow 502   # Modbus TCP
```

### Service Issues
```bash
# Check service logs
docker logs smartwatts-edge-gateway -f

# Restart services
docker-compose -f docker-compose.hardware.yml restart

# Check service health
curl http://localhost:8088/actuator/health
```

## 📊 Monitoring

### Device Status
```bash
# Check discovered devices
curl http://localhost:8088/api/v1/devices/discovered

# Check device communication
curl http://localhost:8088/api/v1/devices/test

# Check MQTT status
curl http://localhost:8088/api/v1/mqtt/status
```

### Dashboard Access
- **Main Dashboard**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Edge Gateway**: http://localhost:8088
- **MQTT Broker**: localhost:1883

## 🔄 Production Deployment

### Raspberry Pi 5
```bash
# 1. Flash Raspberry Pi OS
# 2. Enable SSH and serial port
sudo raspi-config

# 3. Clone and deploy
git clone https://github.com/your-repo/smartwatts.git
cd smartwatts
./scripts/deploy-hardware.sh

# 4. Enable auto-start
sudo systemctl enable smartwatts-edge
```

### Industrial Computer
```bash
# 1. Install Ubuntu Server
# 2. Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 3. Deploy SmartWatts
git clone https://github.com/your-repo/smartwatts.git
cd smartwatts
docker-compose -f docker-compose.hardware.yml up -d
```

## 📞 Support

- **Documentation**: Check the full Hardware Integration Guide
- **Issues**: GitHub Issues
- **Community**: SmartWatts Discord
- **Email**: support@mysmartwatts.com

## 🎯 Next Steps

1. **Connect your hardware** using the examples above
2. **Configure the edge gateway** with your device settings
3. **Test the integration** using the provided test methods
4. **Monitor the dashboard** to see your data
5. **Scale up** by adding more devices

The SmartWatts platform will automatically discover and communicate with your hardware once properly configured!
