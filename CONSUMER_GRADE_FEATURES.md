# SmartWatts Consumer-Grade Dashboard Features

This document outlines the implementation of best-in-class consumer-grade features inspired by Sense, Emporia Vue 3, and SolarEdge, integrated into the SmartWatts Energy Management Platform.

## 🚀 Features Implemented

### 1. AI Appliance Recognition

**Overview**: Machine learning module using NILM (Non-Intrusive Load Monitoring) techniques to detect and label common appliances from real-time current/voltage signatures.

**Components**:
- `ApplianceRecognitionService` - Core NILM analysis engine
- `ApplianceSignature` - Model for storing appliance signatures
- `ApplianceDetection` - Model for tracking detected appliances
- `ApplianceRecognitionController` - REST API endpoints

**Key Features**:
- ✅ Real-time appliance detection from energy signatures
- ✅ Training pipeline with user feedback ("This is my washing machine")
- ✅ Device-level usage, alerts, and timelines
- ✅ Confidence scoring and accuracy tracking
- ✅ Support for 15+ appliance types (fridge, AC, dryer, EV charger, etc.)

**API Endpoints**:
```
POST /api/v1/appliance-recognition/devices/{deviceId}/detect
POST /api/v1/appliance-recognition/devices/{deviceId}/train
GET  /api/v1/appliance-recognition/devices/{deviceId}/usage
PUT  /api/v1/appliance-recognition/detections/{detectionId}/confirm
GET  /api/v1/appliance-recognition/devices/{deviceId}/detections
```

**Frontend Component**: `ApplianceRecognitionWidget.tsx`

### 2. Circuit-Level Nesting & Sub-Panel Support (Emporia-like)

**Overview**: Support for multiple meters/CTs in one property with hierarchical circuit management.

**Components**:
- `CircuitManagementService` - Circuit hierarchy management
- `Circuit` - Model for individual circuits
- `SubPanel` - Model for sub-panels
- `CircuitManagementController` - REST API endpoints

**Key Features**:
- ✅ Hierarchical circuit structure (Device → Sub-Panel → Circuit)
- ✅ Real-time load monitoring and status tracking
- ✅ Overload detection and alerts
- ✅ Support for Modbus-RTU/TCP meters and CT sensors
- ✅ Expandable tree view for circuits

**API Endpoints**:
```
POST /api/v1/circuits
POST /api/v1/circuits/sub-panels
GET  /api/v1/circuits/devices/{deviceId}/hierarchy
GET  /api/v1/circuits/devices/{deviceId}/tree
GET  /api/v1/circuits/{circuitId}/load
GET  /api/v1/circuits/devices/{deviceId}/status
PUT  /api/v1/circuits/{circuitId}/readings
```

**Frontend Component**: `CircuitTreeView.tsx`

### 3. Solar Panel-Level Monitoring

**Overview**: Per-panel solar monitoring via inverter API with visual array mapping and fault detection.

**Components**:
- `SolarPanelMonitoringService` - Solar monitoring and analytics
- `SolarPanel` - Model for individual panels
- `SolarString` - Model for panel strings
- `SolarInverter` - Model for inverters
- `SolarPanelMonitoringController` - REST API endpoints

**Key Features**:
- ✅ Per-panel and per-string monitoring
- ✅ Support for Deye, Solis, Growatt inverters
- ✅ Visual solar array heatmap (green/yellow/red status)
- ✅ Real-time vs historical production comparison
- ✅ Fault detection (power drop, temperature alerts)
- ✅ Performance analytics and efficiency tracking

**API Endpoints**:
```
POST /api/v1/solar/inverters/{inverterId}/update
GET  /api/v1/solar/inverters/{inverterId}/heatmap
GET  /api/v1/solar/inverters/{inverterId}/comparison
GET  /api/v1/solar/inverters/{inverterId}/faults
GET  /api/v1/solar/inverters/{inverterId}/analytics
```

**Frontend Component**: `SolarArrayHeatmap.tsx`

### 4. Community & Benchmarking

**Overview**: Optional anonymized data sharing with regional efficiency comparisons and leaderboards.

**Components**:
- `CommunityBenchmarkingService` - Community analytics engine
- `CommunityBenchmark` - Model for regional benchmarks
- `CommunityBenchmarkingController` - REST API endpoints

**Key Features**:
- ✅ Regional efficiency rankings and percentiles
- ✅ Community leaderboards (top 10, 20% performers)
- ✅ Solar utilization comparisons
- ✅ Energy savings potential analysis
- ✅ Anonymized data sharing with privacy protection
- ✅ Personalized recommendations based on regional data

**API Endpoints**:
```
GET  /api/v1/community/benchmark/{region}/user/{userId}
GET  /api/v1/community/leaderboard/{region}
GET  /api/v1/community/solar-comparison/{region}/user/{userId}
GET  /api/v1/community/savings-comparison/{region}/user/{userId}
POST /api/v1/community/benchmark/{region}/{metricType}
```

**Frontend Component**: `CommunityLeaderboardWidget.tsx`

### 5. Enhanced Dashboard UI

**Overview**: Modern, responsive dashboard with new widgets and Pro Mode for advanced users.

**Components**:
- `EnhancedDashboard` - Main dashboard page
- Multiple specialized widgets
- Pro Mode toggle for advanced features
- Responsive design for mobile and desktop

