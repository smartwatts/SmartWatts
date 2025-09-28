import React, { useState, useEffect } from 'react'
import { useRouter } from 'next/router'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import { usePageStyles } from '../utils/pageStyles'
import { useAuth } from '../hooks/useAuth'
import {
  ChartBarIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  BoltIcon,
  SunIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  EyeIcon,
  ArrowDownTrayIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, Area, AreaChart, BarChart, Bar, PieChart, Pie, Cell } from 'recharts'

interface AnalyticsData {
  period: string
  consumption: number
  generation: number
  efficiency: number
  cost: number
}

interface TrendData {
  metric: string
  current: number
  previous: number
  change: number
  trend: 'up' | 'down' | 'stable'
}

export default function Analytics() {
  const { user } = useAuth()
  const router = useRouter()
  const [selectedPeriod, setSelectedPeriod] = useState<'7d' | '30d' | '90d' | '1y'>('30d')

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
  
  // Generate data based on selected period
  const generateAnalyticsData = (period: string): AnalyticsData[] => {
    const baseData = {
      '7d': [
        { period: 'Mon', consumption: 12.1, generation: 8.2, efficiency: 89, cost: 42.30 },
        { period: 'Tue', consumption: 11.8, generation: 7.9, efficiency: 87, cost: 41.60 },
        { period: 'Wed', consumption: 13.2, generation: 9.1, efficiency: 91, cost: 46.20 },
        { period: 'Thu', consumption: 12.5, generation: 8.5, efficiency: 88, cost: 43.75 },
        { period: 'Fri', consumption: 14.1, generation: 6.8, efficiency: 85, cost: 49.35 },
        { period: 'Sat', consumption: 10.9, generation: 7.2, efficiency: 86, cost: 38.15 },
        { period: 'Sun', consumption: 9.8, generation: 8.8, efficiency: 92, cost: 34.30 },
      ],
      '30d': [
        { period: 'Week 1', consumption: 45.2, generation: 28.5, efficiency: 87, cost: 156.80 },
        { period: 'Week 2', consumption: 42.8, generation: 31.2, efficiency: 89, cost: 148.40 },
        { period: 'Week 3', consumption: 48.1, generation: 26.8, efficiency: 85, cost: 168.20 },
        { period: 'Week 4', consumption: 44.6, generation: 29.4, efficiency: 88, cost: 154.60 },
      ],
      '90d': [
        { period: 'Month 1', consumption: 180.5, generation: 115.2, efficiency: 88, cost: 632.75 },
        { period: 'Month 2', consumption: 175.3, generation: 128.7, efficiency: 91, cost: 614.05 },
        { period: 'Month 3', consumption: 182.1, generation: 112.4, efficiency: 86, cost: 637.35 },
      ],
      '1y': [
        { period: 'Q1', consumption: 540.2, generation: 340.8, efficiency: 88, cost: 1890.70 },
        { period: 'Q2', consumption: 520.1, generation: 385.2, efficiency: 91, cost: 1820.35 },
        { period: 'Q3', consumption: 535.8, generation: 320.5, efficiency: 87, cost: 1875.30 },
        { period: 'Q4', consumption: 510.3, generation: 295.7, efficiency: 89, cost: 1786.05 },
      ],
    }
    return baseData[period as keyof typeof baseData] || baseData['30d']
  }

  const generateTrendsData = (period: string): TrendData[] => {
    const baseTrends = {
      '7d': [
        { metric: 'Energy Consumption', current: 9.8, previous: 14.1, change: -30.5, trend: 'down' as const },
        { metric: 'Solar Generation', current: 8.8, previous: 6.8, change: 29.4, trend: 'up' as const },
        { metric: 'Efficiency Score', current: 92, previous: 85, change: 8.2, trend: 'up' as const },
        { metric: 'Energy Cost', current: 34.30, previous: 49.35, change: -30.5, trend: 'down' as const },
      ],
      '30d': [
        { metric: 'Energy Consumption', current: 44.6, previous: 48.1, change: -7.3, trend: 'down' as const },
        { metric: 'Solar Generation', current: 29.4, previous: 26.8, change: 9.7, trend: 'up' as const },
        { metric: 'Efficiency Score', current: 88, previous: 85, change: 3.5, trend: 'up' as const },
        { metric: 'Energy Cost', current: 154.60, previous: 168.20, change: -8.1, trend: 'down' as const },
      ],
      '90d': [
        { metric: 'Energy Consumption', current: 182.1, previous: 175.3, change: 3.9, trend: 'up' as const },
        { metric: 'Solar Generation', current: 112.4, previous: 128.7, change: -12.7, trend: 'down' as const },
        { metric: 'Efficiency Score', current: 86, previous: 91, change: -5.5, trend: 'down' as const },
        { metric: 'Energy Cost', current: 637.35, previous: 614.05, change: 3.8, trend: 'up' as const },
      ],
      '1y': [
        { metric: 'Energy Consumption', current: 510.3, previous: 535.8, change: -4.8, trend: 'down' as const },
        { metric: 'Solar Generation', current: 295.7, previous: 320.5, change: -7.7, trend: 'down' as const },
        { metric: 'Efficiency Score', current: 89, previous: 87, change: 2.3, trend: 'up' as const },
        { metric: 'Energy Cost', current: 1786.05, previous: 1875.30, change: -4.8, trend: 'down' as const },
      ],
    }
    return baseTrends[period as keyof typeof baseTrends] || baseTrends['30d']
  }

  const [analyticsData, setAnalyticsData] = useState<AnalyticsData[]>(generateAnalyticsData(selectedPeriod))
  const [trends, setTrends] = useState<TrendData[]>(generateTrendsData(selectedPeriod))

  // Update data when period changes
  useEffect(() => {
    const newData = generateAnalyticsData(selectedPeriod)
    const newTrends = generateTrendsData(selectedPeriod)
    setAnalyticsData(newData)
    setTrends(newTrends)
  }, [selectedPeriod])

  const styles = usePageStyles()

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'up': return <ArrowTrendingUpIcon className="h-5 w-5 text-green-600" />
      case 'down': return <ArrowTrendingDownIcon className="h-5 w-5 text-red-600" />
      default: return <ChartBarIcon className="h-5 w-5 text-gray-600" />
    }
  }

  const getTrendColor = (trend: string) => {
    switch (trend) {
      case 'up': return 'text-green-600'
      case 'down': return 'text-red-600'
      default: return 'text-gray-600'
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
                Energy Analytics
              </h1>
              <p className={`text-xl ${styles.headerText} mb-6`}>
                Deep insights into energy patterns, trends, and optimization opportunities
              </p>
              
              {/* Period Selector */}
              <div className="flex space-x-2">
                {(['7d', '30d', '90d', '1y'] as const).map((period) => (
                  <button
                    key={period}
                    onClick={() => setSelectedPeriod(period)}
                    className={`px-4 py-2 rounded-xl font-medium transition-all duration-300 ${
                      selectedPeriod === period
                        ? 'bg-white text-blue-600 shadow-lg'
                        : 'text-white hover:bg-white/20'
                    }`}
                  >
                    {period === '7d' ? '7 Days' : 
                     period === '30d' ? '30 Days' : 
                     period === '90d' ? '90 Days' : '1 Year'}
                  </button>
                ))}
              </div>
            </div>

            {/* Background Pattern */}
            <div className="absolute inset-0 opacity-10">
              <div className="absolute inset-0 bg-gradient-to-br from-purple-400 to-blue-400 rounded-3xl"></div>
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
              <p className="text-xs font-medium text-white/80">Total Consumption</p>
              <p className="text-xl font-bold text-white">180.7 kWh</p>
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
              <p className="text-xl font-bold text-white">115.9 kWh</p>
                  </div>
                </div>
              </div>

        <div className={styles.kpiCardWarm}>
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-white/20">
              <ChartBarIcon className="h-5 w-5 text-white" />
              </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-white/80">Efficiency Score</p>
              <p className="text-xl font-bold text-white">87.3%</p>
            </div>
          </div>
                </div>

        <div className={styles.kpiCardPurple}>
          <div className="flex items-center">
            <div className="p-2 rounded-lg bg-white/20">
              <ClockIcon className="h-5 w-5 text-white" />
            </div>
            <div className="ml-3">
              <p className="text-xs font-medium text-white/80">Cost Savings</p>
              <p className="text-xl font-bold text-white">$64.20</p>
                </div>
              </div>
            </div>
          </div>

      {/* Trends Section */}
      <div className={styles.section}>
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Performance Trends</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {trends.map((trend) => (
            <div key={trend.metric} className="p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-600 dark:text-gray-300">{trend.metric}</span>
                {getTrendIcon(trend.trend)}
                </div>
              <div className="text-2xl font-bold text-gray-900 dark:text-white">{trend.current}</div>
              <div className={`text-sm ${getTrendColor(trend.trend)}`}>
                {trend.change > 0 ? '+' : ''}{trend.change.toFixed(1)}% from previous
                    </div>
                  </div>
                ))}
              </div>
            </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        {/* Energy Consumption Trend */}
        <div className={styles.chart}>
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Energy Consumption Trend</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={analyticsData}>
              <XAxis dataKey="period" />
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

        {/* Efficiency vs Cost Analysis */}
        <div className={styles.chart}>
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Efficiency vs Cost</h3>
                <ResponsiveContainer width="100%" height={300}>
            <BarChart data={analyticsData}>
              <XAxis dataKey="period" />
              <YAxis yAxisId="left" />
              <YAxis yAxisId="right" orientation="right" />
              <Tooltip />
              <Bar yAxisId="left" dataKey="efficiency" fill="#10B981" name="Efficiency %" />
              <Bar yAxisId="right" dataKey="cost" fill="#F59E0B" name="Cost $" />
            </BarChart>
                </ResponsiveContainer>
                      </div>
                    </div>

      {/* Insights Section */}
      <div className={styles.section}>
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Key Insights</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-xl border border-green-200 dark:border-green-800">
            <div className="flex items-center mb-2">
              <CheckCircleIcon className="h-5 w-5 text-green-600 mr-2" />
              <h4 className="font-semibold text-green-800 dark:text-green-200">Positive Trends</h4>
            </div>
            <ul className="text-sm text-green-700 dark:text-green-300 space-y-1">
              <li>• Solar generation increased by 9.7%</li>
              <li>• Efficiency score improved by 3.5%</li>
              <li>• Monthly cost reduced by 8.1%</li>
            </ul>
          </div>

          <div className="p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-xl border border-yellow-200 dark:border-yellow-800">
            <div className="flex items-center mb-2">
              <ExclamationTriangleIcon className="h-5 w-5 text-yellow-600 mr-2" />
              <h4 className="font-semibold text-yellow-800 dark:text-yellow-200">Areas for Improvement</h4>
            </div>
            <ul className="text-sm text-yellow-700 dark:text-yellow-300 space-y-1">
              <li>• Energy consumption varies significantly</li>
              <li>• Peak demand periods identified</li>
              <li>• Optimization opportunities available</li>
            </ul>
                  </div>
                </div>
                  </div>
                  
      {/* Quick Actions */}
      <div className={styles.section}>
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <button className={styles.button}>
            <EyeIcon className="h-5 w-5 mr-2 inline" />
            Detailed Analysis
          </button>
          <button className={styles.buttonSecondary}>
            <ArrowDownTrayIcon className="h-5 w-5 mr-2 inline" />
            Export Report
          </button>
          <button className={styles.buttonSecondary}>
            <ChartBarIcon className="h-5 w-5 mr-2 inline" />
            Custom Metrics
          </button>
              </div>
            </div>
        </div>
      </Layout>
    </ProtectedRoute>
  )
}