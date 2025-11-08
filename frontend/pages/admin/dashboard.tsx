import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import Layout from '../../components/Layout'
import AdminRoute from '../../components/AdminRoute'
import {
  ServerIcon,
  UsersIcon,
  ChartBarIcon,
  ShieldCheckIcon,
  CogIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  BanknotesIcon,
  DeviceTabletIcon,
  BoltIcon,
  GlobeAltIcon,
} from '@heroicons/react/24/outline'

interface SystemHealth {
  status: 'HEALTHY' | 'WARNING' | 'CRITICAL'
  uptime: string
  responseTime: number
  activeConnections: number
  errorRate: number
}

interface BusinessMetrics {
  totalUsers: number
  activeUsers: number
  newUsersThisMonth: number
  totalRevenue: number
  monthlyRecurringRevenue: number
  customerChurnRate: number
  averageCustomerLifetime: number
}

interface SystemMetrics {
  totalDevices: number
  activeDevices: number
  dataProcessedToday: number
  apiRequestsPerMinute: number
  databaseSize: string
  backupStatus: 'SUCCESS' | 'PENDING' | 'FAILED'
}

export default function AdminDashboard() {
  return (
    <AdminRoute>
      <Layout>
        <AdminDashboardContent />
      </Layout>
    </AdminRoute>
  )
}

function AdminDashboardContent() {
  const router = useRouter()
  const [isMounted, setIsMounted] = useState(false)
  
  const [systemHealth, setSystemHealth] = useState<SystemHealth>({
    status: 'HEALTHY',
    uptime: '99.97%',
    responseTime: 145,
    activeConnections: 1247,
    errorRate: 0.03
  })

  // Handle component mounting/unmounting
  useEffect(() => {
    setIsMounted(true)
    return () => {
      setIsMounted(false)
    }
  }, [])

  // Quick Actions handlers
  const handleUserManagement = () => {
    if (!isMounted) return
    try {
      router.push('/admin/users')
    } catch (error) {
      console.error('Navigation error:', error)
    }
  }

  const handleSecurityAudit = () => {
    if (!isMounted) return
    try {
      router.push('/admin/security')
    } catch (error) {
      console.error('Navigation error:', error)
    }
  }

  const handleSystemSettings = () => {
    if (!isMounted) return
    try {
      router.push('/admin/config')
    } catch (error) {
      console.error('Navigation error:', error)
    }
  }

  const [businessMetrics, setBusinessMetrics] = useState<BusinessMetrics>({
    totalUsers: 15420,
    activeUsers: 12890,
    newUsersThisMonth: 1247,
    totalRevenue: 2847500,
    monthlyRecurringRevenue: 187500,
    customerChurnRate: 2.3,
    averageCustomerLifetime: 18.5
  })

  const [systemMetrics, setSystemMetrics] = useState<SystemMetrics>({
    totalDevices: 45678,
    activeDevices: 42345,
    dataProcessedToday: 2.4,
    apiRequestsPerMinute: 1247,
    databaseSize: '2.4 TB',
    backupStatus: 'SUCCESS'
  })

  const [selectedTimeframe, setSelectedTimeframe] = useState<'24h' | '7d' | '30d' | '90d'>('7d')

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          SmartWatts System Administration
        </h1>
        <p className="mt-2 text-gray-600 dark:text-gray-300">
          System health, business metrics, and operational overview
        </p>
      </div>

      {/* System Health Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className={`p-2 rounded-lg ${
              systemHealth.status === 'HEALTHY' ? 'bg-green-100 text-green-600' :
              systemHealth.status === 'WARNING' ? 'bg-yellow-100 text-yellow-600' :
              'bg-red-100 text-red-600'
            }`}>
              <ServerIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">System Status</p>
              <p className={`text-lg font-semibold ${
                systemHealth.status === 'HEALTHY' ? 'text-green-600' :
                systemHealth.status === 'WARNING' ? 'text-yellow-600' :
                'text-red-600'
              }`}>
                {systemHealth.status}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-blue-100 text-blue-600">
              <ClockIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Uptime</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{systemHealth.uptime}</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-purple-100 text-purple-600">
              <BoltIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Response Time</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{systemHealth.responseTime}ms</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-indigo-100 text-indigo-600">
              <GlobeAltIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Active Connections</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{systemHealth.activeConnections.toLocaleString()}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Business Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center">
            <BanknotesIcon className="h-5 w-5 mr-2 text-green-600" />
            Business Performance
          </h3>
          <div className="space-y-4">
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Total Users</span>
              <span className="font-semibold text-gray-900 dark:text-white">{businessMetrics.totalUsers.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Active Users</span>
              <span className="font-semibold text-gray-900 dark:text-white">{businessMetrics.activeUsers.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">New Users (This Month)</span>
              <span className="font-semibold text-gray-900 dark:text-white">{businessMetrics.newUsersThisMonth.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Monthly Recurring Revenue</span>
              <span className="font-semibold text-green-600">₦{businessMetrics.monthlyRecurringRevenue.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Customer Churn Rate</span>
              <span className="font-semibold text-red-600">{businessMetrics.customerChurnRate}%</span>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center">
            <DeviceTabletIcon className="h-5 w-5 mr-2 text-blue-600" />
            System Infrastructure
          </h3>
          <div className="space-y-4">
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Total Devices</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.totalDevices.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Active Devices</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.activeDevices.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Data Processed Today</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.dataProcessedToday} TB</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">API Requests/Min</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.apiRequestsPerMinute.toLocaleString()}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Database Size</span>
              <span className="font-semibold text-gray-900 dark:text-white">{systemMetrics.databaseSize}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600 dark:text-gray-400">Backup Status</span>
              <span className={`font-semibold ${
                systemMetrics.backupStatus === 'SUCCESS' ? 'text-green-600' :
                systemMetrics.backupStatus === 'PENDING' ? 'text-yellow-600' :
                'text-red-600'
              }`}>
                {systemMetrics.backupStatus}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button 
            onClick={handleUserManagement}
            className="flex items-center justify-center px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <UsersIcon className="h-5 w-5 mr-2" />
            User Management
          </button>
          <button 
            onClick={handleSecurityAudit}
            className="flex items-center justify-center px-4 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
          >
            <ShieldCheckIcon className="h-5 w-5 mr-2" />
            Security Audit
          </button>
          <button 
            onClick={handleSystemSettings}
            className="flex items-center justify-center px-4 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors"
          >
            <CogIcon className="h-5 w-5 mr-2" />
            System Settings
          </button>
        </div>
      </div>

      {/* Recent Alerts */}
      <div className="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Recent System Alerts</h3>
        <div className="space-y-3">
          <div className="flex items-center p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
            <CheckCircleIcon className="h-5 w-5 text-green-600 mr-3" />
            <span className="text-green-800 dark:text-green-200">Database backup completed successfully</span>
            <span className="ml-auto text-sm text-green-600">2 hours ago</span>
          </div>
          <div className="flex items-center p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
            <ExclamationTriangleIcon className="h-5 w-5 text-yellow-600 mr-3" />
            <span className="text-yellow-800 dark:text-yellow-200">High memory usage detected on server-03</span>
            <span className="ml-auto text-sm text-yellow-600">4 hours ago</span>
          </div>
          <div className="flex items-center p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
            <CheckCircleIcon className="h-5 w-5 text-blue-600 mr-3" />
            <span className="text-blue-800 dark:text-blue-200">New user registration system deployed</span>
            <span className="ml-auto text-sm text-blue-600">6 hours ago</span>
          </div>
        </div>
      </div>
    </div>
  )
}
