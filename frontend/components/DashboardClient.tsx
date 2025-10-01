'use client'

import React, { useState, useEffect } from 'react'
import { useDashboardTheme } from '../contexts/DashboardThemeContext'
import {
  ChartBarIcon,
  BoltIcon,
  SunIcon,
  CreditCardIcon,
  LightBulbIcon,
  ClockIcon,
  BanknotesIcon,
  ArrowTrendingUpIcon,
  ArrowTrendingDownIcon,
  CheckCircleIcon,
  XCircleIcon,
  CogIcon,
  UserIcon,
  BellIcon,
  WrenchScrewdriverIcon,
  EyeIcon,
  PlusIcon,
  SparklesIcon,
  GlobeAltIcon,
  CpuChipIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Area, AreaChart, BarChart, Bar, PieChart, Pie, Cell, RadialBarChart, RadialBar } from 'recharts'
import { useAuth } from '../hooks/useAuth'

interface EnergyData {
  time: string
  consumption: number
  generation: number
  cost: number
}

interface DashboardStats {
  currentConsumption: number
  totalEnergyConsumption: number
  monthlyCost: number
  totalCost: number
  solarGeneration: number
  totalEnergyGeneration: number
  offPeakSavings: number
  costSavings: number
  totalSavings: number
  peakDemand: number
  efficiencyScore: number
  efficiency: number
  carbonFootprint: number
}

interface CostOptimization {
  id: string
  title: string
  description: string
  potentialSavings: number
  implementationCost: number
  paybackPeriod: number
  priority: 'high' | 'medium' | 'low'
  category: string
}

interface EnergyEfficiency {
  id: string
  metric: string
  currentValue: number
  targetValue: number
  unit: string
  status: 'excellent' | 'good' | 'fair' | 'poor'
  trend: 'up' | 'down' | 'stable'
}

interface EnergyAlert {
  id: string
  message: string
  severity: 'critical' | 'warning' | 'info'
  timestamp: string
  category: string
}

interface EnergyForecast {
  id: string
  period: string
  predictedConsumption: number
  confidence: number
  factors: string[]
}

interface SmartRecommendation {
  id: string
  title: string
  description: string
  impact: 'high' | 'medium' | 'low'
  implementation: 'easy' | 'moderate' | 'complex'
  category: string
}

interface PowerQuality {
  voltage: number
  current: number
  frequency: number
  powerFactor: number
  totalHarmonicDistortion: number
  status: 'excellent' | 'good' | 'fair' | 'poor'
}

interface LoadProfile {
  peakDemand: number
  offPeakDemand: number
  baseLoad: number
  peakHours: string[]
  offPeakHours: string[]
  loadFactor: number
  hourly?: any[]
  daily?: any[]
  weekly?: any[]
  monthly?: any[]
}

interface CarbonFootprint {
  totalEmissions: number
  renewablePercentage: number
  carbonSaved: number
  equivalentTrees: number
  equivalentCars: number
}

interface DeviceConsumption {
  id: string
  name: string
  type: string
  powerConsumption: number
  dailyUsage: number
  monthlyCost: number
  efficiency: number
  status: 'active' | 'idle' | 'offline'
}

interface TimeOfUseAnalysis {
  peakRate: number
  offPeakRate: number
  peakHours: string
  offPeakHours: string
  peakConsumption: number
  offPeakConsumption: number
  potentialSavings: number
}

interface WeatherImpact {
  temperature: number
  humidity: number
  solarIrradiance: number
  energyImpact: number
  hvacLoad: number
  solarEfficiency: number
  impact?: number
}

interface EfficiencyBenchmark {
  metric: string
  currentValue: number
  industryAverage: number
  bestPractice: number
  performance: 'above' | 'average' | 'below'
  improvement: number
}

