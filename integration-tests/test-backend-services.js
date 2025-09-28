/**
 * SmartWatts Backend Services Integration Tests
 * Phase 1: Integration Testing
 */

const axios = require('axios');

class BackendServicesTester {
    constructor() {
        this.baseUrls = {
            analytics: 'http://localhost:8084',
            device: 'http://localhost:8083',
            user: 'http://localhost:8081',
            energy: 'http://localhost:8082',
            billing: 'http://localhost:8085',
            notification: 'http://localhost:8086',
            edgeGateway: 'http://localhost:8088'
        };
        
        this.testResults = {
            passed: 0,
            failed: 0,
            total: 0,
            details: []
        };
    }

    async runTest(testName, testFunction) {
        this.testResults.total++;
        try {
            console.log(`\n🧪 Running test: ${testName}`);
            await testFunction();
            this.testResults.passed++;
            this.testResults.details.push({ name: testName, status: 'PASSED' });
            console.log(`✅ ${testName} - PASSED`);
        } catch (error) {
            this.testResults.failed++;
            this.testResults.details.push({ name: testName, status: 'FAILED', error: error.message });
            console.log(`❌ ${testName} - FAILED: ${error.message}`);
        }
    }

    // Test 1: Service Discovery
    async testServiceDiscovery() {
        const response = await axios.get('http://localhost:8761/eureka/apps');
        if (response.status !== 200) {
            throw new Error('Service discovery not responding');
        }
    }

