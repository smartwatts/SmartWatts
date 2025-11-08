import React, { useState, useEffect } from 'react'
import Layout from '../components/Layout'
import ProtectedRoute from '../components/ProtectedRoute'
import { useFeatureFlags } from '../hooks/useFeatureFlags'
import FeatureUpgradeModal from '../components/FeatureUpgradeModal'
import {
  CreditCardIcon,
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  BoltIcon,
  CogIcon,
  WrenchScrewdriverIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  ShieldCheckIcon,
  BanknotesIcon,
  ArrowTrendingUpIcon,
  ChartBarIcon,
  CalculatorIcon,
  DocumentTextIcon,
} from '@heroicons/react/24/outline'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell, AreaChart, Area } from 'recharts'

interface BillingPlan {
  id: string
  name: string
  description: string
  price: number
  duration: string
  features: string[]
  status: 'active' | 'inactive' | 'trial'
  hardwareRequired: string[]
  maxDevices: number
  maxUsers: number
  icon: React.ComponentType<any>
  color: string
  savings: number
  roi: number
  paybackPeriod: number
}

interface HardwareDevice {
  id: string
  name: string
  type: string
  isCertified: boolean
  isActivated: boolean
  macAddress: string
  icon: React.ComponentType<any>
  color: string
  efficiency: number
  costSavings: number
  maintenanceCost: number
  lastMaintenance: string
}

interface BillingAnalytics {
  monthlySpending: number
  costSavings: number
  energyEfficiency: number
  peakDemandReduction: number
  carbonFootprintReduction: number
  roi: number
  paybackPeriod: number
}

interface CostOptimization {
  category: string
  currentCost: number
  potentialSavings: number
  implementationCost: number
  paybackPeriod: number
  priority: 'high' | 'medium' | 'low'
  recommendations: string[]
}

interface UsageMetrics {
  month: string
  energyConsumption: number
  cost: number
  efficiency: number
  savings: number
}

