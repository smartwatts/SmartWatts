import React, { useState, useEffect } from 'react'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import {
  ChartBarIcon,
  BoltIcon,
  SunIcon,
  CreditCardIcon,
  LightBulbIcon,
  ClockIcon,
  BanknotesIcon,
  FireIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  XCircleIcon,
  CogIcon,
  UserIcon,
  BellIcon,
  ShieldCheckIcon,
  WrenchScrewdriverIcon,
  EyeIcon,
  PlusIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Area, AreaChart, BarChart, Bar, PieChart, Pie, Cell } from 'recharts'

interface EnergyData {
  timestamp: string
  consumption: number
  cost: number
  efficiency: number
}

interface DashboardStats {
  currentConsumption: number
  monthlyCost: number
  solarGeneration: number
  offPeakSavings: number
  costSavings: number
  peakDemand: number
  efficiencyScore: number
  carbonFootprint: number
}

interface CostOptimization {
  category: string
  currentCost: number
  potentialSavings: number
  priority: 'high' | 'medium' | 'low'
  recommendations: string[]
}

interface EnergyEfficiency {
  metric: string
  value: number
  target: number
  status: 'excellent' | 'good' | 'fair' | 'poor'
  trend: 'up' | 'down' | 'stable'
}

interface EnergyAlert {
  id: string
  type: 'warning' | 'error' | 'info' | 'success'
  message: string
  timestamp: string
  severity: 'low' | 'medium' | 'high'
}

interface EnergyForecast {
  period: string
  predictedConsumption: number
  confidence: number
  factors: string[]
}

interface SmartRecommendation {
  category: string
  title: string
  description: string
  impact: 'high' | 'medium' | 'low'
  implementation: 'easy' | 'moderate' | 'complex'
  estimatedSavings: number
}

