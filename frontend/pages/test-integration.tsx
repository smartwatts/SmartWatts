import React, { useState, useEffect } from 'react'
import { useAuth } from '../hooks/useAuth'
import Layout from '../components/Layout'
import { userApi, energyApi, deviceApi, analyticsApi, billingApi } from '../utils/api'
import { CheckCircleIcon, XCircleIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline'

interface TestResult {
  service: string
  endpoint: string
  status: 'success' | 'error' | 'pending'
  message: string
  data?: any
}

export default function TestIntegration() {
  const { user } = useAuth()
  const [testResults, setTestResults] = useState<TestResult[]>([])
  const [isRunning, setIsRunning] = useState(false)

  const runTests = async () => {
    setIsRunning(true)
    const results: TestResult[] = []

    // Test User Service
    try {
      // First test a public endpoint (registration availability)
      const registerResponse = await userApi.register({
        username: 'testuser',
        email: 'test@example.com',
        password: 'password123',
        firstName: 'Test',
        lastName: 'User',
        phoneNumber: '+2348012345678'
      })
      results.push({
        service: 'User Service',
        endpoint: 'POST /users/register',
        status: 'success',
        message: 'User registration endpoint is working',
        data: registerResponse.data,
      })
    } catch (error: any) {
      // If registration fails, it might be because user already exists, which is fine
      if (error.response?.status === 400 && error.response?.data?.message?.includes('already exists')) {
        results.push({
          service: 'User Service',
          endpoint: 'POST /users/register',
          status: 'success',
          message: 'User service is working (user already exists)',
        })
      } else {
        results.push({
          service: 'User Service',
          endpoint: 'POST /users/register',
          status: 'error',
          message: error.response?.data?.message || 'Failed to test user service',
        })
      }
    }

    // Test Profile endpoint (requires authentication)
    try {
      const profileResponse = await userApi.getProfile()
      results.push({
        service: 'User Service',
        endpoint: 'GET /users/profile',
        status: 'success',
        message: 'Profile retrieved successfully',
        data: profileResponse.data,
      })
    } catch (error: any) {
      if (error.response?.status === 401) {
        results.push({
          service: 'User Service',
          endpoint: 'GET /users/profile',
          status: 'error',
          message: 'Profile endpoint requires authentication. Please login first.',
        })
      } else {
        results.push({
          service: 'User Service',
          endpoint: 'GET /users/profile',
          status: 'error',
          message: error.response?.data?.message || 'Failed to get profile',
        })
      }
    }

    // Test Energy Service
    try {
      const energyResponse = await energyApi.getReadings({ limit: 5 })
      results.push({
        service: 'Energy Service',
        endpoint: 'GET /energy/readings',
        status: 'success',
        message: 'Energy readings retrieved successfully',
        data: energyResponse.data,
      })
    } catch (error: any) {
      results.push({
        service: 'Energy Service',
        endpoint: 'GET /energy/readings',
        status: 'error',
        message: error.response?.data?.message || 'Failed to get energy readings',
      })
    }

    // Test Device Service
    try {
      const deviceResponse = await deviceApi.getDevices({ limit: 5 })
      results.push({
        service: 'Device Service',
        endpoint: 'GET /devices',
        status: 'success',
        message: 'Devices retrieved successfully',
        data: deviceResponse.data,
      })
    } catch (error: any) {
      results.push({
        service: 'Device Service',
        endpoint: 'GET /devices',
        status: 'error',
        message: error.response?.data?.message || 'Failed to get devices',
      })
    }

    // Test Analytics Service
    try {
      const analyticsResponse = await analyticsApi.getAnalytics({ limit: 5 })
      results.push({
        service: 'Analytics Service',
        endpoint: 'GET /analytics',
        status: 'success',
        message: 'Analytics retrieved successfully',
        data: analyticsResponse.data,
      })
    } catch (error: any) {
      if (error.code === 'ECONNREFUSED') {
        results.push({
          service: 'Analytics Service',
          endpoint: 'GET /analytics',
          status: 'error',
          message: 'Analytics service is not running (port 8084). Service needs to be restarted.',
        })
      } else {
        results.push({
          service: 'Analytics Service',
          endpoint: 'GET /analytics',
          status: 'error',
          message: error.response?.data?.message || 'Failed to get analytics',
        })
      }
    }

    // Test Billing Service
    try {
      const billingResponse = await billingApi.getBills({ limit: 5 })
      results.push({
        service: 'Billing Service',
        endpoint: 'GET /bills',
        status: 'success',
        message: 'Bills retrieved successfully',
        data: billingResponse.data,
      })
    } catch (error: any) {
      if (error.code === 'ECONNREFUSED') {
        results.push({
          service: 'Billing Service',
          endpoint: 'GET /bills',
          status: 'error',
          message: 'Billing service is not running (port 8085). Service needs to be restarted.',
        })
      } else {
        results.push({
          service: 'Billing Service',
          endpoint: 'GET /bills',
          status: 'error',
          message: error.response?.data?.message || 'Failed to get bills',
        })
      }
    }

    setTestResults(results)
    setIsRunning(false)
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'success':
        return <CheckCircleIcon className="h-5 w-5 text-green-600" />
      case 'error':
        return <XCircleIcon className="h-5 w-5 text-red-600" />
      case 'pending':
        return <ExclamationTriangleIcon className="h-5 w-5 text-yellow-600" />
      default:
        return null
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'success':
        return 'bg-green-50 border-green-200'
      case 'error':
        return 'bg-red-50 border-red-200'
      case 'pending':
        return 'bg-yellow-50 border-yellow-200'
      default:
        return 'bg-gray-50 border-gray-200'
    }
  }

  return (
    <Layout>
      <div className="space-y-6">
        {/* Header */}
        <div className="bg-white rounded-lg shadow p-6">
          <h1 className="text-2xl font-bold text-gray-900">Integration Testing</h1>
          <p className="text-gray-600 mt-2">
            Test the connection between frontend and backend services
          </p>
        </div>

        {/* Test Controls */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-medium text-gray-900">Service Tests</h3>
              <p className="text-sm text-gray-600">
                Verify all backend services are accessible and responding correctly
              </p>
            </div>
            <button
              onClick={runTests}
              disabled={isRunning}
              className="btn btn-primary"
            >
              {isRunning ? 'Running Tests...' : 'Run Tests'}
            </button>
          </div>
        </div>

        {/* Test Results */}
        {testResults.length > 0 && (
          <div className="space-y-4">
            {testResults.map((result, index) => (
              <div
                key={index}
                className={`border rounded-lg p-4 ${getStatusColor(result.status)}`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    {getStatusIcon(result.status)}
                    <div>
                      <h4 className="text-sm font-medium text-gray-900">
                        {result.service}
                      </h4>
                      <p className="text-xs text-gray-500">{result.endpoint}</p>
                    </div>
                  </div>
                  <span
                    className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      result.status === 'success'
                        ? 'bg-green-100 text-green-800'
                        : result.status === 'error'
                        ? 'bg-red-100 text-red-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }`}
                  >
                    {result.status.toUpperCase()}
                  </span>
                </div>
                <p className="mt-2 text-sm text-gray-700">{result.message}</p>
                {result.data && (
                  <details className="mt-2">
                    <summary className="text-sm font-medium text-gray-700 cursor-pointer">
                      View Response Data
                    </summary>
                    <pre className="mt-2 text-xs bg-gray-100 p-2 rounded overflow-auto">
                      {JSON.stringify(result.data, null, 2)}
                    </pre>
                  </details>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Summary */}
        {testResults.length > 0 && (
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">Test Summary</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="text-center">
                <div className="text-2xl font-bold text-green-600">
                  {testResults.filter(r => r.status === 'success').length}
                </div>
                <div className="text-sm text-gray-600">Successful</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-red-600">
                  {testResults.filter(r => r.status === 'error').length}
                </div>
                <div className="text-sm text-gray-600">Failed</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-bold text-gray-600">
                  {testResults.length}
                </div>
                <div className="text-sm text-gray-600">Total</div>
              </div>
            </div>
          </div>
        )}

        {/* Connection Info */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Connection Information</h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">API Base URL:</span>
              <span className="font-mono">
                {process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">User Status:</span>
              <span className={user ? 'text-green-600' : 'text-red-600'}>
                {user ? 'Authenticated' : 'Not Authenticated'}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">User Email:</span>
              <span>{user?.email || 'N/A'}</span>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
} 