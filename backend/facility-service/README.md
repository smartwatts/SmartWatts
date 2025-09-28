# Facility360 Service

## Overview

The Facility360 Service is a comprehensive facility management microservice integrated into the SmartWatts platform. It provides complete asset management, work order tracking, space management, and fleet management capabilities for commercial and industrial facilities.

## Features

### Core Management Modules

- **Asset Management**: Track and manage all facility assets including equipment, machinery, and infrastructure
- **Work Order Management**: Complete work order lifecycle from creation to completion
- **Space Management**: Manage facility spaces, room assignments, and space utilization
- **Fleet Management**: Track vehicles, maintenance schedules, and fleet operations

### Advanced Capabilities

- **Preventive Maintenance**: Automated maintenance scheduling and tracking
- **Cost Tracking**: Monitor asset costs, maintenance expenses, and ROI
- **Compliance Management**: Track regulatory requirements and audit trails
- **IoT Integration**: Support for facility IoT devices and sensors
- **Real-time Monitoring**: Live status updates and alerts

## Architecture

### Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 15+ (smartwatts_facility360)
- **ORM**: Spring Data JPA with Hibernate
- **Migrations**: Flyway 10.0.1
- **Service Discovery**: Netflix Eureka Client
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Security**: OAuth2 Resource Server (configurable)

### Service Structure

```
facility-service/
├── model/          # JPA entities
├── dto/            # Data transfer objects
├── repository/     # Data access layer
├── service/        # Business logic layer
├── controller/     # REST API endpoints
├── config/         # Configuration classes
└── resources/      # Configuration files and migrations
```

## API Endpoints

### Asset Management
- `GET /api/v1/assets` - List all assets
- `POST /api/v1/assets` - Create new asset
- `GET /api/v1/assets/{id}` - Get asset by ID
- `PUT /api/v1/assets/{id}` - Update asset
- `DELETE /api/v1/assets/{id}` - Delete asset

### Work Order Management
- `GET /api/v1/work-orders` - List all work orders
- `POST /api/v1/work-orders` - Create new work order
- `GET /api/v1/work-orders/{id}` - Get work order by ID
- `PUT /api/v1/work-orders/{id}` - Update work order
- `PATCH /api/v1/work-orders/{id}/status` - Update work order status

### Space Management
- `GET /api/v1/spaces` - List all spaces
- `POST /api/v1/spaces` - Create new space
- `GET /api/v1/spaces/{id}` - Get space by ID
- `PUT /api/v1/spaces/{id}` - Update space
- `PATCH /api/v1/spaces/{id}/assign` - Assign space to user

### Fleet Management
- `GET /api/v1/fleet` - List all fleet vehicles
- `POST /api/v1/fleet` - Create new fleet entry
- `GET /api/v1/fleet/{id}` - Get fleet by ID
- `PUT /api/v1/fleet/{id}` - Update fleet
- `PATCH /api/v1/fleet/{id}/driver` - Assign driver

## Database Schema

### Core Tables

- `assets` - Facility assets and equipment
- `work_orders` - Maintenance and repair work orders
- `spaces` - Facility spaces and rooms
- `fleet` - Vehicle fleet management
- `maintenance_schedules` - Preventive maintenance planning
- `space_bookings` - Space reservation system
- `fleet_trips` - Vehicle trip tracking
- `facility_incidents` - Incident reporting and tracking
- `compliance_checklists` - Regulatory compliance tracking

### Key Features

- **Soft Delete**: All entities support logical deletion
- **Audit Trail**: Complete creation and modification tracking
- **Automatic Timestamps**: Created and updated timestamps
- **Unique Codes**: Auto-generated asset and work order numbers
- **Status Tracking**: Comprehensive status management

## Configuration

### Environment Variables

- `DATABASE_URL`: PostgreSQL connection string
- `DATABASE_USER`: Database username
- `DATABASE_PASSWORD`: Database password
- `EUREKA_SERVER_URL`: Service discovery server URL

### Profiles

- **local**: Development configuration with local PostgreSQL
- **docker**: Containerized deployment configuration
- **test**: Testing configuration with H2 in-memory database

## Development

### Prerequisites

- Java 17+
- Gradle 7.6+
- PostgreSQL 15+
- Docker (optional)

### Local Development

1. **Start PostgreSQL**:
   ```bash
   docker run -d --name postgres-facility \
     -e POSTGRES_DB=smartwatts_facility360 \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15-alpine
   ```

2. **Run Migrations**:
   ```bash
   ./gradlew flywayMigrate
   ```

3. **Start Service**:
   ```bash
   ./gradlew bootRun
   ```

4. **Access API Documentation**:
   ```
   http://localhost:8089/swagger-ui.html
   ```

### Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Integration tests
./gradlew integrationTest
```

## Deployment

### Docker

```bash
# Build image
docker build -t smartwatts-facility-service .

# Run container
docker run -d \
  --name facility-service \
  -p 8089:8089 \
  -e DATABASE_URL=jdbc:postgresql://postgres:5432/smartwatts_facility360 \
  smartwatts-facility-service
```

### Docker Compose

The service is included in the main `docker-compose.yml` file and will start automatically with the platform.

## Integration

### SmartWatts Platform

- **User Service**: Shared authentication and authorization
- **API Gateway**: Unified routing and load balancing
- **Service Discovery**: Automatic service registration
- **Shared Database**: Read-only access to user and energy data

### External Systems

- **IoT Devices**: MQTT-based device communication
- **Payment Gateways**: Integration with billing systems
- **Notification Services**: Email and SMS alerts
- **Reporting Systems**: Data export and analytics

## Monitoring

### Health Checks

- **Health Endpoint**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`

### Logging

- **Log Level**: Configurable per package
- **Structured Logging**: JSON format for production
- **Audit Logging**: Complete operation tracking

## Security

### Authentication

- **OAuth2**: JWT token validation
- **User Service**: Centralized user management
- **Role-Based Access**: Fine-grained permissions

### Data Protection

- **Encryption**: Data at rest and in transit
- **Audit Logging**: Complete access tracking
- **Compliance**: NDPR and industry standards

## Support

### Documentation

- **API Reference**: Swagger UI at `/swagger-ui.html`
- **Database Schema**: Flyway migration scripts
- **Code Examples**: Comprehensive test suite

### Troubleshooting

- **Health Checks**: Monitor service status
- **Logs**: Check application logs for errors
- **Database**: Verify database connectivity
- **Service Discovery**: Check Eureka registration

## License

This service is part of the SmartWatts platform and follows the same licensing terms.