export default function DashboardOption2() {
  const [energyData, setEnergyData] = useState<EnergyData[]>([])
  const [stats, setStats] = useState<DashboardStats>({
    currentConsumption: 0,
    monthlyCost: 0,
    solarGeneration: 0,
    offPeakSavings: 0,
    costSavings: 0,
    peakDemand: 0,
    efficiencyScore: 0,
    carbonFootprint: 0
  })
  const [costOptimizations, setCostOptimizations] = useState<CostOptimization[]>([])
  const [efficiencyMetrics, setEfficiencyMetrics] = useState<EnergyEfficiency[]>([])
  const [energyAlerts, setEnergyAlerts] = useState<EnergyAlert[]>([])
  const [energyForecasts, setEnergyForecasts] = useState<EnergyForecast[]>([])
  const [smartRecommendations, setSmartRecommendations] = useState<SmartRecommendation[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        // Mock data for demonstration
        const mockEnergyData: EnergyData[] = [
          { timestamp: '00:00', consumption: 2.1, cost: 52.5, efficiency: 92 },
          { timestamp: '04:00', consumption: 1.8, cost: 45.0, efficiency: 95 },
          { timestamp: '08:00', consumption: 3.2, cost: 80.0, efficiency: 87 },
          { timestamp: '12:00', consumption: 4.5, cost: 112.5, efficiency: 89 },
          { timestamp: '16:00', consumption: 3.8, cost: 95.0, efficiency: 91 },
          { timestamp: '20:00', consumption: 2.5, cost: 62.5, efficiency: 78 },
          { timestamp: '24:00', consumption: 2.0, cost: 50.0, efficiency: 94 },
        ]

        const mockStats: DashboardStats = {
          currentConsumption: 3.2,
          monthlyCost: 45000,
          solarGeneration: 2.8,
          offPeakSavings: 8500,
          costSavings: 12000,
          peakDemand: 4.5,
          efficiencyScore: 87,
          carbonFootprint: 125
        }

        const mockCostOptimizations: CostOptimization[] = [
          {
            category: 'Peak Demand Management',
            currentCost: 15000,
            potentialSavings: 5000,
            priority: 'high',
            recommendations: [
              'Implement load shifting to off-peak hours',
              'Install smart thermostats for HVAC optimization',
              'Schedule heavy equipment operations during off-peak periods'
            ]
          },
          {
            category: 'Solar Integration',
            currentCost: 8000,
            potentialSavings: 3000,
            priority: 'medium',
            recommendations: [
              'Expand solar panel capacity by 20%',
              'Implement battery storage system',
              'Optimize solar panel positioning'
            ]
          },
          {
            category: 'Equipment Efficiency',
            currentCost: 12000,
            potentialSavings: 4000,
            priority: 'high',
            recommendations: [
              'Replace outdated HVAC systems',
              'Install LED lighting throughout facility',
              'Implement smart power strips'
            ]
          }
        ]

        const mockEfficiencyMetrics: EnergyEfficiency[] = [
          { metric: 'Power Factor', value: 0.95, target: 0.98, status: 'good', trend: 'up' },
          { metric: 'Load Factor', value: 0.72, target: 0.85, status: 'fair', trend: 'up' },
          { metric: 'Efficiency Ratio', value: 0.87, target: 0.90, status: 'good', trend: 'stable' },
          { metric: 'Peak Demand', value: 4.5, target: 3.8, status: 'fair', trend: 'down' }
        ]

        const mockEnergyAlerts: EnergyAlert[] = [
          { id: '1', type: 'warning', message: 'Peak demand approaching threshold', timestamp: '2 minutes ago', severity: 'medium' },
          { id: '2', type: 'info', message: 'Solar generation optimal', timestamp: '5 minutes ago', severity: 'low' },
          { id: '3', type: 'success', message: 'Load shifting completed successfully', timestamp: '10 minutes ago', severity: 'low' }
        ]

        const mockEnergyForecasts: EnergyForecast[] = [
          { period: 'Next Hour', predictedConsumption: 3.8, confidence: 0.92, factors: ['Weather conditions', 'Historical patterns'] },
          { period: 'Next Day', predictedConsumption: 85, confidence: 0.85, factors: ['Weekend patterns', 'Weather forecast'] },
          { period: 'Next Week', predictedConsumption: 580, confidence: 0.78, factors: ['Seasonal trends', 'Equipment maintenance'] }
        ]

        const mockSmartRecommendations: SmartRecommendation[] = [
          {
            category: 'Immediate',
            title: 'Activate Load Shifting',
            description: 'Shift 15% of current load to off-peak hours',
            impact: 'high',
            implementation: 'easy',
            estimatedSavings: 2500
          },
          {
            category: 'Short-term',
            title: 'HVAC Optimization',
            description: 'Adjust temperature setpoints by 2°C',
            impact: 'medium',
            implementation: 'easy',
            estimatedSavings: 1800
          },
          {
            category: 'Long-term',
            title: 'Solar Expansion',
            description: 'Increase solar capacity by 5kW',
            impact: 'high',
            implementation: 'complex',
            estimatedSavings: 8000
          }
        ]

        setEnergyData(mockEnergyData)
        setStats(mockStats)
        setCostOptimizations(mockCostOptimizations)
        setEfficiencyMetrics(mockEfficiencyMetrics)
        setEnergyAlerts(mockEnergyAlerts)
        setEnergyForecasts(mockEnergyForecasts)
        setSmartRecommendations(mockSmartRecommendations)
        setLoading(false)
      } catch (error) {
        console.error('Error loading dashboard data:', error)
        setLoading(false)
      }
    }

    loadDashboardData()
  }, [])

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'bg-red-100 text-red-800 dark:bg-red-900/20 dark:text-red-400'
      case 'medium': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/20 dark:text-yellow-400'
      case 'low': return 'bg-green-100 text-green-800 dark:bg-green-900/20 dark:text-green-400'
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-900/20 dark:text-gray-400'
    }
  }

  const getEfficiencyStatusColor = (status: string) => {
    switch (status) {
      case 'excellent': return 'text-green-600 dark:text-green-400'
      case 'good': return 'text-blue-600 dark:text-blue-400'
      case 'fair': return 'text-yellow-600 dark:text-yellow-400'
      case 'poor': return 'text-red-600 dark:text-red-400'
      default: return 'text-gray-600 dark:text-gray-400'
    }
  }

  const getTrendIcon = (trend: string) => {
    switch (trend) {
      case 'up':
        return <ArrowTrendingUpIcon className="h-5 w-5 text-green-600 dark:text-green-400" />
      case 'down':
        return <ArrowTrendingDownIcon className="h-5 w-5 text-red-600 dark:text-red-400" />
      case 'stable':
        return <div className="h-5 w-5 text-gray-600 dark:text-gray-400">—</div>
      default:
        return <div className="h-5 w-5 text-gray-600 dark:text-gray-400">—</div>
    }
  }

  const getAlertTypeColor = (type: string) => {
    switch (type) {
      case 'warning': return 'text-yellow-600 dark:text-yellow-400'
      case 'error': return 'text-red-600 dark:text-red-400'
      case 'info': return 'text-blue-600 dark:text-blue-400'
      case 'success': return 'text-green-600 dark:text-green-400'
      default: return 'text-gray-600 dark:text-gray-400'
    }
  }

  const getAlertIcon = (type: string) => {
    switch (type) {
      case 'warning': return <ExclamationTriangleIcon className="h-5 w-5 text-yellow-500" />
      case 'error': return <XCircleIcon className="h-5 w-5 text-red-500" />
      case 'info': return <EyeIcon className="h-5 w-5 text-blue-500" />
      case 'success': return <CheckCircleIcon className="h-5 w-5 text-green-500" />
      default: return <BellIcon className="h-5 w-5 text-gray-500" />
    }
  }

  const getImpactColor = (impact: string) => {
    switch (impact) {
      case 'high': return 'text-red-600 dark:text-red-400'
      case 'medium': return 'text-yellow-600 dark:text-yellow-400'
      case 'low': return 'text-green-600 dark:text-green-400'
      default: return 'text-gray-600 dark:text-gray-400'
    }
  }

  const getImplementationColor = (implementation: string) => {
    switch (implementation) {
      case 'easy': return 'text-green-600 dark:text-green-400'
      case 'moderate': return 'text-yellow-600 dark:text-yellow-400'
      case 'complex': return 'text-red-600 dark:text-red-400'
      default: return 'text-gray-600 dark:text-gray-400'
    }
  }

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'Immediate': return 'text-red-600 dark:text-red-400'
      case 'Short-term': return 'text-yellow-600 dark:text-yellow-400'
      case 'Long-term': return 'text-blue-600 dark:text-blue-400'
      default: return 'text-gray-600 dark:text-gray-400'
    }
  }

  const weeklyData = [
    { day: 'Mon', consumption: 85, efficiency: 87 },
    { day: 'Tue', consumption: 78, efficiency: 92 },
    { day: 'Wed', consumption: 92, efficiency: 85 },
    { day: 'Thu', consumption: 88, efficiency: 89 },
    { day: 'Fri', consumption: 95, efficiency: 82 },
    { day: 'Sat', consumption: 72, efficiency: 94 },
    { day: 'Sun', consumption: 68, efficiency: 96 }
  ]

  const energySources = [
    { name: 'Grid Power', value: 45, color: '#3B82F6' },
    { name: 'Solar Generation', value: 35, color: '#F59E0B' },
    { name: 'Battery Storage', value: 15, color: '#10B981' },
    { name: 'Generator', value: 5, color: '#8B5CF6' }
  ]

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="flex items-center justify-center min-h-screen">
            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
          </div>
        </Layout>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="space-y-6 animate-fade-in">
          {/* Header - Option 2: Warm Beige/Cream Theme */}
          <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold mb-2">Energy Intelligence Dashboard</h1>
                <p className="text-blue-200 text-lg">Comprehensive energy analytics and optimization insights</p>
                <div className="flex items-center mt-4 space-x-6">
                  <div className="flex items-center space-x-2">
                    <ClockIcon className="h-5 w-5 text-amber-200 dark:text-blue-300" />
                    <span className="text-sm">Last updated: {new Date().toLocaleTimeString()}</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <BanknotesIcon className="h-5 w-5 text-amber-200 dark:text-green-300" />
                    <span className="text-sm">Monthly Savings: ₦{stats.costSavings.toLocaleString()}</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <FireIcon className="h-5 w-5 text-amber-200 dark:text-yellow-300" />
                    <span className="text-sm">Peak Demand: {stats.peakDemand} kW</span>
                  </div>
                </div>
              </div>
              <div className="hidden md:block">
                <div className="w-24 h-24 bg-white/20 dark:bg-white/10 rounded-full flex items-center justify-center">
                  <ChartBarIcon className="h-12 w-12 text-white" />
                </div>
              </div>
            </div>
          </div>

          {/* Key Performance Indicators - Warm Theme */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-gradient-to-br from-amber-50 to-orange-50 dark:from-blue-500/20 dark:to-blue-600/20 backdrop-blur-sm rounded-2xl p-6 border border-amber-200 dark:border-blue-500/30">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-amber-700 dark:text-blue-300">Current Consumption</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.currentConsumption} kW</p>
                </div>
                <BoltIcon className="h-8 w-8 text-amber-600 dark:text-blue-400" />
              </div>
              <div className="mt-2">
                <span className="text-xs text-amber-600 dark:text-blue-300">Efficiency Score: {stats.efficiencyScore}%</span>
              </div>
            </div>

            <div className="bg-gradient-to-br from-amber-50 to-orange-50 dark:from-green-500/20 dark:to-green-600/20 backdrop-blur-sm rounded-2xl p-6 border border-amber-200 dark:border-green-500/30">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-amber-700 dark:text-green-300">Monthly Cost</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{stats.monthlyCost.toLocaleString()}</p>
                </div>
                <CreditCardIcon className="h-8 w-8 text-amber-600 dark:text-green-400" />
              </div>
              <div className="mt-2">
                <span className="text-xs text-amber-600 dark:text-green-300">Savings: ₦{stats.costSavings.toLocaleString()}</span>
              </div>
            </div>

            <div className="bg-gradient-to-br from-amber-50 to-orange-50 dark:from-orange-500/20 dark:to-orange-600/20 backdrop-blur-sm rounded-2xl p-6 border border-amber-200 dark:border-orange-500/30">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-amber-700 dark:text-orange-300">Solar Generation</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">{stats.solarGeneration} kW</p>
                </div>
                <SunIcon className="h-8 w-8 text-amber-600 dark:text-orange-400" />
              </div>
              <div className="mt-2">
                <span className="text-xs text-amber-600 dark:text-orange-300">Carbon Offset: {stats.carbonFootprint} kg CO2</span>
              </div>
            </div>

            <div className="bg-gradient-to-br from-amber-50 to-orange-50 dark:from-purple-500/20 dark:to-purple-600/20 backdrop-blur-sm rounded-2xl p-6 border border-amber-200 dark:border-purple-500/30">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-amber-700 dark:text-purple-300">Off-Peak Savings</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{stats.offPeakSavings.toLocaleString()}</p>
                </div>
                <LightBulbIcon className="h-8 w-8 text-amber-600 dark:text-purple-400" />
              </div>
              <div className="mt-2">
                <span className="text-xs text-amber-600 dark:text-purple-300">Smart scheduling enabled</span>
              </div>
            </div>
          </div>

          {/* Charts Section - Warm Theme */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Real-time Energy Consumption */}
            <div className="bg-white dark:bg-gray-800/80 backdrop-blur-sm rounded-3xl shadow-xl border border-amber-200 dark:border-gray-700 hover:shadow-2xl transition-all duration-300">
              <div className="p-8 border-b border-amber-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Real-time Energy Consumption</h3>
                    <p className="text-gray-600 dark:text-gray-400">24-hour power consumption monitoring with cost analysis</p>
                  </div>
                </div>
              </div>
              <div className="p-8">
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={energyData}>
                    <defs>
                      <linearGradient id="energyGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#F59E0B" stopOpacity={0.4}/>
                        <stop offset="95%" stopColor="#F59E0B" stopOpacity={0.05}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" className="dark:stroke-gray-600" opacity={0.5} />
                    <XAxis dataKey="timestamp" stroke="#6B7280" className="dark:stroke-gray-400" fontSize={12} />
                    <YAxis stroke="#6B7280" className="dark:stroke-gray-400" fontSize={12} />
                    <Tooltip 
                      contentStyle={{
                        backgroundColor: 'rgba(255, 255, 255, 0.95)',
                        border: '1px solid rgba(229, 231, 235, 0.5)',
                        borderRadius: '16px',
                        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
                        backdropFilter: 'blur(10px)',
                        color: '#374151'
                      }}
                    />
                    <Area
                      type="monotone"
                      dataKey="consumption"
                      stroke="#F59E0B"
                      strokeWidth={3}
                      fill="url(#energyGradient)"
                      name="Consumption (kW)"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Weekly Energy Trend */}
            <div className="bg-white dark:bg-gray-800/80 backdrop-blur-sm rounded-3xl shadow-xl border border-amber-200 dark:border-gray-700 hover:shadow-2xl transition-all duration-300">
              <div className="p-8 border-b border-amber-200 dark:border-gray-700">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">Weekly Energy Efficiency</h3>
                    <p className="text-gray-600 dark:text-gray-400">7-day consumption pattern with efficiency metrics</p>
                  </div>
                </div>
              </div>
              <div className="p-8">
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={weeklyData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" className="dark:stroke-gray-600" opacity={0.5} />
                    <XAxis dataKey="day" stroke="#6B7280" className="dark:stroke-gray-400" fontSize={12} />
                    <YAxis stroke="#6B7280" className="dark:stroke-gray-400" fontSize={12} />
                    <Tooltip 
                      contentStyle={{
                        backgroundColor: 'rgba(255, 255, 255, 0.95)',
                        border: '1px solid rgba(229, 231, 235, 0.5)',
                        borderRadius: '16px',
                        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
                        backdropFilter: 'blur(10px)',
                        color: '#374151'
                      }}
                    />
                    <Bar dataKey="consumption" fill="#F59E0B" name="Consumption (kW)" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          {/* Theme Info */}
          <div className="bg-gradient-to-br from-amber-50 to-orange-50 dark:from-gray-800/80 backdrop-blur-sm rounded-3xl shadow-xl border border-amber-200 dark:border-gray-700 p-8">
            <div className="text-center">
              <h3 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">Option 2: Warm Beige/Cream Theme</h3>
              <p className="text-amber-700 dark:text-gray-400 mb-4">
                This theme uses warm amber and orange colors for a softer, more inviting appearance.
              </p>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div className="bg-white dark:bg-gray-700/50 rounded-xl p-4 border border-amber-200 dark:border-gray-600">
                  <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Header</h4>
                  <p className="text-amber-700 dark:text-gray-400">Amber gradient with warm tones</p>
                </div>
                <div className="bg-white dark:bg-gray-700/50 rounded-xl p-4 border border-amber-200 dark:border-gray-600">
                  <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Cards</h4>
                  <p className="text-amber-700 dark:text-gray-400">Warm beige backgrounds with amber accents</p>
                </div>
                <div className="bg-white dark:bg-gray-700/50 rounded-xl p-4 border border-amber-200 dark:border-gray-600">
                  <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Charts</h4>
                  <p className="text-amber-700 dark:text-gray-400">Amber-colored data visualization</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Layout>
    </ProtectedRoute>
  )
}
