# SmartWatts Device Verification API Documentation

## Overview

The Device Verification API provides comprehensive security and trust management for IoT devices in the SmartWatts platform. This system ensures that only verified and trusted devices can send energy data, protecting the platform from unauthorized access and data manipulation.

## Base URL

```
http://localhost:8083/api/v1/devices/verification
```

## Authentication

All endpoints require valid JWT authentication tokens. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### 1. Submit Device for Verification

**POST** `/submit`

Submits a device for verification review by administrators.

#### Request Body

```json
{
  "deviceId": "string",
  "samplePayload": "string",
  "notes": "string",
  "brand": "string",
  "model": "string",
  "preferredProtocol": "string"
}
```

#### Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `deviceId` | string | Yes | Unique identifier for the device |
| `samplePayload` | string | Yes | Sample data payload from the device |
| `notes` | string | No | Additional notes about the device |
| `brand` | string | No | Device brand/manufacturer |
| `model` | string | No | Device model number |
| `preferredProtocol` | string | No | Preferred communication protocol |

#### Response

**200 OK**
```json
{
  "id": "uuid",
  "deviceId": "string",
  "name": "string",
  "deviceType": "string",
  "manufacturer": "string",
  "model": "string",
  "serialNumber": "string",
  "userId": "uuid",
  "location": "string",
  "protocol": "string",
  "connectionStatus": "string",
  "isVerified": false,
  "verificationStatus": "PENDING",
  "trustLevel": "UNVERIFIED",
  "verificationRequestDate": "2024-01-15T10:30:00Z"
}
```

### 2. Get Devices Pending Verification

**GET** `/pending`

Retrieves all devices that are pending verification review.

#### Response

**200 OK**
```json
[
  {
    "id": "uuid",
    "deviceId": "string",
    "name": "string",
    "deviceType": "string",
    "verificationStatus": "PENDING",
    "trustLevel": "UNVERIFIED",
    "verificationRequestDate": "2024-01-15T10:30:00Z"
  }
]
```

### 3. Get Devices Under Review

**GET** `/under-review`

Retrieves all devices that are currently under review.

#### Response

**200 OK**
```json
[
  {
    "id": "uuid",
    "deviceId": "string",
    "name": "string",
    "deviceType": "string",
    "verificationStatus": "UNDER_REVIEW",
    "trustLevel": "UNVERIFIED",
    "verificationRequestDate": "2024-01-15T10:30:00Z"
  }
]
```

### 4. Review Device Verification

**POST** `/review`

Allows administrators to review and approve/reject device verification requests.

#### Request Body

```json
{
  "deviceId": "string",
  "verificationStatus": "APPROVED|REJECTED|SUSPENDED",
  "notes": "string",
  "reviewerId": "uuid"
}
```

#### Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `deviceId` | string | Yes | Unique identifier for the device |
| `verificationStatus` | string | Yes | New verification status |
| `notes` | string | No | Review notes and comments |
| `reviewerId` | string | Yes | UUID of the reviewer |

#### Response

**200 OK**
```json
{
  "id": "uuid",
  "deviceId": "string",
  "name": "string",
  "deviceType": "string",
  "verificationStatus": "APPROVED",
  "trustLevel": "VERIFIED",
  "isVerified": true,
  "verificationDate": "2024-01-15T11:00:00Z",
  "verificationReviewer": "uuid",
  "verificationNotes": "Device approved after review"
}
```

### 5. Mark Device Under Review

**POST** `/{deviceId}/mark-under-review`

Marks a device as under review for manual verification.

#### Path Parameters

| Parameter | Type | Description |
|------------|------|-------------|
| `deviceId` | string | Unique identifier for the device |

#### Response

**200 OK**
```json
{
  "id": "uuid",
  "deviceId": "string",
  "verificationStatus": "UNDER_REVIEW",
  "trustLevel": "UNVERIFIED"
}
```

### 6. Check Device Data Permission