    // Test 2: Analytics Service Health
    async testAnalyticsServiceHealth() {
        try {
            const response = await axios.get(`${this.baseUrls.analytics}/actuator/health`);
            if (response.data.status !== 'UP') {
                throw new Error(`Analytics service status: ${response.data.status}`);
            }
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                throw new Error('Analytics service not running');
            }
            throw error;
        }
    }

    // Test 3: AI Appliance Recognition API
    async testApplianceRecognitionAPI() {
        const response = await axios.get(`${this.baseUrls.analytics}/api/v1/appliances/detection`);
        if (response.status !== 200) {
            throw new Error('Appliance recognition API not responding');
        }
    }

    // Test 4: Circuit Management API
    async testCircuitManagementAPI() {
        const response = await axios.get(`${this.baseUrls.device}/api/v1/circuits/tree`);
        if (response.status !== 200) {
            throw new Error('Circuit management API not responding');
        }
    }

    // Test 5: Solar Panel Monitoring API
    async testSolarPanelMonitoringAPI() {
        const response = await axios.get(`${this.baseUrls.analytics}/api/v1/solar/array-map`);
        if (response.status !== 200) {
            throw new Error('Solar panel monitoring API not responding');
        }
    }

    // Test 6: Community Benchmarking API
    async testCommunityBenchmarkingAPI() {
        const response = await axios.get(`${this.baseUrls.analytics}/api/v1/community/benchmark/lagos`);
        if (response.status !== 200) {
            throw new Error('Community benchmarking API not responding');
        }
    }

    // Test 7: Device Service Health
    async testDeviceServiceHealth() {
        try {
            const response = await axios.get(`${this.baseUrls.device}/actuator/health`);
            if (response.data.status !== 'UP') {
                throw new Error(`Device service status: ${response.data.status}`);
            }
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                throw new Error('Device service not running');
            }
            throw error;
        }
    }

    // Test 8: User Service Health
    async testUserServiceHealth() {
        try {
            const response = await axios.get(`${this.baseUrls.user}/actuator/health`);
            if (response.data.status !== 'UP') {
                throw new Error(`User service status: ${response.data.status}`);
            }
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                throw new Error('User service not running');
            }
            throw error;
        }
    }

    // Test 9: Energy Service Health
    async testEnergyServiceHealth() {
        try {
            const response = await axios.get(`${this.baseUrls.energy}/actuator/health`);
            if (response.data.status !== 'UP') {
                throw new Error(`Energy service status: ${response.data.status}`);
            }
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                throw new Error('Energy service not running');
            }
            throw error;
        }
    }

    // Test 10: Billing Service Health
    async testBillingServiceHealth() {
        try {
            const response = await axios.get(`${this.baseUrls.billing}/actuator/health`);
            if (response.data.status !== 'UP') {
                throw new Error(`Billing service status: ${response.data.status}`);
            }
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                throw new Error('Billing service not running');
            }
            throw error;
        }
    }

    // Test 11: Notification Service Health
    async testNotificationServiceHealth() {
        try {
            const response = await axios.get(`${this.baseUrls.notification}/actuator/health`);
            if (response.data.status !== 'UP') {
                throw new Error(`Notification service status: ${response.data.status}`);
            }
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                throw new Error('Notification service not running');
            }
            throw error;
        }
    }

    // Test 12: Edge Gateway Health
    async testEdgeGatewayHealth() {
        try {
            const response = await axios.get(`${this.baseUrls.edgeGateway}/actuator/health`);
            if (response.data.status !== 'UP') {
                throw new Error(`Edge Gateway status: ${response.data.status}`);
            }
        } catch (error) {
            if (error.code === 'ECONNREFUSED') {
                throw new Error('Edge Gateway not running');
            }
            throw error;
        }
    }

    // Test 13: Database Connectivity
    async testDatabaseConnectivity() {
        // Test PostgreSQL connection
        const response = await axios.get('http://localhost:5432');
        if (response.status !== 200) {
            throw new Error('PostgreSQL not accessible');
        }
    }

    // Test 14: Redis Connectivity
    async testRedisConnectivity() {
        // Test Redis connection
        const response = await axios.get('http://localhost:6379');
        if (response.status !== 200) {
            throw new Error('Redis not accessible');
        }
    }

    // Test 15: MQTT Broker Connectivity
    async testMQTTBrokerConnectivity() {
        // Test MQTT broker connection
        const response = await axios.get('http://localhost:1883');
        if (response.status !== 200) {
            throw new Error('MQTT broker not accessible');
        }
    }

    async runAllTests() {
        console.log('🚀 Starting SmartWatts Backend Services Integration Tests...');
        
        await this.runTest('Service Discovery', () => this.testServiceDiscovery());
        await this.runTest('Analytics Service Health', () => this.testAnalyticsServiceHealth());
        await this.runTest('AI Appliance Recognition API', () => this.testApplianceRecognitionAPI());
        await this.runTest('Circuit Management API', () => this.testCircuitManagementAPI());
        await this.runTest('Solar Panel Monitoring API', () => this.testSolarPanelMonitoringAPI());
        await this.runTest('Community Benchmarking API', () => this.testCommunityBenchmarkingAPI());
        await this.runTest('Device Service Health', () => this.testDeviceServiceHealth());
        await this.runTest('User Service Health', () => this.testUserServiceHealth());
        await this.runTest('Energy Service Health', () => this.testEnergyServiceHealth());
        await this.runTest('Billing Service Health', () => this.testBillingServiceHealth());
        await this.runTest('Notification Service Health', () => this.testNotificationServiceHealth());
        await this.runTest('Edge Gateway Health', () => this.testEdgeGatewayHealth());
        await this.runTest('Database Connectivity', () => this.testDatabaseConnectivity());
        await this.runTest('Redis Connectivity', () => this.testRedisConnectivity());
        await this.runTest('MQTT Broker Connectivity', () => this.testMQTTBrokerConnectivity());
        
        this.printResults();
    }

    printResults() {
        console.log('\n📊 Backend Services Test Results Summary:');
        console.log('==========================================');
        console.log(`Total Tests: ${this.testResults.total}`);
        console.log(`Passed: ${this.testResults.passed}`);
        console.log(`Failed: ${this.testResults.failed}`);
        console.log(`Success Rate: ${((this.testResults.passed / this.testResults.total) * 100).toFixed(1)}%`);
        
        console.log('\n📋 Detailed Results:');
        this.testResults.details.forEach(test => {
            const status = test.status === 'PASSED' ? '✅' : '❌';
            console.log(`${status} ${test.name}`);
            if (test.error) {
                console.log(`   Error: ${test.error}`);
            }
        });
        
        console.log('\n🎯 Phase 1 Backend Integration Status:');
        if (this.testResults.failed === 0) {
            console.log('✅ All backend services are operational! Consumer-grade features are fully functional.');
        } else if (this.testResults.passed > this.testResults.failed) {
            console.log('⚠️  Most backend services are working. Some issues need attention.');
        } else {
            console.log('❌ Multiple backend service failures. Database connectivity issues need to be resolved.');
        }
    }
}

// Run the tests
async function main() {
    const tester = new BackendServicesTester();
    await tester.runAllTests();
}

if (require.main === module) {
    main().catch(console.error);
}

module.exports = BackendServicesTester;
