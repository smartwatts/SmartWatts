'use client'

import React, { useState, useEffect } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { apiClient } from '../../utils/api-client'
import ErrorBoundary from '../../components/ErrorBoundary'
import ServiceUnavailable from '../../components/ServiceUnavailable'
import EmptyState, { EmptyStates } from '../../components/EmptyState'
import {
  BuildingOffice2Icon,
  ChartBarIcon,
  CurrencyDollarIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  CpuChipIcon,
  LightBulbIcon,
  SunIcon,
  BoltIcon,
  TrendingUpIcon,
  TrendingDownIcon,
  GlobeAltIcon,
  ShieldCheckIcon,
  ArrowPathIcon
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, AreaChart, Area, ScatterChart, Scatter } from 'recharts'

interface EnterpriseEnergyData {
  time: string
  consumption: number
  cost: number
  peakDemand: number
  carbonFootprint: number
  gridStability: number
}

interface EnterpriseStats {
  totalConsumption: number
  monthlyCost: number
  peakDemand: number
  efficiency: number
  carbonFootprint: number
  gridStability: number
  demandResponseParticipation: number
  renewablePercentage: number
  costPerTenant: number
  apiIntegrations: number
}

interface TenantData {
  name: string
  consumption: number
  cost: number
  efficiency: number
  carbonFootprint: number
  lastUpdated: string
}

interface GridStability {
  voltage: number
  frequency: number
  powerFactor: number
  harmonics: number
  status: 'stable' | 'unstable' | 'critical'
}

interface DemandResponse {
  eventId: string
  startTime: string
  endTime: string
  reductionTarget: number
  actualReduction: number
  status: 'scheduled' | 'active' | 'completed'
  reward: number
}

interface AdvancedAnalytics {
  forecastAccuracy: number
  anomalyDetection: number
  optimizationScore: number
  predictiveMaintenance: number
}

