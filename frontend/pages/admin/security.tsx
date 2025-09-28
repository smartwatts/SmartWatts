import React, { useState } from 'react'
import Layout from '../../components/Layout'
import {
  ShieldCheckIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  LockClosedIcon,
  KeyIcon,
  EyeIcon,
  DocumentTextIcon,
} from '@heroicons/react/24/outline'

interface SecurityEvent {
  id: string
  type: 'LOGIN' | 'LOGOUT' | 'FAILED_LOGIN' | 'PASSWORD_CHANGE' | 'ROLE_CHANGE' | 'API_ACCESS'
  user: string
  timestamp: string
  ipAddress: string
  userAgent: string
  status: 'SUCCESS' | 'FAILED' | 'WARNING'
  details: string
}

interface ComplianceStatus {
  gdpr: 'COMPLIANT' | 'NON_COMPLIANT' | 'PENDING'
  ndpr: 'COMPLIANT' | 'NON_COMPLIANT' | 'PENDING'
  iso27001: 'COMPLIANT' | 'NON_COMPLIANT' | 'PENDING'
  lastAudit: string
  nextAudit: string
}

export default function SecurityCompliance() {
  return (
    
      <Layout>
        <SecurityComplianceContent />
      </Layout>
    
  )
}

function SecurityComplianceContent() {
  const [securityEvents, setSecurityEvents] = useState<SecurityEvent[]>([
    {
      id: '1',
      type: 'LOGIN',
      user: 'admin@mysmartwatts.com',
      timestamp: '2025-01-01T10:30:00Z',
      ipAddress: '192.168.1.100',
      userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
      status: 'SUCCESS',
      details: 'Successful admin login'
    },
    {
      id: '2',
      type: 'FAILED_LOGIN',
      user: 'unknown@example.com',
      timestamp: '2025-01-01T10:25:00Z',
      ipAddress: '192.168.1.101',
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)',
      status: 'FAILED',
      details: 'Invalid credentials provided'
    },
    {
      id: '3',
      type: 'API_ACCESS',
      user: 'system@smartwatts.ng',
      timestamp: '2025-01-01T10:20:00Z',
      ipAddress: '10.0.0.5',
      userAgent: 'SmartWatts-API-Client/1.0',
      status: 'SUCCESS',
      details: 'Device data synchronization'
    }
  ])

  const [complianceStatus, setComplianceStatus] = useState<ComplianceStatus>({
    gdpr: 'COMPLIANT',
    ndpr: 'COMPLIANT',
    iso27001: 'PENDING',
    lastAudit: '2024-12-15',
    nextAudit: '2025-03-15'
  })

  const getEventIcon = (type: string) => {
    switch (type) {
      case 'LOGIN': return <CheckCircleIcon className="h-5 w-5 text-green-600" />
      case 'LOGOUT': return <ClockIcon className="h-5 w-5 text-blue-600" />
      case 'FAILED_LOGIN': return <ExclamationTriangleIcon className="h-5 w-5 text-red-600" />
      case 'PASSWORD_CHANGE': return <KeyIcon className="h-5 w-5 text-purple-600" />
      case 'ROLE_CHANGE': return <ShieldCheckIcon className="h-5 w-5 text-orange-600" />
      case 'API_ACCESS': return <DocumentTextIcon className="h-5 w-5 text-indigo-600" />
      default: return <EyeIcon className="h-5 w-5 text-gray-600" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SUCCESS': return 'text-green-600 bg-green-100'
      case 'FAILED': return 'text-red-600 bg-red-100'
      case 'WARNING': return 'text-yellow-600 bg-yellow-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  const getComplianceColor = (status: string) => {
    switch (status) {
      case 'COMPLIANT': return 'text-green-600 bg-green-100'
      case 'NON_COMPLIANT': return 'text-red-600 bg-red-100'
      case 'PENDING': return 'text-yellow-600 bg-yellow-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Security & Compliance
        </h1>
        <p className="mt-2 text-gray-600 dark:text-gray-300">
          Monitor security events, audit logs, and compliance status
        </p>
      </div>

      {/* Security Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-green-100 text-green-600">
              <ShieldCheckIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Security Score</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">98/100</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-blue-100 text-blue-600">
              <LockClosedIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Active Sessions</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">24</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-yellow-100 text-yellow-600">
              <ExclamationTriangleIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Failed Logins (24h)</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">3</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-purple-100 text-purple-600">
              <KeyIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">API Keys</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">12</p>
            </div>
          </div>
        </div>
      </div>

      {/* Compliance Status */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-8">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            Compliance Status
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Current compliance status across different standards
          </p>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <h4 className="text-lg font-medium text-gray-900 dark:text-white mb-2">GDPR</h4>
              <span className={`inline-flex px-3 py-1 rounded-full text-sm font-medium ${getComplianceColor(complianceStatus.gdpr)}`}>
                {complianceStatus.gdpr}
              </span>
            </div>
            <div className="text-center">
              <h4 className="text-lg font-medium text-gray-900 dark:text-white mb-2">NDPR</h4>
              <span className={`inline-flex px-3 py-1 rounded-full text-sm font-medium ${getComplianceColor(complianceStatus.ndpr)}`}>
                {complianceStatus.ndpr}
              </span>
            </div>
            <div className="text-center">
              <h4 className="text-lg font-medium text-gray-900 dark:text-white mb-2">ISO 27001</h4>
              <span className={`inline-flex px-3 py-1 rounded-full text-sm font-medium ${getComplianceColor(complianceStatus.iso27001)}`}>
                {complianceStatus.iso27001}
              </span>
            </div>
          </div>
          <div className="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-500 dark:text-gray-400">Last Audit:</span>
                <span className="ml-2 font-medium text-gray-900 dark:text-white">{complianceStatus.lastAudit}</span>
              </div>
              <div>
                <span className="text-gray-500 dark:text-gray-400">Next Audit:</span>
                <span className="ml-2 font-medium text-gray-900 dark:text-white">{complianceStatus.nextAudit}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Security Events */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            Recent Security Events
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Latest security-related activities and events
          </p>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
            <thead className="bg-gray-50 dark:bg-gray-700">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Event
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  User
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  IP Address
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                  Timestamp
                </th>
              </tr>
            </thead>
            <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
              {securityEvents.map((event) => (
                <tr key={event.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      {getEventIcon(event.type)}
                      <div className="ml-3">
                        <div className="text-sm font-medium text-gray-900 dark:text-white">
                          {event.type.replace('_', ' ')}
                        </div>
                        <div className="text-sm text-gray-500 dark:text-gray-400">
                          {event.details}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                    {event.user}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                    {event.ipAddress}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(event.status)}`}>
                      {event.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                    {new Date(event.timestamp).toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
