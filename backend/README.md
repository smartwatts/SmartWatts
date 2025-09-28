# SmartWatts Backend Architecture

## Overview

SmartWatts backend is built using a microservices architecture with Spring Boot, designed specifically for Nigerian energy monitoring and optimization. The system integrates grid, solar, inverter, and generator sources with offline-first edge architecture.

## Architecture Components

### 🏗️ Microservices

| Service | Port | Description |
|---------|------|-------------|
| **Service Discovery** | 8761 | Eureka server for service registration |
| **API Gateway** | 8080 | Central routing and security |
| **User Service** | 8081 | User management and authentication |
| **Energy Service** | 8082 | Energy data collection and processing |
| **Device Service** | 8083 | IoT device management |
| **Analytics Service** | 8084 | Energy analytics and insights |
| **Billing Service** | 8085 | MYTO tariffs and billing management |

### 🗄️ Database Architecture

Each service has its own PostgreSQL database:

- `smartwatts_users` - User management
- `smartwatts_energy` - Energy consumption data
- `smartwatts_devices` - IoT device management
- `smartwatts_analytics` - Analytics and insights
- `smartwatts_billing` - Billing and tariff data

### 🔧 Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security + JWT
- **Documentation**: OpenAPI 3.0
- **Testing**: JUnit 5 + Testcontainers

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Gradle 8.0+

### Local Development

1. **Clone and navigate to backend**
   ```bash
   cd backend
   ```

2. **Start all services with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Verify services are running**
   - Service Discovery: http://localhost:8761
   - API Gateway: http://localhost:8080
   - User Service: http://localhost:8081
   - Energy Service: http://localhost:8082
   - Device Service: http://localhost:8083
   - Analytics Service: http://localhost:8084
   - Billing Service: http://localhost:8085

### Individual Service Development

Each service can be run independently:

```bash
# Navigate to service directory
cd user-service

# Run with Gradle
./gradlew bootRun

# Or build and run
./gradlew build
java -jar build/libs/user-service-0.0.1-SNAPSHOT.jar
```

## 📋 Service Details

### User Service (`user-service`)
- User registration and authentication
- JWT token management
- Role-based access control
- Profile management

**Key APIs:**
- `POST /api/v1/users/register` - User registration
- `POST /api/v1/users/login` - User authentication
- `GET /api/v1/users/profile` - Get user profile
- `PUT /api/v1/users/profile` - Update user profile

### Energy Service (`energy-service`)
- Real-time energy data collection
- Consumption tracking
- Energy source management
- Historical data storage

**Key APIs:**
- `POST /api/v1/energy/consumption` - Record energy consumption
- `GET /api/v1/energy/consumption/{userId}` - Get user consumption
- `GET /api/v1/energy/sources/{userId}` - Get energy sources
- `GET /api/v1/energy/history/{userId}` - Get consumption history

### Device Service (`device-service`)
- IoT device management
- Device registration and configuration
- Device status monitoring
- Firmware updates

**Key APIs:**
- `POST /api/v1/devices/register` - Register new device
- `GET /api/v1/devices/{deviceId}` - Get device details
- `PUT /api/v1/devices/{deviceId}/config` - Update device config
- `GET /api/v1/devices/user/{userId}` - Get user devices

### Analytics Service (`analytics-service`)
- Energy consumption analytics
- Cost optimization insights
- Usage patterns analysis
- Predictive analytics

**Key APIs:**
- `GET /api/v1/analytics/consumption/{userId}` - Consumption analytics
- `GET /api/v1/analytics/cost-optimization/{userId}` - Cost insights
- `GET /api/v1/analytics/patterns/{userId}` - Usage patterns
- `GET /api/v1/analytics/predictions/{userId}` - Consumption predictions

### Billing Service (`billing-service`)
- MYTO tariff management
- Bill generation and calculation
- Prepaid token management
- Payment processing

**Key APIs:**
- `POST /api/v1/bills` - Create new bill
- `GET /api/v1/bills/user/{userId}` - Get user bills
- `POST /api/v1/tokens` - Create prepaid token
- `GET /api/v1/tariffs/active` - Get active tariffs

## 🔐 Security

### Authentication
- JWT-based authentication
- Token refresh mechanism
- Role-based authorization

### API Gateway Security
- Rate limiting (100 requests/minute)
- Circuit breaker pattern
- CORS configuration
- Request validation

### Database Security
- Encrypted connections (TLS)
- Prepared statements
- Input validation
- SQL injection prevention

## 📊 Monitoring & Health Checks

### Actuator Endpoints
Each service exposes health and metrics endpoints:
- `GET /actuator/health` - Service health
- `GET /actuator/metrics` - Performance metrics
- `GET /actuator/info` - Service information

### Circuit Breaker
Resilience4j circuit breakers protect against service failures:
- Failure rate threshold: 50%
- Sliding window size: 10 requests
- Recovery time: 5 seconds

## 🧪 Testing

### Test Coverage
- Unit tests: 80%+ coverage
- Integration tests with Testcontainers
- API tests with MockMvc
- Performance tests with JMeter

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific service tests
cd user-service
./gradlew test

# Run with coverage
./gradlew jacocoTestReport
```

## 🚀 Deployment

### Docker Deployment
```bash
# Build all services
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Production Deployment
- Kubernetes orchestration
- AWS/GCP cloud deployment
- Blue-green deployment strategy
- Auto-scaling configuration

## 📈 Performance

### Response Times
- API calls: < 200ms
- Complex queries: < 2s
- Database operations: < 100ms

### Scalability
- Horizontal scaling support
- Load balancing
- Database connection pooling
- Caching strategies

## 🔧 Configuration

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/smartwatts_users
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

### Profiles
- `dev` - Development configuration
- `docker` - Docker environment
- `prod` - Production configuration

## 📚 API Documentation

### OpenAPI/Swagger
Each service provides interactive API documentation:
- User Service: http://localhost:8081/swagger-ui.html
- Energy Service: http://localhost:8082/swagger-ui.html
- Device Service: http://localhost:8083/swagger-ui.html
- Analytics Service: http://localhost:8084/swagger-ui.html
- Billing Service: http://localhost:8085/swagger-ui.html

## 🐛 Troubleshooting

### Common Issues

1. **Service not starting**
   ```bash
   # Check logs
   docker-compose logs service-name
   
   # Check health endpoint
   curl http://localhost:8080/actuator/health
   ```

2. **Database connection issues**
   ```bash
   # Check PostgreSQL
   docker-compose logs postgres
   
   # Test connection
   psql -h localhost -U postgres -d smartwatts_users
   ```

3. **Service discovery issues**
   ```bash
   # Check Eureka dashboard
   http://localhost:8761
   
   # Verify service registration
   curl http://localhost:8761/eureka/apps
   ```

## 🤝 Contributing

### Development Workflow
1. Create feature branch
2. Write tests
3. Implement feature
4. Ensure 80%+ test coverage
5. Submit pull request

### Code Standards
- Follow Google Java Style Guide
- Use meaningful commit messages
- Include comprehensive tests
- Update documentation

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue on GitHub
- Contact the development team
- Check the documentation wiki 