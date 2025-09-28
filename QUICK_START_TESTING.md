# 🚀 SmartWatts Integration Testing - Quick Start

## ⚡ Quick Test Setup

### 1. Start Backend Services
```bash
# Run the automated test script
./test-integration.sh
```

**Expected Output:**
```
🚀 Starting SmartWatts Backend Services for Integration Testing
===============================================================
📦 Building and starting backend services...
⏳ Waiting for services to start...
🔍 Checking service health...
✅ user-service is healthy
✅ energy-service is healthy
✅ device-service is healthy
✅ analytics-service is healthy
✅ billing-service is healthy
```

### 2. Start Frontend
```bash
cd frontend
npm run dev
```

**Frontend will be available at:** http://localhost:3000

### 3. Test Integration
Navigate to: **http://localhost:3000/test-integration**

Click "Run Tests" to verify all services are connected.

## 🧪 Quick Test Checklist

### ✅ Authentication Test
1. Go to: http://localhost:3000/register
2. Create account with test data
3. Verify redirect to dashboard

### ✅ Dashboard Test
1. Go to: http://localhost:3000/dashboard
2. Verify all cards display data
3. Check energy consumption chart

### ✅ Energy Monitor Test
1. Go to: http://localhost:3000/energy
2. Verify energy sources chart
3. Check device status indicators

### ✅ Billing Test
1. Go to: http://localhost:3000/billing
2. Verify bills table displays
3. Check payment action buttons

## 🔧 Troubleshooting

### Services Not Starting?
```bash
# Check Docker status
docker ps

# Restart services
docker-compose down
docker-compose up --build -d
```

### Frontend Connection Issues?
```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# Verify API URL in frontend/utils/api.ts
```

### Database Issues?
```bash
# Check database logs
docker-compose logs postgres

# Restart database
docker-compose restart postgres
```

## 📊 Expected Results

### ✅ Success Indicators
- All 5 backend services show "SUCCESS" in test page
- Dashboard loads with real data
- Charts render properly
- Authentication works
- Navigation is smooth

### ❌ Failure Indicators
- Services show "ERROR" in test page
- Dashboard shows loading forever
- Charts don't render
- Authentication fails
- Navigation errors

## 🎯 Next Steps

After successful testing:
1. **Edge Gateway**: Implement IoT device management
2. **Performance**: Optimize loading times
3. **Security**: Add vulnerability testing
4. **Deployment**: Prepare for production

## 📞 Support

If tests fail:
1. Check the troubleshooting section above
2. Review `docs/integration-testing.md` for detailed guide
3. Check service logs: `docker-compose logs`
4. Verify all ports are available (8080-8085, 3000)

---

**Happy Testing! 🎉** 