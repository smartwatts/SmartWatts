import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import { usePageStyles } from '../utils/pageStyles'
import { useAuth } from '../hooks/useAuth'
import { isAdmin } from '../utils/roles'
import {
  BoltIcon,
  SunIcon,
  ChartBarIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, Area, AreaChart, BarChart, Bar, Cell } from 'recharts'

interface EnergyData {
  time: string
  consumption: number
  generation: number
  cost: number
}

interface EnergyAlert {
  id: string
  message: string
  severity: 'critical' | 'warning' | 'info'
  timestamp: string
}

export default function EnergyMonitor() {
  const { user } = useAuth()
  const router = useRouter()
  const [selectedTimeframe, setSelectedTimeframe] = useState<'1h' | '24h' | '7d' | '30d'>('24h')
  const [energyData, setEnergyData] = useState<EnergyData[]>([])
  const [loading, setLoading] = useState(true)

  // Fetch energy data from API
  const fetchEnergyData = async (timeframe: string) => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}
      
      // Calculate time range based on timeframe
      const now = new Date()
      let startTime: Date
      
      switch (timeframe) {
        case '1h':
          startTime = new Date(now.getTime() - 60 * 60 * 1000) // 1 hour ago
          break
        case '24h':
          startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000) // 24 hours ago
          break
        case '7d':
          startTime = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000) // 7 days ago
          break
        case '30d':
          startTime = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000) // 30 days ago
          break
        default:
          startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000)
      }

      // Convert to LocalDateTime format (remove timezone info)
      const formatForBackend = (date: Date) => {
        return date.toISOString().replace('Z', '')
      }
      
      const response = await fetch(
        `/api/proxy?service=energy&path=/energy/readings/user/${user?.id}/time-range&startTime=${encodeURIComponent(formatForBackend(startTime))}&endTime=${encodeURIComponent(formatForBackend(now))}`,
        { headers: authHeaders }
      )

      if (response.ok) {
        const data = await response.json()
        const readings = data || []
        
        // Transform readings to chart data format
        const chartData: EnergyData[] = readings.map((reading: any) => ({
          time: new Date(reading.timestamp).toLocaleTimeString('en-US', { 
            hour: '2-digit', 
            minute: '2-digit',
            hour12: false 
          }),
          consumption: reading.consumption || 0,
          generation: reading.generation || 0,
          cost: reading.cost || 0
        }))
        
        setEnergyData(chartData)
      } else {
        // Fallback to empty data if API fails
        setEnergyData([])
      }
    } catch (error) {
      console.error('Error fetching energy data:', error)
      setEnergyData([])
    } finally {
      setLoading(false)
    }
  }

  // Update data when timeframe changes
  useEffect(() => {
    if (user?.id) {
      fetchEnergyData(selectedTimeframe)
    }
  }, [selectedTimeframe, user?.id])

  // Calculate dynamic KPI values based on current data
  const calculateKPIs = () => {
    if (!energyData || energyData.length === 0) {
      return {
        currentConsumption: 0,
        solarGeneration: 0,
        monthlyCost: 0,
        efficiencyScore: 0
      }
    }

    const latestData = energyData[energyData.length - 1]
    const totalConsumption = energyData.reduce((sum, item) => sum + item.consumption, 0)
    const totalGeneration = energyData.reduce((sum, item) => sum + item.generation, 0)
    const totalCost = energyData.reduce((sum, item) => sum + item.cost, 0)
    
    // Calculate efficiency score based on generation vs consumption
    const efficiencyScore = totalConsumption > 0 ? Math.round((totalGeneration / totalConsumption) * 100) : 0

    return {
      currentConsumption: latestData.consumption,
      solarGeneration: latestData.generation,
      monthlyCost: totalCost,
      efficiencyScore: Math.min(efficiencyScore, 100) // Cap at 100%
    }
  }

  const kpiValues = calculateKPIs()

  const [alerts] = useState<EnergyAlert[]>([
    { id: '1', message: 'Peak demand approaching threshold', severity: 'warning', timestamp: '2 minutes ago' },
    { id: '2', message: 'Solar generation optimal', severity: 'info', timestamp: '15 minutes ago' },
    { id: '3', message: 'Off-peak rate active', severity: 'info', timestamp: '1 hour ago' }
  ])

  const styles = usePageStyles()

  useEffect(() => {
    // Redirect admins to admin dashboard
    if (isAdmin(user?.role)) {
      router.replace('/admin/dashboard')
    }
  }, [user, router])

  // Don't render customer pages for admins
  if (isAdmin(user?.role)) {
    return null
  }

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'critical': return 'text-red-600 bg-red-100 dark:bg-red-900/20'
      case 'warning': return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-900/20'
      case 'info': return 'text-blue-600 bg-blue-100 dark:bg-blue-900/20'
      default: return 'text-gray-600 bg-gray-100 dark:bg-gray-900/20'
    }
  }

  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case 'critical': return <ExclamationTriangleIcon className="h-5 w-5 text-red-600" />
      case 'warning': return <ExclamationTriangleIcon className="h-5 w-5 text-yellow-600" />
      case 'info': return <CheckCircleIcon className="h-5 w-5 text-blue-600" />
      default: return <CheckCircleIcon className="h-5 w-5 text-gray-600" />
    }
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Enhanced Header */}
          <div className={`${styles.header} mb-8`}>
            <div className="relative z-10">
              <h1 className="text-3xl font-bold mb-2">Energy Monitor</h1>
              <p className="text-blue-200 text-lg">Real-time energy consumption, generation, and cost monitoring</p>
              
              {/* Timeframe Selector */}
              <div className="flex space-x-2">
                {(['1h', '24h', '7d', '30d'] as const).map((timeframe) => (
                  <button
                    key={timeframe}
                    onClick={() => setSelectedTimeframe(timeframe)}
                    className={`px-4 py-2 rounded-xl font-medium transition-all duration-300 ${
                      selectedTimeframe === timeframe
                        ? 'bg-white text-blue-600 shadow-lg'
                        : 'text-white hover:bg-white/20'
                    }`}
                  >
                    {timeframe === '1h' ? '1 Hour' : 
                     timeframe === '24h' ? '24 Hours' : 
                     timeframe === '7d' ? '7 Days' : '30 Days'}
                  </button>
                ))}
              </div>
            </div>
            
            {/* Background Pattern */}
            <div className="absolute inset-0 opacity-10">
              <div className="absolute inset-0 bg-gradient-to-br from-blue-500/20 to-blue-600/20 rounded-3xl"></div>
            </div>
          </div>

          {/* Key Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div className={styles.kpiCard}>
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-white/20">
              <BoltIcon className="h-5 w-5 text-white" />
                      </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-white/80">Current Consumption</p>
              <p className="text-xl font-bold text-white">{kpiValues.currentConsumption.toFixed(1)} kW</p>
                      </div>
                    </div>
                  </div>

        <div className={styles.kpiCardAlt}>
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-white/20">
              <SunIcon className="h-5 w-5 text-white" />
                  </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-white/80">Solar Generation</p>
              <p className="text-xl font-bold text-white">{kpiValues.solarGeneration.toFixed(1)} kW</p>
                    </div>
                  </div>
          </div>

        <div className={styles.kpiCardWarm}>
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-white/20">
              <ChartBarIcon className="h-5 w-5 text-white" />
            </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-white/80">Total Cost</p>
              <p className="text-xl font-bold text-white">₦{kpiValues.monthlyCost.toFixed(2)}</p>
            </div>
          </div>
                </div>

        <div className={styles.kpiCardPurple}>
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-white/20">
              <ClockIcon className="h-5 w-5 text-white" />
            </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-white/80">Efficiency Score</p>
              <p className="text-xl font-bold text-white">{kpiValues.efficiencyScore}%</p>
                </div>
              </div>
            </div>
          </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Energy Consumption Chart */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Energy Consumption</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={energyData}>
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
                  <Area
                    type="monotone"
                dataKey="consumption" 
                    stroke="#3B82F6"
                fill="#3B82F6" 
                fillOpacity={0.3}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Cost Analysis Chart */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Cost Analysis</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={energyData}>
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="cost" fill="#10B981" />
            </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

      {/* Alerts Section */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Energy Alerts</h3>
        <div className="space-y-3">
          {alerts.map((alert) => (
            <div key={alert.id} className={`flex items-center p-4 rounded-xl ${getSeverityColor(alert.severity)}`}>
              {getSeverityIcon(alert.severity)}
              <div className="ml-3 flex-1">
                <p className="text-sm font-medium">{alert.message}</p>
                <p className="text-xs opacity-75">{alert.timestamp}</p>
              </div>
            </div>
          ))}
                </div>
              </div>

      {/* Quick Actions */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className={styles.button}>
            <ArrowTrendingUpIcon className="h-5 w-5 mr-2 inline" />
            View Detailed Report
          </button>
          <button className={styles.buttonSecondary}>
            <ChartBarIcon className="h-5 w-5 mr-2 inline" />
            Export Data
          </button>
          <button className={styles.buttonSecondary}>
            <ExclamationTriangleIcon className="h-5 w-5 mr-2 inline" />
            Set Alerts
          </button>
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  )
} 