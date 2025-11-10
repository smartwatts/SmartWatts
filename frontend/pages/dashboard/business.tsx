'use client'

import React, { useState, useEffect } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { apiClient } from '../../utils/api-client'
import ErrorBoundary from '../../components/ErrorBoundary'
import ServiceUnavailable from '../../components/ServiceUnavailable'
import EmptyState, { EmptyStates } from '../../components/EmptyState'
import {
  BuildingOfficeIcon,
  ChartBarIcon,
  CurrencyDollarIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  WrenchScrewdriverIcon,
  LightBulbIcon,
  SunIcon,
  BoltIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, AreaChart, Area } from 'recharts'

interface BusinessEnergyData {
  time: string
  consumption: number
  cost: number
  peakDemand: number
}

interface BusinessStats {
  totalConsumption: number
  monthlyCost: number
  peakDemand: number
  efficiency: number
  costPerLocation: number
  solarGeneration: number
  carbonFootprint: number
  roi: number
}

interface LocationData {
  name: string
  consumption: number
  cost: number
  efficiency: number
  devices: number
}

interface EquipmentHealth {
  name: string
  status: 'excellent' | 'good' | 'fair' | 'poor'
  lastMaintenance: string
  nextMaintenance: string
  efficiency: number
}

interface CostOptimization {
  title: string
  potentialSavings: number
  implementationCost: number
  paybackPeriod: number
  priority: 'high' | 'medium' | 'low'
}

