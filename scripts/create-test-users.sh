#!/bin/bash
set -e

echo "Creating test users for SmartWatts dashboard..."

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 10

# Base URL for API Gateway
BASE_URL="http://localhost:8080"

# Create Household User
echo "Creating Household User..."
HOUSEHOLD_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "household_user",
    "email": "household@smartwatts.com",
    "password": "Household123!",
    "firstName": "John",
    "lastName": "Doe",
    "userType": "HOUSEHOLD",
    "phone": "+234-801-234-5678",
    "address": "123 Lagos Street, Lagos"
  }')

echo "Household user created: $HOUSEHOLD_RESPONSE"

# Create Business User
echo "Creating Business User..."
BUSINESS_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "business_user",
    "email": "business@smartwatts.com",
    "password": "Business123!",
    "firstName": "Jane",
    "lastName": "Smith",
    "userType": "BUSINESS",
    "phone": "+234-802-345-6789",
    "address": "456 Victoria Island, Lagos",
    "companyName": "Smith Enterprises Ltd"
  }')

echo "Business user created: $BUSINESS_RESPONSE"

# Create Enterprise User
echo "Creating Enterprise User..."
ENTERPRISE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/users/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "enterprise_user",
    "email": "enterprise@smartwatts.com",
    "password": "Enterprise123!",
    "firstName": "Michael",
    "lastName": "Johnson",
    "userType": "ENTERPRISE",
    "phone": "+234-803-456-7890",
    "address": "789 Ikoyi, Lagos",
    "companyName": "Johnson Industries",
    "department": "IT Operations"
  }')

echo "Enterprise user created: $ENTERPRISE_RESPONSE"

echo ""
echo "=== SmartWatts Test User Credentials ==="
echo ""
echo "🏠 HOUSEHOLD USER:"
echo "   Username: household_user"
echo "   Password: Household123!"
echo "   Email: household@smartwatts.com"
echo "   Dashboard: http://localhost:3000/dashboard/household"
echo ""
echo "🏢 BUSINESS USER:"
echo "   Username: business_user"
echo "   Password: Business123!"
echo "   Email: business@smartwatts.com"
echo "   Dashboard: http://localhost:3000/dashboard/business"
echo ""
echo "🏭 ENTERPRISE USER:"
echo "   Username: enterprise_user"
echo "   Password: Enterprise123!"
echo "   Email: enterprise@smartwatts.com"
echo "   Dashboard: http://localhost:3000/dashboard/enterprise"
echo ""
echo "🌐 Main Dashboard: http://localhost:3000/dashboard"
echo "🔧 API Gateway: http://localhost:8080"
echo "📊 Spring Boot Admin: http://localhost:9090"
echo ""
echo "All users have been created successfully!"







