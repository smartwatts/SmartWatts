# SmartWatts - AI-Powered Energy Intelligence Platform

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)
[![Next.js](https://img.shields.io/badge/Next.js-14+-black.svg)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

## 🌟 Overview

SmartWatts is a comprehensive AI-powered energy monitoring and optimization platform designed specifically for Nigeria and African energy realities. The platform integrates grid, solar, inverter, and generator sources with a hybrid edge-cloud architecture that works offline-first.

### 🎯 Key Features

- **🔌 Universal Device Support**: MQTT, Modbus RTU/TCP, HTTP, CoAP protocols
- **🧠 AI-Powered Analytics**: TensorFlow Lite for energy forecasting and anomaly detection
- **📊 Real-Time Monitoring**: Live energy consumption and generation tracking
- **💰 Cost Optimization**: MYTO tariff calculations and savings recommendations
- **🌍 Offline-First**: Works without internet connectivity
- **📱 Mobile-First**: Responsive design for smartphones and tablets
- **☁️ Cloud Sync**: Automatic data synchronization when online
- **🔍 Device Discovery**: Automatic detection and configuration of IoT devices

## 🏗️ Architecture

### Backend Services (13 Microservices)
- **API Gateway** (Port 8080) - Request routing and rate limiting
- **User Service** (Port 8081) - Authentication and user management
- **Energy Service** (Port 8082) - Energy data collection and processing
- **Device Service** (Port 8083) - IoT device management
- **Analytics Service** (Port 8084) - Data analytics and insights
- **Billing Service** (Port 8085) - Cost calculations and billing
- **API Docs Service** (Port 8086) - API documentation
- **Spring Boot Admin** (Port 8087) - Service monitoring
- **Edge Gateway** (Port 8088) - Edge device management
- **Facility Service** (Port 8089) - Facility and asset management
- **Feature Flag Service** (Port 8090) - Feature toggles
- **Device Verification** (Port 8091) - Device validation
- **Appliance Monitoring** (Port 8092) - Appliance-level monitoring

### Frontend
- **Next.js 14** with React 18 and TypeScript
- **Tailwind CSS** for styling
- **Zustand** for state management
- **Recharts** for data visualization
- **Responsive Design** for mobile and desktop

### Edge Gateway
- **FastAPI** with Python 3.11+
- **SQLite** for local storage
- **MQTT Broker** for device communication
- **Modbus RTU/TCP** for industrial devices
- **TensorFlow Lite** for AI inference
- **Prometheus** for monitoring

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose
- Node.js 18+ and npm
- Java 17+ (for local development)
- PostgreSQL 15+ (for local development)

### 1. Clone the Repository
```bash
git clone https://github.com/smartwatts/SmartWatts.git
cd SmartWatts
```

### 2. Backend Setup
```bash
# Start all backend services
docker-compose up -d

# Check service health
curl http://localhost:8080/actuator/health
```

### 3. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```

### 4. Access the Application
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **API Documentation**: http://localhost:8086
- **Admin Dashboard**: http://localhost:8087

## 🔧 Edge Gateway Installation

### For R501 RK3588 and Other Edge Devices

1. **Download Installation Guide**:
   - [Word Document](edge-gateway/docs/SmartWatts_Edge_Gateway_Installation_Guide.docx)
   - [HTML Version](edge-gateway/docs/SmartWatts_Edge_Gateway_Installation_Guide.html)
   - [Markdown Source](edge-gateway/docs/SmartWatts_Edge_Gateway_Installation_Guide.md)

2. **Quick Installation**:
```bash
# Download and run installation script
curl -fsSL https://raw.githubusercontent.com/smartwatts/SmartWatts/main/edge-gateway/deploy/install.sh | sudo bash
```

3. **Verify Installation**:
```bash
# Check service status
sudo systemctl status smartwatts-edge

# Test API
curl http://localhost:8080/api/v1/health
```

## 📊 Dashboard Features

### Consumer-Grade Features
- **AI Appliance Recognition**: NILM-based appliance detection
- **Circuit-Level Management**: Hierarchical circuit management
- **Solar Panel Monitoring**: Per-panel solar monitoring
- **Community Benchmarking**: Regional efficiency comparisons
- **Real-Time Alerts**: Smart notifications and recommendations

### Enterprise Features
- **Multi-Tenant Support**: Organization and facility management
- **Advanced Analytics**: Predictive analytics and forecasting
- **Cost Optimization**: MYTO tariff calculations
- **Device Management**: Comprehensive IoT device lifecycle
- **API Integration**: RESTful APIs for third-party integration

## 🛠️ Development

### Backend Development
```bash
# Start individual services
cd backend/user-service
./gradlew bootRun

# Run tests
./gradlew test

# Build Docker image
./gradlew bootBuildImage
```

### Frontend Development
```bash
cd frontend
npm run dev          # Development server
npm run build        # Production build
npm run start        # Production server
npm run lint         # Linting
npm run type-check   # TypeScript checking
```

### Edge Gateway Development
```bash
cd edge-gateway
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python main.py
```

## 📁 Project Structure

```
SmartWatts/
├── backend/                 # Spring Boot microservices
│   ├── api-gateway/        # API Gateway service
│   ├── user-service/       # User management
│   ├── energy-service/     # Energy monitoring
│   ├── device-service/     # Device management
│   ├── analytics-service/  # Data analytics
│   └── ...
├── frontend/               # Next.js React application
│   ├── components/         # React components
│   ├── pages/             # Next.js pages
│   ├── hooks/             # Custom React hooks
│   └── styles/            # Tailwind CSS styles
├── edge-gateway/          # Edge gateway implementation
│   ├── core/              # Core services
│   ├── services/          # MQTT, Modbus, AI services
│   ├── api/               # REST API endpoints
│   ├── deploy/            # Deployment scripts
│   └── docs/              # Documentation
├── qa-automation/         # Testing framework
├── monitoring/            # Prometheus and Grafana
├── nginx/                 # Reverse proxy configuration
├── ssl/                   # SSL certificates
└── docker-compose.yml     # Docker orchestration
```

## 🔒 Security

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Granular permission system
- **API Rate Limiting**: Protection against abuse
- **Data Encryption**: AES-256 for data at rest
- **TLS 1.3**: Secure data transmission
- **NDPR Compliance**: Nigeria Data Protection Regulation compliance

## 📈 Monitoring

### Health Checks
- **Service Health**: `/actuator/health` on all services
- **Database Health**: Connection and query monitoring
- **Redis Health**: Cache and rate limiting status
- **Edge Gateway Health**: Device connectivity and AI inference

### Metrics
- **Prometheus**: System and application metrics
- **Grafana**: Visualization dashboards
- **Log Aggregation**: Centralized logging with correlation IDs

## 🚀 Deployment

### Azure Free Tier Deployment
```bash
# Deploy to Azure with $0/month cost
./deploy-mysmartwatts.sh
```

### Production Deployment
```bash
# Production deployment with scaling
docker-compose -f docker-compose.prod.yml up -d
```

### Edge Device Deployment
```bash
# Deploy to edge device
scp -r edge-gateway/ user@edge-device:/opt/smartwatts/
ssh user@edge-device "cd /opt/smartwatts && sudo ./deploy/install.sh"
```

## 📚 Documentation

- [Installation Guide](edge-gateway/docs/SmartWatts_Edge_Gateway_Installation_Guide.md)
- [API Documentation](http://localhost:8086) (when running)
- [Architecture Overview](docs/ARCHITECTURE.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Contributing Guide](CONTRIBUTING.md)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌍 Support

- **Documentation**: [GitHub Wiki](https://github.com/bintinray/SmartWatts/wiki)
- **Issues**: [GitHub Issues](https://github.com/bintinray/SmartWatts/issues)
- **Discussions**: [GitHub Discussions](https://github.com/bintinray/SmartWatts/discussions)
- **Email**: support@mysmartwatts.com

## 🎯 Roadmap

### Phase 1: Foundation ✅ Complete
- [x] Core microservices architecture
- [x] Frontend dashboard
- [x] Edge gateway implementation
- [x] Basic device support

### Phase 2: Intelligence 🚧 In Progress
- [ ] Advanced AI models
- [ ] Predictive analytics
- [ ] Automated optimization
- [ ] Machine learning pipeline

### Phase 3: Scale 🌟 Planned
- [ ] Multi-region deployment
- [ ] Enterprise features
- [ ] Third-party integrations
- [ ] Mobile applications

## 🙏 Acknowledgments

- **Spring Boot** for the robust backend framework
- **Next.js** for the modern frontend framework
- **FastAPI** for the high-performance edge gateway
- **TensorFlow Lite** for edge AI capabilities
- **PostgreSQL** for reliable data storage
- **Docker** for containerization

---

**SmartWatts** - Revolutionizing energy monitoring in Nigeria and Africa with AI-powered intelligence.

Made with ❤️ for a sustainable energy future.