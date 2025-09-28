import React, { useState } from 'react'
import Layout from '../../components/Layout'
import {
  ServerIcon,
  ServerStackIcon,
  CogIcon,
  WifiIcon,
} from '@heroicons/react/24/outline'

interface ServiceHealth {
  name: string
  status: 'HEALTHY' | 'WARNING' | 'CRITICAL' | 'OFFLINE'
  responseTime: number
  uptime: string
  lastCheck: string
  endpoint: string
}

interface SystemMetrics {
  cpuUsage: number
  memoryUsage: number
  diskUsage: number
  networkLatency: number
  activeConnections: number
  errorRate: number
}

export default function SystemHealth() {
  return (
    
      <Layout>
        <SystemHealthContent />
      </Layout>
    
  )
}

function SystemHealthContent() {
  const [services, setServices] = useState<ServiceHealth[]>([
    {
      name: 'User Service',
      status: 'HEALTHY',
      responseTime: 45,
      uptime: '99.98%',
      lastCheck: '2 minutes ago',
      endpoint: '/api/users'
    },
    {
      name: 'Device Service',
      status: 'HEALTHY',
      responseTime: 67,
      uptime: '99.95%',
      lastCheck: '2 minutes ago',
      endpoint: '/api/devices'
    },
    {
      name: 'Energy Service',
      status: 'WARNING',
      responseTime: 234,
      uptime: '99.87%',
      lastCheck: '2 minutes ago',
      endpoint: '/api/energy'
    },
    {
      name: 'Analytics Service',
      status: 'HEALTHY',
      responseTime: 89,
      uptime: '99.92%',
      lastCheck: '2 minutes ago',
      endpoint: '/api/analytics'
    },
    {
      name: 'Billing Service',
      status: 'HEALTHY',
      responseTime: 56,
      uptime: '99.96%',
      lastCheck: '2 minutes ago',
      endpoint: '/api/billing'
    },
    {
      name: 'Edge Gateway',
      status: 'CRITICAL',
      responseTime: 1200,
      uptime: '98.45%',
      lastCheck: '2 minutes ago',
      endpoint: '/api/edge'
    }
  ])

  const [systemMetrics, setSystemMetrics] = useState<SystemMetrics>({
    cpuUsage: 23.4,
    memoryUsage: 67.8,
    diskUsage: 45.2,
    networkLatency: 12,
    activeConnections: 1247,
    errorRate: 0.03
  })

  const [isRefreshing, setIsRefreshing] = useState(false)

  const refreshMetrics = async () => {
    setIsRefreshing(true)
    try {
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      // Update services with new data
      setServices(prev => prev.map(service => ({
        ...service,
        responseTime: Math.floor(Math.random() * 200) + 20,
        lastCheck: 'Just now',
        status: Math.random() > 0.1 ? 'HEALTHY' : Math.random() > 0.5 ? 'WARNING' : 'CRITICAL'
      })))
      
      // Update system metrics with new data
      setSystemMetrics(prev => ({
        ...prev,
        cpuUsage: Math.round((Math.random() * 50 + 10) * 10) / 10,
        memoryUsage: Math.round((Math.random() * 40 + 30) * 10) / 10,
        diskUsage: Math.round((Math.random() * 30 + 20) * 10) / 10,
        networkLatency: Math.floor(Math.random() * 20) + 5,
        activeConnections: Math.floor(Math.random() * 500) + 1000,
        errorRate: Math.round((Math.random() * 0.1) * 100) / 100
      }))
    } catch (error) {
      console.error('Failed to refresh metrics:', error)
    } finally {
      setIsRefreshing(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'HEALTHY': return 'text-green-600 bg-green-100'
      case 'WARNING': return 'text-yellow-600 bg-yellow-100'
      case 'CRITICAL': return 'text-red-600 bg-red-100'
      case 'OFFLINE': return 'text-gray-600 bg-gray-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }



  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          System Health & Performance
        </h1>
        <p className="mt-2 text-gray-600 dark:text-gray-300">
          Real-time monitoring of SmartWatts platform services and infrastructure
        </p>
      </div>

      {/* Overall System Status */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-blue-100 text-blue-600">
              <ServerIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Services Online</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">
                {services.filter(s => s.status === 'HEALTHY').length}/{services.length}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-green-100 text-green-600">
              <CogIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">CPU Usage</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{systemMetrics.cpuUsage}%</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-purple-100 text-purple-600">
              <ServerStackIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Memory Usage</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{systemMetrics.memoryUsage}%</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-indigo-100 text-indigo-600">
              <WifiIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Network Latency</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{systemMetrics.networkLatency}ms</p>
            </div>
          </div>
        </div>
      </div>

      {/* Service Health Grid */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-8">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            Microservice Health Status
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Real-time status of all platform services
          </p>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {services.map((service) => (
              <div key={service.name} className="border border-gray-200 dark:border-gray-700 rounded-lg p-4">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium text-gray-900 dark:text-white">{service.name}</h4>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(service.status)}`}>
                    {service.status}
                  </span>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-500 dark:text-gray-400">Response Time:</span>
                    <span className="font-medium text-gray-900 dark:text-white">{service.responseTime}ms</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500 dark:text-gray-400">Uptime:</span>
                    <span className="font-medium text-gray-900 dark:text-white">{service.uptime}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500 dark:text-gray-400">Last Check:</span>
                    <span className="font-medium text-gray-900 dark:text-white">{service.lastCheck}</span>
                  </div>
                </div>
                <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700">
                  <p className="text-xs text-gray-500 dark:text-gray-400 font-mono">{service.endpoint}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* System Performance Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Performance Metrics</h3>
          <div className="space-y-4">
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600 dark:text-gray-400">CPU Usage</span>
                <span className="text-gray-900 dark:text-white">{systemMetrics.cpuUsage}%</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div 
                  className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${systemMetrics.cpuUsage}%` }}
                ></div>
              </div>
            </div>
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600 dark:text-gray-400">Memory Usage</span>
                <span className="text-gray-900 dark:text-white">{systemMetrics.memoryUsage}%</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div 
                  className="bg-green-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${systemMetrics.memoryUsage}%` }}
                ></div>
              </div>
            </div>
            <div>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-600 dark:text-gray-400">Disk Usage</span>
                <span className="text-gray-900 dark:text-white">{systemMetrics.diskUsage}%</span>
              </div>
              <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2">
                <div 
                  className="bg-purple-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${systemMetrics.diskUsage}%` }}
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Network & Connections</h3>
          <div className="space-y-4">
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Active Connections</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.activeConnections.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Network Latency</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.networkLatency}ms</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Error Rate</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.errorRate}%</span>
            </div>
            <div className="pt-4">
              <button 
                onClick={refreshMetrics}
                disabled={isRefreshing}
                className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 disabled:cursor-not-allowed text-white px-4 py-2 rounded-lg font-medium transition-colors flex items-center justify-center space-x-2"
              >
                {isRefreshing ? (
                  <>
                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                    <span>Refreshing...</span>
                  </>
                ) : (
                  <span>Refresh Metrics</span>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