export default function EnterpriseDashboard() {
  const [energyData, setEnergyData] = useState<EnterpriseEnergyData[]>([])
  const [stats, setStats] = useState<EnterpriseStats>({
    totalConsumption: 0,
    monthlyCost: 0,
    peakDemand: 0,
    efficiency: 0,
    carbonFootprint: 0,
    gridStability: 0,
    demandResponseParticipation: 0,
    renewablePercentage: 0,
    costPerTenant: 0,
    apiIntegrations: 0
  })
  const [tenants, setTenants] = useState<TenantData[]>([])
  const [gridStability, setGridStability] = useState<GridStability>({
    voltage: 0,
    frequency: 0,
    powerFactor: 0,
    harmonics: 0,
    status: 'stable'
  })
  const [demandResponse, setDemandResponse] = useState<DemandResponse[]>([])
  const [advancedAnalytics, setAdvancedAnalytics] = useState<AdvancedAnalytics>({
    forecastAccuracy: 0,
    anomalyDetection: 0,
    optimizationScore: 0,
    predictiveMaintenance: 0
  })
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [serviceErrors, setServiceErrors] = useState<Record<string, boolean>>({})

  const { user } = useAuth()

  useEffect(() => {
    const loadEnterpriseData = async () => {
      try {
        setError(null)
        setServiceErrors({})
        
        const token = localStorage.getItem('token')
        const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}
        
        // Load enterprise-specific data
        const [energyResponse, statsResponse, tenantsResponse, gridResponse, demandResponse, analyticsResponse] = await Promise.allSettled([
          apiClient.proxy('energy', `/energy/readings/user/${user?.id}`, { method: 'GET', headers: authHeaders }),
          apiClient.proxy('analytics', '/analytics/dashboard-stats', { method: 'GET', headers: authHeaders }),
          apiClient.proxy('user', `/users/tenants`, { method: 'GET', headers: authHeaders }),
          apiClient.proxy('energy', '/energy/grid-stability', { method: 'GET', headers: authHeaders }),
          apiClient.proxy('energy', '/energy/demand-response', { method: 'GET', headers: authHeaders }),
          apiClient.proxy('analytics', '/analytics/advanced', { method: 'GET', headers: authHeaders })
        ])

        // Handle energy data
        const energyData = energyResponse.status === 'fulfilled' ? energyResponse.value : null
        const energyReadings = energyData?.content || []
        
        const chartData: EnterpriseEnergyData[] = energyReadings.map((reading: any) => ({
          time: new Date(reading.timestamp).toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
          }),
          consumption: reading.consumption || 0,
          cost: reading.cost || 0,
          peakDemand: reading.peakDemand || 0,
          carbonFootprint: reading.carbonFootprint || 0,
          gridStability: reading.gridStability || 0
        }))

        // Handle stats data
        const statsData = statsResponse.status === 'fulfilled' ? statsResponse.value : null
        const enterpriseStats: EnterpriseStats = {
          totalConsumption: statsData?.totalConsumption || chartData.reduce((sum, item) => sum + item.consumption, 0),
          monthlyCost: statsData?.monthlyCost || chartData.reduce((sum, item) => sum + item.cost, 0),
          peakDemand: statsData?.peakDemand || Math.max(...chartData.map(item => item.peakDemand), 0),
          efficiency: statsData?.efficiency || 0,
          carbonFootprint: statsData?.carbonFootprint || 0,
          gridStability: statsData?.gridStability || 0,
          demandResponseParticipation: statsData?.demandResponseParticipation || 0,
          renewablePercentage: statsData?.renewablePercentage || 0,
          costPerTenant: statsData?.costPerTenant || 0,
          apiIntegrations: statsData?.apiIntegrations || 0
        }

        // Handle tenants data
        const tenantsData = tenantsResponse.status === 'fulfilled' ? tenantsResponse.value : null
        const tenantsList: TenantData[] = tenantsData || []

        // Handle grid stability
        const gridData = gridResponse.status === 'fulfilled' ? gridResponse.value : null
        const gridStabilityData: GridStability = gridData || {
          voltage: 0,
          frequency: 0,
          powerFactor: 0,
          harmonics: 0,
          status: 'stable'
        }

        // Handle demand response
        const demandData = demandResponse.status === 'fulfilled' ? demandResponse.value : null
        const demandResponseList: DemandResponse[] = demandData || []

        // Handle advanced analytics
        const analyticsData = analyticsResponse.status === 'fulfilled' ? analyticsResponse.value : null
        const analyticsList: AdvancedAnalytics = analyticsData || {
          forecastAccuracy: 0,
          anomalyDetection: 0,
          optimizationScore: 0,
          predictiveMaintenance: 0
        }

        // Track service errors
        if (energyResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, energy: true }))
        if (statsResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, analytics: true }))
        if (tenantsResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, user: true }))
        if (gridResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, energy: true }))
        if (demandResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, energy: true }))
        if (analyticsResponse.status === 'rejected') setServiceErrors(prev => ({ ...prev, analytics: true }))

        setEnergyData(chartData)
        setStats(enterpriseStats)
        setTenants(tenantsList)
        setGridStability(gridStabilityData)
        setDemandResponse(demandResponseList)
        setAdvancedAnalytics(analyticsList)
        setIsLoading(false)
      } catch (error) {
        console.error('Error loading enterprise data:', error)
        setError(error instanceof Error ? error.message : 'Failed to load enterprise data')
        setIsLoading(false)
      }
    }

    loadEnterpriseData()
  }, [user?.id])

  // Handle error states
  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
        <ServiceUnavailable 
          service="Enterprise Dashboard" 
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
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-purple-500 mx-auto"></div>
          <p className="mt-4 text-xl text-gray-600">Loading your enterprise energy data...</p>
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
      case 'stable': return 'text-green-600 bg-green-100'
      case 'unstable': return 'text-yellow-600 bg-yellow-100'
      case 'critical': return 'text-red-600 bg-red-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  const getEventStatusColor = (status: string) => {
    switch (status) {
      case 'scheduled': return 'text-blue-600 bg-blue-100'
      case 'active': return 'text-green-600 bg-green-100'
      case 'completed': return 'text-gray-600 bg-gray-100'
      default: return 'text-gray-600 bg-gray-100'
    }
  }

  return (
    <ErrorBoundary>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
        {/* Header */}
        <div className="bg-gradient-to-r from-purple-600 to-purple-800 rounded-2xl shadow-xl p-8 text-white mb-8">
          <div className="flex items-center space-x-4 mb-6">
            <div className="p-3 bg-white/20 rounded-xl">
              <BuildingOffice2Icon className="h-8 w-8" />
            </div>
            <div>
              <h1 className="text-3xl font-bold">Enterprise Energy Dashboard</h1>
              <p className="text-purple-100">Advanced analytics and multi-tenant management</p>
            </div>
          </div>
          
          {/* Enterprise KPIs */}
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
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
              <p className="text-white/80 text-sm">Grid Stability</p>
              <p className="text-2xl font-bold">{stats.gridStability}%</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Renewable %</p>
              <p className="text-2xl font-bold">{stats.renewablePercentage}%</p>
            </div>
          </div>
        </div>

        {/* Advanced Analytics Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Energy Consumption with Carbon Footprint */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-blue-500 rounded-xl">
                <ChartBarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Energy & Carbon Analysis</h3>
            </div>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={energyData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="time" />
                  <YAxis yAxisId="left" />
                  <YAxis yAxisId="right" orientation="right" />
                  <Tooltip 
                    formatter={(value, name) => [
                      name === 'consumption' ? `${value} kW` : 
                      name === 'carbonFootprint' ? `${value} kg CO₂` : `₦${value}`,
                      name === 'consumption' ? 'Consumption' : 
                      name === 'carbonFootprint' ? 'Carbon Footprint' : 'Cost'
                    ]}
                  />
                  <Line yAxisId="left" type="monotone" dataKey="consumption" stroke="#3B82F6" strokeWidth={2} />
                  <Line yAxisId="left" type="monotone" dataKey="carbonFootprint" stroke="#10B981" strokeWidth={2} />
                  <Line yAxisId="right" type="monotone" dataKey="cost" stroke="#F59E0B" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Grid Stability Analysis */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-green-500 rounded-xl">
                <ShieldCheckIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Grid Stability</h3>
            </div>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Voltage</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{gridStability.voltage}V</p>
                </div>
                <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Frequency</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{gridStability.frequency}Hz</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Power Factor</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{gridStability.powerFactor}</p>
                </div>
                <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <p className="text-sm text-gray-600 dark:text-gray-400">Harmonics</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{gridStability.harmonics}%</p>
                </div>
              </div>
              <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Status</span>
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(gridStability.status)}`}>
                    {gridStability.status}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Multi-Tenant Management */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
          {/* Tenants Overview */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-purple-500 rounded-xl">
                <BuildingOffice2Icon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Tenants</h3>
            </div>
            <div className="space-y-4">
              {tenants.map((tenant, index) => (
                <div key={index} className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-semibold text-gray-900 dark:text-white">{tenant.name}</h4>
                    <span className="text-xs text-gray-600 dark:text-gray-400">{tenant.lastUpdated}</span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Consumption</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{tenant.consumption} kW</p>
                    </div>
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Cost</p>
                      <p className="font-semibold text-gray-900 dark:text-white">₦{tenant.cost}</p>
                    </div>
                  </div>
                  <div className="mt-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600 dark:text-gray-400">Efficiency</span>
                      <span className="font-semibold text-gray-900 dark:text-white">{tenant.efficiency}%</span>
                    </div>
                    <div className="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2 mt-1">
                      <div 
                        className="bg-green-500 h-2 rounded-full transition-all duration-300"
                        style={{ width: `${tenant.efficiency}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Demand Response Events */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-orange-500 rounded-xl">
                <ArrowPathIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Demand Response</h3>
            </div>
            <div className="space-y-4">
              {demandResponse.map((event, index) => (
                <div key={index} className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-semibold text-gray-900 dark:text-white">{event.eventId}</h4>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEventStatusColor(event.status)}`}>
                      {event.status}
                    </span>
                  </div>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Target Reduction</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{event.reductionTarget} kW</p>
                    </div>
                    <div>
                      <p className="text-gray-600 dark:text-gray-400">Actual Reduction</p>
                      <p className="font-semibold text-gray-900 dark:text-white">{event.actualReduction} kW</p>
                    </div>
                  </div>
                  <div className="mt-2">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600 dark:text-gray-400">Reward</span>
                      <span className="font-semibold text-gray-900 dark:text-white">₦{event.reward}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Advanced Analytics */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-green-500 rounded-xl">
                <CpuChipIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">AI Analytics</h3>
            </div>
            <div className="space-y-4">
              <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Forecast Accuracy</span>
                  <span className="font-semibold text-gray-900 dark:text-white">{advancedAnalytics.forecastAccuracy}%</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2">
                  <div 
                    className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${advancedAnalytics.forecastAccuracy}%` }}
                  ></div>
                </div>
              </div>
              <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Anomaly Detection</span>
                  <span className="font-semibold text-gray-900 dark:text-white">{advancedAnalytics.anomalyDetection}%</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2">
                  <div 
                    className="bg-green-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${advancedAnalytics.anomalyDetection}%` }}
                  ></div>
                </div>
              </div>
              <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Optimization Score</span>
                  <span className="font-semibold text-gray-900 dark:text-white">{advancedAnalytics.optimizationScore}%</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2">
                  <div 
                    className="bg-purple-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${advancedAnalytics.optimizationScore}%` }}
                  ></div>
                </div>
              </div>
              <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-sm text-gray-600 dark:text-gray-400">Predictive Maintenance</span>
                  <span className="font-semibold text-gray-900 dark:text-white">{advancedAnalytics.predictiveMaintenance}%</span>
                </div>
                <div className="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2">
                  <div 
                    className="bg-orange-500 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${advancedAnalytics.predictiveMaintenance}%` }}
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ESG & API Integration Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 bg-green-500 rounded-xl">
                <SunIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Carbon Footprint</h3>
            </div>
            <div className="text-center">
              <p className="text-3xl font-bold text-green-600">{stats.carbonFootprint} kg CO₂</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">This month</p>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 bg-blue-500 rounded-xl">
                <GlobeAltIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Demand Response</h3>
            </div>
            <div className="text-center">
              <p className="text-3xl font-bold text-blue-600">{stats.demandResponseParticipation}%</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">Participation rate</p>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 bg-purple-500 rounded-xl">
                <CurrencyDollarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Cost per Tenant</h3>
            </div>
            <div className="text-center">
              <p className="text-3xl font-bold text-purple-600">₦{stats.costPerTenant}</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">Average</p>
            </div>
          </div>

          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl p-6">
            <div className="flex items-center space-x-3 mb-4">
              <div className="p-2 bg-orange-500 rounded-xl">
                <CpuChipIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">API Integrations</h3>
            </div>
            <div className="text-center">
              <p className="text-3xl font-bold text-orange-600">{stats.apiIntegrations}</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">Active connections</p>
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







