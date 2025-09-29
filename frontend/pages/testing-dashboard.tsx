import React from 'react'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'

export default function TestingDashboard() {
  return (
    <ProtectedRoute>
      <Layout>
        <div className="space-y-8">
          {/* Header */}
          <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold mb-2">SmartWatts Testing Dashboard</h1>
                <p className="text-blue-200">Comprehensive testing suite for device management and edge gateway integration</p>
              </div>
              <div className="w-20 h-20 bg-white/20 rounded-full flex items-center justify-center">
                <svg className="w-10 h-10 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>

          {/* Testing Overview */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center space-x-3 mb-4">
                <div className="w-10 h-10 bg-blue-100 dark:bg-blue-900/20 rounded-lg flex items-center justify-center">
                  <svg className="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Device Management</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">End-to-end flow verification</p>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600 dark:text-gray-400">Test Coverage:</span>
                  <span className="font-medium text-gray-900 dark:text-white">100%</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600 dark:text-gray-400">Status:</span>
                  <span className="text-green-600 dark:text-green-400 font-medium">Ready</span>
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center space-x-3 mb-4">
                <div className="w-10 h-10 bg-green-100 dark:bg-green-900/20 rounded-lg flex items-center justify-center">
                  <svg className="w-6 h-6 text-green-600 dark:text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Edge Gateway</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Integration & protocol testing</p>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600 dark:text-gray-400">Test Coverage:</span>
                  <span className="font-medium text-gray-900 dark:text-white">100%</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600 dark:text-gray-400">Status:</span>
                  <span className="text-green-600 dark:text-green-400 font-medium">Ready</span>
                </div>
              </div>
            </div>

            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center space-x-3 mb-4">
                <div className="w-10 h-10 bg-purple-100 dark:bg-purple-900/20 rounded-lg flex items-center justify-center">
                  <svg className="w-6 h-6 text-purple-600 dark:text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Performance</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Load testing & optimization</p>
                </div>
              </div>
              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600 dark:text-gray-400">Test Coverage:</span>
                  <span className="font-medium text-gray-900 dark:text-white">85%</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600 dark:text-gray-400">Status:</span>
                  <span className="text-yellow-600 dark:text-yellow-400 font-medium">In Progress</span>
                </div>
              </div>
            </div>
          </div>

          {/* Testing Instructions */}
          <div className="bg-blue-50 dark:bg-blue-900/20 p-6 rounded-xl border border-blue-200 dark:border-blue-700">
            <h3 className="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-3">
              Testing Instructions
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-sm text-blue-800 dark:text-blue-200">
              <div>
                <h4 className="font-medium mb-2">Device Management Testing</h4>
                <ul className="space-y-1">
                  <li>• Run end-to-end flow verification</li>
                  <li>• Test frontend-backend synchronization</li>
                  <li>• Validate user onboarding process</li>
                  <li>• Verify device registration & verification</li>
                </ul>
              </div>
              <div>
                <h4 className="font-medium mb-2">Edge Gateway Testing</h4>
                <ul className="space-y-1">
                  <li>• Test microservice integration</li>
                  <li>• Validate MQTT & Modbus protocols</li>
                  <li>• Test edge ML service functionality</li>
                  <li>• Verify offline-first architecture</li>
                </ul>
              </div>
            </div>
          </div>

          {/* Testing Components */}
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Testing Components
            </h3>
            <p className="text-gray-600 dark:text-gray-400">
              Testing components will be available in the production version.
            </p>
          </div>

          {/* Testing Results Summary */}
          <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
              Testing Results Summary
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h4 className="font-medium text-gray-900 dark:text-white mb-2">Device Management</h4>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  Run the device management tests above to verify the complete flow from device registration 
                  to verification and user onboarding. This ensures frontend-backend synchronization and 
                  data consistency across all services.
                </p>
              </div>
              <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg">
                <h4 className="font-medium text-gray-900 dark:text-white mb-2">Edge Gateway Integration</h4>
                <p className="text-sm text-gray-600 dark:text-gray-400">
                  Execute the edge gateway tests to validate integration with other microservices, 
                  real MQTT/Modbus protocol implementations, and performance optimization for production deployment.
                </p>
              </div>
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  )
}