**Key Features**:
- ✅ Appliance-level bubble chart (energy share by device)
- ✅ Circuit tree view with expandable nodes
- ✅ Solar array heatmap (panel-level visualization)
- ✅ Community leaderboard widget
- ✅ Pro Mode toggle for raw data views
- ✅ Mobile-friendly responsive design
- ✅ Real-time data updates

**Frontend Components**:
- `ApplianceRecognitionWidget.tsx`
- `CircuitTreeView.tsx`
- `SolarArrayHeatmap.tsx`
- `CommunityLeaderboardWidget.tsx`
- `EnhancedDashboard.tsx`

## 🏗️ Architecture

### Backend Services

**Analytics Service**:
- Appliance Recognition
- Solar Panel Monitoring
- Community Benchmarking

**Device Service**:
- Circuit Management
- Sub-Panel Support
- Device Hierarchy

### Database Schema

**New Tables**:
- `appliance_signatures` - ML training data
- `appliance_detections` - Real-time detections
- `circuits` - Circuit information
- `sub_panels` - Sub-panel data
- `solar_panels` - Panel-level data
- `solar_strings` - String-level data
- `solar_inverters` - Inverter configuration
- `community_benchmarks` - Regional statistics

### API Design

**RESTful APIs** with:
- Consistent error handling
- Comprehensive logging
- Input validation
- Rate limiting
- Authentication/authorization

## 🔧 Configuration

### Feature Flags

Enable/disable features via configuration:
```yaml
features:
  ai-appliance-recognition: true
  circuit-level-monitoring: true
  solar-panel-monitoring: true
  community-benchmarking: true
```

### Inverter Configuration

Configure supported inverters:
```yaml
solar:
  inverters:
    deye:
      api-base-url: "https://api.deye.com"
      supported-features: ["panel-level", "string-level"]
    solis:
      api-base-url: "https://api.solis.com"
      supported-features: ["string-level"]
    growatt:
      api-base-url: "https://api.growatt.com"
      supported-features: ["panel-level"]
```

## 📊 Performance Metrics

### Target Performance
- **API Response Time**: < 200ms for standard operations
- **Real-time Updates**: < 1 second for live data
- **Dashboard Load Time**: < 3 seconds initial load
- **ML Inference**: < 100ms for appliance detection

### Scalability
- **Concurrent Users**: 1000+ per gateway
- **Data Retention**: 7 years of energy data
- **Uptime**: 99.5% availability target

## 🔒 Security & Privacy

### Data Protection
- **Anonymization**: All community data is anonymized
- **Encryption**: AES-256 for data at rest, TLS 1.3 for transit
- **Access Control**: Role-based permissions
- **Audit Logging**: Complete audit trail

### Privacy Compliance
- **NDPR Compliance**: Nigeria Data Protection Regulation
- **User Consent**: Opt-in for community features
- **Data Minimization**: Only collect necessary data
- **Right to Deletion**: User data deletion support

## 🚀 Deployment

### Docker Support
All services are containerized with Docker Compose:
```bash
docker-compose up -d
```

### Environment Variables
```bash
# Database
DATABASE_URL=postgresql://localhost:5432/smartwatts

# Redis
REDIS_URL=redis://localhost:6379

# External APIs
OPENWEATHER_API_KEY=your_api_key
SOLAR_INVERTER_API_KEY=your_api_key

# Feature Flags
FEATURE_AI_APPLIANCE_RECOGNITION=true
FEATURE_CIRCUIT_LEVEL_MONITORING=true
FEATURE_SOLAR_PANEL_MONITORING=true
FEATURE_COMMUNITY_BENCHMARKING=true
```

## 📈 Future Enhancements

### Phase 2: Advanced Weather Integration
- Weather-based energy predictions
- Seasonal efficiency adjustments
- Storm impact monitoring

### Phase 3: Smart Home Integration
- IoT device integration
- Automated energy management
- Voice assistant integration

### Phase 4: Energy Trading & Grid Services
- Peer-to-peer energy trading
- Grid services participation
- Demand response programs

## 🧪 Testing

### Test Coverage
- **Unit Tests**: 80%+ coverage target
- **Integration Tests**: API endpoint testing
- **Performance Tests**: Load testing with JMeter
- **Security Tests**: Automated vulnerability scanning

### Test Commands
```bash
# Run all tests
./gradlew test

# Run specific service tests
./gradlew :analytics-service:test
./gradlew :device-service:test

# Run integration tests
./gradlew integrationTest
```

## 📚 Documentation

### API Documentation
- **OpenAPI/Swagger**: Available at `/swagger-ui.html`
- **Postman Collection**: Available in `/docs/postman/`
- **API Reference**: Available in `/docs/api/`

### User Guides
- **Setup Guide**: `/docs/setup/`
- **User Manual**: `/docs/user/`
- **Troubleshooting**: `/docs/troubleshooting/`

## 🤝 Contributing

### Development Setup
1. Clone the repository
2. Install dependencies: `./gradlew build`
3. Start services: `docker-compose up -d`
4. Run tests: `./gradlew test`

### Code Standards
- **Java**: Google Java Format
- **TypeScript**: Prettier + ESLint
- **Documentation**: JSDoc for frontend, Javadoc for backend

## 📞 Support

### Contact Information
- **Technical Support**: support@smartwatts.com
- **Documentation**: docs.smartwatts.com
- **Community**: community.smartwatts.com

### Issue Reporting
- **GitHub Issues**: For bug reports and feature requests
- **Security Issues**: security@smartwatts.com

---

**SmartWatts Energy Management Platform** - Empowering Nigerian households with intelligent energy monitoring and optimization.
