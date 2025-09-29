import React, { useEffect, useState } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import AddDeviceModal from '../components/AddDeviceModal'
import { useAuth } from '../hooks/useAuth'
import {
  CogIcon,
  BoltIcon,
  SunIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  XCircleIcon,
  WifiIcon,
  SignalIcon,
  PlusIcon,
  ChartBarIcon,
  ClockIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  ShieldCheckIcon,
  WrenchScrewdriverIcon,
  FireIcon,
  LightBulbIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell, AreaChart, Area } from 'recharts'

interface Device {
  id: string
  name: string
  type: 'smart-meter' | 'solar-panel' | 'inverter' | 'generator' | 'sensor'
  status: 'online' | 'offline' | 'maintenance' | 'error'
  location: string
  lastReading: string
  powerOutput?: number
  efficiency?: number
  batteryLevel?: number
  temperature?: number
  uptime: number
  performanceScore: number
  energyConsumption: number
  costSavings: number
  maintenanceSchedule: string
  lastCalibration: string
  firmwareVersion: string
  securityStatus: 'secure' | 'warning' | 'critical'
  verificationStatus?: 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'SUSPENDED'
  trustLevel?: 'OEM_LOCKED' | 'UNVERIFIED'
  isVerified?: boolean
}

interface DeviceAnalytics {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  maintenanceDevices: number
  errorDevices: number
  averageEfficiency: number
  totalEnergyConsumption: number
  totalCostSavings: number
  systemUptime: number
  securityScore: number
}

interface PerformanceMetrics {
  deviceId: string
  timestamp: string
  efficiency: number
  powerOutput: number
  temperature: number
  energyConsumption: number
}

interface MaintenanceSchedule {
  deviceId: string
  deviceName: string
  nextMaintenance: string
  maintenanceType: 'routine' | 'preventive' | 'emergency'
  estimatedCost: number
  priority: 'high' | 'medium' | 'low'
}

