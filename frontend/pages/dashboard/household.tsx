'use client'

import React, { useState, useEffect } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { apiClient } from '../../utils/api-client'
import ErrorBoundary from '../../components/ErrorBoundary'
import ServiceUnavailable from '../../components/ServiceUnavailable'
import EmptyState, { EmptyStates } from '../../components/EmptyState'
import {
  BoltIcon,
  SunIcon,
  CurrencyDollarIcon,
  ExclamationTriangleIcon,
  ClockIcon,
  FireIcon,
  WifiIcon,
  LightBulbIcon,
  HomeIcon,
  ChartBarIcon
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts'

interface HouseholdEnergyData {
  time: string
  consumption: number
  cost: number
}

interface HouseholdStats {
  currentConsumption: number
  monthlyCost: number
  solarGeneration: number
  generatorRuntime: number
  voltageQuality: number
  tokenBalance: number
  daysUntilDepletion: number
}

interface EnergySource {
  name: string
  value: number
  color: string
}

interface TopAppliance {
  name: string
  consumption: number
  cost: number
  percentage: number
}

export default function HouseholdDashboard() {
  const [energyData, setEnergyData] = useState<HouseholdEnergyData[]>([])
  const [stats, setStats] = useState<HouseholdStats>({
    currentConsumption: 0,
    monthlyCost: 0,
    solarGeneration: 0,
    generatorRuntime: 0,
    voltageQuality: 0,
    tokenBalance: 0,
    daysUntilDepletion: 0
  })
  const [energySources, setEnergySources] = useState<EnergySource[]>([])
  const [topAppliances, setTopAppliances] = useState<TopAppliance[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [serviceErrors, setServiceErrors] = useState<Record<string, boolean>>({})

  const { user } = useAuth()

  useEffect(() => {
    const loadHouseholdData = async () => {
      try {
        setError(null)
        setServiceErrors({})
        
        const token = localStorage.getItem('token')
        const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}
        
        // Load household-specific data
        const [energyResponse, statsResponse, sourcesResponse, appliancesResponse] = await Promise.allSettled([
          apiClient.proxy('energy', `/energy/readings/user/${user?.id}`, { method: 'GET', headers: authHeaders }),
          apiClient.proxy('analytics', '/analytics/dashboard-stats', { method: 'GET', headers: authHeaders }),
          apiClient.proxy('energy', `/energy/source-breakdown/${user?.id}`, { method: 'GET', headers: authHeaders }),
          apiClient.proxy('analytics', '/analytics/device-consumption', { method: 'GET', headers: authHeaders })
        ])

        // Handle energy data
        const energyData = energyResponse.status === 'fulfilled' ? energyResponse.value : null
        const energyReadings = energyData?.content || []
        
        const chartData: HouseholdEnergyData[] = energyReadings.map((reading: any) => ({
          time: new Date(reading.timestamp).toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
          }),
          consumption: reading.consumption || 0,
          cost: reading.cost || 0
        }))

        // Handle stats data
        const statsData = statsResponse.status === 'fulfilled' ? statsResponse.value : null
        const householdStats: HouseholdStats = {
          currentConsumption: chartData.length > 0 ? chartData[chartData.length - 1].consumption : 0,
          monthlyCost: statsData?.monthlyCost || chartData.reduce((sum, item) => sum + item.cost, 0),
          solarGeneration: statsData?.solarGeneration || 0,
          generatorRuntime: statsData?.generatorRuntime || 0,
          voltageQuality: statsData?.voltageQuality || 0,
          tokenBalance: statsData?.tokenBalance || 0,
          daysUntilDepletion: statsData?.daysUntilDepletion || 0
        }

        // Handle energy sources
        const sourcesData = sourcesResponse.status === 'fulfilled' ? sourcesResponse.value : null
        const sources: EnergySource[] = sourcesData || [
          { name: 'Grid', value: 60, color: '#3B82F6' },
          { name: 'Solar', value: 30, color: '#F59E0B' },
          { name: 'Generator', value: 10, color: '#EF4444' }
        ]

        // Handle top appliances
        const appliancesData = appliancesResponse.status === 'fulfilled' ? appliancesResponse.value : null
        const appliances: TopAppliance[] = appliancesData?.slice(0, 5) || []

        // Track service errors
        if (energyResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, energy: true }))
        if (statsResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, analytics: true }))
        if (sourcesResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, energy: true }))
        if (appliancesResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, analytics: true }))

        setEnergyData(chartData)
        setStats(householdStats)
        setEnergySources(sources)
        setTopAppliances(appliances)
        setIsLoading(false)
      } catch (error) {
        console.error('Error loading household data:', error)
        setError(error instanceof Error ? error.message : 'Failed to load household data')
        setIsLoading(false)
      }
    }

    loadHouseholdData()
  }, [user?.id])

  // Handle error states
  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
        <ServiceUnavailable 
          service="Household Dashboard" 
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
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-4 text-xl text-gray-600">Loading your household energy data...</p>
        </div>
      </div>
    )
  }

  // Handle empty data state
  const hasNoData = energyData.length === 0 && stats.currentConsumption === 0
  if (hasNoData) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
        <EmptyStates.EnergyData />
      </div>
    )
  }

  return (
    <ErrorBoundary>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-blue-800 rounded-2xl shadow-xl p-8 text-white mb-8">
          <div className="flex items-center space-x-4 mb-6">
            <div className="p-3 bg-white/20 rounded-xl">
              <HomeIcon className="h-8 w-8" />
            </div>
            <div>
              <h1 className="text-3xl font-bold">Household Energy Dashboard</h1>
              <p className="text-blue-100">Simple energy monitoring for your home</p>
            </div>
          </div>
          
          {/* Quick Stats */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Current Usage</p>
              <p className="text-2xl font-bold">{stats.currentConsumption} kW</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Monthly Cost</p>
              <p className="text-2xl font-bold">₦{stats.monthlyCost.toLocaleString()}</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Solar Generation</p>
              <p className="text-2xl font-bold">{stats.solarGeneration} kWh</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Token Balance</p>
              <p className="text-2xl font-bold">{stats.tokenBalance} kWh</p>
            </div>
          </div>
        </div>

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Energy Consumption Chart */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-blue-500 rounded-xl">
                <ChartBarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Energy Consumption</h3>
            </div>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={energyData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis />
                  <Tooltip 
                    formatter={(value, name) => [
                      name === 'consumption' ? `${value} kW` : `₦${value}`,
                      name === 'consumption' ? 'Consumption' : 'Cost'
                    ]}
                  />
                  <Line type="monotone" dataKey="consumption" stroke="#3B82F6" strokeWidth={2} />
                  <Line type="monotone" dataKey="cost" stroke="#F59E0B" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Energy Sources Breakdown */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-green-500 rounded-xl">
                <SunIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Energy Sources</h3>
            </div>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={energySources}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {energySources.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => `${value}%`} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="mt-4 space-y-2">
              {energySources.map((source, index) => (
                <div key={index} className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <div className="w-3 h-3 rounded-full" style={{ backgroundColor: source.color }}></div>
                    <span className="text-gray-600 dark:text-gray-400">{source.name}</span>
                  </div>
                  <span className="font-semibold text-gray-900 dark:text-white">{source.value}%</span>
                </div>
              ))}
            </div>
          </div>

          {/* Top Energy-Consuming Appliances */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-purple-500 rounded-xl">
                <LightBulbIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Top Appliances</h3>
            </div>
            <div className="space-y-4">
              {topAppliances.map((appliance, index) => (
                <div key={index} className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-blue-100 dark:bg-blue-900 rounded-lg flex items-center justify-center">
                      <span className="text-sm font-bold text-blue-600 dark:text-blue-400">#{index + 1}</span>
                    </div>
                    <div>
                      <p className="font-semibold text-gray-900 dark:text-white">{appliance.name}</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400">{appliance.consumption} kW</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-gray-900 dark:text-white">₦{appliance.cost}</p>
                    <p className="text-sm text-gray-600 dark:text-gray-400">{appliance.percentage}%</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Nigerian-Specific Features */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-orange-500 rounded-xl">
                <CurrencyDollarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Prepaid & Generator</h3>
            </div>
            <div className="space-y-6">
              {/* Prepaid Token Status */}
              <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-green-800 dark:text-green-200">Token Balance</span>
                  <span className="text-lg font-bold text-green-900 dark:text-green-100">{stats.tokenBalance} kWh</span>
                </div>
                <div className="w-full bg-green-200 dark:bg-green-800 rounded-full h-2">
                  <div 
                    className="bg-green-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${Math.min((stats.tokenBalance / 100) * 100, 100)}%` }}
                  ></div>
                </div>
                <p className="text-xs text-green-700 dark:text-green-300 mt-1">
                  {stats.daysUntilDepletion} days remaining
                </p>
              </div>

              {/* Generator Status */}
              <div className="p-4 bg-red-50 dark:bg-red-900/20 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-red-800 dark:text-red-200">Generator Runtime</span>
                  <span className="text-lg font-bold text-red-900 dark:text-red-100">{stats.generatorRuntime} hrs</span>
                </div>
                <p className="text-xs text-red-700 dark:text-red-300">
                  Last 30 days
                </p>
              </div>

              {/* Voltage Quality */}
              <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm font-medium text-blue-800 dark:text-blue-200">Voltage Quality</span>
                  <span className="text-lg font-bold text-blue-900 dark:text-blue-100">{stats.voltageQuality}V</span>
                </div>
                <div className="flex items-center space-x-2">
                  <div className={`w-2 h-2 rounded-full ${
                    stats.voltageQuality >= 220 && stats.voltageQuality <= 240 ? 'bg-green-500' : 'bg-red-500'
                  }`}></div>
                  <span className="text-xs text-blue-700 dark:text-blue-300">
                    {stats.voltageQuality >= 220 && stats.voltageQuality <= 240 ? 'Good' : 'Poor'}
                  </span>
                </div>
              </div>
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