export default function Billing() {
  const { isFeatureEnabled } = useFeatureFlags()
  const [currentPlan, setCurrentPlan] = useState<string>('freemium')
  const [hardwareDevices, setHardwareDevices] = useState<HardwareDevice[]>([])
  const [trialDaysLeft, setTrialDaysLeft] = useState(85)
  const [loading, setLoading] = useState(true)
  const [showUpgradeModal, setShowUpgradeModal] = useState(false)
  const [billingAnalytics, setBillingAnalytics] = useState<BillingAnalytics>({
    monthlySpending: 0,
    costSavings: 0,
    energyEfficiency: 0,
    peakDemandReduction: 0,
    carbonFootprintReduction: 0,
    roi: 0,
    paybackPeriod: 0
  })
  const [costOptimizations, setCostOptimizations] = useState<CostOptimization[]>([])
  const [usageMetrics, setUsageMetrics] = useState<UsageMetrics[]>([])

  useEffect(() => {
    const loadBillingData = async () => {
      try {
        const token = localStorage.getItem('token')
        const authHeaders = token ? { Authorization: `Bearer ${token}` } : {}
        
        // Load real data from APIs
        const [devicesResponse, analyticsResponse, optimizationsResponse] = await Promise.all([
          fetch('/api/proxy?service=device&path=/devices/hardware', { headers: authHeaders }),
          fetch('/api/proxy?service=billing&path=/billing/analytics', { headers: authHeaders }),
          fetch('/api/proxy?service=analytics&path=/analytics/cost-optimizations', { headers: authHeaders })
        ])

        const devices: HardwareDevice[] = devicesResponse.ok ? await devicesResponse.json() : []
        const billingAnalytics: BillingAnalytics = analyticsResponse.ok ? await analyticsResponse.json() : {
          monthlySpending: 0,
          costSavings: 0,
          energyEfficiency: 0,
          carbonFootprint: 0
        }
        const costOptimizations: CostOptimization[] = optimizationsResponse.ok ? await optimizationsResponse.json() : []

        // Load usage metrics
        const usageMetricsResponse = await fetch('/api/proxy?service=billing&path=/billing/usage-metrics', { headers: authHeaders })
        const usageMetrics: UsageMetrics[] = usageMetricsResponse.ok ? await usageMetricsResponse.json() : []

        setHardwareDevices(devices)
        setBillingAnalytics(billingAnalytics)
        setCostOptimizations(costOptimizations)
        setUsageMetrics(usageMetrics)
        setLoading(false)
      } catch (error) {
        console.error('Failed to load billing data:', error)
        setLoading(false)
      }
    }

    loadBillingData()
  }, [])

  const billingPlans: BillingPlan[] = [
    {
      id: 'freemium',
      name: 'Freemium Plan',
      description: '3-month trial with certified SmartWatts hardware',
      price: 0,
      duration: '3 months',
      features: [
        'Real-time energy monitoring',
        'Basic analytics and reports',
        'Mobile app access',
        'Email notifications',
        'Up to 5 devices',
        'Single user access'
      ],
      status: 'trial',
      hardwareRequired: ['GATEWAY'],
      maxDevices: 5,
      maxUsers: 1,
      icon: ShieldCheckIcon,
      color: 'green',
      savings: 0,
      roi: 0,
      paybackPeriod: 0
    },
    {
      id: 'premium',
      name: 'Premium Plan',
      description: 'Enhanced features for power users',
      price: 5000,
      duration: 'month',
      features: [
        'All Freemium features',
        'Enhanced analytics',
        'Custom reports',
        'API access',
        'Priority support',
        'Up to 20 devices',
        'Multi-user access',
        'Data export',
        'SMS notifications'
      ],
      status: 'inactive',
      hardwareRequired: [],
      maxDevices: 20,
      maxUsers: 5,
      icon: BoltIcon,
      color: 'blue',
      savings: 25000,
      roi: 417,
      paybackPeriod: 2.4
    },
    {
      id: 'enterprise',
      name: 'Business Plan',
      description: 'Complete solution for businesses',
      price: 15000,
      duration: 'month',
      features: [
        'All Premium features',
        'Unlimited devices',
        'Multi-site management',
        'Custom integrations',
        'Dedicated support',
        'White-label options',
        'Enhanced security',
        'Compliance reporting'
      ],
      status: 'inactive',
      hardwareRequired: [],
      maxDevices: -1,
      maxUsers: -1,
      icon: CogIcon,
      color: 'purple',
      savings: 75000,
      roi: 500,
      paybackPeriod: 2.4
    }
  ]

  const currentPlanData = billingPlans.find(plan => plan.id === currentPlan)
  const certifiedDevices = hardwareDevices.filter(device => device.isCertified)
  const activatedDevices = hardwareDevices.filter(device => device.isActivated)
  const hasGateway = hardwareDevices.some(device => device.type === 'GATEWAY' && device.isActivated)

  const getPlanStatus = (planId: string) => {
    if (planId === 'freemium') {
      if (hasGateway && activatedDevices.length >= 1) {
        return trialDaysLeft > 0 ? 'trial' : 'expired'
      }
      return 'locked'
    }
    return planId === currentPlan ? 'active' : 'inactive'
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active':
      case 'trial':
        return 'text-green-600 dark:text-green-400'
      case 'inactive':
        return 'text-gray-600 dark:text-gray-400'
      case 'locked':
        return 'text-red-600 dark:text-red-400'
      case 'expired':
        return 'text-orange-600 dark:text-orange-400'
      default:
        return 'text-gray-600 dark:text-gray-400'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'active':
      case 'trial':
        return CheckCircleIcon
      case 'inactive':
        return ClockIcon
      case 'locked':
      case 'expired':
        return XCircleIcon
      default:
        return ClockIcon
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'active':
        return 'Active'
      case 'trial':
        return `${trialDaysLeft} days left`
      case 'inactive':
        return 'Inactive'
      case 'locked':
        return 'Hardware Required'
      case 'expired':
        return 'Trial Expired'
      default:
        return 'Unknown'
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

  if (loading) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="flex items-center justify-center h-64">
            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
          </div>
        </Layout>
      </ProtectedRoute>
    )
  }

  // Check if Billing Dashboard feature is enabled
  if (!isFeatureEnabled('BILLING_DASHBOARD')) {
    return (
      <ProtectedRoute>
        <Layout>
          <div className="min-h-screen flex items-center justify-center">
            <div className="text-center">
              <div className="w-24 h-24 mx-auto mb-6 bg-gray-100 dark:bg-gray-800 rounded-full flex items-center justify-center">
                <CreditCardIcon className="w-12 h-12 text-gray-400" />
              </div>
              <h1 className="text-2xl font-bold text-gray-900 dark:text-white mb-4">
                Billing Dashboard is a Premium Feature
              </h1>
              <p className="text-gray-600 dark:text-gray-400 mb-8 max-w-md">
                Upgrade your plan to access advanced billing analytics, cost optimization, 
                subscription management, and detailed financial insights.
              </p>
              <button
                onClick={() => setShowUpgradeModal(true)}
                className="bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors"
              >
                Upgrade Plan
              </button>
            </div>
          </div>

          <FeatureUpgradeModal
            isOpen={showUpgradeModal}
            onClose={() => setShowUpgradeModal(false)}
            featureName="Billing Dashboard"
            featureDescription="Advanced billing analytics and cost optimization features"
            currentPlan="Freemium"
          />
        </Layout>
      </ProtectedRoute>
    )
  }

  return (
    <ProtectedRoute>
      <Layout>
        <div className="space-y-6 animate-fade-in">
          {/* Header */}
          <div className="bg-gradient-to-r from-gray-800 via-blue-900 to-gray-800 dark:from-gray-900 dark:via-blue-900 dark:to-gray-800 rounded-3xl shadow-2xl p-8 text-white relative overflow-hidden">
            <div className="flex items-center justify-between">
              <div>
                        <h1 className="text-3xl font-bold mb-2">Billing & Cost Management</h1>
        <p className="text-blue-200 dark:text-blue-300 text-lg">Subscription management with cost optimization insights</p>
                <div className="flex items-center mt-4 space-x-6">
                  <div className="flex items-center space-x-2">
                    <ShieldCheckIcon className="h-5 w-5 text-green-300" />
                    <span className="text-sm">Hardware-Based Billing</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <ClockIcon className="h-5 w-5 text-yellow-300" />
                    <span className="text-sm">{trialDaysLeft} days trial remaining</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <BoltIcon className="h-5 w-5 text-blue-300" />
                    <span className="text-sm">{activatedDevices.length} devices active</span>
                  </div>
                </div>
              </div>
              <div className="hidden md:block">
                <div className="w-24 h-24 bg-white/10 rounded-full flex items-center justify-center">
                  <CreditCardIcon className="h-12 w-12 text-white" />
                </div>
              </div>
            </div>
          </div>

          {/* Key Performance Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Monthly Spending</p>
                  <p className="text-2xl font-bold text-gray-900 dark:text-white">₦{billingAnalytics.monthlySpending.toLocaleString()}</p>
                </div>
                <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/20 rounded-xl flex items-center justify-center">
                  <BanknotesIcon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Cost Savings</p>
                  <p className="text-2xl font-bold text-green-600 dark:text-green-400">₦{billingAnalytics.costSavings.toLocaleString()}</p>
                </div>
                <div className="w-12 h-12 bg-green-100 dark:bg-green-900/20 rounded-xl flex items-center justify-center">
                  <ArrowTrendingUpIcon className="h-6 w-6 text-green-600 dark:text-green-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">ROI</p>
                  <p className="text-2xl font-bold text-purple-600 dark:text-purple-400">{billingAnalytics.roi}%</p>
                </div>
                <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/20 rounded-xl flex items-center justify-center">
                  <ChartBarIcon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                </div>
              </div>
            </div>
            
            <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600 dark:text-gray-400">Payback Period</p>
                  <p className="text-2xl font-bold text-orange-600 dark:text-orange-400">{billingAnalytics.paybackPeriod} months</p>
                </div>
                <div className="w-12 h-12 bg-orange-100 dark:bg-orange-900/20 rounded-xl flex items-center justify-center">
                  <CalculatorIcon className="h-6 w-6 text-orange-600 dark:text-orange-400" />
                </div>
              </div>
            </div>
          </div>

          {/* Cost Optimization Dashboard */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Cost Optimization Opportunities</h2>
                <p className="text-gray-600 dark:text-gray-400">Strategic recommendations for reducing energy costs</p>
              </div>
              <div className="text-right">
                <p className="text-sm text-gray-500 dark:text-gray-400">Total Potential Savings</p>
                <p className="text-2xl font-bold text-green-600 dark:text-green-400">
                  ₦{costOptimizations.reduce((sum, opt) => sum + opt.potentialSavings, 0).toLocaleString()}
                </p>
              </div>
            </div>
            
            <div className="space-y-4">
              {costOptimizations.map((optimization, index) => (
                <div key={index} className="border border-gray-200 dark:border-gray-700 rounded-xl p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center space-x-3">
                      <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{optimization.category}</h3>
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${getPriorityColor(optimization.priority)}`}>
                        {optimization.priority.toUpperCase()}
                      </span>
                    </div>
                    <div className="text-right">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Current Cost</p>
                      <p className="text-lg font-semibold text-gray-900 dark:text-white">₦{optimization.currentCost.toLocaleString()}</p>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                    <div className="text-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Potential Savings</p>
                      <p className="text-xl font-bold text-green-600 dark:text-green-400">₦{optimization.potentialSavings.toLocaleString()}</p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Implementation Cost</p>
                      <p className="text-xl font-bold text-blue-600 dark:text-blue-400">₦{optimization.implementationCost.toLocaleString()}</p>
                    </div>
                    <div className="text-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <p className="text-sm text-gray-500 dark:text-gray-400">Payback Period</p>
                      <p className="text-xl font-bold text-purple-600 dark:text-purple-400">{optimization.paybackPeriod} months</p>
                    </div>
                  </div>
                  
                  <div>
                    <h4 className="font-semibold text-gray-900 dark:text-white mb-2">Recommendations:</h4>
                    <div className="space-y-2">
                      {optimization.recommendations.map((rec, recIndex) => (
                        <div key={recIndex} className="flex items-start space-x-3">
                          <div className="w-2 h-2 bg-blue-500 rounded-full mt-2 flex-shrink-0"></div>
                          <p className="text-sm text-gray-600 dark:text-gray-300">{rec}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Usage Trends Chart */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-semibold text-gray-900 dark:text-white">Usage Trends & Cost Analysis</h2>
                <p className="text-gray-600 dark:text-gray-400">6-month energy consumption and cost optimization tracking</p>
              </div>
            </div>
            
            <div className="h-80">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={usageMetrics}>
                  <defs>
                    <linearGradient id="costGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#3B82F6" stopOpacity={0}/>
                    </linearGradient>
                    <linearGradient id="savingsGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10B981" stopOpacity={0.3}/>
                      <stop offset="95%" stopColor="#10B981" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
                  <XAxis dataKey="month" stroke="#9CA3AF" />
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
                    dataKey="cost"
                    stroke="#3B82F6"
                    fill="url(#costGradient)"
                    name="Monthly Cost (₦)"
                  />
                  <Area
                    type="monotone"
                    dataKey="savings"
                    stroke="#10B981"
                    fill="url(#savingsGradient)"
                    name="Monthly Savings (₦)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Hardware Status */}
          <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-lg border border-gray-200 dark:border-gray-700 p-6">
            <h2 className="text-xl font-semibold text-gray-900 dark:text-white mb-4">Hardware Performance & ROI</h2>
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
              {hardwareDevices.map((device) => {
                const IconComponent = device.icon
                const status = device.isActivated ? 'active' : 'inactive'
                const StatusIcon = getStatusIcon(status)
                
                return (
                  <div key={device.id} className="border border-gray-200 dark:border-gray-700 rounded-xl p-4">
                    <div className="flex items-center space-x-3 mb-3">
                      <IconComponent className="h-6 w-6 text-gray-600 dark:text-gray-400" />
                      <div className="flex-1">
                        <p className="font-medium text-gray-900 dark:text-white">{device.name}</p>
                        <p className="text-sm text-gray-500 dark:text-gray-400">{device.macAddress}</p>
                      </div>
                      <StatusIcon className={`h-5 w-5 ${getStatusColor(status)}`} />
                    </div>
                    
                    <div className="space-y-2 text-sm">
                      <div className="flex justify-between">
                        <span className="text-gray-500 dark:text-gray-400">Efficiency:</span>
                        <span className="text-gray-900 dark:text-white">{device.efficiency}%</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500 dark:text-gray-400">Cost Savings:</span>
                        <span className="text-green-600 dark:text-green-400">₦{device.costSavings.toLocaleString()}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500 dark:text-gray-400">Maintenance:</span>
                        <span className="text-gray-900 dark:text-white">₦{device.maintenanceCost.toLocaleString()}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500 dark:text-gray-400">Last Service:</span>
                        <span className="text-gray-900 dark:text-white">{device.lastMaintenance}</span>
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>

            {!hasGateway && (
              <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4">
                <div className="flex items-start space-x-3">
                  <ExclamationTriangleIcon className="h-6 w-6 text-yellow-600 dark:text-yellow-400 flex-shrink-0 mt-1" />
                  <div>
                    <h3 className="font-semibold text-yellow-800 dark:text-yellow-200">
                      Gateway Required
                    </h3>
                    <p className="text-sm text-yellow-700 dark:text-yellow-300 mt-1">
                      A SmartWatts Gateway is required to activate your free trial. Please install and activate your gateway device.
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Billing Plans */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {billingPlans.map((plan) => {
              const IconComponent = plan.icon
              const status = getPlanStatus(plan.id)
              const StatusIcon = getStatusIcon(status)
              
              return (
                <div key={plan.id} className={`bg-white dark:bg-gray-800 rounded-2xl shadow-lg border-2 transition-all duration-300 ${
                  status === 'active' || status === 'trial'
                    ? 'border-blue-500 dark:border-blue-400'
                    : 'border-gray-200 dark:border-gray-700'
                }`}>
                  <div className="p-6">
                    <div className="flex items-center justify-between mb-4">
                      <div className={`w-12 h-12 ${getStatusColor(status).replace('text-', 'bg-').replace('dark:text-', 'dark:bg-')} rounded-xl flex items-center justify-center`}>
                        <IconComponent className="h-6 w-6 text-white" />
                      </div>
                      <div className="text-right">
                        <StatusIcon className={`h-5 w-5 ${getStatusColor(status)}`} />
                        <p className="text-xs text-gray-500 dark:text-gray-400">{getStatusText(status)}</p>
                      </div>
                    </div>
                    
                    <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">{plan.name}</h3>
                    <p className="text-gray-600 dark:text-gray-300 mb-4">{plan.description}</p>
                    
                    <div className="mb-6">
                      <span className="text-3xl font-bold text-gray-900 dark:text-white">
                        ₦{plan.price.toLocaleString()}
                      </span>
                      <span className="text-gray-500 dark:text-gray-400">/{plan.duration}</span>
                    </div>
                    
                    {plan.savings > 0 && (
                      <div className="mb-4 p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
                        <div className="flex items-center justify-between">
                          <span className="text-sm text-green-700 dark:text-green-300">Monthly Savings</span>
                          <span className="text-lg font-bold text-green-700 dark:text-green-300">₦{plan.savings.toLocaleString()}</span>
                        </div>
                        <div className="flex items-center justify-between mt-1">
                          <span className="text-sm text-green-600 dark:text-green-400">ROI</span>
                          <span className="text-sm font-medium text-green-600 dark:text-green-400">{plan.roi}%</span>
                        </div>
                      </div>
                    )}
                    
                    <div className="space-y-2 mb-6">
                      {plan.features.map((feature, index) => (
                        <div key={index} className="flex items-center space-x-2">
                          <CheckCircleIcon className="h-4 w-4 text-green-500 flex-shrink-0" />
                          <span className="text-sm text-gray-600 dark:text-gray-300">{feature}</span>
                        </div>
                      ))}
                    </div>
                    
                    <button
                      className={`w-full py-3 px-4 rounded-lg font-semibold transition-colors ${
                        status === 'active' || status === 'trial'
                          ? 'bg-green-600 hover:bg-green-700 text-white'
                          : status === 'locked'
                          ? 'bg-gray-300 dark:bg-gray-600 text-gray-500 dark:text-gray-400 cursor-not-allowed'
                          : 'bg-blue-600 hover:bg-blue-700 text-white'
                      }`}
                      disabled={status === 'locked'}
                    >
                      {status === 'active' ? 'Current Plan' : 
                       status === 'trial' ? 'Trial Active' :
                       status === 'locked' ? 'Hardware Required' :
                       'Upgrade Now'}
                    </button>
                  </div>
                </div>
              )
            })}
          </div>

          {/* Information Section */}
          <div className="bg-blue-50 dark:bg-blue-900/20 rounded-2xl p-6 border border-blue-200 dark:border-blue-800">
            <div className="flex items-start space-x-4">
              <InformationCircleIcon className="h-6 w-6 text-blue-600 dark:text-blue-400 flex-shrink-0 mt-1" />
              <div>
                <h3 className="text-lg font-semibold text-blue-900 dark:text-blue-100 mb-2">
                  Billing & Cost Management
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-blue-800 dark:text-blue-200">
                  <div>
                    <h4 className="font-semibold mb-1">1. Hardware Activation</h4>
                    <p>Install and activate certified SmartWatts devices to unlock free trial</p>
                  </div>
                  <div>
                    <h4 className="font-semibold mb-1">2. 3-Month Trial</h4>
                    <p>Enjoy full access for 3 months with activated hardware</p>
                  </div>
                  <div>
                    <h4 className="font-semibold mb-1">3. Upgrade Options</h4>
                    <p>Choose Premium or Business plans for enhanced features</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Feature Upgrade Modal */}
          <FeatureUpgradeModal
            isOpen={showUpgradeModal}
            onClose={() => setShowUpgradeModal(false)}
            featureName="Billing Dashboard"
            featureDescription="Advanced billing analytics and cost optimization features"
            currentPlan="Freemium"
          />
        </div>
      </Layout>
    </ProtectedRoute>
  )
}