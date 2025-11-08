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
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData[]>([])
  const [trends, setTrends] = useState<TrendData[]>([])
  const [loading, setLoading] = useState(true)

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

  // Fetch analytics data from API
  const fetchAnalyticsData = async (period: string) => {
    try {
      setLoading(true)
      const token = localStorage.getItem('token')
      const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}
      
      // Calculate date range based on period
      const now = new Date()
      let startDate: Date
      
      switch (period) {
        case '7d':
          startDate = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          break
        case '30d':
          startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
          break
        case '90d':
          startDate = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000)
          break
        case '1y':
          startDate = new Date(now.getTime() - 365 * 24 * 60 * 60 * 1000)
          break
        default:
          startDate = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      }

      // Convert to LocalDateTime format (remove timezone info)
      const formatForBackend = (date: Date) => {
        return date.toISOString().replace('Z', '')
      }
      
      const startDateStr = formatForBackend(startDate)
      const endDateStr = formatForBackend(now)
      
      const response = await fetch(
        `/api/proxy?service=analytics&path=/analytics/energy-analytics/user/${user?.id}/date-range&startDate=${encodeURIComponent(startDateStr)}&endDate=${encodeURIComponent(endDateStr)}`,
        { headers: authHeaders }
      )

      if (response.ok) {
        const data = await response.json()
        const analytics = data || []
        
        // Transform analytics data to chart format
        const chartData: AnalyticsData[] = analytics.map((item: any) => ({
          period: new Date(item.timestamp).toLocaleDateString('en-US', { 
            month: 'short', 
            day: 'numeric' 
          }),
          consumption: item.consumption || 0,
          generation: item.generation || 0,
          efficiency: item.efficiency || 0,
          cost: item.cost || 0
        }))
        
        setAnalyticsData(chartData)
      } else {
        throw new Error(`Analytics API returned error: ${response.status} ${response.statusText}`)
      }
    } catch (error) {
      console.error('Error fetching analytics data:', error)
      setAnalyticsData([])
    } finally {
      setLoading(false)
    }
  }

  const calculateTrendsData = (): TrendData[] => {
    if (!analyticsData || analyticsData.length < 2) {
      return [
        { metric: 'Energy Consumption', current: 0, previous: 0, change: 0, trend: 'stable' as const },
        { metric: 'Solar Generation', current: 0, previous: 0, change: 0, trend: 'stable' as const },
        { metric: 'Efficiency Score', current: 0, previous: 0, change: 0, trend: 'stable' as const },
        { metric: 'Energy Cost', current: 0, previous: 0, change: 0, trend: 'stable' as const },
      ]
    }

    // Calculate current period (last half of data) vs previous period (first half of data)
    const midPoint = Math.floor(analyticsData.length / 2)
    const currentPeriod = analyticsData.slice(midPoint)
    const previousPeriod = analyticsData.slice(0, midPoint)

    const calculateMetricTrend = (currentData: AnalyticsData[], previousData: AnalyticsData[], metric: keyof AnalyticsData) => {
      const current = currentData.reduce((sum, item) => sum + (item[metric] as number), 0)
      const previous = previousData.reduce((sum, item) => sum + (item[metric] as number), 0)
      const change = previous > 0 ? ((current - previous) / previous) * 100 : 0
      const trend = change > 5 ? 'up' : change < -5 ? 'down' : 'stable'
      return { current, previous, change: Math.round(change * 10) / 10, trend }
    }

    const consumptionTrend = calculateMetricTrend(currentPeriod, previousPeriod, 'consumption')
    const generationTrend = calculateMetricTrend(currentPeriod, previousPeriod, 'generation')
    const efficiencyTrend = calculateMetricTrend(currentPeriod, previousPeriod, 'efficiency')
    const costTrend = calculateMetricTrend(currentPeriod, previousPeriod, 'cost')

    return [
      { 
        metric: 'Energy Consumption', 
        current: consumptionTrend.current, 
        previous: consumptionTrend.previous, 
        change: consumptionTrend.change, 
        trend: consumptionTrend.trend as 'up' | 'down' | 'stable'
      },
      { 
        metric: 'Solar Generation', 
        current: generationTrend.current, 
        previous: generationTrend.previous, 
        change: generationTrend.change, 
        trend: generationTrend.trend as 'up' | 'down' | 'stable'
      },
      { 
        metric: 'Efficiency Score', 
        current: efficiencyTrend.current, 
        previous: efficiencyTrend.previous, 
        change: efficiencyTrend.change, 
        trend: efficiencyTrend.trend as 'up' | 'down' | 'stable'
      },
      { 
        metric: 'Energy Cost', 
        current: costTrend.current, 
        previous: costTrend.previous, 
        change: costTrend.change, 
        trend: costTrend.trend as 'up' | 'down' | 'stable'
      },
    ]
  }

  // Update data when period changes
  useEffect(() => {
    if (user?.id) {
      fetchAnalyticsData(selectedPeriod)
    }
  }, [selectedPeriod, user?.id])

  // Calculate trends when analytics data changes
  useEffect(() => {
    const newTrends = calculateTrendsData()
    setTrends(newTrends)
  }, [analyticsData])

  // Calculate dynamic KPI values based on current data
  const calculateKPIs = () => {
    if (!analyticsData || analyticsData.length === 0) {
      return {
        totalConsumption: 0,
        solarGeneration: 0,
        efficiencyScore: 0,
        costSavings: 0
      }
    }

    const totalConsumption = analyticsData.reduce((sum, item) => sum + item.consumption, 0)
    const totalGeneration = analyticsData.reduce((sum, item) => sum + item.generation, 0)
    const totalEfficiency = analyticsData.reduce((sum, item) => sum + item.efficiency, 0)
    const totalCost = analyticsData.reduce((sum, item) => sum + item.cost, 0)
    
    // Calculate average efficiency score
    const avgEfficiency = analyticsData.length > 0 ? totalEfficiency / analyticsData.length : 0
    
    // Calculate cost savings (assuming 20% savings from solar generation)
    const costSavings = totalGeneration * 0.2 * 50 // Assuming ₦50 per kWh savings

    return {
      totalConsumption: totalConsumption,
      solarGeneration: totalGeneration,
      efficiencyScore: Math.round(avgEfficiency),
      costSavings: costSavings
    }
  }

  const kpiValues = calculateKPIs()

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
              <h1 className="text-3xl font-bold mb-2">Energy Analytics</h1>
              <p className="text-blue-200 text-lg">Deep insights into energy patterns, trends, and optimization opportunities</p>
              
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
              <p className="text-xs font-medium text-white/80">Total Consumption</p>
              <p className="text-xl font-bold text-white">{kpiValues.totalConsumption.toFixed(1)} kWh</p>
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
              <p className="text-xl font-bold text-white">{kpiValues.solarGeneration.toFixed(1)} kWh</p>
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
              <p className="text-xl font-bold text-white">{kpiValues.efficiencyScore}%</p>
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
              <p className="text-xl font-bold text-white">₦{kpiValues.costSavings.toFixed(2)}</p>
                </div>
              </div>
            </div>
          </div>

      {/* Trends Section */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Performance Trends</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {trends.map((trend) => (
            <div key={trend.metric} className="p-4 bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700">
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
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
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
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
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
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Key Insights</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="p-4 bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700">
            <div className="flex items-center mb-2">
              <CheckCircleIcon className="h-5 w-5 text-green-600 mr-2" />
              <h4 className="font-semibold text-gray-900 dark:text-white">Positive Trends</h4>
            </div>
            <ul className="text-sm text-gray-600 dark:text-gray-300 space-y-1">
              <li>• Solar generation increased by 9.7%</li>
              <li>• Efficiency score improved by 3.5%</li>
              <li>• Monthly cost reduced by 8.1%</li>
            </ul>
          </div>

          <div className="p-4 bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700">
            <div className="flex items-center mb-2">
              <ExclamationTriangleIcon className="h-5 w-5 text-yellow-600 mr-2" />
              <h4 className="font-semibold text-gray-900 dark:text-white">Areas for Improvement</h4>
            </div>
            <ul className="text-sm text-gray-600 dark:text-gray-300 space-y-1">
              <li>• Energy consumption varies significantly</li>
              <li>• Peak demand periods identified</li>
              <li>• Optimization opportunities available</li>
            </ul>
                  </div>
                </div>
                  </div>
                  
      {/* Quick Actions */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6 mb-6">
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