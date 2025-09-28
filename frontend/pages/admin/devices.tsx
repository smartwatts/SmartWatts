import React, { useState, useEffect } from 'react'
import Layout from '../../components/Layout'
import AddDeviceModal from '../../components/AddDeviceModal'
import DeviceActivationModal from '../../components/DeviceActivationModal'
import {
  DevicePhoneMobileIcon,
  PlusIcon,
  ArrowPathIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ClockIcon,
  WifiIcon,
  SignalSlashIcon,
  ShieldCheckIcon,
  UserIcon,
} from '@heroicons/react/24/outline'

interface Device {
  id: string
  deviceId: string
  name: string
  deviceType: string
  manufacturer: string
  model: string
  status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'ERROR' | 'PENDING' | 'EXPIRED'
  connectionStatus: 'ONLINE' | 'OFFLINE'
  protocol: string
  location: string
  installationDate: string
  lastSeen: string
  userId: string
  userName: string
  customerType: 'RESIDENTIAL' | 'COMMERCIAL'
  trustCategory: 'HIGH' | 'MEDIUM' | 'LOW'
  activatedAt: string
  expiresAt: string
  hardwareId: string
}

interface DeviceAnalytics {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  activeDevices: number
  inactiveDevices: number
  maintenanceDevices: number
  errorDevices: number
  pendingDevices: number
  expiredDevices: number
  recentRegistrations: number
  residentialDevices: number
  commercialDevices: number
  highTrustDevices: number
  mediumTrustDevices: number
  lowTrustDevices: number
}

export default function AdminDevices() {
  try {
    return (
      <Layout>
        <AdminDevicesContent />
      </Layout>
    )
  } catch (error) {
    console.error('Error in AdminDevices:', error)
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
        <div className="p-6">
          <h1 className="text-2xl font-bold text-red-600 mb-4">Error Loading Devices</h1>
          <p className="text-gray-600">There was an error loading the devices page.</p>
        </div>
      </div>
    )
  }
}

