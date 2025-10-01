// SmartWatts Load Testing Script
// Run with: k6 run k6-load-test.js

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('error_rate');
const responseTime = new Trend('response_time');

// Test configuration
export const options = {
  stages: [
    { duration: '2m', target: 10 }, // Ramp up to 10 users
    { duration: '5m', target: 10 }, // Stay at 10 users
    { duration: '2m', target: 20 }, // Ramp up to 20 users
    { duration: '5m', target: 20 }, // Stay at 20 users
    { duration: '2m', target: 50 }, // Ramp up to 50 users
    { duration: '5m', target: 50 }, // Stay at 50 users
    { duration: '2m', target: 0 },  // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% of requests must complete below 2s
    http_req_failed: ['rate<0.1'],     // Error rate must be below 10%
    error_rate: ['rate<0.1'],          // Custom error rate below 10%
  },
};

// Base URL - update for your environment
const BASE_URL = 'https://mysmartwatts.com';
// For local testing: const BASE_URL = 'http://localhost';

// Test data
const testUser = {
  email: 'admin@mysmartwatts.com',
  password: 'password'
};

let authToken = '';

export function setup() {
  // Login and get auth token
  const loginPayload = JSON.stringify({
    usernameOrEmail: testUser.email,
    password: testUser.password
  });

  const loginResponse = http.post(`${BASE_URL}/api/proxy?service=user&path=/users/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(loginResponse, {
    'login successful': (r) => r.status === 200,
    'login response time < 1s': (r) => r.timings.duration < 1000,
  });

  if (loginResponse.status === 200) {
    const loginData = JSON.parse(loginResponse.body);
    return loginData.accessToken;
  }
  
  return null;
}

export default function(data) {
  authToken = data;
  
  if (!authToken) {
    console.error('Failed to get auth token, skipping test');
    return;
  }

  const headers = {
    'Authorization': `Bearer ${authToken}`,
    'Content-Type': 'application/json',
  };

  // Test 1: Dashboard Stats
  const dashboardStatsResponse = http.get(`${BASE_URL}/api/proxy?service=analytics&path=/analytics/dashboard-stats`, { headers });
  check(dashboardStatsResponse, {
    'dashboard stats status 200': (r) => r.status === 200,
    'dashboard stats response time < 500ms': (r) => r.timings.duration < 500,
  });
  errorRate.add(dashboardStatsResponse.status !== 200);
  responseTime.add(dashboardStatsResponse.timings.duration);

  // Test 2: Energy Readings
  const energyReadingsResponse = http.get(`${BASE_URL}/api/proxy?service=energy&path=/energy/readings`, { headers });
  check(energyReadingsResponse, {
    'energy readings status 200': (r) => r.status === 200,
    'energy readings response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(energyReadingsResponse.status !== 200);
  responseTime.add(energyReadingsResponse.timings.duration);

  // Test 3: Power Quality
  const powerQualityResponse = http.get(`${BASE_URL}/api/proxy?service=energy&path=/energy/power-quality`, { headers });
  check(powerQualityResponse, {
    'power quality status 200': (r) => r.status === 200,
    'power quality response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(powerQualityResponse.status !== 200);
  responseTime.add(powerQualityResponse.timings.duration);

  // Test 4: Cost Optimizations
  const costOptimizationsResponse = http.get(`${BASE_URL}/api/proxy?service=analytics&path=/analytics/cost-optimizations`, { headers });
  check(costOptimizationsResponse, {
    'cost optimizations status 200': (r) => r.status === 200,
    'cost optimizations response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(costOptimizationsResponse.status !== 200);
  responseTime.add(costOptimizationsResponse.timings.duration);

  // Test 5: User Profile
  const userProfileResponse = http.get(`${BASE_URL}/api/proxy?service=user&path=/users/profile`, { headers });
  check(userProfileResponse, {
    'user profile status 200': (r) => r.status === 200,
    'user profile response time < 500ms': (r) => r.timings.duration < 500,
  });
  errorRate.add(userProfileResponse.status !== 200);
  responseTime.add(userProfileResponse.timings.duration);

  // Test 6: Load Profile
  const loadProfileResponse = http.get(`${BASE_URL}/api/proxy?service=analytics&path=/analytics/load-profile`, { headers });
  check(loadProfileResponse, {
    'load profile status 200': (r) => r.status === 200,
    'load profile response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(loadProfileResponse.status !== 200);
  responseTime.add(loadProfileResponse.timings.duration);

  // Test 7: Device Consumption
  const deviceConsumptionResponse = http.get(`${BASE_URL}/api/proxy?service=analytics&path=/analytics/device-consumption`, { headers });
  check(deviceConsumptionResponse, {
    'device consumption status 200': (r) => r.status === 200,
    'device consumption response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(deviceConsumptionResponse.status !== 200);
  responseTime.add(deviceConsumptionResponse.timings.duration);

  // Test 8: Weather Impact
  const weatherImpactResponse = http.get(`${BASE_URL}/api/proxy?service=analytics&path=/analytics/weather-impact`, { headers });
  check(weatherImpactResponse, {
    'weather impact status 200': (r) => r.status === 200,
    'weather impact response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(weatherImpactResponse.status !== 200);
  responseTime.add(weatherImpactResponse.timings.duration);

  // Test 9: Time of Use Analysis
  const timeOfUseResponse = http.get(`${BASE_URL}/api/proxy?service=analytics&path=/analytics/time-of-use`, { headers });
  check(timeOfUseResponse, {
    'time of use status 200': (r) => r.status === 200,
    'time of use response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(timeOfUseResponse.status !== 200);
  responseTime.add(timeOfUseResponse.timings.duration);

  // Test 10: Carbon Footprint
  const carbonFootprintResponse = http.get(`${BASE_URL}/api/proxy?service=analytics&path=/analytics/carbon-footprint`, { headers });
  check(carbonFootprintResponse, {
    'carbon footprint status 200': (r) => r.status === 200,
    'carbon footprint response time < 1s': (r) => r.timings.duration < 1000,
  });
  errorRate.add(carbonFootprintResponse.status !== 200);
  responseTime.add(carbonFootprintResponse.timings.duration);

  // Wait between requests
  sleep(1);
}

export function teardown(data) {
  console.log('Load test completed');
}