export default function DashboardClient() {
  const [energyData, setEnergyData] = useState<EnergyData[]>([])
  const [stats, setStats] = useState<DashboardStats>({
    currentConsumption: 0,
    totalEnergyConsumption: 0,
    monthlyCost: 0,
    totalCost: 0,
    solarGeneration: 0,
    totalEnergyGeneration: 0,
    offPeakSavings: 0,
    costSavings: 0,
    totalSavings: 0,
    peakDemand: 0,
    efficiencyScore: 0,
    efficiency: 0,
    carbonFootprint: 0
  })
  const [costOptimizations, setCostOptimizations] = useState<CostOptimization[]>([])
  const [energyEfficiencies, setEnergyEfficiencies] = useState<EnergyEfficiency[]>([])
  const [energyAlerts, setEnergyAlerts] = useState<EnergyAlert[]>([])
  const [energyForecasts, setEnergyForecasts] = useState<EnergyForecast[]>([])
  const [smartRecommendations, setSmartRecommendations] = useState<SmartRecommendation[]>([])
  const [powerQuality, setPowerQuality] = useState<PowerQuality>({
    voltage: 0,
    current: 0,
    frequency: 0,
    powerFactor: 0,
    totalHarmonicDistortion: 0,
    status: 'good'
  })
  const [loadProfile, setLoadProfile] = useState<LoadProfile>({
    peakDemand: 0,
    offPeakDemand: 0,
    baseLoad: 0,
    peakHours: [],
    offPeakHours: [],
    loadFactor: 0
  })
  const [carbonFootprint, setCarbonFootprint] = useState<CarbonFootprint>({
    totalEmissions: 0,
    renewablePercentage: 0,
    carbonSaved: 0,
    equivalentTrees: 0,
    equivalentCars: 0
  })
  const [deviceConsumption, setDeviceConsumption] = useState<DeviceConsumption[]>([])
  const [timeOfUseAnalysis, setTimeOfUseAnalysis] = useState<TimeOfUseAnalysis>({
    peakRate: 0,
    offPeakRate: 0,
    peakHours: '',
    offPeakHours: '',
    peakConsumption: 0,
    offPeakConsumption: 0,
    potentialSavings: 0
  })
  const [weatherImpact, setWeatherImpact] = useState<WeatherImpact>({
    temperature: 0,
    humidity: 0,
    solarIrradiance: 0,
    energyImpact: 0,
    hvacLoad: 0,
    solarEfficiency: 0
  })
  const [efficiencyBenchmarks, setEfficiencyBenchmarks] = useState<EfficiencyBenchmark[]>([])
  const [isLoading, setIsLoading] = useState(true)

  const { dashboardStyle } = useDashboardTheme()
  const { user } = useAuth()

  // Professional Intelligence Platform Theme Styles
  const getThemeStyles = () => {
    switch (dashboardStyle) {
      case 'landing-page':
        return {
          header: 'bg-gradient-intelligence rounded-3xl shadow-2xl border border-intelligence-200 p-8 text-white relative overflow-hidden',
          headerText: 'text-intelligence-100',
          card: 'bg-card rounded-2xl shadow-xl border border-intelligence-200 p-6 transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          cardIcon: 'text-intelligence-500',
          chart: 'bg-card rounded-2xl shadow-xl border border-intelligence-200 p-6 transform hover:scale-105 transition-all duration-300',
          kpiCard: 'bg-gradient-intelligence rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardAlt: 'bg-gradient-trust rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardWarm: 'bg-gradient-to-br from-warning-500 to-warning-600 rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardPurple: 'bg-gradient-to-br from-intelligence-600 to-intelligence-700 rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl'
        }
        case 'system-theme':
        return {
          header: 'bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden',
          headerText: 'text-blue-200',
          card: 'bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-6',
          cardIcon: 'text-blue-500',
          chart: 'bg-white dark:bg-gray-800 rounded-2xl shadow-xl border border-gray-200 dark:border-gray-700 p-6',
          kpiCard: 'bg-green-50 dark:bg-green-900/20 rounded-xl p-6 text-center',
          kpiCardAlt: 'bg-blue-50 dark:bg-blue-900/20 rounded-xl p-6 text-center',
          kpiCardWarm: 'bg-purple-50 dark:bg-purple-900/20 rounded-xl p-6 text-center',
          kpiCardPurple: 'bg-purple-50 dark:bg-purple-900/20 rounded-xl p-6 text-center'
        }
      case 'system-default':
        return {
          header: 'bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-8',
          headerText: 'text-gray-900 dark:text-white',
          card: 'bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6',
          cardIcon: 'text-gray-600 dark:text-gray-400',
          chart: 'bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6',
          kpiCard: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-6 text-center',
          kpiCardAlt: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-6 text-center',
          kpiCardWarm: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-6 text-center',
          kpiCardPurple: 'bg-gray-50 dark:bg-gray-700 rounded-lg p-6 text-center'
        }
      case 'professional-intelligence':
        return {
          header: 'bg-gradient-intelligence rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden',
          headerText: 'text-intelligence-100',
          card: 'bg-card rounded-2xl shadow-xl border border-intelligence-200 p-6 transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          cardIcon: 'text-intelligence-400',
          chart: 'bg-card rounded-2xl shadow-xl border border-intelligence-200 p-6 transform hover:scale-105 transition-all duration-300',
          kpiCard: 'bg-gradient-intelligence rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardAlt: 'bg-gradient-trust rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardWarm: 'bg-gradient-to-br from-warning-500 to-warning-600 rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardPurple: 'bg-gradient-to-br from-intelligence-600 to-intelligence-700 rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl'
        }
      default: // Professional Intelligence theme
        return {
          header: 'bg-gradient-intelligence rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden',
          headerText: 'text-intelligence-100',
          card: 'bg-card rounded-2xl shadow-xl border border-intelligence-200 p-6 transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          cardIcon: 'text-intelligence-400',
          chart: 'bg-card rounded-2xl shadow-xl border border-intelligence-200 p-6 transform hover:scale-105 transition-all duration-300',
          kpiCard: 'bg-gradient-intelligence rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardAlt: 'bg-gradient-trust rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardWarm: 'bg-gradient-to-br from-warning-500 to-warning-600 rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl',
          kpiCardPurple: 'bg-gradient-to-br from-intelligence-600 to-intelligence-700 rounded-2xl shadow-xl p-6 text-white transform hover:scale-105 transition-all duration-300 hover:shadow-2xl'
        }
    }
  }

  const themeStyles = getThemeStyles()

  useEffect(() => {
    const loadDashboardData = async () => {
      try {
        // Get authentication token
        const token = localStorage.getItem('token')
        const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}
        
        // Load real data from APIs
        const [energyResponse, statsResponse, optimizationsResponse] = await Promise.all([
          fetch('/api/proxy?service=energy&path=/energy/readings', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/dashboard-stats', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/cost-optimizations', { headers: authHeaders })
        ])

        // Handle empty responses gracefully - services are running but no data yet
        if (!energyResponse.ok && energyResponse.status !== 400) {
          throw new Error('Failed to load energy data')
        }
        if (!statsResponse.ok && statsResponse.status !== 400) {
          throw new Error('Failed to load analytics data')
        }
        if (!optimizationsResponse.ok && optimizationsResponse.status !== 400) {
          throw new Error('Failed to load optimization data')
        }

        // Parse responses or use default empty data
        const energyResponseData = energyResponse.ok ? await energyResponse.json() : { content: [] }
        const energyData: EnergyData[] = energyResponseData.content || []
        
        // Ensure we have valid chart data even if empty
        const chartData: EnergyData[] = energyData.length > 0 ? energyData : [
          { time: '00:00', consumption: 0, generation: 0, cost: 0 },
          { time: '06:00', consumption: 0, generation: 0, cost: 0 },
          { time: '12:00', consumption: 0, generation: 0, cost: 0 },
          { time: '18:00', consumption: 0, generation: 0, cost: 0 },
          { time: '24:00', consumption: 0, generation: 0, cost: 0 }
        ]
        
        const stats: DashboardStats = statsResponse.ok ? await statsResponse.json() : {
          totalEnergyConsumption: 0,
          totalEnergyGeneration: 0,
          totalCost: 0,
          totalSavings: 0,
          efficiency: 0,
          carbonFootprint: 0
        }
        const costOptimizations: CostOptimization[] = optimizationsResponse.ok ? await optimizationsResponse.json() : []

        // Load additional data from APIs
        const [efficienciesResponse, alertsResponse, forecastsResponse, recommendationsResponse, powerQualityResponse] = await Promise.all([
          fetch('/api/proxy?service=analytics&path=/analytics/efficiency-metrics', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/alerts', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/forecasts', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/recommendations', { headers: authHeaders }),
          fetch('/api/proxy?service=energy&path=/energy/power-quality', { headers: authHeaders })
        ])

        // Parse additional responses or use default empty data
        const energyEfficiencies: EnergyEfficiency[] = efficienciesResponse.ok ? await efficienciesResponse.json() : []
        const energyAlerts: EnergyAlert[] = alertsResponse.ok ? await alertsResponse.json() : []
        const energyForecasts: EnergyForecast[] = forecastsResponse.ok ? await forecastsResponse.json() : []
        const smartRecommendations: SmartRecommendation[] = recommendationsResponse.ok ? await recommendationsResponse.json() : []
        const powerQuality: PowerQuality = powerQualityResponse.ok ? await powerQualityResponse.json() : {
          voltage: 0,
          current: 0,
          frequency: 0,
          powerFactor: 0,
          harmonics: 0,
          quality: 'Good'
        }

        // Load additional analytics data
        const [loadProfileResponse, carbonFootprintResponse, deviceConsumptionResponse, timeOfUseResponse, weatherImpactResponse, efficiencyBenchmarksResponse] = await Promise.all([
          fetch('/api/proxy?service=analytics&path=/analytics/load-profile', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/carbon-footprint', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/device-consumption', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/time-of-use', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/weather-impact', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/efficiency-benchmarks', { headers: authHeaders })
        ])

        // Parse final responses or use default empty data
        const loadProfile: LoadProfile = loadProfileResponse.ok ? await loadProfileResponse.json() : {
          hourly: [],
          daily: [],
          weekly: [],
          monthly: []
        }
        const carbonFootprint: CarbonFootprint = carbonFootprintResponse.ok ? await carbonFootprintResponse.json() : {
          total: 0,
          renewable: 0,
          nonRenewable: 0,
          savings: 0
        }
        const deviceConsumption: DeviceConsumption[] = deviceConsumptionResponse.ok ? await deviceConsumptionResponse.json() : []
        const timeOfUseAnalysis: TimeOfUseAnalysis = timeOfUseResponse.ok ? await timeOfUseResponse.json() : {
          peak: { consumption: 0, cost: 0 },
          offPeak: { consumption: 0, cost: 0 },
          savings: 0
        }
        const weatherImpact: WeatherImpact = weatherImpactResponse.ok ? await weatherImpactResponse.json() : {
          temperature: 0,
          humidity: 0,
          impact: 0
        }
        const efficiencyBenchmarks: EfficiencyBenchmark[] = efficiencyBenchmarksResponse.ok ? await efficiencyBenchmarksResponse.json() : []

        // Set all the real data
        setEnergyData(chartData)
        setStats(stats)
        setCostOptimizations(costOptimizations)
        setEnergyEfficiencies(energyEfficiencies)
        setEnergyAlerts(energyAlerts)
        setEnergyForecasts(energyForecasts)
        setSmartRecommendations(smartRecommendations)
        setPowerQuality(powerQuality)
        setLoadProfile(loadProfile)
        setCarbonFootprint(carbonFootprint)
        setDeviceConsumption(deviceConsumption)
        setTimeOfUseAnalysis(timeOfUseAnalysis)
        setWeatherImpact(weatherImpact)
        setEfficiencyBenchmarks(efficiencyBenchmarks)
        setIsLoading(false)
      } catch (error) {
        console.error('Error loading dashboard data:', error)
        setIsLoading(false)
      }
    }

    loadDashboardData()
  }, [])

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-intelligence-500 mx-auto"></div>
          <p className="mt-4 text-xl text-surface-100">Loading your energy insights...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-6">
      {/* Enhanced Header with 3D Effects */}
      <div className={themeStyles.header}>
        <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent"></div>
        <div className="relative z-10">
          <div className="flex items-center space-x-4 mb-6">
            <div className="p-3 bg-white/20 rounded-2xl backdrop-blur-sm">
              <SparklesIcon className="h-8 w-8 text-white" />
            </div>
            <div>
              <h1 className="text-4xl font-bold text-white mb-2">Energy Intelligence Dashboard</h1>
              <p className={`text-lg ${themeStyles.headerText}`}>
                Real-time insights for smarter energy management
              </p>
            </div>
          </div>
          
          {/* Quick Stats Row */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Current Load</p>
              <p className="text-2xl font-bold text-white">{stats.totalEnergyConsumption || 0} kW</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Efficiency</p>
              <p className="text-2xl font-bold text-white">{stats.efficiency || 0}%</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Solar Gen</p>
              <p className="text-2xl font-bold text-white">{stats.totalEnergyGeneration || 0} kWh</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-xl p-4 text-center">
              <p className="text-white/80 text-sm">Savings</p>
              <p className="text-2xl font-bold text-white">₦{stats.totalSavings || 0}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Enhanced KPI Cards with 3D Effects */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mt-8">
        <div className={themeStyles.kpiCard}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-green-600 dark:text-green-400">Monthly Cost</p>
              <p className="text-3xl font-bold text-green-600 dark:text-green-400">₦{(stats.totalCost || 0).toFixed(2)}</p>
              <p className="text-xs text-green-600 dark:text-green-400 mt-1">Current billing cycle</p>
            </div>
            <div className="p-3 bg-green-100 dark:bg-green-800 rounded-xl">
              <CreditCardIcon className="h-8 w-8 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </div>

        <div className={themeStyles.kpiCardAlt}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-blue-600 dark:text-blue-400">Solar Generation</p>
              <p className="text-3xl font-bold text-blue-600 dark:text-blue-400">{stats.totalEnergyGeneration || 0} kWh</p>
              <p className="text-xs text-blue-600 dark:text-blue-400 mt-1">Today's production</p>
            </div>
            <div className="p-3 bg-blue-100 dark:bg-blue-800 rounded-xl">
              <SunIcon className="h-8 w-8 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </div>

        <div className={themeStyles.kpiCardWarm}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-purple-600 dark:text-purple-400">Cost Savings</p>
              <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">₦{(stats.totalSavings || 0).toFixed(2)}</p>
              <p className="text-xs text-purple-600 dark:text-purple-400 mt-1">This month</p>
            </div>
            <div className="p-3 bg-purple-100 dark:bg-purple-800 rounded-xl">
              <BanknotesIcon className="h-8 w-8 text-purple-600 dark:text-purple-400" />
            </div>
          </div>
        </div>

        <div className={themeStyles.kpiCardPurple}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-purple-600 dark:text-purple-400">Efficiency Score</p>
              <p className="text-3xl font-bold text-purple-600 dark:text-purple-400">{stats.efficiency || 0}%</p>
              <p className="text-xs text-purple-600 dark:text-purple-400 mt-1">Performance rating</p>
            </div>
            <div className="p-3 bg-purple-100 dark:bg-purple-800 rounded-xl">
              <LightBulbIcon className="h-8 w-8 text-purple-600 dark:text-purple-400" />
            </div>
          </div>
        </div>
      </div>

      {/* Enhanced Charts Section with 3D Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mt-8">
        {/* Energy Consumption Chart */}
        <div className={themeStyles.chart}>
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-semibold text-surface-50">Energy Consumption</h3>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-intelligence-500 rounded-full"></div>
              <span className="text-sm text-surface-400">Consumption</span>
              <div className="w-3 h-3 bg-trust-500 rounded-full ml-4"></div>
              <span className="text-sm text-surface-400">Generation</span>
            </div>
          </div>
          {energyData && energyData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={energyData}>
                <defs>
                  <linearGradient id="consumptionGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0.1}/>
                  </linearGradient>
                  <linearGradient id="generationGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#14b8a6" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#14b8a6" stopOpacity={0.1}/>
                  </linearGradient>
                </defs>
                <XAxis dataKey="time" stroke="#64748b" />
                <YAxis stroke="#64748b" />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: '#18181b', 
                    border: '1px solid #6366f1',
                    borderRadius: '12px',
                    color: '#f8fafc',
                    boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)'
                  }}
                />
                <Area 
                  type="monotone" 
                  dataKey="consumption" 
                  stackId="1" 
                  stroke="#6366f1" 
                  fill="url(#consumptionGradient)"
                  strokeWidth={3}
                  connectNulls={false}
                />
                <Area 
                  type="monotone" 
                  dataKey="generation" 
                  stackId="2" 
                  stroke="#14b8a6" 
                  fill="url(#generationGradient)"
                  strokeWidth={3}
                  connectNulls={false}
                />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-[300px] text-surface-400">
              <div className="text-center">
                <div className="text-4xl mb-2">📊</div>
                <p>No energy data available</p>
                <p className="text-sm">Data will appear when devices are connected</p>
              </div>
            </div>
          )}
        </div>

        {/* Cost Analysis Chart */}
        <div className={themeStyles.chart}>
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-semibold text-surface-50">Cost Analysis</h3>
            <div className="text-sm text-surface-400">24-hour breakdown</div>
          </div>
          {energyData && energyData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={energyData}>
                <defs>
                  <linearGradient id="costGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#a855f7" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#a855f7" stopOpacity={0.3}/>
                  </linearGradient>
                </defs>
                <XAxis dataKey="time" stroke="#64748b" />
                <YAxis stroke="#64748b" />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: '#18181b', 
                    border: '1px solid #a855f7',
                    borderRadius: '12px',
                    color: '#f8fafc',
                    boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)'
                  }}
                />
                <Bar dataKey="cost" fill="url(#costGradient)" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-[300px] text-surface-400">
              <div className="text-center">
                <div className="text-4xl mb-2">💰</div>
                <p>No cost data available</p>
                <p className="text-sm">Cost analysis will appear when data is available</p>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Enhanced Insights Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mt-8">
        {/* Smart Recommendations */}
        <div className={themeStyles.card}>
          <div className="flex items-center space-x-3 mb-6">
            <div className="p-2 bg-gradient-to-r from-blue-500 to-purple-500 rounded-xl">
              <CpuChipIcon className="h-6 w-6 text-white" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Smart Recommendations</h3>
          </div>
          <div className="space-y-4">
            {smartRecommendations.map((rec) => (
              <div key={rec.id} className="p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl border-l-4 border-blue-500">
                <h4 className="font-semibold text-gray-900 dark:text-white mb-2">{rec.title}</h4>
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">{rec.description}</p>
                <div className="flex items-center space-x-4 text-xs">
                  <span className={`px-2 py-1 rounded-full ${
                    rec.impact === 'high' ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' :
                    rec.impact === 'medium' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' :
                    'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                  }`}>
                    {rec.impact} impact
                  </span>
                  <span className={`px-2 py-1 rounded-full ${
                    rec.implementation === 'easy' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                    rec.implementation === 'moderate' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' :
                    'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                  }`}>
                    {rec.implementation}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Energy Alerts */}
        <div className={themeStyles.card}>
          <div className="flex items-center space-x-3 mb-6">
            <div className="p-2 bg-gradient-to-r from-orange-500 to-red-500 rounded-xl">
              <BellIcon className="h-6 w-6 text-white" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Energy Alerts</h3>
          </div>
          <div className="space-y-4">
            {energyAlerts.map((alert) => (
              <div key={alert.id} className={`p-4 rounded-xl border-l-4 ${
                alert.severity === 'critical' ? 'bg-red-50 dark:bg-red-900/20 border-red-500' :
                alert.severity === 'warning' ? 'bg-yellow-50 dark:bg-yellow-900/20 border-yellow-500' :
                'bg-blue-50 dark:bg-blue-900/20 border-blue-500'
              }`}>
                <div className="flex items-start space-x-3">
                  <div className={`w-2 h-2 rounded-full mt-2 ${
                    alert.severity === 'critical' ? 'bg-red-500' :
                    alert.severity === 'warning' ? 'bg-yellow-500' :
                    'bg-blue-500'
                  }`}></div>
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-900 dark:text-white mb-1">{alert.message}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">{alert.category}</p>
                    <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">
                      {new Date(alert.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Efficiency Metrics */}
        <div className={themeStyles.card}>
          <div className="flex items-center space-x-3 mb-6">
            <div className="p-2 bg-gradient-to-r from-green-500 to-emerald-500 rounded-xl">
              <ChartBarIcon className="h-6 w-6 text-white" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Efficiency Metrics</h3>
          </div>
          <div className="space-y-4">
            {energyEfficiencies.map((metric) => (
              <div key={metric.id} className="p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                <div className="flex items-center justify-between mb-2">
                  <h4 className="font-medium text-gray-900 dark:text-white">{metric.metric}</h4>
                  <span className={`px-2 py-1 rounded-full text-xs ${
                    metric.status === 'excellent' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                    metric.status === 'good' ? 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200' :
                    metric.status === 'fair' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' :
                    'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                  }`}>
                    {metric.status}
                  </span>
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-2xl font-bold text-gray-900 dark:text-white">{metric.currentValue}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">Target: {metric.targetValue}</p>
                  </div>
                  <div className={`p-2 rounded-full ${
                    metric.trend === 'up' ? 'bg-green-100 dark:bg-green-900/20' :
                    metric.trend === 'down' ? 'bg-red-100 dark:bg-red-900/20' :
                    'bg-gray-100 dark:bg-gray-700'
                  }`}>
                    {metric.trend === 'up' && <ArrowTrendingUpIcon className="h-5 w-5 text-green-600 dark:text-green-400" />}
                    {metric.trend === 'down' && <ArrowTrendingDownIcon className="h-5 w-5 text-red-600 dark:text-red-400" />}
                    {metric.trend === 'stable' && <div className="w-5 h-5 bg-gray-400 rounded-full"></div>}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Enhanced Cost Optimization Section */}
      <div className="mt-8">
        <div className={themeStyles.card}>
          <div className="flex items-center space-x-3 mb-6">
            <div className="p-2 bg-gradient-to-r from-emerald-500 to-teal-500 rounded-xl">
              <BanknotesIcon className="h-6 w-6 text-white" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Cost Optimization Opportunities</h3>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {costOptimizations.map((opt) => (
              <div key={opt.id} className="p-6 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl border border-gray-200 dark:border-gray-600">
                <div className="flex items-start justify-between mb-4">
                  <h4 className="text-lg font-semibold text-gray-900 dark:text-white">{opt.title}</h4>
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                    opt.priority === 'high' ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200' :
                    opt.priority === 'medium' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' :
                    'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                  }`}>
                    {opt.priority} priority
                  </span>
                </div>
                <p className="text-gray-600 dark:text-gray-400 mb-4">{opt.description}</p>
                <div className="flex items-center justify-between">
                  <div className="text-center">
                    <p className="text-sm text-gray-500 dark:text-gray-400">Potential Savings</p>
                    <p className="text-xl font-bold text-green-600 dark:text-green-400">₦{opt.potentialSavings}</p>
                  </div>
                  <div className="text-center">
                    <p className="text-sm text-gray-500 dark:text-gray-400">Category</p>
                    <p className="text-sm font-medium text-gray-900 dark:text-white">{opt.category}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Advanced Analytics Section */}
      <div className="mt-8">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-6">Advanced Analytics</h2>
        
        {/* Power Quality & Load Profile */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Power Quality Metrics */}
          <div className={themeStyles.card}>
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-gradient-to-r from-purple-500 to-indigo-500 rounded-xl">
                <BoltIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Power Quality</h3>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="text-center p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Voltage</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{powerQuality.voltage}V</p>
                <p className={`text-xs ${powerQuality.voltage >= 240 ? 'text-green-600' : 'text-yellow-600'}`}>
                  {powerQuality.voltage >= 240 ? 'Optimal' : 'Low'}
                </p>
              </div>
              <div className="text-center p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Current</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{powerQuality.current}A</p>
                <p className="text-xs text-gray-500">Load Current</p>
              </div>
              <div className="text-center p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Frequency</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{powerQuality.frequency}Hz</p>
                <p className={`text-xs ${Math.abs(powerQuality.frequency - 50) <= 0.5 ? 'text-green-600' : 'text-red-600'}`}>
                  {Math.abs(powerQuality.frequency - 50) <= 0.5 ? 'Stable' : 'Unstable'}
                </p>
              </div>
              <div className="text-center p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Power Factor</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white">{powerQuality.powerFactor}</p>
                <p className={`text-xs ${powerQuality.powerFactor >= 0.9 ? 'text-green-600' : 'text-yellow-600'}`}>
                  {powerQuality.powerFactor >= 0.9 ? 'Good' : 'Needs Improvement'}
                </p>
              </div>
            </div>
          </div>

          {/* Load Profile Analysis */}
          <div className={themeStyles.card}>
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-xl">
                <ChartBarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Load Profile</h3>
            </div>
            <div className="space-y-4">
              <div className="flex justify-between items-center p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Hourly Data Points</p>
                  <p className="text-xl font-bold text-gray-900 dark:text-white">{loadProfile.hourly?.length || 0}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-gray-500 dark:text-gray-400">Daily Data Points</p>
                  <p className="text-xl font-bold text-blue-600">{loadProfile.daily?.length || 0}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="text-center p-3 bg-gradient-to-r from-red-50 to-red-100 dark:from-red-900/20 dark:to-red-800/20 rounded-xl">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Weekly Data</p>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white">{loadProfile.weekly?.length || 0} points</p>
                </div>
                <div className="text-center p-3 bg-gradient-to-r from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-xl">
                  <p className="text-xs text-gray-500 dark:text-gray-400">Monthly Data</p>
                  <p className="text-sm font-semibold text-gray-900 dark:text-white">{loadProfile.monthly?.length || 0} points</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Device Consumption & Carbon Footprint */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Device-Level Consumption */}
          <div className={themeStyles.card}>
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-gradient-to-r from-orange-500 to-red-500 rounded-xl">
                <WrenchScrewdriverIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Device Consumption</h3>
            </div>
            <div className="space-y-3">
              {deviceConsumption.map((device) => (
                <div key={device.id} className="p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center space-x-3">
                      <div className={`w-3 h-3 rounded-full ${
                        device.status === 'active' ? 'bg-green-500' :
                        device.status === 'idle' ? 'bg-yellow-500' : 'bg-gray-400'
                      }`}></div>
                      <h4 className="font-semibold text-gray-900 dark:text-white">{device.name}</h4>
                      <span className="text-xs text-gray-500 dark:text-gray-400">{device.type}</span>
                    </div>
                    <span className="text-sm font-bold text-gray-900 dark:text-white">{device.powerConsumption} kW</span>
                  </div>
                  <div className="grid grid-cols-3 gap-4 text-center">
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">Daily Usage</p>
                      <p className="text-sm font-semibold text-gray-900 dark:text-white">{device.dailyUsage}h</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">Monthly Cost</p>
                      <p className="text-sm font-semibold text-gray-900 dark:text-white">₦{device.monthlyCost}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">Efficiency</p>
                      <p className="text-sm font-semibold text-gray-900 dark:text-white">{device.efficiency}%</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Carbon Footprint */}
          <div className={themeStyles.card}>
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-gradient-to-r from-green-500 to-emerald-500 rounded-xl">
                <GlobeAltIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Environmental Impact</h3>
            </div>
            <div className="space-y-4">
              <div className="text-center p-6 bg-gradient-to-r from-green-50 to-emerald-50 dark:from-green-900/20 dark:to-emerald-900/20 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Carbon Footprint</p>
                <p className="text-3xl font-bold text-gray-900 dark:text-white">{carbonFootprint.totalEmissions} tCO₂</p>
                <p className="text-xs text-gray-500 dark:text-gray-400">This month</p>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="text-center p-4 bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-xl">
                  <p className="text-sm text-gray-500 dark:text-gray-400">Renewable %</p>
                  <p className="text-xl font-bold text-blue-600">{carbonFootprint.renewablePercentage}%</p>
                </div>
                <div className="text-center p-4 bg-gradient-to-r from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-xl">
                  <p className="text-sm text-gray-500 dark:text-gray-400">Carbon Saved</p>
                  <p className="text-xl font-bold text-green-600">{carbonFootprint.carbonSaved} tCO₂</p>
                </div>
              </div>
              <div className="text-center p-4 bg-gradient-to-r from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Equivalent to</p>
                <p className="text-lg font-semibold text-gray-900 dark:text-white">
                  {carbonFootprint.equivalentTrees} trees planted
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Time-of-Use Analysis & Weather Impact */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Time-of-Use Analysis */}
          <div className={themeStyles.card}>
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-gradient-to-r from-indigo-500 to-purple-500 rounded-xl">
                <ClockIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Time-of-Use Analysis</h3>
            </div>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="p-4 bg-gradient-to-r from-red-50 to-red-100 dark:from-red-900/20 dark:to-red-800/20 rounded-xl">
                  <p className="text-sm text-gray-500 dark:text-gray-400">Peak Rate</p>
                  <p className="text-xl font-bold text-gray-900 dark:text-white">₦{timeOfUseAnalysis.peakRate}/kWh</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">{timeOfUseAnalysis.peakHours}</p>
                </div>
                <div className="p-4 bg-gradient-to-r from-green-50 to-green-100 dark:from-green-900/20 dark:to-green-800/20 rounded-xl">
                  <p className="text-sm text-gray-500 dark:text-gray-400">Off-Peak Rate</p>
                  <p className="text-xl font-bold text-gray-900 dark:text-white">₦{timeOfUseAnalysis.offPeakRate}/kWh</p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">{timeOfUseAnalysis.offPeakHours}</p>
                </div>
              </div>
              <div className="p-4 bg-gradient-to-r from-yellow-50 to-yellow-100 dark:from-yellow-900/20 dark:to-yellow-800/20 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Potential Savings</p>
                <p className="text-2xl font-bold text-yellow-600">₦{timeOfUseAnalysis.potentialSavings}</p>
                <p className="text-xs text-gray-500 dark:text-gray-400">By shifting load to off-peak hours</p>
              </div>
            </div>
          </div>

          {/* Weather Impact */}
          <div className={themeStyles.card}>
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-gradient-to-r from-yellow-500 to-orange-500 rounded-xl">
                <SunIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Weather Impact</h3>
            </div>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="text-center p-4 bg-gradient-to-r from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20 rounded-xl">
                  <p className="text-sm text-gray-500 dark:text-gray-400">Temperature</p>
                  <p className="text-xl font-bold text-gray-900 dark:text-white">{weatherImpact.temperature}°C</p>
                </div>
                <div className="text-center p-4 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                  <p className="text-sm text-gray-500 dark:text-gray-400">Humidity</p>
                  <p className="text-xl font-bold text-gray-900 dark:text-white">{weatherImpact.humidity}%</p>
                </div>
              </div>
              <div className="p-4 bg-gradient-to-r from-yellow-50 to-yellow-100 dark:from-yellow-900/20 dark:to-yellow-800/20 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Temperature</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{weatherImpact.temperature}°C</p>
                <p className="text-xs text-gray-500 dark:text-gray-400">Current temperature</p>
              </div>
              <div className="p-4 bg-gradient-to-r from-purple-50 to-purple-100 dark:from-purple-900/20 dark:to-purple-800/20 rounded-xl">
                <p className="text-sm text-gray-500 dark:text-gray-400">Humidity</p>
                <p className="text-xl font-bold text-gray-900 dark:text-white">{weatherImpact.humidity}%</p>
                <p className="text-xs text-gray-500 dark:text-gray-400">Energy Impact: {Math.round(weatherImpact.impact * 100)}%</p>
              </div>
            </div>
          </div>
        </div>

        {/* Efficiency Benchmarking */}
        <div className="mt-8">
          <div className={themeStyles.card}>
            <div className="flex items-center space-x-3 mb-6">
              <div className="p-2 bg-gradient-to-r from-teal-500 to-cyan-500 rounded-xl">
                <ChartBarIcon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-900 dark:text-white">Efficiency Benchmarking</h3>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {efficiencyBenchmarks.map((benchmark) => (
                <div key={benchmark.metric} className="p-6 bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-700 dark:to-gray-800 rounded-xl">
                  <div className="text-center mb-4">
                    <h4 className="font-semibold text-gray-900 dark:text-white mb-2">{benchmark.metric}</h4>
                    <div className="flex items-center justify-center space-x-2">
                      <span className={`px-2 py-1 rounded-full text-xs ${
                        benchmark.performance === 'above' ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' :
                        benchmark.performance === 'average' ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200' :
                        'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                      }`}>
                        {benchmark.performance}
                      </span>
                    </div>
                  </div>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Your Value</span>
                      <span className="font-semibold text-gray-900 dark:text-white">{benchmark.currentValue}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Industry Avg</span>
                      <span className="text-gray-600 dark:text-gray-400">{benchmark.industryAverage}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm text-gray-500 dark:text-gray-400">Best Practice</span>
                      <span className="text-gray-600 dark:text-gray-400">{benchmark.bestPractice}</span>
                    </div>
                    <div className="text-center pt-2 border-t border-gray-200 dark:border-gray-600">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Improvement Potential</p>
                      <p className="text-lg font-bold text-green-600">{benchmark.improvement}%</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
