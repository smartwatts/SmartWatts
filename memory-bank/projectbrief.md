# SmartWatts Project Brief

## Core Mission
Build an AI-powered, hybrid energy monitoring and optimization platform designed specifically for Nigeria and African energy realities.

## Primary Goals
- Help households and businesses understand, control, and optimize their energy usage
- Integrate grid, solar, inverter, and generator sources seamlessly
- Provide data-driven insights and AI-powered optimization
- Work with affordable hardware solutions
- Support offline-first edge architecture for unreliable internet conditions

## Key Differentiators
- **Multi-source Integration**: Grid + Inverter + Solar + Generator out of the box
- **Offline-First**: Edge architecture that works with unreliable internet
- **AI-First**: Energy intelligence tuned for African patterns and constraints
- **Smart Plug Integration**: Circuit-level visibility without complex installations
- **Financial Tools**: Insurance, DisCo billing, cost projections
- **Commerce Stack**: Installer onboarding, replacement parts, token top-ups

## Target User Segments
1. Large enterprises and telcos
2. Banks and commercial buildings
3. SMEs and multi-shop complexes
4. Peri-urban and upwardly mobile households

## Success Metrics
- 20% average energy cost savings per user
- 99.5% system uptime across deployments
- <5% churn rate across user segments
- 85% ML forecast accuracy after 12 months

## Technology Stack
- **Backend**: Spring Boot (Java) microservices
- **Edge AI/ML**: TensorFlow Lite, PyTorch Mobile
- **Gateway OS**: Ubuntu Core, Balena (or alternatives)
- **Gateway Development**: Python workers for ML tasks
- **Frontend**: React/Next.js with Tailwind CSS
- **Communication**: MQTT, Modbus, REST APIs
- **Smart Plugs**: WiFi/Zigbee with energy meter accuracy

## Development Phases
### Phase 1 (MVP)
- Real-time hybrid monitoring dashboard
- Prepaid token tracking + MYTO tariff calculations
- Manual input + smart plug device detection
- DisCo availability + voltage/phase reports
- Basic AI: rule-based tips, cost estimation, savings insights
- Uptime/failure detection + energy summaries
- Local + remote data sync

### Phase 2
- Advanced AI: NILM-based appliance inference, seasonal projections
- Predictive maintenance + anomaly detection
- Forecasting for 3, 6, 12-month cost trends
- Generator health insights
- User-configurable alerts + automation rules

### Phase 3
- Energy commerce layer: device sales, token top-ups, parts marketplace
- Integrated energy insurance (partner-based or underwritten)
- Ecosystem APIs for 3rd-party access and OEM onboarding
- ESG & carbon tracking tools for enterprises

## Business Model
- Hardware: Gateway + smart plug bundles (tiered pricing)
- SaaS Subscription: Basic, Pro, Enterprise plans
- Marketplace: commissions on token top-ups, insurance, hardware sales
- Partner licensing: telcos, DisCos, solar EPCs 