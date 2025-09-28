#!/bin/bash

echo "Testing Complete Appliance Monitoring Integration..."
echo "=================================================="

# Test backend services
echo "1. Testing Backend Services..."
echo "   - Appliance Monitoring Service: $(curl -s http://localhost:8092/api/v1/appliance-monitoring/health | jq -r '.status' 2>/dev/null || echo 'Not accessible')"
echo "   - Feature Flag Service: $(curl -s http://localhost:8090/api/feature-flags/features | jq 'length' 2>/dev/null || echo 'Not accessible') features available"
echo "   - User Service: $(curl -s http://localhost:8081/api/v1/users/health 2>/dev/null | jq -r '.status' 2>/dev/null || echo 'Not accessible')"

# Test feature flag
echo -e "\n2. Testing Feature Flag Integration..."
APPLIANCE_FLAG=$(curl -s http://localhost:8090/api/feature-flags/features | jq '.[] | select(.featureKey == "APPLIANCE_MONITORING") | .featureName' 2>/dev/null)
if [ "$APPLIANCE_FLAG" != "" ]; then
    echo "   ✅ APPLIANCE_MONITORING feature flag is available"
else
    echo "   ❌ APPLIANCE_MONITORING feature flag is missing"
fi

# Test frontend proxy
echo -e "\n3. Testing Frontend Proxy..."
FRONTEND_HEALTH=$(curl -s "http://localhost:3001/api/proxy?service=appliance-monitoring&path=/health" | jq -r '.status' 2>/dev/null)
if [ "$FRONTEND_HEALTH" = "UP" ]; then
    echo "   ✅ Frontend proxy to appliance monitoring service is working"
else
    echo "   ❌ Frontend proxy to appliance monitoring service is not working"
fi

# Test appliance types
echo -e "\n4. Testing Appliance Types API..."
APPLIANCE_TYPES=$(curl -s "http://localhost:3001/api/proxy?service=appliance-monitoring&path=/appliance-types" | jq 'length' 2>/dev/null)
if [ "$APPLIANCE_TYPES" -gt 0 ]; then
    echo "   ✅ Appliance types API is working ($APPLIANCE_TYPES types available)"
else
    echo "   ❌ Appliance types API is not working"
fi

# Test frontend page accessibility
echo -e "\n5. Testing Frontend Page..."
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3001/appliance-monitoring)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "   ✅ Appliance monitoring page is accessible"
else
    echo "   ❌ Appliance monitoring page is not accessible (HTTP $FRONTEND_STATUS)"
fi

echo -e "\n=================================================="
echo "Integration Test Complete!"
echo ""
echo "Next Steps:"
echo "1. Visit http://localhost:3001/appliance-monitoring"
echo "2. Login with a PREMIUM or ENTERPRISE account to access the feature"
echo "3. Test adding appliances and viewing monitoring data"
