import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const apiDuration = new Trend('api_duration');
const requestCount = new Counter('requests');

// Test configuration for 1000+ concurrent users
export const options = {
  stages: [
    { duration: '2m', target: 100 },   // Ramp up to 100 users
    { duration: '3m', target: 500 },   // Ramp up to 500 users
    { duration: '5m', target: 1000 },  // Ramp up to 1000 users
    { duration: '10m', target: 1000 }, // Stay at 1000 users
    { duration: '5m', target: 1500 },  // Peak load test
    { duration: '5m', target: 0 },     // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200', 'p(99)<500'], // 95% < 200ms, 99% < 500ms
    http_req_failed: ['rate<0.01'], // Error rate < 1%
    errors: ['rate<0.05'], // Custom error rate < 5%
  },
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8080';

// Test scenarios
export default function () {
  const scenarios = [
    testEnergyReadings,
    testDashboardStats,
    testDeviceManagement,
    testBillingOperations,
    testAnalytics,
    testNigerianFeatures,
  ];
  
  // Randomly select scenario
  const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
  scenario();
  
  sleep(1);
}

function testEnergyReadings() {
  const userId = `user-${Math.floor(Math.random() * 1000)}`;
  const res = http.get(`${BASE_URL}/api/v1/energy/readings/user/${userId}`);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });
  
  errorRate.add(res.status !== 200);
  apiDuration.add(res.timings.duration);
  requestCount.add(1);
}

function testDashboardStats() {
  const res = http.get(`${BASE_URL}/api/v1/analytics/dashboard-stats`);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'has required fields': (r) => {
      const body = JSON.parse(r.body);
      return body.totalConsumption !== undefined;
    },
  });
  
  errorRate.add(res.status !== 200);
  apiDuration.add(res.timings.duration);
  requestCount.add(1);
}

function testDeviceManagement() {
  const userId = `user-${Math.floor(Math.random() * 1000)}`;
  const res = http.get(`${BASE_URL}/api/v1/devices/user/${userId}`);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  
  errorRate.add(res.status !== 200);
  requestCount.add(1);
}

function testBillingOperations() {
  const userId = `user-${Math.floor(Math.random() * 1000)}`;
  const res = http.get(`${BASE_URL}/api/v1/billing/users/${userId}/bills`);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  
  errorRate.add(res.status !== 200);
  requestCount.add(1);
}

function testAnalytics() {
  const res = http.get(`${BASE_URL}/api/v1/analytics/cost-optimizations`);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  
  errorRate.add(res.status !== 200);
  requestCount.add(1);
}

function testNigerianFeatures() {
  const userId = `user-${Math.floor(Math.random() * 1000)}`;
  const endpoints = [
    `/api/v1/energy/source-breakdown/${userId}`,
    `/api/v1/billing/prepaid-tokens/${userId}`,
    `/api/v1/devices/generator/${userId}/health`,
    `/api/v1/energy/disco-status/${userId}`,
  ];
  
  const endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(`${BASE_URL}${endpoint}`);
  
  check(res, {
    'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
  });
  
  errorRate.add(res.status >= 500);
  requestCount.add(1);
}









