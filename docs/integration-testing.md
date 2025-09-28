# SmartWatts Integration Testing Guide

## Overview
This guide provides step-by-step instructions for testing the integration between the SmartWatts frontend and backend services.

## Prerequisites

### Required Software
- **Docker & Docker Compose**: For running backend services
- **Node.js 18+**: For running the frontend
- **Git**: For version control

### Environment Setup
1. **Clone the repository** (if not already done):
   ```bash
   git clone <repository-url>
   cd mySmartWatts
   ```

2. **Install frontend dependencies**:
   ```bash
   cd frontend
   npm install
   ```

## Step 1: Start Backend Services

### Option A: Using the Test Script
```bash
# Make the script executable
chmod +x test-integration.sh

# Run the script
./test-integration.sh
```

### Option B: Manual Docker Compose
```bash
# Start all services
docker-compose up --build -d

# Wait for services to be ready (30-60 seconds)
sleep 45
```

## Step 2: Start Frontend Development Server

```bash
# Navigate to frontend directory
cd frontend

# Start the development server
npm run dev
```

The frontend will be available at: **http://localhost:3000**

## Step 3: Verify Service Health

### Check Backend Services
```bash
# API Gateway Health
curl http://localhost:8080/actuator/health

# Individual Service Health
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Energy Service
curl http://localhost:8083/actuator/health  # Device Service
curl http://localhost:8084/actuator/health  # Analytics Service
curl http://localhost:8085/actuator/health  # Billing Service
```

### Expected Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

## Step 4: Test User Authentication

### 1. Registration Test
1. Navigate to: **http://localhost:3000/register**
2. Fill in the registration form:
   - First Name: `Test`
   - Last Name: `User`
   - Email: `test@smartwatts.com`
   - Phone: `+2348012345678`
   - Password: `password123`
   - Confirm Password: `password123`
3. Click "Create account"
4. **Expected Result**: Redirected to dashboard

### 2. Login Test
1. Navigate to: **http://localhost:3000/login**
2. Use the credentials from registration
3. Click "Sign in"
4. **Expected Result**: Redirected to dashboard

## Step 5: Test Dashboard Integration

### 1. Dashboard Overview
1. Navigate to: **http://localhost:3000/dashboard**
2. **Expected Elements**:
   - Welcome message with user name
   - Current consumption card
   - Monthly cost card
   - Outstanding bills card
   - Efficiency score card
   - Energy consumption chart
   - Quick action buttons

### 2. Energy Monitor
1. Navigate to: **http://localhost:3000/energy**
2. **Expected Elements**:
   - Current consumption display
   - Solar generation display
   - Grid consumption display
   - Energy sources chart
   - Device status indicators
   - Alerts section

### 3. Billing Interface
1. Navigate to: **http://localhost:3000/billing**
2. **Expected Elements**:
   - Total outstanding amount
   - Pending bills count
   - Overdue bills count
   - Bills table with sample data
   - Payment action buttons

## Step 6: API Integration Testing

### Use the Test Page
1. Navigate to: **http://localhost:3000/test-integration**
2. Click "Run Tests"
3. **Expected Results**:
   - All 5 services should show "SUCCESS"
   - Response data should be displayed
   - No authentication errors

### Manual API Testing
```bash
# Test User Service
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/v1/users/profile

# Test Energy Service
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/v1/energy/readings

# Test Device Service
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/v1/devices

# Test Analytics Service
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/v1/analytics

# Test Billing Service
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost:8080/api/v1/bills
```

## Step 7: Test Error Handling

### 1. Invalid Credentials
1. Go to login page
2. Enter invalid email/password
3. **Expected Result**: Error message displayed

### 2. Network Errors
1. Stop backend services: `docker-compose down`
2. Try to access dashboard
3. **Expected Result**: Appropriate error handling

### 3. Token Expiration
1. Manually expire token in browser dev tools
2. Try to access protected pages
3. **Expected Result**: Redirected to login

## Step 8: Performance Testing

### 1. Page Load Times
- Dashboard: Should load within 3 seconds
- Energy Monitor: Should load within 2 seconds
- Billing: Should load within 2 seconds

### 2. API Response Times
```bash
# Test API response times
time curl -H "Authorization: Bearer YOUR_TOKEN" \
          http://localhost:8080/api/v1/users/profile
```

## Troubleshooting

### Common Issues

#### 1. Services Not Starting
```bash
# Check Docker logs
docker-compose logs

# Restart services
docker-compose down
docker-compose up --build -d
```

#### 2. Frontend Connection Issues
- Verify API URL in `frontend/utils/api.ts`
- Check browser console for CORS errors
- Ensure backend services are running

#### 3. Database Connection Issues
```bash
# Check database container
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

#### 4. Authentication Issues
- Clear browser localStorage
- Check JWT token format
- Verify user service is running

### Debug Commands

```bash
# View all running containers
docker ps

# Check service logs
docker-compose logs -f [service-name]

# Restart specific service
docker-compose restart [service-name]

# Check network connectivity
curl -v http://localhost:8080/actuator/health
```

## Expected Test Results

### ✅ Successful Integration
- All 5 backend services respond correctly
- Frontend can authenticate users
- Dashboard displays real data
- Charts render properly
- Navigation works smoothly
- Error handling works correctly

### ❌ Failed Integration
- Services not responding
- Authentication errors
- CORS issues
- Database connection failures
- Frontend not loading data

## Next Steps

After successful integration testing:

1. **Edge Gateway Implementation**: Add IoT device management
2. **End-to-End Testing**: Complete user journey testing
3. **Performance Optimization**: Caching and CDN implementation
4. **Security Testing**: Vulnerability assessment
5. **Deployment**: Production environment setup

## Support

If you encounter issues during testing:

1. Check the troubleshooting section above
2. Review service logs: `docker-compose logs`
3. Verify environment variables
4. Ensure all dependencies are installed
5. Check network connectivity between services 