import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import { usePageStyles } from '../utils/pageStyles'
import { useAuth } from '../hooks/useAuth'
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
  // Generate data based on selected timeframe
  const generateEnergyData = (timeframe: string): EnergyData[] => {
    const baseData = {
      '1h': [
        { time: '00:00', consumption: 2.1, generation: 0, cost: 0.42 },
        { time: '00:15', consumption: 2.0, generation: 0, cost: 0.40 },
        { time: '00:30', consumption: 1.9, generation: 0, cost: 0.38 },
        { time: '00:45', consumption: 1.8, generation: 0, cost: 0.36 },
      ],
      '24h': [
        { time: '00:00', consumption: 2.1, generation: 0, cost: 0.42 },
        { time: '04:00', consumption: 1.8, generation: 0, cost: 0.36 },
        { time: '08:00', consumption: 4.2, generation: 1.5, cost: 0.54 },
        { time: '12:00', consumption: 5.8, generation: 3.2, cost: 0.52 },
        { time: '16:00', consumption: 6.1, generation: 2.8, cost: 0.66 },
        { time: '20:00', consumption: 4.9, generation: 0, cost: 0.98 },
        { time: '24:00', consumption: 2.3, generation: 0, cost: 0.46 }
      ],
      '7d': [
        { time: 'Mon', consumption: 45.2, generation: 28.5, cost: 156.80 },
        { time: 'Tue', consumption: 42.8, generation: 31.2, cost: 148.40 },
        { time: 'Wed', consumption: 48.1, generation: 26.8, cost: 168.20 },
        { time: 'Thu', consumption: 44.6, generation: 29.4, cost: 154.60 },
        { time: 'Fri', consumption: 46.3, generation: 27.1, cost: 161.05 },
        { time: 'Sat', consumption: 38.9, generation: 22.3, cost: 135.15 },
        { time: 'Sun', consumption: 41.2, generation: 25.7, cost: 143.20 }
      ],
      '30d': [
        { time: 'Week 1', consumption: 315.4, generation: 199.5, cost: 1097.60 },
        { time: 'Week 2', consumption: 299.6, generation: 218.4, cost: 1038.80 },
        { time: 'Week 3', consumption: 336.7, generation: 187.6, cost: 1177.40 },
        { time: 'Week 4', consumption: 312.2, generation: 205.8, cost: 1082.20 }
      ]
    }
    return baseData[timeframe as keyof typeof baseData] || baseData['24h']
  }

  const [energyData, setEnergyData] = useState<EnergyData[]>(generateEnergyData(selectedTimeframe))

  // Update data when timeframe changes
  useEffect(() => {
    const newData = generateEnergyData(selectedTimeframe)
    setEnergyData(newData)
  }, [selectedTimeframe])

  const [alerts] = useState<EnergyAlert[]>([
    { id: '1', message: 'Peak demand approaching threshold', severity: 'warning', timestamp: '2 minutes ago' },
    { id: '2', message: 'Solar generation optimal', severity: 'info', timestamp: '15 minutes ago' },
    { id: '3', message: 'Off-peak rate active', severity: 'info', timestamp: '1 hour ago' }
  ])

  const styles = usePageStyles()

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
              <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-4">
                Energy Monitor
              </h1>
              <p className={`text-xl ${styles.headerText} mb-6`}>
                Real-time energy consumption, generation, and cost monitoring
              </p>
              
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
              <div className="absolute inset-0 bg-gradient-to-br from-blue-400 to-green-400 rounded-3xl"></div>
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
              <p className="text-xl font-bold text-white">4.2 kW</p>
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
              <p className="text-xl font-bold text-white">1.5 kW</p>
                    </div>
                  </div>
          </div>

        <div className={styles.kpiCardWarm}>
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-white/20">
              <ChartBarIcon className="h-5 w-5 text-white" />
            </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-white/80">Monthly Cost</p>
              <p className="text-xl font-bold text-white">$245.60</p>
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
              <p className="text-xl font-bold text-white">87%</p>
                </div>
              </div>
            </div>
          </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Energy Consumption Chart */}
        <div className={styles.chart}>
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
        <div className={styles.chart}>
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
      <div className={styles.section}>
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
      <div className={styles.section}>
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