export default function Devices() {
  const { user } = useAuth()
  const router = useRouter()
  const [devices, setDevices] = useState<Device[]>([])
  const [loading, setLoading] = useState(true)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [deviceAnalytics, setDeviceAnalytics] = useState<DeviceAnalytics>({
    totalDevices: 0,
    onlineDevices: 0,
    offlineDevices: 0,
    maintenanceDevices: 0,
    errorDevices: 0,
    averageEfficiency: 0,
    totalEnergyConsumption: 0,
    totalCostSavings: 0,
    systemUptime: 0,
    securityScore: 0
  })
  const [performanceMetrics, setPerformanceMetrics] = useState<PerformanceMetrics[]>([])
  const [maintenanceSchedule, setMaintenanceSchedule] = useState<MaintenanceSchedule[]>([])

  useEffect(() => {
    // Redirect super admins to admin dashboard
    if (user?.role === 'ROLE_ENTERPRISE_ADMIN') {
      router.replace('/admin/dashboard')
    }
  }, [user, router])

  // Don't render customer pages for super admins
  if (user?.role === 'ROLE_ENTERPRISE_ADMIN') {
    return null
  }

  const loadDevices = async () => {
    try {
      // Mock devices data
      const mockDevices: Device[] = [
        {
          id: '1',
          name: 'Smart Meter - Main',
          type: 'smart-meter',
          status: 'online',
          location: 'Main Distribution Board',
          lastReading: '2 minutes ago',
          powerOutput: 4.2,
          efficiency: 95,
          uptime: 99.8,
          performanceScore: 92,
          energyConsumption: 168,
          costSavings: 25000,
          maintenanceSchedule: '2024-04-15',
          lastCalibration: '2024-01-10',
          firmwareVersion: 'v2.1.4',
          securityStatus: 'secure'
        },
        {
          id: '2',
          name: 'Solar Panel Array 1',
          type: 'solar-panel',
          status: 'online',
          location: 'Roof Top',
          lastReading: '1 minute ago',
          powerOutput: 2.8,
          efficiency: 87,
          temperature: 45,
          uptime: 98.5,
          performanceScore: 89,
          energyConsumption: 112,
          costSavings: 18000,
          maintenanceSchedule: '2024-05-20',
          lastCalibration: '2024-01-15',
          firmwareVersion: 'v1.9.2',
          securityStatus: 'secure'
        },
        {
          id: '3',
          name: 'Inverter System',
          type: 'inverter',
          status: 'online',
          location: 'Utility Room',
          lastReading: '30 seconds ago',
          powerOutput: 3.1,
          efficiency: 92,
          batteryLevel: 85,
          uptime: 99.2,
          performanceScore: 94,
          energyConsumption: 124,
          costSavings: 22000,
          maintenanceSchedule: '2024-03-30',
          lastCalibration: '2024-01-08',
          firmwareVersion: 'v3.0.1',
          securityStatus: 'warning'
        },
        {
          id: '4',
          name: 'Backup Generator',
          type: 'generator',
          status: 'offline',
          location: 'Backyard',
          lastReading: '2 hours ago',
          powerOutput: 0,
          efficiency: 0,
          uptime: 45.2,
          performanceScore: 67,
          energyConsumption: 0,
          costSavings: 0,
          maintenanceSchedule: '2024-02-15',
          lastCalibration: '2024-01-05',
          firmwareVersion: 'v1.5.3',
          securityStatus: 'critical'
        },
        {
          id: '5',
          name: 'Temperature Sensor 1',
          type: 'sensor',
          status: 'online',
          location: 'Living Room',
          lastReading: '5 minutes ago',
          temperature: 24,
          uptime: 99.9,
          performanceScore: 96,
          energyConsumption: 0.5,
          costSavings: 800,
          maintenanceSchedule: '2024-06-10',
          lastCalibration: '2024-01-12',
          firmwareVersion: 'v2.0.0',
          securityStatus: 'secure'
        },
        {
          id: '6',
          name: 'Smart Meter - Kitchen',
          type: 'smart-meter',
          status: 'online',
          location: 'Kitchen',
          lastReading: '1 minute ago',
          powerOutput: 1.8,
          efficiency: 93,
          uptime: 99.5,
          performanceScore: 91,
          energyConsumption: 72,
          costSavings: 12000,
          maintenanceSchedule: '2024-04-20',
          lastCalibration: '2024-01-18',
          firmwareVersion: 'v2.1.4',
          securityStatus: 'secure'
        }
      ]

      // Mock analytics data
      const mockAnalytics: DeviceAnalytics = {
        totalDevices: mockDevices.length,
        onlineDevices: mockDevices.filter(d => d.status === 'online').length,
        offlineDevices: mockDevices.filter(d => d.status === 'offline').length,
        maintenanceDevices: mockDevices.filter(d => d.status === 'maintenance').length,
        errorDevices: mockDevices.filter(d => d.status === 'error').length,
        averageEfficiency: Math.round(mockDevices.reduce((sum, d) => sum + (d.efficiency || 0), 0) / mockDevices.length),
        totalEnergyConsumption: mockDevices.reduce((sum, d) => sum + d.energyConsumption, 0),
        totalCostSavings: mockDevices.reduce((sum, d) => sum + d.costSavings, 0),
        systemUptime: Math.round(mockDevices.reduce((sum, d) => sum + d.uptime, 0) / mockDevices.length),
        securityScore: Math.round(mockDevices.filter(d => d.securityStatus === 'secure').length / mockDevices.length * 100)
      }

      // Mock performance metrics
      const mockPerformanceMetrics: PerformanceMetrics[] = mockDevices.map(device => ({
        deviceId: device.id,
        timestamp: new Date().toISOString(),
        efficiency: device.efficiency || 0,
        powerOutput: device.powerOutput || 0,
        temperature: device.temperature || 0,
        energyConsumption: device.energyConsumption
      }))

      // Mock maintenance schedule
      const mockMaintenanceSchedule: MaintenanceSchedule[] = mockDevices
        .filter(device => device.maintenanceSchedule)
        .map(device => ({
          deviceId: device.id,
          deviceName: device.name,
          nextMaintenance: device.maintenanceSchedule,
          maintenanceType: 'routine' as const,
          estimatedCost: 5000,
          priority: 'medium' as const
        }))

      setDevices(mockDevices)
      setDeviceAnalytics(mockAnalytics)
      setPerformanceMetrics(mockPerformanceMetrics)
      setMaintenanceSchedule(mockMaintenanceSchedule)
      setLoading(false)
    } catch (error) {
      console.error('Error loading devices:', error)
      setLoading(false)
    }
  }

  useEffect(() => {
    loadDevices()
  }, [])

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'online':
        return CheckCircleIcon
      case 'offline':
        return XCircleIcon
      case 'maintenance':
        return ExclamationTriangleIcon
      case 'error':
        return ExclamationTriangleIcon
      default:
        return XCircleIcon
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'online':
        return 'bg-green-100 text-green-800'
      case 'offline':
        return 'bg-gray-100 text-gray-800'
      case 'maintenance':
        return 'bg-yellow-100 text-yellow-800'
      case 'error':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  const getStatusIconColor = (status: string) => {
    switch (status) {
      case 'online':
        return 'text-green-400'
      case 'offline':
        return 'text-gray-400'
      case 'maintenance':
        return 'text-yellow-400'
      case 'error':
        return 'text-red-400'
      default:
        return 'text-gray-400'
    }
  }

  const getDeviceIcon = (type: string) => {
    switch (type) {
      case 'smart-meter':
        return BoltIcon
      case 'solar-panel':
        return SunIcon
      case 'inverter':
        return CogIcon
      case 'generator':
        return CogIcon
      case 'sensor':
        return SignalIcon
      default:
        return CogIcon
    }
  }

  const getSecurityColor = (status: string) => {
    switch (status) {
      case 'secure': return 'text-green-500 bg-green-100 dark:bg-green-900/20'
      case 'warning': return 'text-yellow-500 bg-yellow-100 dark:bg-yellow-900/20'
      case 'critical': return 'text-red-500 bg-red-100 dark:bg-red-900/20'
      default: return 'text-gray-500 bg-gray-100 dark:bg-gray-900/20'
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'text-red-600 bg-red-100 dark:bg-red-900/20'
      case 'medium': return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-900/20'
      case 'low': return 'text-green-600 bg-green-100 dark:bg-green-900/20'
      default: return 'text-gray-600 bg-gray-100 dark:bg-gray-900/20'
    }
  }

  const handleDeviceAdded = () => {
    // Refresh the devices list
    loadDevices()
  }

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="flex items-center justify-center h-64">
            <div className="relative">
              <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
              <div className="absolute inset-0 flex items-center justify-center">
                <CogIcon className="h-8 w-8 text-blue-600 animate-pulse" />
              </div>
            </div>
          </div>
        </Layout>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="space-y-6 animate-fade-in">
          {/* Header */}
          <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold mb-2">Device Management</h1>
                <p className="text-blue-200 text-lg">Monitoring and control of smart energy infrastructure</p>
                <div className="flex items-center mt-4 space-x-6">
                  <div className="flex items-center space-x-2">
                    <CheckCircleIcon className="h-5 w-5 text-green-300" />
                    <span className="text-sm">{deviceAnalytics.onlineDevices} Online</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <ExclamationTriangleIcon className="h-5 w-5 text-yellow-300" />
                    <span className="text-sm">{deviceAnalytics.maintenanceDevices} Maintenance</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <ShieldCheckIcon className="h-5 w-5 text-blue-300" />
                    <span className="text-sm">Security: {deviceAnalytics.securityScore}%</span>
                  </div>
                </div>
              </div>
              <div className="hidden md:block">
                <div className="w-24 h-24 bg-white/10 rounded-full flex items-center justify-center">
                  <CogIcon className="h-12 w-12 text-white" />
                </div>
              </div>
            </div>
          </div>

          {/* Key Performance Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Total Devices</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{deviceAnalytics.totalDevices}</p>
                </div>
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/20 rounded-xl flex items-center justify-center">
                  <CogIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">System Uptime</p>
                  <p className="text-2xl font-bold text-green-600 dark:text-green-400">{Math.round(deviceAnalytics.systemUptime)}%</p>
                </div>
                <div className="w-12 h-12 bg-green-100 dark:bg-green-900/20 rounded-xl flex items-center justify-center">
                  <ClockIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Avg Efficiency</p>
                  <p className="text-2xl font-bold text-purple-600 dark:text-purple-400">{Math.round(deviceAnalytics.averageEfficiency)}%</p>
                </div>
                <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/20 rounded-xl flex items-center justify-center">
                  <ArrowTrendingUpIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Energy Consumption</p>
                  <p className="text-2xl font-bold text-orange-600 dark:text-orange-400">{deviceAnalytics.totalEnergyConsumption} kWh</p>
                </div>
                <div className="w-12 h-12 bg-orange-100 dark:bg-orange-900/20 rounded-xl flex items-center justify-center">
                  <LightBulbIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Cost Savings</p>
                  <p className="text-2xl font-bold text-green-600 dark:text-green-400">₦{deviceAnalytics.totalCostSavings.toLocaleString()}</p>
                </div>
                <div className="w-12 h-12 bg-green-100 dark:bg-green-900/20 rounded-xl flex items-center justify-center">
                  <ArrowTrendingDownIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
              </div>
            </div>
          </div>

          {/* Performance Monitoring Chart */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Device Performance Trends</h2>
                <p className="text-gray-600 dark:text-gray-400">24-hour efficiency and power output monitoring</p>
              </div>
            </div>
            
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={performanceMetrics}>
                  <defs>
                    <linearGradient id="efficiencyGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10B981" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#10B981" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="powerGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#3B82F6" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis dataKey="timestamp" stroke="#9CA3AF" />
                  <YAxis stroke="#9CA3AF" />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: '#1F2937', 
                      border: '1px solid #374151',
                      borderRadius: '8px',
                      color: '#F9FAFB'
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="efficiency"
                    stroke="#10B981"
                    fill="url(#efficiencyGradient)"
                    name="Efficiency (%)"
                  />
                  <Area
                    type="monotone"
                    dataKey="powerOutput"
                    stroke="#3B82F6"
                    fill="url(#powerGradient)"
                    name="Power Output (kW)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Maintenance Schedule */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Maintenance Schedule</h2>
                <p className="text-gray-600 dark:text-gray-400">Upcoming device maintenance and calibration</p>
              </div>
            </div>
            
            <div className="space-y-4">
              {maintenanceSchedule.map((item, index) => (
                <div key={index} className="border border-gray-200 dark:border-gray-700 rounded-xl p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center space-x-3">
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{item.deviceName}</h3>
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getPriorityColor(item.priority)}`}>
                        {item.priority.toUpperCase()}
                      </span>
                    </div>
                    <div className="text-right">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Next Maintenance</p>
                      <p className="text-lg font-semibold text-gray-900 dark:text-white">{item.nextMaintenance}</p>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="text-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Type</p>
                      <p className="text-lg font-bold text-blue-600 dark:text-blue-400 capitalize">{item.maintenanceType}</p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Estimated Cost</p>
                      <p className="text-lg font-bold text-purple-600 dark:text-purple-400">₦{item.estimatedCost.toLocaleString()}</p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Priority</p>
                      <p className="text-lg font-bold text-orange-600 dark:text-orange-400 capitalize">{item.priority}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Device Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {devices.map((device) => {
              const IconComponent = getDeviceIcon(device.type)
              const StatusIconComponent = getStatusIcon(device.status)
              
              return (
                <div key={device.id} className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 hover:shadow-xl transition-all duration-300">
                  <div className="p-6">
                    <div className="flex items-center justify-between mb-4">
                      <div className="flex items-center space-x-3">
                        <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl flex items-center justify-center">
                          <IconComponent className="h-6 w-6 text-white" />
                        </div>
                        <div>
                          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{device.name}</h3>
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(device.status)}`}>
                            {device.status}
                          </span>
                        </div>
                      </div>
                      {StatusIconComponent && <StatusIconComponent className={`h-5 w-5 ${getStatusIconColor(device.status)}`} />}
                    </div>
                    
                    <div className="space-y-3 mb-4">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-500 dark:text-gray-400">Location:</span>
                        <span className="text-gray-900 dark:text-white">{device.location}</span>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-500 dark:text-gray-400">Last Reading:</span>
                        <span className="text-gray-900 dark:text-white">{device.lastReading}</span>
                      </div>
                      
                      {device.powerOutput !== undefined && (
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-gray-500 dark:text-gray-400">Power Output:</span>
                          <span className="text-gray-900 dark:text-white font-semibold">{device.powerOutput} kW</span>
                        </div>
                      )}
                      
                      {device.efficiency !== undefined && (
                        <div className="flex items-center justify-between text-sm">
                          <span className="text-gray-500 dark:text-gray-400">Efficiency:</span>
                          <span className="text-gray-900 dark:text-white font-semibold">{device.efficiency}%</span>
                        </div>
                      )}
                      
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-500 dark:text-gray-400">Uptime:</span>
                        <span className="text-gray-900 dark:text-white font-semibold">{device.uptime}%</span>
                      </div>
                      
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-500 dark:text-gray-400">Performance:</span>
                        <span className="text-gray-900 dark:text-white font-semibold">{device.performanceScore}%</span>
                      </div>
                      
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-500 dark:text-gray-400">Security:</span>
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getSecurityColor(device.securityStatus)}`}>
                          {device.securityStatus}
                        </span>
                      </div>
                    </div>
                    
                    <div className="space-y-2 mb-4 text-xs text-gray-500 dark:text-gray-400">
                      <div className="flex justify-between">
                        <span>Firmware:</span>
                        <span>{device.firmwareVersion}</span>
                      </div>
                      <div className="flex justify-between">
                        <span>Last Calibration:</span>
                        <span>{device.lastCalibration}</span>
                      </div>
                    </div>
                    
                    <div className="flex space-x-2">
                      <button className="flex-1 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium py-2 px-4 rounded-lg transition-colors">
                        Configure
                      </button>
                      <button className="flex-1 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 text-sm font-medium py-2 px-4 rounded-lg transition-colors hover:border-blue-500 hover:text-blue-600 dark:hover:border-blue-400 dark:hover:text-blue-400">
                        Details
                      </button>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>

          {/* Add Device Section */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700">
            <div className="p-8">
              <div className="text-center">
                <div className="w-16 h-16 bg-gradient-to-br from-green-500 to-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  <PlusIcon className="h-8 w-8 text-white" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">Add New Device</h3>
                <p className="text-gray-600 dark:text-gray-400 mb-6">Connect a new smart energy device to your network</p>
                <button 
                  onClick={() => setIsAddModalOpen(true)}
                  className="bg-green-600 hover:bg-green-700 text-white font-semibold py-3 px-6 rounded-lg transition-colors shadow-lg"
                >
                  Add Device
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Add Device Modal */}
        <AddDeviceModal
          isOpen={isAddModalOpen}
          onClose={() => setIsAddModalOpen(false)}
          onDeviceAdded={handleDeviceAdded}
        />
      </Layout>
    </ProtectedRoute>
  )
} 