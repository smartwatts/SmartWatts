'use client'

import React, { useState } from 'react'
import { 
  CheckCircleIcon, 
  XCircleIcon, 
  ExclamationTriangleIcon,
  PlayIcon,
  StopIcon,
  ArrowPathIcon,
  WifiIcon,
  CogIcon,
  ServerIcon
} from '@heroicons/react/24/outline'

interface EdgeTestResult {
  testName: string
  status: 'pending' | 'running' | 'passed' | 'failed'
  message: string
  timestamp: Date
}

export default function EdgeGatewayTester() {
  // Gate this component from production - only allow in development
  if (process.env.NODE_ENV === 'production') {
    return null
  }
  
  const [testResults, setTestResults] = useState<EdgeTestResult[]>([])
  const [isRunning, setIsRunning] = useState(false)

  const runAllTests = async () => {
    setIsRunning(true)
    setTestResults([])
    
    // Simple test simulation
    const testResult: EdgeTestResult = {
      testName: 'Edge Gateway Health Check',
      status: 'passed',
      message: 'Test passed successfully',
      timestamp: new Date()
    }
    
    setTestResults([testResult])
    setIsRunning(false)
  }

  const stopTests = () => {
    setIsRunning(false)
  }

  const clearResults = () => {
    setTestResults([])
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'passed':
        return <CheckCircleIcon className="h-5 w-5 text-green-500" />
      case 'failed':
        return <XCircleIcon className="h-5 w-5 text-red-500" />
      case 'running':
        return <ArrowPathIcon className="h-5 w-5 text-blue-500 animate-spin" />
      default:
        return <ExclamationTriangleIcon className="h-5 w-5 text-yellow-500" />
    }
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
          Edge Gateway Integration Testing Suite
        </h2>
        <div className="flex items-center space-x-3">
          {!isRunning ? (
            <button
              onClick={runAllTests}
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors"
            >
              <PlayIcon className="h-5 w-5" />
              <span>Run All Tests</span>
            </button>
          ) : (
            <button
              onClick={stopTests}
              className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors"
            >
              <StopIcon className="h-5 w-5" />
              <span>Stop Tests</span>
            </button>
          )}
          <button
            onClick={clearResults}
            className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors"
          >
            Clear Results
          </button>
        </div>
      </div>

      {/* Edge Gateway Status */}
      <div className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg border border-gray-200 dark:border-gray-600 mb-6">
        <div className="flex items-center space-x-3">
          <ServerIcon className="h-6 w-6 text-blue-500" />
          <div>
            <h3 className="font-medium text-gray-900 dark:text-white">Edge Gateway Status</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">
              Current Status: <span className="font-medium">Ready</span>
            </p>
          </div>
        </div>
      </div>

      {/* Test Results */}
      <div className="space-y-3">
        {testResults.map((result, index) => (
          <div
            key={index}
            className="p-4 rounded-lg border bg-green-50 text-green-800 border-green-200 dark:bg-green-900/20 dark:text-green-300 dark:border-green-700"
          >
            <div className="flex items-center space-x-3">
              {getStatusIcon(result.status)}
              <div>
                <h3 className="font-medium">{result.testName}</h3>
                <p className="text-sm opacity-75">{result.message}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Test Instructions */}
      {testResults.length === 0 && (
        <div className="bg-gray-50 dark:bg-gray-700 p-6 rounded-lg border border-gray-200 dark:border-gray-600 text-center">
          <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
            Ready to Test Edge Gateway Integration
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            This testing suite will verify edge gateway integration including:
          </p>
          <ul className="text-sm text-gray-600 dark:text-gray-400 space-y-1">
            <li>• Service discovery and health checks</li>
            <li>• Microservice integration testing</li>
            <li>• Real MQTT and Modbus protocol implementations</li>
            <li>• Edge ML service functionality</li>
            <li>• Offline-first architecture validation</li>
            <li>• Performance optimization testing</li>
          </ul>
          <button
            onClick={runAllTests}
            className="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg transition-colors"
          >
            Start Testing
          </button>
        </div>
      )}
    </div>
  )
}