**GET** `/{deviceId}/can-send-data`

Checks if a device is allowed to send data to the platform.

#### Path Parameters

| Parameter | Type | Description |
|------------|------|-------------|
| `deviceId` | string | Unique identifier for the device |

#### Response

**200 OK**
```
true
```
or
```
false
```

### 7. Validate Device Authentication Secret

**POST** `/validate-auth`

Validates a device's authentication secret for secure data transmission.

#### Request Body

```json
{
  "deviceId": "string",
  "authSecret": "string"
}
```

#### Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `deviceId` | string | Yes | Unique identifier for the device |
| `authSecret` | string | Yes | Authentication secret to validate |

#### Response

**200 OK**
```
true
```
or
```
false
```

### 8. Get Device Verification Status

**GET** `/{deviceId}/status`

Retrieves the current verification status of a device.

#### Path Parameters

| Parameter | Type | Description |
|------------|------|-------------|
| `deviceId` | string | Unique identifier for the device |

#### Response

**200 OK**
```
APPROVED
```
or
```
PENDING
```
or
```
UNDER_REVIEW
```
or
```
REJECTED
```
or
```
SUSPENDED
```

### 9. Generate Device Authentication Secret

**POST** `/{deviceId}/generate-auth-secret`

Generates a new authentication secret for an approved device.

#### Path Parameters

| Parameter | Type | Description |
|------------|------|-------------|
| `deviceId` | string | Unique identifier for the device |

#### Response

**200 OK**
```
generated-secret-123
```

### 10. Get Device Verification Details

**GET** `/{deviceId}/details`

Retrieves comprehensive verification details for a device.

#### Path Parameters

| Parameter | Type | Description |
|------------|------|-------------|
| `deviceId` | string | Unique identifier for the device |

#### Response

**200 OK**
```json
{
  "id": "uuid",
  "deviceId": "string",
  "name": "string",
  "deviceType": "string",
  "manufacturer": "string",
  "model": "string",
  "serialNumber": "string",
  "userId": "uuid",
  "location": "string",
  "protocol": "string",
  "connectionStatus": "string",
  "isVerified": true,
  "verificationStatus": "APPROVED",
  "trustLevel": "VERIFIED",
  "deviceAuthSecret": "string",
  "verificationNotes": "string",
  "samplePayload": "string",
  "verificationRequestDate": "2024-01-15T10:30:00Z",
  "verificationReviewDate": "2024-01-15T11:00:00Z",
  "verificationReviewer": "uuid"
}
```

**404 Not Found**
```json
{
  "error": "Device not found",
  "message": "Device with ID NONEXISTENT_DEVICE not found",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

### 11. Get Verification Statistics

**GET** `/stats`

Retrieves statistics about device verification statuses.

#### Response

**200 OK**
```json
{
  "pendingCount": 5,
  "underReviewCount": 2,
  "approvedCount": 15,
  "rejectedCount": 3,
  "suspendedCount": 1
}
```

## Data Models

### Device Verification Status

| Status | Description |
|--------|-------------|
| `PENDING` | Device submitted for verification, awaiting review |
| `UNDER_REVIEW` | Device under manual review by administrator |
| `APPROVED` | Device verified and approved for data transmission |
| `REJECTED` | Device verification rejected |
| `SUSPENDED` | Device temporarily suspended from data transmission |

### Device Trust Level

| Level | Description |
|-------|-------------|
| `OEM_LOCKED` | SmartWatts OEM devices, pre-approved |
| `VERIFIED` | Manually verified and approved devices |
| `UNVERIFIED` | Devices pending verification |
| `SUSPENDED` | Devices suspended due to security concerns |

## Error Responses

### 400 Bad Request
```json
{
  "error": "Validation Error",
  "message": "Device ID is required",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired authentication token",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Insufficient permissions to perform this action",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Device with ID DEVICE_001 not found",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-15T11:00:00Z"
}
```

## Security Considerations

### Device Authentication Secret
- Generated automatically upon device approval
- 32-character random string
- Used for secure data transmission validation
- Can be regenerated if compromised

### Trust Level Management
- OEM devices are automatically trusted
- Third-party devices require manual verification
- Suspended devices cannot transmit data
- Trust levels can be downgraded based on behavior

### Data Validation
- All incoming data is validated against device verification status
- Authentication secrets are validated for each transmission
- Failed validations are logged for security auditing

## Rate Limiting

- **Submit Verification**: 10 requests per minute per user
- **Review Verification**: 50 requests per minute per admin
- **Status Checks**: 100 requests per minute per device
- **Auth Validation**: 1000 requests per minute per device

## Integration Examples

### Frontend Integration

```javascript
// Submit device for verification
const submitVerification = async (deviceData) => {
  const response = await fetch('/api/v1/devices/verification/submit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(deviceData)
  });
  return response.json();
};