export default function BusinessDashboard() {
  const [energyData, setEnergyData] = useState<BusinessEnergyData[]>([])
  const [stats, setStats] = useState<BusinessStats>({
    totalConsumption: 0,
    monthlyCost: 0,
    peakDemand: 0,
    efficiency: 0,
    costPerLocation: 0,
    solarGeneration: 0,
    carbonFootprint: 0,
    roi: 0
  })
  const [locations, setLocations] = useState<LocationData[]>([])
  const [equipmentHealth, setEquipmentHealth] = useState<EquipmentHealth[]>([])
  const [costOptimizations, setCostOptimizations] = useState<CostOptimization[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [serviceErrors, setServiceErrors] = useState<Record<string, boolean>>({})

  const { user } = useAuth()

  useEffect(() => {
    const loadBusinessData = async () => {
      try {
        setError(null)
        setServiceErrors({})
        
        const token = localStorage.getItem('token')
        const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}
        
        // Load business-specific data
        const [energyResponse, statsResponse, locationsResponse, equipmentResponse, optimizationsResponse] = await Promise.allSettled([
          apiClient.proxy('energy', `/energy/readings/user/${user?.id}`, { method: 'GET', headers: authHeaders }),
          apiClient.proxy('analytics', '/analytics/dashboard-stats', { method: 'GET', headers: authHeaders }),
          apiClient.proxy('facility', `/facilities/user/${user?.id}`, { method: 'GET', headers: authHeaders }),
          apiClient.proxy('device', `/devices/health`, { method: 'GET', headers: authHeaders }),
          apiClient.proxy('analytics', '/analytics/cost-optimizations', { method: 'GET', headers: authHeaders })
        ])

        // Handle energy data
        const energyData = energyResponse.status === 'fulfilled' ? energyResponse.value : null
        const energyReadings = energyData?.content || []
        
        const chartData: BusinessEnergyData[] = energyReadings.map((reading: any) => ({
          time: new Date(reading.timestamp).toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
          }),
          consumption: reading.consumption || 0,
          cost: reading.cost || 0,
          peakDemand: reading.peakDemand || 0
        }))

        // Handle stats data
        const statsData = statsResponse.status === 'fulfilled' ? statsResponse.value : null
        const businessStats: BusinessStats = {
          totalConsumption: statsData?.totalConsumption || chartData.reduce((sum, item) => sum + item.consumption, 0),
          monthlyCost: statsData?.monthlyCost || chartData.reduce((sum, item) => sum + item.cost, 0),
          peakDemand: statsData?.peakDemand || Math.max(...chartData.map(item => item.peakDemand), 0),
          efficiency: statsData?.efficiency || 0,
          costPerLocation: statsData?.costPerLocation || 0,
          solarGeneration: statsData?.solarGeneration || 0,
          carbonFootprint: statsData?.carbonFootprint || 0,
          roi: statsData?.roi || 0
        }

        // Handle locations data
        const locationsData = locationsResponse.status === 'fulfilled' ? locationsResponse.value : null
        const locationsList: LocationData[] = locationsData || []

        // Handle equipment health
        const equipmentData = equipmentResponse.status === 'fulfilled' ? equipmentResponse.value : null
        const equipmentList: EquipmentHealth[] = equipmentData || []

        // Handle cost optimizations
        const optimizationsData = optimizationsResponse.status === 'fulfilled' ? optimizationsResponse.value : null
        const optimizationsList: CostOptimization[] = optimizationsData || []

        // Track service errors
        if (energyResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, energy: true }))
        if (statsResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, analytics: true }))
        if (locationsResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, facility: true }))
        if (equipmentResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, device: true }))
        if (optimizationsResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, analytics: true }))

        setEnergyData(chartData)
        setStats(businessStats)
        setLocations(locationsList)
        setEquipmentHealth(equipmentList)
        setCostOptimizations(optimizationsList)
        setIsLoading(false)
      } catch (error) {
        console.error('Error loading business data:', error)
        setError(error instanceof Error ? error.message : 'Failed to load business data')
        setIsLoading(false)
      }
    }

    loadBusinessData()
  }, [user?.id])

  // Handle error states
  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
        <ServiceUnavailable 
          service="Business Dashboard" 
          onRetry={() => {
            setError(null)
            setServiceErrors({})
            setIsLoading(true)
            window.location.reload()
          }}
        />
      </div>
    )
  }

  // Handle loading state
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-green-500 mx-auto"></div>
          <p className="mt-4 text-xl text-gray-600">Loading your business energy data...</p>
        </div>
      </div>
    )
  }

  // Handle empty data state
  const hasNoData = energyData.length === 0 && stats.totalConsumption === 0
  if (hasNoData) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
        <EmptyStates.EnergyData />
      </div>
    )
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'excellent': return 'text-green-600 bg-green-100'
      case 'good': return 'text-blue-600 bg-blue-100'
      case 'fair': return 'text-yellow-600 bg-yellow-100'
      case 'poor': return 'text-red-600 bg-red-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'text-red-600 bg-red-100'
      case 'medium': return 'text-yellow-600 bg-yellow-100'
      case 'low': return 'text-green-600 bg-green-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  return (
    <ErrorBoundary>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
        {/* Header */}
        <div className="bg-gradient-to-r from-green-600 to-green-800 rounded-2xl shadow-xl p-8 text-white mb-8">
          <div className="flex items-center space-x-4 mb-6">
            <div className="p-3 bg-white/20 rounded-xl">
              <BuildingOfficeIcon className="h-8 w-8" />
            </div>
            <div>
              <h1 className="text-3xl font-bold">Business Energy Dashboard</h1>
              <p className="text-green-100">Multi-location energy management and optimization</p>
            </div>
          </div>
          
          {/* Business KPIs */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Total Consumption</p>
              <p className="text-2xl font-bold">{stats.totalConsumption} kW</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Monthly Cost</p>
              <p className="text-2xl font-bold">₦{stats.monthlyCost.toLocaleString()}</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Peak Demand</p>
              <p className="text-2xl font-bold">{stats.peakDemand} kW</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Efficiency</p>
              <p className="text-2xl font-bold">{stats.efficiency}%</p>
            </div>
          </div>
        </div>

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Energy Consumption Trend */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-blue-500 rounded-xl">
                <ChartBarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Energy Consumption Trend</h3>
            </div>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={energyData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis />
                  <Tooltip 
                    formatter={(value, name) => [
                      name === 'consumption' ? `${value} kW` : `₦${value}`,
                      name === 'consumption' ? 'Consumption' : 'Cost'
                    ]}
                  />
                  <Area type="monotone" dataKey="consumption" stackId="1" stroke="#3B82F6" fill="#3B82F6" fillOpacity={0.6} />
                  <Area type="monotone" dataKey="cost" stackId="2" stroke="#F59E0B" fill="#F59E0B" fillOpacity={0.6} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Peak Demand Analysis */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-red-500 rounded-xl">
                <BoltIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Peak Demand Analysis</h3>
            </div>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={energyData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis />
                  <Tooltip formatter={(value) => [`${value} kW`, 'Peak Demand']} />
                  <Bar dataKey="peakDemand" fill="#EF4444" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Multi-Location Management */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Locations Overview */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-purple-500 rounded-xl">
                <BuildingOfficeIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Locations</h3>
            </div>
            <div className="space-y-4">
              {locations.map((location, index) => (
                <div key={index} className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-semibold text-gray-900 dark:text-white">{location.name}</h4>
                    <span className="text-sm text-gray-600 dark:text-gray-400">{location.devices} devices</span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Consumption</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{location.consumption} kW</p>
                    </div>
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Cost</p>
                      <p className="font-semibold text-gray-900 dark:text-white">₦{location.cost}</p>
                    </div>
                  </div>
                  <div className="mt-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600 dark:text-gray-400">Efficiency</span>
                      <span className="font-semibold text-gray-900 dark:text-white">{location.efficiency}%</span>
                    </div>
                    <div className="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2 mt-1">
                      <div 
                        className="bg-green-500 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${location.efficiency}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Equipment Health */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-orange-500 rounded-xl">
                <WrenchScrewdriverIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Equipment Health</h3>
            </div>
            <div className="space-y-4">
              {equipmentHealth.map((equipment, index) => (
                <div key={index} className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-semibold text-gray-900 dark:text-white">{equipment.name}</h4>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(equipment.status)}`}>
                      {equipment.status}
                    </span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Efficiency</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{equipment.efficiency}%</p>
                    </div>
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Next Maintenance</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{equipment.nextMaintenance}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Cost Optimizations */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-green-500 rounded-xl">
                <ArrowTrendingUpIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Cost Optimizations</h3>
            </div>
            <div className="space-y-4">
              {costOptimizations.map((optimization, index) => (
                <div key={index} className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-semibold text-gray-900 dark:text-white">{optimization.title}</h4>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getPriorityColor(optimization.priority)}`}>
                      {optimization.priority}
                    </span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Potential Savings</p>
                      <p className="font-semibold text-gray-900 dark:text-white">₦{optimization.potentialSavings}</p>
                    </div>
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Payback Period</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{optimization.paybackPeriod} months</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* ESG & ROI Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 bg-green-500 rounded-xl">
                <SunIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Solar Generation</h3>
            </div>
            <div className="text-center">
              <p className="text-3xl font-bold text-green-600">{stats.solarGeneration} kWh</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">This month</p>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 bg-blue-500 rounded-xl">
                <LightBulbIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Carbon Footprint</h3>
            </div>
            <div className="text-center">
              <p className="text-3xl font-bold text-blue-600">{stats.carbonFootprint} kg CO₂</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">This month</p>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 bg-purple-500 rounded-xl">
                <CurrencyDollarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">ROI</h3>
            </div>
            <div className="text-center">
              <p className="text-3xl font-bold text-purple-600">{stats.roi}%</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">Energy investments</p>
            </div>
          </div>
        </div>

        {/* Service Error Indicators */}
        {Object.keys(serviceErrors).length > 0 && (
          <div className="mt-8 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-xl p-4">
            <div className="flex items-center space-x-2">
              <ExclamationTriangleIcon className="h-5 w-5 text-yellow-600" />
              <span className="text-sm font-medium text-yellow-800 dark:text-yellow-200">
                Some services are temporarily unavailable: {Object.keys(serviceErrors).join(', ')}
              </span>
            </div>
          </div>
        )}
      </div>
    </ErrorBoundary>
  )
}







