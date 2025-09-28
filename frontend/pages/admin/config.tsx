import React, { useState } from 'react'
import Layout from '../../components/Layout'
import { CogIcon, ServerStackIcon, BellIcon, ShieldCheckIcon } from '@heroicons/react/24/outline'

interface SystemConfig {
  id: string
  category: string
  name: string
  value: string
  description: string
  type: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON'
  isEditable: boolean
  lastModified: string
}

interface ServiceConfig {
  name: string
  status: 'RUNNING' | 'STOPPED' | 'ERROR'
  port: number
  version: string
  lastRestart: string
  uptime: string
}

export default function SystemConfiguration() {
  return (
    <Layout>
      <SystemConfigurationContent />
    </Layout>
  )
}

function SystemConfigurationContent() {
  const [configValues, setConfigValues] = useState<Record<string, string>>({})
  
  // Initialize config values from systemConfigs
  React.useEffect(() => {
    const initialValues: Record<string, string> = {}
    systemConfigs.forEach(config => {
      initialValues[config.id] = config.value
    })
    setConfigValues(initialValues)
  }, [])
  
  const handleConfigChange = (configId: string, value: string) => {
    setConfigValues(prev => ({
      ...prev,
      [configId]: value
    }))
  }
  
  const systemConfigs: SystemConfig[] = [
    {
      id: '1',
      category: 'General',
      name: 'APP_NAME',
      value: 'SmartWatts',
      description: 'Application name displayed in the UI',
      type: 'STRING',
      isEditable: true,
      lastModified: '2025-01-01T10:00:00Z'
    },
    {
      id: '2',
      category: 'General',
      name: 'APP_VERSION',
      value: '1.0.0',
      description: 'Current application version',
      type: 'STRING',
      isEditable: false,
      lastModified: '2025-01-01T10:00:00Z'
    },
    {
      id: '3',
      category: 'Database',
      name: 'DB_CONNECTION_POOL_SIZE',
      value: '20',
      description: 'Maximum number of database connections',
      type: 'NUMBER',
      isEditable: true,
      lastModified: '2025-01-01T09:30:00Z'
    },
    {
      id: '4',
      category: 'Security',
      name: 'JWT_EXPIRATION_HOURS',
      value: '24',
      description: 'JWT token expiration time in hours',
      type: 'NUMBER',
      isEditable: true,
      lastModified: '2025-01-01T09:00:00Z'
    },
    {
      id: '5',
      category: 'Security',
      name: 'ENABLE_TWO_FACTOR_AUTH',
      value: 'true',
      description: 'Enable two-factor authentication',
      type: 'BOOLEAN',
      isEditable: true,
      lastModified: '2025-01-01T08:30:00Z'
    },
    {
      id: '6',
      category: 'Notifications',
      name: 'EMAIL_NOTIFICATIONS_ENABLED',
      value: 'true',
      description: 'Enable email notifications',
      type: 'BOOLEAN',
      isEditable: true,
      lastModified: '2025-01-01T08:00:00Z'
    }
  ]

  const serviceConfigs: ServiceConfig[] = [
    {
      name: 'User Service',
      status: 'RUNNING',
      port: 8081,
      version: '1.2.3',
      lastRestart: '2025-01-01T08:00:00Z',
      uptime: '2 days, 3 hours'
    },
    {
      name: 'Device Service',
      status: 'RUNNING',
      port: 8082,
      version: '1.1.5',
      lastRestart: '2025-01-01T08:00:00Z',
      uptime: '2 days, 3 hours'
    },
    {
      name: 'Energy Service',
      status: 'RUNNING',
      port: 8083,
      version: '1.3.1',
      lastRestart: '2025-01-01T08:00:00Z',
      uptime: '2 days, 3 hours'
    },
    {
      name: 'Analytics Service',
      status: 'RUNNING',
      port: 8084,
      version: '1.0.8',
      lastRestart: '2025-01-01T08:00:00Z',
      uptime: '2 days, 3 hours'
    },
    {
      name: 'Billing Service',
      status: 'RUNNING',
      port: 8085,
      version: '1.1.2',
      lastRestart: '2025-01-01T08:00:00Z',
      uptime: '2 days, 3 hours'
    },
    {
      name: 'Feature Flags Service',
      status: 'RUNNING',
      port: 8090,
      version: '1.0.3',
      lastRestart: '2025-01-01T08:00:00Z',
      uptime: '2 days, 3 hours'
    }
  ]

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'RUNNING': return 'text-green-600 bg-green-100'
      case 'STOPPED': return 'text-red-600 bg-red-100'
      case 'ERROR': return 'text-yellow-600 bg-yellow-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'General': return <CogIcon className="h-5 w-5" />
      case 'Database': return <ServerStackIcon className="h-5 w-5" />
      case 'Security': return <ShieldCheckIcon className="h-5 w-5" />
      case 'Notifications': return <BellIcon className="h-5 w-5" />
      default: return <CogIcon className="h-5 w-5" />
    }
  }

  const groupedConfigs = systemConfigs.reduce((acc, config) => {
    if (!acc[config.category]) {
      acc[config.category] = []
    }
    acc[config.category].push(config)
    return acc
  }, {} as Record<string, SystemConfig[]>)

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          System Configuration
        </h1>
        <p className="mt-2 text-gray-600 dark:text-gray-300">
          Manage system settings, service configurations, and application parameters
        </p>
      </div>

      {/* Service Status Overview */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow mb-8">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            Service Status
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Current status of all microservices
          </p>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {serviceConfigs.map((service) => (
              <div key={service.name} className="border border-gray-200 dark:border-gray-700 rounded-lg p-4">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium text-gray-900 dark:text-white">{service.name}</h4>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(service.status)}`}>
                    {service.status}
                  </span>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-500 dark:text-gray-400">Port:</span>
                    <span className="font-medium text-gray-900 dark:text-white">{service.port}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500 dark:text-gray-400">Version:</span>
                    <span className="font-medium text-gray-900 dark:text-white">{service.version}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500 dark:text-gray-400">Uptime:</span>
                    <span className="font-medium text-gray-900 dark:text-white">{service.uptime}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* System Configuration */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            System Configuration
          </h3>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Application settings and configuration parameters
          </p>
        </div>
        <div className="p-6">
          {Object.entries(groupedConfigs).map(([category, configs]) => (
            <div key={category} className="mb-8">
              <div className="flex items-center mb-4">
                {getCategoryIcon(category)}
                <h4 className="ml-2 text-lg font-medium text-gray-900 dark:text-white">{category}</h4>
              </div>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {configs.map((config) => (
                  <div key={config.id} className="border border-gray-200 dark:border-gray-700 rounded-lg p-4">
                    <div className="flex items-center justify-between mb-2">
                      <h5 className="font-medium text-gray-900 dark:text-white">{config.name}</h5>
                      <span className="text-xs text-gray-500 dark:text-gray-400">{config.type}</span>
                    </div>
                    <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">{config.description}</p>
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <input
                          type={config.type === 'NUMBER' ? 'number' : config.type === 'BOOLEAN' ? 'checkbox' : 'text'}
                          value={configValues[config.id] || config.value}
                          onChange={(e) => handleConfigChange(config.id, e.target.value)}
                          disabled={!config.isEditable}
                          className={`w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-sm ${
                            config.isEditable 
                              ? 'bg-white dark:bg-gray-700 text-gray-900 dark:text-white' 
                              : 'bg-gray-100 dark:bg-gray-600 text-gray-500 dark:text-gray-400'
                          }`}
                        />
                      </div>
                      {config.isEditable && (
                        <button className="ml-2 px-3 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors">
                          Save
                        </button>
                      )}
                    </div>
                    <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">
                      Last modified: {new Date(config.lastModified).toLocaleString()}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}