function AdminDevicesContent() {
  const [devices, setDevices] = useState<Device[]>([])
  const [loading, setLoading] = useState(true)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isActivationModalOpen, setIsActivationModalOpen] = useState(false)
  const [deviceAnalytics, setDeviceAnalytics] = useState<DeviceAnalytics>({
    totalDevices: 0,
    onlineDevices: 0,
    offlineDevices: 0,
    activeDevices: 0,
    inactiveDevices: 0,
    maintenanceDevices: 0,
    errorDevices: 0,
    pendingDevices: 0,
    expiredDevices: 0,
    recentRegistrations: 0,
    residentialDevices: 0,
    commercialDevices: 0,
    highTrustDevices: 0,
    mediumTrustDevices: 0,
    lowTrustDevices: 0
  })

  useEffect(() => {
    loadDevices()
  }, [])

  const loadDevices = async () => {
    setLoading(true)
    try {
      // Load real data from API
      const response = await fetch('/api/proxy?service=device&endpoint=/api/v1/devices')
      if (!response.ok) {
        throw new Error('Failed to load devices')
      }
      const devices: Device[] = await response.json()

      setDevices(devices)
      
      // Load analytics from API
      const analyticsResponse = await fetch('/api/proxy?service=device&endpoint=/api/v1/devices/analytics')
      if (analyticsResponse.ok) {
        const analytics: DeviceAnalytics = await analyticsResponse.json()
        setDeviceAnalytics(analytics)
      } else {
        // Calculate analytics from device data if API not available
        const analytics: DeviceAnalytics = {
          totalDevices: devices.length,
          onlineDevices: devices.filter(d => d.connectionStatus === 'ONLINE').length,
          offlineDevices: devices.filter(d => d.connectionStatus === 'OFFLINE').length,
          activeDevices: devices.filter(d => d.status === 'ACTIVE').length,
          inactiveDevices: devices.filter(d => d.status === 'INACTIVE').length,
          maintenanceDevices: devices.filter(d => d.status === 'MAINTENANCE').length,
          errorDevices: devices.filter(d => d.status === 'ERROR').length,
          pendingDevices: devices.filter(d => d.status === 'PENDING').length,
          expiredDevices: devices.filter(d => d.status === 'EXPIRED').length,
          recentRegistrations: devices.filter(d => new Date(d.installationDate) > new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)).length,
          residentialDevices: devices.filter(d => d.customerType === 'RESIDENTIAL').length,
          commercialDevices: devices.filter(d => d.customerType === 'COMMERCIAL').length,
          highTrustDevices: devices.filter(d => d.trustCategory === 'HIGH').length,
          mediumTrustDevices: devices.filter(d => d.trustCategory === 'MEDIUM').length,
          lowTrustDevices: devices.filter(d => d.trustCategory === 'LOW').length
        }
        setDeviceAnalytics(analytics)
      }
    } catch (error) {
      console.error('Failed to load devices:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDeviceAdded = () => {
    loadDevices()
  }

  const handleDeviceActivated = (newDevice: Device) => {
    setDevices(prev => [...prev, newDevice])
    setIsActivationModalOpen(false)
    loadDevices() // Refresh analytics
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <CheckCircleIcon className="h-5 w-5 text-green-500" />
      case 'INACTIVE':
        return <ClockIcon className="h-5 w-5 text-gray-500" />
      case 'MAINTENANCE':
        return <ExclamationTriangleIcon className="h-5 w-5 text-yellow-500" />
      case 'ERROR':
        return <ExclamationTriangleIcon className="h-5 w-5 text-red-500" />
      case 'PENDING':
        return <ClockIcon className="h-5 w-5 text-yellow-500" />
      case 'EXPIRED':
        return <ExclamationTriangleIcon className="h-5 w-5 text-red-500" />
      default:
        return <ClockIcon className="h-5 w-5 text-gray-500" />
    }
  }

  const getConnectionIcon = (status: string) => {
    return status === 'ONLINE' 
      ? <WifiIcon className="h-5 w-5 text-green-500" />
      : <SignalSlashIcon className="h-5 w-5 text-red-500" />
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'INACTIVE':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
      case 'MAINTENANCE':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'ERROR':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'EXPIRED':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getConnectionColor = (status: string) => {
    return status === 'ONLINE'
      ? 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      : 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
  }

  const getTrustColor = (trust: string) => {
    switch (trust) {
      case 'HIGH':
        return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'LOW':
        return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getCustomerTypeColor = (type: string) => {
    return type === 'RESIDENTIAL'
      ? 'bg-blue-100 text-blue-800 dark:bg-blue-900/20 dark:text-blue-400'
      : 'bg-purple-100 text-purple-800 dark:bg-purple-900/20 dark:text-purple-400'
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
          Device Management & Verification
        </h1>
        <p className="mt-2 text-gray-600 dark:text-gray-300">
          Manage, monitor, and verify all SmartWatts devices across the system
        </p>
      </div>

      {/* Analytics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400">
              <DevicePhoneMobileIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Total Devices</p>
              <p className="text-2xl font-semibold text-gray-900 dark:text-white">{deviceAnalytics.totalDevices}</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400">
              <CheckCircleIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Active Devices</p>
              <p className="text-2xl font-semibold text-gray-900 dark:text-white">{deviceAnalytics.activeDevices}</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400">
              <ClockIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Pending Verification</p>
              <p className="text-2xl font-semibold text-gray-900 dark:text-white">{deviceAnalytics.pendingDevices}</p>
            </div>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400">
              <ExclamationTriangleIcon className="h-6 w-6" />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Expired Devices</p>
              <p className="text-2xl font-semibold text-gray-900 dark:text-white">{deviceAnalytics.expiredDevices}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="mb-8">
        <div className="flex space-x-4">
          <button
            onClick={() => setIsAddModalOpen(true)}
            className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors flex items-center space-x-2"
          >
            <PlusIcon className="h-5 w-5" />
            <span>Register New Device</span>
          </button>
          <button
            onClick={() => setIsActivationModalOpen(true)}
            className="bg-green-600 text-white px-6 py-3 rounded-lg hover:bg-green-700 transition-colors flex items-center space-x-2"
          >
            <ShieldCheckIcon className="h-5 w-5" />
            <span>Activate Device</span>
          </button>
          <button
            onClick={loadDevices}
            disabled={loading}
            className="bg-gray-600 text-white px-6 py-3 rounded-lg hover:bg-gray-700 transition-colors disabled:opacity-50 flex items-center space-x-2"
          >
            <ArrowPathIcon className={`h-5 w-5 ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Device List */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">All Devices</h3>
        </div>
        
        {loading ? (
          <div className="p-8 text-center">
            <ArrowPathIcon className="h-8 w-8 animate-spin text-blue-600 mx-auto mb-4" />
            <p className="text-gray-600 dark:text-gray-400">Loading devices...</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    Device
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    Customer
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    Trust Level
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    Connection
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    Expires
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                    Last Seen
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                {devices.map((device) => (
                  <tr key={device.id} className="hover:bg-gray-50 dark:hover:bg-gray-700">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10">
                          <div className="h-10 w-10 rounded-lg bg-blue-100 dark:bg-blue-900/20 flex items-center justify-center">
                            <DevicePhoneMobileIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900 dark:text-white">
                            {device.name}
                          </div>
                          <div className="text-sm text-gray-500 dark:text-gray-400">
                            {device.deviceId}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900 dark:text-white">
                        {device.userName}
                      </div>
                      <div className="text-sm text-gray-500 dark:text-gray-400">
                        {device.customerType}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {getStatusIcon(device.status)}
                        <span className={`ml-2 inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(device.status)}`}>
                          {device.status}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getTrustColor(device.trustCategory)}`}>
                        {device.trustCategory}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        {getConnectionIcon(device.connectionStatus)}
                        <span className={`ml-2 inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getConnectionColor(device.connectionStatus)}`}>
                          {device.connectionStatus}
                        </span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                      {device.expiresAt ? new Date(device.expiresAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">
                      {new Date(device.lastSeen).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add Device Modal */}
      <AddDeviceModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onDeviceAdded={handleDeviceAdded}
      />

      {/* Device Activation Modal */}
      {isActivationModalOpen && (
        <DeviceActivationModal
          onClose={() => setIsActivationModalOpen(false)}
          onDeviceActivated={handleDeviceActivated}
        />
      )}
    </div>
  )
}
