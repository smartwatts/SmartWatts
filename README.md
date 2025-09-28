# SmartWatts Energy Management Platform

An AI-powered, hybrid energy monitoring and optimization platform designed specifically for Nigeria and African energy realities.

## 🎯 Mission

Help households and businesses understand, control, and optimize their energy usage across grid, solar, inverter, and generator sources — using data-driven insights, AI, and affordable hardware.

## 🏗️ Architecture

SmartWatts uses a hybrid edge-cloud architecture:

- **Edge Gateway**: Python-based with TensorFlow Lite for ML inference
- **Cloud Backend**: Spring Boot microservices on AWS/GCP
- **Frontend**: React/Next.js with Tailwind CSS
- **Communication**: MQTT for real-time, REST for APIs
- **Storage**: PostgreSQL (cloud) + SQLite (edge)

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Python 3.11+
- Node.js 18+
- Docker 24+
- Git

### Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd mySmartWatts
   ```

2. **Start backend services**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

3. **Start frontend application**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Start edge gateway**
   ```bash
   cd edge-gateway
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   python main.py
   ```

5. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis
   ```

## 📁 Project Structure

```
mySmartWatts/
├── backend/                 # Spring Boot microservices
│   ├── user-service/       # User management service
│   ├── energy-service/     # Energy data processing
│   ├── device-service/     # IoT device management
│   ├── analytics-service/  # Analytics and reporting
│   └── billing-service/    # Billing and token tracking
├── frontend/               # React/Next.js dashboard
│   ├── components/         # Reusable UI components
│   ├── pages/             # Next.js pages
│   ├── hooks/             # Custom React hooks
│   └── utils/             # Utility functions
├── edge-gateway/          # Python edge gateway
│   ├── ml/               # Machine learning models
│   ├── drivers/          # IoT device drivers
│   ├── sync/             # Cloud synchronization
│   └── storage/          # Local data storage
├── infrastructure/        # Infrastructure as code
│   ├── terraform/        # Terraform configurations
│   ├── kubernetes/       # Kubernetes manifests
│   └── docker/           # Docker configurations
├── docs/                 # Documentation
│   ├── api/              # API documentation
│   ├── deployment/       # Deployment guides
│   └── user-guides/      # User documentation
└── memory-bank/          # Project documentation
    ├── projectbrief.md   # Core project definition
    ├── productContext.md # Product context and goals
    ├── systemPatterns.md # Architecture patterns
    ├── techContext.md    # Technical specifications
    ├── activeContext.md  # Current work focus
    └── progress.md       # Progress tracking
```

## 🎯 Key Features

### Phase 1 (MVP)
- ✅ Real-time hybrid monitoring dashboard
- ✅ Prepaid token tracking + MYTO tariff calculations
- ✅ Manual input + smart plug device detection
- ✅ DisCo availability + voltage/phase reports
- ✅ Basic AI: rule-based tips, cost estimation, savings insights
- ✅ Uptime/failure detection + energy summaries
- ✅ Local + remote data sync

### Phase 2 (Advanced)
- 🔄 Advanced AI: NILM-based appliance inference
- 🔄 Predictive maintenance + anomaly detection
- 🔄 Forecasting for 3, 6, 12-month cost trends
- 🔄 Generator health insights
- 🔄 User-configurable alerts + automation rules

### Phase 3 (Ecosystem)
- 📋 Energy commerce layer: device sales, token top-ups
- 📋 Integrated energy insurance
- 📋 Ecosystem APIs for 3rd-party access
- 📋 ESG & carbon tracking tools

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.x (Java 17+)
- **Database**: PostgreSQL 15+ for cloud, SQLite for edge
- **Message Queue**: Apache Kafka for event streaming
- **Cache**: Redis for session and data caching
- **Search**: Elasticsearch for energy data analytics

### Frontend
- **Framework**: React 18+ with Next.js 14+
- **Styling**: Tailwind CSS 3.3+ with custom components
- **State Management**: Zustand for client-side state
- **Charts**: Chart.js or D3.js for energy visualizations
- **PWA**: Service workers for offline functionality

### Edge Gateway
- **Runtime**: Python 3.11+ with virtual environment
- **ML Framework**: TensorFlow Lite 2.13+ for edge inference
- **Communication**: MQTT (Eclipse Mosquitto), Modbus TCP/RTU
- **Data Processing**: Pandas, NumPy for energy data analysis

### IoT & Hardware
- **Smart Plugs**: Shelly Pro 3EM, Shelly Plug S, Sonoff POW
- **Protocols**: MQTT, Modbus RTU/TCP, REST APIs
- **Energy Meters**: CT-based sensors for accurate measurement
- **Connectivity**: WiFi 5GHz, Zigbee 3.0, Bluetooth LE

## 📊 Success Metrics

### North Star Metrics
- 20% average energy cost savings per user
- 99.5% system uptime across deployments
- <5% churn rate across user segments
- 85% ML forecast accuracy after 12 months

### Additional KPIs
- Daily active dashboards
- Prepaid token value tracked
- Average generator runtime per week
- Detection latency for appliance anomalies
- Monthly active service partners

## 🔒 Security & Compliance

- **Authentication**: JWT tokens with refresh mechanism
- **Encryption**: AES-256 for data at rest, TLS 1.3 for data in transit
- **Compliance**: NDPR (Nigeria Data Protection Regulation)
- **Access Control**: RBAC with fine-grained permissions
- **Audit Trail**: Complete audit logging for all operations

## 🚀 Deployment

### Cloud Deployment
```bash
# Deploy to AWS/GCP
cd infrastructure/terraform
terraform init
terraform apply
```

### Edge Deployment
```bash
# Deploy edge gateway
cd edge-gateway
docker build -t smartwatts-gateway .
docker run -d smartwatts-gateway
```

## 📚 Documentation

- [API Documentation](./docs/api/)
- [Deployment Guides](./docs/deployment/)
- [User Guides](./docs/user-guides/)
- [Memory Bank](./memory-bank/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support, email support@smartwatts.ng or join our Slack channel.

---

**SmartWatts** - Empowering Africa's energy future through intelligent monitoring and optimization. 