// Check if device can send data
const canDeviceSendData = async (deviceId) => {
  const response = await fetch(`/api/v1/devices/verification/${deviceId}/can-send-data`);
  return response.text() === 'true';
};
```

### Backend Integration

```java
// Validate device data ingestion
@Autowired
private DataIngestionSecurityService securityService;

public void processDeviceData(EnergyReadingDto reading, String authSecret) {
    try {
        securityService.validateDeviceDataIngestion(reading, authSecret);
        // Process data if validation passes
        energyService.saveEnergyReading(reading);
    } catch (DeviceNotVerifiedException e) {
        log.warn("Unverified device {} attempted to send data", e.getDeviceId());
        // Handle unverified device
    } catch (InvalidDeviceAuthException e) {
        log.warn("Invalid auth secret for device {}", e.getDeviceId());
        // Handle invalid authentication
    }
}
```

### Edge Gateway Integration

```java
// MQTT message handler with security validation
@Autowired
private EdgeSecurityService edgeSecurityService;

public void handleDeviceData(String deviceId, String payload, String authSecret) {
    // Check if device can send data
    if (!edgeSecurityService.canDeviceSendData(deviceId)) {
        log.warn("Device {} not verified, rejecting data", deviceId);
        return;
    }
    
    // Validate authentication secret
    if (!edgeSecurityService.validateDeviceAuthSecret(deviceId, authSecret)) {
        log.warn("Invalid auth secret for device {}", deviceId);
        return;
    }
    
    // Process valid data
    processValidDeviceData(deviceId, payload);
}
```

## Testing

### Unit Tests
- All service methods have comprehensive unit tests
- Mock external dependencies for isolated testing
- Test coverage exceeds 80%

### Integration Tests
- End-to-end workflow testing
- Database integration testing
- API endpoint testing with MockMvc

### Security Testing
- Authentication bypass testing
- Authorization testing
- Input validation testing
- SQL injection testing

## Monitoring and Logging

### Security Events
- All verification attempts are logged
- Failed validations are tracked
- Suspicious activity is flagged
- Audit trail maintained for compliance

### Performance Metrics
- Response time monitoring
- Throughput tracking
- Error rate monitoring
- Resource utilization tracking

## Future Enhancements

### Planned Features
- Automated device verification using ML
- Real-time threat detection
- Advanced analytics for device behavior
- Integration with external security services

### Scalability Improvements
- Redis caching for verification status
- Async processing for high-volume operations
- Horizontal scaling support
- Microservice architecture optimization

## Support

For technical support or questions about the Device Verification API:

- **Email**: dev-support@mysmartwatts.com
- **Documentation**: https://docs.mysmartwatts.com
- **GitHub Issues**: https://github.com/smartwatts/backend/issues
- **Slack**: #dev-support channel

## Version History

- **v1.0.0** (2024-01-15): Initial release with core verification workflow
- **v1.1.0** (Planned): Enhanced security features and ML integration
- **v1.2.0** (Planned): Advanced analytics and threat detection
