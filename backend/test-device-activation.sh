#!/bin/bash

echo "🧪 Testing SmartWatts Device Verification & Activation Service"
echo "================================================================"

# Test 1: Residential Device Activation (should get 12 months)
echo ""
echo "📱 Test 1: Residential Device Activation (Expected: 12 months validity)"
echo "----------------------------------------------------------------------"
RESPONSE=$(curl -s -X POST http://localhost:8091/api/device-verification/activate \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "TEST-RES-001",
    "deviceType": "SMART_METER",
    "hardwareId": "HW-RES-001",
    "customerType": "RESIDENTIAL"
  }')

echo "Response: $RESPONSE"
echo ""

# Test 2: Commercial Device Activation (should get 3 months initially)
echo "🏢 Test 2: Commercial Device Activation (Expected: 3 months validity initially)"
echo "---------------------------------------------------------------------------"
RESPONSE=$(curl -s -X POST http://localhost:8091/api/device-verification/activate \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "TEST-COM-001",
    "deviceType": "SMART_METER",
    "hardwareId": "HW-COM-001",
    "customerType": "COMMERCIAL"
  }')

echo "Response: $RESPONSE"
echo ""

# Test 3: Device Validation
echo "🔍 Test 3: Device Validation"
echo "-----------------------------"
RESPONSE=$(curl -s -X POST "http://localhost:8091/api/device-verification/validate?deviceId=TEST-RES-001&token=test-token")
echo "Response: $RESPONSE"
echo ""

echo "✅ Testing Complete!"
