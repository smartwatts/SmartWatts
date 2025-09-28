# SmartWatts Device Verification & Activation Service

## Overview

The Device Verification & Activation Service is a critical security component of the SmartWatts platform that ensures only trusted, verified devices can participate in telemetry ingestion and analytics. This service implements a sophisticated dual token validity system for different customer types.

## Key Features

### 🔐 **Dual Token Validity System**
- **Home/Residential Customers**: 12-month activation tokens initially
- **Commercial/Industrial Customers**: 3-month activation tokens initially, 12 months on renewal
- **All Renewals**: 12-month validity for all customer types after expiry

### 🛡️ **Device Trust Categories**
- **OEM_LOCKED**: Supplied by SmartWatts OEM or certified installer (fully trusted)
- **OFFLINE_LOCKED**: Verified offline with signed activation token (trusted)
- **UNVERIFIED**: Any other device (blocked)
- **EXPIRED**: Device activation expired

### 🔍 **Security Features**
- Tamper detection and audit trail logging
- Docker startup validation on edge devices
- Remote lock/reset capability for compromised devices
- Installer tiers with scoped permissions
- Backup verification paths (SMS codes)

## Architecture

### Service Components
- **Device Verification**: Core verification logic and device management
- **Activation Token Management**: JWT-based token generation and validation
- **Audit Logging**: Comprehensive activity tracking and compliance
- **Installer Management**: Tiered installer permissions and auto-approval

### Database Schema
- `device_verifications`: Main device verification records
- `activation_tokens`: JWT token storage and management
- `verification_audit_logs`: Comprehensive audit trail
- `installers`: Installer management and permissions
- `renewal_requests`: Device renewal and upgrade requests

## API Endpoints

### Device Activation
```http
POST /api/device-verification/activate
```
Activates a new device or renews an existing device with appropriate token validity.

### Device Validation
```http
POST /api/device-verification/validate?deviceId={id}&token={token}
```
Validates device access for telemetry ingestion or dashboard access.

### Device Status
```http
GET /api/device-verification/status/{deviceId}
```
Returns current device status and verification information.

### Service Information
```http
GET /api/device-verification/info
```
Returns service information and feature details.

## Token Validity Rules

### Initial Activation
| Customer Type | Initial Validity | Purpose |
|---------------|------------------|---------|
| **Residential** | 12 months (365 days) | Convenience for home users |
| **Commercial** | 3 months (90 days) | Security control and engagement |

### Renewal Activation
| Customer Type | Renewal Validity | Purpose |
|---------------|------------------|---------|
| **All Customers** | 12 months (365 days) | Reward for renewal, encourage retention |

### Business Benefits
- **Residential**: Longer initial period reduces support calls and improves user experience
- **Commercial**: Shorter initial period allows for:
  - Better security control
  - Regular customer engagement
  - Upsell opportunities for extended support
  - Compliance with business security policies

## Device Activation Flow

### 1. New Device Activation
```
Device Request → Metadata Validation → Trust Category Determination → 
Token Generation → Database Storage → Response with Token
```

### 2. Device Renewal
```
Renewal Request → Device Identity Verification → 
New Token Generation (12 months) → Old Token Revocation → Response
```

### 3. Access Validation
```
Request with Token → Token Validation → Device Status Check → 
Expiry Verification → Tamper Detection → Access Grant/Deny
```

## Configuration

### Environment Variables
```yaml
jwt:
  secret: ${JWT_SECRET:smartwatts-device-verification-secret-key-2025}
  expiration:
    residential: 31536000  # 12 months in seconds
    commercial: 7776000    # 3 months in seconds

device:
  verification:
    max-activation-attempts: 3
    activation-timeout: 300  # 5 minutes
    tamper-detection:
      enabled: true
      firmware-hash-validation: true
      docker-startup-validation: true
```

### Database Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartwatts_device_verification
    username: postgres
    password: postgres
```

## Integration

### SmartWatts Services
- **User Service**: Customer type determination and user validation
- **Device Service**: Device metadata and status synchronization
- **Feature Flag Service**: Access control based on verification status
- **Billing Service**: Subscription plan validation for renewals

### External Systems
- **OEM Systems**: Direct integration for pre-verified devices
- **Installer Portals**: Tiered access for device activation
- **SMS Gateways**: Backup verification for offline installers

## Security Considerations

### Token Security
- JWT tokens with HS512 signing
- Token hashing for database storage
- Automatic token expiration and renewal
- Token revocation on device compromise

### Device Security
- Hardware ID validation
- Firmware hash verification
- Docker image integrity checks
- Tamper detection and alerting

### Access Control
- Role-based installer permissions
- Geographic location validation
- Rate limiting on activation attempts
- Comprehensive audit logging

## Monitoring and Alerting

### Health Checks
- Service health endpoint
- Database connectivity monitoring
- Token validation performance metrics
- Device activation success rates

### Audit Logging
- All device activations and renewals
- Token validation attempts
- Tamper detection events
- Installer activity tracking

## Deployment

### Docker
```bash
# Build the service
docker-compose build device-verification-service

# Start the service
docker-compose up -d device-verification-service
```

### Database Setup
```sql
-- Create database
CREATE DATABASE smartwatts_device_verification;

-- Run migrations
-- V1__Create_device_verification_tables.sql
```

## Development

### Prerequisites
- Java 17+
- Spring Boot 3.2.0+
- PostgreSQL 15+
- Docker

### Local Development
```bash
# Start dependencies
docker-compose up -d postgres service-discovery

# Run the service
./gradlew bootRun
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run integration tests
./gradlew integrationTest
```

## Support and Maintenance

### Common Issues
- **Token Expiration**: Automatic renewal process handles expired tokens
- **Device Tampering**: Automatic detection and status updates
- **Activation Failures**: Comprehensive error logging and troubleshooting

### Maintenance Tasks
- Regular token cleanup (expired/revoked)
- Audit log rotation and archiving
- Database performance optimization
- Security updates and patches

## Future Enhancements

### Planned Features
- **AI-powered Tamper Detection**: Machine learning for advanced threat detection
- **Blockchain Integration**: Immutable device verification records
- **Multi-factor Authentication**: Enhanced security for commercial devices
- **Real-time Monitoring**: Live device status and health monitoring

### Scalability Improvements
- **Horizontal Scaling**: Load balancing across multiple service instances
- **Caching Layer**: Redis integration for improved performance
- **Async Processing**: Event-driven architecture for high throughput
- **Microservice Decomposition**: Further service separation for specific functions

---

**SmartWatts Device Verification & Activation Service** - Ensuring platform security through intelligent device management and dual token validity system.
