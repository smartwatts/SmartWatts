import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import { userApi, energyApi, deviceApi } from '../utils/api'

export default function DebugApi() {
  const router = useRouter()
  
  // Gate this page from production - only allow in development
  useEffect(() => {
    if (process.env.NODE_ENV === 'production') {
      router.replace('/dashboard')
    }
  }, [router])
  
  // Don't render in production
  if (process.env.NODE_ENV === 'production') {
    return null
  }
  const [results, setResults] = useState<any[]>([])
  const [isLoading, setIsLoading] = useState(false)

  const testUserService = async () => {
    try {
      console.log('Testing user service registration...')
      const response = await userApi.register({
        username: 'debuguser',
        email: 'debug@example.com',
        password: 'password123',
        firstName: 'Debug',
        lastName: 'User',
        phoneNumber: '+2348012345684'
      })
      console.log('User service response:', response)
      setResults(prev => [...prev, { service: 'User Service', status: 'success', data: response.data }])
    } catch (error: any) {
      console.error('User service error:', error)
      setResults(prev => [...prev, { 
        service: 'User Service', 
        status: 'error', 
        error: error.message,
        response: error.response?.data,
        statusCode: error.response?.status
      }])
    }
  }

  const testEnergyService = async () => {
    try {
      console.log('Testing energy service...')
      const response = await energyApi.getReadings({ limit: 5 })
      console.log('Energy service response:', response)
      setResults(prev => [...prev, { service: 'Energy Service', status: 'success', data: response.data }])
    } catch (error: any) {
      console.error('Energy service error:', error)
      setResults(prev => [...prev, { 
        service: 'Energy Service', 
        status: 'error', 
        error: error.message,
        response: error.response?.data,
        statusCode: error.response?.status
      }])
    }
  }

  const testDeviceService = async () => {
    try {
      console.log('Testing device service...')
      const response = await deviceApi.getDevices({ limit: 5 })
      console.log('Device service response:', response)
      setResults(prev => [...prev, { service: 'Device Service', status: 'success', data: response.data }])
    } catch (error: any) {
      console.error('Device service error:', error)
      setResults(prev => [...prev, { 
        service: 'Device Service', 
        status: 'error', 
        error: error.message,
        response: error.response?.data,
        statusCode: error.response?.status
      }])
    }
  }

  const runAllTests = async () => {
    setIsLoading(true)
    setResults([])
    
    await testUserService()
    await testEnergyService()
    await testDeviceService()
    
    setIsLoading(false)
  }

  return (
    <Layout>
      <div className="space-y-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h1 className="text-2xl font-bold text-gray-900">API Debug Page</h1>
          <p className="text-gray-600 mt-2">
            Debug API connections and see detailed error information
          </p>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-medium text-gray-900">API Tests</h3>
              <p className="text-sm text-gray-600">
                Test each service individually and see detailed results
              </p>
            </div>
            <button
              onClick={runAllTests}
              disabled={isLoading}
              className="btn btn-primary"
            >
              {isLoading ? 'Running Tests...' : 'Run All Tests'}
            </button>
          </div>
        </div>

        {results.length > 0 && (
          <div className="space-y-4">
            {results.map((result, index) => (
              <div
                key={index}
                className={`border rounded-lg p-4 ${
                  result.status === 'success' 
                    ? 'bg-green-50 border-green-200' 
                    : 'bg-red-50 border-red-200'
                }`}
              >
                <div className="flex items-center justify-between">
                  <h4 className="text-sm font-medium text-gray-900">
                    {result.service}
                  </h4>
                  <span
                    className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      result.status === 'success'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-red-100 text-red-800'
                    }`}
                  >
                    {result.status.toUpperCase()}
                  </span>
                </div>
                
                {result.status === 'success' ? (
                  <div className="mt-2">
                    <p className="text-sm text-gray-700">Success!</p>
                    <details className="mt-2">
                      <summary className="text-sm font-medium text-gray-700 cursor-pointer">
                        View Response Data
                      </summary>
                      <pre className="mt-2 text-xs bg-gray-100 p-2 rounded overflow-auto">
                        {JSON.stringify(result.data, null, 2)}
                      </pre>
                    </details>
                  </div>
                ) : (
                  <div className="mt-2">
                    <p className="text-sm text-red-700">Error: {result.error}</p>
                    {result.statusCode && (
                      <p className="text-sm text-red-600">Status Code: {result.statusCode}</p>
                    )}
                    {result.response && (
                      <details className="mt-2">
                        <summary className="text-sm font-medium text-gray-700 cursor-pointer">
                          View Error Response
                        </summary>
                        <pre className="mt-2 text-xs bg-gray-100 p-2 rounded overflow-auto">
                          {JSON.stringify(result.response, null, 2)}
                        </pre>
                      </details>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  )
} 